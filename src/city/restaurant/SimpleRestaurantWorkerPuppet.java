package city.restaurant;

import UnitTestingCommon.interfaces.SimpleRestaurantWorkerPuppetInterface;
import astar.GridCell;


public abstract class SimpleRestaurantWorkerPuppet extends RestaurantWorkerPuppet implements SimpleRestaurantWorkerPuppetInterface {

	
	private SimpleRestaurantWorkerSocket socket;
	private GridCell workStation;
	
	
	
	public SimpleRestaurantWorkerPuppet(Setup s, SimpleRestaurantWorkerSocket socket, GridCell workStation) {
		super(s);
		this.socket = socket;
		this.workStation = workStation;
	}

	
	
	
	


	@Override
	protected final void actArrivedAtDestination() {
		routeRetryCounter = 0;
		if (myShiftState == ShiftState.arriving) {
			myShiftState = ShiftState.onShift;
			socket.msgWorkerArrived(this);
		}
		if (myShiftState == ShiftState.leaving){
			this.actTerminateParticipation();
		}
	}
	
	private final void actBeginShift() {
		myShiftState = ShiftState.arriving;
		this.setNewDestination(workStation.r, workStation.c);
	}
	
	private final void actEndShift() {
		socket.msgWorkerLeft(this);
		this.actGoToExit();
	}
	
	
	
	@Override
	protected boolean pickAndExecuteAction() {
		if (myShiftState == ShiftState.justEntered) {
			this.actBeginShift();
			return true;
		}
		if (myShiftState == ShiftState.gotPay) {
			this.actEndShift();
			return true;
		}
		return false;
	}
	
	protected void destructor(){
		
	}
	
}