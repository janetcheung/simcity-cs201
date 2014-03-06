package city.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.AgentInterface;
import UnitTestingCommon.interfaces.BuildingInterface;
import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import agent.Agent;
import agent.Agent.BlockingData;
import astar.MovementManager;
import city.Bill;
import city.Broadcaster;
import city.Building;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.Truck;
import city.gui.ZoomedPanel;
import city.market.gui.MarketGui;
import city.restaurant.Restaurant;

public class Market extends Building implements MarketInterface {
	public static final double CAR_PRICE=1000.0; // TODO
	
	// utilities
	//private final TestFrame animation;
	public enum Food {
		// We'll just assume restaurant price to be 2 * marketPrice
		// name, index, minutesCookingTime, marketPrice
		garlic("Garlic", 0, 8, 0.50),
		moose("Moose", 1, 45, 20.0),
		chocolateMilk("Chocolate Milk", 2, 5, 1.25),
		lemon("Lemon", 3, 5, 0.75),
		eggsBenedict("Eggs Benedict", 4, 30, 8.0),
		rat("Rat", 5, 30, 0.01),
		zebra("Zebra", 6, 45, 30.0),
		sweetPotatoes("Sweet Potatoes", 7, 30, 5.0),
		bread("Bread", 8, 8, 2.0),
		clamChowder("Clam Chowder", 9, 30, 7.0),
		peanutButter("Peanut Butter", 10, 8, 1.50),
		banana("Banana", 11, 5, 1.5),
		pumpkinPie("Pumpkin Pie", 12, 20, 4.0),
		noodle("Noodle", 13, 20, 2.0),
		bottleOfRum("Bottle of Rum", 14, 5, 6.0),
		jellyfish("Jellyfish", 15, 15, 10.0),
		sauerkraut("Sauerkraut", 16, 5, 1.25),
		taco("Taco", 17, 15, 3.0),
		quesadilla("Quesadilla", 18, 15, 3.0),
		smokedSalmon("Smoked Salmon", 19, 8, 5.50),
		bagel("Bagel", 20, 10, 2.50),
		asparagus("Asparagus", 21, 15, 2.0),
		roadkillSpecial("Roadkill Special", 22, 55, 15.0),
		freshTofu("Fresh Tofu", 23, 8, 8.0),
		turkishDelight("Turkish Delight", 24, 8, 9.0),
		caramel("Caramel", 25, 10, 1.75),
		cake("Cake", 26, 8, 5.0),
		leftovers("Leftovers",27, 8, 0.05),
		mustard("Mustard", 28, 8, 1.5),
		butter("Butter", 29, 8, 1.0),
		eyeballs("Eyeballs", 30, 12, 12.0),
		giantSquid("Giant Squid", 31, 50, 400.0),
		truffleDeluxe("Truffle Deluxe", 32, 30, 60.0),
		narwhal("Narwhal", 33, 40, 100.0);
		static final Food[] allFoods = { 
			garlic,
			moose,
			chocolateMilk,
			lemon,
			eggsBenedict,
			rat,
			zebra,
			sweetPotatoes,
			bread,
			clamChowder,
			peanutButter,
			banana,
			pumpkinPie,
			noodle,
			bottleOfRum,
			jellyfish,
			sauerkraut,
			taco,
			quesadilla,
			smokedSalmon,
			bagel,
			asparagus,
			roadkillSpecial,
			freshTofu,
			turkishDelight,
			caramel,
			cake,
			leftovers,
			mustard,
			butter,
			eyeballs
		};
		// All data is public final, so no need for accessor methods
		public final String name;
		public final int index;
		public final int minutesCookingTime;
		public final double marketPrice;
		private Food(String name, int index, int minutesCookingTime, double marketPrice) {
			this.name = name;
			this.index = index;
			this.minutesCookingTime = minutesCookingTime;
			this.marketPrice = marketPrice;
		}
		public static Food getFood(int index) {
			return allFoods[index];
		}
		public static int getNumberOfFoods() {
			return allFoods.length;
		}
		public static Food getFood(String name) {
			for (int i = 0; i < Food.getNumberOfFoods(); i++) {
				// iterate allFoods array
				if (allFoods[i].name.equals(name)) {	// in order to find the index
					return allFoods[i];
				}
			}
			//System.err.println("invalid food name");
			AlertLog.getInstance().logError(AlertTag.MARK, "Market", "invalid food name");
			
			return null;
		}
	};
	
	

	public Market(String name,int pr,int pc,int cr,int cc,int tlr,int tlc,int brr,int brc){
		
		super(name,pr,pc,cr,cc,new MovementManager(name+" MovementManager",insideNumRows,insideNumCols,"marketLayout.txt"),tlr,tlc,brr,brc, new MarketGui());
		//gui=new MarketGui();
		//this.animation = animation;
		
		mSignInSys=new MarketSignInSys("signIn");
		mSignInSys.startAgent();
		
		//mTruck = new Truck("truck", manager, this, i, i);
		//mTruck.startAgent();
	}
	
	
	@Override
	public void msgSpawnPuppet(final BlockingData<PuppetInterface> result,final String name,final Setup setupPackage) {
		enqueMutation(new Mutation(){
			public void apply(){
				if (myClosedState == ClosedState.closed || myClosedState == ClosedState.forceClosed) {
					AlertLog.getInstance().logInfo(AlertTag.MARK, "Market Closed -- denying entrance to " + setupPackage.role + " puppet", name);
					result.unblock(null);
					return;
				}
				
				// TODO
				switch (setupPackage.role) {
				/* Seven tasks for each Puppet:
				 * 		1. Instantiate
				 * 		2. Add to movement manager
				 * 		3. Instantiate and add gui
				 * 		4. Add gui to animation panel
				 * 		5. Return Puppet pointer to its Person master
				 * 		6. Start Puppet thread
				 */
				case customer:
					
					// create MarketCustomer
					MarketCustomer mCustomer = new MarketCustomer(name, getMovementManager(),Market.this,setupPackage);
					// set market
					mCustomer.setMarket(Market.this);
					
					// set signInSys
					mCustomer.setMarketSignInSys(mSignInSys);
					// msgSignIn
					//mSignInSys.msgWaitingInLine(mCustomer);
					
					mCustomer.setMainGui((MarketGui) gui);
					gui.addGui(mCustomer.getGui());
					
					getMovementManager().msgAddUnit(mCustomer,1,1,23,28);
					
					/*
					MarketCustomerGui mCustomerGui = new MarketCustomerGui(mCustomer);
					mCustomer.setGui(mCustomerGui);
					*/
					
					result.unblock(mCustomer);
					
					// add to pausables
					//pausables.add(mCustomer); NOT NEEDED ANYMORE
					
					mCustomer.startAgent();
					
					break;
					
				case markClerk:
					//initialize signInSheet
					//add to currentTellers
					//send message to signIn
					
					// create MarketClerk
					MarketClerk mClerk = new MarketClerk(name, getMovementManager(), Market.this, setupPackage);
					// set market
					mClerk.setMarket(Market.this);
					// set signInSys
					mClerk.setMarketSignInSys(mSignInSys);
					// add to signInSys
					mSignInSys.msgAddClerk(mClerk);
					
					mClerk.setMainGui((MarketGui) gui);
					gui.addGui(mClerk.getGui());
					
					// how long does the add take?
					//System.out.println("workingClerks size is " + mSignInSys.workingClerks.size());
					
					
					getMovementManager().msgAddUnit(mClerk,1,1,0,1);
					
					/*
					MarketClerkGui mClerkGui = new MarketClerkGui(mClerk);
					mClerk.setGui(mClerkGui);
					*/
					//animation.addGui(mClerkGui);
					
					result.unblock(mClerk);
					
					// get working position
					//mSignInSys.getClerkWorkingPosition(mClerk, mClerk.home);
					
					// add to pausables
					//pausables.add(mClerk); NOT NEEDED ANYMORE
					
					mClerk.startAgent();
					//mClerkGui.setDestination(20, 20);
					
					
					break;
				default:
					//System.err.println("ERROR: Unknown role specified for new Puppet in Market");
					AlertLog.getInstance().logError(AlertTag.MARK, "Market", "ERROR: Unknown role specified for new Puppet in Market");
					break;

				}
			}
		});
	}
	
	
	public class CheckingRequest {	// public for JUnit testing
		public MarketClerkInterface mClerk;	// public for JUnit testing
		public HashMap<String, Integer> items = new HashMap<String, Integer>();	// public for JUnit testing
		
		CheckingRequest(MarketClerkInterface marketClerk, HashMap<String, Integer> customerOrder) {
			mClerk = marketClerk;
			items = customerOrder;
		}
	}
	
	public class RestockingRequest {	// public for JUnit testing
		public HashMap<String, Integer> items = new HashMap<String, Integer>();	// public for JUnit testing
		
		RestockingRequest(HashMap<String, Integer> restockingItems) {
			//items = restockingItems;
			
			// deep copy would be better here
			// Clerk could be killed at any time and his data would be lost
			for (String key : restockingItems.keySet()) {
				items.put(key, restockingItems.get(key));
			}
		}
	}
	
	private class DeliveryRequest {
		//private MarketClerk mClerk;
		private HashMap<String, Integer> items;
		private Bill bill;
		//private boolean paid;
		
		DeliveryRequest(HashMap<String, Integer> customerOrder, AgentInterface billee) {
			items = new HashMap<String, Integer>();
			for (String key : customerOrder.keySet()) {
				items.put(key, customerOrder.get(key));
			}
			bill = new Bill(i++, billee, Market.this);
			//paid = false;
		}
	}
	
	public synchronized void addPublicMoney(double amount) {
		publicMoney += amount;
	}
	/*
	public MarketGui getGui() {
		return gui;
	}
	*/
	/*
	public void setSignInSys(MarketSignInSys s) {
		mSignInSys = s;
	}
	*/
	
	public void msgKillClerk(MarketClerk mClerk) {
		gui.removeGui(mClerk.getGui());
		//mSignInSys.msgRemoveClerk(mClerk);
		
		// remove from pausables
		//pausables.remove(mClerk); NOT NEEDED ANYMORE
		
		mClerk.stopAgent();
	}
	
	public void msgKillCustomer(MarketCustomer mCustomer) {
		gui.removeGui(mCustomer.getGui());
		// remove from pausables
		//pausables.remove(mCustomer); NOT NEEDED ANYMORE
		
		mCustomer.stopAgent();
	}
	
	// data
	private long currentTime;	// may not need
	
	public List<CheckingRequest> checkingRequests	// public for JUnit testing
	= new ArrayList<CheckingRequest>();
	
	public List<RestockingRequest> restockingRequests	// public for JUnit testing
	= new ArrayList<RestockingRequest>();
	
	private List<DeliveryRequest> deliveryRequests
	= new ArrayList<DeliveryRequest>();
	
	private List<DeliveryRequest> deliveryTasks
	= new ArrayList<DeliveryRequest>();
	
	private List<Bill> unpaidBills
	= new ArrayList<Bill>();
	
	double publicMoney = 0;
	
	Random random = new Random();
	public HashMap<String, Integer> stock = new HashMap<String, Integer>();	// public for JUnit testing
	{
		/*
		stock.put("Steak", 2);
		stock.put("Chicken", 3);
		stock.put("Salad", 3);
		stock.put("Pizza", 1);
		*/
		stock.put("Car", 50);
		
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			stock.put(Food.allFoods[i].name, random.nextInt(50) + 50);
		}
		
		//System.out.println(stock);
		AlertLog.getInstance().logInfo(AlertTag.MARK, getAgentName(), "initialized stock");
	}
	
	public void msgIncrementFoodInventory(final int foodIndex) {
		enqueMutation(new Mutation() {
			public void apply() {
				int temp = stock.get(Market.Food.getFood(foodIndex).name) + 1;
				stock.put(Market.Food.getFood(foodIndex).name, temp);
				
			}
		});
	}
	
	public HashMap<String, Integer> hm = new HashMap<String, Integer>();	// for JUnit testing only
	
	MarketSignInSys mSignInSys;
	
	//private static MarketGui gui;
	
	int i = 0;	// bill ID
	public HashMap<String, Integer> temp = new HashMap<String, Integer>();	// for JUnit testing only
	
	/*
	timeSlot1 = 0;
	timeSlot2 = 0;
	*/
	private int[] shifts = new int[Building.getNumberOfShifts()];
	/*
	// in java all elements are initialized to 0 by default
	// so no need to initialize
	{
		for (int i = 0; i < shifts.length; i++) {
			shifts[i] = 4;
		}
	}
	*/
	//private int shiftIndex = 0;
	
//	public HashMap<String, Double> priceList = new HashMap<String, Double>();
//	{
//		/*
//		priceList.put("Steak", 16.00);
//		priceList.put("Chicken", 11.00);
//		priceList.put("Salad", 6.00);
//		priceList.put("Pizza", 9.00);
//		*/
//		priceList.put("Car", 999.99);
//
//		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
//			priceList.put(Food.allFoods[i].name, Food.allFoods[i].marketPrice);
//		}
//		
//		System.out.println(priceList);
//	}
	// initialize priceList of the MarketInterface
	{
		priceList.put("Car", 999.99);

		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			priceList.put(Food.allFoods[i].name, Food.allFoods[i].marketPrice);
		}
		
		//System.out.println(priceList);
		AlertLog.getInstance().logInfo(AlertTag.MARK, getAgentName(), "initialized priceList");
	}
		
	public Truck mTruck;
	/*
	boolean truckArrived = false;
	boolean truckAvailable = true;
	*/
	
	
	public enum TruckState
	{Available, InJourney, ArrivedAtDestination};
	public TruckState truckState = TruckState.Available;//The start state	// public for JUnit testing
	
	Timer timer = new Timer();
	
	// messages
	public void msgCheckInStock(final MarketClerkInterface mClerk, final HashMap<String, Integer> customerOrder) {
		enqueMutation(new Mutation(){
			public void apply(){
				checkingRequests.add(new CheckingRequest(mClerk, customerOrder));
			}
		});
	}
	
	public void msgItemRestock(final HashMap<String, Integer> restockingItems) {
		enqueMutation(new Mutation(){
			public void apply(){
				restockingRequests.add(new RestockingRequest(restockingItems));
			}
		});
	}
	
	public void msgRestaurantFoodRequest(final String foodChoice, final int quantity, final Agent billee) {
		enqueMutation(new Mutation(){
			public void apply(){
				// TODO
				// create a bill in delivery request
				
				temp.put(foodChoice, quantity);
				deliveryRequests.add(new DeliveryRequest(temp, (AgentInterface) billee));
				
			}
		});
	}
	public void msgRestaurantPayingBill(final int billID, final double amountPaid) {
		enqueMutation(new Mutation(){
			public void apply(){
				// TODO
				for (Bill bill : unpaidBills) {
					if (bill.ID == billID) {
						
						// add to public money
						Market.this.publicMoney += amountPaid;
						// remove it from unpaidBills
						unpaidBills.remove(bill);
					}
				}
			}
		});
	}
	
	
	public void msgTruckArrivedDestination() {
		enqueMutation(new Mutation(){
			public void apply(){
				truckState = TruckState.ArrivedAtDestination;
			}
		});
	}
	
	public void msgTruckArrivedHome() {
		enqueMutation(new Mutation(){
			public void apply(){
				truckState = TruckState.Available;
			}
		});
	}
	
	
	// scheduler
	@Override
	public boolean pickAndExecuteAction() {	// public for JUnit testing
		// TODO handle when myClosedState == ClosedState.needToForceClose
		// (this happens when the Building method msgForceClose() or msgOpenForBusiness() gets called)
		if (myClosedState == ClosedState.needToForceClose) {
			closeMarket();
			return true;
		}
		
		if (!checkingRequests.isEmpty()) {
			ProcessCheckingRequest(checkingRequests.get(0));
		}
		
		if (!restockingRequests.isEmpty()) {
			ProcessRestockingRequest(restockingRequests.get(0));
		}
		
		if (truckState == TruckState.ArrivedAtDestination) {
			//truckState = TruckState.Returning;
			FulfillDeliveryTask(deliveryTasks.get(0));
		}
		
		
		if (!deliveryRequests.isEmpty()) {
			ProcessDeliveryRequest(deliveryRequests.get(0));
		}
		
		if (!deliveryTasks.isEmpty() && truckState == truckState.Available) {
			//truckState = truckState.TravelingToDestination;
			SendTruck(deliveryTasks.get(0));
		}
		return false;
	}
	
	// action
	private void ProcessCheckingRequest(CheckingRequest request) {
		//Do("waiters size " + waiters.size());
		//Do("index = " + index);
		//System.out.println(stock);
		//System.out.println("process checking request");
		
		// update items in request based on availability
		// and update stock
		for (String key : request.items.keySet()) {
			// check if stock contains all items
			if (stock.keySet().contains(key)) {
				if (request.items.get(key) <= stock.get(key)) {
					// update stock
					stock.put(key, stock.get(key) - request.items.get(key));
				} else {
					
					// update items
					request.items.put(key, stock.get(key));
					// update stock
					stock.put(key,0);
					
					// concurrent modification error
					/*
					if (stock.get(key) != 0) {
						// update items
						request.items.put(key, stock.get(key));
						// update stock
						stock.put(key,0);
					}
					else {	// erase 0 items
						// update items
						request.items.remove(key);
					}
					*/
				}
			}
			else {
				// update items
				//System.err.println("unknow item in the shopping list");
				AlertLog.getInstance().logError(AlertTag.MARK, "Market", "unknow item in the shopping list");
				//request.items.remove(key);
			}
		}
		
		request.mClerk.msgItemAvailability(request.items);
		
		hm = request.items;	// for JUnit testing only
		
		checkingRequests.remove(request);
	}
	
	private void ProcessRestockingRequest(RestockingRequest request) {
		// update stock based on request
		//System.out.println(stock);
		//System.out.println(request.items);
		for (String key : request.items.keySet()) {
			stock.put(key, stock.get(key) + request.items.get(key));
		}
		
		restockingRequests.remove(request);
	}
	
	
	
	
	
	private void ProcessDeliveryRequest(DeliveryRequest request) {
		
		// update items in request based on availability
		// and update stock
		for (String key : request.items.keySet()) {
			if (request.items.get(key) <= stock.get(key)) {
				// update stock
				stock.put(key, stock.get(key) - request.items.get(key));
				
				// if instanceOf deliveryRequest.bill.billee == Restaurant
				((RestaurantInterface) request.bill.billee).msgMarketResponse(Food.getFood(key).index, request.items.get(key));
				
				
				// add to unpaideBills
				unpaidBills.add(request.bill.copy());
				
			} else {
				// update items
				request.items.put(key, stock.get(key));
				
				((RestaurantInterface) request.bill.billee).msgMarketResponse(Food.getFood(key).index, stock.get(key));
				
				if (stock.get(key) != 0) {
					// add to unpaidBills
					unpaidBills.add(request.bill.copy());
					
					// update stock
					stock.put(key,0);
				}
				
				
			}
			
		}
		
		int temp = 0;
		for (String key : request.items.keySet()) {
			temp += request.items.get(key);
		}
		
		if (temp != 0) {
			deliveryTasks.add(request);
		}
		
		deliveryRequests.remove(request);
		//request.mClerk.msgItemAvailability(request.items);
	}
	

	private void SendTruck(DeliveryRequest request) {
		// if instanceOf request.bill.billee == Restaurant
		//mTruck.msgGoToRestaurant((Restaurant) request.bill.billee);
		truckState = TruckState.InJourney;
		timer.schedule(new TimerTask() {
            public void run(){
            	Market.this.msgTruckArrivedDestination();
            }
	    },
	    100000);
	}
	
	
	private void FulfillDeliveryTask(DeliveryRequest request) {
		// check if the building is open
		BlockingData<Boolean> closed = new BlockingData<Boolean>();
		((BuildingInterface) request.bill.billee).msgAskIfBuildingIsClosed(closed);
		if (!closed.get()) {
			for (String key : request.items.keySet()) {
				((RestaurantInterface) request.bill.billee).msgMarketSentFood(Food.getFood(key).index, request.items.get(key));
				for (String k : request.items.keySet()) {
					request.bill.addItem(k, priceList.get(k));
				}
				
				((RestaurantInterface) request.bill.billee).msgMarketSentBill(request.bill.copy());
			}
			
			//mTruck.msgReturn(true);
		}
		else {
			// move the request to the end of the queue
			deliveryTasks.remove(0);
			deliveryTasks.add(request);
			
			//mTruck.msgReturn(false);
		}
		
		timer.schedule(new TimerTask() {
            public void run(){
            	Market.this.msgTruckArrivedDestination();
            }
	    },
	    100000);
		
	}
	
	@Override
	public void msgFillAnyOpening(
			final BlockingData<Integer> timeSlotIndex,
			final BlockingData<PuppetType> jobType) {
				enqueMutation(new Mutation() {
					public void apply() {
						/*
						if (timeSlot1 == 4 && timeSlot2 == 4) {
							timeSlotIndex.unblock(null);
							jobType.unblock(null);
						}
						else {
							if (timeSlot1 > timeSlot2) {
								timeSlot2++;
								timeSlotIndex.unblock(0);
								jobType.unblock(PuppetType.markClerk);
							} else {
								timeSlot1++;
								timeSlotIndex.unblock(1);
								jobType.unblock(PuppetType.markClerk);
							}
							
						}
						*/
						/*
						if (shifts[shiftIndex] != 4) {
							timeSlotIndex.unblock(shiftIndex);
							jobType.unblock(PuppetType.markClerk);
							shifts[shiftIndex]++;
							shiftIndex++;
							if (shiftIndex == Building.getNumberOfShifts()) {
								shiftIndex = 0;
							}
						}
						else {
							timeSlotIndex.unblock(null);
							jobType.unblock(null);
						}
						*/
						// find the min
						int min = shifts[0];
						int shiftIndex = 0;
						for (int i = 0; i < Building.getNumberOfShifts(); i++) {
							if (shifts[i] < min) {
								min = shifts[i];
								shiftIndex = i;
							}
						}
						
						if (shifts[shiftIndex] != 4) {
							timeSlotIndex.unblock(shiftIndex);
							jobType.unblock(PuppetType.markClerk);
							shifts[shiftIndex]++;
						}
						else {
							timeSlotIndex.unblock(null);
							jobType.unblock(null);
						}
					}
				});
				
	}
	
	@Override
	public void msgFillSpecifiedOpening(BlockingData<Integer> timeSlotIndex, PuppetType jobType) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void msgUpdateTime(long time) {
		// TODO Auto-generated method stub
		if ( ((time/Broadcaster.DAY_HOURS)%Broadcaster.WEEK_DAYS!=5) && ((time/Broadcaster.DAY_HOURS)%Broadcaster.WEEK_DAYS!=6) && (myClosedState != ClosedState.forceClosed) ) {
			// It is not the weekend, and we are not force-closed, so possibly we should open for business
			// Now check each shift: is it time to start? Is it time to end?
			for (int shiftIndex = 0; shiftIndex < Building.getNumberOfShifts(); shiftIndex++) {
				if (time % Broadcaster.DAY_HOURS == Building.getStartHour(shiftIndex)) {
					// Only start this shift if we have at least the minimum workers needed
					if (shifts[shiftIndex] != 0) {
						myClosedState = ClosedState.open;
					}
				}
				if (time % Broadcaster.DAY_HOURS == Building.getEndHour(shiftIndex)) {
					//myClosedState = ClosedState.closed;
					closeMarket();
				}
			}
		}
		
	}
	
	
	private void closeMarket(){
		if (myClosedState == ClosedState.needToForceClose){
			myClosedState = ClosedState.forceClosed;
			//System.out.println("Market is being forced to shut down");
			AlertLog.getInstance().logInfo(AlertTag.MARK, "Market", "Market is being forced to shut down");

		}
		else {
			myClosedState = ClosedState.closed;
			//System.out.println("Market is going to close");
			AlertLog.getInstance().logInfo(AlertTag.MARK, "Market", "Market is going to close");
		}
		mSignInSys.msgMarketClosed();
	}


	@Override
	public void msgClearAllOpenings(){
		// TODO Auto-generated method stub
		/*
		timeSlot1 = 0;
		timeSlot2 = 0;
		*/
		
		for (int i = 0; i < shifts.length; i++) {
			shifts[i] = 0;
		}
	}


	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}

	
	
}
