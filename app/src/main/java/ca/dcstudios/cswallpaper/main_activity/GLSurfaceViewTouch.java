package ca.dcstudios.cswallpaper.main_activity;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import ca.dcstudios.cswallpaper.renderer.GLRenderer;

public class GLSurfaceViewTouch extends GLSurfaceView {
    private static final String TAG = "GLSurfaceViewTouch";
    private int mInitialTouchX;
    private int mInitialTouchY;
    private GLRenderer mGLRenderer;

    public GLSurfaceViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGLRenderer = null;
    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        mGLRenderer = (GLRenderer) renderer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGLRenderer == null)
            return false;

        final int x = (int) event.getX();
        final int y = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialTouchX = x;
                mInitialTouchY = y;
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mGLRenderer.onStart(x, y);
                    }
                });
                break;
            case MotionEvent.ACTION_MOVE:
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mGLRenderer.onMove(x, y);
                    }
                });
                break;
            case MotionEvent.ACTION_UP:
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mGLRenderer.onEnd(mInitialTouchX, mInitialTouchY, x, y);
                    }
                });
                break;
        }
        return super.onTouchEvent(event);
    }
}
