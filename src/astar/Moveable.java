package astar;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import agent.Agent;

public abstract class Moveable extends Agent{
	private final MovementManager movementGrid;
	private final AgentSem stepInProgress;
	private final Timer waitTimer;
	private final Random generator;
	
	boolean isOnJourney;
	private int currentRow;
	private int currentCol;
	private int currentDestRow;
	private int currentDestCol;
	
	public Moveable(String name,MovementManager m,int r,int c){
		super(name);
		movementGrid=m;
		stepInProgress=new AgentSem(0);
		waitTimer=new Timer();
		generator=new Random();
		isOnJourney=false;
		currentRow=currentDestRow=r;
		currentCol=currentDestCol=c;
	}
	
	/* these methods should be final so they can't be overridden */
	
	public final void msgNoRouteToDestination(){
		enqueMutation(new Mutation(){
			public void apply(){
				isOnJourney=false;
				actReactToBlockedRoute();
			}
		});
	}
	
	public final void msgAlreadyAtDestination(){
		enqueMutation(new Mutation(){
			public void apply(){
				isOnJourney=false;
				actArrivedAtDestination();
			}
		});
	}
	
	public final void msgMoveStep(final int r,final int c){
		enqueMutation(new Mutation(){
			public void apply(){
				actMoveStep(r,c);
				stepInProgress.acquire();
				currentRow=r;
				currentCol=c;
				isOnJourney=true;
			}
		});
	}
	
	/* if this is the last step before destination is reached */
	public final void msgLastStep(final int r,final int c){
		enqueMutation(new Mutation(){
			public void apply(){
				actMoveStep(r,c);
				stepInProgress.acquire();
				currentRow=r;
				currentCol=c;
				isOnJourney=false;
				actArrivedAtDestination();
			}
		});
	}
	
	/* should be called by gui */
	public final void msgFinishedStep(){
		enqueMutation(new Mutation(){
			public void apply(){
				movementGrid.msgReadyForNextMove(Moveable.this);
				return;
			}
		});
		stepInProgress.release();
	}
	
	private final void msgGoToDest(){
		enqueMutation(new Mutation(){
			public void apply(){
				isOnJourney=true;
				movementGrid.msgMoveMeTo(Moveable.this,currentDestRow,currentDestCol);
			}
		});
	}
	
	private final void msgCollision(){
		enqueMutation(new Mutation(){
			public void apply(){
				//actCollision();
			}
		});
	}
	
	//private
	
	protected final boolean isTraveling(){
		return isOnJourney;
	}
	
	protected final void addToManager(int max,int size,int r,int c){
		movementGrid.msgAddUnit(this,max,size,r,c);
	}
	
	protected final void removeFromManager(){
		movementGrid.msgRemoveUnit(this);
	}
	
	protected final void setNewDestination(int r,int c){
		isOnJourney=true;
		currentDestRow=r;
		currentDestCol=c;
		movementGrid.msgMoveMeTo(this,r,c);
	}
	
	protected final int destRow(){
		return currentDestRow;
	}
	
	protected final int destCol(){
		return currentDestCol;
	}
	
	protected final int getRow(){
		return currentRow;
	}
	
	protected final int getCol(){
		return currentCol;
	}
	
	/*
	 *  Helps tell Moveables how close they are to their destination
	 * E.g. a customer would use this to check if he's 1 step away from the host, which counts as arriving at the host
	 */
	protected final int getDistanceToDestinationInCells(boolean diagonalsAreAllowed) {
		if (diagonalsAreAllowed) {
			return Math.max(Math.abs(currentRow-currentDestRow), Math.abs(currentCol-currentDestCol));
		}
		else {
			return Math.abs(currentRow-currentDestRow) + Math.abs(currentCol-currentDestCol);
		}
	}
	
	protected final void actWaitRandomAndTryAgain(){
		waitTimer.schedule(new TimerTask(){
			public void run(){
				msgGoToDest();
			}
		},50+generator.nextInt(51));
	}
	
	protected abstract void actMoveStep(int r,int c);
	protected abstract void actReactToBlockedRoute();
	protected abstract void actArrivedAtDestination();
	//protected abstract void actCollision();
}
