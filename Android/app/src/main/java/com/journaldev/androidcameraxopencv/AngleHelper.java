package com.journaldev.androidcameraxopencv;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class AngleHelper {
    Markers markers;
    double acceptThreshold = 50;

    public AngleHelper(Markers markers){
        this.markers = markers;
    }

    public void DrawAngleHelper(Mat display){
        if (markers.size() > 2){
            Log.e("Max diagonal diff", Double.toString(markers.getMaxDiagonalDiff()));
            if(markers.getMaxDiagonalDiff() < acceptThreshold)
                DrawInformation(display, Information.PERFECT);
            else
                DrawInformation(display, Information.BAD);
        }
    }

    public void DrawInformation(Mat display, Information information){
        Point displayCenter = new Point((float)display.width()/2, (float)display.height()/2);
        Imgproc.putText(display, information.toString(), displayCenter,
                1, 3, new Scalar(130, 0, 0), 4);
    }

    enum Information{
        PERFECT,
        BAD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
