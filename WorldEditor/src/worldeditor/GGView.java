/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.world.WorldEngine;
import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.world.components.viewmodel.Element;
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
    private ViewModel cvm;
    private boolean complete;
    private List<GGElement> elements = new ArrayList<>();
    
    public GGView(Composite editarea, ViewModel cvm){
        this.cvm = cvm;
        for(Element element : cvm.getElements()){
            elements.add(new GGElement(editarea, element, this));
        }
        
        Button remove = new Button(editarea, SWT.PUSH);
        remove.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                WorldEngine.markForRemoval(cvm.getComponent());
                WorldEngine.removeMarked();
                editarea.getDisplay().asyncExec(() -> {
                    WorldEditor.refreshComponentList();
                });
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

    public boolean isComplete(){
        return complete;
    }
}
