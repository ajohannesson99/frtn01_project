package graphics;

import se.lth.control.BoxPanel;

import javax.swing.*;
import java.awt.*;

public class ProgressBox extends BoxPanel {

    private String name;

    private ProgressSquare progressSquare;
    private JLabel nameLabel;


    public ProgressBox(String name){
        super(BoxPanel.VERTICAL);

        progressSquare = new ProgressSquare();
        nameLabel = new JLabel(name);
        nameLabel.setForeground(Color.GRAY);

        this.add(progressSquare);
        this.add(nameLabel);
    }

    public void activate() {
        progressSquare.activate();
        nameLabel.setForeground(Color.BLACK);
    }

    public void deactivate() {
        progressSquare.deactivate();
        nameLabel.setForeground(Color.GRAY);
    }
}
