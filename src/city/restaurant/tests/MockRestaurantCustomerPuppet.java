package city.restaurant.tests;

import java.util.HashMap;

import city.Bill;
import city.restaurant.gui.RestaurantParticipantGui;
import UnitTestingCommon.Mock;
import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;



public class MockRestaurantCustomerPuppet extends Mock implements RestaurantCustomerPuppetInterface {

	public MockRestaurantCustomerPuppet(String name) {
		super(name);
	}

	
	
	
	@Override
	public RestaurantParticipantGui getGui() {
		return null;
	}
	@Override
	public void msgLeaveBuilding() {}
	@Override
	public void msgLastStep(int r, int c) {}

	@Override
	public void msgComeToHost() {
		System.out.println(this.getAgentName() + ": received msgComeToHost");
	}

	@Override
	public void msgFollowWaiterToTable(AbstractWaiterPuppetInterface w,	int tableID, HashMap<String, Double> menu) {}
	@Override
	public void msgWaiterSaysToReorder(HashMap<String, Double> menuUpdate) {}
	@Override
	public void msgWaiterDeliveredOrder(String foodChoice) {}
	@Override
	public void msgDoneEating() {}
	@Override
	public void msgWaiterGaveBill(Bill bill) {}
	@Override
	public void msgCashierConfirmedPay() {}
	@Override
	public void msgRanOutOfPatience() {}

	
	
	
	
	
	
	
	
	
}