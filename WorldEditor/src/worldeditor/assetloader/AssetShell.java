/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader;

import com.opengg.core.engine.GGConsole;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import worldeditor.assetloader.loader.AnimMeshesLoader;
import worldeditor.assetloader.loader.AnimModel;
import worldeditor.assetloader.loader.Mesh;
import worldeditor.assetloader.loader.Model;
import worldeditor.assetloader.loader.StaticMeshesLoader;

/**
 *
 * @author Warren
 */
public class AssetShell {

    public int currentversion = 1;

    public static Shell shell = new Shell(Display.getCurrent());

    public static final Label label = new Label(shell, SWT.SMOOTH);
    public static final ProgressBar bar = new ProgressBar(shell, SWT.SMOOTH);
    public static final Button animated = new Button(shell, SWT.CHECK);

    class Open implements SelectionListener {

        public void widgetSelected(SelectionEvent event) {
            try {
                FileDialog fd = new FileDialog(shell, SWT.OPEN);
                fd.setText("Load Model");
                fd.setFilterPath("C:/");
                String[] filterExt = {"*.obj", "*.3ds", "*.dae", "*.*"};
                fd.setFilterExtensions(filterExt);
                String selected = fd.open();
                if (selected != null ) {
                    //Mesh[] scientistman = ModelLoader.load(selected, "");
                    String endloc = new File(selected).getAbsolutePath();

                    endloc.substring(0, endloc.lastIndexOf(File.separator));
                    FileOutputStream ps;
                    ps = new FileOutputStream(endloc + ".bmf");
                    try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(ps))) {
                        if ((Boolean) animated.getSelection()) {
                            System.out.println("Loading Animated Model");
                            AnimModel sd = AnimMeshesLoader.loadAnimModel(selected, "");
                            sd.putData(dos);
                        } else {
                            System.out.println("Loading Static Model");
                            Mesh[] s = StaticMeshesLoader.load(selected, "");
                            Model m = new Model();
                            m.setMeshes(s);
                            m.putData(dos, false);
                        }
                        System.out.println("We Done");

                    } catch (Exception ex) {
                        Logger.getLogger(AssetShell.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ps.close();
                    GGConsole.log("We Done");
                } else {
                    System.out.println("No Model Selected");
                }
            } catch (Exception ex) {
                Logger.getLogger(AssetShell.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        public void widgetDefaultSelected(SelectionEvent event) {
        }
    }

    public AssetShell() {
        bar.setBounds(10, 10, 200, 32);
        label.setText("Ready");
        label.setAlignment(SWT.RIGHT);
        label.setBounds(10, 10, 80, 20);
        animated.setText("Is Animated");
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;

        shell.setLayout(layout);

        final Button button = new Button(shell, SWT.PUSH);
        button.setText("Load Model");
        button.setVisible(true);

        button.addSelectionListener(new Open());
    }

    public void open() {
        shell.open();
    }

    public void close() {
        shell.setVisible(false);
    }
}
