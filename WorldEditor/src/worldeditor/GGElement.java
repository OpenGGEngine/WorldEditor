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

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import static java.awt.GridBagConstraints.*;

/**
 *
 * @author Javier
 */
public class GGElement extends JPanel{
    public Element element;
    public GGView view;
    public List<Component> all = new ArrayList<>();
    
    public GGElement(Element element, GGView view) {
        super();
        this.element = element;
        this.view = view;

        this.setLayout(new GridLayout(1,2));//new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setBorder(Theme.padding);
        JLabel l = new JLabel(element.name+": ");

        this.add(l);
        if (element.type == Element.Type.VECTOR4F) {
            JFormattedTextField v1 = new JFormattedTextField(new DecimalFormat());
            this.add(v1);
            all.add(v1);

            JFormattedTextField v2 = new JFormattedTextField(new DecimalFormat());
            this.add(v2);
            all.add(v2);

            JFormattedTextField v3 = new JFormattedTextField(new DecimalFormat());
            this.add(v3);
            all.add(v3);

            JFormattedTextField v4 = new JFormattedTextField(new DecimalFormat());
            this.add(v4);

            all.add(v4);

            update(true);

            PropertyChangeListener mf = (e) -> {
                try {
                    Vector4f nvector = new Vector4f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText()), Float.parseFloat(v4.getText()));
                    element.value = nvector;
                    if (view != null && element.autoupdate) fireEvent(element);
                } catch (Exception ex) {
                }
            };
            v1.addPropertyChangeListener(mf);
            v2.addPropertyChangeListener(mf);
            v3.addPropertyChangeListener(mf);
            v4.addPropertyChangeListener(mf);

        } else if (element.type == Element.Type.VECTOR3F) {
            l.setIcon(Theme.vec3);
            JFormattedTextField v1 = new JFormattedTextField(new DecimalFormat());
            this.add(v1);
            all.add(v1);

            JFormattedTextField v2 = new JFormattedTextField(new DecimalFormat());
            this.add(v2);
            all.add(v2);

            JFormattedTextField v3 = new JFormattedTextField(new DecimalFormat());
            this.add(v3);
            all.add(v3);
            update(true);

            PropertyChangeListener mf = (e) -> {
                try {
                    Vector3f nvector = new Vector3f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText()));
                    element.value = nvector;
                    if (view != null && element.autoupdate) fireEvent(element);
                } catch (Exception ex) {
                }
            };
            v1.addPropertyChangeListener(mf);
            v2.addPropertyChangeListener(mf);
            v3.addPropertyChangeListener(mf);
        } else if (element.type == Element.Type.VECTOR2F) {

            JFormattedTextField v1 = new JFormattedTextField(new DecimalFormat());
            this.add(v1);
            all.add(v1);

            JFormattedTextField v2 = new JFormattedTextField(new DecimalFormat());
            this.add(v2);
            all.add(v2);

            update(true);

            PropertyChangeListener mf = (e) -> {
                try {
                    Vector2f nvector = new Vector2f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()));
                    element.value = nvector;
                    if (view != null && element.autoupdate) fireEvent(element);
                } catch (Exception ex) {
                }
            };
            v1.addPropertyChangeListener(mf);
            v2.addPropertyChangeListener(mf);

        } else if (element.type == Element.Type.FLOAT) {

            JFormattedTextField v1 = new JFormattedTextField(new DecimalFormat());
            this.add(v1);
            all.add(v1);

            update(true);

            v1.addPropertyChangeListener((e) -> {
                try {
                    element.value = Float.parseFloat(v1.getText());
                    if (view != null && element.autoupdate) fireEvent(element);
                } catch (Exception ex) {
                }
            });


            JLabel fillspace = new JLabel();
            this.add(fillspace,
                    new GridBagConstraints(RELATIVE, 0, 3, 1, 0.5, 0.5, CENTER, BOTH,
                            new Insets(5,5,5,5), 2, 2));
        } else if (element.type == Element.Type.INTEGER) {

            JFormattedTextField v1 = new JFormattedTextField(NumberFormat.getIntegerInstance());
            this.add(v1);
            all.add(v1);

            update(true);

            v1.addPropertyChangeListener((e) -> {
                try {
                    element.value = Integer.parseInt(v1.getText());
                    if (view != null && element.autoupdate) fireEvent(element);
                } catch (Exception ex) {
                }
            });
        } else if (element.type == Element.Type.STRING) {

            JTextField v1 = new RoundedTextField(12);
            this.add(v1, new GridBagConstraints(RELATIVE, 0, 3, 1, 0.5, 0.5, CENTER, BOTH, new Insets(5,5,5,5), 2, 2));
            all.add(v1);

            update(true);


            v1.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    element.value = v1.getText();
                    if (view != null && element.autoupdate) fireEvent(element);
                }
            });
            /*v1.addPropertyChangeListener((e) -> {
                element.value = v1.getText();
                if (view != null && element.autoupdate) fireEvent(element);
            });*/

        } else if (element.type == Element.Type.BOOLEAN) {

            JToggleButton button = new JToggleButton();
            this.add(button);
            all.add(button);

            update(true);

            button.addItemListener(e -> {
                element.value = e.getStateChange() == ItemEvent.SELECTED;
                button.setText(e.getStateChange() == ItemEvent.SELECTED ? "True" : "False");
                if (view != null && element.autoupdate) fireEvent(element);
            });

        } else if (element.type == Element.Type.TEXTURE) {

            JButton button = new JButton("Choose Texture");
            this.add(button);
            all.add(button);

            update(true);

            button.addActionListener(e ->
                OpenGG.asyncExec(() -> {
                    TextureData data = TextureSelectionDialog.getData(SwingUtilities.getWindowAncestor(this)).get();
                    if (data == null) return;

                    element.value = data;
                    if (view != null && element.autoupdate) fireEvent(element);
                    update(true);
                }));

        } else if (element.type == Element.Type.MODEL) {
            JButton button = new JGradientButton("Choose Model");
            this.add(button);
            all.add(button);

            update(true);

            button.addActionListener(e ->
                OpenGG.asyncExec(() -> {
                    Model model = ModelSelectionDialog.getModel(SwingUtilities.getWindowAncestor(this)).get();
                    if (model == null) return;
                    element.value = model;
                    if (view != null && element.autoupdate) fireEvent(element);
                    update(true);
                }));

        }

        if (!element.autoupdate) {
            JButton button = new JGradientButton("Enter");
            this.add(button);
            button.addActionListener(e -> {
                if (view != null) fireEvent(element);
            });
        }

        update(true);
    }
    
    public void update(){
        update(false);
    }
    
    public void update(boolean force){
        /*try{
            ((Label)all.get(0)).setText(element.name);
        }catch(Exception e){
            GGConsole.warning("Exception when loading GGElement: " + e.getMessage());
            this.removeAll();
            WorldEditor.setView(null);
            return; 
        }*/
        if(!element.forceupdate && !force) return;
        
        if(element.type == Element.Type.VECTOR4F){
            Vector4f data = (Vector4f)element.value;
            ((JFormattedTextField)all.get(0)).setText(Float.toString(data.x));
            ((JFormattedTextField)all.get(1)).setText(Float.toString(data.y));
            ((JFormattedTextField)all.get(2)).setText(Float.toString(data.z));
            ((JFormattedTextField)all.get(3)).setText(Float.toString(data.w));
        }else if(element.type == Element.Type.VECTOR3F){
            Vector3f data = (Vector3f)element.value;
            ((JFormattedTextField)all.get(0)).setText(Float.toString(data.x()));
            ((JFormattedTextField)all.get(1)).setText(Float.toString(data.y()));
            ((JFormattedTextField)all.get(2)).setText(Float.toString(data.z()));
        }else if(element.type == Element.Type.VECTOR2F){
            Vector2f data = (Vector2f)element.value;
            ((JFormattedTextField)all.get(0)).setText(Float.toString(data.x));
            ((JFormattedTextField)all.get(1)).setText(Float.toString(data.y));
        }else if(element.type == Element.Type.FLOAT){
            float data = (Float)element.value;
            ((JFormattedTextField)all.get(0)).setText(Float.toString(data));
        }else if(element.type == Element.Type.INTEGER){
            int data = (Integer)element.value;
            ((JFormattedTextField)all.get(0)).setText(Integer.toString(data));
        }else if(element.type == Element.Type.STRING){
            String data = (String)element.value;
            ((JTextField)all.get(0)).setText(data);
        }else if(element.type == Element.Type.BOOLEAN){
            boolean data = (Boolean)element.value;
            ((JToggleButton)all.get(0)).setText(data ? "True" : "False");
            ((JToggleButton)all.get(0)).setSelected(data);
        }else if(element.type == Element.Type.TEXTURE){
            TextureData data = (TextureData)element.value;
            ((JButton)all.get(0)).setText(data.source);
        }else if(element.type == Element.Type.MODEL){
            Model model = (Model)element.value;
            ((JGradientButton)all.get(0)).setText(model == null? "Choose Model" : model.getName());
        }
    }
    
    public void fireEvent(Element element){
        OpenGG.asyncExec(() -> {
            view.getViewModel().fireEvent(element);
            view.getViewModel().updateLocal(element);
        });
    }
}
