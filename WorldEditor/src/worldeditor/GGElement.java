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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
    
    public GGElement(Composite editarea, ViewModelElement element){
        if(element.type == ViewModelElement.VECTOR4F){
                Vector4f data = (Vector4f)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v1);
                v1.setText(Float.toString(data.x));
                
                Text v2 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v2);
                v2.setText(Float.toString(data.y));
                
                Text v3 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v3);
                v3.setText(Float.toString(data.z));
                
                Text v4 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v4);
                v4.setText(Float.toString(data.w));
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            }else if(element.type == ViewModelElement.VECTOR3F){
                Vector3f data = (Vector3f)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v1);
                v1.setText(Float.toString(data.x));
                
                Text v2 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v2);
                v2.setText(Float.toString(data.y));
                
                Text v3 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v3);
                v3.setText(Float.toString(data.z));
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }else if(element.type == ViewModelElement.VECTOR2F){
                Vector2f data = (Vector2f)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v1);
                v1.setText(Float.toString(data.x));
                
                Text v2 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v2);
                v2.setText(Float.toString(data.y));
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace2 = new Label(editarea, SWT.NULL);
                fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }else if(element.type == ViewModelElement.FLOAT){
                float data = (Float)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addDecimalListener(v1);
                v1.setText(Float.toString(data));
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace2 = new Label(editarea, SWT.NULL);
                fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace3 = new Label(editarea, SWT.NULL);
                fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }else if(element.type == ViewModelElement.INTEGER){
                int data = (Integer)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                addIntegerListener(v1);
                v1.setText(Float.toString(data));
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace2 = new Label(editarea, SWT.NULL);
                fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace3 = new Label(editarea, SWT.NULL);
                fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }else if(element.type == ViewModelElement.STRING){
                String data = (String)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
                v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
                v1.setText(data);
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }else if(element.type == ViewModelElement.BOOLEAN){
                boolean data = (Boolean)element.value;
                
                Label label = new Label(editarea, SWT.NULL);
                label.setText(element.name);
                label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Button button = new Button(editarea, SWT.TOGGLE);
                button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                button.setText(Boolean.toString(data));
                button.setSelection(data);
                
                Label fillspace = new Label(editarea, SWT.NULL);
                fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace2 = new Label(editarea, SWT.NULL);
                fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
                
                Label fillspace3 = new Label(editarea, SWT.NULL);
                fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            }
            
            if(element.autoupdate){
                
            }else{}
    }
}
