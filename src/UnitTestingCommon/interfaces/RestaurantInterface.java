package UnitTestingCommon.interfaces;

import java.util.HashMap;
import city.Bill;
import city.restaurant.RestaurantParticipantPuppet;

public interface RestaurantInterface extends BuildingInterface {
	
	
	public void msgParticipantLeft(final RestaurantParticipantPuppetInterface p);
	public void msgCashierNeedsCash(final double amount);
	public void msgCashierGivesCash(final double amount);
	public void msgBankAccountRanOut();
	public void msgBankSentMoney(final double amount);
	public void msgCookUpdatesInventoryAndMenu(final HashMap<String,Double> newMenu, final int[] inventory);
	public void msgCookNeedsResupply(final int foodIndex, final int quantity);
	public void msgMarketResponse(final int foodIndex, final int quantityAbleToDeliver);
	public void msgMarketSentFood(final int foodIndex, final int quantity);
	public void msgMarketSentBill(final Bill b);
	public void msgIncrementFoodInventory(final int foodIndex);
	
	
	
}