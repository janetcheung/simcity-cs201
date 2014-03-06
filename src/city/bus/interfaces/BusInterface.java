package city.bus.interfaces;

import city.Person;
import city.bus.Bus;

public interface BusInterface {

	public abstract void msgAddPassenger(final Person p);
	
	public abstract void msgContinueToNextStop();
	
}
