package ca.dcstudios.cswallpaper.service;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView.Renderer;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import ca.dcstudios.cswallpaper.renderer.GLRenderer;

public class GLWallpaperService extends WallpaperService {
    public static final boolean WANT_DEBUG = false;

    public GLWallpaperService() {
        if (WANT_DEBUG)
            android.os.Debug.waitForDebugger();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    public Renderer getNewRenderer(GLEngine glEngine) {
        return new GLRenderer(this, glEngine);
    }

    public interface TouchControl {
        void onStart(int x, int y);

        void onMove(int x, int y);

        void onEnd(int x1, int y1, int x2, int y2);
    }

    public class GLEngine extends Engine {
        private static final String TAG = "GLEngine";
        private GLThread mGLThread;

        private GLRenderer mRenderer;
        private int mInitialTouchX;
        private int mInitialTouchY;

        public GLEngine() {
            super();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

            Log.d(TAG, "supportsEs2: " + supportsEs2);
            Log.d(TAG, "getGlEsVersion: " + configurationInfo.getGlEsVersion());

            setTouchEventsEnabled(true);

            mRenderer = (GLRenderer) getNewRenderer(this);
            setRenderer(mRenderer);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            Log.d(TAG, "onSurfaceRedrawNeeded");
            mGLThread.requestRender();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mInitialTouchX = x;
                    mInitialTouchY = y;
                    mRenderer.onStart(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mRenderer.onMove(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    mRenderer.onEnd(mInitialTouchX, mInitialTouchY, x, y);
                    break;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mGLThread.requestExitAndWait();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
            Log.d(TAG, "onSurfaceCreated");
            mGLThread.surfaceCreated(surfaceHolder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.d(TAG, "onSurfaceChanged");
            mGLThread.surfaceChanged(width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mGLThread.surfaceDestryoed();
        }

        private void setRenderer(Renderer renderer) {
            mGLThread = new GLThread(renderer);
            mGLThread.start();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                onResume();
            } else {
                onPause();
            }
            super.onVisibilityChanged(visible);
        }

        public void onPause() {
            mGLThread.onPause();
        }

        public void onResume() {
            mGLThread.onResume();
        }

        public void requestRender() {
            mGLThread.requestRender();
        }

        public void setRenderMode(int renderMode) {
            mGLThread.setRenderMode(renderMode);
        }

/*
        public void queueEvent(Runnable r) {
            mGLThread.queueEvent(r);
        }
*/

    }
}

