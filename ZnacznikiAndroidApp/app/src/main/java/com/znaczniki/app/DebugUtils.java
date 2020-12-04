package com.znaczniki.app;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DebugUtils {
    Markers markers;

    public DebugUtils(Markers markers) {
        this.markers = markers;
    }

    public void DrawMarkersInfo(Mat display, boolean id, boolean width, boolean height, boolean centerPosition) {
        for (int i = 0; i < markers.size(); i++) {
            String s;
            double x = markers.get(i).center.x;
            double y = markers.get(i).center.y;
            if (id) {
                s = "Id: " + markers.get(i).id + "\n";
                Imgproc.putText(display, s, new Point(x, y), 1, 3, new Scalar(130, 0, 0), 4);
                y += 40;
            }
            if (width) {
                s = "Width: " + markers.get(i).getWidth() + "\n";
                Imgproc.putText(display, s, new Point(x, y), 1, 3, new Scalar(130, 0, 0), 4);
                y += 40;
            }
            if (height) {
                s = "Height: " + markers.get(i).getHeight() + "\n";
                Imgproc.putText(display, s, new Point(x, y), 1, 3, new Scalar(130, 0, 0), 4);
                y += 40;
            }
            if (centerPosition) {
                s = "center:(" + markers.get(i).center.x + ", " + markers.get(i).center.y + ")";
                Imgproc.putText(display, s, new Point(x, y), 1, 3, new Scalar(130, 0, 0), 4);
            }
        }
    }

    public void DrawMarkersBox(Mat display) {
        for (int i = 0; i < markers.size(); i++) {
            Imgproc.line(display, markers.get(i).ver0, markers.get(i).ver1, new Scalar(0, 255, 0), 3);
            Imgproc.line(display, markers.get(i).ver1, markers.get(i).ver2, new Scalar(0, 255, 0), 3);
            Imgproc.line(display, markers.get(i).ver2, markers.get(i).ver3, new Scalar(0, 255, 0), 3);
            Imgproc.line(display, markers.get(i).ver3, markers.get(i).ver0, new Scalar(0, 255, 0), 3);
        }
    }

    public void PrintMarkersInfo(boolean width, boolean height, boolean centerPosition, boolean diagonalLength) {
        StringBuilder s = new StringBuilder();
        s.append("Detected " + markers.size() + " markers: ");
        for (int i = 0; i < markers.size(); i++) {
            s.append("ID: " + markers.get(i).id + " ");
            if (width)
                s.append("Width: " + markers.get(i).getWidth() + " ");
            if (height)
                s.append("Width: " + markers.get(i).getHeight() + " ");
            if (centerPosition)
                s.append("Center:(" + markers.get(i).center.x + ", " + markers.get(i).center.y + ")" + " ");
            if (diagonalLength)
                s.append("Diagonal: " + markers.get(i).getDiagonalLength() + ")");
            s.append("\n                  ");
        }
        Log.e("", s.toString());
    }

    public void DrawMiddleCircle(Mat display) {
        Point circleMiddle = new Point();
        for (int i = 0; i < markers.size(); i++) {
            for (int j = 0; j < markers.size(); j++) {
                if ((markers.get(i).id == 0 && markers.get(j).id == 10) ||
                        (markers.get(i).id == 2 && markers.get(j).id == 8)) {
                    circleMiddle.x = (markers.get(i).center.x + markers.get(j).center.x) / 2;
                    circleMiddle.y = (markers.get(i).center.y + markers.get(j).center.y) / 2;
                }
            }
        }
        if (circleMiddle.y == 0 && circleMiddle.x == 0)
            return;
        double radius = markers.getCircleRadiusEstimate();
        Imgproc.circle(display, circleMiddle, (int) radius, new Scalar(255, 0, 0), 15);
    }

    public double getCircleRadiusEstimate() {
        double sum = 0;
        for (int i = 0; i < markers.size(); i++)
            sum += (markers.get(i).getHeight() + markers.get(i).getWidth()) / 2;
        return sum / markers.size() * 2;
    }
}

