/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class WorldEditor {

    public static void main(String[] args) {
        int minClientWidth = 640;
        int minClientHeight = 480;
        final Display display = new Display();
        final Shell shell = new Shell(display);

        shell.setLayout(new FillLayout());
        shell.setText("World Editor");

        Menu menuBar = new Menu(shell, SWT.BAR);
        MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeFileMenu.setText("&File");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        cascadeFileMenu.setMenu(fileMenu);

        MenuItem cascadeEditMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeEditMenu.setText("&Edit");

        MenuItem subMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
        subMenuItem.setText("Import");

        Menu submenu = new Menu(shell, SWT.DROP_DOWN);
        subMenuItem.setMenu(submenu);

        MenuItem feedItem = new MenuItem(submenu, SWT.PUSH);
        feedItem.setText("&Import news feed...");

        MenuItem bmarks = new MenuItem(submenu, SWT.PUSH);
        bmarks.setText("&Import bookmarks...");

        MenuItem mailItem = new MenuItem(submenu, SWT.PUSH);
        mailItem.setText("&Import mail...");

        MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
        exitItem.setText("&Exit");
        shell.setMenuBar(menuBar);
        
        ScrolledComposite c2 = new ScrolledComposite(shell,SWT.BORDER);
        final Tree tree = new Tree(c2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        tree.setSize(500, 800);
        for (int loopIndex0 = 0; loopIndex0 < 10; loopIndex0++) {
            TreeItem treeItem0 = new TreeItem(tree, 0);
            treeItem0.setText("WorldObject " + loopIndex0);
            for (int loopIndex1 = 0; loopIndex1 < 10; loopIndex1++) {
                TreeItem treeItem1 = new TreeItem(treeItem0, 0);
                treeItem1.setText("Component" + loopIndex1);
                
            }
        }

        
        
        shell.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.ALT && (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR)) {
                    shell.setFullScreen(!shell.getFullScreen());
                }
            }
        });
        int dw = shell.getSize().x - shell.getClientArea().width;
        int dh = shell.getSize().y - shell.getClientArea().height;
        shell.setMinimumSize(minClientWidth + dw, minClientHeight + dh);
        GLData data = new GLData();
        data.doubleBuffer = true;
        final GLCanvas canvas = new GLCanvas(shell, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, data);
        canvas.setCurrent();
        GL.createCapabilities();

        final Rectangle rect = new Rectangle(0, 0, 0, 0);
        canvas.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                Rectangle bounds = canvas.getBounds();
                rect.width = bounds.width;
                rect.height = bounds.height;
            }
        });
        shell.addListener(SWT.Traverse, new Listener() {
            public void handleEvent(Event event) {
                switch (event.detail) {
                    case SWT.TRAVERSE_ESCAPE:
                        shell.close();
                        event.detail = SWT.TRAVERSE_NONE;
                        event.doit = false;
                        break;
                    default:
                        break;
                }
            }
        });

        final Text text = new Text(shell, SWT.SHADOW_IN);
        tree.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    text.setText(event.item + " was checked.");
                } else {
                    text.setText(event.item + " was selected");
                }
            }
        });
 
        glClearColor(0.3f, 0.5f, 0.8f, 1.0f);

        // Create a simple shader program
        int program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform float rot;"
                + "uniform float aspect;"
                + "void main(void) {"
                + "  vec4 v = gl_Vertex * 0.5;"
                + "  vec4 v_ = vec4(0.0, 0.0, 0.0, 1.0);"
                + "  v_.x = v.x * cos(rot) - v.y * sin(rot);"
                + "  v_.y = v.y * cos(rot) + v.x * sin(rot);"
                + "  v_.x /= aspect;"
                + "  gl_Position = v_;"
                + "}");
        glCompileShader(vs);
        glAttachShader(program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "void main(void) {"
                + "  gl_FragColor = vec4(0.1, 0.3, 0.5, 1.0);"
                + "}");
        glCompileShader(fs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        glUseProgram(program);
        final int rotLocation = glGetUniformLocation(program, "rot");
        final int aspectLocation = glGetUniformLocation(program, "aspect");

        // Create a simple quad
        int vbo = glGenBuffers();
        int ibo = glGenBuffers();
        float[] vertices = {
            -1, -1, 0,
            1, -1, 0,
            1, 1, 0,
            -1, 1, 0
        };
        int[] indices = {
            0, 1, 2,
            2, 3, 0
        };
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils
                .createFloatBuffer(vertices.length).put(vertices).flip(),
                GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils
                .createIntBuffer(indices.length).put(indices).flip(),
                GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L);

        shell.setSize(800, 600);
        shell.setMaximized(true);
        shell.open();

        display.asyncExec(new Runnable() {
            float rot;
            long lastTime = System.nanoTime();

            public void run() {
                if (!canvas.isDisposed()) {
                    canvas.setCurrent();
                    glClear(GL_COLOR_BUFFER_BIT);
                    glViewport(0, 0, rect.width, rect.height);

                    float aspect = (float) rect.width / rect.height;
                    glUniform1f(aspectLocation, aspect);
                    glUniform1f(rotLocation, rot);
                    glDrawElements(GL11.GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

                    canvas.swapBuffers();
                    display.asyncExec(this);

                    long thisTime = System.nanoTime();
                    float delta = (thisTime - lastTime) / 1E9f;
                    rot += delta;
                    if (rot > 2.0 * Math.PI) {
                        rot -= 2.0f * (float) Math.PI;
                    }
                    lastTime = thisTime;
                }
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
