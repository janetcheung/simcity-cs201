package UnitTestingCommon.interfaces;

import agent.Agent.BlockingData;
import city.Broadcaster.BroadcastReceiver;
import city.bus.BusStop;
import city.bus.interfaces.BusStopInterface;


public interface PersonInterface extends MoveableInterface,BroadcastReceiver {
	
	
	public void msgAddMoney(final double amount);
	public void msgUpdateMoney(final double amount);
	public void msgUpdateAccountBalance(final double balance);
	public void msgAteFood();
	public void msgPutAwayGroceries();
	public void msgUpdateInventory(final int[] quantities);
	public void msgPuppetLeftBuilding();
	public void msgRideBus();
	public void msgBusArrived(final BlockingData<Boolean> gettingOff,final BusStopInterface b);
	
	
	
}