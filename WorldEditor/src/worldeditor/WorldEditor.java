/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

/**
 *
 * @author Warren
 */
import java.awt.BorderLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.*;

public class WorldEditor {

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
	public static void main(String[] args) {
		try {
			WorldEditor window = new WorldEditor();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1466, 873);
		shell.setText("Hammer 2, The Cancer Continues");
		shell.setLayout(new GridLayout(3, false));
		
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
		
		Composite composite = new Composite(shell, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.heightHint = 775;
		gd_composite.widthHint = 217;
		composite.setLayoutData(gd_composite);
		
		txtWorldNameHere = new Text(composite, SWT.BORDER);
		txtWorldNameHere.setText("World Name Here");
		txtWorldNameHere.setBounds(10, 8, 197, 31);
		
		List list = new List(composite, SWT.BORDER);
		list.setItems(new String[] {"WorldObject1", "WorldObject2", "WorldObject3", "WorldObject4"});
		list.setBounds(10, 45, 197, 689);
		
		Button btnOpenSelected = new Button(composite, SWT.NONE);
		btnOpenSelected.setBounds(10, 740, 197, 35);
		btnOpenSelected.setText("Edit Selected");
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_1.widthHint = 968;
		gd_composite_1.heightHint = 777;
		composite_1.setLayoutData(gd_composite_1);

		
		Composite composite_2 = new Composite(shell, SWT.NONE);
		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_2.heightHint = 780;
		gd_composite_2.widthHint = 248;
		composite_2.setLayoutData(gd_composite_2);
		
		txtWorldobjectnamehere = new Text(composite_2, SWT.BORDER);
		txtWorldobjectnamehere.setText("WorldObjectNameHere");
		txtWorldobjectnamehere.setBounds(10, 10, 228, 31);
		
		txtPos = new Text(composite_2, SWT.BORDER);
		txtPos.setText("Pos:1000,230,2000");
		txtPos.setBounds(10, 49, 228, 31);
		
		txtRot = new Text(composite_2, SWT.BORDER);
		txtRot.setText("Rot: 360,360,360");
		txtRot.setBounds(10, 86, 228, 31);
		
		List list_1 = new List(composite_2, SWT.BORDER);
		list_1.setItems(new String[] {"ModelRenderComponent", "ParticleRenderComponent", "PhysicsComponent"});
		list_1.setBounds(10, 163, 228, 566);
		
		txtComponentList = new Text(composite_2, SWT.BORDER);
		txtComponentList.setText("Component List");
		txtComponentList.setBounds(10, 123, 228, 31);
		
		Button btnEditSelected = new Button(composite_2, SWT.NONE);
		btnEditSelected.setBounds(10, 735, 60, 35);
		btnEditSelected.setText("Edit");
		
		Button btnDeleteSelected = new Button(composite_2, SWT.NONE);
		btnDeleteSelected.setBounds(151, 735, 87, 35);
		btnDeleteSelected.setText("Delete");
		
		Button btnAdd = new Button(composite_2, SWT.NONE);
		btnAdd.setBounds(72, 735, 78, 35);
		btnAdd.setText("Add");

	}
}


