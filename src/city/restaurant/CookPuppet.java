package city.restaurant;

import UnitTestingCommon.interfaces.CookPuppetInterface;




public class CookPuppet extends SimpleRestaurantWorkerPuppet implements CookPuppetInterface {
	
	
	public CookPuppet(Setup s, CookSocket socket) {
		super(s, socket, Restaurant.locCook);
	}	
	
	
	
}