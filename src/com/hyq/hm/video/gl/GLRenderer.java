package com.hyq.hm.video.gl;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.hyq.hm.video.gl.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL30C.*;

public class GLRenderer implements LWJGLCanvas.Renderer {
    private final int[] textures = new int[1];

    private final int[] bos = new int[2];

    private int programId = -1;
    private int aPositionHandle;
    private int aTextureCoordHandle;
    private int uTextureSamplerHandle;


    private int imageWidth,imageHeight;
    private ByteBuffer imageBuffer;
    public void setImage(BufferedImage image){
        synchronized (textures) {
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            imageBuffer = imageToBuffer(image);
        }
    }

    @Override
    public void onCreated() {
        try {
            ByteBuffer vs = ioResourceToByteBuffer("Y:\\java\\VideoGL\\res\\gears.vert", 4096);
            ByteBuffer fs = ioResourceToByteBuffer("Y:\\java\\VideoGL\\res\\gears.frag", 4096);
            programId = ShaderUtils.createProgram(vs, fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(programId == -1){
            return;
        }
        aPositionHandle = glGetAttribLocation(programId, "aPosition");
        aTextureCoordHandle = glGetAttribLocation(programId, "aTexCoord");
        uTextureSamplerHandle = glGetUniformLocation(programId, "sTexture");


        final float[] vertexData = {
                1f, -1f, 0f,
                -1f, -1f, 0f,
                1f, 1f, 0f,
                -1f, 1f, 0f
        };


        final float[] textureVertexData = {
                1f, 0f,
                0f, 0f,
                1f, 1f,
                0f, 1f
        };
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        FloatBuffer textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        glGenBuffers(bos);
        glBindBuffer(GL_ARRAY_BUFFER, bos[0]);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, bos[1]);
        glBufferData(GL_ARRAY_BUFFER,  textureVertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);


        glGenTextures(textures);
        glBindTexture(GL_TEXTURE_2D, textures[0]);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, imageWidth, imageHeight,0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    private int screenWidth,screenHeight;
    @Override
    public void onChanged(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    public void onDrawFrame() {
        if(programId == -1){
            return;
        }
        viewportSize(screenWidth,screenHeight,imageWidth,imageHeight);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f,1.0f,1.0f,1.0f);

        glUseProgram(programId);
        synchronized (textures){
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textures[0]);
//            glTexSubImage2D(GL_TEXTURE_2D, 0,0, 0, imageWidth,imageHeight, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, imageWidth, imageHeight,0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
            glUniform1i(uTextureSamplerHandle, 0);
        }


        glBindBuffer(GL_ARRAY_BUFFER, bos[0]);
        glEnableVertexAttribArray(aPositionHandle);
        glVertexAttribPointer(aPositionHandle, 3, GL_FLOAT, false,
                0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, bos[1]);
        glEnableVertexAttribArray(aTextureCoordHandle);
        glVertexAttribPointer(aTextureCoordHandle, 2, GL_FLOAT, false, 0, 0);


        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }
    private void viewportSize(int screenWidth,int screenHeight,int width,int height) {
        int left, top, viewWidth, viewHeight;
        float sh = screenWidth * 1.0f / screenHeight;
        float vh = width * 1.0f / height;
        if (sh < vh) {
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int) (height * 1.0f / width * viewWidth);
            top = (screenHeight - viewHeight) / 2;
        } else {
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int) (width * 1.0f / height * viewHeight);
            left = (screenWidth - viewWidth) / 2;
        }
        glViewport(left, top, viewWidth, viewHeight);
    }

    public static ByteBuffer imageToBuffer(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return buffer;
    }
}
