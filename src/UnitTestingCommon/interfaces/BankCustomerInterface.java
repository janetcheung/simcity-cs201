package UnitTestingCommon.interfaces;

import city.bank.TellerAgent;

public interface BankCustomerInterface extends PuppetInterface {

	public abstract void setTestTeller(TellerInterface t);
	
	public abstract void msgGoToTeller(final TellerInterface t, final int counter);
	
	public abstract void msgAtClerk();
	
	public abstract void msgHowMayIHelpYou();
	
	public abstract void msgMoneyWasDeposited();
	
	public abstract void msgHereIsYourMoney(final int cash);
	 
	public abstract void msgOutOfBank();
	 
	public abstract void msgLeaveBuilding();
	 
	 
	
}
