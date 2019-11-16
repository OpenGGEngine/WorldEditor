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
import com.opengg.core.editor.ViewModel;
import com.opengg.core.engine.*;
import com.opengg.core.extension.ExtensionManager;
import com.opengg.core.gui.GUIController;
import com.opengg.core.io.ControlType;
import com.opengg.core.io.input.mouse.MouseController;
import com.opengg.core.math.*;
import com.opengg.core.render.*;
import com.opengg.core.render.objects.ObjectCreator;
import com.opengg.core.render.shader.ShaderController;
import com.opengg.core.render.texture.Texture;
import com.opengg.core.render.window.WindowController;
import com.opengg.core.render.window.WindowInfo;
import com.opengg.core.util.FileUtil;
import com.opengg.core.util.JarClassUtil;
import com.opengg.core.world.Action;
import com.opengg.core.world.*;
import com.opengg.core.world.components.Component;
import com.opengg.core.editor.BindingAggregate;
import com.opengg.core.world.components.viewmodel.ComponentViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelComponentRegistry;
import com.opengg.core.world.structure.CuboidWorldGeometry;
import com.opengg.core.world.structure.ModelWorldGeometry;
import com.opengg.core.world.structure.WorldGeometry;
import com.opengg.core.world.structure.viewmodel.CuboidWorldGeometryViewModel;
import com.opengg.core.world.structure.viewmodel.GeometryViewModel;
import com.opengg.core.world.structure.viewmodel.ModelWorldGeometryViewModel;
import com.opengg.ext.awt.AWTExtension;
import com.opengg.ext.awt.window.GGCanvas;
import worldeditor.assetloader.AssetDialog;
import worldeditor.components.FileTreeModel;
import worldeditor.components.JGradientButton;
import worldeditor.components.RoundedTextField;
import worldeditor.dataview.GGViewModelView;
import worldeditor.resources.NewComponentDialog;
import worldeditor.resources.NewGeometryDialog;
import worldeditor.scripteditor.ScriptEditor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.opengg.core.io.input.keyboard.Key.*;


public class WorldEditor extends GGApplication implements Actionable {
    private static final boolean useCustomTheme = true;
    private static final Object initlock = new Object();
    private static JFrame window;
    private static JPanel mainPanel;
    private static JList<String> geomList;
    private static JTree tree;

    private static GGViewModelView currentView;
    private static JPanel editArea;
    private static JPanel addGeometryRegion;
    private static JPanel listGeometryRegion;
    private static JPanel canvasRegion;
    private static JPanel componentTreeArea;
    private static JTextArea consoleText;
    private static JLabel directoryLabel;

    private static boolean refresh;
    private static Vector3fm control = new Vector3fm();
    private static Camera cam;

    private static ActionTransmitter transmitter;
    private static DefaultTreeModel treeModel;
    private static DefaultMutableTreeNode upperTreeNode;

    private static Component currentComponent;

    private static String runtimeJar = "";
    private static GGApplication underlyingApp;

    private static JButton creator;

    public static void main(String[] args) {
        String initialDirectory = "";
        if (args.length > 0 && !args[0].isEmpty() && new File(args[0]).exists()) {
            initialDirectory = args[0].trim();
        } else {
            var dialog = new JFileChooser(GGInfo.getApplicationPath());
            dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (dialog.showDialog(null, "Load game...") == JFileChooser.APPROVE_OPTION)
                initialDirectory = dialog.getSelectedFile().getAbsolutePath();
        }

        if (!initialDirectory.isEmpty()) {
            Resource.setDefaultPath(initialDirectory);

            runtimeJar = Arrays.stream(Objects.requireNonNull(new File(initialDirectory).listFiles()))
                    .filter(f -> f.getName().contains("lib"))
                    .flatMap(s -> Arrays.stream(s.listFiles()))
                    .map(File::getAbsolutePath)
                    .peek(System.out::println)
                    .filter(s -> s.contains(".jar"))
                    .filter(s -> !s.contains("lwjgl"))
                    .filter(s -> !s.contains("steamworks"))
                    .peek(System.out::println)
                    .filter(s -> JarClassUtil.loadAllClassesFromJar(s).stream()
                            .peek(System.out::println)
                            .map(Objects::requireNonNull)
                            .anyMatch(GGApplication.class::isAssignableFrom))
                    .findAny().orElseThrow(() -> new RuntimeException("Failed to find any runnable OpenGG jarfile"));

        }

        Thread ui = new Thread(WorldEditor::initSwing, "UI Thread");
        ui.start();

        synchronized (initlock) {
            try {
                initlock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ExtensionManager.addExtension(new AWTExtension(canvasRegion));

        WindowInfo w = new WindowInfo();
        w.width = 800;
        w.height = 600;
        w.resizable = false;
        w.type = "AWT";
        w.vsync = true;

        OpenGG.initialize(new WorldEditor(), new InitializationOptions().setWindowInfo(w));
    }

    public static void initSwing() {
        try {
            if (useCustomTheme) {
                Theme.applyTheme();
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        window = new JFrame("World Editor");
        window.setMinimumSize(new Dimension(1920, 1080));
        window.setIconImage(new ImageIcon("resources\\tex\\emak.png").getImage());
        window.setLayout(new BorderLayout());
        setupMenu();

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setMinimumSize(window.getMinimumSize());
        window.add(mainPanel);


        var raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

        var componentPanel = new JPanel(new GridBagLayout());

        componentTreeArea = new JPanel(new GridBagLayout());
        componentTreeArea.setBorder(raisedetched);

        var gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;


        gbc.weightx = gbc.weighty = 1.0;
        componentPanel.add(componentTreeArea, gbc);
        setupTree();

        JTabbedPane elementsPane = new JTabbedPane();

        elementsPane.addTab("Components", componentPanel);
        elementsPane.setMnemonicAt(0, KeyEvent.VK_1);

        var geometryPanel = new JPanel(new GridBagLayout());

        addGeometryRegion = new JPanel();
        addGeometryRegion.setLayout(new GridBagLayout());
        addGeometryRegion.setBorder(raisedetched);

        listGeometryRegion = new JPanel();
        listGeometryRegion.setLayout(new GridBagLayout());
        listGeometryRegion.setBorder(raisedetched);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = gbc.weighty = 1.0;
        geometryPanel.add(listGeometryRegion, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.weightx = gbc.weighty = 1.0;
        geometryPanel.add(addGeometryRegion, gbc);

        elementsPane.addTab("Geometry", geometryPanel);
        elementsPane.setMnemonicAt(1, KeyEvent.VK_2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.25;
        gbc.weighty = 0.70;
        mainPanel.add(elementsPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.weightx = 0.50;
        gbc.weighty = 0.33;

        canvasRegion = new JPanel(new BorderLayout());
        canvasRegion.setBorder(raisedetched);
        canvasRegion.setMinimumSize(new Dimension(800, 600));
        canvasRegion.setMaximumSize(new Dimension(800, 600));
        canvasRegion.setPreferredSize(new Dimension(800, 600));
        mainPanel.add(canvasRegion, gbc);

        editArea = new JPanel(new GridBagLayout());
        editArea.setBorder(raisedetched);


        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.25;
        gbc.weighty = 0.33;
        mainPanel.add(editArea, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;

        gbc.weightx = 1;
        gbc.weighty = 0.33;
        gbc.ipady = 50;

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        setupAssetViewer(tabbedPane);
        setupConsole(tabbedPane);
        mainPanel.add(tabbedPane, gbc);


        mainPanel.doLayout();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                OpenGG.endApplication();
                e.getWindow().dispose();
                System.exit(0);
            }
        });

        window.setVisible(true);

        synchronized (initlock) {
            initlock.notifyAll();
        }
    }

    private static void setupMenu(){

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

        var tools = new JMenu();
        tools.setText("Tools");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(gameMenu);
        menuBar.add(worldMenu);
        menuBar.add(objects);
        menuBar.add(tools);

        var newWorld = new JMenuItem();
        newWorld.setText("Create new world");
        newWorld.addActionListener((e) -> createWorld());

        var loadmap = new JMenuItem();
        loadmap.setText("Load world");
        loadmap.addActionListener((e) -> createWorldLoadChooser());

        var savemap = new JMenuItem();
        savemap.setText("Save world");
        savemap.addActionListener((e) -> createWorldSaveChooser());

        var openGameDirectory = new JMenuItem();
        openGameDirectory.setText("Open game directory");
        openGameDirectory.addActionListener(a -> {try {
            Runtime.getRuntime().exec("explorer.exe /select," + Resource.getAbsoluteFromLocal(""));
        } catch (IOException ex) {
            ex.printStackTrace();
        }});

        var quit = new JMenuItem();
        quit.setText("Quit");
        quit.addActionListener((e) -> System.exit(0));

        var reload = new JMenuItem();
        reload.setText("Restart/Reload jar");
        reload.addActionListener((e) -> restart());

        fileMenu.add(newWorld);
        fileMenu.add(loadmap);
        fileMenu.add(savemap);
        fileMenu.add(openGameDirectory);
        fileMenu.addSeparator();
        fileMenu.add(quit);
        fileMenu.add(reload);

        var worldEditor = new JMenuItem();
        worldEditor.setText("Edit world properties");
        //worldEditor.addActionListener();

        worldMenu.add(worldEditor);

        var assetLoader = new JMenuItem();
        assetLoader.setText("Asset Loader");
        assetLoader.addActionListener((e) -> new AssetDialog(getFrame()));

        var scriptEditor = new JMenuItem();
        scriptEditor.setText("Script Editor");
        scriptEditor.addActionListener((e) -> new ScriptEditor());

        tools.add(assetLoader);
        tools.add(scriptEditor);
    }

    private static void setupAssetViewer(JTabbedPane tabbedPane){

        FileTreeModel fileTreeModel = new FileTreeModel(Resource.getAbsoluteFromLocal("resources"));
        JTree tree = new JTree(fileTreeModel);
        Box verticalBox = Box.createVerticalBox();
        JPanel horizontalBox = new JPanel();
        horizontalBox.setLayout(new BoxLayout(horizontalBox, BoxLayout.LINE_AXIS));
        horizontalBox.setMaximumSize(new Dimension(1920, 50));
        horizontalBox.setBackground(Theme.textArea.brighter());
        directoryLabel = new JLabel(Resource.getAbsoluteFromLocal("resources"));
        JTextField searchBar = new RoundedTextField(1);
        horizontalBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Border outer = searchBar.getBorder();
        Border search = new MatteBorder(0, 16, 0, 0, Theme.searchIcon);
        searchBar.setBorder(new CompoundBorder(outer, search));
        horizontalBox.add(directoryLabel);
        horizontalBox.add(Box.createHorizontalStrut(100));
        horizontalBox.add(searchBar);
        verticalBox.add(horizontalBox);
        JScrollPane left = new JScrollPane(tree);
        DefaultListModel<FileTreeModel.FileToStringFix> model = new DefaultListModel<>();
        JList<FileTreeModel.FileToStringFix> list = new JList<>(model);
        JScrollPane right = new JScrollPane(list);
        verticalBox.add(right);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        list.setCellRenderer(new AssetBrowserListRenderer());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, verticalBox);

        final String[] currDirectory = new String[]{Resource.getAbsoluteFromLocal("resources")};
        updateListAssetView(new File(currDirectory[0]), model, "");

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    FileTreeModel.FileToStringFix file = list.getSelectedValue();
                    String ext = FileUtil.getFileExt(file.getName());
                    if (ext.equals(file.getName())) {
                        currDirectory[0] = file.getAbsolutePath();
                        updateListAssetView(file, model, "");
                    } else {
                        switch (ext) {
                            case "ssf" -> new ScriptEditor(file);
                            case "ogg", "png", "jpg", "gif" -> {
                                try {
                                    Desktop.getDesktop().open(file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                trigger();
            }

            public void removeUpdate(DocumentEvent e) {
                trigger();
            }

            public void insertUpdate(DocumentEvent e) { trigger(); }

            void trigger() {
                updateListAssetView(new File(currDirectory[0]), model, searchBar.getText());
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    FileTreeModel.FileToStringFix lastComp = (FileTreeModel.FileToStringFix) tree.getLastSelectedPathComponent();
                    currDirectory[0] = lastComp.getAbsolutePath();
                    updateListAssetView(lastComp, model, "");
                }
            }
        });
        tabbedPane.addTab("Assets", null, splitPane, null);
    }

    private static void setupConsole(JTabbedPane tabbedPane){


        JScrollPane console = new JScrollPane();
        console.setLayout(new ScrollPaneLayout());
        console.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        console.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        console.setWheelScrollingEnabled(true);
        console.setPreferredSize(new Dimension(1000, 200));
        consoleText = new JTextArea();
        consoleText.setEditable(false);
        consoleText.setFont(new Font("Consolas", Font.PLAIN, 13));

        DefaultCaret caret = (DefaultCaret) consoleText.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        console.setViewportView(consoleText);
        consoleText.setFont(new Font("Consolas",Font.PLAIN,13));

        GGConsole.addOutputConsumer(new DefaultLoggerOutputConsumer(Level.DEBUG, s -> consoleText.append(s + "\n")));
        tabbedPane.addTab("OpenGG console output", null, console);

    }



    private static void updateListAssetView(File file, DefaultListModel model, String filter) {
        model.clear();
        int lastIn = file.getAbsolutePath().lastIndexOf(File.separator + "resource") + 1;
        directoryLabel.setText(file.getAbsolutePath().substring(lastIn).replace(File.separator, " > "));
        Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(fi ->
                fi.getName().contains(filter)).forEach(h -> {
            model.addElement(new FileTreeModel.FileToStringFix(h.getAbsolutePath()));
            AssetBrowserListRenderer.requestImageThumbnail(h);
        });
    }

    private static void createWorld() {
        World world = new World();
        world.setEnabled(false);
        WorldEngine.setOnlyActiveWorld(world);
    }

    private static void createWorldSaveChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());
        dialog.setFileFilter(new FileNameExtensionFilter("World Files", "bwf"));

        if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            var resultfile = dialog.getSelectedFile();
            OpenGG.asyncExec(() -> WorldLoader.saveWorldFile(WorldEngine.getCurrent(), resultfile.getAbsolutePath()));
            refreshComponentList();
        }
    }

    private static void restart() {

    }

    private static void createWorldLoadChooser() {
        var dialog = new JFileChooser(GGInfo.getApplicationPath());

        dialog.setFileFilter(new FileNameExtensionFilter("World Files", "bwf"));

        if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            var resultfile = dialog.getSelectedFile();
            OpenGG.syncExec(() -> {
                WorldEngine.setOnlyActiveWorld(WorldLoader.loadWorld(resultfile.getAbsolutePath()));
                RenderEngine.useView(cam);
                BindController.addTransmitter(transmitter);
            });
            refreshComponentList();
        }
    }

    private static void createComponentCreatorPanel(String name) {
        var info = ViewModelComponentRegistry.getByClassname(name);
        Class clazz = info.getComponent();
        Class vmclazz = ViewModelComponentRegistry.findViewModel(clazz);
        ComponentViewModel viewmodel;

        try {
            viewmodel = (ComponentViewModel) Objects.requireNonNull(vmclazz).getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            GGConsole.error("Failed to create instance of a ViewModel for " + clazz.getName() + ", is there a default constructor?");
            GGConsole.exception(ex);
            return;
        }

        var initializer = viewmodel.getInitializer(new BindingAggregate());
        if (initializer.getDataBindings().isEmpty()) {
            createComponent(initializer, viewmodel);
        } else {
            new NewComponentDialog(initializer, viewmodel, getFrame());
        }
    }

    private static void createGeometryCreatorPanel(String name) {
        var viewModel = switch (name){
            case "CuboidWorldGeometry" -> new CuboidWorldGeometryViewModel();
            case "ModelWorldGeometry" -> new ModelWorldGeometryViewModel();
            default -> throw new InvalidParameterException("Invalid geometry type!");
        };
        var initializer = viewModel.getInitializer(new BindingAggregate());
        if (initializer.getDataBindings().isEmpty()) {
            createGeometry(initializer, viewModel);
        } else {
            new NewGeometryDialog(initializer, viewModel, getFrame());
        }
    }

    public static void createComponent(BindingAggregate vmi, ComponentViewModel cvm){
        OpenGG.asyncExec(() -> {
            try {
                var ncomp = (Component) cvm.getFromInitializer(vmi);
                WorldEngine.getCurrent().attach(ncomp);
                WorldEngine.getCurrent().rescanRenderables();
                refreshComponentList();
                ncomp.setPositionOffset(cam.getPosition());
                tree.setSelectionPath(findByGUID(ncomp.getGUID()));
            } catch (Exception e) {
                GGConsole.error("Failed to initialize component: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static void createGeometry(BindingAggregate vmi, GeometryViewModel cvm){
        OpenGG.asyncExec(() -> {
            var newGeom = (WorldGeometry) cvm.getFromInitializer(vmi);
            WorldEngine.getCurrent().getStructure().addGeometry(newGeom);
            refreshComponentList();
            newGeom.setPosition(cam.getPosition());
            useGeometry(newGeom);
        });
    }

    public static void updateNewComponentList(){

        creator.addActionListener((a) -> System.out.println("Rartarded"));

        var strings = ViewModelComponentRegistry.getAllRegistries().stream()
                .map(reg -> reg.getComponent().getSimpleName())
                .sorted()
                .toArray(String[]::new);

        JList<String> classes = new JList<>(strings);
        classes.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        classes.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        classes.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value, int index, boolean isSelected,
                                                                   boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                listCellRendererComponent.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Theme.buttonBG)
                                , new EmptyBorder(4, 4, 4, 4)));
                listCellRendererComponent.setHorizontalAlignment(CENTER);
                return listCellRendererComponent;
            }
        });


        JScrollPane listScroller = new JScrollPane(classes);
        listScroller.setMinimumSize(new Dimension(320, 300));

        classes.addListSelectionListener(s -> {
            if (!s.getValueIsAdjusting()) {
                creator.setEnabled(classes.getSelectedIndex() != -1);

            }
        });

        JPopupMenu componentSelector = new JPopupMenu();

        for (ActionListener al : creator.getActionListeners()) {
            creator.removeActionListener(al);
        }

        JTextField searchBar = new RoundedTextField(5);
        componentSelector.add(searchBar);

        JButton groupCrumb = new JButton("Components");
        componentSelector.add(groupCrumb);

        DefaultListModel<String> comGroupModel = new DefaultListModel<>();
        JList<String> componentGroupView = new JList<>(comGroupModel);
        componentGroupView.setVisibleRowCount(11);
        Arrays.stream(strings).forEach(comGroupModel::addElement);
        creator.addActionListener((a) -> componentSelector.show(creator, 0, creator.getHeight()));
        componentGroupView.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 1) {
                    createComponentCreatorPanel((String) theList.getSelectedValue());
                }
            }
        });

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                onUpdate();
            }

            public void removeUpdate(DocumentEvent e) {
                onUpdate();
            }

            public void insertUpdate(DocumentEvent e) {
                onUpdate();
            }

            private void onUpdate() {
                comGroupModel.clear();
                Arrays.stream(strings).filter(e -> e.toLowerCase().contains(searchBar.getText().toLowerCase())).forEach(comGroupModel::addElement);
            }
        });

        componentSelector.add(new JScrollPane(componentGroupView));
    }

    public static void updateNewGeometryList(){
        addGeometryRegion.removeAll();

        var strings = new String[]{"CuboidWorldGeometry", "ModelWorldGeometry"};
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
        listScroller.setMinimumSize(new Dimension(350,200));
        addGeometryRegion.add(listScroller,c);

        JGradientButton creator = new JGradientButton("Create Geometry");
        c.gridy = 1;
        addGeometryRegion.add(creator,c);

        addGeometryRegion.validate();

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
            createGeometryCreatorPanel(selection);
        });
    }

    public static void refreshComponentList(){
        setupGeometryList();

        var world = WorldEngine.getCurrent();

        upperTreeNode.removeAllChildren();
        treeModel.reload();
        for (var compo : world.getChildren()) {
            mystery6(compo, upperTreeNode);
        }

        editArea.removeAll();

        var node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        //if(node != null){
        //    useTreeItem((String) node.getUserObject());
        //}
    }

    public static void mystery6(Component comp, DefaultMutableTreeNode parent) {
        var child = addComponentToTree(comp, parent);
        for (var compo : comp.getChildren()) {
            mystery6(compo, child);
        }
    }

    private static DefaultMutableTreeNode addComponentToTree(Component comp, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(new TreeNodeComponentHolder(comp));
        treeModel.insertNodeInto(child, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(child.getPath()));
        return child;
    }

    private static void setupTree() {
        upperTreeNode = new DefaultMutableTreeNode("Components");
        treeModel = new DefaultTreeModel(upperTreeNode);

        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);

        tree.addTreeSelectionListener((e) -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node == null) return;
            useTreeItem((TreeNodeComponentHolder) node.getUserObject());
        });

        JScrollPane treescroller = new JScrollPane(tree);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = 15;
        componentTreeArea.add(treescroller, gbc);

        gbc.gridheight = 1;
        gbc.gridy = 15;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 30, 10, 30);
        creator = new JGradientButton("Create Component");
        componentTreeArea.add(creator, gbc);

        /*TreeItem[] dragitem = new TreeItem[1];

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


        tree.pack();
        */
    }

    public static void useTreeItem(TreeNodeComponentHolder item){
        geomList.clearSelection();

        long id = item.component.getGUID();
        Component component = WorldEngine.getCurrent().findByGUID(id).get();
        Class clazz = component.getClass();
        Class vmclass = ViewModelComponentRegistry.findViewModel(clazz);

        if(vmclass == null){
            editArea.removeAll();
            currentView = null;
            return;
        }

        OpenGG.asyncExec(() -> {
            try{
                ComponentViewModel cvm = (ComponentViewModel) vmclass.getDeclaredConstructor().newInstance();
                cvm.setModel(component);
                useViewModel(cvm);
                window.setVisible(true);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                GGConsole.error("Failed to create instance of a ComponentViewModel for " + component.getName() + ", is there a default constructor?");
            }
        });
    }

    public static void setupGeometryList() {
        listGeometryRegion.removeAll();

        var strings = WorldEngine.getCurrent().getStructure().getAllGeometry()
                .stream()
                .map(g -> g.getClass().getSimpleName() + ": " + g.getGuid())
                .collect(Collectors.toList());
        GridBagConstraints c = new GridBagConstraints();
        geomList = new JList<>(strings.toArray(new String[]{}));
        geomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        geomList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        geomList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value, int index, boolean isSelected,
                                                                   boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                listCellRendererComponent.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,Theme.textArea)
                                ,new EmptyBorder(1,2,2,2)));
                listCellRendererComponent.setHorizontalAlignment(LEFT);
                return listCellRendererComponent;
            }
        });


        JScrollPane listScroller = new JScrollPane(geomList);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        listScroller.setMinimumSize(new Dimension(350,200));
        listGeometryRegion.add(listScroller,c);
        listGeometryRegion.validate();

        geomList.addListSelectionListener(s -> {
            if (!s.getValueIsAdjusting()) {
                if (geomList.getSelectedIndex() != -1) {
                    tree.setSelectionRow(-1);
                    var geom = WorldEngine.getCurrent().getStructure().getByGUID(
                            Long.parseLong(geomList.getSelectedValue().substring(geomList.getSelectedValue().indexOf(":")+1).trim()));
                    useGeometry(geom.get());
                }
            }
        });

    }

    public static void useGeometry(WorldGeometry geom){
        switch (geom.getClass().getSimpleName()){
            case "CuboidWorldGeometry" -> {
                var vm = new CuboidWorldGeometryViewModel();
                vm.setModel((CuboidWorldGeometry) geom);
                useViewModel(vm);
            }
            case "ModelWorldGeometry" -> {
                var vm = new ModelWorldGeometryViewModel();
                vm.setModel((ModelWorldGeometry) geom);
                useViewModel(vm);
            }
        }
    }

    public static void useViewModel(ViewModel cvm) {
        cvm.createMainViewModel();

        editArea.removeAll();
        GGViewModelView view = new GGViewModelView(cvm);
        editArea.add(view);
        editArea.validate();
        if(cvm instanceof ComponentViewModel)
            currentComponent = (Component) cvm.getModel();
        else
            currentComponent = null;
        currentView = view;
    }

    private static TreePath findByGUID(long id) {
        Enumeration<TreeNode> e = WorldEditor.upperTreeNode.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (((TreeNodeComponentHolder) node.getUserObject()).component.getGUID() == id) {
                return new TreePath(node.getPath());
            }
        }
        return null;
    }

    public static JFrame getFrame() {
        return window;
    }

    @Override
    public void setup() {
        ViewModelComponentRegistry.initialize();
        GGDebugRenderer.setEnabled(true);
        OpenGG.getDebugOptions().setLogOnComponentCreation(true);

        refreshComponentList();
        updateNewGeometryList();

        WorldEngine.shouldUpdate(false);
        RenderEngine.setProjectionData(ProjectionData.getPerspective(100, 0.2f, 3000f));

        var cube = ObjectCreator.createCube(5);
        RenderGroup group = new RenderGroup("renderer");
        group.setPipeline("texture");
        group.add(cube);

        var gray = Texture.ofColor(Color.GRAY);
        var green = Texture.ofColor(Color.GREEN);
        var blue = Texture.ofColor(Color.BLUE);

        RenderEngine.addRenderPath(new RenderOperation("editorrender", () -> {
            for(Component c : WorldEngine.getCurrent().getAllDescendants()){
                if(currentComponent == null || c instanceof Renderable) continue;
                ShaderController.setPosRotScale(c.getPosition(),c.getRotation(), new Vector3f(0.1f));
                if(c == currentComponent){
                    green.use(0);
                }else if(currentComponent.getAllDescendants().contains(c)){
                    blue.use(0);
                }else{
                    gray.use(0);
                }
                ShaderController.useConfiguration(group.getPipeline());
                group.render();
            }
        }));

        Executor.every(Duration.ofMinutes(5), () -> {
            GGConsole.log("Autosaving world to autosave.bwf...");
            WorldLoader.saveWorldFile(WorldEngine.getCurrent(), "autosave.bwf");
            GGConsole.log("Autosave completed!");
        });

        Executor.every(Duration.ofMillis(200), () -> {
            if (currentView != null && currentView.isComplete()) {
                currentView.updateUI();
            }
        });

        if (!runtimeJar.isEmpty()) {
            try {
                var classes = JarClassUtil.loadAllClassesFromJar(runtimeJar);
                var runnableClass = classes.stream()
                        .map(Objects::requireNonNull)
                        .filter(GGApplication.class::isAssignableFrom)
                        .map(s -> (Class<GGApplication>) s).findFirst().get();

                GGConsole.log("Found runnable class in jarfile: " + runnableClass.getName());

                GGConsole.log("Instantiating class...");

                underlyingApp = runnableClass.getDeclaredConstructor().newInstance();

                var method = runnableClass.getDeclaredMethod("setup");

                GGConsole.log("Initializing game file...");

                method.invoke(underlyingApp);

                ViewModelComponentRegistry.register(classes);
                ViewModelComponentRegistry.createRegisters();
                updateNewComponentList();

                GGConsole.log("Successfully initialized instance of " + underlyingApp.applicationName);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            ViewModelComponentRegistry.createRegisters();
            updateNewComponentList();
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

        transmitter = this::onAction;
        BindController.addTransmitter(transmitter);

        WorldEngine.getCurrent().getRenderEnvironment().setSkybox(new Skybox(Texture.getSRGBCubemap(
                Resource.getTexturePath("skybox\\majestic_ft.png"),
                Resource.getTexturePath("skybox\\majestic_bk.png"),
                Resource.getTexturePath("skybox\\majestic_up.png"),
                Resource.getTexturePath("skybox\\majestic_dn.png"),
                Resource.getTexturePath("skybox\\majestic_rt.png"),
                Resource.getTexturePath("skybox\\majestic_lf.png")), 1500f));

        window.pack();

        window.setVisible(true);
    }

    @Override
    public void render() {

    }

    @Override
    public void update(float delta) {
        if (refresh) {
            refreshComponentList();
            refresh = false;
        }
        if (((GGCanvas) WindowController.getWindow()).hasFocus()) {
            Vector2f mousepos = MouseController.get();
            float mult = 0.5f;

            Vector3f currentRotation = new Vector3f(-(mousepos.multiply(mult).y-180), -mousepos.multiply(mult).x, 0);
            Vector3f nvector = new Vector3f(control).multiply(delta * 15);

            nvector = Quaternionf.createYXZ(new Vector3f(currentRotation.x, currentRotation.y, currentRotation.z)).transform(nvector);

            cam.setRotation(Quaternionf.createYXZ(currentRotation));
            cam.setPosition(cam.getPosition().add(nvector));
        }
        RenderEngine.useView(cam);
    }

    @Override
    public void onAction(Action action) {
        if (action.type == ActionType.PRESS) {
            switch (action.name) {
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
                case "clear":
                    control = new Vector3fm();
                    break;
            }
        } else {
            switch (action.name) {
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

            }
        }
    }

    static class TreeNodeComponentHolder {
        Component component;

        public TreeNodeComponentHolder(Component c) {
            this.component = c;
        }

        @Override
        public String toString() {
            return component.getName() + " (" + component.getClass().getSimpleName() + ")";
        }
    }
}