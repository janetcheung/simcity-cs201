package city.restaurant.gui;

import java.awt.Color;
import java.awt.*;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

import astar.GridCell;
import city.restaurant.Restaurant;
import city.restaurant.RestaurantParticipantPuppet;


public class RestaurantCustomerGui extends RestaurantParticipantGui {
	
	private boolean displayFoodQuestion, displayFoodBeingEaten;
	private Image imageSelf;
	private String foodAbbreviation;
	
	public RestaurantCustomerGui(RestaurantParticipantPuppet p, GridCell startLoc) {
		super(p, startLoc);
		displayFoodQuestion = false;
		displayFoodBeingEaten = false;
		imageSelf = (new ImageIcon("img/pacman.png").getImage());
		foodAbbreviation = null;
	}
	
	
	@Override
	public synchronized void draw(Graphics2D g) {
		int off=(GRID_SQUARE_SIZE-SIZE)/2;
		g.drawImage(imageSelf, c+off, r+off, SIZE, SIZE, null, null);
		
		g.setFont(new Font("Arial", Font.BOLD, 16));
		if (displayFoodQuestion) {
			g.setColor(Color.black);
			g.drawString(foodAbbreviation + "?", c + RestaurantGui.GRID_SQUARE_SIZE + 4, r + 16);
		}
		if (displayFoodBeingEaten) {
			g.setColor(Color.orange);
			g.drawString(foodAbbreviation, c + RestaurantGui.GRID_SQUARE_SIZE + 4, r + 16);
		}
	}
	
	
	public synchronized void displayFoodQuestion(String foodName) {
		displayFoodBeingEaten = false;
		displayFoodQuestion = true;
		foodAbbreviation = Restaurant.getAbbreviation(foodName);
	}
	public synchronized void displayFoodBeingEaten(String foodName) {
		displayFoodQuestion = false;
		displayFoodBeingEaten = true;
		foodAbbreviation = Restaurant.getAbbreviation(foodName);
	}
	public synchronized void clearExtraDisplays() {
		displayFoodQuestion = false;
		displayFoodBeingEaten = false;
	}
}