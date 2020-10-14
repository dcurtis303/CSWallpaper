package ca.dcstudios.cswallpaper.renderer;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LightManager {
    public static final int MAX_LIGHTS = 4; // also defined in fragment shader

    private static final String TAG = "LightManager";

    private final float[] mPosition = new float[MAX_LIGHTS * 3];
    private final float[] mColor = new float[MAX_LIGHTS * 3];
    private final float[] mAttenuation = new float[MAX_LIGHTS];
    private final float[] mAmbCoeff = new float[MAX_LIGHTS];

    private final FloatBuffer mBufferPosition;
    private final FloatBuffer mBufferColor;

    private int mLightCount = 0;

    public LightManager() {
        ByteBuffer bb = ByteBuffer.allocateDirect(MAX_LIGHTS * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        mBufferPosition = bb.asFloatBuffer();

        bb = ByteBuffer.allocateDirect(MAX_LIGHTS * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        mBufferColor = bb.asFloatBuffer();
    }

    public int getCount() {
        return mLightCount;
    }

    public float[] getPositionsArray() {
        return mPosition;
    }

    public float[] getColorArray() {
        return mColor;
    }

    public float getAttenuation(int index) {
        return mAttenuation[index];
    }

    public float getAmbientCoefficent(int index) {
        return mAmbCoeff[index];
    }

    public void setPosition(int index, float x, float y, float z) {
        index *= 3;

        mPosition[index] = x;
        mPosition[index + 1] = y;
        mPosition[index + 2] = z;
    }

    public float[] getPosition(int index) {
        index *= 3;
        return new float[]{mPosition[index], mPosition[index + 1], mPosition[index + 2]};
    }

    public FloatBuffer getPositionBuffer() {
        mBufferPosition.put(mPosition);
        mBufferPosition.position(0);
        return mBufferPosition;
    }

    public FloatBuffer getColorBuffer() {
        mBufferColor.put(mColor);
        mBufferColor.position(0);
        return mBufferColor;
    }

    public float[] getColor(int index) {
        index *= 3;
        return new float[]{mColor[index], mColor[index + 1], mColor[index + 2]};
    }

    public void add(float[] position, float[] color, float attentuation, float ambcoeff) {
        if (mLightCount < MAX_LIGHTS) {
            int index = mLightCount * 3;
            mPosition[index] = position[0];
            mPosition[index + 1] = position[1];
            mPosition[index + 2] = position[2];

            mColor[index] = color[0];
            mColor[index + 1] = color[1];
            mColor[index + 2] = color[2];

            mAttenuation[mLightCount] = attentuation;
            mAmbCoeff[mLightCount] = ambcoeff;

            mLightCount++;
        } else
            Log.e(TAG, "MAX_LIGHT count reached, light not added.");
    }
}
