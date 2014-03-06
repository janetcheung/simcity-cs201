package UnitTestingCommon.interfaces;

import agent.Agent.BlockingData;


public interface HostSocketInterface extends SimpleRestaurantWorkerSocketInterface {
	
	
	
	public void msgCheckFrontOfLine();
	public void msgCustomerCheckingIn(final RestaurantCustomerPuppetInterface c);
	public void msgWaiterTookCustomer();
	public void msgWaiterBeganShift(final AbstractWaiterPuppetInterface w);
	public void msgWaiterEndedShift(final AbstractWaiterPuppetInterface w, final BlockingData<Boolean> blocker);
	public void msgCustomerLeaving(final int tableID);
	
	
	
	
	
}