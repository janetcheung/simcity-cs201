package UnitTestingCommon.interfaces;

import city.Person;
import city.bank.BankCustomer;
import city.bank.Bank.Account;

public interface TellerInterface extends PuppetInterface {

	public abstract void msgAtCounterReadyToWork();
	
	public abstract void msgPleaseAssistMe(final Person p, final BankCustomerInterface bp);
	
	public abstract void msgDepositMoney(final int cash);
	
	public abstract void msgYouHaveACustomerComing();
	
	public abstract void msgWithdrawMoney(final int money);
	
	public abstract void msgHereIsTheAccount(final Account a);
	
	public abstract void msgOutOfBankRange();
	
	public abstract void msgLeaveBuilding();

	public abstract boolean checkTellerReady();

	public abstract int getCounter();
	
	
	
	
}
