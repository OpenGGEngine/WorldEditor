/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.render.window.GLFWWindow;
import static com.opengg.core.render.window.RenderUtil.endFrame;
import static com.opengg.core.render.window.RenderUtil.startFrame;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *
 * @author Warren
 */
public class Main {
      /**
     * @param args the command line arguments
     */
    	protected Shell shell;
	private Text txtWorldNameHere;
	private Text txtWorldobjectnamehere;
	private Text txtPos;
	private Text txtRot;
	private Text txtComponentList;

	/**
	 * Launch the application.
	 * @param args
	 */
	

	/**
	 * Open the window.
	 */
	public void open(GLFWWindow win) {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
                        startFrame();
                        endFrame();
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(3000, 2000);
		shell.setText("Hammer 2, The Cancer Continues");
		shell.setLayout(new BorderLayout());
    
    Button buttonWest = new Button(shell, SWT.PUSH);
    buttonWest.setText("West");
    buttonWest.setLayoutData(new BorderLayout.BorderData(BorderLayout.WEST));
    
    Button buttonEast = new Button(shell, SWT.PUSH);
    buttonEast.setText("East");
    buttonEast.setLayoutData(new BorderLayout.BorderData(BorderLayout.EAST));  

    Button buttonNorth = new Button(shell, SWT.PUSH);
    buttonNorth.setText("North");
    buttonNorth.setLayoutData(new BorderLayout.BorderData(BorderLayout.NORTH));
    
    Button buttonSouth = new Button(shell, SWT.PUSH);
    buttonSouth.setText("South");
    buttonSouth.setLayoutData(new BorderLayout.BorderData(BorderLayout.SOUTH));    
    
    Text text = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    text.setText("Center");
    text.setLayoutData(new BorderLayout.BorderData(BorderLayout.CENTER));
    
 shell.pack();
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmNew = new MenuItem(menu_1, SWT.NONE);
		mntmNew.setText("New");
		
		MenuItem mntmImportWorld = new MenuItem(menu_1, SWT.NONE);
		mntmImportWorld.setText("Import World");
		
		MenuItem mntmSave = new MenuItem(menu_1, SWT.NONE);
		mntmSave.setText("Save");
		
		MenuItem mntmImportModel = new MenuItem(menu, SWT.NONE);
		mntmImportModel.setText("Import Model");
		   

		
	}
}
