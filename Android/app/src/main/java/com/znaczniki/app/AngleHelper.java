package com.znaczniki.app;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AngleHelper {
    Markers markers;
    double diagonalAcceptThreshold;
    double sideAcceptThreshold;
    double sidesDiffThreshold;
    double flanksDiffThreshold;
    int searchRange;

    List<Information> lastMessages;

    public AngleHelper(Markers markers, List<Information> lastMessages, Mat display) {
        this.markers = markers;
        this.lastMessages = lastMessages;
        float displaySize = (float) display.width() * (float) display.height() / 10000;
        this.diagonalAcceptThreshold = displaySize * 0.35;
        this.sideAcceptThreshold = displaySize * 0.1;
        this.sidesDiffThreshold = displaySize * 0.2;
        this.flanksDiffThreshold = displaySize * 0.35;
        this.searchRange = 10;
    }

    public static void DrawInformation(Mat display, Information information) {
        if (information == AngleHelper.Information.NONE)
            return;

        Point displayCenter = new Point((float) display.width() / 2, (float) display.height() / 2);
        Imgproc.putText(display, information.toString(), displayCenter,
                1, 3, new Scalar(130, 0, 0), 4);
    }

    public Information GetAngleHelper() {
        boolean detected = false;
        if (markers.size() == 4 && markers.getMaxDiagonalDiff() < diagonalAcceptThreshold)
            detected = true;
        if (markers.size() == 3 &&
                markers.getMaxDiagonalDiff() < diagonalAcceptThreshold &&
                markers.getMaxSideDiff() < sideAcceptThreshold)
            detected = true;

        if (detected)
            return getInformation(Information.PERFECT);

        return getInformation(GetHelp());
    }

    public Information GetHelp() {
        double sidesDiff = markers.getSideDiagonalLengthDiff();
        double flanksDiff = markers.getFlankDiagonalLengthDiff();

        if (sidesDiff == 0 && flanksDiff == 0)
            return Information.NONE;

        if (sidesDiff > sidesDiffThreshold && Math.abs(flanksDiff) > flanksDiffThreshold) {
            if (flanksDiff < 0)
                return Information.UP_LEFT;
            return Information.UP_RIGHT;
        } else if (sidesDiff > sidesDiffThreshold) {
            return Information.UP;
        } else if (Math.abs(flanksDiff) > flanksDiffThreshold) {
            if (flanksDiff < 0)
                return Information.LEFT;
            return Information.RIGHT;
        }

        return Information.NONE;
    }

    public Information getInformation(Information information) {
        lastMessages.add(information);

        if (lastMessages.size() > searchRange)
            information = getMostOccurringLately(searchRange);

        return information;
    }

    public Information getMostOccurringLately(int searchRange) {
        Map<Information, Integer> map = new HashMap<>();
        for (int i = 1; i < searchRange; i++) {
            Information information = lastMessages.get(lastMessages.size() - i);
            if (map.containsKey(information))
                map.put(information, map.get(information) + 1);
            else
                map.put(information, 1);
        }
        Map.Entry<Information, Integer> maxEntry = null;
        for (Map.Entry<Information, Integer> entry : map.entrySet()) {
            if ((maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                    && entry.getKey() != Information.NONE) {
                maxEntry = entry;
            }
        }
        if (maxEntry == null)
            return Information.NONE;
        return maxEntry.getKey();
    }

    enum Information {
        NONE,
        PERFECT,
        LEFT,
        RIGHT,
        UP,
        UP_LEFT,
        UP_RIGHT
    }
}
