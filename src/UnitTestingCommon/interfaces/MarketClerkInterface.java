package UnitTestingCommon.interfaces;

import java.util.HashMap;

import city.market.Position;


public interface MarketClerkInterface {
	// messages
	public abstract void msgThisIsMyOrder(final MarketCustomerInterface mCustomer, final HashMap<String, Integer> groceryList);
	
	public abstract void msgItemAvailability(final HashMap<String, Integer> updatedList);
	
	public abstract void msgHereIsMyDecision(final HashMap<String, Integer> decisionList, final double cash);
	
	public abstract String getName();

	public abstract void msgAssignHomePosition(Position p);
	
	
	public abstract void msgYouCanLeave();
	
	public abstract void msgLeaveBuilding();
	
	public abstract void msgImComing();
	
	//public abstract void setMarket(final MarketInterface market);
}
