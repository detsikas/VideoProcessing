package com.example.videoprocessing;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class Renderer {
    private static final String TAG = Renderer.class.getSimpleName();
    private static final String VERTEX_SHADER_NAME = "shader.vert";
    private static final String FRAGMENT_SHADER_NAME = "shader.frag";

    private final float[] IDENTITY_MATRIX = new float[16];
    private static final int SIZEOF_FLOAT = 4;

    private static final float[] QUAD_COORDS = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };

    private static final float[] QUAD_TEXCOORDS = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    private FloatBuffer mTextureVertexBuffer;
    private FloatBuffer mVertexBuffer;

    // OpenGL handles
    private int mProgram;

    private int quadPositionParam;
    private int quadTexCoordParam;

    private String mVertexShader;
    private String mFragmentShader;

    private int muTexMatrixLoc;

    Renderer(Context context)
    {
        super();
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
        parseShaders(context);
        createProgram();
        createTextureVertexBuffer();
        createVertexBuffer();
    }

    private void parseShaders(Context context)
    {
        String vertexShaderFile, fragmentShaderFile;
        vertexShaderFile = VERTEX_SHADER_NAME;
        fragmentShaderFile = FRAGMENT_SHADER_NAME;
        mVertexShader = loadShaderFile(context, vertexShaderFile);
        mFragmentShader = loadShaderFile(context, fragmentShaderFile);
    }

    void cleanup()
    {
        GLES30.glDeleteProgram(mProgram);
    }

    void onDrawFrame(int texture, int viewPortWidth, int viewPortHeight)
    {
        GLES30.glViewport(0, 0, viewPortWidth, viewPortHeight);
        GLES30.glUseProgram(mProgram);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);

        // Copy the texture transformation matrix over.
        GLES30.glUniformMatrix4fv(muTexMatrixLoc, 1, false, IDENTITY_MATRIX, 0);

        // Set the vertex positions.
        int COORDS_PER_VERTEX = 2;
        GLES30.glVertexAttribPointer(
                quadPositionParam,
                COORDS_PER_VERTEX,
                GLES30.GL_FLOAT,
                false,
                COORDS_PER_VERTEX *SIZEOF_FLOAT,
                mVertexBuffer);

        // Set the texture coordinates.
        GLES30.glVertexAttribPointer(
                quadTexCoordParam,
                COORDS_PER_VERTEX,
                GLES30.GL_FLOAT,
                false,
                COORDS_PER_VERTEX *SIZEOF_FLOAT,
                mTextureVertexBuffer);

        // Enable vertex arrays
        GLES30.glEnableVertexAttribArray(quadPositionParam);
        GLES30.glEnableVertexAttribArray(quadTexCoordParam);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        // Disable vertex arrays
        GLES30.glDisableVertexAttribArray(quadPositionParam);
        GLES30.glDisableVertexAttribArray(quadTexCoordParam);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        GLES30.glUseProgram(0);
    }

    private void createProgram()
    {
        int vertexShader = loadGLShader(mVertexShader, GLES30.GL_VERTEX_SHADER);
        int fragmentShader = loadGLShader(mFragmentShader, GLES30.GL_FRAGMENT_SHADER);

        mProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(mProgram, vertexShader);
        GLES30.glAttachShader(mProgram, fragmentShader);
        GLES30.glLinkProgram(mProgram);
        GLES30.glUseProgram(mProgram);

        quadPositionParam = GLES30.glGetAttribLocation(mProgram, "a_Position");
        quadTexCoordParam = GLES30.glGetAttribLocation(mProgram, "a_TexCoord");
        muTexMatrixLoc = GLES30.glGetUniformLocation(mProgram, "uTexMatrix");
    }

    private static String loadShaderFile(Context context, String filename)
    {
        try
        {
            InputStream inputStream = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder shaderText = new StringBuilder();
            String inputLine = reader.readLine();
            while (inputLine != null) {
                shaderText.append(inputLine).append("\n");
                inputLine = reader.readLine();
            }
            return shaderText.toString();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Shader not found");
        }

        return null;
    }

    private static int loadGLShader(String shaderCode, int type)
    {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        // If shader could not be compiled throw runtime exceptions
        final int[] result = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, result, 0);

        // If the compilation failed, delete the shader.
        if (result[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    private void createTextureVertexBuffer()
    {
        // initialize vertex byte buffer for shape coordinates
        int COORDS_PER_VERTEX = 2;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                4 * COORDS_PER_VERTEX * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        mTextureVertexBuffer = bb.asFloatBuffer();
        mTextureVertexBuffer.put(QUAD_TEXCOORDS);
        mTextureVertexBuffer.position(0);
    }

    private void createVertexBuffer()
    {
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.length * SIZEOF_FLOAT);
        bbVertices.order(ByteOrder.nativeOrder());
        mVertexBuffer = bbVertices.asFloatBuffer();
        mVertexBuffer.put(QUAD_COORDS);
        mVertexBuffer.position(0);
    }
}
