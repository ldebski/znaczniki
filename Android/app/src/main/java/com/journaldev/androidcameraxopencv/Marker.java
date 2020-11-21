package com.journaldev.androidcameraxopencv;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import java.lang.Math;

public class Marker {
    public int id;
    public Point ver0, ver1, ver2, ver3;

    public Marker(Mat vertices, int id){
        this.id = id;
        this.ver0 = new Point(vertices.get(0, 0));
        this.ver1 = new Point(vertices.get(0, 1));
        this.ver2 = new Point(vertices.get(0, 2));
        this.ver3 = new Point(vertices.get(0, 3));
    }

    public Point getCenter(){
        return new Point((this.ver0.x + this.ver2.x)/2, (this.ver0.y + this.ver2.y)/2);
    }

    public double getWidth(){
        return Math.abs(ver0.x - ver2.x);
    }
    public double getHeight(){
        return Math.abs(ver0.y - ver2.y);
    }
}
