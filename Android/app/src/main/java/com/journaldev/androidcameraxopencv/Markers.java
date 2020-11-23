package com.journaldev.androidcameraxopencv;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Markers {
    List<Marker> markers;

    public Markers(List<Mat> mats, Mat ids){
        markers = new ArrayList<>();
        for(int i=0;i<mats.size();i++){
            markers.add(new Marker(mats.get(i), (int) ids.get(i, 0)[0]));
        }
    }

    public int size(){
        return markers.size();
    }

    public Marker get(int i){
        return markers.get(i);
    }

    public double getCircleRadiusEstimate(){
        double sum = 0;
        for (int i=0;i<markers.size();i++)
            sum += (markers.get(i).getHeight() + markers.get(i).getWidth())/2;
        return sum/markers.size()*2;
    }

    // returns difference between markers biggest and smallest diagonal
    public double getMaxDiagonalDiff() {
        Optional<Marker> minDiagonalMarker = markers.stream().min(Comparator.comparing(Marker::getDiagonalLength));
        Optional<Marker> maxDiagonalMarker = markers.stream().max(Comparator.comparing(Marker::getDiagonalLength));

        if (!minDiagonalMarker.isPresent() || !maxDiagonalMarker.isPresent())
            return Double.MAX_VALUE;
        return maxDiagonalMarker.get().getDiagonalLength() - minDiagonalMarker.get().getDiagonalLength();
    }
}
