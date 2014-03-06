package city.restaurant.gui;

import java.awt.Graphics2D;

import javax.swing.ImageIcon;

import city.gui.BuildingGui;
import city.gui.ZoomedPanel;

public class RestaurantGui extends BuildingGui{
	public static final int GRID_SQUARE_SIZE=20;
	
	private final ImageIcon background;
	private final ZoomedPanel zoomedPanel;

	public RestaurantGui(ZoomedPanel zoomedPanel){
        background=new ImageIcon("img/restaurantBackground.png");
        this.zoomedPanel = zoomedPanel;
	}
	
	public void draw(Graphics2D g2){
		background.paintIcon(null,g2,0,0);
		drawGuis(g2);
	}
	
	public void updateInventoryDisplay(int[] newInventory) {
		if (zoomedPanel != null) {
			zoomedPanel.msgUpdateFood(newInventory);
		}
	}
}
