package city.restaurant.tests;

import java.awt.geom.Rectangle2D.Double;
import java.util.HashMap;

import agent.Agent.BlockingData;
import astar.GridCell;
import astar.MovementManager;
import city.Bill;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.gui.BuildingGui;
import UnitTestingCommon.Mock;
import UnitTestingCommon.interfaces.PuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import UnitTestingCommon.interfaces.RestaurantParticipantPuppetInterface;


public class MockRestaurant extends Mock implements RestaurantInterface {

	public MockRestaurant(String name) {
		super(name);
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
	public void msgFillAnyOpening(BlockingData<Integer> timeSlotIndex,	BlockingData<PuppetType> jobType) {}
	@Override
	public void msgFillSpecifiedOpening(BlockingData<Integer> timeSlotIndex, PuppetType jobType) {}
	@Override
	public void msgClearAllOpenings() {}
	@Override
	public void msgUpdateTime(long time) {}
	@Override
	public void msgParticipantLeft(RestaurantParticipantPuppetInterface p) {}
	@Override
	public void msgCashierNeedsCash(double amount) {}
	@Override
	public void msgCashierGivesCash(double amount) {}
	@Override
	public void msgBankAccountRanOut() {}
	@Override
	public void msgBankSentMoney(double amount) {}
	@Override
	public void msgCookUpdatesInventoryAndMenu(HashMap<String, java.lang.Double> newMenu, int[] inventory) {}
	@Override
	public void msgCookNeedsResupply(int foodIndex, int quantity) {}
	@Override
	public void msgMarketResponse(int foodIndex, int quantityAbleToDeliver) {}
	@Override
	public void msgMarketSentFood(int foodIndex, int quantity) {}
	@Override
	public void msgMarketSentBill(Bill b) {}
	@Override
	public void msgIncrementFoodInventory(int foodIndex) {}
	
	
	
	
	
}