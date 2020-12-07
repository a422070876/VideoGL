package com.hyq.hm.video.gl;


import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
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
            image = ImageIO.read(new File("Y:\\java\\TestImage\\res\\test1\\ic_car.jpg"));
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
        frame.add(new JTextField(), BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        new Thread(){
            @Override
            public void run() {
                super.run();
                FFmpegFrameGrabber frameGrabber =
                        new FFmpegFrameGrabber("Y:\\java\\TestVideo\\res\\2.mp4");
                frameGrabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
                try {
                    frameGrabber.start();
                    int fLength  = frameGrabber.getLengthInFrames();
                    int fNumber  = frameGrabber.getFrameNumber();
                    Java2DFrameConverter converter = new Java2DFrameConverter();
                    int n = -1;
                    while (fNumber < fLength){
                        Frame frame = frameGrabber.grab();
                        if(frame.image != null){
                            BufferedImage bufferedImage = converter.getBufferedImage(frame);
                            renderer.setImage(bufferedImage);
                            fNumber  = frameGrabber.getFrameNumber();
                            if(n != fNumber)n = fNumber;else break;
                        }
                    }
                    frameGrabber.release();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

}
