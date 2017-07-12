/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import com.opengg.core.math.Vector4f;
import com.opengg.core.world.components.viewmodel.ViewModelElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import static worldeditor.WorldEditor.addDecimalListener;
import static worldeditor.WorldEditor.addIntegerListener;

/**
 *
 * @author Javier
 */
public class GGElement {
    public ViewModelElement element;
    public GGView view;
    public List<Control> all = new ArrayList<>();
    
    public GGElement(Composite editarea, ViewModelElement element, GGView view){
        this.view = view;
        this.element = element;
        if(element.type == ViewModelElement.VECTOR4F){
            Label label = new Label(editarea, SWT.NULL);
            label.setText(element.name);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);
            
            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v1);
            all.add(v1);
            
            Text v2 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v2);
            all.add(v2);

            Text v3 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v3);
            all.add(v3);

            Text v4 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v4);
            all.add(v4);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        }else if(element.type == ViewModelElement.VECTOR3F){
            Label label = new Label(editarea, SWT.NULL);
            label.setText(element.name);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v1);
            all.add(v1);

            Text v2 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v2);
            all.add(v2);

            Text v3 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v3);
            all.add(v3);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == ViewModelElement.VECTOR2F){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);
            
            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v1);
            all.add(v1);

            Text v2 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v2);
            all.add(v2);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace2 = new Label(editarea, SWT.NULL);
            fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == ViewModelElement.FLOAT){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);
            
            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v1);
            all.add(v1);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace2 = new Label(editarea, SWT.NULL);
            fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace3 = new Label(editarea, SWT.NULL);
            fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == ViewModelElement.INTEGER){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addIntegerListener(v1);
            all.add(v1);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace2 = new Label(editarea, SWT.NULL);
            fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace3 = new Label(editarea, SWT.NULL);
            fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == ViewModelElement.STRING){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
            all.add(v1);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == ViewModelElement.BOOLEAN){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Button button = new Button(editarea, SWT.TOGGLE);
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(button);
            button.addSelectionListener(new SelectionListener(){

                @Override
                public void widgetSelected(SelectionEvent e) {
                    element.value = button.getSelection();
                    if(view != null) view.cvm.fireEvent(element);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {}
            });

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace2 = new Label(editarea, SWT.NULL);
            fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace3 = new Label(editarea, SWT.NULL);
            fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }

        if(element.autoupdate){
            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else{
            Label placeholder = new Label(editarea, SWT.NULL);
            placeholder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }
    }
    
    public void update(){
        ((Label)all.get(0)).setText(element.name);
        if(element.type == ViewModelElement.VECTOR4F){
            Vector4f data = (Vector4f)element.value;
            ((Text)all.get(1)).setText(Float.toString(data.x));
            ((Text)all.get(2)).setText(Float.toString(data.y));
            ((Text)all.get(3)).setText(Float.toString(data.z));
            ((Text)all.get(4)).setText(Float.toString(data.w));
        }else if(element.type == ViewModelElement.VECTOR3F){
            Vector3f data = (Vector3f)element.value;
            ((Text)all.get(1)).setText(Float.toString(data.x));
            ((Text)all.get(2)).setText(Float.toString(data.y));
            ((Text)all.get(3)).setText(Float.toString(data.z));
        }else if(element.type == ViewModelElement.VECTOR2F){
            Vector2f data = (Vector2f)element.value;
            ((Text)all.get(1)).setText(Float.toString(data.x));
            ((Text)all.get(2)).setText(Float.toString(data.y));
        }else if(element.type == ViewModelElement.FLOAT){
            float data = (Float)element.value;
            ((Text)all.get(1)).setText(Float.toString(data));
        }else if(element.type == ViewModelElement.INTEGER){
            int data = (Integer)element.value;
            ((Text)all.get(1)).setText(Integer.toString(data));
        }else if(element.type == ViewModelElement.STRING){
            String data = (String)element.value;
            ((Text)all.get(1)).setText(data);
        }else if(element.type == ViewModelElement.BOOLEAN){
            boolean data = (Boolean)element.value;
            ((Button)all.get(1)).setText(Boolean.toString(data));
            ((Button)all.get(1)).setSelection(data);
        }
    }
    
    public void remove(){
        for(Control c : all){
            c.dispose();
        }
    }
}
