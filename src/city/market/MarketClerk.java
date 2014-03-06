package city.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketCustomerInterface;
import UnitTestingCommon.interfaces.MarketInterface;
import UnitTestingCommon.interfaces.MarketSignInSysInterface;
import astar.MovementManager;
import city.Building;
import city.Puppet;
import city.market.Market.Food;
import city.market.gui.MarketGui;
import city.market.gui.MarketClerkGui;

public class MarketClerk extends Puppet implements MarketClerkInterface {

	// utilities
	public MarketClerk(String name,MovementManager m,Building b,Setup s) {
		super(name, m, b, s);
		// TODO Auto-generated constructor stub
		this.name = name;
		mClerkGui = new MarketClerkGui(this);
		
	}
	
	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub
		timeToLeave = true;
		mSignInSys.msgRemoveClerk(this);
		//this.pickAndExecuteAction();
	}

	
	@Override
	protected void actMoveStep(int r, int c) {
		// TODO Auto-generated method stub
		mClerkGui.setDestination(r, c);
	}

	@Override
	protected void actReactToBlockedRoute() {
		// TODO Auto-generated method stub
		//System.out.println("got blocked. " + "current position: " + this.getRow() + ", " + this.getCol());
		AlertLog.getInstance().logMessage(AlertTag.MARK_CLERK, getAgentName(), "got blocked. " + "current position: " + this.getRow() + ", " + this.getCol());
		this.actWaitRandomAndTryAgain();
	}

	@Override
	protected void actArrivedAtDestination() {
		// TODO Auto-generated method stub
		//arrived.release();
		/*
		System.out.println("arrived destination " + destinations.get(0));
		//if (!destinations.isEmpty()) {
			// remove from destination list
			destinations.remove(0);
			if (!destinations.isEmpty()) {
				// set the next destination
				this.setNewDestination(address.get(destinations.get(0)).row, address.get(destinations.get(0)).col);
			}
		//}
		 */
			
		//System.out.println("arrived destination " + destinations.get(0));
		
//		if (destinations.isEmpty()) {
//			//System.out.println("destinations size is " + destinations.size());
//			if (this.getRow() == address.get("Home").row && this.getCol() == address.get("Home").col) {
//				/*
//				if (state == AgentState.Available) {
//					mSignInSys.msgImAvailable(this);
//				}
//				*/
//				
//				if (state == AgentState.GoFetchingItems) {
//					for (String key : customerOrder.keySet()) {
//						// deep copy HashMap customerList to itemsInventory
//						itemsInventory.put(key, customerOrder.get(key));
//					}
//					
//					myCustomer.msgHereIsBill(customerOrder, priceList);
//					//this.
//				}
//				
//				if (state == AgentState.Cashier) {
//					// notify market
//					market.msgItemRestock(itemsInventory);
//					
//					// update public money
//					market.addPublicMoney(customerPayment);
//					
//					event = AgentEvent.CustomerLeaving;
//					this.pickAndExecuteAction();
//					//stateChange()
//					/*
//					enqueMutation(new Mutation(){
//						public void apply(){
//							event = AgentEvent.CustomerLeaving;
//						}
//					});
//					*/
//				}
//			} 
//			
//			else if (this.getRow() == address.get("Exit").row && this.getCol() == address.get("Exit").col) {
//				this.getMaster().msgPuppetLeftBuilding();
//				this.removeFromManager();
//				market.msgKillClerk(this);
//				
//			}
//			
//			
//			else {
//			// go home
//				this.setNewDestination(address.get("Home").row, address.get("Home").col);
//			}
//		}
//			
//		else {
//			// remove from destination list
//			destinations.remove(0);
//			if (!destinations.isEmpty()) {
//				// set the next destination
//				this.setNewDestination(address.get(destinations.get(0)).row, address.get(destinations.get(0)).col);
//			}
//		}
		
		/*
		System.out.println(this.name + " arrived. " + "home position: " + addresses.get("Home").row + ", " + addresses.get("Home").col +  
				" current position: " + this.getRow() + ", " + this.getCol() + 
				" destinations size: " + destinations.size() + 	
				" AgentState: " + state
				);
		*/
		
		
		if (this.getRow() == addresses.get("Home").row && this.getCol() == addresses.get("Home").col) {
			
			if (state == AgentState.Entering) {
				event = AgentEvent.finishEntering;
				this.pickAndExecuteAction();
			}
			
			if (state == AgentState.GoFetchingItems) {
				event = AgentEvent.finishFetching;
				this.pickAndExecuteAction();
			}
			
			if (state == AgentState.CheckingPaymentGoRestocking) {
				event = AgentEvent.finishRestocking;
				this.pickAndExecuteAction();
			}
			
		} 
		else if (this.getRow() == addresses.get("Exit").row && this.getCol() == addresses.get("Exit").col && state == AgentState.Leaving) {
			this.getMaster().msgPuppetLeftBuilding();
			this.removeFromManager();
			market.msgKillClerk(this);
			
		}
		
		else {
			destinations.remove(0);
			if (!destinations.isEmpty()) {
				this.setNewDestination(addresses.get(destinations.get(0)).row, addresses.get(destinations.get(0)).col);
			}
			else {
				this.setNewDestination(addresses.get("Home").row, addresses.get("Home").col);
			}
		}
		
		
	}

	public String getName() {
		return name;
	}
	
	public void setMarket(MarketInterface m) {
		market = m;
	}
	
	public void setMarketSignInSys(MarketSignInSysInterface mSignIn) {
		mSignInSys = mSignIn;
	}
	
	/*
	public void setGui(MarketClerkGui g) {
		this.mClerkGui = g;
	}
	*/
	
	
	public MarketClerkGui getGui() {
		return mClerkGui;
	}
	
	public void setMainGui(MarketGui mGui) {
		mainGui = mGui;
	}
	
	/*
	public int getHomeRow() {
		return home.row;
	}
	
	public int getHomeCol() {
		return home.col;
	}
	*/
	
	/*
	public class Position {	// public for JUnit testing
		public int row;	// public for JUnit testing
		public int col;	// public for JUnit testing
		Position(int r, int c) {
			row = r;
			col = c;
		}
	}
	*/
	
	// data
	private String name;
	
	public enum AgentState
	{Initial, Entering, Available, Idle, CheckingInStock, GoFetchingItems, Cashier, CheckingPaymentGoRestocking, UpdatingStock, Leaving};
	public AgentState state = AgentState.Initial;//The start state	// public for JUnit testing

	public enum AgentEvent
	{none, finishEntering, customerComing, orderRequestFromCustomer, itemAvailabilityResponse, finishFetching, customerPaying, finishRestocking, stockUpdated, signInUpdated};
	public AgentEvent event = AgentEvent.none;	// public for JUnit testing
	
	public HashMap<String, Integer> customerOrder = new HashMap<String, Integer>();	// public for JUnit testing
	/*{
		hm.put("Steak", 12000);
		hm.put("Chicken", 10000);
		hm.put("Salad", 6000);
		hm.put("Pizza", 8000);
	}*/
	
	public HashMap<String, Integer> itemsInventory = new HashMap<String, Integer>();	// public for JUnit testing
	
	/*
	public HashMap<String, Double> priceList = new HashMap<String, Double>();	// public for JUnit testing
	{
		priceList.put("Steak", 15.99);
		priceList.put("Chicken", 10.99);
		priceList.put("Salad", 5.99);
		priceList.put("Pizza", 8.99);
		priceList.put("Car", 999.99);
	}
	*/
	
	private MarketInterface market;
	public MarketCustomerInterface myCustomer;	// public for JUnit testing
	private MarketSignInSysInterface mSignInSys;
	
	public double customerPayment;	// public for JUnit testing
	
	private MarketClerkGui mClerkGui;
	
	//public Position home;	// public for JUnit testing
	
	public HashMap<String, Position> addresses = new HashMap<String, Position>();	// public for JUnit testing // really?
	{
		/*
		addresses.put("Steak", new Position(5, 6));
		addresses.put("Chicken", new Position(5, 11));
		addresses.put("Salad", new Position(5, 16));
		addresses.put("Pizza", new Position(5, 21));
		*/
		addresses.put("Car", new Position(5, 26));
		addresses.put("Exit", new Position(0, 2));

		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (Food.allFoods[i].index % 4 == 0) {
				addresses.put(Food.allFoods[i].name, new Position(5, 6));
			}
			
			else if (Food.allFoods[i].index % 4 == 1) {
				addresses.put(Food.allFoods[i].name, new Position(5, 11));
			}
			
			else if (Food.allFoods[i].index % 4 == 2) {
				addresses.put(Food.allFoods[i].name, new Position(5, 16));
			}
			
			else if (Food.allFoods[i].index % 4 == 3) {
				addresses.put(Food.allFoods[i].name, new Position(5, 21));
			}
			
			else {
				//System.err.println("modulus error");
				AlertLog.getInstance().logError(AlertTag.MARK_CLERK, getAgentName(), "modulus error");
			}
		}
	}
	
	private MarketGui mainGui;
	
	//private Semaphore arrived = new Semaphore(0,true);	// the scheduler has to be running for gui moving
	
	public List<String> destinations = new ArrayList<String>();
	
	private boolean timeToLeave = false;
	private boolean allowedToLeave = false;
	
	Timer timer = new Timer();
	private Semaphore timeSem = new Semaphore(0,true);
	
	// messages
	public void msgImComing() {
		enqueMutation(new Mutation(){
			public void apply(){
				event = AgentEvent.customerComing;
			}
		});
	}
	
	public void msgThisIsMyOrder(final MarketCustomerInterface mCustomer, final HashMap<String, Integer> groceryList) {
		enqueMutation(new Mutation(){
			public void apply(){
				//System.out.println("msgThisIsMyOrder received");
				AlertLog.getInstance().logMessage(AlertTag.MARK_CLERK, getAgentName(), "msgThisIsMyOrder received");
				myCustomer = mCustomer;
				customerOrder = groceryList;
				event = AgentEvent.orderRequestFromCustomer;
			}
		});
	}
	
	public void msgItemAvailability(final HashMap<String, Integer> updatedList) {
		enqueMutation(new Mutation(){
			public void apply(){
				//System.out.println("msgItemAvailability received");
				AlertLog.getInstance().logMessage(AlertTag.MARK_CLERK, getAgentName(), "msgItemAvailability received");
				customerOrder = updatedList;
				event = AgentEvent.itemAvailabilityResponse;
				//System.out.println("event is " + event + ", next destination is = " + destinations.get(0) + ", isTraveling is " + MarketClerk.this.isTraveling());
			}
		});
	}
	
	public void msgHereIsMyDecision(final HashMap<String, Integer> decisionList, final double cash) {
		enqueMutation(new Mutation(){
			public void apply(){
				customerOrder = decisionList;
				customerPayment = cash;
				event = AgentEvent.customerPaying;
			}
		});
	}
	
	/*
	public void msgGoodbye() {
		enqueMutation(new Mutation(){
			public void apply(){
				event = AgentEvent.CustomerLeaving;
			}
		});
	}*/
	
	public void msgAssignHomePosition(final Position p) {
		enqueMutation(new Mutation(){
			public void apply(){
				//home = new Position(p.row, p.col);
				addresses.put("Home", new Position(p.row, p.col));
				state = AgentState.Initial;
				//MarketClerk.this.pickAndExecuteAction();
			}
		});
	}
	
	public void msgYouCanLeave() {
		enqueMutation(new Mutation(){
			public void apply(){
				allowedToLeave = true;
				MarketClerk.this.pickAndExecuteAction();
			}
		});
	}
	
	// scheduler
	@Override
	public boolean pickAndExecuteAction() {	// public for JUnit testing
		// TODO Auto-generated method stub
		/*
		if (this.isTraveling()) {
			if (!destinations.isEmpty()) {
				return true;
			}
			return false;
		}
		*/
		
		/*
		if (!destinations.isEmpty()) {
			return false;
		}
		*/
		
		//if (state == AgentState.Initial && event == AgentEvent.none){
		if (state == AgentState.Initial) {
			state = AgentState.Entering;
			enterMarket();
			return true;
		}
		
		if (state == AgentState.Entering && event == AgentEvent.finishEntering) {
			state = AgentState.Available;
			updateSignIn();
			return true;
		}
		
		if (state == AgentState.Available && event == AgentEvent.customerComing) {
			// wait for mCustomer coming and do not leave
			state = AgentState.Idle;
			return true;
		}
		
		if (state == AgentState.Idle && event == AgentEvent.orderRequestFromCustomer) {
			state = AgentState.CheckingInStock;
			checkInStock();
			return true;
		}
		
		if (state == AgentState.CheckingInStock && event == AgentEvent.itemAvailabilityResponse) {
			state = AgentState.GoFetchingItems;
			//System.out.println("going to fetch items");
			AlertLog.getInstance().logMessage(AlertTag.MARK_CLERK, getAgentName(), "going to fetch items");
			goCheckInStock();
			return true;
		}
		
		if (state == AgentState.GoFetchingItems && event == AgentEvent.finishFetching) {
			state = AgentState.Cashier;
			getBackToCustomer();
			return true;
		}
		
		if (state == AgentState.Cashier && event == AgentEvent.customerPaying) {
			state = AgentState.CheckingPaymentGoRestocking;
			checkPaymentGoRestock();
			return true;
		}
		
		if (state == AgentState.CheckingPaymentGoRestocking && event == AgentEvent.finishRestocking) {
			state = AgentState.UpdatingStock;
			updateStock();
			return true;
		}
		
		if (state == AgentState.UpdatingStock && event == AgentEvent.stockUpdated) {
			state = AgentState.Available;
			updateSignIn();
			return true;
		}
		
		// get off work
		// potential bugs could occur w/o a response from mSignInSys b/c the message processing could be delayed
		if (state == AgentState.Available && (event == AgentEvent.signInUpdated || event == AgentEvent.finishEntering) && allowedToLeave) {
			state = AgentState.Leaving;
			getOffWork();
			return true;
		}
		
		
		/*
		if (address.get("Home") != null) {
			goToHomePosition();
			//System.out.println("going to home position");
		}
		*/
		return false;
	}
	
	// action
	private void enterMarket() {
		//System.out.println("going to home position. row = " + address.get("Home").row +" col = "+ address.get("Home").col);
		
		//destinations.add("Home");
		
		if (addresses.get("Home") != null) {
			this.setNewDestination(addresses.get("Home").row, addresses.get("Home").col);
			//System.out.println("going to home position");
		}
		
		
		else {
			//System.out.println(this.name + " has no home position");
			AlertLog.getInstance().logMessage(AlertTag.MARK_CLERK, getAgentName(), this.name + " has no home position");
			//state = AgentState.Initial;
		}
		
	}
	
	private void checkInStock() {
		
		// delay 500
		timer.schedule(new TimerTask() {
            public void run(){
            	timeSem.release();
            }
	    },
	    500);
		
		try {
			timeSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		market.msgCheckInStock(this, customerOrder);
	}
	
	private void goCheckInStock() {
		// availability has already updated but still pretend to go check for 0 items
		//System.out.println("going to check in stock and then get back to customer. item availabilities are " + customerOrder);
		AlertLog.getInstance().logMessage(AlertTag.MARK_CLERK, getAgentName(), "going to check in stock and then get back to customer. item availabilities are " + customerOrder);
		
		// gui moves to fetch items
		for (String key: customerOrder.keySet()) {
			destinations.add(key);
		}
		// then returns back to customer
		//destinations.add("Home");
		//System.out.println("destinations are " + destinations);
		
		// check if destination list is empty
		if (!destinations.isEmpty()) {
			this.setNewDestination(addresses.get(destinations.get(0)).row, addresses.get(destinations.get(0)).col);
		}
		else {	// destination list is empty
			event = AgentEvent.finishFetching;
			this.pickAndExecuteAction();
		}
		
		
		/*
		for (String key : customerOrder.keySet()) {
			// deep copy HashMap customerList to itemsInventory
			itemsInventory.put(key, customerOrder.get(key));
		}
		
		myCustomer.msgHereIsBill(customerOrder, priceList);
		*/
	}
	
	private void getBackToCustomer() {
		for (String key : customerOrder.keySet()) {
			// deep copy HashMap customerList to itemsInventory
			if (customerOrder.get(key) != 0) {	// erase 0 items
				itemsInventory.put(key, customerOrder.get(key));
			}
		}
		
		myCustomer.msgHereIsBill(customerOrder, MarketInterface.priceList);
	}
	
	private void checkPaymentGoRestock() {
		// deep copy customerOrder
		// b/c customer is about to leave, his data is not safe any more
		HashMap<String, Integer> tempHM = customerOrder;
		customerOrder = new HashMap<String, Integer>();
		for (String key : tempHM.keySet()) {
			if (tempHM.get(key) != 0) {	// erase 0 items
				customerOrder.put(key, tempHM.get(key));
			}
		}
		
		// check if the payment is correct
		double temp = 0;
		for (String key : customerOrder.keySet()) {
			temp += market.priceList.get(key) * customerOrder.get(key);
		}
		
		// delay 200
		timer.schedule(new TimerTask() {
            public void run(){
            	timeSem.release();
            }
	    },
	    200);
		
		try {
			timeSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if ((int)temp == (int)customerPayment) {
			// thank you goodbye
			myCustomer.msgThankYouGoodbye();
		}
		
		// figure out what to put back to stock (update stock)
		for (String key : customerOrder.keySet()) {
			itemsInventory.put(key, itemsInventory.get(key) - customerOrder.get(key));
			
			/*
			if (itemsInventory.get(key) - customerOrder.get(key) != 0) {
				itemsInventory.put(key, itemsInventory.get(key) - customerOrder.get(key));
			}
			else {	// erase 0 items
				itemsInventory.remove(key);
			}
			*/
		}
		
		// deep copy itemsInventory to erase 0 items
		tempHM = itemsInventory;
		itemsInventory = new HashMap<String, Integer>();
		for (String key : tempHM.keySet()) {
			if (tempHM.get(key) != 0) {	// erase 0 items
				itemsInventory.put(key, tempHM.get(key));
			}
		}
		
		// gui moves to put items back
		for (String key : itemsInventory.keySet()) {
			destinations.add(key);
		}
		//destinations.add("Home");
		
		// check if destination list is empty
		if (!itemsInventory.isEmpty()) {
			this.setNewDestination(addresses.get(destinations.get(0)).row, addresses.get(destinations.get(0)).col);
		}
		else {	// destination list is empty
			event = AgentEvent.finishRestocking;
			this.pickAndExecuteAction();
		}
		
	}
	
	private void updateStock() {
		
		// notify market
		market.msgItemRestock(itemsInventory);
		
		// update public money
		market.addPublicMoney(customerPayment);
		
		event = AgentEvent.stockUpdated;
		/*
		//stateChange()
		enqueMutation(new Mutation(){
			public void apply(){
				event = AgentEvent.CustomerLeaving;
			}
		});
		*/
	}
	
	private void updateSignIn() {
		myCustomer = null;
		mSignInSys.msgImAvailable(this);
		
		event = AgentEvent.signInUpdated;
	}
	
	private void getOffWork() {
		this.setNewDestination(addresses.get("Exit").row, addresses.get("Exit").col);
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
	
}
