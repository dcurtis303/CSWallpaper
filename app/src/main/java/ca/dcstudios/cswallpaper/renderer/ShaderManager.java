package ca.dcstudios.cswallpaper.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import ca.dcstudios.cswallpaper.R;
import ca.dcstudios.cswallpaper.Util;

public class ShaderManager {
    private static final String TAG = "ShaderManager";
    private static final Program[] mPrograms = {
            new Program(ShaderName.mo7, R.raw.mo7_vert, R.raw.mo7_frag),
    };

    public static void init(Context context) {
        for (Program p : mPrograms) {
            p.program = createProgram(
                    Util.readTextFileFromRawResource(context, p.idVertexShader),
                    Util.readTextFileFromRawResource(context, p.idFragmentShader));
            Log.d(TAG, "result of " + p.name + " = " + p.program);
        }
    }

    public static int getProgram(ShaderName mProgramName) {
        for (Program p : mPrograms) {
            if (p.name == mProgramName)
                return p.program;
        }
        return -1;
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            Util.checkGlError(TAG, "glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            Util.checkGlError(TAG, "glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public enum ShaderName {
        mo7,
    }

    private static class Program {
        ShaderName name;
        int program;
        int idVertexShader;
        int idFragmentShader;

        private Program(ShaderName name, int idVertexShader, int idFragmentShader) {
            this.name = name;
            this.program = 0;
            this.idVertexShader = idVertexShader;
            this.idFragmentShader = idFragmentShader;
        }
    }
}