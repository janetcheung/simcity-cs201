package city.bank;

import java.util.ArrayList;
import java.util.List;

import UnitTestingCommon.interfaces.BankCustomerInterface;
import UnitTestingCommon.interfaces.SignInSheetInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import agent.Agent;

//This agent is the invisible host for the bank. Its job is to keep track of who is first in line, and sent that 
//person to the correct bank teller.
public class SignInSheet extends Agent implements SignInSheetInterface{

	public SignInSheet(String name) {
		super(name);
	}

	private List<TellerInterface> tellers = new ArrayList<TellerInterface>();
	private List<TellerInterface> openTellers = new ArrayList<TellerInterface>();
	private List<BankCustomerInterface> waitingCustomers = new ArrayList<BankCustomerInterface>();

	private int counterLoc;
	/////FOR TESTING PURPSOSES//////
	public int returnTellerListTest(){
		return tellers.size();
	}
	
	public int returnCustomerListTest(){
		return waitingCustomers.size();
	}
	
	public int returnActiveTellerListTest(){
		return openTellers.size();
	}

	//Messages/////////////

	public void msgWaitingInLine(final BankCustomerInterface b) {
		enqueMutation(new Mutation(){
			public void apply(){                                             //a customer is waiting in line
				waitingCustomers.add(b);
			}
		});
	}
	
	public void msgSickOfWaitingInLine(final BankCustomerInterface b) {
		enqueMutation(new Mutation(){
			public void apply(){                                             //a customer is waiting in line
				for(int i = 0; i<waitingCustomers.size(); i++){
					if(waitingCustomers.get(i)==b){
						waitingCustomers.remove(i);
						
					}
				}
			}
		});
	}

	public void msgImAvailable(final TellerInterface t) {
		enqueMutation(new Mutation(){                                       //a teller is available 
			public void apply(){
				openTellers.add(t);
			}
		});
	}

	public void msgAddClerkToWorkingList(final TellerInterface t) {
		enqueMutation(new Mutation(){                                       //updates the current tellers
			public void apply(){
				tellers.add(t);
			}
		});
	}

	public void msgRemoveClerkFromWorkingList(final TellerInterface t){
		enqueMutation(new Mutation(){
			public void apply(){
				for (int i=0;i<tellers.size();i++){                          //removes non working tellers
					if (tellers.get(i)==t){
						tellers.remove(i);
					}
				}
				
				for (int i=0;i<openTellers.size();i++){
					if (openTellers.get(i)==t){
						openTellers.remove(i);
					}
				}
			}
		});
	}

	@Override
	public boolean pickAndExecuteAction() {
		
		if(!waitingCustomers.isEmpty()){
			for(int i=0; i<openTellers.size(); i++){
				if(tellers.get(i).checkTellerReady()==true){
					counterLoc = openTellers.get(i).getCounter();
					openTellers.get(i).msgYouHaveACustomerComing();
					NotifyBankCustomers(openTellers.get(i), counterLoc);
					openTellers.remove(i);
					return true;
				}
			}
		}
		
		return false;
	}


	//ACTIONS////////////////////

	private void NotifyBankCustomers(TellerInterface t, int c){
		waitingCustomers.get(0).msgGoToTeller(t,c);
		waitingCustomers.remove(0);   
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}

}
