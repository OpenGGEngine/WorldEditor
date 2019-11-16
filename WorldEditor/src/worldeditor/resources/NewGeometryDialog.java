package worldeditor.resources;

import com.opengg.core.editor.BindingAggregate;
import com.opengg.core.world.structure.viewmodel.GeometryViewModel;
import worldeditor.WorldEditor;
import worldeditor.components.JGradientButton;
import worldeditor.dataview.GGElement;

import javax.swing.*;
import java.awt.*;

public class NewGeometryDialog extends JDialog {
    public NewGeometryDialog(BindingAggregate initializer, GeometryViewModel cvm, Window window){
        super(window, "New Geometry");

        setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        this.setLayout(new FlowLayout());
        this.setSize(400, 400);

        JPanel total = new JPanel();
        total.setLayout(new GridLayout(0,1));
        this.getContentPane().add(total);

        JScrollPane content = new JScrollPane();
        content.setLayout(new ScrollPaneLayout());
        content.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        content.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        initializer.getDataBindings().stream()
                .map(GGElement::new)
                .forEach(total::add);

        JGradientButton create = new JGradientButton("Create Geometry");
        create.addActionListener(e -> {
            WorldEditor.createGeometry(initializer, cvm);
            this.dispose();
        });
        create.setBorderPainted(false);
        total.add(create);
        total.revalidate();

        this.pack();
        this.repaint();
        this.revalidate();
        this.setVisible(true);
    }
}