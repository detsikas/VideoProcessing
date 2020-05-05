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
import android.widget.ProgressBar;
import android.widget.VideoView;

import net.peeknpoke.apps.frameprocessor.FrameProcessor;
import net.peeknpoke.apps.frameprocessor.FrameProcessorObserver;
import net.peeknpoke.apps.videoprocessing.permissions.StoragePermissionHandler;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements FrameProcessorObserver {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PICK_FROM_GALLERY = 1;
    private StoragePermissionHandler mStoragePermissionHandler;

    private VideoView mVideoView;
    private Button mProcessButton;
    private FrameProcessor mFrameProcessor;
    private Uri mVideoUri;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStoragePermissionHandler = new StoragePermissionHandler(getResources().getString(R.string.app_name));
        mVideoView = findViewById(R.id.videoView);
        mProcessButton = findViewById(R.id.process);
        mProgressBar = findViewById(R.id.processingBar);
    }

    public void onLoad(View view)
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
    }

    public void onProcess(View view)
    {
        mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);
        try {
            mFrameProcessor = new FrameProcessor(getApplicationContext(), mVideoUri,
                    getResources().getInteger(R.integer.MAX_FRAMES),
                    getResources().getString(R.string.app_name));
            mFrameProcessor.registerObserver(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFrameProcessor!=null)
        {
            mFrameProcessor.release();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mVideoUri = data.getData();
            mVideoView.setVideoURI(mVideoUri);
            mVideoView.setOnPreparedListener(mp -> {
                mVideoView.start();
                mProcessButton.setVisibility(View.VISIBLE);
            });

            mVideoView.setOnCompletionListener(mp -> mProcessButton.setVisibility(View.VISIBLE));
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

    @Override
    public void doneProcessing() {
        mFrameProcessor.removeObserver(this);
        runOnUiThread(() -> mProgressBar.setVisibility(View.INVISIBLE));
    }
}
