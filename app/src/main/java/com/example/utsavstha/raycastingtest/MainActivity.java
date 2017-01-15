package com.example.utsavstha.raycastingtest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity {

    private MyGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new MyGLSurfaceView(this);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        OpenGLRenderer renderer = new OpenGLRenderer(this);
        mGLSurfaceView.setRenderer(renderer);
        mGLSurfaceView.renderer = renderer;

        setContentView(mGLSurfaceView);
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

    public float previousX, previousY;

    public MyGLSurfaceView(Context context)
    {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        float x = e.getX();
        float y = e.getY();

        switch(e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;

                renderer.onTouch(x, y);
        }

        previousX = x;
        previousY = y;
        return true;
    }
}