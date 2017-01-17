package com.example.utsavstha.raycastingtest;

/**
 * Created by utsavstha on 1/17/17.
 */
public class Line {

    Coordinate one;
    Coordinate two;
    private float angle;
    public Line() {
    }

    public Coordinate getOne() {
        return one;
    }

    public Coordinate getTwo() {
        return two;
    }

    public float getAngle() {
        return angle;
    }

    public void setOne(Coordinate one) {
        this.one = one;
    }

    public void setTwo(Coordinate two) {
        this.two = two;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}