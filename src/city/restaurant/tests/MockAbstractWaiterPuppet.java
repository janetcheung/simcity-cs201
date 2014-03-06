package city.restaurant.tests;

import java.util.HashMap;

import city.Bill;
import city.restaurant.gui.RestaurantParticipantGui;
import UnitTestingCommon.Mock;
import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;



public class MockAbstractWaiterPuppet extends Mock implements AbstractWaiterPuppetInterface {

	public MockAbstractWaiterPuppet(String name) {
		super(name);
	}

	
	
	
	
	
	@Override
	public void msgTakePayAndLeave(double amount) {}
	@Override
	public RestaurantParticipantGui getGui() {
		return null;
	}
	@Override
	public void msgLeaveBuilding() {}
	@Override
	public void msgLastStep(int r, int c) {}
	@Override
	
	public void msgNewCustomerAssignment(RestaurantCustomerPuppetInterface c, int tableID) {
		System.out.println(this.getAgentName() + ": received msgNewCustomerAssignment");
	}

	@Override
	public void msgCustomerOrdered(RestaurantCustomerPuppetInterface c, String foodName) {}
	@Override
	public void msgCookCannotDoOrder(RestaurantCustomerPuppetInterface c, HashMap<String, Double> menuUpdate) {}
	@Override
	public void msgCookAcceptedOrder(RestaurantCustomerPuppetInterface c, HashMap<String, Double> menuUpdate) {}
	@Override
	public void msgCookFinishedOrder(RestaurantCustomerPuppetInterface c) {}
	@Override
	public void msgCookGaveOrder(RestaurantCustomerPuppetInterface c) {}
	@Override
	public void msgCashierUpdatedBill(RestaurantCustomerPuppetInterface c) {}
	@Override
	public void msgCustomerWantsBill(RestaurantCustomerPuppetInterface c) {}
	@Override
	public void msgCashierGaveBill(Bill bill) {}
	@Override
	public void msgCustomerLeavingRestaurant(RestaurantCustomerPuppetInterface c) {}

	
	
	

	
	
	
	
	
	
	
	
	
}