package UnitTestingCommon.interfaces;

import java.awt.geom.Rectangle2D;

import agent.Agent.BlockingData;
import astar.GridCell;
import astar.MovementManager;
import city.Broadcaster.BroadcastReceiver;
import city.Puppet;
import city.Puppet.Setup;
import city.gui.BuildingGui;

public interface BuildingInterface extends AgentInterface,BroadcastReceiver {
	
	
	public BuildingGui getGui();
	public GridCell getEntrance(boolean hasCar);
	public Rectangle2D.Double getRect();
	public MovementManager getMovementManager();
	
	public void msgAskIfBuildingIsClosed(final BlockingData<Boolean> result);
	public void msgForceClose();
	public void msgReleaseFromForceClose();
	public void msgSpawnPuppet(final BlockingData<PuppetInterface> result, final String name, final Setup setupPackage);
	public void msgFillAnyOpening(final BlockingData<Integer> timeSlotIndex, final BlockingData<Puppet.PuppetType> jobType);
	public void msgFillSpecifiedOpening(final BlockingData<Integer> timeSlotIndex, final Puppet.PuppetType jobType);
	public void msgClearAllOpenings();
	
	
	
}