package com.example.utsavstha.raycastingtest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private MyGLSurfaceView mGLSurfaceView;
    public static TextView angle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.surfaceView);
        angle = (TextView) findViewById(R.id.angle);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        OpenGLRenderer renderer = new OpenGLRenderer(this);
        mGLSurfaceView.setRenderer(renderer);
        mGLSurfaceView.renderer = renderer;
        //setContentView(mGLSurfaceView);
    }



    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
}

class MyGLSurfaceView extends GLSurfaceView {

    public OpenGLRenderer renderer;

    public float previousX = 0, previousY = 0;
    int count = 0;
    float oldX = 0, oldY = 0;
    public MyGLSurfaceView(Context context)
    {
        super(context);
    }
    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        float x = e.getX();
        float y = e.getY();


        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                renderer.size++;
                if(previousX == 0 && previousY == 0){
                    previousX = x;
                    previousY = y;
                }
                if(count ==0){
                    oldX = x;
                    oldY = y;
                    count++;
                }

               // renderer.onTouch(previousX, previousY,e.getX(), e.getY());

                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;

                renderer.onTouch(previousX, previousY,e.getX(), e.getY());
               // renderer.onTouch(-1.198502f, -0.34644192f, 0.44943827f, 0.2771536f);
                //renderer.onTouch(-3f, -3f, 3f, -3f);
                Log.d("opengl","Touch"+ Arrays.toString(renderer.getWorldCoords(e.getX(), e.getY())));
                break;
            case MotionEvent.ACTION_UP:
                renderer.onActionUp(previousX, previousY,e.getX(), e.getY());
                previousX = e.getX();
                previousY = e.getY();
                Line one = new Line();
                Line two = new Line();


                /*one.setOne(new Coordinate(1,2));
                one.setTwo(new Coordinate(3,4));
                two.setOne(new Coordinate(3,4));
                two.setTwo(new Coordinate(2,5));*/
                one.setOne(new Coordinate(oldX, oldY));
                one.setTwo(new Coordinate(previousX,previousY));
                two.setOne(new Coordinate(previousX,previousY));
                two.setTwo(new Coordinate(e.getX(),e.getY()));

                if(count > 0){
                    float ang =  renderer.calculateAngle(one, two);
                    MainActivity.angle.setText("angle is: "+ ang);
                    renderer.setAngle(ang);
                }


                //Toast.makeText(getContext(), , Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }
}