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
import com.opengg.core.io.ControlType;
import static com.opengg.core.io.input.keyboard.Key.*;
import com.opengg.core.math.Quaternionf;
import com.opengg.core.math.Vector3f;
import com.opengg.core.render.shader.ShaderController;
import com.opengg.core.render.texture.Texture;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.core.world.Action;
import com.opengg.core.world.ActionType;
import com.opengg.core.world.Actionable;
import com.opengg.core.world.Camera;
import com.opengg.core.world.Skybox;
import com.opengg.core.world.World;
import com.opengg.core.world.components.Component;
import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegisterInfoContainer;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegistry;
import com.opengg.core.world.components.viewmodel.Initializer;
import com.opengg.module.swt.SWTExtension;
import com.opengg.module.swt.window.GGCanvas;
import com.opengg.module.swt.window.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class WorldEditor extends GGApplication implements Actionable{
    private static Display display;
    private static Shell shell;
    private static Tree tree;
    private static GGView currentview;
    private static Composite editarea;
    private static Composite addregion;
    private static boolean refresh;
    private static Composite treearea;
    private static Vector3f control = new Vector3f();
    private static Vector3f controlrot = new Vector3f();
    private static Vector3f currot = new Vector3f();
    private static float rotspeed = 30;
    private static Camera cam;
    private static EditorTransmitter transmitter;

    public static void main(String[] args) {
        Thread ui = new Thread(() -> {
            initSWT();
        });
        ui.setName("UI Thread");
        ui.start();
        
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {}
        
        
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

        Image image = new Image(display, "resources\\tex\\emak.png");
        shell.setImage(image);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = true;

        shell.setLayout(layout);
        shell.setText("World Editor");

        Menu menuBar = new Menu(shell, SWT.BAR);
        MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeFileMenu.setText("&File");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        cascadeFileMenu.setMenu(fileMenu);

        MenuItem cascadeEditMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeEditMenu.setText("&Edit");

        MenuItem gamepath = new MenuItem(fileMenu, SWT.CASCADE);
        gamepath.setText("Set root game path");
        gamepath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                String npath = dialog.open();
                Resource.setDefaultPath(npath);
                shell.setText("World Editor: " + npath);
            }
        });
        
        MenuItem jarload = new MenuItem(fileMenu, SWT.CASCADE);
        jarload.setText("Load game JAR");
        jarload.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Game JAR file");
                dialog.setFilterExtensions(new String[]{"*.jar"});
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                String result = dialog.open();
                ViewModelComponentRegistry.clearRegistry();
                ViewModelComponentRegistry.initialize();
                ViewModelComponentRegistry.registerAllFromJar(result);
                ViewModelComponentRegistry.createRegisters();
                updateAddRegion();
            }
        });
        
        MenuItem loadmap = new MenuItem(fileMenu, SWT.CASCADE);
        loadmap.setText("Load world");
        loadmap.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Load world...");
                dialog.setFilterExtensions(new String[]{"*.bwf"});
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                String result = dialog.open();
                if(result == null)
                    return;
                OpenGG.addExecutable(() -> {
                    WorldEngine.useWorld(WorldEngine.loadWorld(result));
                    RenderEngine.useCamera(cam);
                    BindController.setOnlyController(transmitter);
                    display.syncExec(() -> {
                        refreshComponentList();
                    });
                });
            }
        });
        
        MenuItem savemap = new MenuItem(fileMenu, SWT.CASCADE);
        savemap.setText("Save world");
        savemap.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Save world...");
                dialog.setFilterExtensions(new String[]{"*.bwf"});
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                String result = dialog.open();
                if(result == null)
                    return;
                OpenGG.addExecutable(() -> {
                    WorldEngine.saveWorld(WorldEngine.getCurrent(), result);

                    display.syncExec(() -> {
                        refreshComponentList();
                    });
                });
            }
        });

        shell.setMenuBar(menuBar);
        
        treearea = new Composite(shell,SWT.BORDER);
        GridData treedata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        treearea.setLayoutData(treedata);
        treearea.setLayout(new FillLayout());

        setupTree();
        
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
        
        while(!shell.isDisposed()){
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        close();
        display.dispose();
    }
    
    public static void initSWT2(){
        editarea = new Composite(shell, SWT.BORDER);
        editarea.setLayout(new GridLayout(6, false));
        editarea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        
        addregion = new Composite(shell, SWT.BORDER);
        addregion.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        addregion.setLayout(new FillLayout());
        
        ScrolledComposite console = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
        console.setLayout(new FillLayout());
        console.setExpandHorizontal(true);
        console.setExpandVertical(true);
        console.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
        console.setMinHeight(120);
        console.layout();
        
        Text consoletext = new Text(console, SWT.READ_ONLY | SWT.MULTI);
        /*
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
        */
        GLCanvas localcanvas = ((GGCanvas)OpenGG.getWindow()).getCanvas();
        localcanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
    }
    
    @Override
    public void setup(){
        ViewModelComponentRegistry.createRegisters();
        WorldEngine.getCurrent().setEnabled(false);
        
        display.asyncExec(() -> {
            initSWT2();
            refreshComponentList();
        
            updateAddRegion();
            shell.open();
        });       
        
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
        BindController.addBind(ControlType.KEYBOARD, "clear", KEY_G);
        
        RenderEngine.setSkybox(new Skybox(Texture.getCubemap(
                Resource.getTexturePath("skybox\\majestic_ft.png"),
                Resource.getTexturePath("skybox\\majestic_bk.png"),
                Resource.getTexturePath("skybox\\majestic_up.png"),
                Resource.getTexturePath("skybox\\majestic_dn.png"),
                Resource.getTexturePath("skybox\\majestic_rt.png"),
                Resource.getTexturePath("skybox\\majestic_lf.png")), 1500f));
 
        cam = new Camera();
        RenderEngine.useCamera(cam);
        cam.setPos(new Vector3f(0,0,-10));
        
        transmitter = new EditorTransmitter();
        transmitter.editor = this;
        
        BindController.setOnlyController(transmitter);
        WorldEngine.shouldUpdate(false);
    }

    @Override
    public void render() {
        ShaderController.setPerspective(90, OpenGG.getWindow().getRatio(), 0.2f, 3000f);
    }

    int i = 0;
    
    @Override
    public void update(float delta) {
        display.syncExec(() -> {
            i++;
            if(i == 15){
                i = 0;
                if(currentview != null && currentview.complete){
                    currentview.update();
                }
            } 

            if(refresh){
                refreshComponentList();
                refresh = false;
            }
        });
        
        currot.x += controlrot.x * rotspeed * delta;
        currot.y += controlrot.y * rotspeed * delta;
        currot.z += controlrot.z * rotspeed * delta;
        cam.setRot(new Quaternionf(new Vector3f(0,currot.y,currot.z)).multiply(new Quaternionf(new Vector3f(currot.x,0,0))));    
       
        Vector3f nvector = control.multiply(delta * 15);
        nvector = cam.getRot().invert().transform(nvector);
        cam.setPos(cam.getPos().addThis(nvector));
    }
 
    public static void useTreeItem(TreeItem item){
        clearArea(editarea);
        
        String text = item.getText();
        int id = Integer.parseInt(text.substring(text.lastIndexOf(":") + 2));
        
        Component component = WorldEngine.getCurrent().find(id);
        Class clazz = component.getClass();
        Class vmclass = ViewModelComponentRegistry.findViewModel(clazz);
        if(vmclass == null){
            for (Control control : editarea.getChildren()) {
                control.dispose();
            }
            currentview = null;
            return;
        }
        
        
        OpenGG.addExecutable(() -> {
            try {
                ViewModel cvm = (ViewModel) vmclass.newInstance();
                cvm.setComponent(component);
                cvm.updateLocal();
                display.syncExec(() -> {
                    useViewModel(cvm);
                });
            } catch (InstantiationException | IllegalAccessException ex) {
                GGConsole.error("Failed to create instance of a ComponentViewModel for " + component.getName() + ", is there a default constructor?");
            }               
        });
            
            
        
    }
    
    public static void useViewModel(ViewModel cvm){
        clearArea(editarea);
        
        GGView view = new GGView(editarea, cvm);
        currentview = view;
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
    
    public static void updateAddRegion(){
        clearArea(addregion);
        
        List classes = new List(addregion, SWT.V_SCROLL);
        
        for(ViewModelComponentRegisterInfoContainer info : ViewModelComponentRegistry.getAllRegistries()){
            classes.add(info.component.getSimpleName());
        }
        
        classes.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if(classes.getSelectionIndex() < 0)
                    return;
                ViewModelComponentRegisterInfoContainer info = ViewModelComponentRegistry.getAllRegistries().get(classes.getSelectionIndex());
                Class clazz = info.component;
                Class vmclazz = ViewModelComponentRegistry.findViewModel(clazz);
                
                try {
                    ViewModel cvm = (ViewModel) vmclazz.newInstance();
                    Initializer vmi = cvm.getInitializer();
                    if(vmi.elements.isEmpty()){
                        createComponent(vmi, cvm);
                    }else{
                        NewComponentShell ncs = new NewComponentShell(vmi, shell, cvm);
                        ncs.nshell.addDisposeListener(new DisposeListener(){
                            @Override
                            public void widgetDisposed(DisposeEvent e) {
                                shell.setEnabled(true);
                            }
                        });
                        
                        shell.setEnabled(false);
                    }
                } catch (InstantiationException | IllegalAccessException ex) {
                    GGConsole.error("Failed to create instance of a ComponentViewModel for " + clazz.getName()+ ", is there a default constructor?");
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {

            }
        });
    }
    
    public static void markForRefresh(){
        refresh = true;
    }
    
    public static void refreshComponentList(){
        World world = WorldEngine.getCurrent();

        tree.removeAll();
        for(Component compo : world.getChildren()){
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText(compo.getClass().getSimpleName() + ": " + compo.getId());
            mystery6(compo, item);
        }
        clearArea(editarea);
        if(tree.getItems().length == 0){
            currentview = null;
        }else{
            tree.setSelection(tree.getItem(0));
            useTreeItem(tree.getItem(0));
        }
    }
    
    public static void mystery6(Component comp, TreeItem treepart){
        for(Component compo : comp.getChildren()){
            TreeItem item = new TreeItem(treepart, SWT.NONE);
            item.setText(compo.getClass().getSimpleName() + ": " + compo.getId());
            mystery6(compo, item);
        }
    }
    
    public static void clearArea(Composite region){
        for (Control control : region.getChildren()) {
            control.dispose();
        }
    }
    
    public static void createComponent(Initializer vmi, ViewModel cvm){
        OpenGG.addExecutable(() -> {
            Component ncomp = cvm.getFromInitializer(vmi);
            WorldEngine.getCurrent().attach(ncomp);
            WorldEngine.rescanCurrent();
            display.asyncExec(() -> {
                refreshComponentList();
                useTreeItem(tree.getItems()[tree.getItems().length-1]);
                tree.setSelection(tree.getItems()[tree.getItems().length-1]);
            });
        });
        
    }
    
    public static void setupTree(){
        tree = new Tree(treearea, SWT.V_SCROLL);
        TreeItem[] dragitem = new TreeItem[1];
        
        DragSource source = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);       
        source.setTransfer(new Transfer[] { TextTransfer.getInstance() });
        source.addDragListener(new DragSourceListener(){

            @Override
            public void dragStart(DragSourceEvent event) {
                TreeItem[] selection = tree.getSelection();
                if (selection.length > 0 && selection[0].getItemCount() == 0) {
                    event.doit = true;
                    dragitem[0] = selection[0];
                } else {
                    event.doit = false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = dragitem[0].getText();
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                if (event.detail == DND.DROP_MOVE)
                    dragitem[0].dispose();
                dragitem[0] = null;
            }
        
        });
        
        DropTarget target = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
        
        target.addDropListener(new DropTargetAdapter(){
            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
                if (event.item != null) {
                    TreeItem item = (TreeItem) event.item;
                    Point pt = display.map(null, tree, event.x, event.y);
                    Rectangle bounds = item.getBounds();
                    if (pt.y < bounds.y + bounds.height / 3) {
                        event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
                    } else if (pt.y > bounds.y + 2 * bounds.height / 3) {
                        event.feedback |= DND.FEEDBACK_INSERT_AFTER;
                    } else {
                        event.feedback |= DND.FEEDBACK_SELECT;
                    }
                }
            }

            public void drop(DropTargetEvent event) {
                if (event.data == null) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                String text = (String) event.data;
                if (event.item == null) {
                    TreeItem item = new TreeItem(tree, SWT.NONE);
                    item.setText(text);
                    
                    int id = Integer.parseInt(text.substring(text.lastIndexOf(":") + 2));
                    Component nchild = WorldEngine.getCurrent().find(id);
                    
                    WorldEngine.getCurrent().attach(nchild);
                } else {
                    TreeItem item = (TreeItem) event.item;
                    String ntext = item.getText();
                    
                    int id = Integer.parseInt(ntext.substring(ntext.lastIndexOf(":") + 2));
                    Component parent = WorldEngine.getCurrent().find(id);
                    
                    int id2 = Integer.parseInt(text.substring(text.lastIndexOf(":") + 2));
                    Component child = WorldEngine.getCurrent().find(id2);
                    
                    parent.attach(child);
                    
                    refreshComponentList();
                }
            }
        });
        
        tree.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetSelected(SelectionEvent event) {
                useTreeItem((TreeItem)event.item);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                useTreeItem((TreeItem)event.item);
            }
        });
        tree.pack();
    }
    
    public static void setView(GGView view){
        currentview = view;
    }

    @Override
    public void onAction(Action action) {
        if(action.type == ActionType.PRESS){
            switch(action.name){
                case "forward":
                    control.z += 1;
                    break;
                case "backward":
                    control.z -= 1;
                    break;
                case "left":
                    control.x += 1;
                    break;
                case "right":
                    control.x -= 1;
                    break;
                case "up":
                    control.y -= 1;
                    break;
                case "down":
                    control.y += 1;
                    break;
                case "lookright":
                    controlrot.y += 1;
                    break;
                case "lookleft":
                    controlrot.y -= 1;
                    break;
                 case "lookup":
                    controlrot.x -= 1;
                    break;
                case "lookdown":
                    controlrot.x += 1;
                    break;
                case "clear":
                    controlrot = new Vector3f();
                    control = new Vector3f();
                    break;
            }
        }else{
            switch(action.name){
                case "forward":
                    control.z -= 1;
                    break;
                case "backward":
                    control.z += 1;
                    break;
                case "left":
                    control.x -= 1;
                    break;
                case "right":
                    control.x += 1;
                    break;
                case "up":
                    control.y += 1;
                    break;
                case "down":
                    control.y -= 1;
                    break;
                case "lookright":
                    controlrot.y -= 1;
                    break;
                case "lookleft":
                    controlrot.y += 1;
                    break;
                case "lookup":
                    controlrot.x += 1;
                    break;
                case "lookdown":
                    controlrot.x -= 1;
                    break;

            }
        }
    }
    
    public static void close(){
        
    }
}
