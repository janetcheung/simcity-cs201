package city.bank;

import java.util.Timer;
import java.util.TimerTask;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.BankCustomerInterface;
import UnitTestingCommon.interfaces.SignInSheetInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import astar.MovementManager;
import city.Person;
import city.Puppet;
import city.bank.gui.BankCustomerGui;
import city.bank.gui.BankGui;

public class BankCustomer extends Puppet implements BankCustomerInterface{


	///DATA////////////////////////////////////////////////////////////////////////////////////////////////////////


	private TellerInterface teller;
	private SignInSheetInterface signIn;
	private Person         controllerId;
	private int           moneyInWallet;
	private int            moneyInAction;
	private enum reasonHere{depositing, withdrawing};
	private reasonHere customerMission = reasonHere.depositing;   //default value, to be changed in constructor 

	private BankGui mainGui;
	private int row=24;
	private int col=16;
	private boolean checkReasonHere;
	private enum bankCustomerStates {Entering, Waiting, GoingToClerk, Moving, AtClerk, Leaving, 
		MovingToCounter,MovingToExit,WaitingForReply, TellerReplied,Left,GoToFrontOfLine};
	private bankCustomerStates state = bankCustomerStates.Entering;
	boolean sickOfWaiting=false;
	boolean notified = false;
	private BankCustomerGui customerGui;

	private int counterNumber;

	Timer waiting = new Timer();

	public BankCustomer(String name, MovementManager m, Bank b, Setup s) {
		super(name, m, b, s);
		// TODO Auto-generated constructor stub
		this.controllerId=s.master;
		this.moneyInWallet=(int) s.money;
		this.moneyInAction=(int) s.money/2;
		this.checkReasonHere=s.withdrawingTrueDepositingFalse;
		this.customerGui= new BankCustomerGui(this);
		
		if(checkReasonHere==true){
			moneyInAction=50;
			customerMission=reasonHere.withdrawing;
		}

		else if(checkReasonHere==false){
			customerMission=reasonHere.depositing;
		}
		//set controllerId
		//set moneyInAction
		//set reasonHere
	}

	public BankCustomerGui getCustomerGui() {
		return customerGui;
	}
	
	public void setMainGui(BankGui mainGui) {
		this.mainGui = mainGui;
	}
	
	public void setSignIn(SignInSheetInterface s){
		this.signIn=s;
	}
	
	////FOR TEST SETUP//////////////////////////////
	public void setTestTeller(TellerInterface t){
		this.teller=t;
	}
	
	public int returnMoneyInWalletTest(){
		return moneyInWallet;
	}
	
	public void setToWithdrawTest(){
		customerMission=reasonHere.withdrawing;
	}

	////MESSAGES///////////////////////////////////////////////////////////////////////////////////////////////////

	public void msgGoToTeller(final TellerInterface t, final int counter) {
		enqueMutation(new Mutation(){
			public void apply(){
				notified=true;
				teller = t;                                                       //sent to counter by the invisible host
				counterNumber=counter;
				state= bankCustomerStates.GoingToClerk;

			}
		});
	}

	public void msgAtClerk(){
		enqueMutation(new Mutation(){
			public void apply(){                                                    //customer is at teller
				state=bankCustomerStates.AtClerk;

			}
		});
	}

	public void msgHowMayIHelpYou(){
		enqueMutation(new Mutation(){                                              //teller is ready for customer
			public void apply(){
				state=bankCustomerStates.TellerReplied;

			}
		});
	}

	public void msgMoneyWasDeposited(){
		enqueMutation(new Mutation(){                                           //customer's money was deposited 
			public void apply(){
				moneyInWallet-=moneyInAction;
				state=bankCustomerStates.Leaving;

			}
		});
	}

	public void msgHereIsYourMoney(final int cash){                                  //customer receives his money
		enqueMutation(new Mutation(){
			public void apply(){
				moneyInWallet+=cash;
				state=bankCustomerStates.Leaving;

			}
		});
	}
	
	public void msgOutOfBank(){                                              //customer has left bank
		enqueMutation(new Mutation(){
			public void apply(){
				state=bankCustomerStates.Left;

			}
		});
	
	}


	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub

	}

	////SCHEDULER///////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public boolean pickAndExecuteAction() {
		if (state==bankCustomerStates.Entering) {
			state=bankCustomerStates.GoToFrontOfLine;  
			this.setNewDestination(row, col);
			return true;
		}

		if (state==bankCustomerStates.GoingToClerk)  {
			state=bankCustomerStates.MovingToCounter;
			DoGoToClerk();      
			return true;
		}

		if (state==bankCustomerStates.AtClerk   && customerMission==reasonHere.depositing) {
			state=bankCustomerStates.Waiting;
			RequestDeposit();  
			return true;

		}

		if (state == bankCustomerStates.AtClerk && customerMission==reasonHere.withdrawing) {
			state=bankCustomerStates.Waiting;
			RequestWithdraw();   
			return true;
		}

		if (state==bankCustomerStates.TellerReplied   && customerMission==reasonHere.depositing) {
			state=bankCustomerStates.Waiting;
			RequestDepositPt2(); 
			return true;

		}

		if (sickOfWaiting==true && notified==false){
			sickOfWaiting=false;
			signIn.msgSickOfWaitingInLine(this);
			state=bankCustomerStates.MovingToExit;
			DoLeaveBank(); 

			return true;
			
		}
		
		if (state == bankCustomerStates.TellerReplied && customerMission==reasonHere.withdrawing) {
			state=bankCustomerStates.Waiting;
			RequestWithdrawPt2();   
			return true;
		}


		
		if (state==bankCustomerStates.Leaving) {
			state=bankCustomerStates.MovingToExit;
			DoLeaveBank(); 
			return true;
		}
		
		if (state==bankCustomerStates.Left){
			
			state=bankCustomerStates.Moving;
			EndPuppetTransmission();
			return true;
		}
		return false;
	}


	//////ACTIONS////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void DoGoToClerk(){
		
		row=16;
		if (counterNumber==0){
			col=6;                                                //finds the correct counter to go to
		}
		else if (counterNumber==1){
			col=12;
		}
		else if (counterNumber==2){
			col=18;
		}
		else if (counterNumber==3){
			col=24;
		}
		this.setNewDestination(row, col);
		//-guiAction
	}

	private void RequestDeposit(){                                                         //asks to make a deposit 
        AlertLog.getInstance().logMessage(AlertTag.BANK_CUSTOMER, getAgentName(), "Please assist me.");

		teller.msgPleaseAssistMe(controllerId, this);
		state=bankCustomerStates.WaitingForReply;
	}
	
	private void RequestDepositPt2(){
        AlertLog.getInstance().logMessage(AlertTag.BANK_CUSTOMER, getAgentName(), "I would like to make a deposit of " + (moneyInAction) + " dollars.");
		teller.msgDepositMoney(moneyInAction);  

	}

	private void RequestWithdraw(){                                              //asks to make a withdrawal 
        AlertLog.getInstance().logMessage(AlertTag.BANK_CUSTOMER, getAgentName(), "Please assist me.");
		teller.msgPleaseAssistMe(controllerId, this);
		state=bankCustomerStates.WaitingForReply;
	}
	
	private void RequestWithdrawPt2(){
        AlertLog.getInstance().logMessage(AlertTag.BANK_CUSTOMER, getAgentName(), "I would like to make a withdrawal of "  + (moneyInAction) + " dollars.");
		teller.msgWithdrawMoney(moneyInAction); 

	}

	private void DoLeaveBank(){                                              //exits the bank
        AlertLog.getInstance().logMessage(AlertTag.BANK_CUSTOMER, getAgentName(), "Thank you, goodbye.");
		row = 28;
		col = 1;
		this.setNewDestination(row, col);
		                             
	}

	private void EndPuppetTransmission(){                                             
		controllerId.msgUpdateMoney(moneyInWallet);
		getMaster().msgPuppetLeftBuilding();
		this.stopAgent();
	}

	protected void actMoveStep(int r, int c) {
		// TODO Auto-generated method stub
		customerGui.setDestination(r, c);
	}

	protected void actReactToBlockedRoute() {
		// TODO Auto-generated method stub
		this.actWaitRandomAndTryAgain();
	}

	protected void actArrivedAtDestination() {
		// TODO Auto-generated method stub
		if(state==bankCustomerStates.GoToFrontOfLine){
			state=bankCustomerStates.Waiting;
			signIn.msgWaitingInLine(this); 
			this.pickAndExecuteAction();
			startWaiting();
		}
		
		if(state==bankCustomerStates.MovingToCounter){
			state=bankCustomerStates.AtClerk;
			this.pickAndExecuteAction();
		}
		
		if(state==bankCustomerStates.MovingToExit){
			state=bankCustomerStates.Left;
			mainGui.removeGui(customerGui);
			this.pickAndExecuteAction();
			
		}
	}
	
	private void startWaiting(){
        waiting.schedule(new TimerTask() {


            public void run(){
                    //finishOrder

                    sickOfWaiting=true;
                    BankCustomer.this.pickAndExecuteAction();
                    
            }


    },
    5000);
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
}
