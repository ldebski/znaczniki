package com.journaldev.androidcameraxopencv;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Markers {
    private final List<Marker> markers;
    int idLeftDown = 8, idRightDown = 10, idLeftUp = 0, idRightUp = 2;

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

    // return the biggest difference between marker's height and width
    public double getMaxSideDiff(){
        Optional<Marker> maxSideDiffMarker = markers.stream().max(Comparator.comparing(Marker::getSideDiff));
        return maxSideDiffMarker.map(Marker::getSideDiff).orElse(Double.MAX_VALUE);
    }

    public double getSideDiagonalLengthDiff(){
        return Math.abs(getDiagonalLengthDiff(idLeftDown, idRightDown));
    }

    public double getFlankDiagonalLengthDiff(){
        if (markers.size() == 2 || markers.size() == 4)
            return getDiagonalLengthDiff(idLeftDown, idLeftUp);
        return 0;
    }

    private double getDiagonalLengthDiff(int id1, int id2){
        double side1 = 0, side2 = 0;
        for (int i=0;i<markers.size();i++){
            if (markers.get(i).id == id1 || markers.get(i).id == id2){
                if (side1 > 0)
                    side1 = (side1 + markers.get(i).getDiagonalLength())/2;
                else
                    side1 += markers.get(i).getDiagonalLength();
            }
            else {
                if (side2 > 0)
                    side2 = (side2 + markers.get(i).getDiagonalLength())/2;
                else
                    side2 += markers.get(i).getDiagonalLength();
            }
        }
        if (side1 == 0 || side2 == 0)
            return 0;
        return side1-side2;
    }
}
