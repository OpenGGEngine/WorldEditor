package worldeditor;

import javax.swing.*;
import java.awt.*;

public  final class JGradientButton extends JButton {
    public JGradientButton(String text){
        super(text);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setPaint(new GradientPaint(
                new Point(0, 0),
                getBackground(),
                new Point(0, 3*getHeight()/5),
                getBackground().darker()));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(),3,3);
        g2.dispose();

        super.paintComponent(g);
    }
}
