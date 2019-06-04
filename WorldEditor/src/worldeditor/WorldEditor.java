/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.GGInfo;
import com.opengg.core.console.DefaultLoggerOutputConsumer;
import com.opengg.core.console.GGConsole;
import com.opengg.core.console.Level;
import com.opengg.core.engine.*;
import com.opengg.core.extension.ExtensionManager;
import com.opengg.core.gui.GUIController;
import com.opengg.core.io.ControlType;
import com.opengg.core.io.input.keyboard.KeyboardController;
import com.opengg.core.io.input.mouse.MouseController;
import com.opengg.core.math.*;
import com.opengg.core.model.Model;
import com.opengg.core.model.io.AssimpModelLoader;
import com.opengg.core.model.io.BMFFile;
import com.opengg.core.render.*;
import com.opengg.core.render.objects.ObjectCreator;
import com.opengg.core.render.texture.Texture;
import com.opengg.core.render.window.WindowController;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.core.util.JarClassUtil;
import com.opengg.core.world.Action;
import com.opengg.core.world.*;
import com.opengg.core.world.components.Component;
import com.opengg.core.world.components.viewmodel.Initializer;
import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegistry;
import com.opengg.ext.awt.AWTExtension;
import com.opengg.ext.awt.window.GGCanvas;
import worldeditor.assetloader.AssetDialog;
import worldeditor.guieditor.GUIEditor;
import worldeditor.scripteditor.ScriptEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

import static com.opengg.core.io.input.keyboard.Key.*;
import static java.util.Map.entry;


public class WorldEditor extends GGApplication implements Actionable{
    private static final Object initlock = new Object();
    private static JFrame window;
    private static JPanel mainpanel;
    private static JTree tree;

    private static GGView currentview;
    private static JPanel editarea;
    private static JPanel addregion;
    private static JPanel canvasregion;
    private static JPanel treearea;
    private static JTextArea consoletext;

    private static boolean refresh;
    private static Vector3fm control = new Vector3fm();
    private static Vector3fm controlrot = new Vector3fm();
    private static Camera cam;

    private static EditorTransmitter transmitter;
    private static DefaultTreeModel treeModel;
    private static DefaultMutableTreeNode upperTreeNode;

    private static Component currentComponent;

    private static String runtimeJar = "";
    private static GGApplication underlyingApp;

    public static void main(String[] args){
        //try {
            //ModelEditorWindow win = new ModelEditorWindow(AssimpModelLoader.loadModel("C:\\Users\\warre\\Desktop\\Models\\Wii U - Mario Kart 8 - 3DS Music Park\\3DS Music Park.obj"));
            ///win.setEnabled(true);
            //win.show();
        ///} catch (IOException e) {
           // e.printStackTrace();
        //}
        String initialDirectory;
        if(args.length > 0 && !args[0].isEmpty() && new File(args[0]).exists()){
            initialDirectory = args[0].trim();
        }else{
            var dialog = new JFileChooser(GGInfo.getApplicationPath());
            dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dialog.setApproveButtonText("Load game from directory...");
            dialog.showDialog(null, "Load game...");

            if(dialog.getSelectedFile() != null)
                initialDirectory = dialog.getSelectedFile().getAbsolutePath();
            else
                initialDirectory = "";
        }

        if(!initialDirectory.isEmpty()){
            Resource.setDefaultPath(initialDirectory);

            runtimeJar = Arrays.stream(Objects.requireNonNull(new File(initialDirectory).listFiles()))
                    .filter(f -> f.getName().contains("lib"))
                    .flatMap(s -> Arrays.stream(s.listFiles()))
                    .map(File::getAbsolutePath)
                    .filter(s -> s.contains(".jar"))
                    .filter(s -> !s.contains("lwjgl"))
                    .filter(s -> JarClassUtil.getAllClassesFromJar(s).stream()
                            .map(Objects::requireNonNull)
                            .anyMatch(GGApplication.class::isAssignableFrom))
                    .findAny().orElseThrow(() -> new RuntimeException("Failed to find any runnable OpenGG jarfile"));

        }

        Thread ui = new Thread(WorldEditor::initSwing);

        ui.setName("UI Thread");
        ui.start();

        synchronized (initlock){
            try {
                initlock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ExtensionManager.addExtension(new AWTExtension(canvasregion));

        WindowInfo w = new WindowInfo();
        w.width = 800;
        w.height = 600;
        w.resizable = false;
        w.type = "AWT";
        w.vsync = true;

        //OpenGG.initialize(new WorldEditor(), new InitializationOptions().setWindowInfo(w));
    }

    public static void initSwing() {
        try {
            boolean cool = true;
            if(cool) {
                Theme.applyTheme();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        window = new JFrame();
        window.setMinimumSize(new Dimension(1920, 1080));
        window.setIconImage(new ImageIcon("resources\\tex\\emak.png").getImage());
        window.setLayout(new BorderLayout());
        window.setTitle("World Editor");

        mainpanel = new JPanel();
        mainpanel.setMinimumSize(window.getMinimumSize());
        window.add(mainpanel);


        mainpanel.setLayout(new GridBagLayout());

        var menuBar = new JMenuBar();
        window.setJMenuBar(menuBar);

        var fileMenu = new JMenu();
        fileMenu.setText("File");

        var editMenu = new JMenu();
        editMenu.setText("Edit");

        var gameMenu = new JMenu();
        gameMenu.setText("Game");

        var worldMenu = new JMenu();
        worldMenu.setText("World");

        var objects = new JMenu();
        objects.setText("Objects");

        var assetLoader = new JMenuItem();
        assetLoader.setText("Asset Loader");
        assetLoader.addActionListener((e) -> new AssetDialog(getFrame()));

        var scriptEditor = new JMenuItem();
        scriptEditor.setText("Script Editor");
        scriptEditor.addActionListener((e) -> new ScriptEditor("New Script","").show());


        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(gameMenu);
        menuBar.add(worldMenu);
        menuBar.add(objects);
        menuBar.add(assetLoader);
        menuBar.add(scriptEditor);

        var newWorld = new JMenuItem();
        newWorld.setText("Create new world");
        newWorld.addActionListener((e) -> createWorld());

        var loadmap = new JMenuItem();
        loadmap.setText("Load world");
        loadmap.addActionListener((e) -> createWorldLoadChooser());

        var savemap = new JMenuItem();
        savemap.setText("Save world");
        savemap.addActionListener((e) -> createWorldSaveChooser());

        var quit = new JMenuItem();
        quit.setText("Quit");
        quit.addActionListener((e) -> System.exit(0));

        var reload = new JMenuItem();
        reload.setText("Restart/Reload jar");
        reload.addActionListener((e) -> restart());

        fileMenu.add(newWorld);
        fileMenu.add(loadmap);
        fileMenu.add(savemap);
        fileMenu.addSeparator();
        fileMenu.add(quit);
        fileMenu.add(reload);


        OpenGG.asyncExec(() -> generateObjectMenu(objects));


        var gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        var raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

        treearea = new JPanel();
        treearea.setLayout(new BorderLayout());
        treearea.setBorder(raisedetched);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        mainpanel.add(treearea, gbc);
        mainpanel.add(treearea, gbc);

        addregion = new JPanel();
        addregion.setLayout(new GridBagLayout());
        addregion.setBorder(raisedetched);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        mainpanel.add(addregion, gbc);

        canvasregion = new JPanel();
        canvasregion.setLayout(new BorderLayout());
        canvasregion.setBorder(raisedetched);
        canvasregion.setMinimumSize(new Dimension(800, 600));
        canvasregion.setMaximumSize(new Dimension(800, 600));
        canvasregion.setPreferredSize(new Dimension(800, 600));

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        mainpanel.add(canvasregion, gbc);

        editarea = new JPanel();
        editarea.setLayout(new GridBagLayout());
        editarea.setBorder(raisedetched);

        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        mainpanel.add(editarea, gbc);

        JScrollPane console = new JScrollPane();
        console.setLayout(new ScrollPaneLayout());
        console.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        console.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        console.setWheelScrollingEnabled(true);
        console.setPreferredSize(new Dimension(1000, 200));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.gridheight = 1;
        mainpanel.add(console, gbc);

        consoletext = new JTextArea();
        consoletext.setEditable(false);

        DefaultCaret caret = (DefaultCaret)consoletext.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        console.setViewportView(consoletext);

        GGConsole.addOutputConsumer(new DefaultLoggerOutputConsumer(Level.DEBUG, s -> consoletext.append(s + "\n")));

        setupTree();

         mainpanel.doLayout();
         window. addWindowListener(new WindowAdapter()  {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 OpenGG.endApplication();
                 e.getWindow().dispose();
             }
         });

        window.setVisible(true);

         synchronized (initlock){
             initlock.notifyAll();
         }
    }

    private static void generateObjectMenu(JMenu menu) {
        Map<String, Model> objectlist = Map.ofEntries(
                entry("Torus", Resource.getModel("defaults\\torus.bmf")),
                entry("Sphere", Resource.getModel("defaults\\sphere.bmf")),
                entry("HemiSphere", Resource.getModel("defaults\\hemi.bmf")),
                entry("Plane", Resource.getModel("defaults\\plane.bmf"))
        );
        for(Map.Entry<String,Model> entry:objectlist.entrySet()){
            JMenuItem item = new JMenuItem(entry.getKey());
            menu.add(item);
        }

    }

    private static void createWorld(){
        World world = new World();
        world.setEnabled(false);

        WorldEngine.useWorld(world);
    }

    private static void createWorldSaveChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.showSaveDialog(null);
        dialog.setFileFilter(new FileNameExtensionFilter("OpenGG world savefile", "bwf"));

        var resultfile = dialog.getSelectedFile();
        if (resultfile == null) return;
        var result = resultfile.getAbsolutePath();

        OpenGG.asyncExec(() -> WorldLoader.saveWorld(WorldEngine.getCurrent(), result));
        refreshComponentList();
    }

    private static void restart(){

    }

    private static void createWorldLoadChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.setFileFilter(new FileNameExtensionFilter("OpenGG world savefiles","bwf"));
        dialog.showOpenDialog(null);

        var resultfile = dialog.getSelectedFile();
        if (resultfile == null) return;
        var result = resultfile.getAbsolutePath();

        OpenGG.syncExec(() -> {
            WorldEngine.useWorld(WorldLoader.loadWorld(result));
            RenderEngine.useView(cam);
            BindController.addController(transmitter);
        });

        refreshComponentList();
    }

    private static void createComponentCreatorPanel(String name) {
        var info = ViewModelComponentRegistry.getByClassname(name);
        Class clazz = info.getComponent();
        Class vmclazz = ViewModelComponentRegistry.findViewModel(clazz);
        ViewModel viewmodel;

        try {
            viewmodel = (ViewModel) Objects.requireNonNull(vmclazz).getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            GGConsole.error("Failed to create instance of a ViewModel for " + clazz.getName() + ", is there a default constructor?");
            return;
        }

        var initializer = viewmodel.getInitializer(new Initializer());
        if (initializer.elements.isEmpty()) {
            createComponent(initializer, viewmodel);
        } else {
            NewComponentDialog ncs = new NewComponentDialog(initializer, viewmodel, getFrame());
        }
    }

    public static void createComponent(Initializer vmi, ViewModel cvm){
        OpenGG.asyncExec(() -> {
            try {
                var ncomp = cvm.getFromInitializer(vmi);
                WorldEngine.getCurrent().attach(ncomp);
                WorldEngine.getCurrent().rescanRenderables();
                refreshComponentList();
                ncomp.setPositionOffset(cam.getPosition());
                tree.setSelectionPath(findById(ncomp.getId()));
            }catch(Exception e){
                GGConsole.error("Failed to initialize component: " + e.getMessage());
                e.printStackTrace();
            }
        });

    }

    public static void useViewModel(ViewModel cvm){
        editarea.removeAll();
        GGView view = new GGView(cvm);
        editarea.add(view);
        editarea.validate();
        currentComponent = cvm.component;
        currentview = view;
    }

    public static void updateAddRegion(){
        addregion.removeAll();

        var strings = ViewModelComponentRegistry.getAllRegistries().stream()
                .map(reg -> reg.getComponent().getSimpleName())
                .sorted()
                .toArray(String[]::new);
        GridBagConstraints c = new GridBagConstraints();
        JList<String> classes = new JList<>(strings);
        classes.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        classes.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        classes.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value, int index, boolean isSelected,
                                                                   boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                listCellRendererComponent.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,Theme.buttonBG)
                        ,new EmptyBorder(4,4,4,4)));
                listCellRendererComponent.setHorizontalAlignment(CENTER);
                return listCellRendererComponent;
            }
        });


        JScrollPane listScroller = new JScrollPane(classes);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        listScroller.setMinimumSize(new Dimension(320,300));
        addregion.add(listScroller,c);

        JGradientButton creator = new JGradientButton("Create Component");
        c.gridy = 1;
        addregion.add(creator,c);

        addregion.validate();

        classes.addListSelectionListener(s -> {
            if (!s.getValueIsAdjusting()) {
                if (classes.getSelectedIndex() == -1) {
                    creator.setEnabled(false);

                } else {
                    creator.setEnabled(true);
                }
            }
        });

        creator.addActionListener((a) -> {
            var selection = classes.getSelectedValue();
            createComponentCreatorPanel(selection);
        });
    }

    public static void refreshComponentList(){
        var world = WorldEngine.getCurrent();

        upperTreeNode.removeAllChildren();
        treeModel.reload();
        for(var compo : world.getChildren()){
            mystery6(compo, upperTreeNode);
        }

        editarea.removeAll();

        var node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        //if(node != null){
        //    useTreeItem((String) node.getUserObject());
        //}
    }

    public static void mystery6(Component comp, DefaultMutableTreeNode parent){
        var child = addComponentToTree(comp, parent);
        for(var compo : comp.getChildren()){
            mystery6(compo, child);
        }
    }

    public static DefaultMutableTreeNode addComponentToTree(Component comp, DefaultMutableTreeNode parent){
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(comp.getClass().getSimpleName() + ": " + comp.getId());
        treeModel.insertNodeInto(child, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(child.getPath()));
        return child;
    }

    public static void setupTree(){
        upperTreeNode = new DefaultMutableTreeNode("Components");
        treeModel = new DefaultTreeModel(upperTreeNode);

        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);

        treearea.add(tree);

        tree.addTreeSelectionListener((e) -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    tree.getLastSelectedPathComponent();
            if(node == null) return;
            useTreeItem((String) node.getUserObject());
        });
        /*
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
                    Point pt = window.map(null, tree, event.x, event.y);
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


        tree.pack();*/
    }

    public static void useTreeItem(String item){
        int id = Integer.parseInt(item.substring(item.lastIndexOf(":") + 2));

        Component component = WorldEngine.getCurrent().find(id);
        Class clazz = component.getClass();
        Class vmclass = ViewModelComponentRegistry.findViewModel(clazz);

        if(vmclass == null){
            editarea.removeAll();
            currentview = null;
            return;
        }

        OpenGG.asyncExec(() -> {
            try{
                ViewModel cvm = (ViewModel) vmclass.getDeclaredConstructor().newInstance();
                cvm.setComponent(component);
                cvm.updateAll();
                useViewModel(cvm);
                window.setVisible(true);
            }catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex){
                GGConsole.error("Failed to create instance of a ComponentViewModel for " + component.getName() + ", is there a default constructor?");
            }
        });
    }

    private static TreePath findById(int id) {
        Enumeration<TreeNode> e = WorldEditor.upperTreeNode.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (Integer.parseInt(node.toString().substring(node.toString().lastIndexOf(":") + 2 )) == id) {
                return new TreePath(node.getPath());
            }
        }
        return null;
    }

    public static JFrame getFrame(){
        return window;
    }

    @Override
    public void setup(){
        ViewModelComponentRegistry.initialize();
        GGDebugRenderer.setEnabled(true);

        refreshComponentList();
        updateAddRegion();

        WorldEngine.shouldUpdate(false);
        RenderEngine.setProjectionData(ProjectionData.getPerspective(100, 0.2f, 3000f));

        var cube = ObjectCreator.createCube(5);
        RenderGroup group = new RenderGroup("renderer");
        group.add(cube);

        var gray = Texture.ofColor(Color.GRAY);
        var green = Texture.ofColor(Color.GREEN);
        var blue = Texture.ofColor(Color.BLUE);

        RenderEngine.addRenderPath(new RenderOperation("editorrender", () -> {
            for(Component c : WorldEngine.getCurrent().getAllDescendants()){
                if(currentComponent == null) continue;
                if(c instanceof Renderable) continue;
                cube.setMatrix(new Matrix4f().translate(c.getPosition()).rotate(c.getRotation()).scale(new Vector3f(0.1f)));
                if(c == currentComponent){
                    green.use(0);
                }else if(currentComponent.getAllDescendants().contains(c)){
                    blue.use(0);
                }else{
                    gray.use(0);
                }
                group.render();
            }
        }));

        Executor.every(Duration.ofMinutes(5), () -> {
            GGConsole.log("Autosaving world to autosave.bwf...");
            WorldLoader.saveWorld(WorldEngine.getCurrent(), "autosave.bwf");
            GGConsole.log("Autosave completed!");
        });

        Executor.every(Duration.ofMillis(100), () -> {
            if (currentview != null && currentview.isComplete()) {
                currentview.update();
            }
        });

        if(!runtimeJar.isEmpty()){
            try {
                var classes = JarClassUtil.loadAllClassesFromJar(runtimeJar);

                var runnableClass = classes.stream()
                        .map(Objects::requireNonNull)
                        .filter(GGApplication.class::isAssignableFrom)
                        .map(s -> ((Class<GGApplication>)s) ).findFirst().get();

                GGConsole.log("Found runnable class in jarfile: " + runnableClass.getName());

                GGConsole.log("Instantiating class...");

                underlyingApp = runnableClass.getDeclaredConstructor().newInstance();

                var method = runnableClass.getDeclaredMethod("setup");

                GGConsole.log("Initializing game file...");

                method.invoke(underlyingApp);

                ViewModelComponentRegistry.register(classes);
                ViewModelComponentRegistry.createRegisters();
                updateAddRegion();

                GGConsole.log("Succesfully initialized instance of " + underlyingApp.applicationName);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            ViewModelComponentRegistry.createRegisters();
            updateAddRegion();
        }

        createWorld();

        GUIController.setEnabled(false);

        BindController.clearControllers();
        BindController.clearBinds();
        BindController.addBind(ControlType.KEYBOARD, "forward", KEY_W);
        BindController.addBind(ControlType.KEYBOARD, "backward", KEY_S);
        BindController.addBind(ControlType.KEYBOARD, "left", KEY_A);
        BindController.addBind(ControlType.KEYBOARD, "right", KEY_D);
        BindController.addBind(ControlType.KEYBOARD, "up", KEY_SPACE);
        BindController.addBind(ControlType.KEYBOARD, "down", KEY_LEFT_SHIFT);
        BindController.addBind(ControlType.KEYBOARD, "lookright", KEY_Q);
        BindController.addBind(ControlType.KEYBOARD, "lookleft", KEY_E);
        BindController.addBind(ControlType.KEYBOARD, "lookup", KEY_R);
        BindController.addBind(ControlType.KEYBOARD, "lookdown", KEY_F);
        BindController.addBind(ControlType.KEYBOARD, "clear", KEY_G);

        cam = new Camera();
        RenderEngine.useView(cam);

        transmitter = new EditorTransmitter();
        transmitter.editor = this;
        BindController.addController(transmitter);

        WorldEngine.getCurrent().getRenderEnvironment().setSkybox(new Skybox(Texture.getSRGBCubemap(Resource.getTexturePath("skybox\\majestic_ft.png"),
                Resource.getTexturePath("skybox\\majestic_bk.png"),
                Resource.getTexturePath("skybox\\majestic_up.png"),
                Resource.getTexturePath("skybox\\majestic_dn.png"),
                Resource.getTexturePath("skybox\\majestic_rt.png"),
                Resource.getTexturePath("skybox\\majestic_lf.png")), 1500f));

        window.setVisible(true);
    }

    @Override
    public void render(){

    }

    @Override
    public void update(float delta){
        if (refresh) {
            refreshComponentList();
            refresh = false;
        }
        if(((GGCanvas)WindowController.getWindow()).hasFocus()){
            Vector2f mousepos = MouseController.get();
            float mult = 0.5f;
            Vector3f currot = new Vector3f(mousepos.multiply(mult).y-180, mousepos.multiply(mult).x, 0);
            cam.setRotation(new Quaternionf(new Vector3f(currot.x, currot.y, currot.z)).invert());

            Vector3f nvector = new Vector3f(control).inverse().multiply(delta * 15);

            nvector = new Quaternionf(new Vector3f(currot.x, currot.y, currot.z)).transform(nvector);
            cam.setPosition(cam.getPosition().add(nvector));
        }
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
