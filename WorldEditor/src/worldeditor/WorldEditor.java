/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.console.GGConsole;
import com.opengg.core.engine.*;
import com.opengg.core.extension.ExtensionManager;
import com.opengg.core.io.ControlType;
import com.opengg.core.math.Matrix4f;
import com.opengg.core.math.Quaternionf;
import com.opengg.core.math.Vector3f;
import com.opengg.core.math.Vector3fm;
import com.opengg.core.model.Model;
import com.opengg.core.render.*;
import com.opengg.core.render.drawn.Drawable;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.core.world.*;
import com.opengg.core.world.components.Component;
import com.opengg.core.world.components.viewmodel.Initializer;
import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegistry;
import com.opengg.module.swt.SWTExtension;
import com.opengg.module.swt.window.GGCanvas;
import com.opengg.module.swt.window.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Text;
import worldeditor.assetloader.AssetShell;

import static com.opengg.core.io.input.keyboard.Key.*;

public class WorldEditor extends GGApplication implements Actionable{

    private static Display display;
    private static Shell shell;
    private static Tree tree;
    private static GGView currentview;
    private static Composite editarea;
    private static Composite addregion;
    private static boolean refresh;
    private static Composite treearea;
    private static Vector3fm control = new Vector3fm();
    private static Vector3fm controlrot = new Vector3fm();
    private static Vector3fm currot = new Vector3fm();
    private static float rotspeed = 30;
    private static Camera cam;
    private static EditorTransmitter transmitter;
    private static Text consoletext;
    int i = 0;

    public static void main(String[] args){
        Thread ui = new Thread(() -> {
            initSWT();
        });
        ui.setName("UI Thread");
        ui.start();

        try{
            Thread.sleep(1000);
        }catch(InterruptedException ex){
        }

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

        var menuBar = new Menu(shell, SWT.BAR);

        var cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeFileMenu.setText("&File");

        var fileMenu = new Menu(shell, SWT.DROP_DOWN);
        cascadeFileMenu.setMenu(fileMenu);

        var cascadeEditMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeEditMenu.setText("&Edit");

        var cascadeWorldMenu = new MenuItem(menuBar, SWT.CASCADE);
        cascadeWorldMenu.setText("&World");

        var worldMenu = new Menu(shell, SWT.DROP_DOWN);
        cascadeWorldMenu.setMenu(worldMenu);

        var gamepath = new MenuItem(fileMenu, SWT.CASCADE);
        gamepath.setText("Set root game path");
        gamepath.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                var dialog = new DirectoryDialog(shell, SWT.OPEN);
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                String npath = dialog.open();
                Resource.setDefaultPath(npath);
                shell.setText("World Editor: " + npath);
                GGConsole.log("Switched game path to " + npath);
            }
        });

        var jarload = new MenuItem(fileMenu, SWT.CASCADE);
        jarload.setText("Load game JAR");
        jarload.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                var dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Game JAR file");
                dialog.setFilterExtensions(new String[]{"*.jar"});
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                String result = dialog.open();
                if(result == null || result.isEmpty()) return;
                ViewModelComponentRegistry.clearRegistry();
                ViewModelComponentRegistry.registerAllFromJar(result);
                ViewModelComponentRegistry.createRegisters();
                updateAddRegion();
            }
        });

        var loadmap = new MenuItem(fileMenu, SWT.CASCADE);
        loadmap.setText("Load world");
        loadmap.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                var dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Load world...");
                dialog.setFilterExtensions(new String[]{"*.bwf"});
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                var result = dialog.open();
                if(result == null){
                    return;
                }
                OpenGG.syncExec(() -> {
                    WorldEngine.useWorld(WorldEngine.loadWorld(result));
                    RenderEngine.useCamera(cam);
                    BindController.addController(transmitter);
                });

                refreshComponentList();
            }
        });

        var savemap = new MenuItem(fileMenu, SWT.CASCADE);
        savemap.setText("Save world");
        savemap.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                var dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("Save world...");
                dialog.setFilterExtensions(new String[]{"*.bwf"});
                dialog.setFilterPath(Resource.getAbsoluteFromLocal(""));
                var result = dialog.open();
                if(result == null) return;

                OpenGG.asyncExec(() -> WorldEngine.saveWorld(WorldEngine.getCurrent(), result));
                refreshComponentList();
            }
        });

        var FileMenu = new MenuItem(menuBar, SWT.CASCADE);
        FileMenu.setText("&AssetLoader");
        FileMenu.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                AssetShell.loadModel(shell);
            }

        });

        shell.setMenuBar(menuBar);

        var treedata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        treearea = new Composite(shell, SWT.BORDER);
        treearea.setLayoutData(treedata);
        treearea.setLayout(new FillLayout());

        setupTree();

        int dw = shell.getSize().x - shell.getClientArea().width;
        int dh = shell.getSize().y - shell.getClientArea().height;
        shell.setMinimumSize(minClientWidth + dw, minClientHeight + dh);

        shell.addListener(SWT.Traverse, (event) -> {
            switch(event.detail){
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
            if(!display.readAndDispatch()){
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

        consoletext = new Text(console, SWT.READ_ONLY | SWT.MULTI);

        console.setContent(consoletext);
         /*PrintStream oldout = System.out;
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
        GLCanvas localcanvas = ((GGCanvas) OpenGG.getWindow()).getCanvas();
        localcanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
    }

    public static void useTreeItem(TreeItem item){
        clearArea(editarea);

        String text = item.getText();
        int id = Integer.parseInt(text.substring(text.lastIndexOf(":") + 2));

        Component component = WorldEngine.getCurrent().find(id);
        Class clazz = component.getClass();
        Class vmclass = ViewModelComponentRegistry.findViewModel(clazz);

        if(vmclass == null){
            clearArea(editarea);
            currentview = null;
            return;
        }

        OpenGG.asyncExec(() -> {
            try{
                ViewModel cvm = (ViewModel) vmclass.newInstance();
                cvm.setComponent(component);
                cvm.updateLocal();
                display.syncExec(() -> {
                    useViewModel(cvm);
                });
            }catch(InstantiationException | IllegalAccessException ex){
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
            Text text = (Text) e.getSource();
            String oldS = text.getText();
            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

            boolean valid = true;
            try{
                Float.parseFloat(newS);
            }catch(NumberFormatException ex){
                valid = false;
            }

            if(!valid){
                e.doit = false;
            }
        });
    }

    public static void addIntegerListener(Text textField){
        textField.addVerifyListener((VerifyEvent e) -> {
            Text text = (Text) e.getSource();
            String oldS = text.getText();
            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

            boolean valid = true;
            try{
                Integer.parseInt(newS);
            }catch(NumberFormatException ex){
                valid = false;
            }

            if(!valid){
                e.doit = false;
            }
        });
    }

    public static void updateAddRegion(){
        clearArea(addregion);
        try{
            Thread.sleep(100);
        }catch(Exception e){

        }

        List classes = new List(addregion, SWT.V_SCROLL);
        addregion.layout();
        for(var info : ViewModelComponentRegistry.getAllRegistries()){
            classes.add(info.getComponent().getSimpleName());
        }

        classes.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent event){
                if(classes.getSelectionIndex() < 0) return;
                var info = ViewModelComponentRegistry.getAllRegistries().get(classes.getSelectionIndex());
                Class clazz = info.getComponent();
                Class vmclazz = ViewModelComponentRegistry.findViewModel(clazz);

                try{
                    var viewmodel = (ViewModel) vmclazz.getDeclaredConstructor().newInstance();
                    var initializer = viewmodel.getInitializer(new Initializer());
                    if(initializer.elements.isEmpty()){
                        createComponent(initializer, viewmodel);
                    }else{
                        NewComponentShell ncs = new NewComponentShell(initializer, shell, viewmodel);
                        ncs.nshell.addDisposeListener(e -> shell.setEnabled(true));

                        shell.setEnabled(false);
                    }
                }catch(Exception ex){
                    GGConsole.error("Failed to create instance of a ViewModel for " + clazz.getName() + ", is there a default constructor?");
                }
            }

        });
    }

    public static void markForRefresh(){
        refresh = true;
    }

    public static void refreshComponentList(){
        var world = WorldEngine.getCurrent();

        tree.removeAll();
        for(var compo : world.getChildren()){
            var item = new TreeItem(tree, SWT.NONE);
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
        for(var compo : comp.getChildren()){
            var item = new TreeItem(treepart, SWT.NONE);
            item.setText(compo.getClass().getSimpleName() + ": " + compo.getId());
            mystery6(compo, item);
        }
    }

    public static void clearArea(Composite region){
        for(var control : region.getChildren()){
            control.dispose();
        }
    }

    public static void createComponent(Initializer vmi, ViewModel cvm){
        OpenGG.asyncExec(() -> {
            var ncomp = cvm.getFromInitializer(vmi);
            WorldEngine.getCurrent().attach(ncomp);
            WorldEngine.getCurrent().rescanRenderables();
            display.asyncExec(() -> {
                refreshComponentList();
                useTreeItem(tree.getItems()[tree.getItems().length - 1]);
                tree.setSelection(tree.getItems()[tree.getItems().length - 1]);
            });
        });

    }

    public static void setupTree(){
        tree = new Tree(treearea, SWT.V_SCROLL);
        TreeItem[] dragitem = new TreeItem[1];

        DragSource source = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        source.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        source.addDragListener(new DragSourceListener(){

            @Override
            public void dragStart(DragSourceEvent event){
                TreeItem[] selection = tree.getSelection();
                if(selection.length > 0 && selection[0].getItemCount() == 0){
                    event.doit = true;
                    dragitem[0] = selection[0];
                }else{
                    event.doit = false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event){
                event.data = dragitem[0].getText();
            }

            @Override
            public void dragFinished(DragSourceEvent event){
                if(event.detail == DND.DROP_MOVE){
                    dragitem[0].dispose();
                }
                dragitem[0] = null;
            }

        });

        DropTarget target = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        target.setTransfer(new Transfer[]{TextTransfer.getInstance()});

        target.addDropListener(new DropTargetAdapter(){
            public void dragOver(DropTargetEvent event){
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
                if(event.item != null){
                    TreeItem item = (TreeItem) event.item;
                    Point pt = display.map(null, tree, event.x, event.y);
                    Rectangle bounds = item.getBounds();
                    if(pt.y < bounds.y + bounds.height / 3){
                        event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
                    }else if(pt.y > bounds.y + 2 * bounds.height / 3){
                        event.feedback |= DND.FEEDBACK_INSERT_AFTER;
                    }else{
                        event.feedback |= DND.FEEDBACK_SELECT;
                    }
                }
            }

            public void drop(DropTargetEvent event){
                if(event.data == null){
                    event.detail = DND.DROP_NONE;
                    return;
                }
                String text = (String) event.data;
                if(event.item == null){
                    TreeItem item = new TreeItem(tree, SWT.NONE);
                    item.setText(text);

                    int id = Integer.parseInt(text.substring(text.lastIndexOf(":") + 2));
                    Component nchild = WorldEngine.getCurrent().find(id);

                    WorldEngine.getCurrent().attach(nchild);
                }else{
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
            public void widgetSelected(SelectionEvent event){
                useTreeItem((TreeItem) event.item);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event){
                useTreeItem((TreeItem) event.item);
            }
        });
        tree.pack();
    }

    public static void setView(GGView view){
        currentview = view;
    }

    public static void close(){

    }

    @Override
    public void setup(){
        ViewModelComponentRegistry.initialize();
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

        cam = new Camera();
        RenderEngine.useCamera(cam);
        cam.setPos(new Vector3f(0, 0, -10));

        transmitter = new EditorTransmitter();
        transmitter.editor = this;

        BindController.addController(transmitter);
        WorldEngine.shouldUpdate(false);
        RenderEngine.setProjectionData(ProjectionData.getPerspective(100, 0.2f, 3000f));
        Model m = Resource.getModel("sphere");
        Drawable pos = m.getDrawable();
        RenderGroup group = new RenderGroup("renderer");
        group.add(pos);

        RenderEngine.addRenderPath(new RenderPath("editorrender", () -> {
            for(Component c : WorldEngine.getCurrent().getAll()){
                if(c instanceof Renderable) continue;
                pos.setMatrix(new Matrix4f().translate(c.getPosition()).rotate(c.getRotation()).scale(new Vector3f(0.4f)));
                group.render();
            }
        }));

        Executable autosave = new Executable(){
            @Override
            public void execute(){
                try{
                    GGConsole.log("Autosaving world to autosave.bwf...");
                    WorldEngine.saveWorld(WorldEngine.getCurrent(), "autosave.bwf");
                    GGConsole.log("Autosave completed!");
                    OpenGG.asyncExec(60 * 3, this);
                }catch(Exception e){
                    GGConsole.warn("Failed to autosave world!");
                }
            }
        };

        OpenGG.asyncExec(60 * 5, autosave);
    }

    @Override
    public void render(){
        //ShaderController.setPerspective(90, OpenGG.getWindow().getRatio(), 0.2f, 3000f);
    }

    @Override
    public void update(float delta){
        if(display.isDisposed()){
            return;
        }
        display.syncExec(() -> {
            i++;
            if(i == 15){
                i = 0;
                if(currentview != null && currentview.isComplete()){
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
        cam.setRot(new Quaternionf(new Vector3f(0, currot.y, currot.z)).multiply(new Quaternionf(new Vector3f(currot.x, 0, 0))));

        Vector3f nvector = cam.getRot().invert().transform(new Vector3f(control).multiply(delta * 5));

        cam.setPos(cam.getPos().add(nvector.multiply(10)));
        display.asyncExec(() -> {
            consoletext.setText(cam.getPos().add(nvector.multiply(10)).toString());
        });

    }

    @Override
    public void onAction(Action action){
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
                    controlrot = new Vector3fm();
                    control = new Vector3fm();
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
}
