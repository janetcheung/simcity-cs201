package city.restaurant;

import UnitTestingCommon.interfaces.RestaurantInterface;
import UnitTestingCommon.interfaces.SimpleRestaurantWorkerPuppetInterface;
import UnitTestingCommon.interfaces.SimpleRestaurantWorkerSocketInterface;
import agent.Agent;


public abstract class SimpleRestaurantWorkerSocket extends Agent implements SimpleRestaurantWorkerSocketInterface {
	
	
	protected final RestaurantInterface restaurant;
	protected SimpleRestaurantWorkerPuppetInterface worker;
	
	
	
	
	public SimpleRestaurantWorkerSocket(String name, RestaurantInterface r) {
		super(name);
		restaurant = r;
		worker = null;
	}
	
	
	
	
	
	
	public void msgWorkerArrived(final SimpleRestaurantWorkerPuppetInterface w) {
		enqueMutation(new Mutation() {
			public void apply() {
				worker = w;
			}
		});
	}
	public void msgWorkerLeft(final SimpleRestaurantWorkerPuppetInterface w) {
		enqueMutation(new Mutation() {
			public void apply() {
				if(worker==w){
					worker = null;
				}
			}
		});
	}
	
	public void destructor(){
		
	}
	
}