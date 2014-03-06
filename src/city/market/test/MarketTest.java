package city.market.test;

import java.util.HashMap;

import agent.Agent;
import astar.MovementManager;
import junit.framework.TestCase;
import city.market.Market;
import city.market.MarketSignInSys;
import city.market.test.mock.MockMarketClerk;
import city.market.test.mock.MockMarketCustomer;

public class MarketTest extends TestCase {
	//these are instantiated for each test separately via the setUp() method.
	Market market;
	MockMarketClerk mockMarketClerk;
	
	MovementManager m;
	
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp();
		//m = new MovementManager("movementManager", 0, 0);
		
		market = new Market("Market", 0, 0, 0, 0, 0, 0, 0, 0);
		mockMarketClerk = new MockMarketClerk("clerk");	
		
		//market.underTest();
		Agent.setJUNIT(true);
	}
	
	
	public void testOneNormalScenario()
	{
		// add some test items into stock
		market.stock.put("Steak", 2);
		market.stock.put("Chicken", 3);
		market.stock.put("Salad", 3);
		market.stock.put("Pizza", 1);
		
		
		//setUp() runs first before this test!
		//mSignInSys.startAgent();
		
		//check preconditions
		// correct order expected:<> was:<> doesn't matter if they are equal
		assertEquals("Market should have 0 checkingRequests. It didn't.", 0, market.checkingRequests.size());
		assertEquals("Market should have 0 restockingRequests. It didn't.", 0, market.restockingRequests.size());
		//assertEquals("Market should have 0 deliveryRequests. It didn't.", 0, market.deliveryRequests.size());
		
		assertEquals("MockMarketClerk should have an empty event log. Instead, the MockMarketClerk's event log reads: "
				+ mockMarketClerk.log.toString(), 0, mockMarketClerk.log.size());
		
		//step 1
		assertFalse("scheduler should have returned false, but didn't.", market.pickAndExecuteAction());
		
		HashMap<String, Integer> checkingList = new HashMap<String, Integer>();
		{
			checkingList.put("Steak", 2);
			checkingList.put("Chicken", 2);
			checkingList.put("Salad", 2);
			checkingList.put("Pizza", 2);
		}
		
		market.msgCheckInStock(mockMarketClerk, checkingList);;//send the message from a mClerk
		
		assertEquals("Market should have 1 checkingRequests. It didn't.", 1, market.checkingRequests.size());
		assertEquals("mClerk in checkintRequest set wrong", mockMarketClerk, market.checkingRequests.get(0).mClerk);
		assertEquals("items in checkintRequest set wrong", checkingList, market.checkingRequests.get(0).items);
	
		//step 2
		assertFalse("scheduler should have returned false, but didn't.", market.pickAndExecuteAction());
		
		HashMap<String, Integer> updatedStock1 = new HashMap<String, Integer>();
		{
			updatedStock1.put("Steak", 0);
			updatedStock1.put("Chicken", 1);
			updatedStock1.put("Salad", 1);
			updatedStock1.put("Pizza", 0);
		}
		for (String key : updatedStock1.keySet()) {
			assertEquals("stock modification error 1", updatedStock1.get(key), market.stock.get(key));
		}
		
		HashMap<String, Integer> updatedAvailabilityList = new HashMap<String, Integer>();
		{
			updatedAvailabilityList.put("Steak", 2);
			updatedAvailabilityList.put("Chicken", 2);
			updatedAvailabilityList.put("Salad", 2);
			updatedAvailabilityList.put("Pizza", 1);
		}
		for (String key : updatedAvailabilityList.keySet()) {
			assertEquals("checking request items modification error", updatedAvailabilityList.get(key), market.hm.get(key));
		}
		
		assertEquals("Market should have 0 checkingRequests. It didn't.", 0, market.checkingRequests.size());
		
		assertTrue("MockMarketClerk should have logged \"received msgItemAvailability\" but didn't. His log reads instead: " 
				+ mockMarketClerk.log.getLastLoggedEvent().toString(), mockMarketClerk.log.containsString("Received msgItemAvailability from Market. Available items are " + checkingList));
		
		//step 3
		assertFalse("scheduler should have returned false, but didn't.", market.pickAndExecuteAction());
		
		
		HashMap<String, Integer> restockingList = new HashMap<String, Integer>();
		{
			restockingList.put("Steak", 0);
			restockingList.put("Chicken", 0);
			restockingList.put("Salad", 1);
			restockingList.put("Pizza", 1);
		}
		
		market.msgItemRestock(restockingList);;//send the message from a mClerk
		
		assertEquals("Market should have 1 restockingingRequests. It didn't.", 1, market.restockingRequests.size());
		assertEquals("items in restockintRequest set wrong", restockingList, market.restockingRequests.get(0).items);
		
		//step 4
		assertFalse("scheduler should have returned false, but didn't.", market.pickAndExecuteAction());
		
		HashMap<String, Integer> updatedStock2 = new HashMap<String, Integer>();
		{
			updatedStock2.put("Steak", 0);
			updatedStock2.put("Chicken", 1);
			updatedStock2.put("Salad", 2);
			updatedStock2.put("Pizza", 1);
		}
		for (String key : updatedStock2.keySet()) {
			assertEquals("stock modification error 2", updatedStock2.get(key), market.stock.get(key));
		}
		
		assertEquals("Market should have 0 restockingRequests. It didn't.", 0, market.restockingRequests.size());
		
		assertFalse("scheduler should have returned false, but didn't.", market.pickAndExecuteAction());
		
	}//end one normal scenario
}
