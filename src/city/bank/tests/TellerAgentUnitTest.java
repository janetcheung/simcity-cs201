package city.bank.tests;

import agent.Agent;
import astar.MovementManager;
import city.Building;
import city.Person;
import city.Puppet.Setup;
import city.bank.Bank;
import city.bank.TellerAgent;
import junit.framework.TestCase;

public class TellerAgentUnitTest extends TestCase{

	TellerAgent teller;
	MovementManager m;
	Setup s;
	MockSignIn invisHost;
	Person  mockAndrew;
	Person  mockMike;
	Person  mockJanet;
	Person  mockTeller;
	city.Person.Setup z;

	MockBankCustomer andrew;
	MockBankCustomer mike;
	MockBankCustomer janet;
	Bank  bank;

	public void setUp() throws Exception{
		super.setUp();
		bank= new Bank("Bank", 0, 0, 0, 0, 0, 0, 0, 0);
		invisHost = new MockSignIn("InvisHost");
		m = new MovementManager("Move",20,20);
		z = new city.Person.Setup();

		mockTeller = new Person("Teller", m, 0, 0, z);
		s = new Setup();

		s.master= null;
		//s.master= mockTeller; TODO temp comment out to avoid error

		teller = new TellerAgent("Teller",m,bank,s,1);
		mockAndrew = new Person("Andrew", m, 0, 0, z);
		mockMike = new Person("Mike", m, 0, 0, z);
		mockJanet = new Person("Janet", m, 0, 0, z);
		andrew = new MockBankCustomer("Andrew",mockAndrew);
		mike = new MockBankCustomer("Mike", mockMike);
		janet = new MockBankCustomer("Janet", mockJanet);
		teller.setSignInSheet(invisHost);
		Agent.setJUNIT(true);

	}

	/////////TEST 1//////////////////////////////

	public void testHelpingCustomersDeposit(){
		System.out.println("                                                    ");
		System.out.println("*****TEST FOR HELPING CUSTOMERS DEPOSIT MONEY*****");
		bank.msgCreateAccountPerson(mockAndrew, 350);
		bank.msgCreateAccountPerson(mockJanet, 230);

		teller.msgAtCounterReadyToWork();
		teller.pickAndExecuteAction();

		teller.msgPleaseAssistMe(mockAndrew, andrew);
		teller.pickAndExecuteAction();
		teller.msgDepositMoney(320);
		teller.pickAndExecuteAction();
		bank.pickAndExecuteAction();
		teller.pickAndExecuteAction();
		bank.pickAndExecuteAction();
		teller.pickAndExecuteAction();

		assertTrue("Andrew's account should now have 670 dollars.", bank.getAccountCashAmountTest(mockAndrew)==670);
		assertFalse("Andrew's account does not have 320 dollars.", bank.getAccountCashAmountTest(mockAndrew)==320);

		teller.pickAndExecuteAction();
		teller.msgPleaseAssistMe(mockJanet, janet);
		teller.pickAndExecuteAction();
		teller.msgDepositMoney(480);
		teller.pickAndExecuteAction();
		bank.msgPleaseSendAccount(mockJanet, teller);

		bank.pickAndExecuteAction();
		teller.pickAndExecuteAction();
		bank.pickAndExecuteAction();
		assertTrue("Janet's account should now have 710 dollars.", bank.getAccountCashAmountTest(mockJanet)==710);
		assertFalse("Janet's account does not have 230 dollars.", bank.getAccountCashAmountTest(mockJanet)==230);

	}

	//////////TEST 2///////////////////////////

	public void testHelpingCustomersWithdraw(){
		System.out.println("                                                    ");
		System.out.println("*****TEST FOR HELPING CUSTOMERS WITHDRAW MONEY*****");
		bank.msgCreateAccountPerson(mockAndrew, 500);
		bank.msgCreateAccountPerson(mockMike, 200);

		teller.msgAtCounterReadyToWork();
		teller.pickAndExecuteAction();

		teller.msgPleaseAssistMe(mockAndrew, andrew);
		teller.pickAndExecuteAction();
		teller.msgWithdrawMoney(240);
		teller.pickAndExecuteAction();                         //Normal withdraw
		bank.pickAndExecuteAction();
		teller.pickAndExecuteAction();
		bank.pickAndExecuteAction();
		// assertTrue("Andrew's account should now have 260 dollars.", bank.getAccountCashAmountTest(mockAndrew)==260);
		// assertFalse("Andrew's account does not have 500 dollars.", bank.getAccountCashAmountTest(mockAndrew)==500);

		teller.pickAndExecuteAction();
		teller.msgPleaseAssistMe(mockMike, mike);
		teller.pickAndExecuteAction();
		teller.msgWithdrawMoney(270);
		teller.pickAndExecuteAction();                     //customer tries to take to much money 
		bank.pickAndExecuteAction();
		teller.pickAndExecuteAction();
		bank.pickAndExecuteAction();
		assertTrue("Mike's account should now have 0 dollars.", bank.getAccountCashAmountTest(mockMike)==0);
		assertFalse("Mikes's account does not have 200 dollars.", bank.getAccountCashAmountTest(mockMike)==200);

		teller.pickAndExecuteAction();
		teller.msgPleaseAssistMe(mockMike, mike);
		teller.pickAndExecuteAction();
		teller.msgWithdrawMoney(300);
		teller.pickAndExecuteAction();                     //customer now tries to take money from an empty account
		bank.pickAndExecuteAction();
		teller.pickAndExecuteAction();
		bank.pickAndExecuteAction();
		assertTrue("Mike's account should still have 0 dollars.", bank.getAccountCashAmountTest(mockMike)==0);
		assertFalse("Mikes's account does not have 300 dollars.", bank.getAccountCashAmountTest(mockMike)==300);

	}

}
