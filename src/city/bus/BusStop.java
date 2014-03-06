package city.bus;

import java.util.ArrayList;
import java.util.List;

import trace.AlertLog;
import trace.AlertTag;

import agent.Agent;
import city.Building;
import city.Person;
import city.bus.interfaces.BusStopInterface;

public class BusStop extends Agent implements BusStopInterface{
	private final List<Person> people;
	private final Building[] nearestBuildings;
	private final int entranceRow;
	private final int entranceCol;
	private final int stopRow;
	private final int stopCol;

	public BusStop(String name,int r,int c,int sr,int sc,Building[] b){
		super(name);
		people=new ArrayList<Person>();
		nearestBuildings=b;
		entranceRow=r;
		entranceCol=c;
		stopRow=sr;
		stopCol=sc;
	}

	public void msgWaitingForBus(final Person p){
		enqueMutation(new Mutation(){
			public void apply(){
				people.add(p);
				AlertLog.getInstance().logInfo(AlertTag.BUS_STOP, getAgentName(), p.getAgentName() + " is waiting for bus.");
			}
		});
	}

	public void msgBusArrived(final Bus b,final int num){
		enqueMutation(new Mutation(){
			public void apply(){
				for(int i=0;i<num && i<people.size();i++){
					Person p=people.remove(0);
					b.msgAddPassenger(p);
					p.msgRideBus();
					AlertLog.getInstance().logInfo(AlertTag.BUS_STOP, getAgentName(), b.getAgentName() + " has arrived.");
				}
				b.msgContinueToNextStop();
			}
		});
	}
	
	protected boolean pickAndExecuteAction(){
		return false;
	}

	public final int getBuildingRow(){
		return entranceRow;
	}

	public final int getBuildingCol(){
		return entranceCol;
	}
	
	public final int getStopRow(){
		return stopRow;
	}

	public final int getStopCol(){
		return stopCol;
	}
	
	public Building getNearBuilding(int i){
		return nearestBuildings[i];
	}
	
	public int getLength(){
		return nearestBuildings.length;
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
}
