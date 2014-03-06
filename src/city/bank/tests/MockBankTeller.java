package city.bank.tests;

import UnitTestingCommon.interfaces.BankCustomerInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import UnitTestingCommon.Mock;
import city.Person;
import city.bank.Bank.Account;

public class MockBankTeller extends Mock implements TellerInterface {

	 boolean ready = false;
	
	public MockBankTeller(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void msgAtCounterReadyToWork() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgPleaseAssistMe(Person p, BankCustomerInterface bp) {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved message for assistance.");

	}

	@Override
	public void msgDepositMoney(int cash) {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved message to deposit "+ (cash)+ " dollars.");

	}

	@Override
	public void msgYouHaveACustomerComing() {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved message that a customer is coming.");

	}

	@Override
	public void msgWithdrawMoney(int money) {
		// TODO Auto-generated method stub
        System.out.println((getName())+ ": Recieved message to withdraw " + (money) + " dollars.");                

	}

	@Override
	public void msgHereIsTheAccount(Account a) {
		// TODO Auto-generated method stub
        System.out.println((getName())+": Recieved account for " + (a.getCust())+" with " + (a.getMoneyInAccount())+" dollars.");

	}

	@Override
	public void msgOutOfBankRange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkTellerReady() {
        // TODO Auto-generated method stub
        if(ready==true){
                return true;
        }
        else{
        return false;}

	}

	@Override
	public int getCounter() {
		// TODO Auto-generated method stub
		return 0;
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
