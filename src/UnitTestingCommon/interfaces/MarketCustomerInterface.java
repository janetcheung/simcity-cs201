package UnitTestingCommon.interfaces;

import java.util.HashMap;

import city.market.Position;


public interface MarketCustomerInterface {
	// messages
	public abstract void msgGoToClerk(final MarketClerkInterface mClerk, final Position p);
	
	public abstract void msgHereIsBill(final HashMap<String, Integer> availableItems, final HashMap<String, Double> pl);
	
	public abstract void msgThankYouGoodbye();
	
	public abstract String getName();
	
	
	public abstract void msgMarketClosed();
	
	public abstract void msgYouCanLeave();
}
