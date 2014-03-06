package city.bus;

import java.util.ArrayList;
import java.util.List;

import trace.AlertLog;
import trace.AlertTag;
import city.Broadcaster;
import city.bus.interfaces.BusStopInterface;
import city.gui.CityGui;
import city.gui.BusGui;
import UnitTestingCommon.interfaces.PersonInterface;
import astar.Moveable;
import astar.MovementManager;

public class Bus extends Moveable{
	private static final int BUS_SQUARE_MAX=1;
	private static final int BUS_SIZE=2;
	private static final int CAPACITY=8;

	private final BusGui gui;
	private final List<PersonInterface> passengers;
	private final BusStopInterface[] busStops;

	private int stopIndex;

	public List<PersonInterface> getPassengers() {
		return passengers;
	}

	public int getAmountOfPassengers(){
		return passengers.size();
	}

	public Bus(String name, int n, MovementManager m,int r,int c,BusStopInterface[] stops){
		super(name,m,r,c);
		gui=new BusGui(this);
		passengers=new ArrayList<PersonInterface>();
		busStops=stops;

		stopIndex=n;

		this.msgContinueToNextStop();
	}

	public BusGui getGui(){
		return gui;
	}

	protected boolean pickAndExecuteAction(){
		return false;
	}

	public void msgAddPassenger(final PersonInterface p){
		enqueMutation(new Mutation(){
			public void apply(){
				AlertLog.getInstance().logMessage(AlertTag.BUS, getAgentName(), "Picking up " + p.getAgentName() + " at " +busStops[stopIndex]);
				passengers.add(p);
			}
		});
	}

	public void msgContinueToNextStop(){
		enqueMutation(new Mutation(){
			public void apply(){
				stopIndex++;
				if(stopIndex==busStops.length){
					stopIndex=0;
				}
				delay(Broadcaster.getMinuteMillis());
				setNewDestination(busStops[stopIndex].getStopRow(),busStops[stopIndex].getStopCol());
				AlertLog.getInstance().logInfo(AlertTag.BUS, getAgentName(), "Continuing to " + busStops[stopIndex].getAgentName());
			}
		});
	}

	protected void actMoveStep(int r, int c){
		gui.setDestination(r, c);
	}


	protected void actReactToBlockedRoute(){
		actWaitRandomAndTryAgain();        
	}

	protected void actArrivedAtDestination(){
		List<PersonInterface> copy=new ArrayList<PersonInterface>(passengers);
		for(PersonInterface p:copy){
			BlockingData<Boolean> gettingOff=new BlockingData<Boolean>();
			p.msgBusArrived(gettingOff,busStops[stopIndex]);
			if(gettingOff.get()){
				AlertLog.getInstance().logMessage(AlertTag.BUS, getAgentName(), "Dropping off " + p.getAgentName() + " at " +busStops[stopIndex].getAgentName());
				passengers.remove(p);
			}
		}
		busStops[stopIndex].msgBusArrived(this,CAPACITY-passengers.size());
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub

	}
}
