package UnitTestingCommon.interfaces;

import city.restaurant.RestaurantCustomerPuppet;

public interface CookSocketInterface extends SimpleRestaurantWorkerSocketInterface {
	
	
	
	public void msgAddFood(final int foodIndex, final int quantityInitial, final int quantityLowThreshold, final int quantityToResupply, final int minutesCookingTime, final double price, final int quantToAdd);
	public void msgCheckTheRevolvingStand();
	public void msgCustomerPlacedOrder(final AbstractWaiterPuppetInterface w, final RestaurantCustomerPuppetInterface c, final int foodIndex);
	public void msgThrowAwayOrder(final RestaurantCustomerPuppetInterface c);
	public void msgOrderDoneCooking(final RestaurantCustomerPuppetInterface c);
	public void msgWaiterAsksForOrder(final RestaurantCustomerPuppetInterface c);
	//public void msgSupplyDelivered(final int foodIndex, final int quantity, final double price);
	public void msgMarketsOutOfStock(final int foodIndex);
	
	
	
	
}