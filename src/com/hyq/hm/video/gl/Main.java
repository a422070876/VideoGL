package com.hyq.hm.video.gl;


import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.Platform;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class Main {

    public static void main(String[] args) {
	// write your code here
        if (Platform.get() == Platform.MACOSX) {
            throw new UnsupportedOperationException("This demo cannot run on macOS.");
        }

        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }
        LWJGLCanvas canvas = new LWJGLCanvas();
        canvas.setSize(640, 480);

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("Y:\\java\\TestImage\\res\\test1\\ic_car.jpg"));//ic_car.jpg   ic_pkq.png
        } catch (IOException e) {
            e.printStackTrace();
        }
        GLRenderer renderer = new GLRenderer();
        canvas.addRenderer(renderer);
        if(image != null){
            renderer.setImage(image);
        }
        JFrame frame = new JFrame("JAWT Demo");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                canvas.destroy();
                frame.dispose();
                glfwTerminate();
                Objects.requireNonNull(glfwSetErrorCallback(null)).free();
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getID() == KeyEvent.KEY_PRESSED) {
                canvas.destroy();
                frame.dispose();
                glfwTerminate();
                Objects.requireNonNull(glfwSetErrorCallback(null)).free();
                return true;
            }

            return false;
        });

        frame.setLayout(new BorderLayout());
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        new Thread(){
            @Override
            public void run() {
                super.run();
                FFmpegFrameGrabber frameGrabber =
                        new FFmpegFrameGrabber("Y:\\java\\TestVideo\\res\\2.mp4");
                //默认是AV_PIX_FMT_BGR24,用默认格式的时候Shader里要改成out_Color = vec4(rgba.b,rgba.g,rgba.r,rgba.a);
                frameGrabber.setPixelFormat(avutil.AV_PIX_FMT_RGB24);
                try {
                    frameGrabber.start();
                    int fLength  = frameGrabber.getLengthInFrames();
                    int fNumber  = frameGrabber.getFrameNumber();
//                    Java2DFrameConverter converter = new Java2DFrameConverter();
                    int n = -1;
                    while (fNumber < fLength && !canvas.isDestroy()){
                        Frame frame = frameGrabber.grab();
                        if(frame == null)break;
                        if(frame.image != null){
//                            BufferedImage bufferedImage = converter.getBufferedImage(frame);
                            int width = frame.imageWidth;
                            int height = frame.imageHeight;
//                            int stride = frame.imageStride;
//                            int rowBytes = width*4;
                            renderer.setBuffer((ByteBuffer) frame.image[0],
                                    width,height);
                            fNumber  = frameGrabber.getFrameNumber();
                            if(n != fNumber)n = fNumber;else break;
                        }
                    }
                    renderer.setBuffer(null,
                            frameGrabber.getImageWidth(),frameGrabber.getImageHeight());
                    frameGrabber.stop();
                    frameGrabber.release();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }
    private static ByteBuffer bgr2rgba(ByteBuffer in, int width, int height, int stride, int rowBytes) {
        ByteBuffer out = BufferUtils.createByteBuffer( height * rowBytes);
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
//                int rgb;
//                if (x >= width - 1 && y >= height - 1) {
//                    int b = in.get(y * stride + 3 * x) & 255;
//                    int g = in.get(y * stride + 3 * x + 1) & 255;
//                    int r = in.get(y * stride + 3 * x + 2) & 255;
//                    rgb = r << 16 | g << 8 | b;
//                } else {
//                    rgb = in.getInt(y * stride + 3 * x);
//                }
//                out.putInt(y * rowBytes + 4 * x, rgb << 8 | 255);
                int rgba;
                int b = in.get(y * stride + 3 * x) & 255;
                int g = in.get(y * stride + 3 * x + 1) & 255;
                int r = in.get(y * stride + 3 * x + 2) & 255;
                int a = 255;
                rgba = r | g << 8 | b << 16 | a << 24 ;
                out.putInt(y * rowBytes + 4 * x,  rgba);
            }
        }
        out.flip();
        return out;
    }
}
