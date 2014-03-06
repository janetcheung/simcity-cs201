package city;

import city.gui.TruckGui;
import city.market.Market;
import city.restaurant.Restaurant;
import astar.Moveable;
import astar.MovementManager;

public class Truck extends Moveable{
	private static final int TRUCK_MAX=1;
	private static final int TRUCK_SIZE=2;
	
	private final TruckGui gui;
	private final Market home;
	
	public Truck(String name,MovementManager m,Market mark,int r,int c){
		super(name,m,r,c);
		gui=new TruckGui(this);
		home=mark;
	}
	
	protected boolean pickAndExecuteAction(){
		return false;
	}
	
	public void msgGoToRestaurant(Restaurant r){
		
	}
	
	protected void actMoveStep(int r,int c){
		
	}
	
	protected void actReactToBlockedRoute(){
		this.actWaitRandomAndTryAgain();
	}
	
	protected void actArrivedAtDestination(){
		
	}

	protected void destructor(){
		// TODO Auto-generated method stub
		
	}
}
