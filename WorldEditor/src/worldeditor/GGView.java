/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.world.WorldEngine;
import com.opengg.core.world.components.viewmodel.ViewModel;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

/**
 *
 * @author Javier
 */
public class GGView extends JPanel{
    private ViewModel cvm;
    private boolean complete;
    private List<GGElement> elements;
    
    public GGView(ViewModel cvm){
        this.cvm = cvm;

        elements = cvm.getElements().stream()
                .map(e -> new GGElement(e, this))
                .peek(this::add)
                .collect(Collectors.toList());
        
        JButton remove = new JButton();
        this.add(remove);

        remove.addActionListener(e -> {
            WorldEngine.markForRemoval(cvm.getComponent());
            WorldEngine.removeMarked();
            WorldEditor.refreshComponentList();
            });

        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        remove.setText("Remove Component");
        this.add(remove, gbc);
        this.doLayout();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        complete = true;
    }
    
    public void update(){
        cvm.updateLocal();
        for(GGElement element : elements){
            element.update();
        }
    }

    public boolean isComplete(){
        return complete;
    }

    public ViewModel getViewModel(){
        return cvm;
    }

}
