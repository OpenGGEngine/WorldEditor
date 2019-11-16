package worldeditor.dataview;

import com.opengg.core.editor.BindingAggregate;
import com.opengg.core.editor.ViewModel;
import worldeditor.Theme;
import worldeditor.WorldEditor;
import worldeditor.components.JGradientButton;

import javax.swing.*;
import java.awt.*;

public class GGView extends JPanel{
    protected BindingAggregate bindings;

    public GGView(BindingAggregate bindings){
        this.bindings = bindings;
        this.doLayout();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        bindings.getDataBindings().stream()
                .map(GGElement::new)
                .forEach(this::add);
    }

    public BindingAggregate getBindings(){
        return this.bindings;
    }

}
