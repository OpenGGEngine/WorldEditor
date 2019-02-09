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
    
    public GGView(ViewModel<com.opengg.core.world.components.Component> cvm){
        this.cvm = cvm;
        this.doLayout();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        elements = cvm.getElements().stream()
                .map(e -> new GGElement(e, this))
                .peek(this::add)
                .collect(Collectors.toList());
        
        JButton remove = new JGradientButton("Remove Component");
        remove.setBorder(Theme.extremeBut);
        remove.setBackground(Theme.toggleFalse);
        remove.setIcon(Theme.trash);

        remove.addActionListener(e -> {
            WorldEngine.markForRemoval(cvm.getComponent());
            WorldEngine.removeMarked();
            WorldEditor.refreshComponentList();
            });

        remove.setMaximumSize(new Dimension(Integer.MAX_VALUE, remove.getMinimumSize().height));
        this.add(remove);

        complete = true;
    }
    
    public void update(){
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
