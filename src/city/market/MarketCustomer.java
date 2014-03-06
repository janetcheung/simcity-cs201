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
import UnitTestingCommon.interfaces.MarketSignInSysInterface;
//import agent.Agent.Mutation;
import astar.MovementManager;
import city.Building;
import city.Person;
import city.Puppet;
import city.Puppet.Setup;
import city.bank.BankCustomer;
import city.market.gui.MarketGui;
import city.market.Market.CheckingRequest;
import city.market.Market.Food;
import city.market.gui.MarketClerkGui;
import city.market.gui.MarketCustomerGui;

public class MarketCustomer extends Puppet implements MarketCustomerInterface {
	
	// utilities

	public MarketCustomer(String name,MovementManager m,Building b,Setup s) {
		super(name, m, b, s);


		// TODO Auto-generated constructor stub
		this.name = name;
		mCustomerGui = new MarketCustomerGui(this);
		
		// initialization
		walletMoney = s.money;
		groceryList = new HashMap<String, Integer>();
		priorities = new ArrayList<String>();
		priceList = new HashMap<String, Double>();
				
		for (int i = 0; i < s.foodsToBuyQuantities.length; i++) {
			if (s.foodsToBuyQuantities[i] != 0) {
				groceryList.put(Food.allFoods[i].name, s.foodsToBuyQuantities[i]);
				priorities.add(Food.allFoods[i].name);
			}
			
		}
		
	}

	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void actMoveStep(int r, int c) {
		// TODO Auto-generated method stub
		mCustomerGui.setDestination(r, c);
	}

	@Override
	protected void actReactToBlockedRoute() {
		// TODO Auto-generated method stub
		this.actWaitRandomAndTryAgain();
	}

	@Override
	protected void actArrivedAtDestination() {
		// TODO Auto-generated method stub
		//arrived.release();
		//System.out.println("arrived");
		
		/*
		if (state == AgentState.Waiting) {
			mSignInSys.msgWaitingInLine(this);
			//System.out.println(event);
			
			System.out.println("signed in");
			
			event = AgentEvent.SignedIn;
			this.pickAndExecuteAction();
		}
		
		if (state == AgentState.Communicating) {
			myClerk.msgThisIsMyOrder(this, groceryList);
		}
		
		if (state == AgentState.Leaving) {
			this.getMaster().msgPuppetLeftBuilding();
			this.removeFromManager();
			market.msgKillCustomer(this);
		}
		*/
		
		if (state == AgentState.Entering) {
			event = AgentEvent.FinishEntering;
			this.pickAndExecuteAction();
		}
		
		if (state == AgentState.MovingToClerk) {
			event = AgentEvent.FinishMoving;
			this.pickAndExecuteAction();
		}
		
		if (state == AgentState.Leaving) {
			
			this.getMaster().msgUpdateInventory(foodsBoughtQuantities);
			this.getMaster().msgPuppetLeftBuilding();
			this.removeFromManager();
			market.msgKillCustomer(this);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setMarketSignInSys(MarketSignInSysInterface mSignIn) {
		mSignInSys = mSignIn;
	}
	
	/*
	public void setGui(MarketCustomerGui g) {
		this.mCustomerGui = g;
	}
	*/
	
	public MarketCustomerGui getGui() {
		return mCustomerGui;
	}
	
	public void setMainGui(MarketGui mGui) {
		mainGui = mGui;
	}
	
	public void setMarket(Market m) {
		market = m;
	}
	
	// data
	private String name;
	
	public enum AgentState
	{Initial, Entering, Waiting, MovingToClerk, Communicating, DecidingAndPaying, Leaving};
	public AgentState state = AgentState.Initial;//The start state	// public for JUnit testing

	public enum AgentEvent
	{none, FinishEntering, SignedIn, RemoveConfirmationReceived, ClerkAvailable, FinishMoving, BillReady, BillPaid};
	public AgentEvent event = AgentEvent.none;	// public for JUnit testing
	
	/*
	public HashMap<String, Integer> groceryList = new HashMap<String, Integer>();	// public for JUnit testing
	{
		groceryList.put("Steak", 2);
		groceryList.put("Chicken", 2);
		groceryList.put("Salad", 2);
		groceryList.put("Pizza", 2);
	}
	*/
	public HashMap<String, Integer> groceryList;
	
	/*
	public List<String> priorities	// public for JUnit testing
	= new ArrayList<String>();
	{
		priorities.add("Steak");
		priorities.add("Chicken");
		priorities.add("Salad");
		priorities.add("Pizza");
	}
	*/
	public List<String> priorities;
	
	//public HashMap<String, Double> priceList = new HashMap<String, Double>();	// public for JUnit testing
	public HashMap<String, Double> priceList;
	
	MarketSignInSysInterface mSignInSys;
	public MarketClerkInterface myClerk;	// public for JUnit testing
	
	public double walletMoney;	// public for JUnit testing
	
	public double paymentAmount;	// public for JUnit testing
	
	
	
	private Position dest;
	private Position exit = new Position(16, 29);
	
	private MarketCustomerGui mCustomerGui;
	
	private MarketGui mainGui;
	
	//private Semaphore arrived = new Semaphore(0,true);	
	
	private Market market;
	

	Timer timer = new Timer();
	boolean isPatient = true;
	boolean signedOut = false;
	
	private Semaphore timeSem = new Semaphore(0,true);
	
	public int[] foodsBoughtQuantities = new int[Food.getNumberOfFoods()];
	
	// messages
	public void msgYouCanLeave() {
		enqueMutation(new Mutation(){
			public void apply(){
				//System.out.println(MarketCustomer.this.name + ": msgYouCanLeave received");
				if (state == AgentState.Waiting && event != AgentEvent.ClerkAvailable) {
					event = AgentEvent.RemoveConfirmationReceived;
					//MarketCustomer.this.pickAndExecuteAction();
				}
			}
		});
	}
	
	public void msgGoToClerk(final MarketClerkInterface mClerk, final Position clerkPos) {
		enqueMutation(new Mutation(){
			public void apply(){
				//System.out.println(MarketCustomer.this.name + ": msgGoToClerk received");
				AlertLog.getInstance().logMessage(AlertTag.MARK_CUSTOMER, getAgentName(), MarketCustomer.this.name + ": msgGoToClerk received");
				myClerk = mClerk;
				dest = new Position(clerkPos.row, clerkPos.col + 3);
				//System.out.println("dest = (" + dest.row + ", " + dest.col + ")");
				AlertLog.getInstance().logMessage(AlertTag.MARK_CUSTOMER, getAgentName(), "dest = (" + dest.row + ", " + dest.col + ")");
				event = AgentEvent.ClerkAvailable;
			}
		});
	}
	
	public void msgHereIsBill(final HashMap<String, Integer> availableItems, final HashMap<String, Double> pl) {
		enqueMutation(new Mutation(){
			public void apply(){
				// update grocery list
				groceryList = availableItems;
				priceList = pl;
				//System.out.println(priceList);
				
				event = AgentEvent.BillReady;
			}
		});
	}
	
	public void msgThankYouGoodbye() {
		enqueMutation(new Mutation(){
			public void apply(){
				// update grocery list
				
				// convert to foodsBoughtQuantities
				for (String key : groceryList.keySet()) {
					/*
					// iterate allFoods array
					for (int i = 0; i < Food.getNumberOfFoods(); i++) {
						if (Food.allFoods[i].name == key) {	// in order to find the index
							foodsBoughtQuantities[i] = groceryList.get(key);
							break;
						}
					}
					*/
					
					// in java all elements are initialized to 0 by default
					foodsBoughtQuantities[Food.getFood(key).index] = groceryList.get(key);
				}
				
				event = AgentEvent.BillPaid;
			}
		});
	}
	
	public void msgMarketClosed() {
		enqueMutation(new Mutation(){
			public void apply(){
				isPatient = false;
            	//MarketCustomer.this.pickAndExecuteAction();
			}
		});
	}
	
	// scheduler
	@Override
	public boolean pickAndExecuteAction() {	// public for JUnit testing
		// TODO Auto-generated method stub
		//if (state == AgentState.Initial && event == AgentEvent.none) {
		if (state == AgentState.Initial) {
			state = AgentState.Entering;
			EnterMarket();
			return true;
		}
		
		if (state == AgentState.Entering && event == AgentEvent.FinishEntering) {
			state = AgentState.Waiting;
			return true;
		}
		
		//if (state == AgentState.Waiting && event != AgentEvent.SignedIn && event != AgentEvent.NoPatience && event != AgentEvent.ClerkAvailable){
		if (state == AgentState.Waiting && event == AgentEvent.FinishEntering){
			SignIn();
			return true;
		}
		
		if (state == AgentState.Waiting && event != AgentEvent.ClerkAvailable && !isPatient && !signedOut) {
			SignOut();
			return true;
		}
		
		// potential bugs could occur w/o a confirmation from mSignInSys b/c the message may not be processed immediately
		if (state == AgentState.Waiting && event == AgentEvent.RemoveConfirmationReceived) {
			state = AgentState.Leaving;
			LeaveMarket();
			return true;
		}
		
		if (state == AgentState.Waiting && event == AgentEvent.ClerkAvailable){
			state = AgentState.MovingToClerk;
			MoveToClerk();
			return true;
		}
		
		if (state == AgentState.MovingToClerk && event == AgentEvent.FinishMoving){
			//System.out.println("arrived at the clerk");
			AlertLog.getInstance().logMessage(AlertTag.MARK_CUSTOMER, getAgentName(), "arrived at the clerk");
			state = AgentState.Communicating;
			TalkToClerk();
			return true;
		}
		
		if (state == AgentState.Communicating && event == AgentEvent.BillReady){
			state = AgentState.DecidingAndPaying;
			MakeDecisionAndPay();
			return true;
		}
		
		if (state == AgentState.DecidingAndPaying && event == AgentEvent.BillPaid){
			state = AgentState.Leaving;
			LeaveMarket();
			return true;
		}
		
		/*
		if (state == AgentState && event == AgentEvent){
			state = AgentState;
			//goCheck();
			return true;
		}
		*/
		
		return false;
	}

	// action
	private void EnterMarket() {
		//System.out.println("entering market");
		mSignInSys.msgCustomerEntered(this);
		
		timer.schedule(new TimerTask() {
            public void run(){
            	isPatient = false;
            	MarketCustomer.this.pickAndExecuteAction();
            }
	    },
	    10000);
		
		this.setNewDestination(28, 19);
	}
	
	private void SignIn() {
		// gui move to the front of the line
		//this.setNewDestination(28, 19);
		
		
		
		mSignInSys.msgWaitingInLine(this);
		//System.out.println(event);
		
		//System.out.println("signed in");
		AlertLog.getInstance().logMessage(AlertTag.MARK_CUSTOMER, getAgentName(), "signed in");
		
		event = AgentEvent.SignedIn;
		
		//stateChange()
		/*
		enqueMutation(new Mutation(){
			public void apply(){
				event = AgentEvent.SignedIn;
			}
		});
		*/
		
	}
	
	private void SignOut() {
		mSignInSys.msgRemoveCustomer(this);
		signedOut = true;
	}
	
	private void MoveToClerk() {
		//System.out.println("go talk to clerk");
		AlertLog.getInstance().logMessage(AlertTag.MARK_CUSTOMER, getAgentName(), "go talk to clerk");
		
		// notify mClerk so that he won't leave
		myClerk.msgImComing();
		
		// gui moves to my clerk
		this.setNewDestination(dest.row, dest.col);
		
		//myClerk.msgThisIsMyOrder(this, groceryList);
	}
	
	private void TalkToClerk() {
		//System.out.println("talk to clerk");
		AlertLog.getInstance().logMessage(AlertTag.MARK_CUSTOMER, getAgentName(), "talk to clerk");
		
		myClerk.msgThisIsMyOrder(this, groceryList);
	}
	
	private void MakeDecisionAndPay() {
		double temp = 0;
		// make a decision based on priorities
		// and update grocery list
		for (String key : priorities) {
			if (temp <= walletMoney) {
				for (int i = 0; i < groceryList.get(key); i++) {
					//System.out.println(i);
					temp += priceList.get(key);
					//System.out.println(temp);
					if (temp > walletMoney) {
						//System.out.println(i);
						groceryList.put(key, i);
						break;
						
						/*
						if (i != 0) {
							groceryList.put(key, i);
							break;
						}
						else {	// erase 0 items
							groceryList.remove(key);
							break;
						}
						*/
						
					}
				}
			}
			
			else {
				// set the amount of all rest items in groceryList to 0
				groceryList.put(key, 0);
				
				// another way is to remove them from the list
				//groceryList.remove(key);
			}
			
		}
		
		// compute payment
		for (String key : groceryList.keySet()) {
			paymentAmount += priceList.get(key) * groceryList.get(key);
		}
		
		walletMoney -= paymentAmount;
		
		// delay 1000
		timer.schedule(new TimerTask() {
            public void run(){
            	timeSem.release();
            }
	    },
	    1000);
		
		try {
			timeSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// make a payment
		myClerk.msgHereIsMyDecision(groceryList, paymentAmount);
		
	}
	
	private void LeaveMarket() {
		//myClerk.msgGoodbye();
		
		// gui moves to door
		this.setNewDestination(exit.row, exit.col);
		
		
		
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
	
}
