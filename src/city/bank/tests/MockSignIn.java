package city.bank.tests;

import UnitTestingCommon.interfaces.BankCustomerInterface;
import UnitTestingCommon.interfaces.SignInSheetInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import UnitTestingCommon.Mock;
import city.bank.BankCustomer;

public class MockSignIn extends Mock implements SignInSheetInterface{

	public MockSignIn(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void msgWaitingInLine(BankCustomerInterface b) {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved message customer is waiting in line.");

	}

	@Override
	public void msgSickOfWaitingInLine(BankCustomerInterface b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgImAvailable(TellerInterface t) {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved message that teller is now free.");

	}

	@Override
	public void msgAddClerkToWorkingList(TellerInterface t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgRemoveClerkFromWorkingList(TellerInterface t) {
		// TODO Auto-generated method stub
		
	}

}
