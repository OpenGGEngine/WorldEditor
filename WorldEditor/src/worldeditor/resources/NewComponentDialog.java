/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.resources;

import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.editor.Initializer;
import worldeditor.JGradientButton;
import worldeditor.WorldEditor;
import worldeditor.dataview.GGElement;

import java.awt.*;

import javax.swing.*;

/**
 *
 * @author Javier
 */
public class NewComponentDialog extends JDialog{
    public NewComponentDialog(Initializer initializer, ViewModel cvm, Window window){
        super(window, "New Component");

        setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        this.setLayout(new FlowLayout());
        this.setSize(400, 400);

        JPanel total = new JPanel(new GridLayout(0,1));
        this.getContentPane().add(total);

        JScrollPane content = new JScrollPane();
        content.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        initializer.dataBindings.stream()
                .map(GGElement::new)
                .forEach(total::add);

        JGradientButton create = new JGradientButton("Create Component");
        create.addActionListener(e -> {
            WorldEditor.createComponent(initializer, cvm);
            this.dispose();
        });
        create.setBorderPainted(false);
        total.add(create);
        total.revalidate();

        this.pack();
        this.repaint();
        this.revalidate();
        this.setVisible(true);
    }
}
