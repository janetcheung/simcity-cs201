package city.bank.tests;

import UnitTestingCommon.interfaces.BankCustomerInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import UnitTestingCommon.Mock;
import city.Person;

public class MockBankCustomer extends Mock implements BankCustomerInterface{

	Person p;
	
	public MockBankCustomer(String name, Person p) {
		super(name);
		// TODO Auto-generated constructor stub
		this.p=p;
	}

    public Person returnMockPerson(){
        return p;
}
	
	@Override
	public void setTestTeller(TellerInterface t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgGoToTeller(TellerInterface t, int counter) {
		// TODO Auto-generated method stub
		System.out.println((getName())+ ": Recieved notification to go to teller. " + (t));
	}

	@Override
	public void msgAtClerk() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHowMayIHelpYou() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgMoneyWasDeposited() {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved notification that money was deposited.");

	}

	@Override
	public void msgHereIsYourMoney(int cash) {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved "+ (cash)+" dollars.");

	}

	@Override
	public void msgOutOfBank() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAgentName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void msgLastStep(int r, int c) {
		// TODO Auto-generated method stub
		
	}

}
