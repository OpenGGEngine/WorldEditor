/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;


import com.opengg.core.engine.GGApplication;
import com.opengg.core.engine.OpenGG;
import com.opengg.core.engine.Resource;
import com.opengg.core.extension.ExtensionManager;
import com.opengg.core.gui.GUI;
import com.opengg.core.gui.GUIText;
import com.opengg.core.math.Vector2f;
import com.opengg.core.render.texture.text.GGFont;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.module.swt.SWTExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class WorldEditor extends GGApplication{
    private static Display display;
    private static Shell shell;
    private static Tree tree;

    public static void main(String[] args) {
        int minClientWidth = 1920;
        int minClientHeight = 1080;
        display = new Display();
        shell = new Shell(display);

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
        tree = new Tree(c2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
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
        
        ExtensionManager.addExtension(new SWTExtension(shell));
        
        WindowInfo w = new WindowInfo();
        w.width = 640;
        w.height = 480;
        w.resizable = false;
        w.type = "SWT";
        w.vsync = true;
        OpenGG.initialize(new WorldEditor(), w);
    }

    @Override
    public void setup() {
        
        GGFont font = Resource.getFont("test", "test.png");
        com.opengg.core.render.Text text = new com.opengg.core.render.Text("Turmoil has engulfed the Galactic Republic. The taxation of trade routes to outlying star systems is in dispute. \n\n"
                + " Hoping to resolve the matter with a blockade of deadly battleships, "
                + " the greedy Trade Federation has stopped all shipping to the small planet of Naboo. \n\n"
                + " While the congress of the Republic endlessly debates this alarming chain of events,"
                + " the Supreme Chancellor has secretly dispatched two Jedi Knights,"
                + " the guardians of peace and justice in the galaxy, to settle the conflict...", new Vector2f(), 1f, 0.5f, false);
        GUI.addItem("aids", new GUIText(text, font, new Vector2f(0f,0)));
        
        final Text updatetext = new Text(shell, SWT.SHADOW_IN);
        tree.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    updatetext.setText(event.item + " was checked.");
                } else {
                    updatetext.setText(event.item + " was selected");
                }
            }
        });
        
        //shell.setSize(800, 600);
        //shell.setMaximized(true);
        shell.open();
        
    }

    @Override
    public void render() {
         if (!display.readAndDispatch()) {
            display.sleep();
        }
    }

    @Override
    public void update() {
        
    }
}
