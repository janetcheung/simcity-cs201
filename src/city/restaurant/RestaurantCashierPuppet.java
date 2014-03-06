package city.restaurant;

import UnitTestingCommon.interfaces.RestaurantCashierPuppetInterface;


public class RestaurantCashierPuppet extends SimpleRestaurantWorkerPuppet implements RestaurantCashierPuppetInterface {
	
	
	
	public RestaurantCashierPuppet(Setup s, RestaurantCashierSocket socket) {
		super(s, socket, Restaurant.locCashier);
	}
	

	
	
	
}