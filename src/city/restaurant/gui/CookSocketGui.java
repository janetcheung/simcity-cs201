package city.restaurant.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import astar.GridCell;
import city.gui.Gui;
import city.restaurant.Restaurant;
import city.restaurant.RestaurantCustomerPuppet;



// Displays the foods being cooked or plated.
// Belongs to a CookSocket instead of a CookAgent because it needs to persist between cook shifts.
public class CookSocketGui implements Gui {


	private class Food {
		public final RestaurantCustomerPuppetInterface ID;
		public boolean plated;
		public String foodAbbreviation;
		public Food(RestaurantCustomerPuppetInterface ID, String foodName) {
			plated = false;
			this.ID = ID;
			foodAbbreviation = Restaurant.getAbbreviation(foodName);
		}
	}
	private class FoodSlot {
		public final GridCell loc;
		public Food occupyingFood;
		public FoodSlot(GridCell loc) {
			this.loc = loc;
			occupyingFood = null;
		}
	}
	
	
	private final ArrayList<FoodSlot> foodSlots;
	private final GridCell translationToAdjacentFood;
	
	
	
	
	public CookSocketGui() {
		foodSlots = new ArrayList<FoodSlot>();
		if (Restaurant.offsetCookingAreaOriginFromCook.r == 0) {
			// Cooking/plating areas run up and down
			translationToAdjacentFood = new GridCell(1, 0);
		}
		else {
			// Cooking/plating areas run side to side
			translationToAdjacentFood = new GridCell(0, 1);
		}
		foodSlots.add(new FoodSlot(Restaurant.locCook));
	}

	@Override
	public synchronized void draw(Graphics2D g) {
		g.setFont(new Font("Arial", Font.BOLD, 12));
		for (FoodSlot fs : foodSlots) {
			if (fs.occupyingFood != null) {
				GridCell actualGridLoc;
				if (fs.occupyingFood.plated) {
					g.setColor(Color.orange);
					actualGridLoc = fs.loc.add(Restaurant.offsetPlatingAreaOriginFromCook);
				}
				else {
					g.setColor(Color.pink);
					actualGridLoc = fs.loc.add(Restaurant.offsetCookingAreaOriginFromCook);
				}
				g.drawString(fs.occupyingFood.foodAbbreviation, actualGridLoc.c*RestaurantParticipantGui.GRID_SQUARE_SIZE + 2, actualGridLoc.r*RestaurantParticipantGui.GRID_SQUARE_SIZE + 12);		
			}
		}
	}
	
	
	public synchronized void displayFoodBeingCooked(RestaurantCustomerPuppetInterface foodID, String foodName) {
		FoodSlot freeSlot = null;
		for (FoodSlot temp : foodSlots) {
			if (temp.occupyingFood == null) {
				freeSlot = temp;
			}
		}
		if (freeSlot == null) {
			// Need to create new FoodSlot
			GridCell slotToBranchFrom;
			if (foodSlots.size() == 1) {
				slotToBranchFrom = foodSlots.get(0).loc;	// First slot
			}
			else {
				slotToBranchFrom = foodSlots.get(foodSlots.size()-2).loc;	// Slot that was added two times ago
			}
			if (foodSlots.size() % 2 == 1) {
				// For alternate slot additions, add it in the positive direction
				foodSlots.add(new FoodSlot(slotToBranchFrom.add(translationToAdjacentFood)));
			}
			else {
				// For alternate slot additions, add it in the negative direction
				foodSlots.add(new FoodSlot(slotToBranchFrom.minus(translationToAdjacentFood)));
			}
			freeSlot = foodSlots.get(foodSlots.size()-1);	// the slot we will use is the same as the one we just added
		}
		freeSlot.occupyingFood = new Food(foodID, foodName);
	}
	
	public synchronized void displayFoodPlated(RestaurantCustomerPuppetInterface foodID) {
		for (FoodSlot fs : foodSlots) {
			if (fs.occupyingFood != null) {
				if (fs.occupyingFood.ID == foodID) {
					fs.occupyingFood.plated = true;
				}
			}
		}
	}
	
	public synchronized void removeFoodDisplay(RestaurantCustomerPuppetInterface foodID) {
		for (FoodSlot fs : foodSlots) {
			if (fs.occupyingFood != null) {
				if (fs.occupyingFood.ID == foodID) {
					fs.occupyingFood = null;
				}
			}
		}
	}

	@Override
	public void updatePosition() {}
	@Override
	public void setDestination(int x, int y) {}
	@Override
	public void setPosition(int x, int y) {}
	
	
	
	
}