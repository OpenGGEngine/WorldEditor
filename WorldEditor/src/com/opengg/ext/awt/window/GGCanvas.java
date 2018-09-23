package com.opengg.ext.awt.window;

import com.opengg.core.exceptions.WindowCreationException;
import com.opengg.core.io.input.keyboard.KeyboardController;
import com.opengg.core.io.input.mouse.MouseController;
import com.opengg.core.render.window.Window;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.ext.awt.input.AWTKeyboardHandler;
import com.opengg.ext.awt.input.AWTMouseButtonHandler;
import com.opengg.ext.awt.input.AWTMousePosHandler;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.InvocationTargetException;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class GGCanvas extends JPanel implements Window {
    private AWTGLCanvas canvas;
    private AWTMousePosHandler mousePosCallback;
    private AWTMouseButtonHandler mouseCallback;
    private AWTKeyboardHandler keyCallback;
    public static Container container;

    @Override
    public void setup(WindowInfo info) {
        GLData data = new GLData();
        data.api = GLData.API.GL;
        data.majorVersion = 4;
        data.minorVersion = 1;
        data.samples = 1;
        data.redSize = info.rbit;
        data.blueSize = info.bbit;
        data.greenSize = info.gbit;
        data.profile = GLData.Profile.CORE;
        data.swapInterval = info.vsync ? 1 : 0;
        data.forwardCompatible = true;

        canvas = new AWTGLCanvas(data) {
            private static final long serialVersionUID = 1L;
            public void initGL() {
                canvas.makeCurrent();
                GL.createCapabilities();
            }
            public void paintGL() {
            }
        };

        this.setLayout(new BorderLayout());
        this.add(canvas);

        this.setPreferredSize(new Dimension(info.width, info.height));
        canvas.setPreferredSize(new Dimension(info.width, info.height));
        container.add(this);

        canvas.init();

        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
        canvas.addMouseListener(mouseCallback = new AWTMouseButtonHandler());
        canvas.addKeyListener(keyCallback = new AWTKeyboardHandler());
        canvas.addMouseMotionListener(mousePosCallback = new AWTMousePosHandler());

        KeyboardController.setHandler(keyCallback);
        MouseController.setPosHandler(mousePosCallback);
        MouseController.setButtonHandler(mouseCallback);

        if (glGetError() != GL_NO_ERROR) {
            throw new WindowCreationException("OpenGL initialization during window creation failed");
        }
    }

    @Override
    public void startFrame(){
        canvas.makeCurrent();
        GL.createCapabilities();
    }

    @Override
    public void endFrame() {
        canvas.swapBuffers();
    }

    @Override
    public float getRatio() {
        return (float)this.getWidth()/(float)this.getHeight();
    }

    @Override
    public boolean shouldClose() {
        return !this.isEnabled();
    }

    @Override
    public void destroy() {
        //this.is
    }

    @Override
    public long getID() {
        return canvas.context;
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    public boolean getSuccessfulConstruction() {
        return false;
    }

    @Override
    public String getType() {
        return "AWT";
    }

    @Override
    public void setIcon(String path) throws Exception {

    }

    @Override
    public void setVSync(boolean vsync) {

    }

    @Override
    public void setCurrentContext() {
        canvas.makeCurrent();
    }

    @Override
    public void setCursorLock(boolean lock) {

    }
}
