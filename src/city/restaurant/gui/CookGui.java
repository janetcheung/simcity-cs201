package city.restaurant.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import astar.GridCell;
import city.restaurant.Restaurant;
import city.restaurant.RestaurantCustomerPuppet;
import city.restaurant.RestaurantParticipantPuppet;


public class CookGui extends RestaurantParticipantGui {
	
	

	private Image imageSelf;
	
	
	
	
	public CookGui(RestaurantParticipantPuppet p, GridCell startLoc) {
		super(p, startLoc);
		imageSelf = (new ImageIcon("img/chef.png").getImage());
	}

	
	@Override
	public synchronized void draw(Graphics2D g) {
		//g.setColor(Color.yellow);
		int off=(GRID_SQUARE_SIZE-SIZE)/2;
		//g.fillRect(c+off,r+off,SIZE,SIZE);
		g.drawImage(imageSelf, c+off, r+off, SIZE, SIZE, null, null);
	}
	
	
	
	
	
	
}