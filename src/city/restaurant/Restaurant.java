package city.restaurant;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.CookPuppetInterface;
import UnitTestingCommon.interfaces.HostPuppetInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import UnitTestingCommon.interfaces.RegularWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCashierPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import UnitTestingCommon.interfaces.RestaurantParticipantPuppetInterface;
import astar.GridCell;
import astar.MovementManager;
import city.Bill;
import city.Building;
import city.Puppet;
import city.Broadcaster;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.bank.Bank;
import city.gui.ZoomedPanel;
import city.market.Market;
import city.market.Market.Food;
import city.restaurant.gui.CookGui;
import city.restaurant.gui.CookSocketGui;
import city.restaurant.gui.HostGui;
import city.restaurant.gui.RestaurantCashierGui;
import city.restaurant.gui.RestaurantCustomerGui;
import city.restaurant.gui.RestaurantGui;
import city.restaurant.gui.RestaurantParticipantGui;
import city.restaurant.gui.WaiterGui;


public class Restaurant extends Building implements RestaurantInterface {
	
	// PARAMETERS
	
	public static final double workerWage = 30.0;
	
	public static final GridCell locCustomerEntry = new GridCell(1, 1);
	public static final GridCell locEmployeeEntry = new GridCell(28, 28);
	public static final GridCell locExit = new GridCell(23, 1);
	public static final GridCell locFrontOfLine = new GridCell(2, 26);
	public static final GridCell locHost = new GridCell(4, 27);
	public static final GridCell locCook = new GridCell(27, 15);
	public static final GridCell locCashier = new GridCell(23, 5);
	public static final GridCell locWaiterBase = new GridCell(19, 26);
	public static final GridCell offsetCustomerFromTable = new GridCell(0, -1);
	public static final GridCell offsetWaiterFromTable = new GridCell(1, -1);
	public static final GridCell offsetCustomerFromHost = new GridCell(0, -1);
	public static final GridCell offsetWaiterFromHost = new GridCell(0, -2);
	public static final GridCell offsetFromCook = new GridCell(-2, 0);
	public static final GridCell offsetFromCashier = new GridCell(-1, 0);
	public static final GridCell offsetCookingAreaOriginFromCook = new GridCell(1, 0);
	public static final GridCell offsetPlatingAreaOriginFromCook = new GridCell(-1, 0);
	// When a waiter is idle, he goes to locWaiterBase + a random displacement.
	// The random displacement (in cells) is bound by these following two parameters.
	// E.g. if allowableWaiterDisplacementFromBaseRow = 3, then waiter will go to either (r+0,c), (r+1,c), (r-1,c), or (r+2,c)
	public static final int allowableWaiterDisplacementFromBaseRow = 8;
	public static final int allowableWaiterDisplacementFromBaseCol = 2;
	
	public static final int minutesForImpatientCustomer = 20;
	public static final int minutesForPatientCustomer = 80;
	
	public static final double cashierCashLowThreshold = 50.0;
	public static final double cashierCashResupplyAmount = 200.0;
	
	public static final double priceIncreaseFactorOnMarketPrice = 2.0;
	public static final double chanceThatRestaurantCarriesFood = 0.4;
	// Initial market food stock = ceiling( restaurantStockQuantityMultiplier/food.restaurantPrice plus-or-minus (restaurantStockQuantityPercentRandomness)% )
	public static final double restaurantStockQuantityMultiplier = 100.0;
	public static final double restaurantStockQuantityPercentRandomness = 0.2;
	
	public static final int tableWidth = 2;
	public static final int tableHeight = 2;
	// Private so that it can't be edited (must be accessed through a public method, defined below)
	private static final GridCell[] tableMap = {
		new GridCell(7, 4),
		new GridCell(7, 10),
		new GridCell(7, 16),
		new GridCell(7, 22),
		new GridCell(12, 4),
		new GridCell(12, 10),
		new GridCell(12, 16),
		new GridCell(12, 22),
		new GridCell(17, 4),
		new GridCell(17, 10),
		new GridCell(17, 16),
		new GridCell(17, 22)
	};
	/*
	// Private so that it can't be edited (must be accessed through a public method, defined below)
	private static final Food[] foods = {
		// Food(String name, int startQuantity, int minutesCookingTime, double price, int lowQuantityThreshold, int resupplyQuantity)
		new Food("Garlic", 4, 10, 1.0, 5, 5),
		new Food("Monkey Brains", 2, 50, 30.0, 1, 3),
		new Food("Llama", 10, 25, 4.0, 6, 10),
		new Food("Moose", 8, 40, 6.0, 5, 8),
		new Food("Pickle", 8, 40, 6.0, 5, 8),
		new Food("Chocolate Milk", 2, 10, 1.50, 5, 10),
		new Food("Pizza", 2, 50, 6, 5, 10),
		new Food("Dog", 3, 60, 6, 3, 5)
	};*/
	
	
	
	
	
	
	
	
	// DATA
	
	private final Bank cityBank;
	private final Market[] cityMarkets;
	
	// Socket data members are temporarily public for debugging purposes
	public final HostSocket hostSocket;
	public final CookSocket cookSocket;
	public final RestaurantCashierSocket cashierSocket;
	private final ArrayList<MyWorker> workers;
	
	private int[] inventory;
	private final ExtraFoodInfo[] extraFoodInfo;
	private HashMap<String,Double> menu;
	private final HashMap<String,Double> priceSheet;
	private final RevolvingStandMonitor stand;
	
	private boolean noMoneyInBank;
	private boolean pendingBankAction;
	private boolean needToEndShift;
	private boolean inZoomedPanelDisplay;
	private double cashOnHand;
	private double cashNeededByCashier;
	private final ArrayList<Bill> marketBills;
	private final HashMap<String,MyCookOrder> foodOrders; // String key is the foodChoice name
	private final ArrayList<WorkerSlot> workerSlots;
	
	
	
	
	
	// ENUMS AND CLASSES
	
	private enum WorkerState { onShift, waiterToPay, waiterOnTheWayOut, waitingForOldWaitersToLeave, otherToPay, otherOnTheWayOut };
	private class MyCookOrder {
		public final int foodIndex;
		public int quantityStillToOrder;
		public int quantityEnRoute;
		public int quantityReceived;
		public HashSet<Market> marketsAlreadyAsked;
		public boolean pendingMarketResponse;
		public boolean allMarketsTried;
		public MyCookOrder(int foodIndex, int quantity) {
			this.foodIndex = foodIndex;
			quantityStillToOrder = quantity;
			quantityEnRoute = 0;
			quantityReceived = 0;
			marketsAlreadyAsked = new HashSet<Market>();
			pendingMarketResponse = false;
			allMarketsTried = false;
		}
	}
	public static class ExtraFoodInfo {
		public final double price;
		public final int quantityInitial;
		public final int quantityLowThreshold;
		public final int quantityToResupply;
		public ExtraFoodInfo(int foodIndex) {
			price = priceIncreaseFactorOnMarketPrice*Food.getFood(foodIndex).marketPrice;
			
			// Randomly choose whether the restaurant will carry this item or not
			if (Math.random() <= Restaurant.chanceThatRestaurantCarriesFood) {
				// Calculate quantityInitial from price
				double result = restaurantStockQuantityMultiplier/price;
				double randomDisplacement = restaurantStockQuantityPercentRandomness*Math.random()*result;
				randomDisplacement = 2*randomDisplacement - randomDisplacement; // add the negative part of the range
				result += randomDisplacement;
				result = Math.ceil(result);
				quantityInitial = (int)result;
			}
			else {
				quantityInitial = 0;
			}
			
			quantityLowThreshold = quantityInitial/2;
			quantityToResupply = quantityInitial;
		}
	}
	private static class WorkerSlot {
		public final Puppet.PuppetType type;
		public final int shift;
		public boolean filled;
		public WorkerSlot(Puppet.PuppetType type, int shift) {
			this.type = type;
			this.shift = shift;
			filled = false;
		}
	}
	private static class MyWorker {
		public final RestaurantWorkerPuppet worker;
		public WorkerState state;
		public MyWorker(RestaurantWorkerPuppet w) {
			worker = w;
			state = WorkerState.onShift;
		}
	}
	
	
	
	
	
	
	// BASIC UTITLITIES
	
	public static GridCell getLocOfTable(int tableID) {
		return tableMap[tableID];
	}/*
	public static ExtraFoodInfo getFoodInfo(int foodIndex) {
		for (ExtraFoodInfo fi : extraFoodInfo) {
			if (mf.name.equals(name)) {
				return f;
			}
		}
		return null;
	}*/
	public static String getAbbreviation(String input) {
		String abb = "";
		String[] words = input.split(" ");
		for (String word : words) {
			abb += word.charAt(0);
		}
		return abb;
	}
	private MyWorker findMyWorker(RestaurantWorkerPuppet w) {
		for (MyWorker mw : workers) {
			if (mw.worker == w) {
				return mw;
			}
		}
		return null;
	}
	private MyWorker findMyWorker(WorkerState s) {
		for (MyWorker mw : workers) {
			if (mw.state == s) {
				return mw;
			}
		}
		return null;
	}
	private MyWorker findMyWorker(Puppet.PuppetType type) {
		for (MyWorker mw : workers) {
			if (type == Puppet.PuppetType.restHost && mw.worker instanceof HostPuppetInterface) {
				return mw;
			}
			if (type == Puppet.PuppetType.restCook && mw.worker instanceof CookPuppetInterface) {
				return mw;
			}
			if (type == Puppet.PuppetType.restCashier && mw.worker instanceof RestaurantCashierPuppetInterface) {
				return mw;
			}
			if (type == Puppet.PuppetType.restAnyWaiter && mw.worker instanceof AbstractWaiterPuppetInterface) {
				return mw;
			}
			if (type == Puppet.PuppetType.restRegWaiter && mw.worker instanceof RegularWaiterPuppetInterface) {
				return mw;
			}
			if (type == Puppet.PuppetType.restStandWaiter && mw.worker instanceof RevolvingStandWaiterPuppet) {
				return mw;
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	// CONSTRUCTOR ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Restaurant(String name,int pr,int pc,int cr,int cc,double startingCash,Bank cityBank,Market[] cityMarkets,ZoomedPanel zp,int tlr,int tlc,int brr,int brc){
		super(name,pr,pc,cr,cc,new MovementManager(name+" MovementManager",insideNumRows,insideNumCols,"restaurantLayout.txt"),tlr,tlc,brr,brc,new RestaurantGui(zp));
		
		
		this.cityBank=cityBank;
		this.cityMarkets=cityMarkets;
		stand = new RevolvingStandMonitor();
		
		inventory = new int[Food.getNumberOfFoods()];
		extraFoodInfo = new ExtraFoodInfo[Food.getNumberOfFoods()];
		menu = new HashMap<String,Double>();
		priceSheet = new HashMap<String,Double>();
		
		// Add foods to extraFoodInfo, menu, priceSheet, and cook
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			extraFoodInfo[i] = new ExtraFoodInfo(i);
			inventory[i] = extraFoodInfo[i].quantityInitial;
			if (extraFoodInfo[i].quantityInitial > 0) {
				menu.put(Food.getFood(i).name, extraFoodInfo[i].price);
				priceSheet.put(Food.getFood(i).name, extraFoodInfo[i].price);
			}
		}
		
		hostSocket = new HostSocket(this.getAgentName() + "HostSocket", this.getMovementManager(), this, tableMap.length);
		cookSocket = new CookSocket(this.getAgentName() + "CookSocket", this, stand);
		cashierSocket = new RestaurantCashierSocket(this.getAgentName() + "CashierSocket", this, priceSheet);
		
		workers = new ArrayList<MyWorker>();
		
		CookSocketGui csGui = new CookSocketGui();
		cookSocket.setGui(csGui);
		gui.addGui(csGui);
		
		hostSocket.startAgent();
		cookSocket.startAgent();
		cashierSocket.startAgent();
		
		// Add foods to cook's food list
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			cookSocket.msgAddFood(i, extraFoodInfo[i].quantityInitial, extraFoodInfo[i].quantityLowThreshold, extraFoodInfo[i].quantityToResupply,
						Food.getFood(i).minutesCookingTime, extraFoodInfo[i].price, extraFoodInfo[i].quantityInitial);
		}
		
		noMoneyInBank = false;
		pendingBankAction = false;
		needToEndShift = false;
		inZoomedPanelDisplay = false;
		cashOnHand = startingCash;
		cashNeededByCashier = 0.0;
		marketBills = new ArrayList<Bill>();
		foodOrders = new HashMap<String,MyCookOrder>();
		workerSlots = new ArrayList<WorkerSlot>();
		
		// Add 4 minimum needed job slots for shift 0, 4 minimum needed job slots for shift 1, ...
		for (int shiftIndex = 0; shiftIndex < Building.getNumberOfShifts(); shiftIndex++) {
			workerSlots.add(new WorkerSlot(Puppet.PuppetType.restCashier, shiftIndex));
			workerSlots.add(new WorkerSlot(Puppet.PuppetType.restHost, shiftIndex));
			workerSlots.add(new WorkerSlot(Puppet.PuppetType.restAnyWaiter, shiftIndex));
			workerSlots.add(new WorkerSlot(Puppet.PuppetType.restCook, shiftIndex));
		}
		// Add one waiter slot for each shift, repeat for a total of 8 additional waiters
		for (int a = 1; a <= 8; a++) {
			for (int shiftIndex = 0; shiftIndex < Building.getNumberOfShifts(); shiftIndex++) {
				workerSlots.add(new WorkerSlot(Puppet.PuppetType.restAnyWaiter, shiftIndex));
			}
		}
		
	}
	
	
	@Override
	protected void destructor() {
		// do nothing
	}

	
	
	
	
	
	
	
	
	// MESSAGES
	
	@Override
	public void msgFillAnyOpening(final BlockingData<Integer> timeSlotIndex, final BlockingData<PuppetType> jobType) {
		enqueMutation(new Mutation() {
			public void apply() {
				// Find a free worker slot
				int slotIndex;
				for (slotIndex = 0; slotIndex < workerSlots.size(); slotIndex++) {
					if (! workerSlots.get(slotIndex).filled) {
						break;
					}
				}
				if (slotIndex == workerSlots.size()) {
					// No free slot was found
					timeSlotIndex.unblock(null);
					jobType.unblock(null);
				}
				else {
					// The slot currently indexed by slotIndex is the one to fill
					WorkerSlot slot = workerSlots.get(slotIndex);
					slot.filled = true;
					timeSlotIndex.unblock(slot.shift);
					jobType.unblock(slot.type);
				}
			}
		});
	}
	@Override
	public void msgFillSpecifiedOpening(final BlockingData<Integer> timeSlotIndex, final Puppet.PuppetType jobType) {
		enqueMutation(new Mutation() {
			public void apply() {
				// Find a free worker slot of type jobType
				PuppetType jobTypeCategory = jobType;
				if (jobType == PuppetType.restRegWaiter || jobType == PuppetType.restStandWaiter) {
					jobTypeCategory = PuppetType.restAnyWaiter;
				}
				int slotIndex;
				for (slotIndex = 0; slotIndex < workerSlots.size(); slotIndex++) {
					if ( (workerSlots.get(slotIndex).type == jobTypeCategory) && (!workerSlots.get(slotIndex).filled) ) {
						break;
					}
				}
				if (slotIndex == workerSlots.size()) {
					// No free slot was found
					timeSlotIndex.unblock(null);
				}
				else {
					// The slot currently indexed by slotIndex is the one to fill
					WorkerSlot slot = workerSlots.get(slotIndex);
					slot.filled = true;
					timeSlotIndex.unblock(slot.shift);
				}
			}
		});
	}
	@Override
	public void msgClearAllOpenings() {
		enqueMutation(new Mutation() {
			public void apply() {
				for (WorkerSlot ws : workerSlots) {
					ws.filled = false;
				}
			}
		});
	}
	@Override
	public void msgSpawnPuppet(final BlockingData<PuppetInterface> result, final String name, final Puppet.Setup setupPackage) {
		enqueMutation(new Mutation() {
			public void apply() {
				if (myClosedState == ClosedState.closed || myClosedState == ClosedState.forceClosed) {
					AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Closed -- denying entrance to " + setupPackage.role + " puppet");
					result.unblock(null);
					return;
				}
				RestaurantParticipantPuppet.Setup customSetup = new RestaurantParticipantPuppet.Setup();
				setupPackage.copyTo(customSetup);
				customSetup.copyData(name, Restaurant.this.getMovementManager(), Restaurant.this, hostSocket, cookSocket, cashierSocket);
				switch (setupPackage.role) {
					/* 7 tasks for each Puppet:
					 * 		1. Instantiate
					 * 		2. Add to movement manager
					 * 		3. Instantiate and add gui
					 * 		4. Add Puppet gui to gui panel
					 * 		5. If worker, add to workers list
					 * 		6. Return Puppet pointer to its Person master
					 * 		7. Start Puppet thread
					 */
					case customer:
						
						//flake, impatient, picky, [foodName]
						customSetup.isFlake = name.toLowerCase().contains("flake");
						customSetup.minutesWaitingPatience = (name.toLowerCase().contains("impatient")) ? minutesForImpatientCustomer : minutesForPatientCustomer;
						customSetup.foodPreferenceIsStrong = name.toLowerCase().contains("picky");
						
						RestaurantCustomerPuppet customer = new RestaurantCustomerPuppet(customSetup);					
						Restaurant.this.getMovementManager().msgAddUnit(customer,1,1,locCustomerEntry.r,locCustomerEntry.c);
						RestaurantParticipantGui customerGui = new RestaurantCustomerGui(customer, locCustomerEntry); 
						customer.setGui(customerGui);
						gui.addGui(customerGui);
						result.unblock(customer);
						customer.startAgent();
						break;
					case restHost:
						HostPuppet host = new HostPuppet(customSetup, hostSocket);
						Restaurant.this.getMovementManager().msgAddUnit(host,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
						RestaurantParticipantGui hostGui = new HostGui(host, locEmployeeEntry);
						host.setGui(hostGui);
						gui.addGui(hostGui);
						workers.add(new MyWorker(host));
						result.unblock(host);
						host.startAgent();
						break;
					case restRegWaiter:
						RegularWaiterPuppet rwaiter = new RegularWaiterPuppet(customSetup, menu);
						Restaurant.this.getMovementManager().msgAddUnit(rwaiter,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
						RestaurantParticipantGui rwaiterGui = new WaiterGui(rwaiter, locEmployeeEntry, true); 
						rwaiter.setGui(rwaiterGui);
						gui.addGui(rwaiterGui);
						workers.add(new MyWorker(rwaiter));
						result.unblock(rwaiter);
						rwaiter.startAgent();
						break;
					case restStandWaiter:
						RevolvingStandWaiterPuppet pcwaiter = new RevolvingStandWaiterPuppet(customSetup, menu, stand);
						Restaurant.this.getMovementManager().msgAddUnit(pcwaiter,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
						RestaurantParticipantGui pcwaiterGui = new WaiterGui(pcwaiter, locEmployeeEntry, false); 
						pcwaiter.setGui(pcwaiterGui);
						gui.addGui(pcwaiterGui);
						workers.add(new MyWorker(pcwaiter));
						result.unblock(pcwaiter);
						pcwaiter.startAgent();
						break;
					case restAnyWaiter:
						// Randomly choose a regular waiter or a revolving stand waiter
						if (Math.random() > 0.5) {
							// Choose a RevlovingStandWaiterAgent
							RevolvingStandWaiterPuppet pcwaiter2 = new RevolvingStandWaiterPuppet(customSetup, menu, stand);
							Restaurant.this.getMovementManager().msgAddUnit(pcwaiter2,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
							RestaurantParticipantGui pcwaiterGui2 = new WaiterGui(pcwaiter2, locEmployeeEntry, false); 
							pcwaiter2.setGui(pcwaiterGui2);
							gui.addGui(pcwaiterGui2);
							workers.add(new MyWorker(pcwaiter2));
							result.unblock(pcwaiter2);
							pcwaiter2.startAgent();
						}
						else {
							// Choose a RegularWaiterAgent
							RegularWaiterPuppet rwaiter2 = new RegularWaiterPuppet(customSetup, menu);
							Restaurant.this.getMovementManager().msgAddUnit(rwaiter2,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
							RestaurantParticipantGui rwaiterGui2 = new WaiterGui(rwaiter2, locEmployeeEntry, true); 
							rwaiter2.setGui(rwaiterGui2);
							gui.addGui(rwaiterGui2);
							workers.add(new MyWorker(rwaiter2));
							result.unblock(rwaiter2);
							rwaiter2.startAgent();
						}
						break;
					case restCook:					
						CookPuppet cook = new CookPuppet(customSetup, cookSocket);
						Restaurant.this.getMovementManager().msgAddUnit(cook,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
						RestaurantParticipantGui cookGui = new CookGui(cook, locEmployeeEntry);
						cook.setGui(cookGui);
						gui.addGui(cookGui);
						MyWorker oldCook = Restaurant.this.findMyWorker(Puppet.PuppetType.restCook);
						if (oldCook != null) {
							oldCook.state = WorkerState.otherToPay;
						}
						workers.add(new MyWorker(cook));
						result.unblock(cook);
						cook.startAgent();
						break;
					case restCashier:
						RestaurantCashierPuppet cashier = new RestaurantCashierPuppet(customSetup, cashierSocket);
						Restaurant.this.getMovementManager().msgAddUnit(cashier,1,1,locEmployeeEntry.r,locEmployeeEntry.c);
						RestaurantParticipantGui cashierGui = new RestaurantCashierGui(cashier, locEmployeeEntry);
						cashier.setGui(cashierGui);
						gui.addGui(cashierGui);
						MyWorker oldCashier = Restaurant.this.findMyWorker(Puppet.PuppetType.restCashier);
						if (oldCashier != null) {
							oldCashier.state = WorkerState.otherToPay;
						}
						workers.add(new MyWorker(cashier));
						result.unblock(cashier);
						cashier.startAgent();
						break;
					default:
						AlertLog.getInstance().logError(AlertTag.REST, getAgentName(), "Unknown role specified for new Puppet in Restaurant");
						break;
				}			
			}
		});
	}
	@Override
	public void msgUpdateTime(final long time) {
		enqueMutation(new Mutation() {
			public void apply() {
				if (((time/Broadcaster.DAY_HOURS)%Broadcaster.WEEK_DAYS!=5) && ((time/Broadcaster.DAY_HOURS)%Broadcaster.WEEK_DAYS!=6) && (myClosedState != ClosedState.forceClosed) ) {
					// It is not the weekend, and we are not force-closed, so possibly we should open for business
					// Now check each shift: is it time to start? Is it time to end?
					for (int shiftIndex = 0; shiftIndex < Building.getNumberOfShifts(); shiftIndex++) {
						if (time % Broadcaster.DAY_HOURS == Building.getStartHour(shiftIndex)) {
							// Only start this shift if we have at least the minimum workers needed
							int fi = 4*shiftIndex;	// fi = first index of minimum needed worker slots for this shift
							if (workerSlots.get(fi+0).filled && workerSlots.get(fi+1).filled && workerSlots.get(fi+2).filled && workerSlots.get(fi+3).filled) {
								myClosedState = ClosedState.open;
							}
						}
						if (time % Broadcaster.DAY_HOURS == Building.getEndHour(shiftIndex)) {
							myClosedState = ClosedState.closed;
							needToEndShift = true;
						}
					}
				}
			}
		});
	}
	public void msgParticipantLeft(final RestaurantParticipantPuppetInterface p) {
		enqueMutation(new Mutation() {
			public void apply() {
				gui.removeGui(p.getGui());
				if (p instanceof RestaurantWorkerPuppet) {
					workers.remove(Restaurant.this.findMyWorker((RestaurantWorkerPuppet)p));
				}
			}
		});
	}
	public void msgCashierNeedsCash(final double amount) {
		enqueMutation(new Mutation() {
			public void apply() {
				cashNeededByCashier = amount;
			}
		});
	}
	public void msgCashierGivesCash(final double amount) {
		enqueMutation(new Mutation() {
			public void apply() {
				cashOnHand += amount;
			}
		});
	}
	public void msgBankAccountRanOut() {
		enqueMutation(new Mutation() {
			public void apply() {
				noMoneyInBank = true;
			}
		});
	}
	public void msgBankSentMoney(final double amount) {
		enqueMutation(new Mutation() {
			public void apply() {
				AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Received $" + amount + " from bank");
				pendingBankAction = false;
				cashOnHand += amount;
			}
		});
	}
	public void msgZoomedPanelDisplay() {
		enqueMutation(new Mutation() {
			public void apply() {
				inZoomedPanelDisplay = true;
				((RestaurantGui)gui).updateInventoryDisplay(inventory);
			}
		});
	}
	public void msgZoomedPanelHide() {
		enqueMutation(new Mutation() {
			public void apply() {
				inZoomedPanelDisplay = false;
			}
		});
	}
	public void msgCookUpdatesInventoryAndMenu(final HashMap<String,Double> newMenu, final int[] inventory) {
		enqueMutation(new Mutation() {
			public void apply() {
				menu = newMenu;
				Restaurant.this.inventory = inventory;
				if (inZoomedPanelDisplay) {
					((RestaurantGui)gui).updateInventoryDisplay(inventory);
				}
			}
		});
	}
	public void msgCookNeedsResupply(final int foodIndex, final int quantity) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCookOrder mco = foodOrders.get(Food.getFood(foodIndex));
				ExtraFoodInfo fi = extraFoodInfo[foodIndex];
				AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Received cook request for " + quantity + " of " + Food.getFood(foodIndex).name);
				if (mco != null) {
					mco.quantityStillToOrder += quantity;
				}
				else {
					foodOrders.put(Food.getFood(foodIndex).name, new MyCookOrder(foodIndex, quantity));
				}
			}
		});
	}
	public void msgMarketResponse(final int foodIndex, final int quantityAbleToDeliver) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCookOrder mco = foodOrders.get(Food.getFood(foodIndex));
				mco.quantityStillToOrder -= quantityAbleToDeliver;
				mco.quantityEnRoute += quantityAbleToDeliver;
				mco.pendingMarketResponse = false;
			}
		});
	}
	public void msgMarketSentFood(final int foodIndex, final int quantity) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCookOrder mco = foodOrders.get(Food.getFood(foodIndex));
				mco.quantityEnRoute -= quantity;
				mco.quantityReceived += quantity;
			}
		});
	}
	public void msgMarketSentBill(final Bill b) {
		enqueMutation(new Mutation() {
			public void apply() {
				marketBills.add(b);
			}
		});
	}
	public void msgIncrementFoodInventory(final int foodIndex) {
		enqueMutation(new Mutation() {
			public void apply() {
				cookSocket.msgAddFood(foodIndex, extraFoodInfo[foodIndex].quantityInitial,
						extraFoodInfo[foodIndex].quantityLowThreshold, extraFoodInfo[foodIndex].quantityToResupply,
						Food.getFood(foodIndex).minutesCookingTime, extraFoodInfo[foodIndex].price, 1);
				//cookSocket.msgSupplyDelivered(foodIndex, 1, priceIncreaseFactorOnMarketPrice*Food.getFood(foodIndex).marketPrice);
			}
		});
	}
	

	
	
	
	
	
	
	// ACTIONS
	
	private void actClose(boolean forced) {
		if (forced) {
			myClosedState = ClosedState.forceClosed;
		}
		else {
			needToEndShift = false;
		}
		// Tell all the waiters to leave, then, once they leave, tell the cook and cashier to leave
		for (MyWorker mw : workers) {
			if (mw.worker instanceof AbstractWaiterPuppetInterface) {
				mw.state = WorkerState.waiterToPay;
			}
			else if (mw.worker instanceof HostPuppetInterface) {
				mw.state = WorkerState.otherToPay;
			}
			else {
				mw.state = WorkerState.waitingForOldWaitersToLeave;
			}
		}
	}
	private void actTryToPayWorker(MyWorker mw) {
		if (cashOnHand >= workerWage) {
			// Pay and dismiss the worker
			AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Paying " + mw.worker.getAgentName() + " $" + workerWage);
			mw.worker.msgTakePayAndLeave(workerWage);
			cashOnHand -= workerWage;
			if (mw.worker instanceof AbstractWaiterPuppetInterface) {
				mw.state = WorkerState.waiterOnTheWayOut;
			}
			else {
				mw.state = WorkerState.otherOnTheWayOut;
			}
		}
		else {
			// Ask the bank for the money
			if (cityBank != null) {
				AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Asking bank for $" + workerWage + " for paying a worker");
				pendingBankAction = true;
				cityBank.msgRestaurantWithdrawRequest(this, workerWage);
			}
		}
	}
	private void actTryToGiveCashierCash() {
		if (cashOnHand >= cashNeededByCashier) {
			// Give the money
			cashierSocket.msgRestaurantGotMoneyFromBank(cashNeededByCashier);
			cashOnHand -= cashNeededByCashier;
			cashNeededByCashier = 0;
		}
		else {
			// Ask the bank for the money if there is a bank
			if (cityBank != null) {
				AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Asking bank for $" + cashNeededByCashier + " for the cashier");
				pendingBankAction = true;
				cityBank.msgRestaurantWithdrawRequest(this, cashNeededByCashier);
			}
		}
		
	}
	private void actTryToPayMarketBill(Bill b) {
		if (cashOnHand >= b.getTotal()) {
			// Pay the bill
			AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Paying market bill worth " + b.getTotal());
			((Market)b.biller).msgRestaurantPayingBill(b.ID, b.getTotal());
			cashOnHand -= b.getTotal();
			marketBills.remove(b);
		}
		else {
			// Ask the bank for the money if there is a bank
			if (cityBank != null) {
				AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Asking bank for $" + b.getTotal() + " for paying a market bill");
				pendingBankAction = true;
				cityBank.msgRestaurantWithdrawRequest(this, b.getTotal());
			}
		}
	}
	private void actDepositCashOnHand() {
		AlertLog.getInstance().logInfo(AlertTag.REST, getAgentName(), "Depositing $" + cashOnHand + " in bank");
		noMoneyInBank = false;
		cityBank.msgRestaurantDepositRequest(this, cashOnHand);
		cashOnHand = 0;
	}
	private void actDismissOrder(MyCookOrder mco, boolean orderWasCompleted) {
		if (!orderWasCompleted) {
			// The markets ran out of stock of this item
			cookSocket.msgMarketsOutOfStock(mco.foodIndex);
		}
		foodOrders.remove(mco.foodIndex);
	}
	private void actGiveFoodToCook(MyCookOrder mco) {
		cookSocket.msgAddFood(mco.foodIndex, extraFoodInfo[mco.foodIndex].quantityInitial,
				extraFoodInfo[mco.foodIndex].quantityLowThreshold, extraFoodInfo[mco.foodIndex].quantityToResupply,
				Food.getFood(mco.foodIndex).minutesCookingTime, extraFoodInfo[mco.foodIndex].price, mco.quantityReceived);
		//cookSocket.msgSupplyDelivered(mco.foodIndex, mco.quantityReceived, priceIncreaseFactorOnMarketPrice*Food.getFood(mco.foodIndex).marketPrice);
		mco.quantityReceived = 0;
	}
	private void actAskAnotherMarketForFood(MyCookOrder mco) {
		// Find a market we haven’t asked yet
		Market m = null;
		for (Market temp : cityMarkets) {
			if (!mco.marketsAlreadyAsked.contains(temp)) {
				m = temp;
				break;
			}
		}
		if (m == null) {
			mco.allMarketsTried = true;
		}
		else {
			mco.marketsAlreadyAsked.add(m);
			mco.pendingMarketResponse = true;
			m.msgRestaurantFoodRequest(Food.getFood(mco.foodIndex).name, mco.quantityStillToOrder, this);
		}
	}
	
	// SCHEDULER
	
	@Override
	protected boolean pickAndExecuteAction() {
		MyWorker mw;
		
		
		if (myClosedState == ClosedState.needToForceClose) {
			this.actClose(true);
			return true;
		}
		if (needToEndShift) {
			this.actClose(false);
			return true;
		}
		if ( (!pendingBankAction && !noMoneyInBank) || cityBank == null ) {
			if ( (mw = this.findMyWorker(WorkerState.waiterToPay)) != null) {
				this.actTryToPayWorker(mw);
				return true;
			}
			if ( (mw = this.findMyWorker(WorkerState.otherToPay)) != null) {
				this.actTryToPayWorker(mw);
				return true;
			}
			if (this.findMyWorker(WorkerState.waiterToPay) == null && this.findMyWorker(WorkerState.waiterOnTheWayOut) == null && (mw = this.findMyWorker(WorkerState.waitingForOldWaitersToLeave)) != null) {
				// Once all the old-shift waiters have gone, it is safe to dismiss the old-shift cashier and cook
				this.actTryToPayWorker(mw);
				return true;
			}
			if (cashNeededByCashier > 0) {
				this.actTryToGiveCashierCash();
				return true;
			}
			if (marketBills.size() > 0) {
				this.actTryToPayMarketBill(marketBills.get(0));
				return true;
			}
		}
		if (cityBank != null) {
			if (!pendingBankAction && cashOnHand > 0) {
				this.actDepositCashOnHand();
				return true;
			}
		}
		for (MyCookOrder mco : foodOrders.values()) {
			if (mco.quantityStillToOrder + mco.quantityEnRoute + mco.quantityReceived == 0) {
				this.actDismissOrder(mco, true);
				return true;
			}
		}
		for (MyCookOrder mco : foodOrders.values()) {
			if (!mco.pendingMarketResponse && (mco.quantityEnRoute + mco.quantityReceived == 0) && mco.allMarketsTried) {
				this.actDismissOrder(mco, false);
				return true;
			}
		}
		for (MyCookOrder mco : foodOrders.values()) {
			if (mco.quantityReceived > 0) {
				this.actGiveFoodToCook(mco);
				return true;
			}
		}
		if (cityMarkets != null && cityMarkets.length > 0) {
			for (MyCookOrder mco : foodOrders.values()) {
				if (!mco.pendingMarketResponse && !mco.allMarketsTried && mco.quantityStillToOrder > 0) {
					this.actAskAnotherMarketForFood(mco);
					return true;
				}
			}
		}
		return false;
	}
	

	
	
}