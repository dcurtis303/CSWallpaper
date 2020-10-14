package ca.dcstudios.cswallpaper.service;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import ca.dcstudios.cswallpaper.renderer.GLRenderer;

class GLThread extends Thread {
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private static final String TAG = "GLThread";
    private static final long mSleepTimeAfterDraw = 10;
    private final GLThreadManager sGLThreadManager = new GLThreadManager();
    private EGLHelper mEglHelper;
    private SurfaceHolder mHolder;
    private GLRenderer mRenderer;
    private GLThread mEglOwner;
    private int mWidth;
    private int mHeight;
    private boolean mHaveEgl;
    private boolean mPaused;
    private boolean mDone;
    private boolean mHasSurface;
    private boolean mWaitingForSurface;
    private boolean mRequestRender;
    private boolean mEventsWaiting;
    private boolean mSizeChanged;
    private ArrayList<Runnable> mEventQueue = new ArrayList<>();
    private int mRenderMode;

    GLThread(GLSurfaceView.Renderer renderer) {
        super();

        mDone = false;
        mWidth = 0;
        mHeight = 0;
        mRequestRender = true;
        mRenderMode = RENDERMODE_WHEN_DIRTY;
        mRenderer = (GLRenderer) renderer;
    }

    @Override
    public void run() {
        setName("GLThread_" + getId());

        Log.d(TAG, "GLThread started id: " + getName());

        try {
            guardedRun();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        } finally {
            sGLThreadManager.threadExiting(this);
        }
    }

    private void guardedRun() {
        mEglHelper = new EGLHelper();
        try {
            GL10 gl = null;
            boolean sendCreateMessage = true;
            boolean sendChangedMessage = true;

            while (!isDone()) {
                int w = 0;
                int h = 0;
                boolean changed = false;
                boolean needStart = false;
                boolean eventsWaiting = false;

                synchronized (sGLThreadManager) {
                    while (true) {
                        if (mPaused) {
                            stopEglLocked();
                        }

                        if (!mHasSurface) {
                            if (!mWaitingForSurface) {
                                stopEglLocked();
                                mWaitingForSurface = true;
                                sGLThreadManager.notifyAll();
                            }
                        } else {
                            if (!mHaveEgl) {
                                if (sGLThreadManager.tryAcquireEglSurface(this)) {
                                    mHaveEgl = true;
                                    mEglHelper.start();
                                    mRequestRender = true;
                                    needStart = true;
                                }
                            }
                        }

                        if (mDone) {
                            return;
                        }

                        if (mEventsWaiting) {
                            eventsWaiting = true;
                            mEventsWaiting = false;
                            break;
                        }

                        if ((!mPaused)
                                && mHasSurface
                                && mHaveEgl
                                && (mWidth > 0)
                                && (mHeight > 0)
                                && ((mRequestRender) || (mRenderMode == RENDERMODE_CONTINUOUSLY))) {
                            changed = mSizeChanged;
                            w = mWidth;
                            h = mHeight;
                            mSizeChanged = false;
                            mRequestRender = false;
                            if (mHasSurface && mWaitingForSurface) {
                                changed = true;
                                mWaitingForSurface = false;
                                sGLThreadManager.notifyAll();
                            }
                            break;
                        }
                        sGLThreadManager.wait();
                    }
                }

                if (eventsWaiting) {
                    Runnable r;
                    while ((r = getEvent()) != null) {
                        r.run();
                        if (isDone()) {
                            return;
                        }
                    }
                }

                if (needStart) {
                    sendCreateMessage = true;
                    changed = true;
                }

                if (changed) {
                    gl = (GL10) mEglHelper.createSurface(mHolder);
                    sendChangedMessage = true;
                }

                if (sendCreateMessage) {
                    mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
                    sendCreateMessage = false;
                }
                if (sendChangedMessage) {
                    mRenderer.onSurfaceChanged(gl, w, h);
                    sendChangedMessage = false;
                }

                if ((w > 0) && (h > 0)) {
                    mRenderer.onDrawFrame(gl);
                    mEglHelper.swap();
                    Thread.sleep(mSleepTimeAfterDraw);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        } finally {
            synchronized (sGLThreadManager) {
                stopEglLocked();
                mEglHelper.finish();
            }
        }
    }

    private Runnable getEvent() {
        synchronized (this) {
            if (mEventQueue.size() > 0) {
                return mEventQueue.remove(0);
            }
        }
        return null;
    }

    private boolean isDone() {
        synchronized (sGLThreadManager) {
            return mDone;
        }
    }

    public void onPause() {
        synchronized (sGLThreadManager) {
            mPaused = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void onResume() {
        synchronized (sGLThreadManager) {
            mPaused = false;
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = surfaceHolder;
        synchronized (sGLThreadManager) {
            mHasSurface = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void surfaceChanged(int width, int height) {
        synchronized (sGLThreadManager) {
            mWidth = width;
            mHeight = height;
            mSizeChanged = true;
            sGLThreadManager.notifyAll();
        }
    }

    private void stopEglLocked() {
        if (mHaveEgl) {
            mHaveEgl = false;
            mEglHelper.destroySurface();
            sGLThreadManager.releaseEglSurface(this);
        }
    }

    public void requestExitAndWait() {
        synchronized (sGLThreadManager) {
            mDone = true;
            sGLThreadManager.notifyAll();
        }
        try {
            join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void surfaceDestryoed() {
        synchronized (sGLThreadManager) {
            mHasSurface = false;
            sGLThreadManager.notifyAll();
            while (!mWaitingForSurface && isAlive() && !mDone) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void setRenderMode(int renderMode) {
        if (!((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY))) {
            throw new IllegalArgumentException("renderMode");
        }
        synchronized (sGLThreadManager) {
            mRenderMode = renderMode;
            sGLThreadManager.notifyAll();
        }
    }

    public void queueEvent(Runnable r) {
        synchronized (this) {
            mEventQueue.add(r);
            synchronized (sGLThreadManager) {
                mEventsWaiting = true;
                sGLThreadManager.notifyAll();
            }
        }
    }

    public void requestRender() {
        synchronized (sGLThreadManager) {
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    private class GLThreadManager {
        public synchronized void threadExiting(GLThread glThread) {
            glThread.mDone = true;
            if (mEglOwner == glThread)
                mEglOwner = null;
            notifyAll();
        }

        public synchronized void releaseEglSurface(GLThread glThread) {
            if (mEglOwner == glThread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean tryAcquireEglSurface(GLThread glThread) {
            if (mEglOwner == glThread || mEglOwner == null) {
                mEglOwner = glThread;
                notifyAll();
                return true;
            }
            return false;
        }
    }
}
