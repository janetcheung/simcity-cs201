package UnitTestingCommon.interfaces;

import java.util.HashMap;
import city.Bill;

public interface RestaurantCustomerPuppetInterface extends RestaurantParticipantPuppetInterface {
	
	
	public void msgComeToHost();
	public void msgFollowWaiterToTable(final AbstractWaiterPuppetInterface w, final int tableID, final HashMap<String,Double> menu);
	public void msgWaiterSaysToReorder(final HashMap<String,Double> menuUpdate);
	public void msgWaiterDeliveredOrder(final String foodChoice);
	public void msgDoneEating();
	public void msgWaiterGaveBill(final Bill bill);
	public void msgCashierConfirmedPay();
	public void msgRanOutOfPatience();
	
	
	
}