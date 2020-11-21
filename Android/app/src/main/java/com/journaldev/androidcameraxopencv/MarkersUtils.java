package com.journaldev.androidcameraxopencv;

import org.opencv.core.Point;

import org.opencv.core.Mat;

import java.util.List;
import java.lang.Math;

public class MarkersUtils {
    Marker mark1, mark2;

    public MarkersUtils(List<Mat> mats, Mat ids) {
        for (int i = 0; i < mats.size(); i++) {
            for (int j = 0; j < mats.size(); j++) {
                if (((int) ids.get(i, 0)[0] == 0 && (int) ids.get(j, 0)[0] == 10) ||
                    ((int) ids.get(i, 0)[0] == 2 && (int) ids.get(j, 0)[0] == 8)) {
                    mark1 = new Marker(mats.get(i), (int) ids.get(i, 0)[0]);
                    mark2 = new Marker(mats.get(j), (int) ids.get(j, 0)[0]);
                }
            }
        }
    }

    public boolean validate(){
        return mark1 != null && mark2 != null;
    }

    public Point getCircleMiddle(){
        return new Point((mark1.getCenter().x + mark2.getCenter().x)/2,
                         (mark1.getCenter().y + mark2.getCenter().y)/2);
    }

    public double getRadius(){
        return mark1.getWidth()*2;
    }

}
