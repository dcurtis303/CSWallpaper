package ca.dcstudios.cswallpaper.renderer;

import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Mesh {

    private static final String TAG = "Mesh";


    // shader vars for drawing object
    private int mTextureUnit;
    private int mFaceCount;
    private FloatBuffer mBufferVertex;
    private FloatBuffer mBufferUV;
    private FloatBuffer mBufferNormal;
    private ShortBuffer mBufferDrawList;


    // initial transform
    private float[] mRotation = {0, 0, 0};
    private float[] mTranslation = {0, 0, 0};
    private float[] mScale = {1, 1, 1};
    private float[] mModelMatrix = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };


    public float[] getRotation() {
        return mRotation;
    }

    public void setRotation(float[] mRotation) {
        this.mRotation = mRotation;
    }

    public float[] getTranslation() {
        return mTranslation;
    }

    public void setTranslation(float[] mTranslation) {
        this.mTranslation = mTranslation;
    }

    public float[] getScale() {
        return mScale;
    }

    public void setScale(float[] mScale) {
        this.mScale = mScale;
    }

    public void translate(float x, float y, float z) {
        mTranslation[0] += x;
        mTranslation[1] += y;
        mTranslation[2] += z;
    }

    public void rotateX(float v) {
        mRotation[0] += v;
    }

    public void rotateY(float v) {
        mRotation[1] += v;
    }

    public void rotateZ(float v) {
        mRotation[2] += v;
    }

    public float[] getModelMatrix() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, mTranslation[0], mTranslation[1], mTranslation[2]);
        Matrix.scaleM(mModelMatrix, 0, mScale[0], mScale[1], mScale[2]);
        Matrix.rotateM(mModelMatrix, 0, mRotation[0], 1, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, mRotation[1], 0, 1, 0);
        Matrix.rotateM(mModelMatrix, 0, mRotation[2], 0, 0, 1);

        return mModelMatrix;
    }

    public FloatBuffer getVertexBuffer() {
        return mBufferVertex;
    }

    public FloatBuffer getNormalBuffer() {
        return mBufferNormal;
    }

    public FloatBuffer getUVBuffer() {
        return mBufferUV;
    }

    public int getTextureUnit() {
        return mTextureUnit;
    }

    public int getFaceCount() {
        return mFaceCount;
    }

    public ShortBuffer getBufferDrawList() {
        return mBufferDrawList;
    }

    public void setTextureUnit(int textureUnit) {
        this.mTextureUnit = textureUnit;
    }

    public void setFaceCount(int faceCount) {
        this.mFaceCount = faceCount;
    }

    public FloatBuffer setUVBuffer(FloatBuffer floatBuffer) {
        return mBufferUV = floatBuffer;
    }

    public FloatBuffer setVertexBuffer(FloatBuffer floatBuffer) {
        return mBufferVertex = floatBuffer;
    }

    public FloatBuffer setNormalBuffer(FloatBuffer floatBuffer) {
        return mBufferNormal = floatBuffer;
    }

    public ShortBuffer setDrawListBuffer(ShortBuffer shortBuffer) {
        return mBufferDrawList = shortBuffer;
    }
}
