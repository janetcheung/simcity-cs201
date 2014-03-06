package UnitTestingCommon.interfaces;

import city.bank.BankCustomer;

public interface SignInSheetInterface {

	public abstract void msgWaitingInLine(final BankCustomerInterface b);
	
	public abstract void msgSickOfWaitingInLine(final BankCustomerInterface b);
	
	public abstract void msgImAvailable(final TellerInterface t);
	
	public abstract void msgAddClerkToWorkingList(final TellerInterface t);
	
	public abstract void msgRemoveClerkFromWorkingList(final TellerInterface t);
	
}
