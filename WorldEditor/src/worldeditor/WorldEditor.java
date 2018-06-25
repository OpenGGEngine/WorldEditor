/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.GGInfo;
import com.opengg.core.console.GGConsole;
import com.opengg.core.engine.*;
import com.opengg.core.extension.ExtensionManager;
import com.opengg.core.io.ControlType;
import com.opengg.core.math.Matrix4f;
import com.opengg.core.math.Quaternionf;
import com.opengg.core.math.Vector3f;
import com.opengg.core.math.Vector3fm;
import com.opengg.core.render.*;
import com.opengg.core.render.objects.ObjectCreator;
import com.opengg.core.render.texture.Texture;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.core.world.*;
import com.opengg.core.world.Action;
import com.opengg.core.world.components.Component;
import com.opengg.core.world.components.viewmodel.Initializer;
import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegistry;

import com.opengg.ext.awt.AWTExtension;
import worldeditor.assetloader.AssetDialog;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Objects;

import static com.opengg.core.io.input.keyboard.Key.*;


public class WorldEditor extends GGApplication implements Actionable{

    private static final Object initlock = new Object();
    private static JFrame window;
    private static JPanel mainpanel;
    private static JTree tree;
    private static GGView currentview;
    private static JPanel editarea;
    private static JPanel addregion;
    private static JPanel canvasregion;
    private static boolean refresh;
    private static JPanel treearea;
    private static Vector3fm control = new Vector3fm();
    private static Vector3fm controlrot = new Vector3fm();
    private static Vector3fm currot = new Vector3fm();
    private static float rotspeed = 30;
    private static Camera cam;
    private static EditorTransmitter transmitter;
    private static JTextArea consoletext;
    private static DefaultTreeModel treeModel;
    private static DefaultMutableTreeNode upperTreeNode;
    int i = 0;

    public static void main(String[] args){

        Thread ui = new Thread(() -> {
            initSwing();
        });
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
        w.width = 640;
        w.height = 480;
        w.resizable = false;
        w.type = "AWT";
        w.vsync = true;
        OpenGG.initialize(new WorldEditor(), w);
    }

    public static void initSwing(){
        int minClientWidth = 1920;
        int minClientHeight = 1080;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }



        window = new JFrame();
        window.setMinimumSize(new Dimension(1920,1080));
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

        var worldMenu = new JMenu();
        worldMenu.setText("World");

        var assetLoader = new JMenuItem();
        assetLoader.setText("Asset Loader");
        assetLoader.addActionListener((e) -> new AssetDialog(getFrame()));


        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(worldMenu);
        menuBar.add(assetLoader);


        var gamepath = new JMenuItem();
        gamepath.setText("Set root game path");
        gamepath.addActionListener((e) -> {
            createGamePathChooser();
        });

        var jarload = new JMenuItem();
        jarload.setText("Load game JAR");
        jarload.addActionListener((e) -> {
            createJarLoadChooser();
        });

        var loadmap = new JMenuItem();
        loadmap.setText("Load world");
        loadmap.addActionListener((e) -> {
            createWorldLoadChooser();
        });

        var savemap = new JMenuItem();
        savemap.setText("Save world");
        savemap.addActionListener((e) -> {
            createWorldSaveChooser();
        });

        fileMenu.add(gamepath);
        fileMenu.add(jarload);
        fileMenu.add(loadmap);
        fileMenu.add(savemap);

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

        addregion = new JPanel();
        addregion.setLayout(new BoxLayout(addregion, BoxLayout.PAGE_AXIS));
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
        console.setBorder(raisedetched);
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

        console.setViewportView(consoletext);

        PrintStream oldout = System.out;
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //if (consoletext.isEnabled()) return;
                consoletext.append(String.valueOf((char) b));
                oldout.write(b);
            }
        };

         System.setOut(new PrintStream(out));

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

    private static void createWorldSaveChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.showSaveDialog(null);
        dialog.setFileFilter(new FileNameExtensionFilter("OpenGG world savefile", "bwf"));

        var resultfile = dialog.getSelectedFile();
        if (resultfile == null) return;
        var result = resultfile.getAbsolutePath();

        OpenGG.asyncExec(() -> WorldEngine.saveWorld(WorldEngine.getCurrent(), result));
        refreshComponentList();
    }

    private static void createGamePathChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dialog.showDialog(null, "Set game path...");

        var resultfile = dialog.getSelectedFile();
        if (resultfile == null) return;
        var result = resultfile.getAbsolutePath();

        Resource.setDefaultPath(result);
        mainpanel.setName("World Editor: " + result);
        GGConsole.log("Switched game path to " + result);
    }

    private static void createJarLoadChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.setFileFilter(new FileNameExtensionFilter("OpenGG application JAR","jar"));
        dialog.showDialog(null, "Load game JAR...");

        var resultfile = dialog.getSelectedFile();
        if (resultfile == null) return;
        var result = resultfile.getAbsolutePath();

        ViewModelComponentRegistry.clearRegistry();
        ViewModelComponentRegistry.registerAllFromJar(result);
        ViewModelComponentRegistry.createRegisters();
        updateAddRegion();
    }

    private static void createWorldLoadChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.setFileFilter(new FileNameExtensionFilter("OpenGG world savefiles","bwf"));
        dialog.showOpenDialog(null);

        var resultfile = dialog.getSelectedFile();
        if (resultfile == null) return;
        var result = resultfile.getAbsolutePath();

        OpenGG.syncExec(() -> {
            WorldEngine.useWorld(WorldEngine.loadWorld(result));
            RenderEngine.useCamera(cam);
            BindController.addController(transmitter);
        });

        refreshComponentList();
    }

    public static void useViewModel(ViewModel cvm){
        editarea.removeAll();
        GGView view = new GGView(cvm);
        editarea.add(view);
        currentview = view;
    }

    public static void updateAddRegion(){
        addregion.removeAll();

        var strings = ViewModelComponentRegistry.getAllRegistries().stream()
                .map(reg -> reg.getComponent().getSimpleName())
                .sorted()
                .toArray(String[]::new);

        JList<String> classes = new JList<>(strings);
        classes.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        classes.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value, int index, boolean isSelected,
                                                                   boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                listCellRendererComponent.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,Color.BLACK));
                listCellRendererComponent.setHorizontalAlignment(CENTER);
                listCellRendererComponent.setMinimumSize(new Dimension(10,8));
                return listCellRendererComponent;
            }
        });
        classes.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        classes.setVisibleRowCount(-1);

        JScrollPane listScroller = new JScrollPane(classes);
        listScroller.setPreferredSize(new Dimension(100, 300));

        addregion.add(listScroller);

        JButton creator = new JButton("Create Component");
        addregion.add(creator);

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

    public static void markForRefresh(){
        refresh = true;
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

    public static void createComponent(Initializer vmi, ViewModel cvm){
        OpenGG.asyncExec(() -> {
            try {
                var ncomp = cvm.getFromInitializer(vmi);
                WorldEngine.getCurrent().attach(ncomp);
                WorldEngine.getCurrent().rescanRenderables();
                refreshComponentList();
                tree.setSelectionPath(findById(ncomp.getId()));
            }catch(Exception e){
                GGConsole.error("Failed to initialize component: " + e.getMessage());
                e.printStackTrace();
            }
        });

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
                ViewModel cvm = (ViewModel) vmclass.newInstance();
                cvm.setComponent(component);
                cvm.updateLocal();
                useViewModel(cvm);
            }catch(InstantiationException | IllegalAccessException ex){
                GGConsole.error("Failed to create instance of a ComponentViewModel for " + component.getName() + ", is there a default constructor?");
            }
        });

        window.setVisible(true);
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

    public static void setView(GGView view){
        currentview = view;
    }

    public static JFrame getFrame(){
        return window;
    }

    @Override
    public void setup(){
        ViewModelComponentRegistry.initialize();
        ViewModelComponentRegistry.createRegisters();
        WorldEngine.getCurrent().setEnabled(false);

        refreshComponentList();
        updateAddRegion();

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

        var cube = ObjectCreator.createCube(5);
        RenderGroup group = new RenderGroup("renderer");
        group.add(cube);


        RenderEngine.addRenderPath(new RenderPath("editorrender", () -> {
            for(Component c : WorldEngine.getCurrent().getAll()){
                if(c instanceof Renderable) continue;
                cube.setMatrix(new Matrix4f().translate(c.getPosition()).rotate(c.getRotation()).scale(new Vector3f(0.4f)));
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
        i++;
        if (i == 15) {
            i = 0;
            if (currentview != null && currentview.isComplete()) {
                currentview.update();
            }
        }

        if (refresh) {
            refreshComponentList();
            refresh = false;
        }

        currot.x += controlrot.x * rotspeed * delta;
        currot.y += controlrot.y * rotspeed * delta;
        currot.z += controlrot.z * rotspeed * delta;
        cam.setRot(new Quaternionf(new Vector3f(0, currot.y, currot.z)).multiply(new Quaternionf(new Vector3f(currot.x, 0, 0))));

        Vector3f nvector = cam.getRot().invert().transform(new Vector3f(control).multiply(delta * 5));

        cam.setPos(cam.getPos().add(nvector.multiply(10)));
        //window.asyncExec(() -> {
            consoletext.setText(cam.getPos().add(nvector.multiply(10)).toString());
        //});
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
