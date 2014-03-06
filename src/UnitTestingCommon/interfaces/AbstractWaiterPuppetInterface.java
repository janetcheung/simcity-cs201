package UnitTestingCommon.interfaces;

import java.util.HashMap;
import city.Bill;


public interface AbstractWaiterPuppetInterface extends RestaurantWorkerPuppetInterface {
	
	
	
	public void msgNewCustomerAssignment(final RestaurantCustomerPuppetInterface c, final int tableID);
	public void msgCustomerOrdered(final RestaurantCustomerPuppetInterface c, final String foodName);
	public void msgCookCannotDoOrder(final RestaurantCustomerPuppetInterface c, final HashMap<String,Double> menuUpdate);
	public void msgCookAcceptedOrder(final RestaurantCustomerPuppetInterface c, final HashMap<String,Double> menuUpdate);
	public void msgCookFinishedOrder(final RestaurantCustomerPuppetInterface c);
	public void msgCookGaveOrder(final RestaurantCustomerPuppetInterface c);
	public void msgCashierUpdatedBill(final RestaurantCustomerPuppetInterface c);
	public void msgCustomerWantsBill(final RestaurantCustomerPuppetInterface c);
	public void msgCashierGaveBill(final Bill bill);
	public void msgCustomerLeavingRestaurant(final RestaurantCustomerPuppetInterface c);
	
	
	
	
	
}