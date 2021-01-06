package ca.dcstudios.cswallpaper.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.FileInputStream;
import java.util.Vector;

import ca.dcstudios.cswallpaper.Util;

public class TextureManager {

    public static final String[] mAssetNames = {
            "drawable/world_test",
            "drawable/worldtex",
            "drawable/steve",
            "drawable/default_skin",
    };
    private static final String TAG = "TextureManager";
    private Context mContext;
    private Vector<Asset> mAssetList = new Vector<>();

    public TextureManager(Context context) {
        mContext = context;
    }

    public void LoadTextures() {
        int assetCount = mAssetNames.length;
        int[] texturenames = new int[assetCount];
        GLES20.glGenTextures(assetCount, texturenames, 0);

        for (int i = 0; i < assetCount; i++) {
            int id = mContext.getResources().getIdentifier(mAssetNames[i], null,
                    mContext.getPackageName());
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            bmp.recycle();

            mAssetList.add(new Asset(mAssetNames[i], i, texturenames[i]));
        }
    }

    public Asset LoadTextureFromFile(String file) {
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);
        Bitmap bmp;

        int i = mAssetList.size();

        //Bitmap bmp = BitmapFactory.decodeFile(file);
        try {
            FileInputStream fis = mContext.openFileInput("test.png");
            bmp = BitmapFactory.decodeStream(fis);
            if (bmp == null) {
                Log.d(TAG, "Failed to load external file for bitmap texture");
                return null;
            }
            fis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //Log.d(TAG, "Bitmap width: " + bmp.getWidth() + " height: " + bmp.getHeight());

        if (bmp.getWidth() != 64 || bmp.getHeight() != 64) {
            Log.d(TAG, "debug: Width != 64 | Height != 64");
            return null;
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        Util.checkGlError(TAG, "texImage2d");
        bmp.recycle();


        Asset asset = new Asset(file, i, texturenames[0]);
        mAssetList.add(asset);

        return asset;
    }

    public void ListTextures() {
        for (Asset a : mAssetList) {
            Log.d(TAG, String.format("%s: unit: %d name: %d (IsTexture: %s)",
                    a.name, a.tunit, a.tname, GLES20.glIsTexture(a.tname) ? "true" : "false"));
        }
    }

    public int getAssetCount() {
        return mAssetList.size();
    }

    public class Asset {
        String name;
        int tunit;
        int tname;

        public Asset(String name, int tunit, int tname) {
            this.name = name;
            this.tunit = tunit;
            this.tname = tname;
        }
    }
}
