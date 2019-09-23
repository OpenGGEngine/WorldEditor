/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.resources;


import com.opengg.core.console.GGConsole;
import com.opengg.core.engine.Resource;
import com.opengg.core.model.Model;
import com.opengg.core.model.ModelManager;
import com.opengg.core.util.GGFuture;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Javier
 */
public class ModelSelectionDialog extends JDialog {
    GGFuture<Model> future = new GGFuture<>();
    
    public static GGFuture<Model> getModel(Window window){
        ModelSelectionDialog shell = new ModelSelectionDialog(window);

        return shell.future;
    }
    
    public ModelSelectionDialog(Window window){
        super(window, "Select Model");

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        this.setLocation(100,100);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(200, 200));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        this.getContentPane().add(content);

        JScrollPane pane = new JScrollPane();
        pane.setWheelScrollingEnabled(true);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setLayout(new ScrollPaneLayout());
        pane.setPreferredSize(new Dimension(400,200));
        content.add(pane);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setMinimumSize(new Dimension(400,400));
        pane.setViewportView(panel);
        
        for(var model : ModelManager.getModelList().values()){
            JButton button = new JButton();
            button.setText(model.getName());
            button.setMinimumSize(new Dimension(200,20));
            button.setPreferredSize(new Dimension(200,20));
            panel.add(button);

            button.addActionListener(a -> {
                String name = button.getText();
                future.set(Resource.getModel(name));

                this.dispose();
            });
        }

        JPanel input = new JPanel();
        input.setLayout(new FlowLayout());
        content.add(input);

        JTextField newtex = new JTextField();
        newtex.setToolTipText("Relative or local model filename");
        newtex.setPreferredSize(new Dimension(300, 20));
        input.add(newtex);

        newtex.addActionListener((e) -> {
            try{
                future.set(Resource.getModel(newtex.getText()));
                this.dispose();
            }catch(Exception ex){
                GGConsole.warning("Failed to load model at " + newtex.getText());
            }
        });

        JButton enter = new JButton();
        enter.setText("Load Model");
        input.add(enter);
        enter.addActionListener(e -> {
            try{
                future.set(Resource.getModel(newtex.getText()));
                this.dispose();
            }catch(Exception ex){
                GGConsole.warning("Failed to load model at " + newtex.getText());
            }
        });

        this.pack();
        this.setVisible(true);
    }
}
