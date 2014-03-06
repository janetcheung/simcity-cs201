package city.market.test.mock;

import java.util.HashMap;

import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketInterface;
import city.market.MarketClerk;

public class MockMarket extends Mock implements MarketInterface {
	
	public EventLog log = new EventLog();
	
	public MockMarket(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void msgCheckInStock(MarketClerkInterface mClerk,
			HashMap<String, Integer> customerOrder) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgCheckInStock from mClerk. customer order is " + customerOrder));
		
	}

	@Override
	public void msgItemRestock(HashMap<String, Integer> restockingItems) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgItemRestock from mClerk. restocking items are " + restockingItems));
	}

	@Override
	public void addPublicMoney(double amount) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("addPublicMoney called. amount = " + amount));
		
	}

	@Override
	public void msgKillClerk(MarketClerk marketClerk) {
		// TODO Auto-generated method stub
		
	}

}
