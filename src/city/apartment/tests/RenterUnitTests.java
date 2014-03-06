package city.apartment.tests;

import agent.Agent;
import astar.MovementManager;
import city.Person;
import city.Puppet;
import city.Puppet.PuppetType;
import city.apartment.Renter;
import city.market.Market.Food;
import junit.framework.TestCase;

public class RenterUnitTests extends TestCase {

    
	
	
	MockApartment mockApartment;
	MovementManager dummyManager;
	Renter renter;
	Puppet.Setup puppetSetup;
	
	boolean schedulerReturnValue;
	
    
    
    

    public void setUp() throws Exception {
            super.setUp();
            
            mockApartment = new MockApartment("MockApartment");
            dummyManager = new MovementManager("dummyManager", 0, 0);
            renter = null;
            
            puppetSetup = new Puppet.Setup();
            puppetSetup.master = new Person("DummyMaster", null, 0, 0, new Person.Setup());
            puppetSetup.role = PuppetType.resident;
            puppetSetup.foodInventory = new int[Food.getNumberOfFoods()];
            for (int i = 0; i < Food.getNumberOfFoods(); i++) {
            	puppetSetup.foodInventory[i] = 0;
            }
            puppetSetup.foodInventory[Food.mustard.index] = 3;
            puppetSetup.foodInventory[Food.asparagus.index] = 3;
            puppetSetup.foodInventory[Food.bottleOfRum.index] = 3;
            puppetSetup.foodInventory[Food.butter.index] = 3;
            puppetSetup.apartmentRoomNumber = 0;

            Agent.setJUNIT(true);
    }
    
    
    
    
    
    
    

    /////////TEST 1///////////////////////////

    
    public void testNormalFlow() {
    	// Renter should put groceries in fridge, get food to cook from fridge, cook food, eat food, go to bathroom, go to bed
        System.out.println("*****TEST FOR RENTER NORMAL FLOW*****");
    	
    	puppetSetup.boughtNewGroceries = true;
        puppetSetup.minutesMealDuration = 10;
        renter = new Renter("RenterUnderTest", dummyManager, mockApartment, puppetSetup);
    	
        int startingInventorySize = renter.getTotalFoodInventoryCount();
        
        assertEquals("Renter should have initial state. It doesn't.", Renter.State.initial, renter.state);
        assertTrue("Renter should NOT be doing a visit right now. It IS.", renter.doneWithVisit);
        
        // Should go to put groceries away at fridge
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToPutAwayGroceries. It doesn't.", Renter.State.goingToPutAwayGroceries, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);

        // Should go to get food from fridge
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToGetFoodFromFridge. It doesn't.", Renter.State.goingToGetFoodFromFridge, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);
        assertEquals("Renter should have taken something out of his inventory. He didn't.", startingInventorySize-1, renter.getTotalFoodInventoryCount());
        
        // Should go to cook food at stove
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToCookFoodAtStove. It doesn't.", Renter.State.goingToCookFoodAtStove, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);

        // Should go to eat food at table
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToEatFoodAtTable. It doesn't.", Renter.State.goingToEatFoodAtTable, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);
        
        // Should go to bathroom
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToBathroom. It doesn't.", Renter.State.goingToBathroom, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);
        
        // Should go to bed
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToBed. It doesn't.", Renter.State.goingToBed, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);
        
        // No more to do
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertFalse("Scheduler should have returned false. It didn't.", schedulerReturnValue);
    }
     


    ////////////TEST 2////////////////////////////////

    
    public void testFlowInterruptedByLeaveMessage() {
    	// Renter should go to bathroom, get told to leave, then leave instead of going to bed
    	System.out.println("*****TEST FOR RENTER INTERRUPTED FLOW*****");
    	
    	puppetSetup.boughtNewGroceries = false;
        puppetSetup.minutesMealDuration = 0;
        renter = new Renter("RenterUnderTest", dummyManager, mockApartment, puppetSetup);
        
        assertEquals("Renter should have initial state. It doesn't.", Renter.State.initial, renter.state);
        assertTrue("Renter should NOT be doing a visit right now. It IS.", renter.doneWithVisit);
        
        // Should go to bathroom
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToBathroom. It doesn't.", Renter.State.goingToBathroom, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);
        
        renter.msgLeaveBuilding();
        
        // Should leave
        renter.doneWithVisit = true;
        schedulerReturnValue = renter.callScheduler();
        assertTrue("Scheduler should have returned true. It didn't.", schedulerReturnValue);
        assertEquals("Renter should have state goingToLeave. It doesn't.", Renter.State.goingToLeave, renter.state);
        assertFalse("Renter should be doing a visit right now. It isn't.", renter.doneWithVisit);
    }

    


}
