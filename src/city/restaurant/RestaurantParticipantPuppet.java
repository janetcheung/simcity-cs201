package city.restaurant;


import UnitTestingCommon.interfaces.CookSocketInterface;
import UnitTestingCommon.interfaces.HostSocketInterface;
import UnitTestingCommon.interfaces.RestaurantCashierSocketInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import UnitTestingCommon.interfaces.RestaurantParticipantPuppetInterface;
import astar.MovementManager;
import city.Puppet;
import city.restaurant.gui.RestaurantParticipantGui;




public abstract class RestaurantParticipantPuppet extends Puppet implements RestaurantParticipantPuppetInterface {

	// DATA
	
	protected HostSocketInterface host;
	protected CookSocketInterface cook;
	protected RestaurantCashierSocketInterface cashier;
	protected RestaurantParticipantGui gui;
	
	
	
	// ENUMS AND CLASSES
	
	public static class Setup extends Puppet.Setup {
		public String name;
		public MovementManager manager;
		public RestaurantInterface restaurant;
		public HostSocketInterface host;
		public CookSocketInterface cook;
		public RestaurantCashierSocketInterface cashier;
		public void copyData(String name, MovementManager m, RestaurantInterface r, HostSocketInterface host, CookSocketInterface cook, RestaurantCashierSocketInterface cashier) {
			this.name = name;
			this.manager = m;
			this.restaurant = r;
			this.host = host;
			this.cook = cook;
			this.cashier = cashier;
		}
	}
	
	
	
	
	// CONSTRUCTOR ///////////////////////////////////////////////////////////////////////////////////
	
	public RestaurantParticipantPuppet(Setup s) {
		super(s.name, s.manager, s.restaurant, s);
		this.host = s.host;
		this.cook = s.cook;
		this.cashier = s.cashier;
		gui = null;
	}
	
	
	
	// Only use before starting the thread of the Puppet
	public void setGui(RestaurantParticipantGui g) {
		if (!this.threadRunning()) {
			gui = g;
		}
	}
	
	public RestaurantParticipantGui getGui() {
		return gui;
	}
	
	
	
	
	@Override
	// This shouldn't be used by the Person master; instead, the restaurant itself keeps track of when a shift ends, and dismisses its workers accordingly
	public final void msgLeaveBuilding() {}
	
	
	
	// ACTIONS
	
	@Override
	protected void actMoveStep(int r, int c) {
		gui.setDestination(r, c);
	}
	
	protected void actTerminateParticipation() {
		/* Five tasks for termination:
		 * 		1. Notify Person master of any changes to Person data (may be different depending on subclass of this)
		 * 		2. Notify Person master that this Puppet is done
		 * 		3. Remove from movement manager
		 * 		4. Stop Puppet thread
		 * 		5. Remove gui from animation panel (handled by call to Restaurant's msgParticipantLeft())
		 */
		this.getMaster().msgPuppetLeftBuilding();
		this.removeFromManager();
		this.stopAgent();
		((RestaurantInterface)this.getBuilding()).msgParticipantLeft(this);
	}
	
	
	
}