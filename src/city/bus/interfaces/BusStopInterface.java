package city.bus.interfaces;

import city.Building;
import city.Person;
import city.bus.Bus;
import city.bus.BusStop;;

public interface BusStopInterface {

	public abstract void msgWaitingForBus(final Person p);
	
	public abstract void msgBusArrived(final Bus b,final int num);

	public abstract int getStopRow();
	
	public abstract int getStopCol();

	public abstract String getAgentName();

	public abstract int getBuildingRow();

	public abstract int getBuildingCol();

	public abstract Building getNearBuilding(int i);

	public abstract int getLength();
	
	
}
