/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader;

import assetloader2.ModelLoader12;
import com.opengg.core.GGInfo;
import com.opengg.core.console.GGConsole;

import java.awt.*;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Warren
 */
public class AssetDialog extends JDialog {

    public AssetDialog(JFrame parent) {
        super(parent, "Convert Model");

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        this.setLayout(new FlowLayout());
        this.setSize(40, 60);

        JPanel main = new JPanel();
        main.setLayout(new FlowLayout());
        this.getContentPane().add(main);
        
        JLabel label = new JLabel();
        label.setBounds(10, 10, 80, 20);
        
        //JButton animated = new JButton();
        //animated.setText("Load Animations");
        
        JButton button = new JButton();
        button.setText("Load Model");
        button.setVisible(true);

        main.add(label);
        main.add(button);

        button.addActionListener(e -> {
            try {
                var dialog = new JFileChooser(GGInfo.getApplicationPath());
                dialog.setFileFilter(new FileNameExtensionFilter("obj; 3ds; dae; fbx; stl; lwo; blend"));
                dialog.showDialog(null, "Load model...");

                var resultfile = dialog.getSelectedFile();
                if (resultfile == null) return;
                var path = resultfile.getAbsolutePath();

                System.out.println(path);
                ModelLoader12.loadModel(new File(path));
            } catch (Exception ex) {
                GGConsole.error("Error during model loading! " + ex.toString());
            }
        });
        
        this.setVisible(true);
    }
}
