package net.peeknpoke.apps.videoprocessing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import net.peeknpoke.apps.videoprocessing.permissions.StoragePermissionHandler;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PICK_FROM_GALLERY = 1;
    final StoragePermissionHandler mStoragePermissionHandler = new StoragePermissionHandler();

    private VideoView videoView;
    private Button processButton;

    private Uri mVideoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        processButton = findViewById(R.id.process);
    }

    public void onLoad(View view)
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
    }

    public void onProcess(View view)
    {
        try {
            FrameProcessor frameProcessor = new FrameProcessor(getApplicationContext(), mVideoUri);
            frameProcessor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mVideoUri = data.getData();
            videoView.setVideoURI(mVideoUri);
            videoView.setOnPreparedListener(mp -> {
                videoView.start();
                processButton.setVisibility(View.INVISIBLE);
            });

            videoView.setOnCompletionListener(mp -> processButton.setVisibility(View.VISIBLE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode != StoragePermissionHandler.CODE) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                mStoragePermissionHandler.checkAndRequestPermission(this, requestCode);

                Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + grantResults[0]);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStoragePermissionHandler.checkAndRequestPermission(MainActivity.this, StoragePermissionHandler.CODE);
    }
}
