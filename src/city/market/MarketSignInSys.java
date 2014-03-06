package city.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketCustomerInterface;
import UnitTestingCommon.interfaces.MarketSignInSysInterface;
import agent.Agent;
//import agent.Agent.Mutation;

public class MarketSignInSys extends Agent implements MarketSignInSysInterface {
	
	// utilities
	public MarketSignInSys(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public class WorkingClerk {	// public for JUnit testing
		private MarketClerkInterface mClerk;
		public boolean busy;	// public for JUnit testing
		Position p;
		
		WorkingClerk(MarketClerkInterface marketClerk, Position pos) {
			mClerk = marketClerk;
			busy = true;
			p = new Position(pos.row, pos.col);
		}
		
	}
	
	/*
	public class Position {
		int row;
		int col;
		
		Position(int x, int y) {
			row = x;
			col = y;
		}
	}
	*/
	
	/*
	public void getClerkWorkingPosition(MarketClerk mClerk, Position p) {
		System.out.println("get working position called. clerk is " + mClerk.getName() + " workingClerks size is " + workingClerks.size());
		for (WorkingClerk workingClerk : workingClerks) {
			if (workingClerk.mClerk == mClerk) {
				p = new Position(workingClerk.p.row, workingClerk.p.col);
				//System.out.println("working position has set");
			}
			else {
				System.err.println("invalid mClerk");
			}
		}
	}
	*/
	
	// data
	private long currentTime;	// may not need
	
	public List<WorkingClerk> workingClerks	// public for JUnit testing
	= new ArrayList<WorkingClerk>();
	
	public List<MarketCustomerInterface> waitingCustomers	// public for JUnit testing
	= new ArrayList<MarketCustomerInterface>();
	
	public List<Position> workingPositions	// public for JUnit testing
	= new ArrayList<Position>();
	{
		workingPositions.add(new Position(26, 4));
		workingPositions.add(new Position(21, 4));
		workingPositions.add(new Position(16, 4));
		workingPositions.add(new Position(11, 4));
	}
	
	private List<MarketCustomerInterface> enteredCustomers	// list of all customers entered the market
	= new ArrayList<MarketCustomerInterface>();
	// messages
	public void msgImAvailable(final MarketClerkInterface marketClerk) {
		enqueMutation(new Mutation(){
			public void apply(){
				for (WorkingClerk workingClerk : workingClerks) {
					if (workingClerk.mClerk == marketClerk) {
						workingClerk.busy = false;
						break;
					}
				}
			}
		});
	}
	
	public void msgWaitingInLine(final MarketCustomerInterface mCustomer) {
		enqueMutation(new Mutation(){
			public void apply(){
				waitingCustomers.add(mCustomer);
			}
		});
	}
	
	public void msgAddClerk(final MarketClerkInterface marketClerk) {
		enqueMutation(new Mutation(){
			public void apply(){
				if (workingPositions.size() > 0) {
					workingClerks.add(new WorkingClerk(marketClerk, workingPositions.get(0)));
					// notify that clerk
					marketClerk.msgAssignHomePosition(workingPositions.get(0));
					
					// remove the assigned workingPosition
					workingPositions.remove(0);
					//System.out.println("clerk added. workingClerks size is " + workingClerks.size());

				} else {
					//System.err.println("not enough working positions");
					AlertLog.getInstance().logError(AlertTag.MARK, "MarketSignIn", "not enough working positions");
				}
				
			}
		});
	}
	
	public void msgRemoveClerk(final MarketClerkInterface marketClerk) {
		enqueMutation(new Mutation(){
			public void apply(){
				for (WorkingClerk workingClerk : workingClerks) {
					if (workingClerk.mClerk == marketClerk) {
						// recycle the workingPosition
						workingPositions.add(new Position(workingClerk.p.row, workingClerk.p.col));
						workingClerks.remove(workingClerk);
						marketClerk.msgYouCanLeave();
						break;
					}
				}
			}
		});
	}
	
	public void msgRemoveCustomer(final MarketCustomerInterface marketCustomer) {
		enqueMutation(new Mutation(){
			public void apply(){
				for (MarketCustomerInterface mCustomer : waitingCustomers) {
					if (mCustomer == marketCustomer) {
						waitingCustomers.remove(marketCustomer);
						marketCustomer.msgYouCanLeave();
						break;
					}
				}
				
				// also remove from enteredCustomers list
				enteredCustomers.remove(marketCustomer);
			}
		});
	}
	
	public void msgMarketClosed() {
		enqueMutation(new Mutation(){
			public void apply(){
				for (WorkingClerk workingClerk : workingClerks) {
					workingClerk.mClerk.msgLeaveBuilding();
				}
				for (MarketCustomerInterface marketCustomer: enteredCustomers) {
					marketCustomer.msgMarketClosed();
					// Concurrent Modification Exception
					//waitingCustomers.remove(marketCustomer);
				}
				
			}
		});
	}
	
	public void msgCustomerEntered(final MarketCustomerInterface mCustomer) {
		enqueMutation(new Mutation(){
			public void apply(){
				//System.out.println("msgCustomerEntered received");
				enteredCustomers.add(mCustomer);
			}
		});
	}
	
	// scheduler
	public boolean pickAndExecuteAction() {	// public for JUnit testing
		// TODO Auto-generated method stub
		if (!waitingCustomers.isEmpty()) {
			for (WorkingClerk workingClerk : workingClerks) {
				if (!workingClerk.busy) {
					AssignAClerk(workingClerk);
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	// action
	private void AssignAClerk(WorkingClerk workingClerk) {
		//Do("waiters size " + waiters.size());
		//Do("index = " + index);
		waitingCustomers.get(0).msgGoToClerk(workingClerk.mClerk, workingClerk.p);
		enteredCustomers.remove(waitingCustomers.get(0));
		waitingCustomers.remove(0);
		workingClerk.busy = true;
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
	
	
}
