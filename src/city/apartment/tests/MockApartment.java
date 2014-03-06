package city.apartment.tests;

import java.awt.geom.Rectangle2D.Double;

import agent.Agent.BlockingData;
import astar.GridCell;
import astar.MovementManager;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.gui.BuildingGui;
import UnitTestingCommon.Mock;
import UnitTestingCommon.interfaces.ApartmentInterface;
import UnitTestingCommon.interfaces.PersonInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import UnitTestingCommon.interfaces.RenterInterface;



public class MockApartment extends Mock implements ApartmentInterface {

	public boolean closed;
	
	public MockApartment(String name) {
		super(name);
		closed = true;
	}

	
	@Override
	public BuildingGui getGui() {
		return null;
	}
	@Override
	public GridCell getEntrance(boolean hasCar) {
		return null;
	}
	@Override
	public Double getRect() {
		return null;
	}
	@Override
	public MovementManager getMovementManager() {
		return null;
	}
	@Override
	public void msgAskIfBuildingIsClosed(BlockingData<Boolean> result) {}
	@Override
	public void msgForceClose() {}
	@Override
	public void msgReleaseFromForceClose() {}
	@Override
	public void msgSpawnPuppet(BlockingData<PuppetInterface> result, String name, Setup setupPackage) {}
	@Override
	public void msgFillAnyOpening(BlockingData<Integer> timeSlotIndex, BlockingData<PuppetType> jobType) {}
	@Override
	public void msgFillSpecifiedOpening(BlockingData<Integer> timeSlotIndex, PuppetType jobType) {}
	@Override
	public void msgClearAllOpenings() {}
	@Override
	public void msgUpdateTime(long time) {}

	@Override
	public void msgRenterLeft(RenterInterface r) {
		System.out.println(this.getAgentName() + ": received msgRenterLeft()");
	}

	@Override
	public void msgTenantPayingRent(PersonInterface p, double rent) {}

	
	
}