package com.example.utsavstha.raycastingtest;

/**
 * Created by utsavstha on 1/17/17.
 */

public class Coordinate {
    private float x;
    private float y;
    private float z;

    public Coordinate(){}

    public Coordinate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
