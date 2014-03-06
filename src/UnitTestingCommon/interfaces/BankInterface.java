package UnitTestingCommon.interfaces;

import city.Person;
import city.bank.TellerAgent;
import city.bank.Bank.Account;
import city.restaurant.Restaurant;

public interface BankInterface extends BuildingInterface {

	public abstract void msgUpdateTime(final long time);

	public abstract void msgCreateAccountPerson(final Person p, final int money);

	public abstract void msgCreateAccountRestaurant(final Restaurant r, final int money);

	public abstract void msgPleaseSendAccount(final Person p, final TellerInterface t);

	public abstract void msgRestaurantWithdrawRequest(final Restaurant r, final double amount);

	public abstract void msgRestaurantDepositRequest(final Restaurant r,final double amount);

	public abstract void msgUpdateAccount(final Account a);

	public abstract void msgTellerEndingShift(final TellerAgent t);


}
