package city.market.test.mock;

import UnitTestingCommon.interfaces.MarketClerkInterface;
import UnitTestingCommon.interfaces.MarketCustomerInterface;
import UnitTestingCommon.interfaces.MarketSignInSysInterface;



public class MockMarketSignInSys extends Mock implements MarketSignInSysInterface {
	
	public EventLog log = new EventLog();
	
	public MockMarketSignInSys(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void msgImAvailable(MarketClerkInterface marketClerk) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgImAvailable from mClerk. clerk is " + marketClerk.getName()));
	}

	@Override
	public void msgWaitingInLine(MarketCustomerInterface mCustomer) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgWaitingInLine from mCustomer. customer is " + mCustomer.getName()));
	}

	@Override
	public void msgAddClerk(MarketClerkInterface marketClerk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgRemoveClerk(MarketClerkInterface marketClerk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgCustomerEntered(MarketCustomerInterface marketCustomer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgRemoveCustomer(MarketCustomerInterface marketCustomer) {
		// TODO Auto-generated method stub
		
	}

}
