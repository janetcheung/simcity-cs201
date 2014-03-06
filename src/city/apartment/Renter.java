package city.apartment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.ApartmentInterface;
import UnitTestingCommon.interfaces.RenterInterface;
import astar.GridCell;
import astar.MovementManager;
import city.Broadcaster;
import city.Building;
import city.Puppet;
import city.apartment.gui.RenterGui;
import city.market.Market.Food;

public class Renter extends Puppet implements RenterInterface {
	
	
	
	private static int minutesToPutAwayGroceries = 5;
	private static int minutesToGetFoodFromFridge = 3;
	private static int minutesToSitOnToilet = 10;
	private static int minutesMinimumToLieInBed = 10;
	private static int minutesToGoOutDoor = 0;
	
	
	
	
	
	// DATA
	
	private final RenterGui gui;
	private boolean holdingNewGroceries;
	private int minutesToEat;
	public final int[] foodInventory;
	private boolean needToLeave;
	public State state;
	private int minutesVisitDuration;
	public boolean doneWithVisit;
	private int indexFoodToCook;
	
	private final GridCell locFridgeAccess;
	private final GridCell locStoveAccess;
	private final GridCell locTableAccess;
	private final GridCell locToilet;
	private final GridCell locBed;
	
	
	
	
	
	// ENUMS AND CLASSES
	
	public enum State { initial, goingToPutAwayGroceries, goingToGetFoodFromFridge, goingToCookFoodAtStove,
		goingToEatFoodAtTable, goingToBathroom, goingToBed, goingToLeave }
	
	
	
	
	
	
	// BASIC UTILITIES
	
	public int getTotalFoodInventoryCount() {
		int count = 0;
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			count += foodInventory[i];
		}
		return count;
	}
	
	
	
	
	
	
	
	
	
	
	// CONSTRUCTOR ///////////////////////////////////////////////////////////////////
	
	public Renter(String name, MovementManager m, ApartmentInterface a, Setup s) {
		super(name, m, a, s);
		gui = new RenterGui(this, Apartment.locEntrance);
		holdingNewGroceries = s.boughtNewGroceries;
		minutesToEat = s.minutesMealDuration;
		foodInventory = s.foodInventory;
		needToLeave = false;
		doneWithVisit = true;
		state = State.initial;
		minutesVisitDuration = 0;
		GridCell locRoomEntrance = Apartment.locRoomEntrances[s.apartmentRoomNumber];
		if (locRoomEntrance.r < Apartment.locEntrance.r) {
			// Room is in upper half of apartment
			locFridgeAccess = locRoomEntrance.add(Apartment.offsetFridgeAccess);
			locStoveAccess = locRoomEntrance.add(Apartment.offsetStoveAccess);
			locTableAccess = locRoomEntrance.add(Apartment.offsetTableAccess);
			locToilet = locRoomEntrance.add(Apartment.offsetToilet);
			locBed = locRoomEntrance.add(Apartment.offsetBed);
		}
		else {
			// Room is in lower half of apartment
			locFridgeAccess = locRoomEntrance.add(Apartment.offsetFridgeAccess.invertRow());
			locStoveAccess = locRoomEntrance.add(Apartment.offsetStoveAccess.invertRow());
			locTableAccess = locRoomEntrance.add(Apartment.offsetTableAccess.invertRow());
			locToilet = locRoomEntrance.add(Apartment.offsetToilet.invertRow());
			locBed = locRoomEntrance.add(Apartment.offsetBed.invertRow());
		}
		
		/*System.out.println(this.getAgentName() + " food inventory:");
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (foodInventory[i] > 0) {
				System.out.println("	" + foodInventory[i] + " of " + Food.getFood(i).name);
			}
		}*/
	}
	
	
	
	
	
	
	public RenterGui getGui() {
		return gui;
	}
	
	
	
	
	
	
	
	
	
	
	
	// MESSAGES
	
	@Override
	public void msgLeaveBuilding() {
		enqueMutation(new Mutation(){
			public void apply(){
				needToLeave = true;
			}
		});
	}
	
	
	

	
	
	
	
	
	
	// ACTIONS
	
	@Override
	protected void actMoveStep(int r, int c) {
		gui.setDestination(r, c);
	}
	@Override
	protected void actReactToBlockedRoute() {
		this.actWaitRandomAndTryAgain();
	}
	@Override
	protected void actArrivedAtDestination() {
		if (state == State.goingToCookFoodAtStove) {
			gui.displayFoodBeingCooked(Food.getFood(indexFoodToCook).name);
		}
		if (state == State.goingToEatFoodAtTable) {
			gui.displayFoodBeingEaten(Food.getFood(indexFoodToCook).name);
		}
		final long trueMillisecondsVisitDuration = minutesVisitDuration*Broadcaster.getMinuteMillis();
		(new Timer()).schedule(
			new TimerTask() {
				public void run() {
					enqueMutation(new Mutation() {
						public void apply() {
							if (state == State.goingToCookFoodAtStove) {
								gui.clearFoodDisplay();
							}
							if (state == State.goingToEatFoodAtTable) {
								gui.clearFoodDisplay();
							}
							doneWithVisit = true;
						}
					});
				}
			}
			, trueMillisecondsVisitDuration);
	}
	public void actVisitLoc(GridCell dest, int minutesVisitDuration) {
		doneWithVisit = false;
		this.minutesVisitDuration = minutesVisitDuration;
		this.setNewDestination(dest.r, dest.c);
	}
	public void actPutAwayGroceries() {
		AlertLog.getInstance().logMessage(AlertTag.APT_RESIDENT, getAgentName(), "Putting away groceries");
		holdingNewGroceries = false;
		state = State.goingToPutAwayGroceries;
		this.actVisitLoc(locFridgeAccess, minutesToPutAwayGroceries);
		this.getMaster().msgPutAwayGroceries();
	}
	public void actGetFoodFromFridge() {
		// Randomly choose the food to get from the foods available
		ArrayList<Integer> foodOptions = new ArrayList<Integer>();
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (foodInventory[i] > 0) {
				foodOptions.add(i);
			}
		}
		if (foodOptions.size() > 0) {
			state = State.goingToGetFoodFromFridge;
			this.actVisitLoc(locFridgeAccess, minutesToGetFoodFromFridge);
			int randomNumber = (int)Math.floor(Math.random()*foodOptions.size());
			indexFoodToCook = foodOptions.get(randomNumber);
			foodInventory[indexFoodToCook]--;
			AlertLog.getInstance().logMessage(AlertTag.APT_RESIDENT, getAgentName(), "Getting food " + Food.getFood(indexFoodToCook) + " from fridge");
		}
		else {
			// We are not able to cook anything, so make sure that the Renter doesn't try again
			minutesToEat = 0;
		}
	}
	public void actCookFood() {
		state = State.goingToCookFoodAtStove;
		this.actVisitLoc(locStoveAccess, Food.getFood(indexFoodToCook).minutesCookingTime);
	}
	public void actEatFood() {
		AlertLog.getInstance().logMessage(AlertTag.APT_RESIDENT, getAgentName(), "Eating " + Food.getFood(indexFoodToCook).name);
		gui.displayFoodBeingCarried(Food.getFood(indexFoodToCook).name);
		state = State.goingToEatFoodAtTable;
		this.actVisitLoc(locTableAccess, minutesToEat);
		minutesToEat = 0;
		this.getMaster().msgAteFood();
	}
	public void actGoToBathroom() {
		state = State.goingToBathroom;
		this.actVisitLoc(locToilet, minutesToSitOnToilet);
	}
	public void actGoToBed() {
		state = State.goingToBed;
		this.actVisitLoc(locBed, minutesMinimumToLieInBed);
	}
	public void actLeave() {
		state = State.goingToLeave;
		this.actVisitLoc(Apartment.locExit, minutesToGoOutDoor);
	}
	public void actTerminate() {
		/*System.out.println(this.getAgentName() + " food inventory:");
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (foodInventory[i] > 0) {
				System.out.println("	" + foodInventory[i] + " of " + Food.getFood(i).name);
			}
		}*/
		
		this.getMaster().msgUpdateInventory(foodInventory);
		this.getMaster().msgPuppetLeftBuilding();
		this.removeFromManager();
		this.stopAgent();
		((Apartment)this.getBuilding()).msgRenterLeft(this);
	}
	
	
	
	
	
	
	
	// SCHEDULER
	
	@Override
	protected boolean pickAndExecuteAction() {
		
		
		if (doneWithVisit) {
			
			// Continuations of action sequences
			
			if (state == State.goingToGetFoodFromFridge) {
				this.actCookFood();
				return true;
			}
			if (state == State.goingToCookFoodAtStove) {
				this.actEatFood();
				return true;
			}
			if (state == State.goingToLeave) {
				this.actTerminate();
				return true;
			}
			
			// Roots of action sequences
			
			if (holdingNewGroceries) {
				this.actPutAwayGroceries();
				return true;
			}
			if (needToLeave) {
				this.actLeave();
				return true;
			}
			if (minutesToEat > 0) {
				this.actGetFoodFromFridge();
				return true;
			}
			if (state == State.goingToBathroom) {
				this.actGoToBed();
				return true;
			}
			if (state != State.goingToBed) {
				this.actGoToBathroom();
				return true;
			}
			
			
		}
		return false;
	}






	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}

}
