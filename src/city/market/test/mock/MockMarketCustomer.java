package city.market.test.mock;

import java.util.HashMap;

import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketCustomerInterface;
import city.market.Position;

public class MockMarketCustomer extends Mock implements MarketCustomerInterface {
	
	private String name;
	public EventLog log = new EventLog();
	
	public MockMarketCustomer(String name) {
		super(name);
		// TODO Auto-generated constructor stub
		this.name = name;
	}

	@Override
	public void msgGoToClerk(MarketClerkInterface mClerk, Position p) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgGoToClerk from mSignInSys. mClerk is " + mClerk.getName() + ", row = " + p.row + ", col = " + p.col));
	}

	@Override
	public void msgHereIsBill(HashMap<String, Integer> availableItems,
			HashMap<String, Double> pl) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgHereIsBill from mClerk. Available items are " + availableItems + ", prices are " + pl));
	}

	@Override
	public void msgThankYouGoodbye() {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgThankYouGoodbye from mClerk."));
	}

	@Override
	public void msgMarketClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgYouCanLeave() {
		// TODO Auto-generated method stub
		
	}
	
	

}
