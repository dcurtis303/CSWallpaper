package ca.dcstudios.cswallpaper.renderer;

import java.util.ArrayList;

public class AnimatedMesh {
    private ArrayList<Mesh> mMeshes = new ArrayList<>();
    private int mCurrentFrame = 0;
    private int mFrameCount = 0;
    private boolean mIsValid = false;
    private boolean mIsVisible = false;

    public boolean isVisible() {
        return mIsVisible;
    }

    public void setVisible(boolean visible) {
        mIsVisible = visible;
    }

    public Mesh getCurrentMesh() {
        if (mIsValid)
            return mMeshes.get(mCurrentFrame);
        return null;
    }

    public void addFrame(Mesh m) {
        mMeshes.add(m);
        mFrameCount++;
        mIsValid = true;
    }

    public void setTextureUnit(int i) {
        for (Mesh m : mMeshes) {
            m.setTextureUnit(i);
        }
    }

    public boolean incrementFrame() {
        if (++mCurrentFrame >= mFrameCount) {
            mCurrentFrame = 0;
            return true;
        }
        return false;
    }
}
