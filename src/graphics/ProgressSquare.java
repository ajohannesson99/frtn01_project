package graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ProgressSquare extends JPanel {
    private int squareX = 5;
    private int squareY = 5;
    private int squareW = 40;
    private int squareH = 40;
    private Color activeColour = Color.RED;

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(activeColour);
        g.fillRect(squareX, squareY, squareW, squareH);
    }

    public void activate() {
        this.activeColour = Color.GREEN;
        this.repaint();
    }

    public void deactivate() {
        this.activeColour = Color.RED;
        this.repaint();
    }
}
