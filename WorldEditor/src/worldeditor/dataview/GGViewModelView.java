/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.dataview;

import com.opengg.core.editor.ViewModel;
import worldeditor.Theme;
import worldeditor.WorldEditor;
import worldeditor.components.JGradientButton;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Javier
 */
public class GGViewModelView extends GGView{
    private boolean complete;

    public GGViewModelView(ViewModel<?> cvm){
        super(cvm);
        JButton remove = new JGradientButton("Remove Component");
        remove.setBorder(Theme.extremeBut);
        remove.setBackground(Theme.toggleFalse);
        remove.setIcon(Theme.trash);

        remove.addActionListener(e -> {
            cvm.delete();
            WorldEditor.refreshComponentList();
        });

        remove.setMaximumSize(new Dimension(Integer.MAX_VALUE, remove.getMinimumSize().height));
        this.add(remove);
        complete = true;
    }

    public boolean isComplete(){
        return complete;
    }
}
