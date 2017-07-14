/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.world.components.viewmodel.ViewModel;
import com.opengg.core.world.components.viewmodel.Element;
import com.opengg.core.world.components.viewmodel.Initializer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * @author Javier
 */
public class NewComponentShell {
    List<GGElement> elements = new ArrayList<>();
    Shell nshell;
    
    public NewComponentShell(Initializer initializer, Shell shell, ViewModel cvm){
        nshell = new Shell(shell);
        nshell.setLayout(new FillLayout());
        nshell.setText("Create Component");
        nshell.setMinimumSize(400, 10);

        Composite total = new Composite(nshell, SWT.NONE);
        total.setLayout(new GridLayout(1, false));
        

        Composite content = new Composite(total, SWT.NONE);
        content.setLayout(new GridLayout(6, false));
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

        for(Element element : initializer.elements){
            element.autoupdate = true;
            elements.add(new GGElement(content, element, null));
        }
        
        Button create = new Button(total, SWT.PUSH);
        create.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        create.setText("Create Component");
        create.pack();
        
        create.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                nshell.dispose();
                WorldEditor.createComponent(initializer, cvm);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
            
        });
        
        total.layout();
        content.layout();
        content.pack();
        nshell.layout();
        nshell.pack();
        nshell.open();
    }
}
