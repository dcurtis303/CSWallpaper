package ca.dcstudios.cswallpaper.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ca.dcstudios.cswallpaper.R;
import ca.dcstudios.cswallpaper.Util;

public class SettingsActivity extends AppCompatActivity {
    private static final int OPEN_IMAGE_FILE = 0x22;
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_IMAGE_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                assert uri != null;
                TextView tv = findViewById(R.id.textView2);
                tv.setText(Util.getFileName(this, uri));

                Log.d(TAG, "trying to write app private data...");
                try {
                    InputStream in = getContentResolver().openInputStream(uri);
                    OutputStream out = openFileOutput("test.png", MODE_PRIVATE);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();

                    Log.d(TAG, "write to private data succeeded");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    public void openDocumentPicker(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/png");
        intent = Intent.createChooser(intent, "Pick File");
        startActivityForResult(intent, OPEN_IMAGE_FILE);
    }
}
