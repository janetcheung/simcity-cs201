package city.bank.tests;

import agent.Agent;
import astar.MovementManager;
import city.Person;
import city.bank.Bank;
import city.restaurant.Restaurant;
import junit.framework.TestCase;

public class BankAgentUnitTest extends TestCase{


    Bank bank;
    MovementManager m;
    city.Person.Setup s;
    Restaurant restaurant1;
    Restaurant restaurant2;
    Restaurant restaurant3;
    //MovementManager m;
    MockBankTeller teller;
    Person andrew;
    Person sean;
    Person jacob;


    public void setUp() throws Exception{
            super.setUp();
            bank= new Bank("Bank", 0, 0, 0, 0, 0, 0, 0, 0);
            s = new city.Person.Setup();
            m = new MovementManager("Move", 0, 0);
            restaurant1= new Restaurant("Rest1", 0, 0, 0, 0, 0, bank, null, null, 0, 0, 0,0);
            restaurant2= new Restaurant("Rest2", 0, 0, 0, 0, 0, bank, null, null,0, 0, 0, 0);
            restaurant3= new Restaurant("Rest3", 0, 0, 0, 0, 0, bank, null, null, 0, 0, 0, 0);
            teller = new MockBankTeller("BankTeller");
            andrew = new Person("Andrew", m, 0, 0, s);
            sean = new Person("Sean", m, 0, 0, s);
            jacob = new Person("Jacob", m, 0, 0, s);

            Agent.setJUNIT(true);
    }

    /////////TEST 1///////////////////////////

    
    public void testAddingPersonAccounts(){
            //TEST to see if adding accounts functions properly 
            System.out.println("*****TEST FOR ADDING PERSON ACCOUNTS*****");
            assertEquals("Bank should have 0 person accounts in it. It doesn't.",bank.getStoredAccountsSize(), 0);                

            bank.msgCreateAccountPerson(andrew, 250);
            assertTrue("Bank should have one person account in it. It doesn't", bank.getStoredAccountsSize()==1);



            bank.msgCreateAccountPerson(sean, 300);
            assertTrue("Bank should have two person accounts in it. It doesn't", bank.getStoredAccountsSize()==2);
            assertFalse("Bank should not have four person accounts in it. It does", bank.getStoredAccountsSize()==4);



            bank.msgCreateAccountPerson(jacob, 300);
            assertTrue("Bank should have three accounts in it. It doesn't", bank.getStoredAccountsSize()==3);
    }
     


    ////////////TEST 2////////////////////////////////

    
    public void testRetrievingAccounts(){
            //TEST to see if bank sends the correctly requested account
            System.out.println("*****TEST FOR RETRIEVING ACCOUNTS*****");
            assertEquals("Bank should have 0 person accounts in it. It doesn't.",bank.getStoredAccountsSize(), 0);                
            bank.msgCreateAccountPerson(jacob, 530);
            bank.msgCreateAccountPerson(andrew, 260);
            assertEquals("Bank should have 0 person accounts in it. It doesn't.",bank.getStoredAccountsSize(), 2);                

            bank.msgPleaseSendAccount(jacob, teller);
            bank.pickAndExecuteAction();
    }

     



    ///////TEST 3///////////////////////////////////

    public void testUpdatingAccounts(){
            System.out.println("*****TEST FOR UPDATING ACCOUNTS*****");
            bank.msgCreateAccountPerson(andrew, 120);
            bank.msgCreateAccountPerson(jacob, 230);

            bank.msgUpdateAccount(bank.createTestAccount(andrew, 340));
            bank.pickAndExecuteAction();
            assertEquals("Account andrew should now hold 340 dollars", bank.getAccountCashAmountTest(andrew),340);                        

            bank.msgUpdateAccount(bank.createTestAccount(jacob, 245));
            bank.pickAndExecuteAction();
            assertEquals("Account jacob should now hold 245 dollars", bank.getAccountCashAmountTest(jacob),245);                        


    }

     
    ///////////////TEST 4//////////////////////////

    public void testAddingRestaurantAccounts(){
            System.out.println("*****TEST FOR ADDING RESTAURANTS*****");
            assertEquals("Bank should have 0 restaurant accounts in it. It doesn't.", bank.getStoredRestaurantAccountSize(),0);
            bank.msgCreateAccountRestaurant(restaurant1, 600);
            assertTrue("Bank should have one restaurant account in it. It doesn't", bank.getStoredRestaurantAccountSize()==1);


            bank.msgCreateAccountRestaurant(restaurant2, 700);
            assertTrue("Bank should have two restaurant accounts in it. It doesn't", bank.getStoredRestaurantAccountSize()==2);
            assertFalse("Bank should not have three restaurant accounts in it. It does", bank.getStoredRestaurantAccountSize()==3);


            bank.msgCreateAccountRestaurant(restaurant3, 550);
            assertTrue("Bank should have three restaurant accounts in it. It doesn't", bank.getStoredRestaurantAccountSize()==3);

    }
     


    ////////////////TEST 5////////////////////////////////

    public void testRestaurantWithdrawAndDeposit(){
            System.out.println("*****TEST FOR WITHDRAWING AND DEPOSITING FROM RESTAURANTS*****");
            bank.msgCreateAccountRestaurant(restaurant1, 340);
            bank.msgCreateAccountRestaurant(restaurant2, 450);
            bank.msgRestaurantDepositRequest(restaurant1, 320);
            
            bank.pickAndExecuteAction();
            assertEquals("Bank should have 660 dollars in it, it doesn't", bank.getRestuarantCashTest(restaurant1),660);        

    

            bank.msgRestaurantWithdrawRequest(restaurant2, 220);
            bank.pickAndExecuteAction();
            assertEquals("Bank should have 230 dollars in it, it doesn't", bank.getRestuarantCashTest(restaurant2),230);        


}


}
