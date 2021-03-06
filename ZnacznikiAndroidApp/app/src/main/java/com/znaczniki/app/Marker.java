package com.znaczniki.app;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Marker {
    public int id;
    public Point ver0, ver1, ver2, ver3;
    public Point center;

    // vertices are always in clockwise order
    public Marker(Mat vertices, int id) {
        this.id = id;
        this.ver0 = new Point(vertices.get(0, 0));
        this.ver1 = new Point(vertices.get(0, 1));
        this.ver2 = new Point(vertices.get(0, 2));
        this.ver3 = new Point(vertices.get(0, 3));
        this.center = new Point((this.ver0.x + this.ver2.x) / 2, (this.ver0.y + this.ver2.y) / 2);
    }

    public double getWidth() {
        return Math.abs(ver0.x - ver2.x);
    }

    public double getHeight() {
        return Math.abs(ver0.y - ver2.y);
    }

    public double getDiagonalLength() {
        return Math.sqrt(Math.pow(this.ver0.x - this.ver2.x, 2) + Math.pow(this.ver0.x - this.ver2.x, 2));
    }

    public double getSideDiff() {
        return Math.abs(getWidth() - getHeight());
    }
}
