package ca.dcstudios.cswallpaper.renderer;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

public class OBJImporter {
    //private static final String TAG = "OBJImporter";

    private static final int FLOATS_PER_VERTEX = 3;
    private static final int FLOATS_PER_UV = 2;
    private static final int FLOATS_PER_NORMAL = 3;
    private static final int INTS_PER_FACE = 3;
    private Context mContext;
    private String mFile;
    private int mCount_Objects;
    private int mCount_Vertices;
    private int mCount_UVs;
    private int mCount_Faces;
    private int mCount_Normals;
    private float[] mVertices_In;
    private float[] mUVs_In;
    private float[] mNormals_In;
    private Vector<Face> mFaces_In = new Vector<>();
    private float[] mVertices_Out;
    private float[] mUVs_Out;
    private float[] mNormals_Out;
    private short[] mFaces_Out;
    HashMap<String, Integer> mVAMap = new HashMap<>();

    public OBJImporter(Context context, String file) {
        mContext = context;
        mFile = file;

        if (preReadFile()) {
            readFile();
            buildVertexAttribArrays();
        }
    }

    public int getFaceCount() {
        return mCount_Faces;
    }

    public float[] getVertices() {
        return mVertices_Out;
    }

    public float[] getUVs() {
        return mUVs_Out;
    }

    public float[] getNormals() {
        return mNormals_Out;
    }

    public short[] getFaces() {
        return mFaces_Out;
    }

    private boolean preReadFile() {
        boolean success = true;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mContext.getResources().getAssets().open(mFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("o ")) mCount_Objects++;
                if (line.contains("v ")) mCount_Vertices++;
                if (line.contains("vt")) mCount_UVs++;
                if (line.contains("vn")) mCount_Normals++;
                if (line.contains("f ")) mCount_Faces++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }

        return success;
    }

    private boolean readFile() {
        boolean success = true;
        int ndx_v = 0;
        int ndx_vt = 0;
        int ndx_vn = 0;

        mVertices_In = new float[mCount_Vertices * FLOATS_PER_VERTEX];
        mUVs_In = new float[mCount_UVs * FLOATS_PER_UV];
        mNormals_In = new float[mCount_Normals * FLOATS_PER_NORMAL];

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mContext.getResources().getAssets().open(mFile)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                String type = values[0];
                String[] entries;
                short[] iV = new short[3];
                short[] iT = new short[3];
                short[] iN = new short[3];

                if (type.equals("o")) {
                    // deal with multiple objects per file
                }
                if (type.equals("v")) {
                    mVertices_In[ndx_v++] = Float.parseFloat(values[1]);
                    mVertices_In[ndx_v++] = Float.parseFloat(values[2]);
                    mVertices_In[ndx_v++] = Float.parseFloat(values[3]);
                }
                if (type.equals("vt")) {
                    mUVs_In[ndx_vt++] = Float.parseFloat(values[1]);
                    mUVs_In[ndx_vt++] = Float.parseFloat(values[2]);
                }
                if (type.equals("vn")) {
                    mNormals_In[ndx_vn++] = Float.parseFloat(values[1]);
                    mNormals_In[ndx_vn++] = Float.parseFloat(values[2]);
                    mNormals_In[ndx_vn++] = Float.parseFloat(values[3]);
                }
                if (type.equals("f")) {
                    entries = values[1].split("/");
                    iV[0] = (short) (Integer.parseInt(entries[0]) - 1);
                    iT[0] = (short) (Integer.parseInt(entries[1]) - 1);
                    iN[0] = (short) (Integer.parseInt(entries[2]) - 1);

                    entries = values[2].split("/");
                    iV[1] = (short) (Integer.parseInt(entries[0]) - 1);
                    iT[1] = (short) (Integer.parseInt(entries[1]) - 1);
                    iN[1] = (short) (Integer.parseInt(entries[2]) - 1);

                    entries = values[3].split("/");
                    iV[2] = (short) (Integer.parseInt(entries[0]) - 1);
                    iT[2] = (short) (Integer.parseInt(entries[1]) - 1);
                    iN[2] = (short) (Integer.parseInt(entries[2]) - 1);

                    Face f = new Face(iV, iT, iN);
                    mFaces_In.add(f);

                    mVAMap.put(f.va[0].toString(), -1);
                    mVAMap.put(f.va[1].toString(), -1);
                    mVAMap.put(f.va[2].toString(), -1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }

        return success;
    }

    private void buildVertexAttribArrays() {
        int size = mVAMap.size();
        mVertices_Out = new float[size * FLOATS_PER_VERTEX];
        mNormals_Out = new float[size * FLOATS_PER_NORMAL];
        mUVs_Out = new float[size * FLOATS_PER_UV];
        mFaces_Out = new short[mCount_Faces * INTS_PER_FACE];

        int ndxVAOut = 0;
        int ndxFaceOut = 0;

        for (Face f : mFaces_In) {
            for (int i = 0; i < 3; i++) {
                if (mVAMap.get(f.va[i].toString()) == -1) {
                    mVAMap.put(f.va[i].toString(), ndxVAOut);

                    int ndxIn = f.va[i].v * FLOATS_PER_VERTEX;
                    int ndxOut = ndxVAOut * FLOATS_PER_VERTEX;
                    mVertices_Out[ndxOut] = mVertices_In[ndxIn];
                    mVertices_Out[ndxOut + 1] = mVertices_In[ndxIn + 1];
                    mVertices_Out[ndxOut + 2] = mVertices_In[ndxIn + 2];

                    ndxIn = f.va[i].n * FLOATS_PER_NORMAL;
                    ndxOut = ndxVAOut * FLOATS_PER_NORMAL;
                    mNormals_Out[ndxOut] = mNormals_In[ndxIn];
                    mNormals_Out[ndxOut + 1] = mNormals_In[ndxIn + 1];
                    mNormals_Out[ndxOut + 2] = mNormals_In[ndxIn + 2];

                    ndxIn = f.va[i].t * FLOATS_PER_UV;
                    ndxOut = ndxVAOut * FLOATS_PER_UV;
                    mUVs_Out[ndxOut] = mUVs_In[ndxIn];
                    mUVs_Out[ndxOut + 1] = 1.0f - mUVs_In[ndxIn + 1];

                    ndxVAOut++;
                }
            }
            mFaces_Out[ndxFaceOut++] = (short) (int) (mVAMap.get(f.va[0].toString()));
            mFaces_Out[ndxFaceOut++] = (short) (int) (mVAMap.get(f.va[1].toString()));
            mFaces_Out[ndxFaceOut++] = (short) (int) (mVAMap.get(f.va[2].toString()));
        }
    }

    public int getOjectCount() {
        return mCount_Objects;
    }

    private class VertexAttribute {
        short v;
        short t;
        short n;

        VertexAttribute(short v, short t, short n) {
            this.v = v;
            this.t = t;
            this.n = n;
        }

        @Override
        public String toString() {
            return v + "/" + t + "/" + n;
        }
    }

    private class Face {
        VertexAttribute[] va = new VertexAttribute[3];

        Face(short[] v, short[] t, short[] n) {
            va[0] = new VertexAttribute(v[0], t[0], n[0]);
            va[1] = new VertexAttribute(v[1], t[1], n[1]);
            va[2] = new VertexAttribute(v[2], t[2], n[2]);
        }
    }
}
