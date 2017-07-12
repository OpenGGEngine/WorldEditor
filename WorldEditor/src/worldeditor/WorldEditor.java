/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;


import com.opengg.core.engine.BindController;
import com.opengg.core.engine.GGApplication;
import com.opengg.core.engine.GGConsole;
import com.opengg.core.engine.OpenGG;
import com.opengg.core.engine.RenderEngine;
import com.opengg.core.engine.Resource;
import com.opengg.core.engine.WorldEngine;
import com.opengg.core.extension.ExtensionManager;
import com.opengg.core.gui.GUI;
import com.opengg.core.gui.GUIText;
import com.opengg.core.io.ControlType;
import static com.opengg.core.io.input.keyboard.Key.*;
import com.opengg.core.math.Vector2f;
import com.opengg.core.render.shader.ShaderController;
import com.opengg.core.render.texture.Texture;
import com.opengg.core.render.texture.text.GGFont;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.core.world.Skybox;
import com.opengg.core.world.World;
import com.opengg.core.world.components.Component;
import com.opengg.core.world.components.WorldObject;
import com.opengg.core.world.components.viewmodel.ComponentViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegistry;
import com.opengg.module.swt.SWTExtension;
import com.opengg.module.swt.window.GGCanvas;
import com.opengg.module.swt.window.GLCanvas;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
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
    private static GGView currentview;
    private static Composite editarea;

    public static void main(String[] args) {
        initSWT();
        
        ExtensionManager.addExtension(new SWTExtension(shell, display));
        
        WindowInfo w = new WindowInfo();
        w.width = 640;
        w.height = 480;
        w.resizable = false;
        w.type = "SWT";
        w.vsync = true;
        OpenGG.initialize(new WorldEditor(), w);
    }

    public static void initSWT(){
        int minClientWidth = 1920;
        int minClientHeight = 1080;
        display = new Display();
        shell = new Shell(display);

        GridLayout layout = new GridLayout();
        layout.numColumns = 4;

        shell.setLayout(layout);
        shell.setText("World Editor");

        Menu menuBar = new Menu(shell, SWT.BAR);
        MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeFileMenu.setText("&File");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        cascadeFileMenu.setMenu(fileMenu);

        MenuItem cascadeEditMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeEditMenu.setText("&Edit");

        MenuItem jarload = new MenuItem(fileMenu, SWT.CASCADE);
        jarload.setText("Load game JAR");
        jarload.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[]{"*.jar"});
                dialog.setFilterPath(Resource.getLocal(""));
                String result = dialog.open();
                ViewModelComponentRegistry.clearRegistry();
                ViewModelComponentRegistry.initialize();
                ViewModelComponentRegistry.registerAllFromJar(result);
                ViewModelComponentRegistry.createRegisters();
            }
        });
        
        MenuItem mapload = new MenuItem(fileMenu, SWT.PUSH);
        mapload.setText("Load map");

        shell.setMenuBar(menuBar);
        
        Composite c2 = new Composite(shell,SWT.BORDER);
        GridData treedata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        c2.setLayoutData(treedata);
        c2.setLayout(new FillLayout());

        tree = new Tree(c2, SWT.V_SCROLL);
        tree.addListener(SWT.Selection, (Event event) -> {
            processTreeEvent(event);
        });
        tree.pack();

        shell.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.stateMask == SWT.ALT && (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR)) {
                    shell.setFullScreen(!shell.getFullScreen());
                }
            }
        });
        
        int dw = shell.getSize().x - shell.getClientArea().width;
        int dh = shell.getSize().y - shell.getClientArea().height;
        shell.setMinimumSize(minClientWidth + dw, minClientHeight + dh);
        
        shell.addListener(SWT.Traverse, (Event event) -> {
            switch (event.detail) {
                case SWT.TRAVERSE_ESCAPE:
                    shell.close();
                    event.detail = SWT.TRAVERSE_NONE;
                    event.doit = false;
                    break;
                default:
                    break;
            }
        });
    }
    
    public static void initSWT2(){
        editarea = new Composite(shell, SWT.BORDER);
        editarea.setLayout(new GridLayout(5, false));
        editarea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        
        ScrolledComposite addregion = new ScrolledComposite(shell, SWT.BORDER);
        addregion.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        ScrolledComposite console = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
        console.setLayout(new FillLayout());
        console.setExpandHorizontal(true);
        console.setExpandVertical(true);
        console.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
        console.setMinHeight(120);
        console.layout();
        
        Text consoletext = new Text(console, SWT.READ_ONLY | SWT.MULTI);
        
        console.setContent(consoletext);

        
        PrintStream oldout = System.out;    
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (consoletext.isDisposed()) return;
                consoletext.append(String.valueOf((char) b));
                consoletext.pack();
                oldout.write(b);
            }
        };
        
        System.setOut(new PrintStream(out));
    }
    
    @Override
    public void setup() {
        initSWT2();
        GLCanvas localcanvas = ((GGCanvas)OpenGG.getWindow()).getCanvas();
        localcanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
        
        WorldEngine.getCurrent().setEnabled(false);
        GGFont font = Resource.getFont("test", "test.png");
        com.opengg.core.render.Text text = new com.opengg.core.render.Text("Turmoil has engulfed the Galactic Republic. The taxation of trade routes to outlying star systems is in dispute. \n\n"
                + " Hoping to resolve the matter with a blockade of deadly battleships, "
                + " the greedy Trade Federation has stopped all shipping to the small planet of Naboo. \n\n"
                + " While the congress of the Republic endlessly debates this alarming chain of events,"
                + " the Supreme Chancellor has secretly dispatched two Jedi Knights,"
                + " the guardians of peace and justice in the galaxy, to settle the conflict...", new Vector2f(), 1f, 0.5f, false);
        GUI.addItem("aids", new GUIText(text, font, new Vector2f(0f,0)));
        
        ViewModelComponentRegistry.createRegisters();
        
        BindController.addBind(ControlType.KEYBOARD, "forward", KEY_W);
        BindController.addBind(ControlType.KEYBOARD, "backward", KEY_S);
        BindController.addBind(ControlType.KEYBOARD, "left", KEY_A);
        BindController.addBind(ControlType.KEYBOARD, "right", KEY_D);
        BindController.addBind(ControlType.KEYBOARD, "up", KEY_SPACE);
        BindController.addBind(ControlType.KEYBOARD, "down", KEY_LEFT_SHIFT);
        BindController.addBind(ControlType.KEYBOARD, "lookright", KEY_RIGHT);
        BindController.addBind(ControlType.KEYBOARD, "lookleft", KEY_LEFT);
        BindController.addBind(ControlType.KEYBOARD, "lookup", KEY_UP);
        BindController.addBind(ControlType.KEYBOARD, "lookdown", KEY_DOWN);
        
        RenderEngine.setSkybox(new Skybox(Texture.getCubemap(
                Resource.getTexturePath("skybox\\majestic_ft.png"),
                Resource.getTexturePath("skybox\\majestic_bk.png"),
                Resource.getTexturePath("skybox\\majestic_up.png"),
                Resource.getTexturePath("skybox\\majestic_dn.png"),
                Resource.getTexturePath("skybox\\majestic_rt.png"),
                Resource.getTexturePath("skybox\\majestic_lf.png")), 1500f));
        
        WorldObject w = new WorldObject();
        w.attach(new WorldObject());
        WorldEngine.getCurrent().attach(w);
        WorldEngine.getCurrent().attach(new WorldObject());
        WorldEngine.getCurrent().attach(new WorldObject());
        World world = WorldEngine.getCurrent();
        tree.removeAll();
        for(Component compo : world.getChildren()){
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText(compo.getClass().getSimpleName() + ": " + compo.getId());
            mystery6(compo, item);
        }
        shell.open();
    }

    @Override
    public void render() {
        ShaderController.setPerspective(90, OpenGG.getWindow().getRatio(), 0.2f, 3000f);
    }

    @Override
    public void update() {
        
    }
    
    public static void mystery6(Component comp, TreeItem treepart){
        for(Component compo : comp.getChildren()){
            TreeItem item = new TreeItem(treepart, SWT.NONE);
            item.setText(compo.getClass().getSimpleName() + ": " + compo.getId());
            mystery6(compo, item);
        }
    }
    
    public static void processTreeEvent(Event e){
        TreeItem item = (TreeItem)e.item;
        String text = item.getText();
        int id = Integer.parseInt(text.substring(text.lastIndexOf(":") + 2));
        
        Component component = WorldEngine.getCurrent().find(id);
        Class clazz = component.getClass();
        Class vmclass = ViewModelComponentRegistry.findViewModel(clazz);
        
        try {
            ComponentViewModel cvm = (ComponentViewModel) vmclass.newInstance();
            cvm.setComponent(component);
            useViewModel(cvm);
            
        } catch (InstantiationException | IllegalAccessException ex) {
            GGConsole.error("Failed to create instance of a ComponentViewModel for " + component.getName()+ ", is there a default constructor?");
        }
    }
    
    public static void useViewModel(ComponentViewModel cvm){
        for (Control control : editarea.getChildren()) {
            control.dispose();
        }
        
        GGView view = new GGView(editarea, cvm);
        editarea.layout();
    }
    
    public static void addDecimalListener(Text textField){
        textField.addVerifyListener((VerifyEvent e) -> {
            Text text = (Text)e.getSource();
            String oldS = text.getText();
            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
            
            boolean valid = true;
            try{
                Float.parseFloat(newS);
            }catch(NumberFormatException ex){
                valid = false;
            }
            
            if(!valid)
                e.doit = false;
        });
    }
    
    public static void addIntegerListener(Text textField){
        textField.addVerifyListener((VerifyEvent e) -> {
            Text text = (Text)e.getSource();
            String oldS = text.getText();
            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
            
            boolean valid = true;
            try{
                Integer.parseInt(newS);
            }catch(NumberFormatException ex){
                valid = false;
            }
            
            if(!valid)
                e.doit = false;
        });
    }
}
