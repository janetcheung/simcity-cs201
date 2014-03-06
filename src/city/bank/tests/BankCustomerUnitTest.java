package city.bank.tests;

import agent.Agent;
import astar.MovementManager;
import city.Person;
import city.Puppet.Setup;
import city.bank.Bank;
import city.bank.BankCustomer;
import junit.framework.TestCase;

public class BankCustomerUnitTest extends TestCase {
    
    BankCustomer customer;
    MovementManager m;
    city.Person.Setup z;
    Setup s;
    MockSignIn signIn;
    Person  mockAndrew;
    MockBankTeller mockTeller;
    Bank bank;
    
    public void setUp() throws Exception{
            super.setUp();
            m = new MovementManager("Move",20,20);
            z = new city.Person.Setup();

            signIn = new MockSignIn("SignIn");
        mockAndrew = new Person("Andrew", m, 0, 0, z);
        mockTeller = new MockBankTeller("Teller");
        s = new Setup();    
        s.withdrawingTrueDepositingFalse=false;
            s.money=300;

            s.master= null;

            //s.master= mockAndrew; TODO temp comment out to avoid error

            bank= new Bank("Bank", 0, 0, 0, 0, 0, 0, 0, 0);
            customer = new BankCustomer("Customer",m,bank,s);
            customer.setSignIn(signIn);
            customer.setTestTeller(mockTeller);
            Agent.setJUNIT(true);

    }
    
    public void testCustomerDeposit(){
            System.out.println("                                                    ");
            System.out.println("*****TEST FOR CUSTOMER DEPOSITING MONEY*****");
            bank.msgCreateAccountPerson(mockAndrew, 450);
            customer.pickAndExecuteAction();
            customer.msgAtClerk();
            customer.pickAndExecuteAction();
            customer.msgHowMayIHelpYou();
            customer.pickAndExecuteAction();
            customer.msgMoneyWasDeposited();
            customer.pickAndExecuteAction();
            assertTrue("Customer started with 300 dollars, now has 150", customer.returnMoneyInWalletTest()==150);
            assertFalse("Customer does not have 300 dollars on him/her", customer.returnMoneyInWalletTest()==300);
            
    }
    
    
    public void testCustomerWithdraw(){
            System.out.println("                                                    ");
            System.out.println("*****TEST FOR CUSTOMER WITHDRAWING MONEY THAT EXISTS*****");
            customer.setToWithdrawTest();
            bank.msgCreateAccountPerson(mockAndrew, 250);
            customer.pickAndExecuteAction();
            customer.msgAtClerk();
            customer.pickAndExecuteAction();
            customer.msgHowMayIHelpYou();
            customer.pickAndExecuteAction();
            customer.msgHereIsYourMoney(50);
            customer.pickAndExecuteAction();
            assertTrue("Customer started with 300 dollars, now has 350", customer.returnMoneyInWalletTest()==350);
            assertFalse("Customer does not have 300 dollars on him/her", customer.returnMoneyInWalletTest()==300);
    }
    
    public void testCustomerHalfWithdraw(){
            System.out.println("                                                    ");
            System.out.println("*****TEST FOR CUSTOMER WITHDRAWING MONEY THAT ISN'T THERE *****");
            customer.setToWithdrawTest();
            bank.msgCreateAccountPerson(mockAndrew, 25);
            customer.pickAndExecuteAction();
            customer.msgAtClerk();
            customer.pickAndExecuteAction();
            customer.msgHowMayIHelpYou();
            customer.pickAndExecuteAction();
            customer.msgHereIsYourMoney(25);
            customer.pickAndExecuteAction();
            assertTrue("Customer started with 300 dollars, now has 325", customer.returnMoneyInWalletTest()==325);
            assertFalse("Customer does not have 300 dollars on him/her", customer.returnMoneyInWalletTest()==300);
    }
    
    

}
