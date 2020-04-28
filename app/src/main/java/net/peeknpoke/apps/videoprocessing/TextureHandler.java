package net.peeknpoke.apps.videoprocessing;

import android.opengl.GLES11Ext;
import android.opengl.GLES30;

class TextureHandler {
    private int mTexture;

    TextureHandler() {
        mTexture = createTexture();
        loadTexture();
    }

    private int createTexture()
    {
        final int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error creating texture.");
        }

        return textureHandle[0];
    }

    private void loadTexture() {
        if (mTexture != 0)
        {
            // Bind to the texture in OpenGL
            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexture);

            // Set filtering
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        }
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
