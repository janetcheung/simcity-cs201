package city.restaurant;

import UnitTestingCommon.interfaces.HostPuppetInterface;






public class HostPuppet extends SimpleRestaurantWorkerPuppet implements HostPuppetInterface {
	
	
	
	
	public HostPuppet(Setup s, HostSocket socket) {
		super(s, socket, Restaurant.locHost);
	}
	
	
	
	
	
	
}