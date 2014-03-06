package UnitTestingCommon.interfaces;

import java.util.HashMap;

import city.market.MarketClerk;
import city.market.Market.Food;


public interface MarketInterface {
	// messages
	public abstract void msgCheckInStock(final MarketClerkInterface mClerk, final HashMap<String, Integer> customerOrder);
	public abstract void msgItemRestock(final HashMap<String, Integer> restockingItems);
	public abstract void addPublicMoney(double amount);
	public abstract void msgKillClerk(MarketClerk marketClerk);
	
	public HashMap<String, Double> priceList = new HashMap<String, Double>();
}
