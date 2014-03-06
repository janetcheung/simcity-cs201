package city.restaurant.tests;

import java.util.ArrayList;

import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import agent.Agent;
import agent.Agent.BlockingData;
import astar.MovementManager;
import city.Person;
import city.Puppet;
import city.Puppet.PuppetType;
import city.apartment.Renter;
import city.market.Market.Food;
import city.restaurant.HostSocket;
import city.restaurant.RestaurantCustomerPuppet;
import city.restaurant.RestaurantParticipantPuppet;
import junit.framework.TestCase;

public class HostUnitTests extends TestCase {

    
	
	static final int numTables = 10;
	
	
	
	HostSocket host;
	boolean schedulerReturnValue;
	
    
    
    

    public void setUp() throws Exception {
            super.setUp();
            
            MockRestaurant mockRestaurant = new MockRestaurant("MockRestaurant");
            host = new HostSocket("HostUnderTest", new MovementManager("dummyManager", 0, 0), mockRestaurant, numTables);
            

            Agent.setJUNIT(true);
    }
    
    
    
    /*
	public void msgCustomerCheckingIn(final RestaurantCustomerPuppetInterface c);
	public void msgWaiterTookCustomer();
	public void msgWaiterBeganShift(final AbstractWaiterPuppetInterface w);
	public void msgWaiterEndedShift(final AbstractWaiterPuppetInterface w, final BlockingData<Boolean> blocker);
	public void msgCustomerLeaving(final int tableID);
    */
    
    /*
	public boolean needToCheckFrontOfLine;
	public ArrayList<RestaurantCustomerPuppetInterface> customersToSeat;
	public ArrayList<AbstractWaiterPuppetInterface> waitersToUse;
	public ArrayList<Integer> tablesFree;
	*/
    
    /*
    if (worker != null) {
		if (needToCheckFrontOfLine && !customerIsWithMe) {
			this.actCheckFrontOfLine();
			return true;
		}
		if (customersToSeat.size() > 0 && waitersToUse.size() > 0 && tablesFree.size() > 0) {
			this.actMakeCustomerAssignment();
			return true;
		}
	}*/
    
    
    
    
    

    /////////TEST 1///////////////////////////

    
    public void testNoCustomers() {
    	// There are waiters and tables but no customers. Host should do nothing.
        System.out.println("*****TEST FOR HOST NO CUSTOMERS*****");
    	
        assertEquals("Host should NOT have any customers. It DOES.", 0, host.customersToSeat.size());
        assertEquals("Host should NOT have any waiters. It DOES.", 0, host.waitersToUse.size());
        assertEquals("Host should NOT have any free tables. It DOES.", 0, host.tablesFree.size());
        
    	
        
        
    }
     

    


}
