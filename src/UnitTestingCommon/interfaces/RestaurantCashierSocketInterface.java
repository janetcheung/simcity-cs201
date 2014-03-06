package UnitTestingCommon.interfaces;

import city.restaurant.AbstractWaiterPuppet;
import city.restaurant.RestaurantCustomerPuppet;

public interface RestaurantCashierSocketInterface extends SimpleRestaurantWorkerSocketInterface {
	
	
	public void msgRestaurantGotMoneyFromBank(final double amount);
	public void msgCustomerReceivedOrder(final AbstractWaiterPuppetInterface w, final RestaurantCustomerPuppetInterface c, final String foodChoice);
	public void msgCustomerWantsBill(final AbstractWaiterPuppetInterface w, final RestaurantCustomerPuppetInterface c);
	public void msgCustomerPaidBill(final RestaurantCustomerPuppetInterface c, final double amount);
	
	
	
}