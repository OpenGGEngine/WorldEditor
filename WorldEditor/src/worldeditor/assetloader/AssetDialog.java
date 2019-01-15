/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader;

import com.opengg.core.GGInfo;
import com.opengg.core.console.GGConsole;
import com.opengg.core.math.Tuple;
import com.opengg.core.model.Model;
import com.opengg.core.model.io.AssimpModelLoader;
import com.opengg.core.model.io.BMFFile;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Warren
 */
public class AssetDialog extends JDialog {

    public boolean isprocessing = false;

    public ModelOptions defaultop = new ModelOptions(true,false,false,false);
    public ModelOptions currentop = defaultop;

    public ArrayList<ModelOptions> models = new ArrayList<>();
    class ModelTask extends SwingWorker<Integer,Tuple<Integer,Integer>>{
        ModelProcess p;
        Model m;
        JProgressBar bar;
        ModelTask(ModelProcess p, Model m, JProgressBar bar){
            this.p = p;
            this.p.run = ()->publish(new Tuple<>(p.numcompleted,p.totaltasks));
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
            for(Tuple<Integer,Integer> chunk: chunks){
                bar.setValue(chunk.x);
                bar.setMaximum(chunk.y);
                bar.setString("Sub-Process: " + chunk.x + "/" + chunk.y);
            }
        }
    }
    public AssetDialog(JFrame parent) {
        super(parent, "Convert Model");

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        this.setSize(600, 450);

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
        left.setLayout(new BoxLayout(left,BoxLayout.PAGE_AXIS));

        JPanel right = new JPanel();
        right.setBorder(BorderFactory.createEtchedBorder());
        right.setLayout(new BoxLayout(right,BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("<html><h2>Asset Loader</h2></html>");

        JPanel assimpconf = new JPanel();
        assimpconf.setLayout(new BoxLayout(assimpconf,BoxLayout.PAGE_AXIS));
        assimpconf.add(new JLabel("<html><h3>Assimp Config</h3></html>"));
        assimpconf.add(new JCheckBox("Optimize Meshes"));
        assimpconf.add(new JCheckBox("Optimize Graph"));

        JPanel exportconf = new JPanel();
        exportconf.setLayout(new BoxLayout(exportconf,BoxLayout.PAGE_AXIS));
        exportconf.add(new JLabel("<html><h3>Processing Config</h3></html>"));
        JCheckBox[] boxes = new JCheckBox[]{
                new JCheckBox("Generate Convex Hull"),
                new JCheckBox("Tootle"),
                new JCheckBox("Generate LOD"),
                new JCheckBox("Export to BMF")
        };
        for(JCheckBox box:boxes) exportconf.add(box);

        updateExportConf(currentop,boxes);

        JButton button = new JGradientButton("Load Model");

        JProgressBar bar = new JProgressBar(0,100);
        bar.setStringPainted(true);
        bar.setString("Sub-Process Total:");

        JProgressBar totalbar = new JProgressBar(0,100);
        totalbar.setString("Total Progress:");
        totalbar.setStringPainted(true);

        JButton defaults = new JGradientButton("Default Config");
        JButton assimpconfig = new JGradientButton("Assimp Import Config");
        JButton process = new JGradientButton("Process");

        JLabel processCount = new JLabel("On: 0/0");

        DefaultListModel<String> list = new  DefaultListModel();
        JList wrapperlist = new JList<>(list);
        wrapperlist.setBackground(Theme.scrollBG);
        wrapperlist.setVisibleRowCount(6);
        JScrollPane scroll = new JScrollPane(wrapperlist);
        wrapperlist.setCellRenderer(new FileRenderer(true));

        wrapperlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wrapperlist.addListSelectionListener(e->{
            updateOptions(currentop,boxes);
            currentop = models.get(e.getFirstIndex());
            assimpconf.setVisible(false);
            exportconf.setVisible(true);
            updateExportConf(currentop,boxes);
        });
        top.add(label);
        left.add(defaults);
        left.add(Box.createVerticalStrut(5));
        left.add(assimpconfig);
        left.add(Box.createVerticalStrut(5));
        left.add(button);
        left.add(Box.createVerticalStrut(5));
        left.add(scroll);

        main.add(top,BorderLayout.PAGE_START);
        main.add(left,BorderLayout.LINE_START);
        main.add(right,BorderLayout.LINE_END);
        main.add(center,BorderLayout.CENTER);
        center.add(assimpconf);
        exportconf.setVisible(false);
        assimpconf.setVisible(false);
        center.add(exportconf);


        right.add(process);
        right.add(Box.createVerticalStrut(10));
        right.add(processCount);
        right.add(Box.createVerticalStrut(10));
        right.add(totalbar);
        right.add(Box.createVerticalStrut(10));
        right.add(bar);


        main.add(bottom, BorderLayout.PAGE_END);

        defaults.addActionListener(e->{
            updateOptions(currentop,boxes);
            wrapperlist.clearSelection();
            exportconf.setVisible(true);
            assimpconf.setVisible(false);
            currentop = defaultop;
            updateExportConf(currentop,boxes);
        });
        assimpconfig.addActionListener(e->{
            updateOptions(currentop,boxes);
            wrapperlist.clearSelection();
            exportconf.setVisible(false);
            assimpconf.setVisible(true);
        });

        button.addActionListener(e -> {
            updateOptions(currentop,boxes);
            try {
                var dialog = new JFileChooser(GGInfo.getApplicationPath());
                dialog.setFileFilter(new FileNameExtensionFilter("obj; 3ds; dae; fbx; stl; lwo; blend","obj","3ds","dae","fbx","stl"));
                dialog.showDialog(null, "Load model...");

                var resultfile = dialog.getSelectedFile();
                if (resultfile == null) return;
                var path = resultfile.getAbsolutePath();

                Model model = AssimpModelLoader.loadModel(path);
                ModelOptions mo = new ModelOptions(defaultop);
                mo.name = new File(path).getName();
                mo.model = model;
                models.add(mo);
                list.addElement(mo.name);
            } catch (Exception ex) {
                GGConsole.error("Error during model loading! " + ex.toString());
                ex.printStackTrace();
            }
        });
        button.setFont(Font.getFont("Verdana"));
        button.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));
        process.addActionListener(e->{
            process.setEnabled(false);
            updateOptions(currentop,boxes);
            isprocessing = true;
            for(JCheckBox box:boxes) box.setEnabled(false);
            int nummodels = 1;
            for(ModelOptions option:models){
                int numprocesses = bool(option.tootle)
                        + bool(option.convexhull) +
                        bool(option.lod) + bool(option.bmf);
                totalbar.setMaximum(numprocesses);
                int pdone = 0;
                if(option.tootle){
                    new ModelTask(new GGTootle(),option.model,bar).run();
                    pdone++;
                    totalbar.setValue(pdone);
                    totalbar.setString(pdone+"/"+numprocesses);
                }
                if(option.bmf){
                    Path p = Paths.get(option.name);
                    String file = p.getFileName().toString();

                    option.model.fileLocation = file;
                    new ModelTask(new BMFFile(),option.model,bar).run();
                    pdone++;
                    totalbar.setValue(pdone);
                    totalbar.setString(pdone+"/"+numprocesses);
                }
                processCount.setText("On "+ nummodels+"/"+models.size() + ":"+option.model.fileLocation);
                nummodels++;
            }
            processCount.setText("Finished");
            for(JCheckBox box:boxes){
                box.setEnabled(true);
            }
            isprocessing = false;
            process.setEnabled(true);
        });



        this.setVisible(true);
    }

    public void updateOptions(ModelOptions m,JCheckBox[] boxes){
        m.convexhull = boxes[0].isSelected();
        m.tootle = boxes[1].isSelected();
        m.lod = boxes[2].isSelected();
        m.bmf = boxes[3].isSelected();
    }
    public void updateExportConf(ModelOptions m,JCheckBox[] boxes){
        boxes[0].setSelected(m.convexhull);
        boxes[1].setSelected(m.tootle);
        boxes[2].setSelected(m.lod);
        boxes[3].setSelected(m.bmf);
    }
    public int bool(boolean b){
        return b?1:0;
    }

    class FileRenderer extends DefaultListCellRenderer {

        private boolean pad;
        private Border padBorder = new EmptyBorder(2,2,2,2);

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
                    list,value,index,isSelected,cellHasFocus);
            JLabel l = (JLabel)c;
            l.setText((String)value);
            l.setIcon(UIManager.getIcon("FileView.fileIcon"));
            if (pad) {
                l.setBorder(padBorder);
            }

            return l;
        }
    }
}
