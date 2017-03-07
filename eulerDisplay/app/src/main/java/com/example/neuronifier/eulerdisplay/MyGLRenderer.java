package com.example.neuronifier.eulerdisplay;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.hardware.SensorManager;

import static android.hardware.SensorManager.getRotationMatrixFromVector;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.sqrt;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    //private Triangle mTriangle;
    private Square   mSquare;
    float qw = 0;
    float qx = 0;
    float qy = 0;
    float qz = 0;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] quat = new float[4];

    private float mAngle;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //mTriangle = new Triangle();
        mSquare   = new Square();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -7, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation for the triangle
        Matrix.setIdentityM(scratch, 0);

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

       // Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 1.0f, 0);

       /* float n = (float) (1.0f/ sqrt(qx*qx+qy*qy+qz*qz+qw*qw));
        qx *= n;
        qy *= n;
        qz *= n;
        qw *= n;
        //Matrix.setRotateM(mRotationMatrix, 0, qw, qx, qy, qz);

       float xx      = qx*qx;
       float xy      = qx * qy;
       float xz      = qx * qz;
       float xw      = qx * qw;

       float yy      = qy * qy;
       float yz      = qy * qz;
       float yw      = qy * qw;

       float zz      = qz * qz;
       float zw      = qz * qw;

        mRotationMatrix[0]  = 1 - 2 * ( yy + zz );
        mRotationMatrix[1]  =     2 * ( xy - zw );
        mRotationMatrix[2]  =     2 * ( xz + yw );

        mRotationMatrix[4]  =     2 * ( xy + zw );
        mRotationMatrix[5]  = 1 - 2 * ( xx + zz );
        mRotationMatrix[6]  =     2 * ( yz - xw );

        mRotationMatrix[8]  =     2 * ( xz - yw );
        mRotationMatrix[9]  =     2 * ( yz + xw );
        mRotationMatrix[10] = 1 - 2 * ( xx + yy );

        mRotationMatrix[3]  = mRotationMatrix[7] = mRotationMatrix[11] = mRotationMatrix[12] = mRotationMatrix[13] = mRotationMatrix[14] = 0;
        mRotationMatrix[15] = 1;*/
        quat[0] = qw;
        quat[1] = qx;
        quat[2] = qy;
        quat[3] = qz;

        getRotationMatrixFromVector(mRotationMatrix, quat);
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw triangle
        mSquare.draw(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void newEulers(float w, float x, float y , float z){
        qw = w;
        qx= x;
        qy = y;
        qz = z;
    }

}