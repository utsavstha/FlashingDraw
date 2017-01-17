package com.example.utsavstha.raycastingtest;

/**
 * Created by utsavstha on 1/15/17.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import static java.security.AccessController.getContext;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final FloatBuffer triangleVertices;

    private FloatBuffer lineVertices;

    private final int bytesPerFloat = 4;

    private float[] viewMatrix = new float[16];

    private static Context context;

    private int mMVPMatrixHandle;

    private int mPositionHandle;

    private int mColorHandle;

    private float[] mProjectionMatrix = new float[16];

    private float[] mModelMatrix = new float[16];

    private float[] mMVPMatrix = new float[16];

    private float[] mMVMatrix = new float[16];

    private int[] viewport = new int[4];

    private final int strideBytes = 7 * bytesPerFloat;
    private final int lineStrideBytes = 3 * bytesPerFloat;

    private final int positionOffset = 0;

    private final int positionDataSize = 3;

    private final int colorOffset = 3;

    private final int colorDataSize = 4;

    private float width, height;
    private List<Float> angle = new ArrayList<>();
    private float[] lineStartPoint = new float[]{0, 0, 1f};
    private float[] oldVertexData;
    private float[] lineEndPoint = new float[]{0, 0, 0};
    private float[] vertexDataf;
    private float[] cameraPos = new float[]{0f, 0f, 2.5f};
    private float[] cameraLook = new float[]{0f, 0f, -5f};
    private float[] cameraUp = new float[]{0f, 1f, 0f};
    public int size = 0;

    private final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec4 a_Color;" +
                    "varying vec4 v_Color;" +
                    "void main() {" +
                    "  v_Color = a_Color;" +
                    "   gl_Position = u_MVPMatrix * a_Position;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 v_Color;" +
                    "void main() {" +
                    "  gl_FragColor = v_Color;" +
                    "}";
    List<Float> vertexData = new ArrayList<>();
    public OpenGLRenderer(Context context) {
        this.context = context;

        final float[] triangleVerticesData = {
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        };

        triangleVertices = ByteBuffer.allocateDirect(triangleVerticesData.length * bytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        triangleVertices.put(triangleVerticesData).position(0);

        float[] lineVerticesData = {
                lineStartPoint[0], lineStartPoint[1], lineStartPoint[2],
                lineEndPoint[0], lineEndPoint[1], lineEndPoint[2]
        };
        lineVertices = ByteBuffer.allocateDirect(lineVerticesData.length * bytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
        lineVertices.put(lineVerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        Matrix.setLookAtM(viewMatrix, 0, cameraPos[0], cameraPos[1], cameraPos[2], cameraLook[0], cameraLook[1], cameraLook[2], cameraUp[0], cameraUp[1], cameraUp[2]);

        try {
            int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

            if (vertexShaderHandle != 0)
            {
                GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);

                GLES20.glCompileShader(vertexShaderHandle);

                final int[] compileStatus = new int[1];
                GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

                if (compileStatus[0] == 0)
                {
                    GLES20.glDeleteShader(vertexShaderHandle);
                    vertexShaderHandle = 0;
                }
            }

            if (vertexShaderHandle == 0)
            {
                throw new RuntimeException("Error creating vertex shader");
            }

            int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

            if (fragmentShaderHandle != 0)
            {
                GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);

                GLES20.glCompileShader(fragmentShaderHandle);

                final int[] compileStatus = new int[1];
                GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

                if (compileStatus[0] == 0)
                {
                    GLES20.glDeleteShader(fragmentShaderHandle);
                    fragmentShaderHandle = 0;
                }
            }
            if (fragmentShaderHandle == 0)
            {
                throw new RuntimeException("Error creating fragment shader.");
            }

            int programHandle = GLES20.glCreateProgram();

            if (programHandle != 0)
            {
                GLES20.glAttachShader(programHandle, vertexShaderHandle);
                GLES20.glAttachShader(programHandle, fragmentShaderHandle);

                GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
                GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

                GLES20.glLinkProgram(programHandle);

                final int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

                if (linkStatus[0] == 0)
                {
                    GLES20.glDeleteProgram(programHandle);
                    programHandle = 0;
                }
            }

            if (programHandle == 0)
            {
                throw new RuntimeException("Error creating program.");
            }

            mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
            mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
            mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

            GLES20.glUseProgram(programHandle);
        } catch (RuntimeException e)
        {
            Log.d("OpenGLES2Test", e.getMessage());
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width/2, height/2);
        DisplayMetrics displaymetrics = new DisplayMetrics();

        this.width = width;
        this.height = height;

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() % 10000L;

        GLES20.glViewport(0, 0, (int)(width), (int)(height));
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setLookAtM(viewMatrix, 0, cameraPos[0], cameraPos[1], cameraPos[2], cameraLook[0], cameraLook[1], cameraLook[2], cameraUp[0], cameraUp[1], cameraUp[2]);

        Matrix.multiplyMM(mMVMatrix, 0, viewMatrix, 0, mModelMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

       // drawTriangle(triangleVertices);
        drawIntersectionLine();
    }

    private void drawTriangle(final FloatBuffer triangleBuffer)
    {
        triangleBuffer.position(positionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, positionDataSize, GLES20.GL_FLOAT, false, strideBytes, triangleBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        triangleBuffer.position(colorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, colorDataSize, GLES20.GL_FLOAT, false, strideBytes, triangleBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    private void drawIntersectionLine()
    {
        lineVertices.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, positionDataSize, GLES20.GL_FLOAT, false, lineStrideBytes, lineVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2 * size );
    }

    private void moveIntersectionLineEndPoint(float[] lineEndPoint)
    {

        /*float[] lineVerticesData = {
                lineEndPoint[0], lineEndPoint[1], lineEndPoint[2],
                lineEndPoint[3], lineEndPoint[4], lineEndPoint[5],

        };*/
        if(vertexDataf != null){
            float[] data = new float[lineEndPoint.length + vertexDataf.length];
            System.arraycopy(vertexDataf, 0, data, 0, vertexDataf.length);
            System.arraycopy(lineEndPoint, 0, data, vertexDataf.length, lineEndPoint.length);
            // size = lineEndPoint.length;
           // Log.d("opengl","vertexList"+ Arrays.toString(lineEndPoint));
            lineVertices = ByteBuffer.allocateDirect(data.length * bytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            lineVertices.put(data).position(0);
        }else{
            lineVertices = ByteBuffer.allocateDirect(lineEndPoint.length * bytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
            lineVertices.put(lineEndPoint).position(0);
        }


    }

    public static String readShader(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filePath)));
        StringBuilder sb = new StringBuilder();
        String line;
        while( ( line = reader.readLine() ) != null)
        {
            sb.append(line + "\n");
        }
        reader.close();
        return sb.toString();
    }

    public float[] getMouseRayProjection(float touchX, float touchY, float windowWidth, float windowHeight, float[] view, float[] projection)
    {
        float[] rayDirection = new float[4];

        float normalizedX = 2f * touchX/windowWidth - 1f;
        float normalizedY = 1f - 2f*touchY/windowHeight;
        float normalizedZ = 1.0f;

        float[] rayNDC = new float[]{normalizedX, normalizedY, normalizedZ};

        float[] rayClip = new float[]{rayNDC[0], rayNDC[1], -1f, 1f};

        float[] inverseProjection = new float[16];
        Matrix.invertM(inverseProjection, 0, projection, 0);
        float[] rayEye = multiplyMat4ByVec4(inverseProjection, rayClip);

        //rayClip = new float[]{rayClip[0], rayClip[1], -1f, 0f};
        rayEye = new float[]{rayEye[0], rayEye[1], -1f, 0f};
        float[] inverseView = new float[16];
        Matrix.invertM(inverseView, 0, view, 0);
        float[] rayWorld4D = multiplyMat4ByVec4(inverseView, rayEye);
        float[] rayWorld = new float[]{rayWorld4D[0], rayWorld4D[1], rayWorld4D[2]};
        Log.d("opengl", Arrays.toString(rayWorld));
      //  rayDirection = normalizeVector3(rayWorld);

        return rayWorld;
    }

    public float[] normalizeVector3(float[] vector3)
    {
        float[] normalizedVector = new float[3];
        float magnitude = (float) Math.sqrt((vector3[0] * vector3[0]) + (vector3[1] * vector3[1]) + (vector3[2] * vector3[2]));
        normalizedVector[0] = vector3[0] / magnitude;
        normalizedVector[1] = vector3[1] / magnitude;
        normalizedVector[2] = vector3[2] / magnitude;
        return normalizedVector;
    }



    public float[] getCameraPos(float[] modelView)
    {
        float[] modelviewInverse = new float[16];
        Matrix.invertM(modelviewInverse, 0, modelView, 0);
        float[] cameraPos = new float[4];
        cameraPos[0] = modelviewInverse[12];
        cameraPos[1] = modelviewInverse[13];
        cameraPos[2] = modelviewInverse[14];
        cameraPos[3] = modelviewInverse[15];
        return cameraPos;
    }

    public String floatArrayAsString(float[] array)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Float f : array)
        {
            sb.append(f + ", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    public float[] getInverseMatrix(float[] originalMatrix)
    {
        float[] inverseMatrix = new float[16];
        Matrix.invertM(inverseMatrix, 0, originalMatrix, 0);
        return inverseMatrix;
    }

    public float[] multiplyMat4ByVec4(float[] matrix4, float[] vector4)
    {
        float[] returnMatrix = new float[4];

        returnMatrix[0] = (matrix4[0] * vector4[0]) + (matrix4[1] * vector4[1]) + (matrix4[2] * vector4[2]) + (matrix4[3] * vector4[3]);
        returnMatrix[1] = (matrix4[4] * vector4[0]) + (matrix4[5] * vector4[1]) + (matrix4[6] * vector4[2]) + (matrix4[7] * vector4[3]);
        returnMatrix[2] = (matrix4[8] * vector4[0]) + (matrix4[9] * vector4[1]) + (matrix4[10] * vector4[2]) + (matrix4[11] * vector4[3]);
        returnMatrix[3] = (matrix4[12] * vector4[0]) + (matrix4[13] * vector4[1]) + (matrix4[14] * vector4[2]) + (matrix4[15] * vector4[3]);

        return returnMatrix;
    }

    public void onTouch(float startX, float startY,float endX, float endY)
    {
       // float[] mouseRayProjection = getMouseRayProjection(touchX, touchY, width, height, mMVMatrix, mProjectionMatrix);
        float[] start = getWorldCoords(startX, startY);
        float[] end = getWorldCoords(endX, endY);

        //float[] start = new float[]{startX, startY, -1f};
       // float[] end = new float[]{endX, endY, -1f};

        float[] mouseRayProjection = new float[start.length + end.length];
        System.arraycopy(start, 0, mouseRayProjection, 0, start.length);
        System.arraycopy(end, 0, mouseRayProjection, start.length, end.length);
        Log.d("opengl", "Mouse Ray: " + floatArrayAsString(mouseRayProjection));

      /* for(int i = 0; i < mouseRayProjection.length; i++){
           vertexData.add(mouseRayProjection[i]);
       }


        List<Float> floatList = vertexData;
        float[] floatArray = new float[floatList.size()];
        int i = 0;

        for (Float f : floatList) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }*/
        moveIntersectionLineEndPoint(mouseRayProjection);
        oldVertexData = new float[mouseRayProjection.length];
        oldVertexData = mouseRayProjection;
    }
    public void onActionUp(float startX, float startY,float endX, float endY) {
        // float[] mouseRayProjection = getMouseRayProjection(touchX, touchY, width, height, mMVMatrix, mProjectionMatrix);
        /*float[] start = getWorldCoords(startX, startY);
        float[] end = getWorldCoords(endX, endY);

        //float[] start = new float[]{startX, startY, -1f};
        // float[] end = new float[]{endX, endY, -1f};

        float[] mouseRayProjection = new float[start.length + end.length];
        System.arraycopy(start, 0, mouseRayProjection, 0, start.length);
        System.arraycopy(end, 0, mouseRayProjection, start.length, end.length);*/


        for(int i = 0; i < oldVertexData.length; i++){
            vertexData.add(oldVertexData[i]);
        }


        List<Float> floatList = vertexData;
        vertexDataf = new float[floatList.size()];
        int i = 0;

        for (Float f : floatList) {
            vertexDataf[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
      //  moveIntersectionLineEndPoint(floatArray);

    }
    public float[] getWorldCoords(float x, float y)
    {
        // Initialize auxiliary variables.

        // SCREEN height & width (ej: 320 x 480)
        float screenW = width;
        float screenH = height;

        // Auxiliary matrix and vectors
        // to deal with ogl.
        float[] invertedMatrix, transformMatrix,
                normalizedInPoint, outPoint;
        invertedMatrix = new float[16];
        transformMatrix = new float[16];
        normalizedInPoint = new float[4];
        outPoint = new float[4];

        // Invert y coordinate, as android uses
        // top-left, and ogl bottom-left.
        int oglTouchY = (int) (screenH - y);

       /* Transform the screen point to clip
       space in ogl (-1,1) */
        normalizedInPoint[0] =
                (float) (x * 2.0f / screenW - 1.0);
        normalizedInPoint[1] =
                (float) ((oglTouchY) * 2.0f / screenH - 1.0);
        normalizedInPoint[2] = - 1.0f;
        normalizedInPoint[3] = 1.0f;

       /* Obtain the transform matrix and
       then the inverse. */
        //Matrix.setIdentityM(mMVMatrix, 0);
        Matrix.multiplyMM(
                transformMatrix, 0,
                mProjectionMatrix, 0,
                mMVMatrix, 0);
        Matrix.invertM(invertedMatrix, 0,
                transformMatrix, 0);

       /* Apply the inverse to the point
       in clip space */
        Matrix.multiplyMV(
                outPoint, 0,
                invertedMatrix, 0,
                normalizedInPoint, 0);
/*
        if (outPoint[3] == 0.0)
        {
            // Avoid /0 error.
            //Log.e("World coords", "ERROR!");
            return worldPos;
        }*/

        // Divide by the 3rd component to find
        // out the real position.
        /*worldPos.Set(
                outPoint[0] / outPoint[3],
                outPoint[1] / outPoint[3]);*/
       /* List<Float> value = new ArrayList<>();
        value.add(outPoint[0] / outPoint[3] *2.5f);
        value.add(outPoint[1] / outPoint[3]*2.5f);
        value.add(outPoint[0] / outPoint[3] *2.5f);*/
        float[] value = new float[]{outPoint[0] / outPoint[3] *2.5f, outPoint[1] / outPoint[3]*2.5f, -1};
        return value;
    }
    public float calculateAngle(Line one, Line two){
        float slopeOne, slopeTwo;

        slopeOne = calculateSlope(one);

        slopeTwo = calculateSlope(two);

        return (float)calulateAngle(slopeOne, slopeTwo);

    }
    private float calculateSlope(Line one){
      return   (one.getTwo().getY() - one.getOne().getY()) / (one.getTwo().getX() - one.getOne().getX());
    }

    private double calulateAngle(double m1, double m2){

       return Math.toDegrees(Math.atan2((m2 - m1), (1 + (m1 * m2))));
    }

    public void setAngle(float angle) {
        this.angle.add(angle);
    }

    public List<Float> getVertexData() {
        return vertexData;
    }
}