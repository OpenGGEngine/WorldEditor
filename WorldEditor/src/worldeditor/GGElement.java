/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.engine.OpenGG;
import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import com.opengg.core.math.Vector4f;
import com.opengg.core.model.Model;
import com.opengg.core.render.texture.TextureData;
import com.opengg.core.world.components.viewmodel.Element;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
    public Element element;
    public GGView view;
    public List<Control> all = new ArrayList<>();
    public Composite editarea;
    
    public GGElement(Composite editarea, Element element, GGView view){
        this.view = view;
        this.element = element;
        this.editarea = editarea;
        if(element.type == Element.VECTOR4F){
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
            
            update(true);
            
            ModifyListener mf = (ModifyEvent e) -> {
                try{
                    Vector4f nvector = new Vector4f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText()), Float.parseFloat(v4.getText()));
                    element.value = nvector;
                    if(view != null && element.autoupdate) fireEvent(element);
                }catch(Exception ex){}
            };
            v1.addModifyListener(mf);
            v2.addModifyListener(mf);
            v3.addModifyListener(mf);
            v4.addModifyListener(mf);

        }else if(element.type == Element.VECTOR3F){
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

            update(true);
            
            ModifyListener mf = (ModifyEvent e) -> {
                try{
                    Vector3f nvector = new Vector3f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText()));
                    element.value = nvector;
                    if(view != null && element.autoupdate) fireEvent(element);
                }catch(Exception ex){}
            };
            v1.addModifyListener(mf);
            v2.addModifyListener(mf);
            v3.addModifyListener(mf);
            
            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == Element.VECTOR2F){
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
            
            update(true);
            
            ModifyListener mf = (ModifyEvent e) -> {
                try{
                    Vector2f nvector = new Vector2f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()));
                    element.value = nvector;
                    if(view != null && element.autoupdate) fireEvent(element);
                }catch(Exception ex){}
            };
            v1.addModifyListener(mf);
            v2.addModifyListener(mf);

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        }else if(element.type == Element.FLOAT){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);
            
            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addDecimalListener(v1);
            all.add(v1);
            
            update(true);
            
            v1.addModifyListener((e) -> {
                try{
                    element.value = Float.parseFloat(v1.getText());
                    if(view != null && element.autoupdate) fireEvent(element);
                }catch(Exception ex){}
            });
            

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        }else if(element.type == Element.INTEGER){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            addIntegerListener(v1);
            all.add(v1);
            
            update(true);
            
            v1.addModifyListener((e) -> {
                try{
                    element.value = Integer.parseInt(v1.getText());
                    if(view != null && element.autoupdate) fireEvent(element);
                }catch(Exception ex){}    
            });
            
            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace2 = new Label(editarea, SWT.NULL);
            fillspace2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

            Label fillspace3 = new Label(editarea, SWT.NULL);
            fillspace3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == Element.STRING){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Text v1 = new Text(editarea, SWT.SINGLE | SWT.BORDER);
            v1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
            all.add(v1);
            
            update(true);
            
            v1.addModifyListener((e) -> {
                element.value = v1.getText();
                if(view != null && element.autoupdate) fireEvent(element);
            });

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == Element.BOOLEAN){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Button button = new Button(editarea, SWT.TOGGLE);
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(button);
            
            update(true);
            
            button.addSelectionListener(new SelectionListener(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    element.value = button.getSelection();
                    if(view != null && element.autoupdate) fireEvent(element);      
                }

                @Override public void widgetDefaultSelected(SelectionEvent e) {}
            });

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        }else if(element.type == Element.TEXTURE){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Button button = new Button(editarea, SWT.PUSH);
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
            button.setText("Choose Texture");
            all.add(button);
            
            update(true);
            
            button.addSelectionListener(new SelectionListener(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    TextureData data = TextureSelectionShell.getData(editarea.getShell());
                    if(data == null) return;
                    element.value = data;
                    if(view != null && element.autoupdate) fireEvent(element);
                    update(true);
                }

                @Override public void widgetDefaultSelected(SelectionEvent e) {}
            });

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else if(element.type == Element.MODEL){
            Label label = new Label(editarea, SWT.NULL);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            all.add(label);

            Button button = new Button(editarea, SWT.PUSH);
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
            button.setText("Choose Model");
            all.add(button);
            
            update(true);
            
            button.addSelectionListener(new SelectionListener(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Model model = ModelSelectionShell.getModel(editarea.getShell());
                    if(model == null) return;
                    element.value = model;
                    if(view != null && element.autoupdate) fireEvent(element);
                    update(true);
                }

                @Override public void widgetDefaultSelected(SelectionEvent e) {}
            });

            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }

        if(element.autoupdate){
            Label fillspace = new Label(editarea, SWT.NULL);
            fillspace.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }else{
            Button button = new Button(editarea, SWT.PUSH);
            button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
            button.setText("Enter");
            button.addSelectionListener(new SelectionListener(){
                @Override
                public void widgetSelected(SelectionEvent event) {
                    if(view != null) fireEvent(element);
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent event) {}
            });
        }
        
        update(true);
    }
    
    public void update(){
        update(false);
    }
    
    public void update(boolean force){
        try{
            ((Label)all.get(0)).setText(element.name);
        }catch(Exception e){
            WorldEditor.clearArea(editarea);
            WorldEditor.setView(null);
            return; 
        }
        if(!element.forceupdate && !force) return;
        
        if(element.type == Element.VECTOR4F){
            Vector4f data = (Vector4f)element.value;
            ((Text)all.get(1)).setText(Float.toString(data.x));
            ((Text)all.get(2)).setText(Float.toString(data.y));
            ((Text)all.get(3)).setText(Float.toString(data.z));
            ((Text)all.get(4)).setText(Float.toString(data.w));
        }else if(element.type == Element.VECTOR3F){
            Vector3f data = (Vector3f)element.value;
            ((Text)all.get(1)).setText(Float.toString(data.x));
            ((Text)all.get(2)).setText(Float.toString(data.y));
            ((Text)all.get(3)).setText(Float.toString(data.z));
        }else if(element.type == Element.VECTOR2F){
            Vector2f data = (Vector2f)element.value;
            ((Text)all.get(1)).setText(Float.toString(data.x));
            ((Text)all.get(2)).setText(Float.toString(data.y));
        }else if(element.type == Element.FLOAT){
            float data = (Float)element.value;
            ((Text)all.get(1)).setText(Float.toString(data));
        }else if(element.type == Element.INTEGER){
            int data = (Integer)element.value;
            ((Text)all.get(1)).setText(Integer.toString(data));
        }else if(element.type == Element.STRING){
            String data = (String)element.value;
            ((Text)all.get(1)).setText(data);
        }else if(element.type == Element.BOOLEAN){
            boolean data = (Boolean)element.value;
            ((Button)all.get(1)).setText(Boolean.toString(data));
            ((Button)all.get(1)).setSelection(data);
        }else if(element.type == Element.TEXTURE){
            TextureData data = (TextureData)element.value;
            ((Button)all.get(1)).setText(data.source);
        }
    }
    
    public void remove(){
        for(Control c : all){
            c.dispose();
        }
    }
    
    public void fireEvent(Element element){
        OpenGG.addExecutable(() -> {
            view.cvm.fireEvent(element);
            view.cvm.updateLocal();  
        });
    }
}
