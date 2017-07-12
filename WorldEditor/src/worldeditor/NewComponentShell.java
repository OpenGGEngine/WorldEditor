/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.world.components.viewmodel.ViewModelInitializer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * @author Javier
 */
public class NewComponentShell {
    List<GGElement> elements = new ArrayList<>();
    
    public NewComponentShell(ViewModelInitializer initializer, Shell shell){
        Shell nshell = new Shell(shell);
        nshell.setSize(400, 600);
        
        
        nshell.open();
    }
}
