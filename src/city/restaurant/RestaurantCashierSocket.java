package city.restaurant;


import java.util.ArrayList;
import java.util.HashMap;

import UnitTestingCommon.interfaces.AbstractWaiterPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantCashierSocketInterface;
import UnitTestingCommon.interfaces.RestaurantCustomerPuppetInterface;
import UnitTestingCommon.interfaces.RestaurantInterface;
import UnitTestingCommon.interfaces.SimpleRestaurantWorkerPuppetInterface;
import trace.AlertLog;
import trace.AlertTag;
import city.Bill;


public class RestaurantCashierSocket extends SimpleRestaurantWorkerSocket implements RestaurantCashierSocketInterface {
	
	
	

	// DATA
	
	
	private double cash;
	private int billCounter;	// unique ID for each bill
	private final ArrayList<MyBill> bills;
	private final HashMap<String,Double> priceSheet;
	private boolean needToCheckIfCashIsTooLow;
	private boolean needToGiveCashToRestaurant;
	
	
	
	
	
	// ENUMS AND CLASSES

	private enum BillState { noActionNeeded, needToConfirmUpdateToWaiter, needsToBeDeliveredToWaiter, customerJustPaid };
	public class MyBill {
		public Bill bill;
		public AbstractWaiterPuppetInterface waiterOfCustomer;
		public BillState state;
		public double amountPaid;
		public MyBill(AbstractWaiterPuppetInterface w, RestaurantCustomerPuppetInterface c) {
			bill = new Bill(billCounter++, c, RestaurantCashierSocket.this);
			waiterOfCustomer = w;
			state = BillState.noActionNeeded;
			amountPaid = 0.0;
		}
	}
	
	
	
	
	
	// BASIC UTILITY
	
	private MyBill findMyBill(RestaurantCustomerPuppetInterface c) {
		for (MyBill mb : bills) {
			if (mb.bill.billee == c) {
				return mb;
			}
		}
		return null;
	}
	private MyBill findMyBill(BillState s) {
		for (MyBill mb : bills) {
			if (mb.state == s) {
				return mb;
			}
		}
		return null;
	}
	
	
	
	
	
	
	// CONSTRUCTOR ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public RestaurantCashierSocket(String name, RestaurantInterface r, HashMap<String,Double> priceSheet) {
		super(name, r);
		this.cash = 0.0;
		this.billCounter = 0;
		this.bills = new ArrayList<MyBill>();
		this.priceSheet = priceSheet;
		needToCheckIfCashIsTooLow = false;
		needToGiveCashToRestaurant = false;
	}

	
	
	
	
	
	
	
	// NEW MESSAGES
	
	public void msgRestaurantGotMoneyFromBank(final double amount) {
		enqueMutation(new Mutation() {
			public void apply() {
				AlertLog.getInstance().logInfo(AlertTag.REST_CASHIER, getAgentName(), "Receieved $" + amount + " cash from restaurant");
				cash += amount;
			}
		});
	}

	public void msgCustomerReceivedOrder(final AbstractWaiterPuppetInterface w, final RestaurantCustomerPuppetInterface c, final String foodChoice) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyBill mb = RestaurantCashierSocket.this.findMyBill(c);
				if (mb == null) {
					mb = new MyBill(w, c);
					bills.add(mb);
				}
				mb.bill.addItem(foodChoice, priceSheet.get(foodChoice));
				mb.state = BillState.needToConfirmUpdateToWaiter;
			}
		});
	}
	public void msgCustomerWantsBill(final AbstractWaiterPuppetInterface w, final RestaurantCustomerPuppetInterface c) {
		enqueMutation(new Mutation() {
			public void apply() {
				MyBill mb = RestaurantCashierSocket.this.findMyBill(c);
				mb.waiterOfCustomer = w;
				mb.state = BillState.needsToBeDeliveredToWaiter;
			}
		});
	}
	public void msgCustomerPaidBill(final RestaurantCustomerPuppetInterface c, final double amount) {
		enqueMutation(new Mutation() {
			public void apply() {
				cash += amount;
				MyBill mb = RestaurantCashierSocket.this.findMyBill(c);
				mb.amountPaid = amount;
				mb.state = BillState.customerJustPaid;
			}
		});
	}
	
	
	
	
	
	
	// OVERRIDES
	@Override
	public void msgWorkerArrived(final SimpleRestaurantWorkerPuppetInterface w) {
		super.msgWorkerArrived(w);
		enqueMutation(new Mutation() {
			public void apply() {
				needToCheckIfCashIsTooLow = true;
			}
		});
	}
	@Override
	public void msgWorkerLeft(final SimpleRestaurantWorkerPuppetInterface w) {
		super.msgWorkerLeft(w);
		enqueMutation(new Mutation() {
			public void apply() {
				needToGiveCashToRestaurant = true;
			}
		});
	}
	
	
	
	
	
	
	// NEW ACTIONS
	
	private void actCheckIfCashIsTooLow() {
		needToCheckIfCashIsTooLow = false;
		if (cash <= Restaurant.cashierCashLowThreshold) {
			AlertLog.getInstance().logInfo(AlertTag.REST_CASHIER, getAgentName(), "Cash, at $" + cash + ", is too low; telling restaurant to resupply");
			restaurant.msgCashierNeedsCash(Restaurant.cashierCashResupplyAmount);
		}
		else {
			AlertLog.getInstance().logInfo(AlertTag.REST_CASHIER, getAgentName(), "Cash level, at $" + cash + ", is fine");
		}
	}
	private void actGiveCashToRestaurant() {
		needToGiveCashToRestaurant = false;
		AlertLog.getInstance().logInfo(AlertTag.REST_CASHIER, getAgentName(), "Giving all of $" + cash + " cash to restaurant to deposit");
		restaurant.msgCashierGivesCash(cash);
		cash = 0.0;
	}
	private void actConfirmBillUpdate(MyBill mb) {
		mb.state = BillState.noActionNeeded;
		mb.waiterOfCustomer.msgCashierUpdatedBill((RestaurantCustomerPuppet)mb.bill.billee);
	}
	private void actDeliverBill(MyBill mb) {
		mb.state = BillState.noActionNeeded;
		mb.waiterOfCustomer.msgCashierGaveBill(mb.bill.copy());
	}
	private void actReactToCustomerPayment(MyBill mb) {
		mb.state = BillState.noActionNeeded;
		if (mb.amountPaid > mb.bill.getTotal()) {
			AlertLog.getInstance().logInfo(AlertTag.REST_CASHIER, getAgentName(), "Customer " + mb.bill.billee.getAgentName() + " overpaid!");
		}
		else if (mb.amountPaid < mb.bill.getTotal()) {
			AlertLog.getInstance().logInfo(AlertTag.REST_CASHIER, getAgentName(), "Customer " + mb.bill.billee.getAgentName() + " UNDERPAID!");
		}
		((RestaurantCustomerPuppet)mb.bill.billee).msgCashierConfirmedPay();
		AlertLog.getInstance().logMessage(AlertTag.REST_CASHIER, getAgentName(), "Cash stands at $" + cash);
	}
	
	
	
	
	
	
	
	
	// SCHEDULER
	
	@Override
	public boolean pickAndExecuteAction() {
		
		if (needToCheckIfCashIsTooLow) {
			this.actCheckIfCashIsTooLow();
			return true;
		}
		if (needToGiveCashToRestaurant) {
			this.actGiveCashToRestaurant();
			return true;
		}
		
		if (worker != null) {
			MyBill mb;
			if ((mb = this.findMyBill(BillState.needToConfirmUpdateToWaiter)) != null) {
				this.actConfirmBillUpdate(mb);
				return true;
			}
			if ((mb = this.findMyBill(BillState.needsToBeDeliveredToWaiter)) != null) {
				this.actDeliverBill(mb);
				return true;
			}
			if ((mb = this.findMyBill(BillState.customerJustPaid)) != null) {
				this.actReactToCustomerPayment(mb);
				return true;
			}
		}
		return false;
	}
	
	
	
}