/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader;

import com.opengg.core.GGInfo;
import com.opengg.core.console.GGConsole;
import com.opengg.core.engine.Resource;
import com.opengg.core.math.Tuple;
import com.opengg.core.model.Model;
import com.opengg.core.model.ModelManager;
import com.opengg.core.model.io.AssimpModelLoader;
import com.opengg.core.model.io.BMFFile;
import com.opengg.core.model.process.ConvexHullUtil;
import com.opengg.core.model.process.GGTootle;
import com.opengg.core.model.process.ModelProcess;
import worldeditor.JGradientButton;
import worldeditor.Theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Warren
 */
public class AssetDialog extends JDialog {

    public boolean isprocessing = false;

    public ModelOptions defaultop = new ModelOptions(true, false, false, false, true);
    public ModelOptions currentop = defaultop;

    public ArrayList<ModelOptions> models = new ArrayList<>();

    class ModelTask extends SwingWorker<Integer, Tuple<Integer, Integer>> {
        ModelProcess p;
        Model m;
        JProgressBar bar;

        ModelTask(ModelProcess p, Model m, JProgressBar bar) {
            this.p = p;
            this.p.run = () -> {
                publish(new Tuple<>(p.numcompleted, p.totaltasks));
            };
            this.m = m;
            this.bar = bar;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            p.process(m);
            return 1;
        }

        @Override
        protected void process(List<Tuple<Integer, Integer>> chunks) {
            for (Tuple<Integer, Integer> chunk : chunks) {
                bar.setValue(chunk.x);
                bar.setMaximum(chunk.y);
                bar.setString("Sub-Process: " + chunk.x + "/" + chunk.y);
            }
        }
    }

    public AssetDialog(JFrame parent) {
        super(parent, "Convert Model");

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        this.setSize(800, 550);

        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        this.getContentPane().add(main);

        JPanel center = new JPanel();

        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEtchedBorder());

        JPanel top = new JPanel();
        top.setBorder(BorderFactory.createEtchedBorder());

        JPanel left = new JPanel();
        left.setBorder(BorderFactory.createEtchedBorder());
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));

        JPanel right = new JPanel();
        right.setBorder(BorderFactory.createEtchedBorder());
        right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("<html><h2>Asset Loader</h2></html>");

        JPanel assimpconf = new JPanel();
        assimpconf.setLayout(new BoxLayout(assimpconf, BoxLayout.PAGE_AXIS));
        assimpconf.add(new JLabel("<html><h3>Assimp Config</h3></html>"));
        assimpconf.add(new JSeparator());
        assimpconf.add(new JCheckBox("Optimize Meshes"));
        assimpconf.add(new JCheckBox("Optimize Graph"));

        JPanel exportconf = new JPanel();
        exportconf.setLayout(new BoxLayout(exportconf, BoxLayout.PAGE_AXIS));
        JLabel configtitle = new JLabel("<html><h3>Default Processing Config</h3></html>");
        exportconf.add(configtitle);
        exportconf.add(new JSeparator());
        JCheckBox[] boxes = new JCheckBox[]{
                new JCheckBox("Generate Convex Hull"),
                new JCheckBox("Tootle"),
                new JCheckBox("Generate LOD"),
                new JCheckBox("Export to BMF"),
                new JCheckBox("Add to Editor")
        };
        for (JCheckBox box : boxes) exportconf.add(box);

        updateExportConf(currentop, boxes);

        JButton button = new JGradientButton("Load Model");

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setString("Sub-Process Total:");

        JProgressBar totalbar = new JProgressBar(0, 100);
        totalbar.setString("Total Progress:");
        totalbar.setStringPainted(true);

        JButton defaults = new JGradientButton("Default Model");
        JButton assimpconfig = new JGradientButton("Assimp Loader");
        JButton process = new JGradientButton("Process Models");

        JLabel processCount = new JLabel("On: 0/0");

        DefaultListModel<String> list = new DefaultListModel();
        JList wrapperlist = new JList<>(list);
        wrapperlist.setBackground(Theme.scrollBG);
        wrapperlist.setVisibleRowCount(6);
        JScrollPane scroll = new JScrollPane(wrapperlist);
        wrapperlist.setCellRenderer(new FileRenderer(true));

        wrapperlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wrapperlist.addListSelectionListener(e -> {
            updateOptions(currentop, boxes);
            currentop = models.get(e.getFirstIndex());
            assimpconf.setVisible(false);
            exportconf.setVisible(true);
            configtitle.setText("<html><h3>" + currentop.model.getName() + "'s Processing Config</h3></html>");
            updateExportConf(currentop, boxes);
        });
        top.add(label);
        Box temp2 = Box.createHorizontalBox();
        temp2.add(new JLabel("<html><h4>Configs</h4></html>"));
        temp2.add(Box.createRigidArea(new Dimension(20, 0)));
        temp2.add(defaults);
        temp2.add(Box.createRigidArea(new Dimension(6, 0)));
        temp2.add(assimpconfig);
        left.add(temp2);

        Box temp = Box.createHorizontalBox();
        temp.add(new JLabel("<html><h4>Model List</h4></html>"));
        temp.add(button);
        left.add(temp);
        left.add(Box.createVerticalStrut(10));
        left.add(scroll);

        main.add(top, BorderLayout.PAGE_START);
        main.add(left, BorderLayout.LINE_START);
        main.add(right, BorderLayout.LINE_END);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.PAGE_END);

        center.add(assimpconf);
        exportconf.setVisible(true);
        assimpconf.setVisible(false);
        center.add(exportconf);

        right.add(process);
        right.add(Box.createVerticalStrut(1));
        right.add(processCount);
        right.add(Box.createVerticalStrut(10));
        right.add(totalbar);
        right.add(Box.createVerticalStrut(10));
        right.add(bar);

        defaults.addActionListener(e -> {
            updateOptions(currentop, boxes);
            wrapperlist.clearSelection();
            exportconf.setVisible(true);
            assimpconf.setVisible(false);
            currentop = defaultop;
            updateExportConf(currentop, boxes);
            configtitle.setText("<html><h3>Default Processing Config</h3></html>");
        });
        assimpconfig.addActionListener(e -> {
            updateOptions(currentop, boxes);
            wrapperlist.clearSelection();
            exportconf.setVisible(false);
            assimpconf.setVisible(true);
        });

        button.addActionListener(e -> {
            updateOptions(currentop, boxes);
            try {
                var dialog = new JFileChooser(GGInfo.getApplicationPath());
                dialog.setFileFilter(new FileNameExtensionFilter("obj; 3ds; dae; fbx; stl; lwo; blend", "obj", "3ds", "dae", "fbx", "stl"));
                dialog.showDialog(null, "Load model...");

                var resultfile = dialog.getSelectedFile();
                if (resultfile == null) return;
                var path = resultfile.getAbsolutePath();
                Model model = AssimpModelLoader.loadModel(path);
                ModelOptions mo = new ModelOptions(defaultop);
                mo.model = model;
                mo.name = model.getName();
                models.add(mo);
                list.addElement(mo.name);
            } catch (Exception ex) {
                GGConsole.error("Error during model loading! " + ex.toString());
                ex.printStackTrace();
            }
        });
        button.setFont(Font.getFont("Verdana"));
        button.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));
        process.addActionListener(e -> {
            if (models.size() > 0) {
                process.setEnabled(false);
                updateOptions(currentop, boxes);
                isprocessing = true;
                for (JCheckBox box : boxes) box.setEnabled(false);
                int nummodels = 1;
                for (ModelOptions option : models) {
                    int numprocesses = bool(option.tootle)
                            + bool(option.convexhull) +
                            bool(option.lod) + bool(option.bmf);
                    totalbar.setMaximum(numprocesses);
                    int pdone = 0;
                    if (option.tootle) {
                        new ModelTask(new GGTootle(), option.model, bar).run();
                        pdone++;
                        totalbar.setValue(pdone);
                        totalbar.setString(pdone + "/" + numprocesses);
                    }
                    if (option.convexhull) {
                        new ModelTask(new ConvexHullUtil(), option.model, bar).run();
                        pdone++;
                        option.model.setExportConfig(option.model.getExportConfig() | BMFFile.CONVEXHULL);
                        totalbar.setValue(pdone);
                        totalbar.setString(pdone + "/" + numprocesses);
                    }
                    if (option.bmf) {
                        Path p = Paths.get(option.name);
                        if (option.addToEditor) {
                            copyTexToEditorDir(option);
                        }
                        new ModelTask(new BMFFile(), option.model, bar).run();
                        pdone++;
                        totalbar.setValue(pdone);
                        totalbar.setString(pdone + "/" + numprocesses);
                    }
                    processCount.setText("On " + nummodels + "/" + models.size() + ":" + option.model.getFileLocation());
                    nummodels++;
                    if (option.addToEditor) {
                        ModelManager.addModel(option.model);
                        GGConsole.log("Added " + option.model.getName() + " to editor.");
                    }
                }
                processCount.setText("Finished");
                for (JCheckBox box : boxes) {
                    box.setEnabled(true);
                }
                isprocessing = false;
                process.setEnabled(true);
            } else {
                processCount.setText("No models loaded.");
            }
        });


        this.setVisible(true);
    }

    public void updateOptions(ModelOptions m, JCheckBox[] boxes) {
        m.convexhull = boxes[0].isSelected();
        m.tootle = boxes[1].isSelected();
        m.lod = boxes[2].isSelected();
        m.bmf = boxes[3].isSelected();
        m.addToEditor = boxes[4].isSelected();
    }

    public void updateExportConf(ModelOptions m, JCheckBox[] boxes) {
        boxes[0].setSelected(m.convexhull);
        boxes[1].setSelected(m.tootle);
        boxes[2].setSelected(m.lod);
        boxes[3].setSelected(m.bmf);
        boxes[4].setSelected(m.addToEditor);
    }

    public int bool(boolean b) {
        return b ? 1 : 0;
    }

    class FileRenderer extends DefaultListCellRenderer {

        private boolean pad;
        private Border padBorder = new EmptyBorder(2, 2, 2, 2);

        FileRenderer(boolean pad) {
            this.pad = pad;
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            JLabel l = (JLabel) c;
            l.setText((String) value);
            l.setIcon(UIManager.getIcon("FileView.fileIcon"));
            if (pad) {
                l.setBorder(padBorder);
            }

            return l;
        }
    }
    private void copyTexToEditorDir(ModelOptions option){
        File texDirectory = new File(option.model.getFileLocation() + File.separator + "tex");
        String modelDirectory = Resource.getAbsoluteFromLocal("resources/models/" + option.model.getName());
        File newModelDir = new File(modelDirectory);
        if (!newModelDir.exists()) newModelDir.mkdir();

        File newTexDir = new File(modelDirectory + File.separator + "tex");
        if (!newTexDir.exists()) newTexDir.mkdir();

        //Copy tex directory
        if (texDirectory.exists() && texDirectory.listFiles() != null) {
            for (File f : texDirectory.listFiles()) {
                try {
                    Path newFilePath = newTexDir.toPath().resolve(f.getName());
                    Files.copy(f.toPath(), newFilePath,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    GGConsole.error("Failed to copy " + f.getAbsolutePath());
                }
            }
        }
        option.model.setFileLocation(modelDirectory);
    }
}
