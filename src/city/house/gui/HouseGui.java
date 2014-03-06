package city.house.gui;

import java.awt.Graphics2D;
import javax.swing.ImageIcon;
import city.gui.BuildingGui;

public class HouseGui extends BuildingGui{
	public static final int GRID_SQUARE_SIZE=20;
	
	private final ImageIcon background;
	
	public HouseGui(){
		background=new ImageIcon("img/housebackground.png");
	}

	public void draw(Graphics2D g2){
		background.paintIcon(null,g2,0,0);
		drawGuis(g2);
	}
}
