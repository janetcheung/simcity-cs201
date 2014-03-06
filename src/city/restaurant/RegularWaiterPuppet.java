package city.restaurant;

import java.util.HashMap;

import UnitTestingCommon.interfaces.RegularWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;



public class RegularWaiterPuppet extends AbstractWaiterPuppet implements RegularWaiterPuppetInterface {
	
	
	
	
	public RegularWaiterPuppet(RestaurantParticipantPuppet.Setup s, HashMap<String,Double> menu) {
		super(s, menu);
	}

	
	
	
	
	@Override
	protected void actPlaceCustomerOrder(RestaurantCustomerPuppetInterface c, int foodIndex) {
		cook.msgCustomerPlacedOrder(this, c, foodIndex);
	}
	
	
}