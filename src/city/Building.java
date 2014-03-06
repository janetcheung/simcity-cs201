package city;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import city.Broadcaster.BroadcastReceiver;
import city.Puppet.Setup;
import city.gui.BuildingGui;
import city.gui.CityGui;
import UnitTestingCommon.interfaces.BuildingInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import agent.Agent;
import astar.GridCell;
import astar.MovementManager;

public abstract class Building extends Agent implements BuildingInterface{
	private static final int GRID=CityGui.CITY_GRID_SQUARE;
	
	// PARAMETERS
	
	public static final int insideNumRows=30;
	public static final int insideNumCols= 30;
	private static final WorkTimeSlot[] timeSlots={new WorkTimeSlot(7,12),new WorkTimeSlot(12,17)};
	
	
	
	// DATA
	
	private final Rectangle2D.Double rect;
	private final GridCell personEntrance;
	private final GridCell carEntrance;
	private final MovementManager manager;
	protected ClosedState myClosedState;
	//protected final ArrayList<Agent> pausables;
	protected final BuildingGui gui;
	
	
	
	// ENUMS AND CLASSES
	
	protected enum ClosedState{
		closed,forceClosed,open,needToForceClose;
	};
	
	private static class WorkTimeSlot{
		public final int startHour;
		public final int endHour;
		public WorkTimeSlot(int startHour,int endHour){
			this.startHour=startHour;
			this.endHour=endHour;
		}
	}
	
	// ACCESSORS
	
	public static int getStartHour(int timeSlotIndex){
		return timeSlots[timeSlotIndex].startHour;
	}
	public static int getEndHour(int timeSlotIndex){
		return timeSlots[timeSlotIndex].endHour;
	}
	public static int getNumberOfShifts(){
		return timeSlots.length;
	}
	
	
	
	// CONSTRUCTOR //////////////////////////////////////////////////////////////////////////////
	
	public Building(String name, int pr,int pc,int cr,int cc,MovementManager m,int tlr,int tlc,int brr,int brc, BuildingGui gui){
		super(name);
		personEntrance=new GridCell(pr,pc);
		carEntrance=new GridCell(cr,cc);
		manager=m;
		rect=new Rectangle2D.Double(tlc*GRID,tlr*GRID,(brc-tlc+1)*GRID,(brr-tlr+1)*GRID);
		myClosedState=ClosedState.closed;
		//pausables = new ArrayList<Agent>();
		this.gui = gui;

		if(m!=null){
			manager.startAgent();
		}
	}
	
	public final BuildingGui getGui(){
		return gui;
	}
	
	public final GridCell getEntrance(boolean hasCar){
		return hasCar?carEntrance:personEntrance;
	}
	
	public final Rectangle2D.Double getRect(){
		return rect;
	}
	
	public final MovementManager getMovementManager() {
		return manager;
	}
	
	
	// MESSAGES
	
	public final void msgAskIfBuildingIsClosed(final BlockingData<Boolean> result) {
		enqueMutation(new Mutation() {
			public void apply() {
				result.unblock(myClosedState == ClosedState.closed || myClosedState == ClosedState.forceClosed);
			}
		});	
	}
	public final void msgForceClose() {
		enqueMutation(new Mutation() {
			public void apply() {
				if (myClosedState == ClosedState.closed) {
					myClosedState = ClosedState.forceClosed;
				}
				else if (myClosedState != ClosedState.forceClosed) {
					myClosedState = ClosedState.needToForceClose;
				}
			}
		});	
	}
	public final void msgReleaseFromForceClose() {
		enqueMutation(new Mutation() {
			public void apply() {
				if (myClosedState == ClosedState.forceClosed || myClosedState == ClosedState.needToForceClose) {
					myClosedState = ClosedState.closed;
				}
			}
		});	
	}
	
	public abstract void msgSpawnPuppet(final BlockingData<PuppetInterface> result, final String name, final Setup setupPackage);
	public abstract void msgFillAnyOpening(final BlockingData<Integer> timeSlotIndex, final BlockingData<Puppet.PuppetType> jobType);
	public abstract void msgFillSpecifiedOpening(final BlockingData<Integer> timeSlotIndex, final Puppet.PuppetType jobType);
	public abstract void msgClearAllOpenings();
}
