package city.market.test;

import java.util.HashMap;

import agent.Agent;
import astar.MovementManager;
import junit.framework.TestCase;
import city.Puppet.Setup;
import city.market.Market;
import city.market.MarketClerk;
import city.market.MarketClerk.AgentEvent;
import city.market.MarketClerk.AgentState;
import city.market.MarketSignInSys;
import city.market.Position;
import city.market.gui.MarketClerkGui;
import city.market.test.mock.MockMarket;
import city.market.test.mock.MockMarketClerk;
import city.market.test.mock.MockMarketCustomer;
import city.market.test.mock.MockMarketSignInSys;

public class MarketClerkTest extends TestCase {
	//these are instantiated for each test separately via the setUp() method.
	MarketClerk mClerk;
	MockMarket mockMarket;
	MockMarketSignInSys mockMarketSignInSys;
	MockMarketCustomer mockMarketCustomer;
	
	MovementManager m;
	Setup s;
	Market market;
	
	MarketClerkGui mClerkGui;
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp();
		
		m = new MovementManager("movementManager", 0, 0);
		s = new Setup();
		market = new Market("Market", 0, 0, 0, 0, 0, 0, 0, 0);
		
		
		mClerk = new MarketClerk("clerk", m, market, s);
		mockMarket = new MockMarket("market");
		mockMarketSignInSys = new MockMarketSignInSys("signIn");	
		mockMarketCustomer = new MockMarketCustomer("customer");
		
		//mClerkGui = new MarketClerkGui(mClerk);
		
		mClerk.setMarket(mockMarket);
		mClerk.setMarketSignInSys(mockMarketSignInSys);
		
		//mClerk.setGui(mClerkGui);
		
		//mClerk.underTest();
		Agent.setJUNIT(true);
	}
	
	
	public void testOneNormalScenario()
	{
		//setUp() runs first before this test!
		//mSignInSys.startAgent();
		
		// set up test data
		mClerk.addresses.put("Steak", new Position(5, 6));
		mClerk.addresses.put("Chicken", new Position(5, 11));
		mClerk.addresses.put("Salad", new Position(5, 16));
		mClerk.addresses.put("Pizza", new Position(5, 21));
		
		mockMarket.priceList.put("Steak", 15.99);
		mockMarket.priceList.put("Chicken", 10.99);
		mockMarket.priceList.put("Salad", 5.99);
		mockMarket.priceList.put("Pizza", 8.99);
		
		
		HashMap<String, Integer> groceryList = new HashMap<String, Integer>();
		{
			groceryList.put("Steak", 2);
			groceryList.put("Chicken", 2);
			groceryList.put("Salad", 2);
			groceryList.put("Pizza", 2);
		}
		
		//check preconditions
		// correct order expected:<> was:<> doesn't matter if they are equal
		assertEquals("state should be Entering. it wasn't.", AgentState.Initial, mClerk.state);
		assertEquals("event should be none. it wasn't", AgentEvent.none, mClerk.event);
		
		assertEquals("MockMarket should have an empty event log. Instead, the MockMarket event log reads: "
				+ mockMarket.log.toString(), 0, mockMarket.log.size());
		assertEquals("MockMarketCustomer should have an empty event log. Instead, the MockMarketCustomer's event log reads: "
				+ mockMarketCustomer.log.toString(), 0, mockMarketCustomer.log.size());
		assertEquals("MockMarketSignInSys should have an empty event log. Instead, the MockMarketSignInSys event log reads: "
				+ mockMarketSignInSys.log.toString(), 0, mockMarketSignInSys.log.size());
		
		//gui state test
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		assertEquals("state should be Entering. it wasn't.", AgentState.Entering, mClerk.state);
		
		
		mClerk.event = AgentEvent.finishEntering;
		
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		assertEquals("state should be Available. it wasn't.", AgentState.Available, mClerk.state);
		
		//added step of the test
		assertFalse("scheduler should have returned false, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		mClerk.msgImComing();
		assertEquals("event should be none. it wasn't", AgentEvent.customerComing, mClerk.event);
		
		//
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		assertEquals("state should be Idle. it wasn't.", AgentState.Idle, mClerk.state);
		
		
		//step 1 of the test
		assertFalse("scheduler should have returned false, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		mClerk.msgThisIsMyOrder(mockMarketCustomer, groceryList);//send the message from a mCustomer
		
		assertEquals("myCustomer set wrong", mockMarketCustomer.getName(), mClerk.myCustomer.getName());
		assertEquals("costomerOrder set wrong", groceryList, mClerk.customerOrder);
		assertEquals("event should be orderRequestFromCustomer. it wasn't", AgentEvent.orderRequestFromCustomer, mClerk.event);
		
		//step 2 of the test
		//assertEquals("state should be GoChecking. it wasn't.", AgentState.GoChecking, mClerk.state);
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		assertEquals("state should be GoChecking. it wasn't.", AgentState.CheckingInStock, mClerk.state);
		
		assertTrue("MockMarket should have logged \"received msgCheckInStock\" but didn't. His log reads instead: " 
				+ mockMarket.log.getLastLoggedEvent().toString(), mockMarket.log.containsString("Received msgCheckInStock from mClerk. customer order is " + groceryList));
		
		//step 3
		assertFalse("scheduler should have returned false, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		groceryList.put("Pizza", 1);// updated by market
		
		mClerk.msgItemAvailability(groceryList);//send the message from market
		
		assertEquals("costomerOrder set wrong", groceryList, mClerk.customerOrder);
		assertEquals("event should be itemAvailabilityResponse. it wasn't", AgentEvent.itemAvailabilityResponse, mClerk.event);
		
		//step 4
		//assertEquals("state should be Return. it wasn't.", AgentState.Return, mClerk.state);
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		assertEquals("state should be Return. it wasn't.", AgentState.GoFetchingItems, mClerk.state);
		
		/*
		
		for (String key : groceryList.keySet()) {
			assertEquals("deep copy customerList to itemsInventory error", groceryList.get(key), mClerk.itemsInventory.get(key));
		}
		
		assertTrue("MockMarketCustomer should have logged \"received msgHereIsBill\" but didn't. His log reads instead: " 
				+ mockMarketCustomer.log.getLastLoggedEvent().toString(), mockMarketCustomer.log.containsString("Received msgHereIsBill from mClerk. Available items are " + groceryList + ", prices are " + mClerk.priceList));
		*/
		
		// gui state test
		mClerk.event = AgentEvent.finishFetching;
		
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		assertEquals("state should be Cashier. it wasn't.", AgentState.Cashier, mClerk.state);
		
		
		//step 5
		assertFalse("scheduler should have returned false, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		groceryList.put("Salad", 1);// updated by mCustomer
		groceryList.put("Pizza", 0);// updated by mCustomer
		double cash = 59.95;// computed by mCustomer
		
		mClerk.msgHereIsMyDecision(groceryList, cash);//send the message from mCustomer
		
		assertEquals("costomerOrder set wrong", groceryList, mClerk.customerOrder);
		assertEquals("costomerPayment set wrong", cash, mClerk.customerPayment);
		assertEquals("event should be customerPaying. it wasn't", AgentEvent.customerPaying, mClerk.event);
		
		//step 6
		//assertEquals("state should be Cashier. it wasn't.", AgentState.Cashier, mClerk.state);
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		assertEquals("state should be CheckingPaymentGoRestocking. it wasn't.", AgentState.CheckingPaymentGoRestocking, mClerk.state);
		
		// gui state test goRestock
		mClerk.event = AgentEvent.finishRestocking;
		
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		
		assertEquals("state should be UpdatingStock. it wasn't.", AgentState.UpdatingStock, mClerk.state);
		
		
		// updateStock
		// erase 0 items (Pizza)
		groceryList.remove("Pizza");
		
		for (String key : groceryList.keySet()) {
			assertEquals("deep copy customerOrder error", groceryList.get(key), mClerk.customerOrder.get(key));
		}
		
		assertTrue("MockMarketCustomer should have logged \"received msgThankYouGoodbye\" but didn't. His log reads instead: " 
				+ mockMarketCustomer.log.getLastLoggedEvent().toString(), mockMarketCustomer.log.containsString("Received msgThankYouGoodbye from mClerk."));
		
		HashMap<String, Integer> updatedInventoryList = new HashMap<String, Integer>();
		{
			groceryList.put("Steak", 0);
			groceryList.put("Chicken", 0);
			groceryList.put("Salad", 1);
			groceryList.put("Pizza", 1);
		}
		for (String key : updatedInventoryList.keySet()) {
			assertEquals("itemsInventory modification error", updatedInventoryList.get(key), mClerk.itemsInventory.get(key));
		}
		
		assertTrue("MockMarket should have logged \"received msgItemRestock\" but didn't. His log reads instead: " 
				+ mockMarket.log.getLastLoggedEvent().toString(), mockMarket.log.containsString("Received msgItemRestock from mClerk. restocking items are " + mClerk.itemsInventory));
		
		assertTrue("MockMarket should have logged \"received addPublicMoney\" but didn't. His log reads instead: " 
				+ mockMarket.log.getLastLoggedEvent().toString(), mockMarket.log.containsString("addPublicMoney called. amount = " + cash));
		
		assertEquals("event should be stockUpdated. it wasn't", AgentEvent.stockUpdated, mClerk.event);
		
		//step 7 updateSignIn
		assertTrue("scheduler should have returned true, but didn't.", 
				mClerk.pickAndExecuteAction());
		assertEquals("state should be Available. it wasn't.", AgentState.Available, mClerk.state);
		
		assertEquals("myCustomer set wrong", null, mClerk.myCustomer);
		/*
		assertEquals("MockMarketSignInSys should have 1 loggedEvent. Instead, the MockMarketSignInSys event log reads: "
				+ mockMarketSignInSys.log.toString(), 1, mockMarketSignInSys.log.size());
		*/
		// the first message is msgUpdateSignIn right after the clerk arrived at his home position
		assertEquals("MockMarketSignInSys should have 2 loggedEvent. Instead, mockMarketSignInSys.log.size() = "
				+ mockMarketSignInSys.log.size(), 2, mockMarketSignInSys.log.size());
		
		assertTrue("MockMarketSignInSys should have logged \"received msgImAvailable\" but didn't. His log reads instead: " 
				+ mockMarketSignInSys.log.getLastLoggedEvent().toString(), mockMarketSignInSys.log.containsString("Received msgImAvailable from mClerk. clerk is " + mClerk.getName()));
		//assertEquals("myCustomer set wrong", null, mClerk.myCustomer);
		
		assertFalse("scheduler should have returned false, but didn't.", 
				mClerk.pickAndExecuteAction());
		
	}//end one normal scenario
}
