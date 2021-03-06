package worldeditor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Theme {
    public static Color mainBG = Color.decode("#262626");
    public static Color text = Color.decode("#EEEEEE");
    public static Color scrollBG = Color.decode("#666666");
    public static Color defTextArea = Color.LIGHT_GRAY;
    public static Color textArea = Color.decode("#323232");
    public static Color buttonBG = Color.decode("#424242");
    public static Color progressBarBG = Color.decode("#4caf50");
    public static Color toggleFalse = Color.decode("#AC123A");//Color.decode("#F0513F");
    public static Color toggleTrue = Color.decode("#228B22");//Color.decode("#8BC34F");
    public static Border inputborder = new BevelBorder(BevelBorder.LOWERED);
    public static Border buttonNone = new EmptyBorder(1,1,1,1);
    public static Border extremeBut = new EmptyBorder(1,6,1,6);
    public static Border defNone = new EmptyBorder(0,0,0,0);
    public static Border padding = new EmptyBorder(12,4,12,4);
    public static Font consoleFont;

    static {
        try {
            consoleFont = Font.createFont(Font.TRUETYPE_FONT,new File("resources/font/quicksand.ttf"));
            consoleFont = consoleFont.deriveFont(Font.PLAIN,12.3f);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Font toolbarFont = consoleFont;//new Font("Quicksand Medium",0,13);
    public static Font buttonFont = consoleFont;//new Font("Quicksand Medium",Font.BOLD,12);
    public static ImageIcon vec3 =  new ImageIcon(new ImageIcon("resources/icons/smallcoord.png","coord").getImage());
    public static ImageIcon sound =  new ImageIcon(new ImageIcon("resources/icons/soundicon.png","sound").getImage());
    public static ImageIcon trash = new ImageIcon("resources/icons/remove.png","remove");
    public static ImageIcon folderIcon = new ImageIcon("resources/icons/folder.png","folder");
    public static ImageIcon scriptIcon = new ImageIcon("resources/icons/script.png","script");
    public static ImageIcon fileIcon = new ImageIcon("resources/icons/file.png","file");
    public static ImageIcon searchIcon = new ImageIcon("resources/icons/search.png");

    public static Border tabPaneBorder = BorderFactory.createEtchedBorder();

    public static void applyTheme() throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        UIManager.put("Separator.insets",new Insets(10,10,10,10));
        UIManager.put("CheckBox.background",Theme.mainBG);
        UIManager.put("CheckBox.foreground",Theme.text);
        UIManager.put("CheckBox.focus",Theme.mainBG);
        UIManager.put("CheckBox.textIconGap",5);
        UIManager.put("List.background", Theme.mainBG);
        UIManager.put("List.foreground", Theme.text);
        UIManager.put("List.font", Theme.toolbarFont);
        UIManager.put("List.border", Theme.defNone);
        UIManager.put("Panel.background", Theme.mainBG);
        UIManager.put("Label.foreground", Theme.text);
        UIManager.put("Label.font",consoleFont);
        UIManager.put("TextArea.background", Theme.textArea);
        UIManager.put("TextArea.foreground", Theme.text);
        UIManager.put("TextArea.font", Theme.consoleFont);
        UIManager.put("TextArea.border", Theme.defNone);
        UIManager.put("TextPane.background",Theme.textArea);
        UIManager.put("TextPane.foreground",Theme.text);
        UIManager.put("TextPane.font",Theme.consoleFont);
        UIManager.put("TextPane.caretForeground", Color.black);
        UIManager.put("TextField.background", Theme.defTextArea);
        UIManager.put("TextField.foreground", Color.black);
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
        UIManager.put("Tree.font", Theme.consoleFont);
        UIManager.put("OptionPane.messageForeground", Theme.text);
        UIManager.put("OptionPane.border", Theme.defNone);
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
        UIManager.put("Button.font",Theme.buttonFont);
        UIManager.put("ToggleButton.background", Theme.toggleFalse);
        UIManager.put("ToggleButton.border", Theme.buttonNone);
        UIManager.put("ToggleButton.select", Theme.toggleTrue);
        UIManager.put("ToggleButton.shadow",Theme.toggleTrue);
        UIManager.put("ToggleButton.foreground", Theme.text);
        UIManager.put("ToggleButton.font",Theme.buttonFont.deriveFont(Font.BOLD));
        UIManager.put("ScrollPane.border", Theme.defNone);
        UIManager.put("TabbedPane.background",Theme.textArea);
        UIManager.put("TabbedPane.selected",Theme.buttonBG);
        UIManager.put("TabbedPane.foreground",Theme.text);
        UIManager.put("TabbedPane.border", Theme.tabPaneBorder);
        UIManager.put("TabbedPane.borderHightlightColor", Theme.buttonBG.brighter());
        UIManager.put("SplitPane.dividerSize", 5);
        UIManager.put("SplitPane.border", Theme.tabPaneBorder);
        UIManager.put("SplitPaneDivider.border",Theme.tabPaneBorder);
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
