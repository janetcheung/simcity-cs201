package city.restaurant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import astar.GridCell;
import city.Bill;
import city.Broadcaster;
import city.restaurant.gui.RestaurantCustomerGui;


public class RestaurantCustomerPuppet extends RestaurantParticipantPuppet implements RestaurantCustomerPuppetInterface {
	
	
	
	// DATA
	
	private double money;
	private int myTableID;
	private MyState state;
	private AbstractWaiterPuppetInterface waiter;
	private HashMap<String,Double> menu;
	private Bill bill;
	//private final Timer eatingTimer;
	//private final Timer waitTimer;
	private final Timer timer;
	private int minutesMealDuration;
	private boolean foodPreferenceIsStrong;
	private String foodPreference;
	private boolean isFlake;
	private int minutesWaitingPatience;
	
	
	
	// ENUMS AND CLASSES
	
	private enum MyState { needToGoToFrontOfLine, goingToFrontOfLine, atFrontOfLine, needToGoToHost, goingToHost, 
					needToCheckIn, waitingToBeMetByWaiter, needToGoToTable, goingToTable, needToOrder, waitingForOrder, 
					needToBeginEating, eating, finishedEatingAndNeedToCallForBill, waitingForBill, needToGoToCashier, 
					goingToCashier, needToPay, waitingForCashierToAcceptPay, needToLeave, goingOutRestaurant };
	
	
	
	
	
	// CONSTRUCTOR //////////////////////////////////////////////////////////////////////////////////////////
	
	public RestaurantCustomerPuppet(Setup s) {
		super(s);
		this.money = s.money;
		myTableID = -1;
		state = MyState.needToGoToFrontOfLine;
		waiter = null;
		menu = null;
		bill = null;
		//eatingTimer = new Timer();
		//waitTimer = new Timer();
		timer = new Timer();
		this.minutesMealDuration = s.minutesMealDuration;
		foodPreferenceIsStrong = s.foodPreferenceIsStrong;
		foodPreference = s.foodPreference;
		isFlake = s.isFlake;
		minutesWaitingPatience = s.minutesWaitingPatience;
	}

	
	
	
	
	
	
	// NEW MESSAGES
	
	public void msgComeToHost() {
		enqueMutation(new Mutation() {
			public void apply() {
				if (state != MyState.goingOutRestaurant) {
					state = MyState.needToGoToHost;
				}
			}
		});
	}
	public void msgFollowWaiterToTable(final AbstractWaiterPuppetInterface w, final int tableID, final HashMap<String,Double> menu) {
		if (this.threadRunning()) {
			enqueMutation(new Mutation() {
				public void apply() {
					if (state != MyState.goingOutRestaurant) {
						waiter = w;
						myTableID = tableID;
						RestaurantCustomerPuppet.this.menu = menu;
						state = MyState.needToGoToTable;
					}
					else {
						w.msgCustomerLeavingRestaurant(RestaurantCustomerPuppet.this);
					}
				}
			});
		}
		else {
			// Possible if the customer left the restaurant (from impatience) before the waiter could get to him
			w.msgCustomerLeavingRestaurant(this);
		}
	}
	public void msgWaiterSaysToReorder(final HashMap<String,Double> menuUpdate) {
		enqueMutation(new Mutation() {
			public void apply() {
				if (state != MyState.goingOutRestaurant) {
					menu = menuUpdate;
					state = MyState.needToOrder;
				}
			}
		});
	}
	public void msgWaiterDeliveredOrder(final String foodChoice) {
		enqueMutation(new Mutation() {
			public void apply() {
				if (state != MyState.goingOutRestaurant) {
					((RestaurantCustomerGui)gui).displayFoodBeingEaten(foodChoice);
					state = MyState.needToBeginEating;
				}
			}
		});
	}
	public void msgDoneEating() {
		enqueMutation(new Mutation() {
			public void apply() {
				state = MyState.finishedEatingAndNeedToCallForBill;
			}
		});
	}
	public void msgWaiterGaveBill(final Bill bill) {
		enqueMutation(new Mutation() {
			public void apply() {
				if (state != MyState.goingOutRestaurant) {
					RestaurantCustomerPuppet.this.bill = bill;
					state = MyState.needToGoToCashier;
				}
			}
		});
	}
	public void msgCashierConfirmedPay() {
		enqueMutation(new Mutation() {
			public void apply() {
				if (state != MyState.goingOutRestaurant) {
					state = MyState.needToLeave;
				}
			}
		});
	}
	public void msgRanOutOfPatience() {
		enqueMutation(new Mutation() {
			public void apply() {
				AlertLog.getInstance().logMessage(AlertTag.REST_CUSTOMER, getAgentName(), "Ran out of patience");
				state = MyState.needToLeave;
			}
		});
	}

	
	
	
	
	
	
	
	
	// OVERRIDES

	@Override
	protected void actReactToBlockedRoute() {
		this.actWaitRandomAndTryAgain();
	}
	@Override
	protected void actArrivedAtDestination() {
		if (state == MyState.goingToFrontOfLine) {
			state = MyState.atFrontOfLine;
			this.actSetWaitTimer(MyState.atFrontOfLine, MyState.needToGoToHost, MyState.goingToHost, MyState.needToCheckIn, MyState.waitingToBeMetByWaiter);
		}
		else if (state == MyState.goingToHost) {
			state = MyState.needToCheckIn;
		}
		else if (state == MyState.goingToTable) {
			state = MyState.needToOrder;
		}
		else if (state == MyState.goingToCashier) {
			state = MyState.needToPay;
		}
		else if (state == MyState.goingOutRestaurant) {
			this.getMaster().msgUpdateMoney(money);
			this.getMaster().msgAteFood();
			this.actTerminateParticipation();
		}
	}

	
	
	
	
	
	// NEW ACTIONS
	
	private void actGoToFrontOfLine() {
		state = MyState.goingToFrontOfLine;
		this.setNewDestination(Restaurant.locFrontOfLine.r, Restaurant.locFrontOfLine.c);
	}
	private void actGoToHost() {
		state = MyState.goingToHost;
		GridCell temp = Restaurant.locHost.add(Restaurant.offsetCustomerFromHost);
		this.setNewDestination(temp.r, temp.c);
	}
	private void actCheckIn() {
		state = MyState.waitingToBeMetByWaiter;
		host.msgCustomerCheckingIn(this);
	}
	private void actGoToTable() {
		state = MyState.goingToTable;
		GridCell tableLoc = Restaurant.getLocOfTable(myTableID).add(Restaurant.offsetCustomerFromTable);
		this.setNewDestination(tableLoc.r, tableLoc.c);
	}
	private void actTryToOrder() {
		/*
		System.out.println("Customer's menu:");
		for (String choice : menu.keySet()) {
			System.out.println("  " + choice);
		}
		*/
		
		
		String myFoodChoice = null;

		Set<String> choices = new HashSet<String>(menu.keySet());
		while (choices.size() > 0) {
			int randomChoiceIndex = (int)Math.floor(choices.size()*Math.random());
			String tempChoice = null;
			Iterator<String> it = choices.iterator();
			for (int a = 0; a <= randomChoiceIndex; a++) {
				tempChoice = (String)it.next();
			}
			choices.remove(tempChoice);
			// TODO Right now, we are ignoring foodPreference and searching for the food name in the person's actual name (hack) 
			/*if (	(isFlake || menu.get(tempChoice) <= money) &&
					(foodPreference == null || tempChoice.toLowerCase().equals(foodPreference.toLowerCase()) || !foodPreferenceIsStrong)	) {*/
			if (	(isFlake || menu.get(tempChoice) <= money) &&
					(this.getAgentName().toLowerCase().contains(tempChoice.toLowerCase()) || !foodPreferenceIsStrong)	) {
				// The current choice is acceptable
				myFoodChoice = tempChoice;
				//if (foodPreference == null || myFoodChoice.toLowerCase().equals(foodPreference.toLowerCase())) {
				if (this.getAgentName().toLowerCase().contains(tempChoice.toLowerCase())) {
					// The current choice is preferred; stop looking at other choices
					break;
				}
			}
		}
		
		if (myFoodChoice == null) {
			// Leave restaurant
			AlertLog.getInstance().logMessage(AlertTag.REST_CUSTOMER, getAgentName(), "Leaving because there were no acceptable items on the menu! Either I couldn't afford anything or they didn't have what I wanted.");
			this.actLeave();
		}
		else {
			// Order
			((RestaurantCustomerGui)gui).displayFoodQuestion(myFoodChoice);
			AlertLog.getInstance().logMessage(AlertTag.REST_CUSTOMER, getAgentName(), "Ordering " + myFoodChoice);
			state = MyState.waitingForOrder;
			waiter.msgCustomerOrdered(this, myFoodChoice);
			this.actSetWaitTimer(MyState.waitingForOrder);
		}
	}
	private void actEat() {
		state = MyState.eating;
		final long trueMillisecondsMealDuration = minutesMealDuration*Broadcaster.getMinuteMillis();
		//eatingTimer.schedule(
		timer.schedule (
			new TimerTask() {
				public void run() {
					RestaurantCustomerPuppet.this.msgDoneEating();
				}
			}
			, trueMillisecondsMealDuration);
	}
	private void actCallForBill() {
		((RestaurantCustomerGui)gui).clearExtraDisplays();
		state = MyState.waitingForBill;
		waiter.msgCustomerWantsBill(this);
		this.actSetWaitTimer(MyState.waitingForBill);
	}
	private void actGoToCashier() {
		state = MyState.goingToCashier;
		GridCell temp = Restaurant.locCashier.add(Restaurant.offsetFromCashier);
		this.setNewDestination(temp.r, temp.c);
	}
	private void actPay() {
		state = MyState.waitingForCashierToAcceptPay;
		double amountToPay = Math.min(money, bill.getTotal());
		money -= amountToPay;
		cashier.msgCustomerPaidBill(this, amountToPay);
		this.actSetWaitTimer(MyState.waitingForCashierToAcceptPay);
	}
	private void actLeave() {
		((RestaurantCustomerGui)gui).clearExtraDisplays();
		state = MyState.goingOutRestaurant;
		if (waiter != null) {
			waiter.msgCustomerLeavingRestaurant(this);
		}
		this.setNewDestination(Restaurant.locExit.r, Restaurant.locExit.c);
	}
	private void actSetWaitTimer(final MyState ... statesThatMustBeEscaped) {
		final long trueMillisecondsWaitingPatience = minutesWaitingPatience*Broadcaster.getMinuteMillis();
		timer.schedule(
			new TimerTask() {
				public void run() {
					for (MyState ms : statesThatMustBeEscaped) {
						if (state == ms) {
							RestaurantCustomerPuppet.this.msgRanOutOfPatience();
							break;
						}
					}
				}
			}
			, trueMillisecondsWaitingPatience);
	}
	
	
	
	
	
	
	
	
	// SCHEDULER
	
	@Override
	protected boolean pickAndExecuteAction() {
		if (state == MyState.needToGoToFrontOfLine) {
			this.actGoToFrontOfLine();
			return true;
		}
		if (state == MyState.needToGoToHost) {
			this.actGoToHost();
			return true;
		}
		if (state == MyState.needToCheckIn) {
			this.actCheckIn();
			return true;
		}
		if (state == MyState.needToGoToTable) {
			this.actGoToTable();
			return true;
		}
		if (state == MyState.needToOrder) {
			this.actTryToOrder();
			return true;
		}
		if (state == MyState.needToBeginEating) {
			this.actEat();
			return true;
		}
		if (state == MyState.finishedEatingAndNeedToCallForBill) {
			this.actCallForBill();
			return true;
		}
		if (state == MyState.needToGoToCashier) {
			this.actGoToCashier();
			return true;
		}
		if (state == MyState.needToPay) {
			this.actPay();
			return true;
		}
		if (state == MyState.needToLeave) {
			this.actLeave();
			return true;
		}	
		return false;
	}







	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
	
	
}