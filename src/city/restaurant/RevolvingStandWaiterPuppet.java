package city.restaurant;

import java.util.HashMap;

import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import UnitTestingCommon.interfaces.RevolvingStandWaiterPuppetInterface;
import trace.AlertLog;
import trace.AlertTag;



public class RevolvingStandWaiterPuppet extends AbstractWaiterPuppet implements RevolvingStandWaiterPuppetInterface {
	
	
	
	
	private final RevolvingStandMonitor revolvingStand;
	
	
	
	
	
	public RevolvingStandWaiterPuppet(RestaurantParticipantPuppet.Setup s, HashMap<String,Double> menu, RevolvingStandMonitor stand) {
		super(s, menu);
		revolvingStand = stand;
	}
	
	
	
	
	
	
	
	
	@Override
	protected void actPlaceCustomerOrder(RestaurantCustomerPuppetInterface c, int foodIndex) {
		AlertLog.getInstance().logMessage(AlertTag.REST_WAITER, getAgentName(), "Placing customer order on revolving stand");
		revolvingStand.addOrderToQueue(this, c, foodIndex);
	}
	
	
}