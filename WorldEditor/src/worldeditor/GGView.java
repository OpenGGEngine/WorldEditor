/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.world.components.viewmodel.ComponentViewModel;
import com.opengg.core.world.components.viewmodel.ViewModelElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * @author Javier
 */
public class GGView {
    public ComponentViewModel cvm;
    List<GGElement> elements = new ArrayList<>();
    
    public GGView(Composite editarea, ComponentViewModel cvm){
        this.cvm = cvm;
        for(ViewModelElement element : cvm.getElements()){
            elements.add(new GGElement(editarea, element));
        }
    }
}
