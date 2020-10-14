package ca.dcstudios.cswallpaper.renderer;

import android.content.Context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class MeshFactory {
    public static Mesh CreateMesh_Square() {
        Mesh mesh = new Mesh();

        float[] verts = new float[]{
                1.000000f, 3.953804f, 0.000000f,
                1.000000f, 1.953803f, 0.000000f,
                -1.000000f, 3.953803f, -0.000000f,
                -1.000000f, 1.953803f, -0.000000f
        };

        float[] uvs = new float[]{
                0.999900f, 0.999900f,
                0.999900f, 0.000100f,
                0.000100f, 0.999900f,
                0.000100f, 0.000100f
        };

        float[] normals = new float[]{
                0.000000f, 0.000000f, -1.000000f,
                0.000000f, 0.000000f, -1.000000f,
                0.000000f, 0.000000f, -1.000000f,
                0.000000f, 0.000000f, -1.000000f
        };

        short[] drawlist = new short[]{
                0, 1, 2,
                1, 3, 2,
        };

        setFloatBuffer(mesh, verts, 0);
        setFloatBuffer(mesh, uvs, 1);
        setFloatBuffer(mesh, normals, 2);
        setShortBuffer(mesh, drawlist);
        mesh.setFaceCount(2);

        return mesh;
    }

    public static Mesh CreateMesh_OBJ(Context context, String file, int texUnit) {
        Mesh newMesh = new Mesh();
        newMesh.setTextureUnit(texUnit);
        Load(newMesh, context, file);
        return newMesh;
    }

    public static AnimatedMesh CreateMesh_Animated(Context context, String dir, int texUnit) {
        AnimatedMesh animatedMesh = new AnimatedMesh();
        try {
            String[] animfiles = context.getResources().getAssets().list(dir);
            if (animfiles == null) throw new AssertionError();
            for (String animfile : animfiles) {
                Mesh m = new Mesh();
                Load(m, context, dir + "/" + animfile);
                animatedMesh.addFrame(m);
            }
            animatedMesh.setTextureUnit(texUnit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return animatedMesh;
    }

    private static boolean Load(Mesh mesh, Context context, String file) {
        OBJImporter importer = new OBJImporter(context, file);

        setFloatBuffer(mesh, importer.getVertices(), 0);
        setFloatBuffer(mesh, importer.getUVs(), 1);
        setFloatBuffer(mesh, importer.getNormals(), 2);
        setShortBuffer(mesh, importer.getFaces());
        mesh.setFaceCount(importer.getFaceCount());

        return true;
    }

    private static int setShortBuffer(Mesh mesh, short[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = mesh.setDrawListBuffer(bb.asShortBuffer());
        sb.put(array);
        sb.position(0);
        return array.length;
    }

    private static void setFloatBuffer(Mesh mesh, float[] array, int type) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb;
        switch (type) {
            case 0:
                fb = mesh.setVertexBuffer(bb.asFloatBuffer());
                break;
            case 1:
                fb = mesh.setUVBuffer(bb.asFloatBuffer());
                break;
            default:
                fb = mesh.setNormalBuffer(bb.asFloatBuffer());
                break;
        }
        fb.put(array);
        fb.position(0);
    }
}
