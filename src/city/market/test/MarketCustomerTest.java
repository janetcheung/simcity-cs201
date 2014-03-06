package city.market.test;

import java.util.HashMap;

import agent.Agent;
import astar.MovementManager;
import junit.framework.TestCase;
import city.Puppet.Setup;
import city.market.Market;
import city.market.Market.Food;
import city.market.MarketCustomer;
import city.market.MarketCustomer.AgentEvent;
import city.market.MarketCustomer.AgentState;
import city.market.MarketSignInSys;
import city.market.Position;
import city.market.test.mock.MockMarketClerk;
import city.market.test.mock.MockMarketCustomer;
import city.market.test.mock.MockMarketSignInSys;

public class MarketCustomerTest extends TestCase {
	//these are instantiated for each test separately via the setUp() method.
	MarketCustomer mCustomer;
	MockMarketSignInSys mockMarketSignInSys;
	MockMarketClerk mockMarketClerk;
	
	MovementManager m;
	Setup s;
	Market market;
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp();
		
		m = new MovementManager("movementManager", 12, 12);
		s = new Setup();
		
		market = new Market("Market", 0, 0, 0, 0, 0, 0, 0, 0);
		s.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
		
		mCustomer = new MarketCustomer("customer", m, market, s);
		mockMarketSignInSys = new MockMarketSignInSys("signIn");
		mockMarketClerk = new MockMarketClerk("clerk");
		
		mCustomer.setMarketSignInSys(mockMarketSignInSys);
		
		//mCustomer.underTest();
		Agent.setJUNIT(true);
	}
	
	
	public void testOneNormalScenario()
	{
		//setUp() runs first before this test!
		//mSignInSys.startAgent();
		
		// set up test data
		mCustomer.groceryList.put("Steak", 2);
		mCustomer.groceryList.put("Chicken", 2);
		mCustomer.groceryList.put("Salad", 2);
		mCustomer.groceryList.put("Pizza", 2);
		
		mCustomer.priorities.add("Steak");
		mCustomer.priorities.add("Chicken");
		mCustomer.priorities.add("Salad");
		mCustomer.priorities.add("Pizza");
		
		// initialize walletMoney
		mCustomer.walletMoney = 63;
		
		//check preconditions
		// correct order expected:<> was:<> doesn't matter if they are equal
		assertEquals("state should be Entering. it wasn't.", AgentState.Initial, mCustomer.state);
		assertEquals("event should be none. it wasn't.", AgentEvent.none, mCustomer.event);
		
		assertEquals("MockMarketClerk should have an empty event log. Instead, the MockMarketClerk's event log reads: "
				+ mockMarketClerk.log.toString(), 0, mockMarketClerk.log.size());
		assertEquals("MockMarketSignInSys should have an empty event log. Instead, the MockMarketSignInSys event log reads: "
				+ mockMarketSignInSys.log.toString(), 0, mockMarketSignInSys.log.size());
		
		// gui state test
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		assertEquals("state should be Entering. it wasn't.", AgentState.Entering, mCustomer.state);
		
		mCustomer.event = AgentEvent.FinishEntering;
		
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		assertEquals("state should be Waiting. it wasn't.", AgentState.Waiting, mCustomer.state);
		
		
		//step 1
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		assertTrue("MockMarketSignInSys should have logged \"received msgWaitingInLine\" but didn't. His log reads instead: " 
				+ mockMarketSignInSys.log.getLastLoggedEvent().toString(), mockMarketSignInSys.log.containsString("Received msgWaitingInLine from mCustomer. customer is " + mCustomer.getName()));
		
		assertEquals("event should be SignedIn. it wasn't.", AgentEvent.SignedIn, mCustomer.event);
		
		//step 2
		assertFalse("scheduler should have returned false, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		mCustomer.msgGoToClerk(mockMarketClerk, new Position(0,0));//send the message from mSignInSys
		
		assertEquals("myClerk set wrong", mockMarketClerk.getName(), mCustomer.myClerk.getName());
		assertEquals("event should be ClerkAvailable. it wasn't.", AgentEvent.ClerkAvailable, mCustomer.event);
		
		//step 3
		//assertEquals("state should be Communicating. it wasn't.", AgentState.Communicating, mCustomer.state);
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		//assertEquals("state should be Communicating. it wasn't.", AgentState.Communicating, mCustomer.state);
		
		assertEquals("state should be MovingToClerk. it wasn't.", AgentState.MovingToClerk, mCustomer.state);
		
		// gui state test 
		
		
		mCustomer.event = AgentEvent.FinishMoving;
		
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		assertEquals("state should be Communicating. it wasn't.", AgentState.Communicating, mCustomer.state);
		
		
		
		assertTrue("MockMarketClerk should have logged \"received msgThisIsMyOrder\" but didn't. His log reads instead: " 
				+ mockMarketClerk.log.getLastLoggedEvent().toString(), mockMarketClerk.log.containsString("Received msgThisIsMyOrder from mCustomer. customer is " + mCustomer.getName() + ", groceryList is " + mCustomer.groceryList));
		
		
		//step 4
		assertFalse("scheduler should have returned false, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		mCustomer.groceryList.put("Pizza", 1);
		
		
		HashMap<String, Double> priceList = new HashMap<String, Double>();	// public for JUnit testing
		{
			priceList.put("Steak", 15.99);
			priceList.put("Chicken", 10.99);
			priceList.put("Salad", 5.99);
			priceList.put("Pizza", 8.99);
		}
		
		mCustomer.msgHereIsBill(mCustomer.groceryList, priceList);//send from mClerk
		
		assertEquals("groceryList set wrong", mCustomer.groceryList, mCustomer.groceryList);
		assertEquals("priceList set wrong", priceList, mCustomer.priceList);
		assertEquals("event should be BillReady. it wasn't.", AgentEvent.BillReady, mCustomer.event);
		
		//step 5
		//assertEquals("state should be DecidingAndPaying. it wasn't.", AgentState.DecidingAndPaying, mCustomer.state);
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		assertEquals("state should be DecidingAndPaying. it wasn't.", AgentState.DecidingAndPaying, mCustomer.state);
		
		HashMap<String, Integer> updatedGroceryList = new HashMap<String, Integer>();
		{
			updatedGroceryList.put("Steak", 2);
			updatedGroceryList.put("Chicken", 2);
			updatedGroceryList.put("Salad", 1);
			updatedGroceryList.put("Pizza", 0);
		}
		for (String key : updatedGroceryList.keySet()) {
			assertEquals("groceryList modification error", updatedGroceryList.get(key), mCustomer.groceryList.get(key));
		}
		
		double paymentAmount = 59.95;
		assertEquals("payment set wrong", (int)paymentAmount, (int)mCustomer.paymentAmount);
		
		double walletMoney = 63 - 59.95;
		assertEquals("payment set wrong", (int)walletMoney, (int)mCustomer.walletMoney);
		
		assertTrue("MockMarketClerk should have logged \"received msgHereIsMyDecision\" but didn't. His log reads instead: " 
				+ mockMarketClerk.log.getLastLoggedEvent().toString(), mockMarketClerk.log.containsString("Received msgHereIsMyDecision from mCustomer. decisionList is " + mCustomer.groceryList + ", cash = " + (int)mCustomer.paymentAmount));
		
		
		//step 6
		assertFalse("scheduler should have returned false, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
		/*
		mCustomer.msgThankYouGoodbye();// send from mClerk
		
		assertEquals("event should be BillPaid. it wasn't.", AgentEvent.BillPaid, mCustomer.event);
		*/
		
		// test data is can not be converted back to the foodsBoughtQuantities array
		mCustomer.event = AgentEvent.BillPaid;
		
		//step 7
		//assertEquals("state should be Leaving. it wasn't.", AgentState.Leaving, mCustomer.state);
		assertTrue("scheduler should have returned true, but didn't.", 
				mCustomer.pickAndExecuteAction());
		assertEquals("state should be Leaving. it wasn't.", AgentState.Leaving, mCustomer.state);
		
		
		assertFalse("scheduler should have returned false, but didn't.", 
				mCustomer.pickAndExecuteAction());
		
	}//end one normal scenario
}
