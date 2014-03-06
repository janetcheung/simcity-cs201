package city.restaurant;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import city.Broadcaster;
import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.HostSocketInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import astar.MovementManager;


public class HostSocket extends SimpleRestaurantWorkerSocket implements HostSocketInterface {
	
	
	
	// DATA

	private static int minutesAfterWhichCheckLine = 4;
	
	
	// Need to reference the MovementManager to ask it for who is standing at the front of the line
	private MovementManager restaurantGrid;
	public boolean needToCheckFrontOfLine;
	public ArrayList<RestaurantCustomerPuppetInterface> customersToSeat;
	public ArrayList<AbstractWaiterPuppetInterface> waitersToUse;
	public ArrayList<Integer> tablesFree;
	private Timer timer;
	private boolean customerIsWithMe;
	private int roundRobinCounter;		// for assigning which waiter to handle the next customer
	
	
	
	
	
	
	
	
	
	
	
	// CONSTRUCTOR /////////////////////////////////////////////////////////////////////////////////
	
	public HostSocket(String name, MovementManager m, RestaurantInterface r, int numTables) {
		super(name, r);
		restaurantGrid = m;
		needToCheckFrontOfLine = true;
		customersToSeat = new ArrayList<RestaurantCustomerPuppetInterface>();
		waitersToUse = new ArrayList<AbstractWaiterPuppetInterface>();
		tablesFree = new ArrayList<Integer>();
		for (int a = 0; a < numTables; a++) {
			tablesFree.add(a);
		}
		//checkLineTimer = new Timer();
		timer = new Timer();
		customerIsWithMe = false;
		roundRobinCounter = 0;
		
		this.msgCheckFrontOfLine();
	}

	
	
	
	
	
	
	
	// NEW MESSAGES
	
	public void msgCheckFrontOfLine() {
		enqueMutation(new Mutation() {
			public void apply() {
				needToCheckFrontOfLine = true;
				final long trueMillisecondsAfterWhichCheckLine = minutesAfterWhichCheckLine*Broadcaster.getMinuteMillis();
				//checkLineTimer.schedule(
				timer.schedule(
					new TimerTask() {
						public void run() {
							HostSocket.this.msgCheckFrontOfLine();
						}
					}
					, trueMillisecondsAfterWhichCheckLine);
			}
		});
	}
	public void msgCustomerCheckingIn(final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				customersToSeat.add(c);
			}
		});
	}
	public void msgWaiterTookCustomer() {
		enqueMutation(new Mutation() {
			public void apply() {
				customerIsWithMe = false;
			}
		});
	}
	public void msgWaiterBeganShift(final AbstractWaiterPuppetInterface w) {
		enqueMutation(new Mutation(){
			public void apply() {
				waitersToUse.add(0, w);
			}
		});
	}
	public void msgWaiterEndedShift(final AbstractWaiterPuppetInterface w, final BlockingData<Boolean> blocker) {
		enqueMutation(new Mutation(){
			public void apply() {
				waitersToUse.remove(w);
				blocker.unblock(null);
			}
		});
	}
	public void msgCustomerLeaving(final int tableID) {
		enqueMutation(new Mutation(){
			public void apply() {
				tablesFree.add(tableID);
			}
		});
	}
	
	
	
	
	
	
	
	
	
	
	// NEW ACTIONS
	
	private void actCheckFrontOfLine() {
		// If there is a customer a the front of the line, invite him forward. If not, wait and try again
		needToCheckFrontOfLine = false;
		final BlockingData<RestaurantCustomerPuppet> customerAtFrontOfLine=new BlockingData<RestaurantCustomerPuppet>();
		restaurantGrid.msgGetMoveableAt(customerAtFrontOfLine,Restaurant.locFrontOfLine.r,Restaurant.locFrontOfLine.c);
		RestaurantCustomerPuppet c = customerAtFrontOfLine.get();
		if (c != null) { // customerAtFrontOfLine.get() will wait until customerAtFrontOfLine.unblock() is called by the movement manager
			customerIsWithMe = true;
			c.msgComeToHost();
		}
	}
	private void actMakeCustomerAssignment() {
		waitersToUse.get((roundRobinCounter++) % waitersToUse.size()).msgNewCustomerAssignment(customersToSeat.remove(0), tablesFree.remove(0));
	}
	
	
	
	
	
	
	// SCHEDULER
	
	@Override
	public boolean pickAndExecuteAction() {
		if (worker != null) {
			if (needToCheckFrontOfLine && !customerIsWithMe) {
				this.actCheckFrontOfLine();
				return true;
			}
			if (customersToSeat.size() > 0 && waitersToUse.size() > 0 && tablesFree.size() > 0) {
				this.actMakeCustomerAssignment();
				return true;
			}
		}
		return false;
	}
	
	
	
}