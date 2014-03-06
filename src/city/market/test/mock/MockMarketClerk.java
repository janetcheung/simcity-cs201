package city.market.test.mock;

import java.util.HashMap;

import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketCustomerInterface;
import city.market.Position;

public class MockMarketClerk extends Mock implements MarketClerkInterface {
	
	private String name;
	public EventLog log = new EventLog();
	
	public MockMarketClerk(String name) {
		super(name);
		// TODO Auto-generated constructor stub
		this.name = name;
	}

	@Override
	public void msgThisIsMyOrder(MarketCustomerInterface mCustomer,
			HashMap<String, Integer> groceryList) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgThisIsMyOrder from mCustomer. customer is " + mCustomer.getName() + ", groceryList is " + groceryList));
	}

	@Override
	public void msgItemAvailability(HashMap<String, Integer> updatedList) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgItemAvailability from Market. Available items are " + updatedList));
	}

	@Override
	public void msgHereIsMyDecision(HashMap<String, Integer> decisionList,
			double cash) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgHereIsMyDecision from mCustomer. decisionList is " + decisionList + ", cash = " + cash));
	}
	
	public String getName() {
		return name;
	}

	@Override
	public void msgAssignHomePosition(Position p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgYouCanLeave() {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgYouCanLeave"));
	}

	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgLeaveBuilding"));
	}

	@Override
	public void msgImComing() {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgImComing"));
	}

}
