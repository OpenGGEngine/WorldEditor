/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.console.GGConsole;
import com.opengg.core.engine.Resource;
import com.opengg.core.render.texture.TextureData;
import com.opengg.core.render.texture.TextureManager;


import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Javier
 */
public class TextureSelectionDialog extends JDialog {
    LambdaContainer<TextureData> container = new LambdaContainer<>();
    
    public static TextureData getData(Window window){

        TextureSelectionDialog shell = new TextureSelectionDialog(window);
        
        while(!shell.isActive()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return shell.container.value;
    }
    
    public TextureSelectionDialog(Window window){
        super(window, "Select Texture");

        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(200, 200));

        JPanel content = new JPanel();
        content.setLayout(new GridLayout(0,1));
        this.getContentPane().add(content);

        JScrollPane pane = new JScrollPane();
        pane.setWheelScrollingEnabled(true);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setLayout(new ScrollPaneLayout());
        content.add(pane);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 5));
        panel.setMinimumSize(new Dimension(400,400));
        pane.add(panel);
        
        for(var tex : TextureManager.getData().values()){
            JButton button = new JButton();
            button.setText(tex.source);
            panel.add(button);

            button.addActionListener(a -> {
                String name = button.getText();
                container.value = TextureManager.getTextureData(name);

                this.dispose();
            });
        }

        JPanel input = new JPanel();
        input.setLayout(new FlowLayout());
        content.add(input);

        JTextField newtex = new JTextField();
        newtex.setToolTipText("Relative or local texture filename");
        input.add(newtex);

        JButton enter = new JButton();
        enter.setText("Load Texture");
        input.add(enter);
        enter.addActionListener(e -> {
            try{
                container.value = Resource.getTextureData(newtex.getText());
                this.dispose();
            }catch(Exception ex){
                GGConsole.warning("Failed to load texture at " + newtex.getText());
            }
        });

        this.pack();
        this.setVisible(true);
    }
}
