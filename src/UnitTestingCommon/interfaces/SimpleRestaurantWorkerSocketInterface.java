package UnitTestingCommon.interfaces;


public interface SimpleRestaurantWorkerSocketInterface extends AgentInterface {
	
	
	public void msgWorkerArrived(final SimpleRestaurantWorkerPuppetInterface w);
	public void msgWorkerLeft(final SimpleRestaurantWorkerPuppetInterface w);
	
	
	
}