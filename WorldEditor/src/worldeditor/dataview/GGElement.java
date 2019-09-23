/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.dataview;

import com.opengg.core.editor.DataBinding;
import com.opengg.core.engine.OpenGG;
import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import com.opengg.core.math.Vector4f;
import com.opengg.core.model.Model;
import com.opengg.core.render.texture.TextureData;
import worldeditor.JGradientButton;
import worldeditor.RoundedTextField;
import worldeditor.Theme;
import worldeditor.resources.ModelSelectionDialog;
import worldeditor.resources.TextureSelectionDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static java.awt.GridBagConstraints.*;

/**
 * @author Javier
 */
public class GGElement extends JPanel {
    public DataBinding dataBinding;
    public List<Component> all = new ArrayList<>();
    public Object value;

    public GGElement(DataBinding dataBinding) {
        this.dataBinding = dataBinding;

        this.setLayout(new GridLayout(1, 2));//new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setBorder(Theme.padding);

        JLabel l = new JLabel(dataBinding.name + ": ");
        this.add(l);
        //setting up fields
        switch (dataBinding.type) {
            case VECTOR4F -> {
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


                PropertyChangeListener mf = (e) -> {
                    try {
                        Vector4f nvector = new Vector4f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText()), Float.parseFloat(v4.getText()));
                        value = nvector;
                        if (dataBinding.autoupdate) fireEvent();
                    } catch (Exception ex) {
                    }
                };
                v1.addPropertyChangeListener(mf);
                v2.addPropertyChangeListener(mf);
                v3.addPropertyChangeListener(mf);
                v4.addPropertyChangeListener(mf);

                break;
            }
            case VECTOR3F -> {
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


                PropertyChangeListener mf = (e) -> {
                    try {
                        Vector3f nvector = new Vector3f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()), Float.parseFloat(v3.getText()));
                        value = nvector;
                        if (dataBinding.autoupdate) fireEvent();
                    } catch (Exception ex) {
                    }
                };
                v1.addPropertyChangeListener(mf);
                v2.addPropertyChangeListener(mf);
                v3.addPropertyChangeListener(mf);
                break;
            }
            case VECTOR2F -> {

                JFormattedTextField v1 = new JFormattedTextField(new DecimalFormat());
                this.add(v1);
                all.add(v1);

                JFormattedTextField v2 = new JFormattedTextField(new DecimalFormat());
                this.add(v2);
                all.add(v2);


                PropertyChangeListener mf = (e) -> {
                    try {
                        Vector2f nvector = new Vector2f(Float.parseFloat(v1.getText()), Float.parseFloat(v2.getText()));
                        value = nvector;
                        if (dataBinding.autoupdate) fireEvent();
                    } catch (Exception ex) {
                    }
                };
                v1.addPropertyChangeListener(mf);
                v2.addPropertyChangeListener(mf);

                break;
            }
            case FLOAT -> {

                JFormattedTextField v1 = new JFormattedTextField(new DecimalFormat());
                this.add(v1);
                all.add(v1);


                v1.addPropertyChangeListener((e) -> {
                    try {
                        value = Float.parseFloat(v1.getText());
                        if (dataBinding.autoupdate) fireEvent();
                    } catch (Exception ex) {
                    }
                });


                JLabel fillspace = new JLabel();
                this.add(fillspace,
                        new GridBagConstraints(RELATIVE, 0, 3, 1, 0.5, 0.5, CENTER, BOTH,
                                new Insets(5, 5, 5, 5), 2, 2));
                break;
            }
            case INTEGER -> {

                JFormattedTextField v1 = new JFormattedTextField(NumberFormat.getIntegerInstance());
                this.add(v1);
                all.add(v1);


                v1.addPropertyChangeListener((e) -> {
                    try {
                        value = Integer.parseInt(v1.getText());
                        if (dataBinding.autoupdate) fireEvent();
                    } catch (Exception ex) {
                    }
                });
                break;
            }
            case STRING -> {

                JTextField v1 = new RoundedTextField(12);
                this.add(v1, new GridBagConstraints(RELATIVE, 0, 3, 1, 0.5, 0.5, CENTER, BOTH, new Insets(5, 5, 5, 5), 2, 2));
                all.add(v1);


                v1.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        value = v1.getText();
                        if (dataBinding.autoupdate) fireEvent();
                    }
                });
            /*v1.addPropertyChangeListener((e) -> {
                element.value = v1.getText();
                if (element.autoupdate) fireEvent(element);
            });*/

                break;
            }
            case BOOLEAN -> {

                JToggleButton button = new JToggleButton();
                this.add(button);
                all.add(button);


                button.addItemListener(e -> {
                    value = e.getStateChange() == ItemEvent.SELECTED;
                    button.setText(e.getStateChange() == ItemEvent.SELECTED ? "True" : "False");
                    if (dataBinding.autoupdate) fireEvent();
                });

                break;
            }
            case TEXTURE -> {

                JButton button = new JButton("Choose Texture");
                this.add(button);
                all.add(button);


                button.addActionListener(e ->
                        OpenGG.asyncExec(() -> {
                            TextureData data = TextureSelectionDialog.getData(SwingUtilities.getWindowAncestor(this)).get();
                            if (data == null) return;

                            value = data;
                            if (dataBinding.autoupdate) fireEvent();

                        }));

                break;
            }
            case MODEL -> {

                JButton button = new JGradientButton("Choose Model");
                this.add(button);
                all.add(button);


                button.addActionListener(e ->
                        OpenGG.asyncExec(() -> {
                            Model model = ModelSelectionDialog.getModel(SwingUtilities.getWindowAncestor(this)).get();
                            if (model == null) return;
                            value = model;
                            if (dataBinding.autoupdate) fireEvent();

                        }));

                break;
            }
        }

        //setting up updating view
        switch (dataBinding.type) {
            case VECTOR4F -> ((DataBinding<Vector4f>) dataBinding).onDataChange(data -> {
                ((JFormattedTextField) all.get(0)).setText(Float.toString(data.x));
                ((JFormattedTextField) all.get(1)).setText(Float.toString(data.y));
                ((JFormattedTextField) all.get(2)).setText(Float.toString(data.z));
                ((JFormattedTextField) all.get(3)).setText(Float.toString(data.w));
            });
            case VECTOR3F -> ((DataBinding<Vector3f>) dataBinding).onDataChange(data -> {
                ((JFormattedTextField) all.get(0)).setText(Float.toString(data.x()));
                ((JFormattedTextField) all.get(1)).setText(Float.toString(data.y()));
                ((JFormattedTextField) all.get(2)).setText(Float.toString(data.z()));
            });
            case VECTOR2F -> ((DataBinding<Vector2f>) dataBinding).onDataChange(data -> {
                ((JFormattedTextField) all.get(0)).setText(Float.toString(data.x));
                ((JFormattedTextField) all.get(1)).setText(Float.toString(data.y));
            });
            case FLOAT -> ((DataBinding<Float>) dataBinding).onDataChange(data -> ((JFormattedTextField) all.get(0)).setText(Float.toString(data)));
            case INTEGER -> ((DataBinding<Integer>) dataBinding).onDataChange(data -> ((JFormattedTextField) all.get(0)).setText(Integer.toString(data)));
            case STRING -> ((DataBinding<String>) dataBinding).onDataChange(data -> ((JTextField) all.get(0)).setText(data));
            case BOOLEAN -> ((DataBinding<Boolean>) dataBinding).onDataChange(data -> {
                ((JToggleButton) all.get(0)).setText(data ? "True" : "False");
                ((JToggleButton) all.get(0)).setSelected(data);
            });
            case TEXTURE -> ((DataBinding<TextureData>) dataBinding).onDataChange(data -> ((JButton) all.get(0)).setText(data.getSource()));
            case MODEL -> ((DataBinding<Model>) dataBinding).onDataChange(data -> ((JButton) all.get(0)).setText(data.getName()));
        }

        if (!dataBinding.autoupdate) {
            JButton button = new JGradientButton("Enter");
            button.addActionListener(e -> fireEvent());
            this.add(button);

        }
    }

    public void fireEvent() {
        OpenGG.asyncExec(() -> dataBinding.setValueFromView(value));
    }
}
