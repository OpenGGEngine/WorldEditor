/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.engine.Resource;
import com.opengg.core.render.texture.TextureData;
import com.opengg.core.render.texture.TextureManager;
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
public class TextureSelectionShell {
    Shell nshell;
    boolean done;
    TextureInnerClassFix data = new TextureInnerClassFix();    
    
    public static TextureData getData(Shell parent){
        TextureSelectionShell shell = new TextureSelectionShell(parent);
        
        while(!shell.nshell.isDisposed()){
            if(!shell.nshell.getDisplay().readAndDispatch())
                shell.nshell.getDisplay().sleep();
        }
        
        return shell.data.data;
    }
    
    public TextureSelectionShell(Shell parent){
        nshell = new Shell(parent);
        nshell.setLayout(new FillLayout(SWT.VERTICAL));
        nshell.setText("Texture Selection");
        nshell.setMinimumSize(40, 60);
        
        nshell.addDisposeListener((DisposeEvent event) -> {
            parent.setEnabled(true);
        });
        
        Composite composite = new Composite(nshell, SWT.BORDER);
        composite.setLayout(new GridLayout(4, true));
        
        for(TextureData tex : TextureManager.getData().values()){
            Button button = new Button(composite, SWT.PUSH);
            button.setText(tex.source);
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            button.addSelectionListener(new SelectionListener(){

                @Override
                public void widgetSelected(SelectionEvent e) {
                    String id = button.getText();
                    data.data = TextureManager.getTextureData(id);
                    nshell.dispose();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {}
            
            });
        }
        
        Composite input = new Composite(nshell, SWT.BORDER);
        input.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Text newtex = new Text(input, SWT.SINGLE | SWT.BORDER);
        newtex.setMessage("Texture name (Will be searched in resources\\tex\\)");
        
        Button enter = new Button(input, SWT.PUSH);
        enter.setText("Load Texture");
        enter.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                data.data = TextureManager.loadTexture(Resource.getTexturePath(newtex.getText()), true);
                nshell.dispose();
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
