package city.bank.tests;

import agent.Agent;
import astar.MovementManager;
import city.Person;
import city.bank.SignInSheet;
import junit.framework.TestCase;

public class SignInSheetUnitTest extends TestCase{

	SignInSheet signIn;
	MovementManager m;
	Person mockAndrew;
	Person mockJacob;
	city.Person.Setup z;
	MockBankTeller teller1;
	MockBankTeller teller2;
	MockBankCustomer andrew;
	MockBankCustomer jacob;

	public void setUp() throws Exception{
		super.setUp();
		signIn = new SignInSheet("SignIn");
		z = new city.Person.Setup();
		mockAndrew = new Person("Andrew", m, 0, 0, z);
		teller1 = new MockBankTeller("Teller1");
		teller2 = new MockBankTeller("Teller2");
		mockJacob = new Person("Jacob", m, 0, 0, z);
		andrew = new MockBankCustomer("Andrew",mockAndrew);
		jacob = new MockBankCustomer("Jacob", mockJacob);
		Agent.setJUNIT(true);
	}

	public void testSignInLists(){
		assertEquals("SignIn starts with 0 tellers in its list", signIn.returnTellerListTest(),0);
		signIn.msgAddClerkToWorkingList(teller1);
		signIn.msgAddClerkToWorkingList(teller2);
		assertTrue("SignIn has 2 tellers in its list", signIn.returnTellerListTest()==2);
		assertFalse("SignIn does not have 1 teller in its list", signIn.returnTellerListTest()==1);

		assertEquals("SignIn starts with 0 customers in its list", signIn.returnCustomerListTest(),0);
		signIn.msgWaitingInLine(andrew);
		signIn.msgWaitingInLine(jacob);
		assertTrue("SignIn has 2 tellers in its list", signIn.returnCustomerListTest()==2);
		assertFalse("SignIn does not have 1 teller in its list", signIn.returnCustomerListTest()==1);

		signIn.msgRemoveClerkFromWorkingList(teller1);
		assertTrue("SignIn has 2 tellers in its list", signIn.returnTellerListTest()==1);
		assertFalse("SignIn does not have 1 teller in its list", signIn.returnTellerListTest()==2);
	}

	public void testGoingToCorrectClerk(){
		System.out.println("                                                    ");
		System.out.println("*****TEST FOR GOING TO CORRECT CLERK*****");
		signIn.msgAddClerkToWorkingList(teller1);
		signIn.msgAddClerkToWorkingList(teller2);

		signIn.msgWaitingInLine(andrew);
		signIn.pickAndExecuteAction();
		assertTrue("Customer should still be in list as tellers are not ready.", signIn.returnCustomerListTest()==1);

		teller1.ready=true;
		signIn.msgImAvailable(teller1);
		assertTrue("Teller is now in active teller list", signIn.returnActiveTellerListTest()==1);
		signIn.pickAndExecuteAction();
		assertTrue("Customer should have been sent to teller1", signIn.returnCustomerListTest()==0);
		assertTrue("Teller is removed from ctive teller list", signIn.returnActiveTellerListTest()==0);

	}


}