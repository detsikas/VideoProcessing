package com.example.videoprocessing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class CustomContext {
    private final EGLContext mCtx;
    private final EGLDisplay mDpy;
    private final EGLSurface mSurf;
    private TextureHandler mTextureHandler;
    private final ByteBuffer mBB;
    private final Bitmap mBitmap;
    private Renderer mRenderer;
    private int mImageWidth;
    private int mImageHeight;

    public CustomContext(Context context,
                         int imageWidth, int imageHeight)
    {
        mDpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        EGL14.eglInitialize(mDpy, version, 0, version, 1);

        int[] configAttr = {
                EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                EGL14.EGL_LEVEL, 0,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];
        EGL14.eglChooseConfig(mDpy, configAttr, 0,
                configs, 0, 1, numConfig, 0);

        EGLConfig config = configs[0];

        int[] surfAttr = {
                EGL14.EGL_WIDTH, imageWidth,
                EGL14.EGL_HEIGHT, imageHeight,
                EGL14.EGL_NONE
        };
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;

        mSurf = EGL14.eglCreatePbufferSurface(mDpy, config, surfAttr, 0);

        int[] ctxAttrib = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mCtx = EGL14.eglCreateContext(mDpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0);

        EGL14.eglMakeCurrent(mDpy, mSurf, mSurf, mCtx);

        mTextureHandler = new TextureHandler();
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        mBB = ByteBuffer.allocateDirect(imageHeight*imageWidth * 4);
        mBB.order(ByteOrder.nativeOrder());
        mBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        mRenderer = new Renderer(context);
    }

    public void onDrawFrame()
    {
       GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        if (mRenderer!=null)
        {
            mRenderer.onDrawFrame(mTextureHandler.getTexture(), mImageWidth, mImageHeight);
        }
    }

    public void release()
    {
        cleanup();
        mTextureHandler.cleanup();

        EGL14.eglMakeCurrent(mDpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mDpy, mSurf);
        EGL14.eglDestroyContext(mDpy, mCtx);
        EGL14.eglTerminate(mDpy);
    }

    public void processImage(byte[] bytes)
    {
        // Create the image
        Bitmap image = createImage(bytes);

        mTextureHandler.loadTexture(image);
    }

    public void loadTexture(Bitmap image)
    {
        mTextureHandler.loadTexture(image);
    }

    private Bitmap createImage(byte[] bytes)
    {
        return convertJpegToBitmap(bytes);
    }

    static Bitmap convertJpegToBitmap(byte[] bytes)
    {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Matrix getImageRotationMatrix(int cameraRotation, int cameraFacing)
    {
        Matrix rotationMatrix = new Matrix();
        float rotationAngle = 0;
        if (cameraRotation!=0)
        {
            rotationAngle = -(4-cameraRotation)*90;
        }

        rotationMatrix.postRotate(rotationAngle);

        /*
        if (cameraFacing== CameraSource.CAMERA_FACING_BACK)
        {
            rotationMatrix.postRotate(90);
        }
        */
        return rotationMatrix;
    }

    public void copyPixelsIntoBitmap(Bitmap bitmap)
    {
        GLES30.glReadPixels(0, 0, mImageWidth, mImageHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, mBB);

        bitmap.copyPixelsFromBuffer(mBB);
        mBB.rewind();
    }

    public File savePixels(Context context, String groupName, String name)
    {
        GLES30.glReadPixels(0, 0, mImageWidth, mImageHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, mBB);

        mBitmap.copyPixelsFromBuffer(mBB);
        mBB.rewind();

        //bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);

        return saveImage(mBitmap, context, groupName, name);
    }

    public Bitmap getFilteredBitmap()
    {
        return mBitmap;
    }

    private File saveImage(Bitmap image, Context context, String groupName, String name)
    {
        //Store to sdcard
        try {
            File folder = FileOperations.getAppMediaFolder(context);
            if (folder!=null)
            {
                File imageFile = FileOperations.createMediaFile(folder, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                        groupName, name);
                FileOutputStream out = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, out); //Output
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(imageFile));
                context.sendBroadcast(mediaScanIntent);
                return imageFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void cleanup()
    {
        if (mRenderer!=null)
            mRenderer.cleanup();

        mRenderer = null;
    }
}
