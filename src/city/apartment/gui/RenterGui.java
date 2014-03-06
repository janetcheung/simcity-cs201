package city.apartment.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import astar.GridCell;
import astar.Moveable;
import city.gui.Gui;
import city.restaurant.Restaurant;

public class RenterGui implements Gui {
	private static final int SIZE = 16;
	private static final int SPEED = 4;

	
	
	private final Moveable agent;
	private int r, c;
	private int rDest,cDest;
	private boolean moving;
	private FoodState foodState;
	private String foodAbbreviation;
	private final Color color;
	
	
	
	private enum FoodState { noFood, cooking, carrying, eating };

	
	
	public RenterGui(Moveable m, GridCell startLoc) {		
		agent = m;
		r = rDest = startLoc.r*ApartmentGui.GRID_SQUARE_SIZE;
		c = cDest = startLoc.c*ApartmentGui.GRID_SQUARE_SIZE;
		moving = false;
		foodState = FoodState.noFood;
		foodAbbreviation = null;
		
		Color[] colors = {
				Color.green,
				Color.yellow,
				Color.blue,
				Color.red,
				Color.cyan
		};
		int randomNumber = (int)Math.floor(Math.random()*colors.length);
		color = colors[randomNumber];
		
	}
	
	
	
	
	
	
	
	public synchronized void updatePosition() {
		if (r < rDest) {
			r += SPEED;
		}
		else if (r > rDest) {
			r -= SPEED;
		}

		if (c < cDest) {
			c += SPEED;
		}
		else if (c > cDest) {
			c -= SPEED;
		}
		
		if ( (r == rDest) && (c == cDest) ) {
			if (moving) {
				moving = false;
				agent.msgFinishedStep();
			}
		}	
	}
	
	public synchronized void setDestination(int row, int col) {
		moving = true;
		rDest = row*ApartmentGui.GRID_SQUARE_SIZE;
		cDest = col*ApartmentGui.GRID_SQUARE_SIZE;		
	}
	
	public synchronized void draw(Graphics2D g2) {
		g2.setColor(color);
		int off = (ApartmentGui.GRID_SQUARE_SIZE - SIZE)/2;
		g2.fillRect(c+off, r+off, 16, 16);
		
		if (foodState == FoodState.cooking) {
			g2.setFont(new Font("Arial", Font.BOLD, 13));
			g2.setColor(Color.white);
			g2.drawString(foodAbbreviation, c + 4 - ApartmentGui.GRID_SQUARE_SIZE, r + 13);
		}
		
		if (foodState == FoodState.carrying) {
			g2.setFont(new Font("Arial", Font.BOLD, 12));
			g2.setColor(Color.orange);
			g2.drawString(foodAbbreviation, c + 4, r + 12 + ApartmentGui.GRID_SQUARE_SIZE);
		}
		
		if (foodState == FoodState.eating) {
			g2.setFont(new Font("Arial", Font.BOLD, 13));
			g2.setColor(Color.orange);
			g2.drawString(foodAbbreviation, c + 4 + ApartmentGui.GRID_SQUARE_SIZE, r + 13);
		}
	}

	
	public synchronized void displayFoodBeingCooked(String foodName) {
		foodAbbreviation = Restaurant.getAbbreviation(foodName);
		foodState = FoodState.cooking;
	}
	public synchronized void displayFoodBeingCarried(String foodName) {
		foodAbbreviation = Restaurant.getAbbreviation(foodName);
		foodState = FoodState.carrying;
	}
	public synchronized void displayFoodBeingEaten(String foodName) {
		foodAbbreviation = Restaurant.getAbbreviation(foodName);
		foodState = FoodState.eating;
	}
	public synchronized void clearFoodDisplay() {
		foodState = FoodState.noFood;
	}





	@Override
	public void setPosition(int x, int y) {}

	
	
	
	
}
