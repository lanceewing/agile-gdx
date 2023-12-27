package com.agifans.agile.agilib.jagi.awt;

public class Point {

    public int x;

    public int y;

    public Point() {
        this(0, 0);
    }

    public Point(Point p) {
        this(p.x, p.y);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
