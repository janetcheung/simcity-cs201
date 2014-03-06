package city.restaurant;

import java.util.ArrayList;
import java.util.List;

import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;


public class RevolvingStandMonitor {
	private final List<Order> orderQueue;
	
	public RevolvingStandMonitor() {
		orderQueue = new ArrayList<Order>();
	}
	
	public synchronized void addOrderToQueue(AbstractWaiterPuppetInterface w, RestaurantCustomerPuppetInterface c, int foodIndex) {
		orderQueue.add(new Order(w, c, foodIndex));
	}
	
	public synchronized Order takeOrderFromQueue() {
		if (orderQueue.size() == 0) {
			return null;
		}
		return orderQueue.remove(0);
	}
	
	
	public static class Order {
		AbstractWaiterPuppetInterface waiter;
		RestaurantCustomerPuppetInterface orderer;
		int foodIndex;
		public Order(AbstractWaiterPuppetInterface waiter, RestaurantCustomerPuppetInterface orderer, int foodIndex) {
			this.waiter = waiter;
			this.orderer = orderer;
			this.foodIndex = foodIndex;
		}
	}
	
	
	
}