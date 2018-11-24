package worldeditor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

public class Theme {
    public static Color mainBG = Color.decode("#212121");
    public static Color text = Color.decode("#EEEEEE");
    public static Color scrollBG = Color.decode("#666666");
    public static Color buttonBG = Color.decode("#424242");
    public static Color progressBarBG = Color.decode("#4caf50");
    public static Color toggleFalse = Color.decode("#F0513F");
    public static Color toggleTrue = Color.decode("#8BC34F");
    public static Border inputborder = new BevelBorder(BevelBorder.LOWERED);
    public static Border buttonNone = new EmptyBorder(1,1,1,1);
    public static Border extremeBut = new EmptyBorder(1,6,1,6);
    public static Border defNone = new EmptyBorder(0,0,0,0);
    public static Border padding = new EmptyBorder(12,4,12,4);
    public static Font consoleFont = new Font("Verdana",0,11);
    public static Font toolbarFont = new Font("Verdana",0,12);
    public static ImageIcon vec3 =  new ImageIcon(new ImageIcon("resources/icons/smallcoord.png","coord").getImage());
    public static ImageIcon trash = new ImageIcon("resources/icons/remove.png","remove");

    public static void applyTheme() throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        UIManager.put("List.background", Theme.mainBG);
        UIManager.put("List.foreground", Theme.text);
        UIManager.put("List.font", Theme.toolbarFont);
        UIManager.put("List.border", Theme.defNone);
        UIManager.put("Panel.background", Theme.mainBG);
        UIManager.put("Label.foreground", Theme.text);
        UIManager.put("Label.font",consoleFont);
        UIManager.put("TextArea.background", Theme.mainBG);
        UIManager.put("TextArea.foreground", Theme.text);
        UIManager.put("TextArea.font", Theme.consoleFont);
        UIManager.put("TextArea.border", Theme.defNone);
        UIManager.put("TextField.background", Theme.buttonBG);
        UIManager.put("TextField.foreground", Theme.text);
        UIManager.put("TextField.font", Theme.consoleFont);
        UIManager.put("TextField.border", Theme.inputborder);
        UIManager.put("TextField.caretForeground", Theme.text);
        UIManager.put("FormattedTextField.background", Theme.mainBG);
        UIManager.put("FormattedTextField.foreground", Theme.text);
        UIManager.put("FormattedTextField.border", Theme.inputborder);
        UIManager.put("FormattedTextField.caretForeground", Theme.text);
        UIManager.put("FormattedTextField.font", Theme.toolbarFont);
        UIManager.put("Tree.background", Theme.mainBG);
        UIManager.put("Tree.foreground", Theme.text);
        UIManager.put("Menu.background", Theme.buttonBG);
        UIManager.put("Menu.font", Theme.toolbarFont);
        UIManager.put("Menu.foreground", Theme.text);
        UIManager.put("MenuBar.background", Theme.buttonBG);
        UIManager.put("MenuBar.border", Theme.defNone);
        UIManager.put("MenuItem.background", Theme.buttonBG);
        UIManager.put("MenuItem.font", Theme.toolbarFont);
        UIManager.put("MenuItem.foreground", Theme.text);
        UIManager.put("Tree.textBackground", Theme.mainBG);
        UIManager.put("Tree.textForeground", Theme.text);
        UIManager.put("Tree.font", Theme.consoleFont);
        UIManager.put("ProgressBar.background", Theme.buttonBG);
        UIManager.put("ProgressBar.foreground", Theme.progressBarBG);
        UIManager.put("ProgressBar.border", Theme.defNone);
        UIManager.put("Button.background", Theme.buttonBG);
        UIManager.put("Button.border", Theme.buttonNone);
        UIManager.put("Button.select", Theme.buttonBG);
        UIManager.put("Button.foreground", Theme.text);
        UIManager.put("ToggleButton.background", Theme.toggleFalse);
        UIManager.put("ToggleButton.border", Theme.buttonNone);
        UIManager.put("ToggleButton.select", Theme.toggleTrue);
        UIManager.put("ToggleButton.foreground", Theme.text);
        UIManager.put("ScrollPane.border", Theme.defNone);
    }
    private static class RoundedBorder implements Border {

        private int radius;


        RoundedBorder(int radius) {
            this.radius = radius;
        }


        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }


        public boolean isBorderOpaque() {
            return true;
        }


        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
}
