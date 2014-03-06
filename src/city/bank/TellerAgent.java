package city.bank;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.BankCustomerInterface;
import UnitTestingCommon.interfaces.SignInSheetInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import astar.MovementManager;
import city.Person;
import city.Puppet;
import city.bank.Bank.Account;
import city.bank.gui.BankGui;
import city.bank.gui.TellerGui;

public class TellerAgent extends Puppet implements TellerInterface{

	////DATA//////////////////

	private BankCustomerInterface currentCustomer;

	private Person personId;

	private enum TellerStates{
		Available,HelpingCustomer,Deposit,Working,Withdraw,WaitingForAccount,
		Entering,Leaving,Moving,MovingIn,MovingOut,NotifySignIn,RecievedAccount,Left;
	};
	
	private TellerStates bStates= TellerStates.Entering;
	private boolean customerArrived = false;
	private int requestedMoney;
	private Account currentAccount;
	private boolean deposit =false;
	private boolean withdraw = false;
	private boolean doneWorking = false;
	private boolean customerAssigned =false;
	private Bank bank;
	private SignInSheetInterface invisHost;
	private BankGui mainGui;
	private int row = 0;
	private int col = 0;
	private TellerGui tellGui;
	private int counterLocation;
	private Person controllerID;


	
	public TellerAgent(String name, MovementManager m, Bank b, Setup s, int counter){
		super(name, m, b, s);
		// TODO Auto-generated constructor stub
		
		this.bank=b;
		this.controllerID=s.master;
		this.counterLocation=counter;
		
		tellGui = new TellerGui(this);
		//set controllerId
		//set bank
		
	}

	public TellerGui getTellGui() {
		return tellGui;
	}
	
	public int getCounter(){
		return counterLocation;
	}
	
	public void setMainGui(BankGui mainGui) {
		this.mainGui = mainGui;
	}
	
	public void setSignInSheet(SignInSheetInterface s){
		this.invisHost=s;
	}
	
//	public void setTellerGui(TellerGui p){
//		tellGui = p;
//	}
   
	public TellerGui getGui(){
		return tellGui;
	}
	
	public boolean checkTellerReady(){
        if(bStates==TellerStates.Available){
                return true;
        }

        else
                return false;
}

    public Person getControllerId(){
    	
    	return controllerID;
    }

	///////Messages//////////

	public void msgAtCounterReadyToWork(){
		enqueMutation(new Mutation(){
			public void apply(){                                 //DOUBLE CHECK IN GUI
				bStates=TellerStates.NotifySignIn;

			}
		});

	}
    
    public void msgPleaseAssistMe(final Person p, final BankCustomerInterface bp) {
		enqueMutation(new Mutation(){
			public void apply(){                                                    // a bank customer has requested assistance 
				personId=p;
				currentCustomer=bp;
				customerArrived = true;

			}
		});
	}

	public void msgDepositMoney(final int cash) {
		enqueMutation(new Mutation(){
			public void apply(){
				requestedMoney=cash;                                            //customer wants to deposit 
				bStates=TellerStates.Deposit;

			}
		});	
	}
	
	public void msgYouHaveACustomerComing() {
		enqueMutation(new Mutation(){
			public void apply(){                                                         //sign in sheet warns of new customer
				customerAssigned=true;

			}
		});	
	}

	public void msgWithdrawMoney(final int money){
		enqueMutation(new Mutation(){                                                          //customer requests a withdraw 
			public void apply(){
				requestedMoney=money;
				bStates=TellerStates.Withdraw;

			}
		});	
	}

	public void	msgHereIsTheAccount(final Account a){
		enqueMutation(new Mutation(){                                                      //bank sends the needed account
			public void apply(){
				currentAccount = a;
				bStates=TellerStates.RecievedAccount;
			}
		});
 
	}

	public void msgOutOfBankRange(){
		enqueMutation(new Mutation(){                                      //teller is done working and has left
			public void apply(){
				bStates=TellerStates.Left;
			}
		});
	}





	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub                                    //teller's shift is over
		enqueMutation(new Mutation(){
			public void apply(){
				doneWorking=true;
			}
		});
 
	}

	//SCHEDULER/////////////////////////////




	@Override
	public boolean pickAndExecuteAction() {
		
		if(bStates==TellerStates.Entering){
			bStates=TellerStates.MovingIn;
			
			row=13;
			if (counterLocation==0){
				col=6;
			}
			else if (counterLocation==1){
				col=12;
			}
			else if (counterLocation==2){
				col=18;
			}
			else if (counterLocation==3){
				col=24;
			}
			this.setNewDestination(row, col);
			return true;
			//////////////////////////////////////////////////////GUI!!!!!!!!!!!!!!
		}
		
		if(bStates==TellerStates.NotifySignIn){
			invisHost.msgImAvailable(this);
			bStates=TellerStates.Available;
			return true;
		}
		
		if(bStates==TellerStates.RecievedAccount){
			bStates=TellerStates.Working;
			if(deposit==true){
				DepositMoneyPt2();
				deposit=false;
			}
			
			if(withdraw==true){
				WithdrawMoneyPt2();
				withdraw=false;
			}
			return true;
		}
		
		if (bStates==TellerStates.Available && customerArrived==true) {
			bStates=TellerStates.HelpingCustomer;
			customerArrived=false;
			customerAssigned=false;
	        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "How may I be of service today?");
			currentCustomer.msgHowMayIHelpYou();        
			return true;
		}

		if (bStates==TellerStates.Deposit) {
			bStates=TellerStates.Working;
			DepositMoney(); 
			return true;
		}

		if (bStates==TellerStates.Withdraw) {
			bStates=TellerStates.Working;
			WithdrawMoney();  
			return true;
		}

		if(bStates==TellerStates.Available&& doneWorking==true && customerAssigned==false){
			bStates=TellerStates.Leaving;
			return true;
		}
		
		if(bStates==TellerStates.Leaving){
			bStates=TellerStates.MovingOut;
			invisHost.msgRemoveClerkFromWorkingList(this);
			DoLeaveBank();
			return true;
		}
		
		if(bStates==TellerStates.Left){
			bStates=TellerStates.Working;
			EndPuppetTransmission();
			return true;
		}

		return false;
	}




	//ACTIONS//////////////////////////////

	private void DepositMoney(){																		//deposit the customers money
        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "One moment please while I look up your account.");
		bank.msgPleaseSendAccount(personId, this);
		bStates=TellerStates.WaitingForAccount;
		deposit = true;
	}
	
	
	private void DepositMoneyPt2(){                                                                 
		currentAccount.setMoneyInAccount(currentAccount.getMoneyInAccount() + requestedMoney);
		bank.msgUpdateAccount(currentAccount);
        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "Your deposit was of " +(requestedMoney) + " was completed. Have a nice day.");
		currentCustomer.msgMoneyWasDeposited();
		if(doneWorking==false){
		invisHost.msgImAvailable(this);
		bStates=TellerStates.Available;    
		}
		else if(doneWorking==true){
			bStates=TellerStates.Leaving;
		}     
	}

	private void WithdrawMoney(){                                             //withdraw customers money
        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "One moment please while I look up your account.");
		bank.msgPleaseSendAccount(personId, this);
		bStates=TellerStates.WaitingForAccount;
		withdraw=true;
	}
	
	private void WithdrawMoneyPt2(){
		if(requestedMoney<=currentAccount.getMoneyInAccount()){
			currentAccount.setMoneyInAccount(currentAccount.getMoneyInAccount() - requestedMoney);
			bank.msgUpdateAccount(currentAccount); 
	        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "Your withdrawal of " + (requestedMoney) + " dollars was succesful.");
	        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "The remaining balance is now " + (currentAccount.getMoneyInAccount())+ " dollars. Have a nice day.");
		}
		else if(currentAccount.getMoneyInAccount()==0){
	        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "I'm sorry but there is no money in your account. Have a nice day.");
			requestedMoney=0;
		}
		else if(requestedMoney>currentAccount.getMoneyInAccount()){
			requestedMoney=currentAccount.getMoneyInAccount();
			currentAccount.setMoneyInAccount(currentAccount.getMoneyInAccount() - requestedMoney);
			bank.msgUpdateAccount(currentAccount); 
	        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "I was only able to withdraw " + (requestedMoney) + " dollars from your account.");
	        AlertLog.getInstance().logMessage(AlertTag.BANK_TELLER, getAgentName(), "The remaining balance is now " + (currentAccount.getMoneyInAccount())+ " dollars. Have a nice day.");
	        
		}

		currentCustomer.msgHereIsYourMoney(requestedMoney);
		if(doneWorking==false){
		invisHost.msgImAvailable(this);
		bStates=TellerStates.Available;    
		}
		else if(doneWorking==true){
			bStates=TellerStates.Leaving;
		}
	}
	
	private void DoLeaveBank(){                           
		////////////////////////////////////////GUI TO LEAVE BANK
		row = 1;
		col = 28;
		this.setNewDestination(row, col);
		
	}
	
	private void EndPuppetTransmission(){                            //end the puppet 
		this.getMaster().msgPuppetLeftBuilding();
		bank.msgTellerEndingShift(this);
	}


	@Override
	protected void actMoveStep(int r, int c) {
		// TODO Auto-generated method stub
		tellGui.setDestination(r, c);
	}

	@Override
	protected void actReactToBlockedRoute() {
		// TODO Auto-generated method stub
		this.actWaitRandomAndTryAgain();
	}

	@Override
	protected void actArrivedAtDestination() {
		// TODO Auto-generated method stub
		if(bStates==TellerStates.MovingIn){
			bStates=TellerStates.NotifySignIn;
			this.pickAndExecuteAction();
		}
		
		if (bStates==TellerStates.MovingOut){
			bStates=TellerStates.Left;
			mainGui.removeGui(tellGui);
			this.pickAndExecuteAction();
		}
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
}
