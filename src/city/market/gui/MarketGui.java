package city.market.gui;

import java.awt.Graphics2D;

import javax.swing.ImageIcon;

import city.gui.BuildingGui;
import city.gui.ZoomedPanel;

public class MarketGui extends BuildingGui{
	private final ImageIcon background;
	//private final ZoomedPanel zoomedPanel;

	public MarketGui(){
        background = new ImageIcon("img/marketBackground.png");
        //this.zoomedPanel = zoomedPanel;
	}
	
	public synchronized void draw(Graphics2D g2){
		background.paintIcon(null,g2,0,0);
        drawGuis(g2);
	}
	/*
	public void updateInventoryDisplay(int[] newInventory) {
		if (zoomedPanel != null) {
			zoomedPanel.msgUpdateFood(newInventory);
		}
	}
	*/
}
