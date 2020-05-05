package net.peeknpoke.apps.frameprocessor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES30;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

class CustomContext implements SurfaceTexture.OnFrameAvailableListener, ObserverSubject<RendererObserver> {
    private static final String TAG = CustomContext.class.getSimpleName();
    private EGLContext mCtx;
    private EGLDisplay mDpy;
    private EGLSurface mSurf;
    private TextureHandler mTextureHandler;
    private final ByteBuffer mBB;
    private final Bitmap mBitmap;
    private Renderer mRenderer;
    private int mImageWidth;
    private int mImageHeight;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    int mOutputFrameIndex = 0;
    private Context mContext;
    private float[] mTransformMatrix = new float[16];
    private ArrayList<WeakReference<RendererObserver>> mObservers = new ArrayList<>();
    private int mMaxFrames;
    private String mAppname;
    final Object sync = new Object();
    boolean proceed = false;


    CustomContext(Context context,
                         int imageWidth, int imageHeight, int maxFrames, String appname)
    {
        mContext = context;
        mMaxFrames = maxFrames;
        mAppname = appname;
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;

        mBB = ByteBuffer.allocateDirect(imageHeight*imageWidth * 4);
        mBB.order(ByteOrder.nativeOrder());
        mBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    }

    void setupRenderingContext(Context context)
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
                EGL14.EGL_WIDTH, mImageWidth,
                EGL14.EGL_HEIGHT, mImageHeight,
                EGL14.EGL_NONE
        };

        mSurf = EGL14.eglCreatePbufferSurface(mDpy, config, surfAttr, 0);

        int[] ctxAttrib = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mCtx = EGL14.eglCreateContext(mDpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0);

        EGL14.eglMakeCurrent(mDpy, mSurf, mSurf, mCtx);

        mTextureHandler = new TextureHandler();
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        mRenderer = new Renderer(context);

        mSurfaceTexture = new SurfaceTexture(mTextureHandler.getTexture());
        mSurface = new Surface(mSurfaceTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        notifySetupComplete();
    }

    private void onDrawFrame()
    {
       GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        if (mRenderer!=null)
        {
            mRenderer.onDrawFrame(mTransformMatrix, mTextureHandler.getTexture(), mImageWidth, mImageHeight);
        }
    }

    void release()
    {
        cleanup();
        mTextureHandler.cleanup();
        mSurfaceTexture.release();

        EGL14.eglMakeCurrent(mDpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mDpy, mSurf);
        EGL14.eglDestroyContext(mDpy, mCtx);
        EGL14.eglTerminate(mDpy);
    }

    Surface getSurface()
    {
        return mSurface;
    }

    private void savePixels(Context context, String filename)
    {
        GLES30.glReadPixels(0, 0, mImageWidth, mImageHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, mBB);

        mBitmap.copyPixelsFromBuffer(mBB);
        mBB.rewind();

        saveImage(mBitmap, context, filename);
    }

    private void saveImage(Bitmap image, Context context, String filename)
    {
        //Store to sdcard
        try {
            File folder = FileOperations.getAppMediaFolder(mAppname);
            if (folder!=null)
            {
                File imageFile = FileOperations.createMediaFile(folder, filename);
                FileOutputStream out = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, out); //Output
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(imageFile));
                context.sendBroadcast(mediaScanIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup()
    {
        if (mRenderer!=null)
            mRenderer.cleanup();

        mRenderer = null;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mOutputFrameIndex< mMaxFrames)
        {
            Log.d(TAG, "Frame is available for rendering");
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTransformMatrix);
            onDrawFrame();
            String filename = "output_"+mOutputFrameIndex;
            savePixels(mContext, filename);
        }
        synchronized (sync)
        {
            proceed = true;
            sync.notify();
        }

        mOutputFrameIndex++;
    }

    private WeakReference<RendererObserver> findWeakReference(RendererObserver rendererObserver)
    {
        WeakReference<RendererObserver> weakReference = null;
        for(WeakReference<RendererObserver> ref : mObservers) {
            if (ref.get() == rendererObserver) {
                weakReference = ref;
            }
        }
        return weakReference;
    }

    @Override
    public void registerObserver(RendererObserver observer) {
        WeakReference<RendererObserver> weakReference = findWeakReference(observer);
        if (weakReference==null)
            mObservers.add(new WeakReference<>(observer));
    }

    @Override
    public void removeObserver(RendererObserver observer) {
        WeakReference<RendererObserver> weakReference = findWeakReference(observer);
        if (weakReference != null) {
            mObservers.remove(weakReference);
        }
    }

    private void notifySetupComplete()
    {
        for (WeakReference<RendererObserver> co:mObservers){
            RendererObserver observer = co.get();
            if (observer!=null)
                observer.setupComplete();
        }
    }
}
