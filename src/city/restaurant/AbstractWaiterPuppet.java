package city.restaurant;

import java.util.HashMap;
import java.util.LinkedList;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.AgentInterface;
import UnitTestingCommon.interfaces.RestaurantCashierSocketInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import agent.Agent;
import astar.GridCell;
import city.Bill;
import city.market.Market;
import city.market.Market.Food;
import city.restaurant.gui.WaiterGui;



public abstract class AbstractWaiterPuppet extends RestaurantWorkerPuppet implements AbstractWaiterPuppetInterface {
	
	
	
	// DATA
	
	
	private final LinkedList<MyCustomer> customers;
	private CustomerState journeyPurpose;
	private AgentInterface currentCommunicant;
	protected HashMap<String,Double> menu;
	private boolean setMyselfIdle;
	
	
	
	
	
	
	// ENUMS AND CLASSES
	
	private enum CustomerState { needsToBeSeated, waitForCustomerToOrder, orderNeedsToBeGivenToCook, waitForCookToConfirmOrder,
		needsToReorder, orderBeingCooked, orderNeedsToBePickedUp, waitForCookToGiveOrder, orderNeedsToBeDelivered, billNeedsToBeUpdated,
		waitForCashierToUpdateBill, eating, billNeedsToBeUpdatedAndPickedUp, billNeedsToBePickedUp, waitForCashierToGiveBill,
		billNeedsToBeDelivered, payingBill };
	//private enum WaitState { notWaiting, waiting };
	private class MyCustomer {
		public RestaurantCustomerPuppetInterface agent;
		public int tableID;
		public int foodIndex;
		public CustomerState state;
		public Bill bill;
		public boolean leaving;
		public MyCustomer(RestaurantCustomerPuppetInterface agent, int tableID) {
			this.agent = agent;
			this.tableID = tableID;
			foodIndex = -1;
			state = CustomerState.needsToBeSeated;
			bill = null;
			leaving = false;
		}
	}

	
	
	
	
	// BASIC UTILITY
	
	private MyCustomer findMyCustomer(RestaurantCustomerPuppetInterface c) {
		for (MyCustomer mc : customers) {
			if (mc.agent == c) {
				return mc;
			}
		}
		return null;
	}
	private MyCustomer findMyCustomer(CustomerState s) {
		for (MyCustomer mc : customers) {
			if (mc.state == s) {
				return mc;
			}
		}
		return null;
	}
	private MyCustomer findMyLeavingCustomer() {
		for (MyCustomer mc : customers) {
			if (mc.leaving) {
				return mc;
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	// CONSTRUCTOR //////////////////////////////////////////////////////////////////////////////////////////////////
	
	public AbstractWaiterPuppet(RestaurantParticipantPuppet.Setup s, HashMap<String,Double> menu) {
		super(s);
		this.menu = menu;
		customers = new LinkedList<MyCustomer>();
		journeyPurpose = null;
		currentCommunicant = null;
		setMyselfIdle = false;
	}
	
	
	
	
	
	
	// NEW MESSAGES
	
	public void msgNewCustomerAssignment(final RestaurantCustomerPuppetInterface c, final int tableID) {
		enqueMutation(new Mutation() {
			public void apply() {
				customers.add(new MyCustomer(c, tableID));
			}
		});		
	}
	public void msgCustomerOrdered(final RestaurantCustomerPuppetInterface c, final String foodName) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc != null) {
					for (int i = 0; i < Food.getNumberOfFoods(); i++) {
						if (Food.getFood(i).name.equals(foodName)) {
							mc.foodIndex = i;
							break;
						}
					}
					mc.state = CustomerState.orderNeedsToBeGivenToCook;
				}
			}
		});
	}
	public void msgCookCannotDoOrder(final RestaurantCustomerPuppetInterface c, final HashMap<String,Double> menuUpdate) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc != null) {
					mc.state = CustomerState.needsToReorder;
					menu = menuUpdate;
				}
			}
		});
	}
	public void msgCookAcceptedOrder(final RestaurantCustomerPuppetInterface c, final HashMap<String,Double> menuUpdate) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc == null) {
					cook.msgThrowAwayOrder(c);
				}
				else {
					mc.state = CustomerState.orderBeingCooked;
				}
				menu = menuUpdate;
			}
		});
	}
	public void msgCookFinishedOrder(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc == null) {
					cook.msgThrowAwayOrder(c);
				}
				else {
					mc.state = CustomerState.orderNeedsToBePickedUp;
				}
			}
		});
	}
	public void msgCookGaveOrder(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc != null) {
					((WaiterGui)gui).displayFoodBeingCarried(Market.Food.getFood(mc.foodIndex).name);
					mc.state = CustomerState.orderNeedsToBeDelivered;
				}
			}
		});
	}
	public void msgCashierUpdatedBill(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc != null) {
					if (mc.state == CustomerState.waitForCashierToUpdateBill) {
						mc.state = CustomerState.eating;
					}
					// Otherwise, the waiter must be waiting for the cashier to also GIVE him the bill
					// So his mc.state will remain waitForCashierToGiveBill
				}
			}
		});
	}
	public void msgCustomerWantsBill(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				if (mc != null) {
					if (mc.state == CustomerState.eating) {
						mc.state = CustomerState.billNeedsToBePickedUp;
					}
					else if (mc.state == CustomerState.waitForCashierToUpdateBill) {
						mc.state = CustomerState.billNeedsToBePickedUp;
					}
					else if (mc.state == CustomerState.billNeedsToBeUpdated) {
						mc.state = CustomerState.billNeedsToBeUpdatedAndPickedUp;
					}
					else {
						AlertLog.getInstance().logError(AlertTag.REST_WAITER, getAgentName(), "Unknown customer state");
					}
				}
			}
		});
	}
	public void msgCashierGaveBill(final Bill bill) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer((RestaurantCustomerPuppetInterface)bill.billee);
				if (mc != null) {
					mc.bill = bill;
					mc.state = CustomerState.billNeedsToBeDelivered;
				}
			}
		});
	}
	public void msgCustomerLeavingRestaurant(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyCustomer mc = AbstractWaiterPuppet.this.findMyCustomer(c);
				mc.leaving = true;
			}
		});
	}
	
	
	
	
	
	
	
	
	// OVERRIDES

	@Override
	protected void actArrivedAtDestination() {
		routeRetryCounter = 0;
		CustomerState temp = journeyPurpose;
		journeyPurpose = null;
		if (temp == CustomerState.billNeedsToBeDelivered) {
			this.actDeliverBill();
		}
		else if (temp == CustomerState.billNeedsToBePickedUp) {
			// Ideally this action and the next should be combined into one that does all needed jobs at the cashier for all customers who need it
			this.actAskForBill();
		}
		else if (temp == CustomerState.billNeedsToBeUpdated) {
			this.actUpdateAndMaybePickUpBill();
		}
		else if (temp == CustomerState.orderNeedsToBeDelivered) {
			this.actDeliverOrder();
		}
		else if (temp == CustomerState.orderNeedsToBePickedUp) {
			this.actAskToPickUpOrder();
		}
		else if (temp == CustomerState.needsToReorder) {
			this.actGoToTakeReorder();
		}
		else if (temp == CustomerState.orderNeedsToBeGivenToCook) {
			this.actPlaceOrderAbstract();
		}
		else if (temp == CustomerState.needsToBeSeated) {
			this.actSeatCustomer();
		}
		else if (myShiftState == ShiftState.arriving){
			myShiftState = ShiftState.onShift;
		}
		else if (myShiftState == ShiftState.leaving) {
			this.actTerminateParticipation();
		}
	}

	
	
	
	
	// NEW ACTIONS
	
	private void actBeginShift() {
		myShiftState = ShiftState.arriving;
		this.actGoToBase();
		host.msgWaiterBeganShift(this);
	}
	private void actGoToBase() {
		// The randomized displacement has a slight bias for being to the right and down
		int rowDisplacement = (int)Math.round(Restaurant.allowableWaiterDisplacementFromBaseRow*Math.random()) - (int)Math.floor(Restaurant.allowableWaiterDisplacementFromBaseRow * 0.5);
		int colDisplacement = (int)Math.round(Restaurant.allowableWaiterDisplacementFromBaseCol*Math.random()) - (int)Math.floor(Restaurant.allowableWaiterDisplacementFromBaseCol * 0.5);
		this.actBeginJourney(Restaurant.locWaiterBase.add(new GridCell(rowDisplacement, colDisplacement)), null, null);
	}
	private void actBeginJourney(GridCell dest, AgentInterface newCommunicant, CustomerState purpose) {
		this.setNewDestination(dest.r, dest.c);
		currentCommunicant = newCommunicant;
		journeyPurpose = purpose;
	}
	private void actBeIdle() {
		setMyselfIdle = true;
		this.actGoToBase();
	}
	private void actWindDownShift() {
		myShiftState = ShiftState.preparingToLeave;
		BlockingData<Boolean> blocker = new BlockingData<Boolean>();
		host.msgWaiterEndedShift(this, blocker);
		blocker.get();
	}

	private void actSignOutCustomer(MyCustomer mc) {
		host.msgCustomerLeaving(mc.tableID);
		if (mc.state == CustomerState.orderBeingCooked || mc.state == CustomerState.orderNeedsToBePickedUp) {
			cook.msgThrowAwayOrder(mc.agent);
		}
		if (mc.state != CustomerState.payingBill) {
			// Customer left prematurely due to impatience
			// This will purge waiter's current mission, so his scheduler has to reconsider his mission with the new knowledge of not having this cust
			journeyPurpose = null;
			currentCommunicant = null;
		}
		customers.remove(mc);
	}
	private void actDeliverBill() {
		MyCustomer mc = this.findMyCustomer((RestaurantCustomerPuppetInterface)currentCommunicant);
		mc.state = CustomerState.payingBill;
		mc.agent.msgWaiterGaveBill(mc.bill);
	}
	private void actAskForBill() {
		MyCustomer mc = this.findMyCustomer(CustomerState.billNeedsToBePickedUp);
		mc.state = CustomerState.waitForCashierToGiveBill;
		((RestaurantCashierSocketInterface)currentCommunicant).msgCustomerWantsBill(this, mc.agent);
	}
	private void actUpdateAndMaybePickUpBill() {
		MyCustomer mc = this.findMyCustomer(CustomerState.billNeedsToBeUpdated);
		if (mc == null) {
			// In the time that the waiter was walking to the cashier with the bill update, the customer called msgCustomerWantsBill()
			// and thus setting his state to billNeedsToBeUpdatedAndPickedUp
			mc = this.findMyCustomer(CustomerState.billNeedsToBeUpdatedAndPickedUp);
			mc.state = CustomerState.waitForCashierToGiveBill;
			((RestaurantCashierSocketInterface)currentCommunicant).msgCustomerReceivedOrder(this, mc.agent, Food.getFood(mc.foodIndex).name);
			((RestaurantCashierSocketInterface)currentCommunicant).msgCustomerWantsBill(this, mc.agent);
		}
		else {
			mc.state = CustomerState.waitForCashierToUpdateBill;
			((RestaurantCashierSocketInterface)currentCommunicant).msgCustomerReceivedOrder(this, mc.agent, Food.getFood(mc.foodIndex).name);
		}
	}
	private void actDeliverOrder() {
		((WaiterGui)gui).clearExtraDisplays();
		MyCustomer mc = this.findMyCustomer((RestaurantCustomerPuppetInterface)currentCommunicant);
		mc.state = CustomerState.billNeedsToBeUpdated;
		mc.agent.msgWaiterDeliveredOrder(Food.getFood(mc.foodIndex).name);
	}
	private void actAskToPickUpOrder() {
		MyCustomer mc = this.findMyCustomer(CustomerState.orderNeedsToBePickedUp);
		mc.state = CustomerState.waitForCookToGiveOrder;
		((CookSocket)currentCommunicant).msgWaiterAsksForOrder(mc.agent);
	}
	private void actGoToTakeReorder() {
		MyCustomer mc = this.findMyCustomer((RestaurantCustomerPuppetInterface)currentCommunicant);
		mc.state = CustomerState.waitForCustomerToOrder;
		mc.agent.msgWaiterSaysToReorder(new HashMap<String,Double>(menu));
	}
	private void actPlaceOrderAbstract() {
		MyCustomer mc = this.findMyCustomer(CustomerState.orderNeedsToBeGivenToCook);
		mc.state = CustomerState.waitForCookToConfirmOrder;
		// Abstract method which is defined differently by RegularWaiterAgents and ProducerConsumerWaiterAgents
		this.actPlaceCustomerOrder(mc.agent, mc.foodIndex);
	}
	private void actSeatCustomer() {
		MyCustomer mc = this.findMyCustomer((RestaurantCustomerPuppetInterface)currentCommunicant);
		mc.state = CustomerState.waitForCustomerToOrder;
		mc.agent.msgFollowWaiterToTable(this, mc.tableID, new HashMap<String,Double>(menu));
		host.msgWaiterTookCustomer();
		this.actBeginJourney(Restaurant.getLocOfTable(mc.tableID).add(Restaurant.offsetWaiterFromTable), mc.agent, CustomerState.waitForCustomerToOrder);
	}

	protected abstract void actPlaceCustomerOrder(RestaurantCustomerPuppetInterface c, int foodIndex);
	
	
	
	
	
	
	
	
	
	// SCHEDULER
	
	@Override
	protected boolean pickAndExecuteAction() {
		MyCustomer mc;
		
		if (journeyPurpose == null) {			
			
			if ((mc = this.findMyLeavingCustomer()) != null) {
				this.actSignOutCustomer(mc);
				return true;
			}
			
			// First Priority tier -- when the waiter must wait for a response message
			if ((mc = this.findMyCustomer(CustomerState.waitForCashierToGiveBill)) != null) {
				return false;
			}
			if ((mc = this.findMyCustomer(CustomerState.waitForCashierToUpdateBill)) != null) {
				return false;
			}
			if ((mc = this.findMyCustomer(CustomerState.waitForCookToGiveOrder)) != null) {
				return false;
			}
			if ((mc = this.findMyCustomer(CustomerState.waitForCookToConfirmOrder)) != null) {
				return false;
			}
			if ((mc = this.findMyCustomer(CustomerState.waitForCustomerToOrder)) != null) {
				if (!mc.leaving) {
					return false;
				}
			}
			
			// Second Priority tier -- parts of action sequences that should not be interrupted
			if ((mc = this.findMyCustomer(CustomerState.billNeedsToBeDelivered)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.getLocOfTable(mc.tableID).add(Restaurant.offsetWaiterFromTable), mc.agent, CustomerState.billNeedsToBeDelivered);
				return true;
			}
			if ((mc = this.findMyCustomer(CustomerState.billNeedsToBeUpdated)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.locCashier.add(Restaurant.offsetFromCashier), cashier, CustomerState.billNeedsToBeUpdated);
				return true;
			}
			if ((mc = this.findMyCustomer(CustomerState.orderNeedsToBeDelivered)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.getLocOfTable(mc.tableID).add(Restaurant.offsetWaiterFromTable), mc.agent, CustomerState.orderNeedsToBeDelivered);
				return true;
			}
			if ((mc = this.findMyCustomer(CustomerState.needsToReorder)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.getLocOfTable(mc.tableID).add(Restaurant.offsetWaiterFromTable), mc.agent, CustomerState.needsToReorder);
				return true;
			}
			if ((mc = this.findMyCustomer(CustomerState.orderNeedsToBeGivenToCook)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.locCook.add(Restaurant.offsetFromCook), cook, CustomerState.orderNeedsToBeGivenToCook);
				return true;
			}
	
			// Third Priority tier -- root origins of action sequences
			/*if ((mc = this.findMyLeavingCustomer()) != null) {
				if (mc.state != CustomerState.orderBeingCooked && mc.state != CustomerState.orderNeedsToBePickedUp) {
					this.actSignOutCustomer(mc);
					return true;
				}
			}*/
			if (myShiftState == ShiftState.gotPay) {
				this.actWindDownShift();
			}
			if ((mc = this.findMyCustomer(CustomerState.billNeedsToBePickedUp)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.locCashier.add(Restaurant.offsetFromCashier), cashier, CustomerState.billNeedsToBePickedUp);
				return true;
			}
			if ((mc = this.findMyCustomer(CustomerState.orderNeedsToBePickedUp)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.locCook.add(Restaurant.offsetFromCook), cook, CustomerState.orderNeedsToBePickedUp);
				return true;
			}
			if ((mc = this.findMyCustomer(CustomerState.needsToBeSeated)) != null) {
				setMyselfIdle = false;
				this.actBeginJourney(Restaurant.locHost.add(Restaurant.offsetWaiterFromHost), mc.agent, CustomerState.needsToBeSeated);
				return true;
			}
			
			// Fourth Priority tier -- what to do when no jobs to do
			if (myShiftState == ShiftState.justEntered) {
				setMyselfIdle = false;
				this.actBeginShift();
				return true;
			}
			if (myShiftState == ShiftState.onShift && !setMyselfIdle) {
				this.actBeIdle();
				return true;
			}
			if (myShiftState == ShiftState.preparingToLeave) {
				if (customers.size() == 0) {
					setMyselfIdle = false;
					this.actGoToExit();
					return true;
				}
				else {
					if (!setMyselfIdle) {
						this.actBeIdle();
						return true;
					}
				}
			}
			
		}
		
		return false;
	}
	
	public void destructor(){
		
	}
}