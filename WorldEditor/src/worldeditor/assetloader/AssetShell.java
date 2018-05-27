/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader;

import assetloader2.ModelLoader12;
import com.opengg.core.console.GGConsole;
import com.opengg.core.model.Model;
import com.opengg.core.model.ModelManager;
import com.opengg.core.util.GGOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import worldeditor.assetloader.loader.AnimMeshesLoader;
import worldeditor.assetloader.loader.StaticMeshesLoader;

/**
 *
 * @author Warren
 */
public class AssetShell {

    public int currentversion = 1;

    public static AssetShell shell;
    
    public Shell nshell;
    public Label label;
    public ProgressBar bar;
    public Button animated;

    public static void loadModel(Shell parent){
        shell = new AssetShell(parent);
        
        while(!shell.nshell.isDisposed()){
            if(!shell.nshell.getDisplay().readAndDispatch())
                shell.nshell.getDisplay().sleep();
        }
    }
    
    public AssetShell(Shell parent) {
        nshell = new Shell(parent);
        nshell.setLayout(new FillLayout(SWT.VERTICAL));
        nshell.setText("Model Converter");
        nshell.setMinimumSize(40, 60);
        
        nshell.addDisposeListener((DisposeEvent event) -> {
            parent.setEnabled(true);
        });
        
        bar = new ProgressBar(nshell, SWT.SMOOTH);
        bar.setBounds(10, 10, 200, 32);
        
        label = new Label(nshell, SWT.NULL);
        label.setAlignment(SWT.RIGHT);
        label.setBounds(10, 10, 80, 20);
        
        animated = new Button(nshell, SWT.CHECK);
        animated.setText("Load Animations");
     
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        
        final Button button = new Button(nshell, SWT.PUSH);
        button.setText("Load Model");
        button.setVisible(true);

        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    FileDialog fd = new FileDialog(nshell, SWT.OPEN);
                    fd.setText("Load Model");
                    fd.setFilterPath("C:/");
                    String[] filterExt = {"*.obj; *.3ds; *.dae; *.fbx; *.stl; *.lwo; *.blend"};
                    fd.setFilterExtensions(filterExt);
                    String path = fd.open();
                    System.out.println(path);
                    ModelLoader12.loadModel(new File(path));
                 /*   if(path == null || path.isEmpty())
                        return;
                    
                    GGConsole.log("Parsing model at " + path + "...");
                    
                    Model model;
                    
                    if ((Boolean) animated.getSelection()) {
                        model = AnimMeshesLoader.loadAnimModel(path);
                    } else {
                        model = StaticMeshesLoader.load(path);
                    }

                    ModelManager.addModel(model);

                    GGConsole.log("Model at " + path + " has been parsed successfully, writing to disk...");
                    
                    String endloc = new File(path).getAbsolutePath();
                    endloc = endloc.substring(0, endloc.lastIndexOf("."));
                    try (GGOutputStream out = new GGOutputStream(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(endloc + ".bmf"))))) {
                        model.putData(out);
                        GGConsole.log("Model at " + endloc + " has been written successfully");
                    } catch (Exception ex) {
                        GGConsole.error("Failed to write model!");
                        ex.printStackTrace();
                    }
*/    
                } catch (Exception ex) {
                    GGConsole.error("Error during model loading! " + ex.toString());
                    ex.printStackTrace();
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });
        
        nshell.setLayout(layout);
        nshell.pack();
        nshell.open();
        
        parent.setEnabled(false);
    }
}
