#include<opencv2/opencv.hpp>
#include<iostream>
#include<fstream>
#include <opencv2/aruco.hpp>
#include <string> 
#include <chrono>

using namespace std;
using namespace cv;


void createMarker(int markerNo) {
	cv::Mat markerImage;
	cv::Ptr<cv::aruco::Dictionary> dictionary = cv::aruco::getPredefinedDictionary(cv::aruco::DICT_4X4_250);
	cv::aruco::drawMarker(dictionary, markerNo, 200, markerImage, 1);
	cv::imwrite("markers/marker"+to_string(markerNo)+".png", markerImage);
}


int main()
{
	Mat inputImage = imread("images/camera_photo_2.jpg");
	vector<int> markerIds;
	std::vector<std::vector<cv::Point2f>> markerCorners, rejectedCandidates;
	cv::Ptr<cv::aruco::DetectorParameters> parameters = cv::aruco::DetectorParameters::create();
	cv::Ptr<cv::aruco::Dictionary> dictionary = cv::aruco::getPredefinedDictionary(cv::aruco::DICT_4X4_250);
	
	ofstream time_results;
	time_results.open("czas.txt");
	for (int i = 0; i < 20; i++) {
		auto start = chrono::high_resolution_clock::now();
		cv::aruco::detectMarkers(inputImage, dictionary, markerCorners, markerIds, parameters, rejectedCandidates);
		auto stop = chrono::high_resolution_clock::now();
		auto duration = chrono::duration_cast<chrono::milliseconds>(stop - start);
		time_results << to_string(duration.count()) << " miliseconds" << endl;
	}
	time_results.close();

	cv::Mat outputImage = inputImage.clone();
	cv::aruco::drawDetectedMarkers(outputImage, markerCorners, markerIds);
	

	//finding the center
	if (markerIds.size() > 3) {
		float distance;
		float totalX = 0.0, totalY = 0.0;
		std::vector<cv::Point2f> centers;

		for (int i = 0; i < markerIds.size(); i++) {
			centers.push_back(Point2f(float((markerCorners[i][0].x + markerCorners[i][1].x+ markerCorners[i][2].x + markerCorners[i][3].x) / 4),
				float((markerCorners[i][0].y + markerCorners[i][1].y+ markerCorners[i][2].y + markerCorners[i][3].y) / 4)));
		}
		totalX = float((centers[0].x + centers[1].x+ centers[2].x + centers[3].x )/ 4);
		totalY = float((centers[0].y + centers[1].y+ centers[2].y + centers[3].y) / 4);
		cv::circle(outputImage,cv::Point(totalX, totalY), 5, (0, 0, 255), -1);
	}

	/*pose estimation
	cv::Mat cameraMatrix, distCoeffs;
	
	std::vector<cv::Vec3d> rvecs, tvecs;
	cv::aruco::estimatePoseSingleMarkers(markerCorners, 0.05, cameraMatrix, distCoeffs, rvecs, tvecs);
	inputImage.copyTo(outputImage);
	for (int i = 0; i < rvecs.size(); ++i) {
		auto rvec = rvecs[i];
		auto tvec = tvecs[i];
		cv::aruco::drawAxis(outputImage, cameraMatrix, distCoeffs, rvec, tvec, 0.1);
	}
	*/
	//saving image
	cv::imwrite("output_images/camera_2.jpg", outputImage);
	return 0;
}
