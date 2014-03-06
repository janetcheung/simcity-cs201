package city.restaurant.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

import astar.GridCell;
import city.restaurant.Restaurant;
import city.restaurant.RestaurantParticipantPuppet;


public class WaiterGui extends RestaurantParticipantGui {

	private boolean displayFoodBeingCarried;
	private Image imageSelf;
	private String foodAbbreviation;
	
	public WaiterGui(RestaurantParticipantPuppet p, GridCell startLoc, boolean regular) {
		super(p, startLoc);
		displayFoodBeingCarried = false;
		if (regular) {
			imageSelf = (new ImageIcon("img/hand.png").getImage());
		}
		else {
			imageSelf = (new ImageIcon("img/red_hand.png").getImage());
		}
		foodAbbreviation = null;
	}

	@Override
	public synchronized void draw(Graphics2D g) {
		int off=(GRID_SQUARE_SIZE-SIZE)/2;
		g.drawImage(imageSelf, c+off, r+off, SIZE, SIZE, null, null);
		
		g.setFont(new Font("Arial", Font.BOLD, 13));
		if(displayFoodBeingCarried){
			g.setColor(Color.orange);
			g.drawString(foodAbbreviation, c + 4, r + RestaurantGui.GRID_SQUARE_SIZE + 16);
		}
	}
	
	public synchronized void displayFoodBeingCarried(String foodChoice) {
		displayFoodBeingCarried = true;
		foodAbbreviation = Restaurant.getAbbreviation(foodChoice);
	}
	
	public synchronized void clearExtraDisplays() {
		displayFoodBeingCarried = false;
	}
	
	
}