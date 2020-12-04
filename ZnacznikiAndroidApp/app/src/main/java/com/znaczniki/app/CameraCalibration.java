package com.znaczniki.app;

public class CameraCalibration {

    public void calibration() {
        // próba kalibracji kamery trochę nieudana
        // //Aruco.drawDetectedMarkers(mat, corners, ids);
        // Mat cameraMatrix = new Mat(3, 3, CvType.CV_32F);
        // int row = 0, col = 0;
        // double[] data = {1624.2491,0.,959.3577,0.,1614.9941,480.5490,0.,0.,1.};
        // cameraMatrix.put(row,col,data);
        //
        // Mat distort = new Mat(5,1,CvType.CV_32F);
        // row = 0;
        // col = 0;
        // double[] distort_data = {0.0353, 0.8180, 0.00, 0.00,-4.6780};
        // distort.put(row,col,distort_data);
        // Mat rvecs = new Mat(), tvecs = new Mat();
        // Aruco.estimatePoseSingleMarkers(corners, 0.03f, cameraMatrix, distort, rvecs, tvecs);
        // if(ids.height()>1){
        //        for (int i=0;i<rvecs.height();i++) {
        //            double[] rvec = rvecs.get(i, 0);
        //            double[] tvec = tvecs.get(i, 0);
        //            Mat rvec_mat = new Mat(3, 1, CvType.CV_32F);
        //            row = 0;
        //            col = 0;
        //            rvec_mat.put(row, col, rvec);
        //            Mat tvec_mat = new Mat(3, 1, CvType.CV_32F);
        //            row = 0;
        //            col = 0;
        //            tvec_mat.put(row, col, tvec);
        //            drawFrameAxes(displayCopy, cameraMatrix, distort, rvec_mat, tvec_mat, 0.1f);
        //        }
        // }
        // else if(ids.height()==1) {
        //     drawFrameAxes(displayCopy, cameraMatrix, distort, rvecs, tvecs, 0.04f, 10);
        // }

        // for (int i=0;i<rvecs.size().height;i++){
        //     double[] rvec = rvecs.get(i, 0);
        //     double[] tvec = tvecs.get(i,0);
        //     Mat rvec_mat = new Mat(3,1, CvType.CV_32F);
        //     row = 0;col = 0;
        //     rvec_mat.put(row,col,rvec);
        //     Mat tvec_mat = new Mat(3,1, CvType.CV_32F);
        //     row = 0;col = 0;
        //     tvec_mat.put(row,col,tvec);
        //     drawFrameAxes(displayCopy, cameraMatrix, distort, rvec_mat, tvec_mat, 0.1f);
        // }
        // if(corners.size() >= 1){
        //     Log.e("corners size: ", Double.toString(corners.size()));
        //     for(int i=0;i<corners.size();i++){
        //         Log.e("xzcczx", Double.toString(ids.get(i,0)[0]) + ": " + corners.get(i).dump());
        //         int xd = (int)corners.get(0).get(0,2)
        //         // Log.e("xzcczx", corners.get(i).dump());
        //     }
        // }
    }
}
