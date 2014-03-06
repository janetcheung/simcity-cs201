package city.restaurant;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.CookSocketInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import UnitTestingCommon.interfaces.SimpleRestaurantWorkerPuppetInterface;
import UnitTestingCommon.interfaces.SimpleRestaurantWorkerSocketInterface;
import trace.AlertLog;
import trace.AlertTag;
import city.Broadcaster;
import city.market.Market.Food;
import city.restaurant.gui.CookSocketGui;


public class CookSocket extends SimpleRestaurantWorkerSocket implements CookSocketInterface {
	

	
	
	// DATA
	
	public static final int minutesAfterWhichCheckRevolvingStand = 5;
	
	
	
	private final RevolvingStandMonitor revolvingStand;
	private CookSocketGui gui;
	private boolean shouldCheckRevolvingStand;
	private final ArrayList<MyOrder> orders;
	private final MyFood[] foods;
	private final Timer timer;
	private boolean needToCheckAllInventory;
	
	
	
	// ENUMS AND CLASSES
	
	private enum OrderState { needsToBeConfirmed, denied, cooking, needsToBeThrownAway, needsToBePlated, waitingToBePickedUp, askedToBeTakenByWaiter };
	
	private static class MyOrder {
		public AbstractWaiterPuppetInterface waiter;
		public RestaurantCustomerPuppetInterface customer;
		public int foodIndex;
		public OrderState state;
		public MyOrder(AbstractWaiterPuppetInterface w, RestaurantCustomerPuppetInterface c, int foodIndex) {
			waiter = w;
			customer = c;
			this.foodIndex = foodIndex;
			state = OrderState.needsToBeConfirmed;
		}
	}
	private static class MyFood {
		public boolean marketsOutOfStock;
		public int quantityInStock;
		public int quantityRequestedFromMarkets;
		public final int quantityLowThreshold;
		public final int quantityToResupply;
		public final int minutesCookingTime;
		public final double price;
		public MyFood(int quantityInitial, int quantityLowThreshold, int quantityToResupply, int minutesCookingTime, double price) {
			marketsOutOfStock = false;
			this.quantityInStock = quantityInitial;
			quantityRequestedFromMarkets = 0;
			this.quantityLowThreshold = quantityLowThreshold;
			this.quantityToResupply = quantityToResupply;
			this.minutesCookingTime = minutesCookingTime;
			this.price = price;
		}
	}
	
	
	
	
	
	// BASIC UTILITY
	
	private MyOrder findMyOrder(RestaurantCustomerPuppetInterface c) {
		for (MyOrder mo : orders) {
			if (mo.customer == c) {
				return mo;
			}
		}
		return null;
	}
	private MyOrder findMyOrder(OrderState s) {
		for (MyOrder mo : orders) {
			if (mo.state == s) {
				return mo;
			}
		}
		return null;
	}
	private HashMap<String,Double> getMenu() {
		HashMap<String,Double> menu = new HashMap<String,Double>();
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (foods[i] != null && foods[i].quantityInStock > 0) {
				menu.put(Food.getFood(i).name, foods[i].price);
			}
		}
		return menu;
	}
	private int[] getInventory() { // TODO
		int[] inv = new int[Food.getNumberOfFoods()];
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (foods[i] == null) {
				inv[i] = 0;
			}
			else {
				inv[i] = foods[i].quantityInStock;
			}
		}
		return inv;
	}
	
	
	
	
	
	// CONSTRUCTOR //////////////////////////////////////////////////////////////////////////////////////////////////
	
	public CookSocket(String name, RestaurantInterface r, RevolvingStandMonitor stand) {
		super(name, r);
		revolvingStand = stand;
		gui = null;
		shouldCheckRevolvingStand = true;
		orders = new ArrayList<MyOrder>();
		foods = new MyFood[Food.getNumberOfFoods()];
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			foods[i] = new MyFood(0, -1, 0, 0, 0.0);
		}
		timer = new Timer();
		needToCheckAllInventory = false;
	}
	
	
	
	// Only use before starting the thread of the CookSocket
	public void setGui(CookSocketGui g) {
		if (!this.threadRunning()) {
			gui = g;
		}
	}
	
	
	
	
	
	
	// NEW MESSAGES
	
	public void msgAddFood(final int foodIndex, final int quantityInitial, final int quantityLowThreshold, final int quantityToResupply, final int minutesCookingTime, final double price, final int quantToAdd) {
		enqueMutation(new Mutation() {
			public void apply() {
				int tempQuant = foods[foodIndex].quantityInStock;
				foods[foodIndex] = new MyFood(quantityInitial, quantityLowThreshold, quantityToResupply, minutesCookingTime, price);
				foods[foodIndex].quantityInStock = tempQuant + quantToAdd;
				restaurant.msgCookUpdatesInventoryAndMenu(CookSocket.this.getMenu(), CookSocket.this.getInventory());
			}
		});
	}
	public void msgCheckTheRevolvingStand() {
		enqueMutation(new Mutation() {
			public void apply() {
				shouldCheckRevolvingStand = true;
			}
		});
	}
	public void msgCustomerPlacedOrder(final AbstractWaiterPuppetInterface w, final RestaurantCustomerPuppetInterface c, final int foodIndex) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyOrder mo = CookSocket.this.findMyOrder(c);
				if (mo == null) {
					orders.add(new MyOrder(w, c, foodIndex));
				}
				else {
					mo.foodIndex = foodIndex;
					mo.state = OrderState.needsToBeConfirmed;
				}
			}
		});
	}
	public void msgThrowAwayOrder(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyOrder mo = CookSocket.this.findMyOrder(c);
				mo.state = OrderState.needsToBeThrownAway;	
			}
		});
	}
	public void msgOrderDoneCooking(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyOrder mo = CookSocket.this.findMyOrder(c);
				if (mo != null && mo.state != OrderState.needsToBeThrownAway) {
					mo.state = OrderState.needsToBePlated;
				}
			}
		});
	}
	public void msgWaiterAsksForOrder(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyOrder mo = CookSocket.this.findMyOrder(c);
				mo.state = OrderState.askedToBeTakenByWaiter;
			}
		});
	}/*
	public void msgSupplyDelivered(final int foodIndex, final int quantity, final double price) {
		enqueMutation(new Mutation() {
			public void apply() {
				AlertLog.getInstance().logInfo(AlertTag.REST_COOK, getAgentName(), "Got resupply from Restaurant of " + quantity + " of " + Food.getFood(foodIndex));
				MyFood mf = foods[foodIndex];
				if (mf == null) {
					mf = new MyFood(0, -1, 0, Food.getFood(foodIndex).minutesCookingTime, price);
				}
				mf.quantityInStock += quantity;
				mf.quantityRequestedFromMarkets -= quantity;
				if (mf.quantityRequestedFromMarkets < 0) {
					// Possible if this message is received after a msgMarketsOutOfStock()
					mf.quantityRequestedFromMarkets = 0;
				}
				restaurant.msgCookUpdatesInventoryAndMenu(CookSocket.this.getMenu(), CookSocket.this.getInventory());
			}
		});
	}*/
	public void msgMarketsOutOfStock(final int foodIndex) {
		enqueMutation(new Mutation() {
			public void apply() {
				AlertLog.getInstance().logInfo(AlertTag.REST_COOK, getAgentName(), "Markets out of " + Food.getFood(foodIndex) + "!");
				MyFood mf = foods[foodIndex];
				mf.marketsOutOfStock = true;
				mf.quantityRequestedFromMarkets = 0;	// because the remainder of the order is canceled
			}
		});
	}
	
	
	
	
	
	
	// OVERRIDES

	@Override
	public void msgWorkerArrived(final SimpleRestaurantWorkerPuppetInterface w) {
		super.msgWorkerArrived(w);
		enqueMutation(new Mutation() {
			public void apply() {
				needToCheckAllInventory = true;
			}
		});
	}
	
	
	
	
	
	
	
	// NEW ACTIONS

	private void actCheckAllInventory() {
		needToCheckAllInventory = false;
		for (int i = 0; i < Food.getNumberOfFoods(); i++) {
			if (foods[i] != null && foods[i].quantityToResupply > 0) {
				this.actEnsureInventoryIsGood(i);
			}
		}
	}
	private void actEnsureInventoryIsGood(int foodIndex) {
		// Check the inventory to see if we need to restock that food type
		MyFood mf = foods[foodIndex];
		if (	! mf.marketsOutOfStock
				&&
				mf.quantityInStock + mf.quantityRequestedFromMarkets <= foods[foodIndex].quantityLowThreshold
				) {
			int resupplyQuantity = foods[foodIndex].quantityToResupply;
			mf.quantityRequestedFromMarkets += resupplyQuantity;
			restaurant.msgCookNeedsResupply(foodIndex, resupplyQuantity);
		}
	}
	private void actHandleOrderRequest(final MyOrder mo) {		
		MyFood mf = foods[mo.foodIndex];
		if (mf == null || mf.quantityInStock == 0) {
			// Customer ordered a food that's not available; reject order
			AlertLog.getInstance().logMessage(AlertTag.REST_COOK, getAgentName(), "Don't have any of " + Food.getFood(mo.foodIndex).name + "; rejected order");
			mo.state = OrderState.denied;
			mo.waiter.msgCookCannotDoOrder(mo.customer, this.getMenu());
		}
		else {
			// Begin to cook the order
			gui.displayFoodBeingCooked(mo.customer, Food.getFood(mo.foodIndex).name);
			mf.quantityInStock--;
			mo.state = OrderState.cooking;
			mo.waiter.msgCookAcceptedOrder(mo.customer, this.getMenu());
			final long trueMillisecondsCookingTime = mf.minutesCookingTime*Broadcaster.getMinuteMillis();
			timer.schedule(
					new TimerTask() {
						public void run() {
							CookSocket.this.msgOrderDoneCooking(mo.customer);
						}
					}
					, trueMillisecondsCookingTime);
			// Submit an up-to-date menu and inventory to the Restaurant
			restaurant.msgCookUpdatesInventoryAndMenu(this.getMenu(), this.getInventory());
			this.actEnsureInventoryIsGood(mo.foodIndex);
		}
		
		/*
		// Report on all foods carried
		System.out.println("Cook's foods:");
		for (String choice : foods.keySet()) {
			MyFood f = foods.get(choice);
			System.out.println("  " + choice + ":  $" + f.price + "  " + f.quantityInStock + " count");
		}
		*/
	}
	private void actThrowAwayOrder(MyOrder mo) {
		AlertLog.getInstance().logMessage(AlertTag.REST_COOK, getAgentName(), "Threw away " + Food.getFood(mo.foodIndex));
		gui.removeFoodDisplay(mo.customer);
		orders.remove(mo);
	}
	private void actPlateOrder(MyOrder mo) {
		gui.displayFoodPlated(mo.customer);
		mo.state = OrderState.waitingToBePickedUp;
		mo.waiter.msgCookFinishedOrder(mo.customer);
	}
	private void actGiveOrderToWaiter(MyOrder mo) {
		gui.removeFoodDisplay(mo.customer);
		orders.remove(mo);
		mo.waiter.msgCookGaveOrder(mo.customer);
	}
	private void actCheckRevolvingStand() {
		shouldCheckRevolvingStand = false;
		RevolvingStandMonitor.Order order = revolvingStand.takeOrderFromQueue();
		if (order != null) {
			this.msgCustomerPlacedOrder(order.waiter, order.orderer, order.foodIndex);
		}
		// Restart timer for checking the stand
		final long trueMillisecondsAfterWhichCheckRevolvingStand = minutesAfterWhichCheckRevolvingStand*Broadcaster.getMinuteMillis();
		timer.schedule(
			new TimerTask() {
				public void run() {
					CookSocket.this.msgCheckTheRevolvingStand();
				}
			}
			, trueMillisecondsAfterWhichCheckRevolvingStand);
	}
	
	
	
	
	
	// SCHEDULER
	
	@Override
	public boolean pickAndExecuteAction() {
		if (worker != null) {
			MyOrder mo;
			if ((mo = this.findMyOrder(OrderState.askedToBeTakenByWaiter)) != null) {
				this.actGiveOrderToWaiter(mo);
				return true;
			}
			if ((mo = this.findMyOrder(OrderState.needsToBePlated)) != null) {
				this.actPlateOrder(mo);
				return true;
			}
			if ((mo = this.findMyOrder(OrderState.needsToBeThrownAway)) != null) {
				this.actThrowAwayOrder(mo);
				return true;
			}
			if ((mo = this.findMyOrder(OrderState.needsToBeConfirmed)) != null) {
				this.actHandleOrderRequest(mo);
				return true;
			}
			if (shouldCheckRevolvingStand) {
				this.actCheckRevolvingStand();
				return true;
			}
			if (needToCheckAllInventory) {
				this.actCheckAllInventory();
				return true;
			}
		}			
		return false;
	}

	
	
}