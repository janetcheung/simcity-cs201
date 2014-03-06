package city.gui;

import java.awt.*;

import javax.swing.JPanel;

public interface Gui{
    public void updatePosition();
	public void setDestination(int x,int y);
	public void setPosition(int x, int y);
	public void draw(Graphics2D g2);
}
