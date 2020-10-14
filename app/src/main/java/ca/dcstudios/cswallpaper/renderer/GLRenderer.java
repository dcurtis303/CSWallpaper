package ca.dcstudios.cswallpaper.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ca.dcstudios.cswallpaper.service.GLWallpaperService;

public class GLRenderer implements GLSurfaceView.Renderer, GLWallpaperService.TouchControl {
    private static final String TAG = "GLRenderer";
    private static final float CAM_DISTANCE = 4.0f;
    private static final long mSomeTunableNumber = 24;
    private final LightManager mLightManager;
    private final Context mContext;
    private final GLWallpaperService.GLEngine mGLEngine;
    private final TextureManager mTextureManager;
    private final GLSurfaceView mGLSurfaceView;
    private final ArrayList<Mesh> mStaticMeshes = new ArrayList<>();
    private final AnimatedMeshes mAnimatedMeshes = new AnimatedMeshes();
    private int mDrawCallCount = 0;
    private float[] mCamPos = new float[]{0f, 2.2f, CAM_DISTANCE};
    private boolean initDone = false;
    private float[] mCamLookAt = new float[]{0f, 1.8f, 0f};
    private float[] mtrxProjection = new float[16];
    private float[] mtrxView = new float[16];
    private float[] mtrxProjectionAndView = new float[16];
    private int mHeight;
    private int mWidth;
    private long mLastTime;
    private boolean mTouch_Down = false;
    private int mTouch_PosX;
    private int mTouch_Init_PosX;
    private float mTouch_px;
    private long mElapsedCount;
    private boolean mAnimating = false;
    private long mAnimationStartTime;
    private int mCurrentAnimatedMesh = 0; // there can be only one

    public GLRenderer(Context context, GLWallpaperService.GLEngine glEngine) {
        mContext = context;
        mGLEngine = glEngine;
        mGLSurfaceView = null;

        mTextureManager = new TextureManager(mContext);
        mLightManager = new LightManager();

        init();
    }

    public GLRenderer(Context context, GLSurfaceView glSurfaceView) {
        mContext = context;
        mGLEngine = null;
        mGLSurfaceView = glSurfaceView;

        mTextureManager = new TextureManager(mContext);
        mLightManager = new LightManager();

        init();
    }

    private void init() {
        mStaticMeshes.add(MeshFactory.CreateMesh_OBJ(mContext, "world.obj", 1));

        mAnimatedMeshes.add(MeshFactory.CreateMesh_Animated(mContext, "anim/model_base", 2));
        mAnimatedMeshes.add(MeshFactory.CreateMesh_Animated(mContext, "anim/anim_backflip", 2));
        mAnimatedMeshes.add(MeshFactory.CreateMesh_Animated(mContext, "anim/anim_pieces", 2));
        mAnimatedMeshes.add(MeshFactory.CreateMesh_Animated(mContext, "anim/anim_dab", 2));
        mAnimatedMeshes.setVisible(mCurrentAnimatedMesh);

        mLightManager.add(new float[]{0f, 2.0f, 4.0f}, new float[]{1.0f, 1.0f, 1.0f}, 0.5f, 0.01f);
        mLightManager.add(new float[]{0f, 5.0f, 0.0f}, new float[]{1.0f, 1.0f, 1.0f}, 0.5f, 0.1f);
        mLightManager.add(new float[]{0f, 2.0f, -4.0f}, new float[]{1.0f, 1.0f, 1.0f}, 0.5f, 0.01f);

        initDone = true;
        Log.d(TAG, "GLRenderer created: " + this.toString());

        // start background thread
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new BackgroundThread(), 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        ShaderManager.init(mContext);

        mTextureManager.LoadTextures();

        Log.d(TAG, "OpenGL ES initialization complete!");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mWidth = width;
        mHeight = height;
        calculateMVPMatrix();

        Log.d(TAG, String.format("onSurfaceChanged: %d X %d", mWidth, mHeight));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        ++mDrawCallCount;
        Log.d(TAG, String.format("onDrawFrame(%d)", mDrawCallCount));

        if (!initDone) return;

        long now = System.currentTimeMillis();
        if (now < mLastTime) return;
        long elapsed = now - mLastTime;
        mLastTime = now;
        update(elapsed);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int program = ShaderManager.getProgram(ShaderManager.ShaderName.mo7);
        GLES20.glUseProgram(program);

        for (Mesh d : mStaticMeshes)
            draw_MO7(d, program, mtrxProjectionAndView);

        for (AnimatedMesh am : mAnimatedMeshes)
            if (am.isVisible())
                draw_MO7(am.getCurrentMesh(), program, mtrxProjectionAndView);
    }

    @Override
    public void onStart(int x, int y) {
        mTouch_Down = true;
        mTouch_Init_PosX = x;
        mTouch_PosX = x;

        if (!mAnimating && mAnimatedMeshes.size() > 0) {
            mCurrentAnimatedMesh = new Random().nextInt(mAnimatedMeshes.size());
            mAnimatedMeshes.setVisible(mCurrentAnimatedMesh);
            mAnimating = true;
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        }
    }

    @Override
    public void onMove(int x, int y) {
        mTouch_PosX = x;
        requestRender();
    }

    @Override
    public void onEnd(int x1, int y1, int x2, int y2) {
        mTouch_Down = false;
    }

    private void calculateMVPMatrix() {
        float aspect = (float) mWidth / mHeight;
        Matrix.frustumM(mtrxProjection, 0, -aspect, aspect, -1, 1, 1, 50f);
        Matrix.setLookAtM(mtrxView, 0, mCamPos[0], mCamPos[1], mCamPos[2], mCamLookAt[0], mCamLookAt[1], mCamLookAt[2], 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }

    private void update(long elapsed) {
        if (mAnimating) {
            if (mAnimationStartTime == 0) {
                mAnimationStartTime = System.currentTimeMillis();
            }
            mElapsedCount += elapsed;
            if (mElapsedCount > mSomeTunableNumber) {
                mElapsedCount = 0;
                if (mAnimatedMeshes.get(mCurrentAnimatedMesh).incrementFrame()) {
                    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    Log.d(TAG, String.format("Animation finished in: %dms",
                            System.currentTimeMillis() - mAnimationStartTime));
                    mAnimating = false;
                    mAnimationStartTime = 0;
                }
            }
        }

        // Move Camera
        if (mTouch_Down) {
            mTouch_px += (float) (mTouch_Init_PosX - mTouch_PosX) / mWidth * 2.0f;
            mTouch_px = mTouch_px > 6.2832f ? mTouch_px - 6.2832f : mTouch_px;
            mTouch_Init_PosX = mTouch_PosX;
            mCamPos[0] = (float) Math.sin(mTouch_px) * CAM_DISTANCE;
            mCamPos[2] = (float) Math.cos(mTouch_px) * CAM_DISTANCE;
            calculateMVPMatrix();
        }
    }

    private void draw_MO7(Mesh d, int program, float[] matrixMVP) {
        if (d == null) return;

        int hPosition = GLES20.glGetAttribLocation(program, "a_position");
        int hNormal = GLES20.glGetAttribLocation(program, "a_normal");
        int hTexCoord = GLES20.glGetAttribLocation(program, "a_texCoord");
        int hMVPMatrix = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        int hMMatrix = GLES20.glGetUniformLocation(program, "u_MMatrix");
        int hCamPosition = GLES20.glGetUniformLocation(program, "cameraPosition");
        int hMaterialShininess = GLES20.glGetUniformLocation(program, "materialShininess");
        int hMaterialSpecularColor = GLES20.glGetUniformLocation(program, "materialSpecularColor");
        int mSamplerLoc = GLES20.glGetUniformLocation(program, "s_texture");

        GLES20.glVertexAttribPointer(hPosition, 3, GLES20.GL_FLOAT, false, 0, d.getVertexBuffer());
        GLES20.glVertexAttribPointer(hNormal, 3, GLES20.GL_FLOAT, false, 0, d.getNormalBuffer());
        GLES20.glVertexAttribPointer(hTexCoord, 2, GLES20.GL_FLOAT, false, 0, d.getUVBuffer());
        GLES20.glUniform3fv(hCamPosition, 1, mCamPos, 0);
        GLES20.glUniform1f(hMaterialShininess, 10.1f);
        GLES20.glUniform3f(hMaterialSpecularColor, 1.0f, 1.0f, 1.0f);
        GLES20.glUniform1i(mSamplerLoc, d.getTextureUnit());

        float[] t1 = d.getModelMatrix();
        GLES20.glUniformMatrix4fv(hMMatrix, 1, false, t1, 0);
        float[] t2 = new float[16];
        Matrix.multiplyMM(t2, 0, matrixMVP, 0, t1, 0);
        GLES20.glUniformMatrix4fv(hMVPMatrix, 1, false, t2, 0);

        for (int i = 0; i < mLightManager.getCount(); i++) {
            int hLPosition = GLES20.glGetUniformLocation(program, "light[" + i + "].position");
            int hLColor = GLES20.glGetUniformLocation(program, "light[" + i + "].color");
            int hLAttenuation = GLES20.glGetUniformLocation(program, "light[" + i + "].attenuation");
            int hLAmbCoeff = GLES20.glGetUniformLocation(program, "light[" + i + "].ambientCoeff");

            GLES20.glUniform3fv(hLPosition, 1, mLightManager.getPosition(i), 0);
            GLES20.glUniform3fv(hLColor, 1, mLightManager.getColor(i), 0);
            GLES20.glUniform1f(hLAttenuation, mLightManager.getAttenuation(i));
            GLES20.glUniform1f(hLAmbCoeff, mLightManager.getAmbientCoefficent(i));
        }

        GLES20.glEnableVertexAttribArray(hTexCoord);
        GLES20.glEnableVertexAttribArray(hNormal);
        GLES20.glEnableVertexAttribArray(hPosition);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, d.getFaceCount() * 3, GLES20.GL_UNSIGNED_SHORT, d.getBufferDrawList());

        GLES20.glDisableVertexAttribArray(hPosition);
        GLES20.glDisableVertexAttribArray(hNormal);
        GLES20.glDisableVertexAttribArray(hTexCoord);
    }

    private void setRenderMode(int renderMode) {
        if (mGLEngine != null)
            mGLEngine.setRenderMode(renderMode);
        else if (mGLSurfaceView != null)
            mGLSurfaceView.setRenderMode(renderMode);
    }

    private void requestRender() {
        if (mGLEngine != null)
            mGLEngine.requestRender();
        else if (mGLSurfaceView != null)
            mGLSurfaceView.requestRender();
    }

    class BackgroundThread implements Runnable {
        private int mRunCount = 0;
        private long mLastTime;

        @Override
        public void run() {
            if (++mRunCount == 1) {
                Log.d(TAG, String.format("First run - %s", Thread.currentThread().getName()));
                mLastTime = System.currentTimeMillis();
            } else {
                long cur = System.currentTimeMillis();
                long dif = mLastTime - cur;

                if (!mAnimating && !mTouch_Down) {
                    mCamPos[0] = 0f;
                    mCamPos[1] = 2.2f;
                    mCamPos[2] = CAM_DISTANCE;
                    calculateMVPMatrix();
                    requestRender();
                }

                Log.d(TAG, String.format("Background thread (%d) (%d)", mRunCount, dif));
                mLastTime = cur;
            }
        }
    }
}
