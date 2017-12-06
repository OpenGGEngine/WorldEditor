/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.engine.OpenGG;
import com.opengg.core.engine.Resource;
import com.opengg.core.model.Model;
import com.opengg.core.model.ModelLoader;
import com.opengg.core.model.ModelManager;
import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 * @author Javier
 */
public class ModelSelectionShell {
    Shell nshell;
    boolean done;
    ModelInnerClassFix data = new ModelInnerClassFix();    
    
    public static Model getModel(Shell parent){
        ModelSelectionShell shell = new ModelSelectionShell(parent);
        
        while(!shell.nshell.isDisposed()){
            if(!shell.nshell.getDisplay().readAndDispatch())
                shell.nshell.getDisplay().sleep();
        }
        
        return shell.data.model;
    }
    
    public ModelSelectionShell(Shell parent){
        nshell = new Shell(parent);
        nshell.setLayout(new FillLayout(SWT.VERTICAL));
        nshell.setText("Model Selection");
        nshell.setMinimumSize(40, 60);
        
        nshell.addDisposeListener((DisposeEvent event) -> {
            parent.setEnabled(true);
        });
        
        Composite composite = new Composite(nshell, SWT.BORDER);
        composite.setLayout(new GridLayout(10, true));
        
        for(Model model : ModelManager.getModelList().values()){
            Button button = new Button(composite, SWT.PUSH);
            button.setText(model.getName());
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            button.addSelectionListener(new SelectionListener(){

                @Override
                public void widgetSelected(SelectionEvent e) {
                    String name = button.getText();
                    data.model = ModelManager.getModel(name);
                    nshell.dispose();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {}
            
            });
        }
        
        Composite input = new Composite(nshell, SWT.BORDER);
        input.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Text newtex = new Text(input, SWT.SINGLE | SWT.BORDER);
        newtex.setMessage("Model name (Will be searched in resources\\models\\)");
        
        Button enter = new Button(input, SWT.PUSH);
        enter.setText("Load Model");
        enter.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                String mname = newtex.getText();
                OpenGG.asyncExec(() -> {
                    if(new File(mname).isAbsolute()){
                        data.model = ModelLoader.loadModel(mname);
                    }else{
                        
                        data.model = Resource.getModel(mname);
                    }
                        
                    nshell.getDisplay().asyncExec(() -> {
                        nshell.dispose();
                    });
                    
                });
                
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        
        });
        
        input.layout();
        composite.layout();
        nshell.pack();
        nshell.open();
        
        parent.setEnabled(false);
    }
}
