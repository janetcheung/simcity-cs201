package city.restaurant;

import UnitTestingCommon.interfaces.RestaurantWorkerPuppetInterface;



public abstract class RestaurantWorkerPuppet extends RestaurantParticipantPuppet implements RestaurantWorkerPuppetInterface {

	
	public static final int maxRouteRetryAttempts = 8;
	
	
	
	protected double payment;
	protected ShiftState myShiftState;
	protected enum ShiftState { justEntered, arriving, onShift, /*timesUp, windingDown, waitingForPay, */gotPay, preparingToLeave, leaving };
	// Give up retrying to get to work station after a certain number of attempts; just start working from current loc
	protected int routeRetryCounter;
	
	
	
	// CONSTRUCTOR ///////////////////////////////////////////////////////////////////
	
	public RestaurantWorkerPuppet(RestaurantParticipantPuppet.Setup s) {
		super(s);
		payment = 0.0;
		myShiftState = ShiftState.justEntered;
		routeRetryCounter = 0;
	}
	
	
	
	
	
	
	
	
	public void msgTakePayAndLeave(final double amount) {
		enqueMutation(new Mutation() {
			public void apply() {
				payment = amount;
				myShiftState = ShiftState.gotPay;
			}
		});
	}
	
	
	
	
	
	
	@Override
	protected final void actReactToBlockedRoute() {
		if (routeRetryCounter < maxRouteRetryAttempts) {
			this.actWaitRandomAndTryAgain();
			routeRetryCounter++;
		}
		else {
			this.actArrivedAtDestination();
		}
	}
	
	
	
	
	
	
	
	protected void actGoToExit() {
		myShiftState = ShiftState.leaving;
		this.setNewDestination(Restaurant.locExit.r, Restaurant.locExit.c);
	}
	
	@Override
	protected void actTerminateParticipation() {
		this.getMaster().msgAddMoney(payment);
		super.actTerminateParticipation();
	}
	
	
	
	
	
}