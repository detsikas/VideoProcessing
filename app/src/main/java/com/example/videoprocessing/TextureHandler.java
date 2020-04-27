package com.example.videoprocessing;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

class TextureHandler {
    private int mTexture;

    TextureHandler() {
        mTexture = createTexture();
    }

    static int createTexture()
    {
        final int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error creating texture.");
        }

        return textureHandle[0];
    }

    void loadTexture(Bitmap image) {
        // Bind to the texture in OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTexture);

        // Set filtering
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, image, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    int getTexture() {
        return mTexture;
    }

    void cleanup()
    {
        int[] toIDs = new int[1];
        toIDs[0] = mTexture;
        GLES30.glDeleteTextures(1, toIDs, 0);
    }
}
