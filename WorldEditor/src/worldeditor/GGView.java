/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.engine.WorldEngine;
import com.opengg.core.world.components.viewmodel.ComponentViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * @author Javier
 */
public class GGView {
    public ComponentViewModel cvm;
    boolean complete = false;
    List<GGElement> elements = new ArrayList<>();
    
    public GGView(Composite editarea, ComponentViewModel cvm){
        this.cvm = cvm;
        for(ViewModelElement element : cvm.getElements()){
            elements.add(new GGElement(editarea, element, this));
        }
        
        Button remove = new Button(editarea, SWT.PUSH);
        remove.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                WorldEngine.markForRemoval(cvm.getComponent());
                WorldEditor.markForRefresh();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {}
        });
        remove.setText("Remove Component");
        remove.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        editarea.layout();
        complete = true;
    }
    
    public void update(){
        cvm.updateLocal();
        for(GGElement element : elements){
            element.update();
        }
    }
}
