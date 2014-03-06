package city.market.test;

import agent.Agent;
import junit.framework.TestCase;
import city.market.MarketSignInSys;
import city.market.test.mock.MockMarketClerk;
import city.market.test.mock.MockMarketCustomer;

public class MarketSignInSysTest extends TestCase {
	//these are instantiated for each test separately via the setUp() method.
	MarketSignInSys mSignInSys;
	MockMarketClerk mockMarketClerk;
	MockMarketCustomer mockMarketCustomer;
	
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp();
		mSignInSys = new MarketSignInSys("signIn");
		mockMarketClerk = new MockMarketClerk("clerk");		
		mockMarketCustomer = new MockMarketCustomer("customer");
		
		//mSignInSys.underTest();
		Agent.setJUNIT(true);
	}
	
	
	public void testOneNormalScenario()
	{
		//setUp() runs first before this test!
		//mSignInSys.startAgent();
		
		//check preconditions
		// correct order expected:<> was:<> doesn't matter if they are equal
		assertEquals("MarketSignInSys should have 0 working clerks. Instead, there were " + mSignInSys.workingClerks.size() + " working clerks", 0, mSignInSys.workingClerks.size());
		assertEquals("MarketSignInSys should have 0 waiting customers. It didn't.", 0, mSignInSys.waitingCustomers.size());
		assertEquals("MockMarketCustomer should have an empty event log before MockMarketSignInSys calls its msgGoToClerk. Instead, the MockMarketCustomer's event log reads: "
				+ mockMarketCustomer.log.toString(), 0, mockMarketCustomer.log.size());
		
		//step 1 of the test
		assertFalse("MarketSignInSys's scheduler should have returned false, but didn't.", 
				mSignInSys.pickAndExecuteAction());
		
		mSignInSys.msgWaitingInLine(mockMarketCustomer);//send the message from a mCustomer
	
		assertEquals("MarketSignInSys should have 1 waiting customer. It didn't.", 1, mSignInSys.waitingCustomers.size());
		assertEquals("MarketSignInSys should have 0 working clerks. Instead, there were " + mSignInSys.workingClerks.size() + " working clerks", 0, mSignInSys.workingClerks.size());
		
		//step 2 of the test
		assertFalse("MarketSignInSys's scheduler should have returned false when there was no working clerk, but didn't.", 
				mSignInSys.pickAndExecuteAction());
		
		//step 3
		mSignInSys.msgAddClerk(mockMarketClerk);//send the message from a mClerk
		
		assertEquals("MarketSignInSys should have 1 working clerk. Instead, there were " + mSignInSys.workingClerks.size() + " working clerks", 1, mSignInSys.workingClerks.size());
		assertTrue("The working clerk should be busy. But he was not", mSignInSys.workingClerks.get(0).busy);
		assertEquals("MarketSignInSys should have 1 waiting customers. It didn't.", 1, mSignInSys.waitingCustomers.size());
		
		//step 4
		assertFalse("MarketSignInSys's scheduler should have returned false, but didn't.", 
				mSignInSys.pickAndExecuteAction());
		
		mSignInSys.workingClerks.get(0).busy = false;
		
		assertTrue("MarketSignInSys's scheduler should have returned true, but didn't.", 
				mSignInSys.pickAndExecuteAction());
		
		assertTrue("MarketCustomer should have logged \"received msgGoToClerk\" but didn't. His log reads instead: " 
				+ mockMarketCustomer.log.getLastLoggedEvent().toString(), mockMarketCustomer.log.containsString("Received msgGoToClerk from mSignInSys. mClerk is clerk"));
		
		assertEquals("MarketSignInSys should have 0 waiting customers. It didn't.", 0, mSignInSys.waitingCustomers.size());
		
		assertTrue("The working clerk should be busy. But he wasn't", mSignInSys.workingClerks.get(0).busy);
		
		//step 5
		assertFalse("MarketSignInSys's scheduler should have returned false when there was no working clerk, but didn't.", 
				mSignInSys.pickAndExecuteAction());
		
		mSignInSys.msgImAvailable(mockMarketClerk);//send the message from a mClerk
		
		assertFalse("The working clerk should not be busy. But he was", mSignInSys.workingClerks.get(0).busy);
		
		
		assertFalse("MarketSignInSys's scheduler should have returned false, but didn't.", 
				mSignInSys.pickAndExecuteAction());
		
	}//end one normal scenario
}
