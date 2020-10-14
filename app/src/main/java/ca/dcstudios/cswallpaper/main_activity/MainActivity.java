package ca.dcstudios.cswallpaper.main_activity;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import ca.dcstudios.cswallpaper.R;
import ca.dcstudios.cswallpaper.renderer.GLRenderer;
import ca.dcstudios.cswallpaper.service.GLWallpaperService;
import ca.dcstudios.cswallpaper.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {
    public static final boolean WANT_DEBUG = GLWallpaperService.WANT_DEBUG;
    private GLRenderer mGLRenderer;

    public MainActivity() {
        if (WANT_DEBUG)
            android.os.Debug.waitForDebugger();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        GLSurfaceView glSurfaceView = findViewById(R.id.glSurfaceView);
        if (glSurfaceView != null) {
            glSurfaceView.setEGLContextClientVersion(2);
            mGLRenderer = new GLRenderer(this, glSurfaceView);
            glSurfaceView.setRenderer(mGLRenderer);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }

    public void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
