package city.bank;

import java.util.ArrayList;
import java.util.List;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.BankInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import UnitTestingCommon.interfaces.TellerInterface;
import astar.MovementManager;
import city.Building;
import city.Person;
import city.Puppet;
import city.Broadcaster;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.bank.gui.BankGui;
import city.restaurant.Restaurant;

public class Bank extends Building implements BankInterface{

	private List<TellerRequest>        tellerNeedsAccount = new ArrayList<TellerRequest>();

	private List<Account>   storedAccounts = new ArrayList<Account>();
	private List<RestaurantAccount>  restaurantAccounts = new ArrayList<RestaurantAccount>();

	private List<Counters> listOfCounters = new ArrayList<Counters>();
	private SignInSheet signIn;
	private List<Restaurant>     restaurants = new ArrayList<Restaurant>();

	private List<TellerAgent>         tellersEndingShift = new ArrayList<TellerAgent>();
	private List<TellerAgent>         currentTellers = new ArrayList<TellerAgent>();
	//private List<BankCustomer>        currentCustomers = new ArrayList<BankCustomer>();
	//private final BankGui gui;
	
	private List<RestaurantRequest>    withdrawRequest = new ArrayList<RestaurantRequest>();
	private List<RestaurantRequest>    depositRequest = new ArrayList<RestaurantRequest>();
	private List<Account>              updateTheseAccounts = new ArrayList<Account>();
	private boolean   busy=false;
	

	int timeSlot1 = 0;
	int timeSlot2 = 0;
	//Constructors and SETTERS////////////////////


	public Bank(String name,int pr,int pc,int cr,int cc,int tlr,int tlc,int brr,int brc){
		super(name,pr,pc,cr,cc,new MovementManager(name+" MovementManager",insideNumRows,insideNumCols,"bankLayout.txt"),tlr,tlc,brr,brc, new BankGui());

		signIn=new SignInSheet("SignIn");
		signIn.startAgent();
		
		Counters c1 = new Counters(0);
		listOfCounters.add(c1);
		Counters c2 = new Counters(1);
		listOfCounters.add(c2);
		Counters c3 = new Counters(2);
		listOfCounters.add(c3);
		Counters c4 = new Counters(3);
		listOfCounters.add(c4);

	}


	public void setRestaurants(Restaurant r){
		restaurants.add(r);
	}

	///////////////////TEST FUNCTIONS///////////////////////////////////////////

	public int getStoredAccountsSize() {
		return storedAccounts.size();
	}

	public int getStoredRestaurantAccountSize(){
		return restaurantAccounts.size();
	}

	public Account createTestAccount(Person p, int money){
		Account temp2=new Account(p, money);
		return temp2;
	}

	public int getRestuarantCashTest(Restaurant r){
		int temp2 =-1;
		for (int i=0; i<restaurantAccounts.size();i++){
			if(restaurantAccounts.get(i).getRestaurant()==r){
				temp2=restaurantAccounts.get(i).getMoneyInAccount();
			}
		}
		return temp2;
	}

	public int getAccountCashAmountTest(Person p){
		int temp =-1;
		for (int i=0; i<storedAccounts.size(); i++){
			if(storedAccounts.get(i).getCust()==p){
				temp = storedAccounts.get(i).getMoneyInAccount();
			}
		}
		return temp;
	}

	/////////////////////////////////////////////////////////////////////////////////

	private class RestaurantRequest{
		private Restaurant  r;
		private int        cashAmount;

		RestaurantRequest(Restaurant rest, int money){                    //made for easy access lists to call scheduler 
			this.r=rest;
			this.cashAmount=money;
		}

		public int getCashAmount() {
			return cashAmount;
		}

		public Restaurant getRestaurant() {
			return r;
		}

	}

	private class Counters{
		private int counterNumber;
		private boolean isOccupied=false;
		
		Counters(int c){
			this.counterNumber=c;
			isOccupied=false;                                              //made to keep track of the four working bank counters
		}
		
		public int getCounterNumber() {
			return counterNumber;
		}
		
		public boolean checkIsOccupied(){
			if(isOccupied==true){
				return true;
			}
			else{
				return false;
			}
		}
		
		public void setOccupied() {
			this.isOccupied=true;
		}
		
		public void setUnoccupied(){
			this.isOccupied=false;
		}
	}
	
	public class Account{
		private Person    cust;
		private int       moneyInAccount;

		Account(Person p, int amt){
			this.cust=p;
			this.moneyInAccount=amt;
		}

		public Person getCust() {                                                   //people accounts keep track of their id(person pointer) and money
			return cust;
		}

		public int getMoneyInAccount() {
			return moneyInAccount;
		}

		public void setMoneyInAccount(int moneyInAccount) {
			this.moneyInAccount = moneyInAccount;
		}
	}

	public class RestaurantAccount{
		private Restaurant    rest;
		private int       moneyInAccount;                                  //same as regular account but for restaurants

		RestaurantAccount(Restaurant r, int amt){
			this.rest=r;
			this.moneyInAccount=amt;
		}

		public Restaurant getRestaurant() {
			return rest;
		}

		public int getMoneyInAccount() {
			return moneyInAccount;
		}

		public void setMoneyInAccount(int moneyInAccount) {
			this.moneyInAccount = moneyInAccount;
		}
	}	


	private class TellerRequest{
		private TellerInterface   t;                                             //made for easy to access lists when teller needs an account
		private Person          p;

		public TellerRequest(TellerInterface ta, Person pers){
			this.t=ta;
			this.p=pers;
		}

		public Person getP(){
			return p;
		}

		public TellerInterface getT(){
			return t;
		}
	}




	//MESSAGES///////////////////////////

	@Override
	public void msgSpawnPuppet(final BlockingData<PuppetInterface> result,final String name,final Setup setupPackage) {
		enqueMutation(new Mutation(){
			public void apply(){
				// TODO
				if ((myClosedState == ClosedState.closed || myClosedState == ClosedState.forceClosed)&& setupPackage.role!=PuppetType.bankRobber) {
	                AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Closed -- denying entrance to " + setupPackage.role + " puppet");

					result.unblock(null);
					return;                                           //if closed, only bank robbers can get in
				}
				
				
				
				switch (setupPackage.role) {                                          //determines whether to spawn a bank customer or teller
				case customer:
					//create BankCustomer					
					//initialize signInSheet
					//add to currentCustomers
					BankCustomer customerTemp = new BankCustomer(name, getMovementManager(),Bank.this,setupPackage);
					customerTemp.setSignIn(signIn);
					gui.addGui(customerTemp.getCustomerGui());
					customerTemp.setMainGui((BankGui)gui);
					//	currentCustomers.add(customerTemp);
					getMovementManager().msgAddUnit(customerTemp,1,1,28,28);
					customerTemp.startAgent();
					result.unblock(customerTemp);
					break;
				case bankTeller:
					//create TellerAgent
					//initialize signInSheet
					//add to currentTellers
					//send message to signIn
					int tableLocation = 2;
					for (int i =0;i<listOfCounters.size();i++){
						if(listOfCounters.get(i).isOccupied==false){
							tableLocation = i;
							listOfCounters.get(i).setOccupied();
							break;
						}
					}
					TellerAgent tellerTemp = new TellerAgent(name, getMovementManager(), Bank.this, setupPackage, tableLocation);
					gui.addGui(tellerTemp.getGui());
					tellerTemp.setMainGui((BankGui)gui);
					tellerTemp.setSignInSheet(signIn);
					currentTellers.add(tellerTemp);
					signIn.msgAddClerkToWorkingList(tellerTemp);
					getMovementManager().msgAddUnit(tellerTemp,1,1,1,1);
					tellerTemp.startAgent();
					result.unblock(tellerTemp);
					break;
				case bankRobber:
					BankRobber robberTemp = new BankRobber(name, getMovementManager(), Bank.this, setupPackage);
					gui.addGui(robberTemp.getGui());
					robberTemp.setMainGui((BankGui)gui);
					getMovementManager().msgAddUnit(robberTemp, 1, 1, 1, 28);
					robberTemp.startAgent();
					result.unblock(robberTemp);
					break;
				default:
					System.err.println("ERROR: Unknown role specified for new Puppet in Bank.");
					break;

				}
			}
		});
	}

	public void msgCreateAccountPerson(final Person p, final int money){
		enqueMutation(new Mutation(){                                              //creates a person bank account
			public void apply(){
				Account temp=new Account(p, money);
				storedAccounts.add(temp); 
                AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Succesfully created an account for newly added person.");

			}
		});	
	}

	public void msgCreateAccountRestaurant(final Restaurant r, final int money){
		enqueMutation(new Mutation(){
			public void apply(){                                                            //creates a restaurant account
				RestaurantAccount temp=new RestaurantAccount(r, money);
				restaurantAccounts.add(temp); 
                AlertLog.getInstance().logInfo(AlertTag.BANK, getAgentName(), "Succesfully created an account for the restaurant");

			}
		});	
	}

	public void	msgPleaseSendAccount(final Person p, final TellerInterface t){
		enqueMutation(new Mutation(){                                                        //teller requesting person account
			public void apply(){
				TellerRequest temp=new TellerRequest(t, p);
				tellerNeedsAccount.add(temp);
			}
		});
	}

	public void	msgRestaurantWithdrawRequest(final Restaurant r, final double amount) {
		enqueMutation(new Mutation(){                                                          //restaurant requesting money
			public void apply(){
				RestaurantRequest temp= new RestaurantRequest(r, (int) amount);
				withdrawRequest.add(temp);

			}
		});
	}

	public void	msgRestaurantDepositRequest(final Restaurant r,final double amount)  {
		enqueMutation(new Mutation(){
			public void apply(){                                                              //restaurant depositing money
				RestaurantRequest temp= new RestaurantRequest(r, (int) amount);
				depositRequest.add(temp);

			}
		});	
	}  

	public void	msgUpdateAccount(final Account a) {                                             //teller updated person account
		enqueMutation(new Mutation(){
			public void apply(){
				updateTheseAccounts.add(a);
			}
		});	
	}


	public void msgTellerEndingShift(final TellerAgent t){
		enqueMutation(new Mutation(){                                                      //teller is no longer on shift
			public void apply(){
				tellersEndingShift.add(t);
			}
		});
	}




	@Override
	public boolean pickAndExecuteAction() {
		// TODO handle when myClosedState == ClosedState.needToForceClose
		// (this happens when the Building method msgForceClose() or msgOpenForBusiness() gets called)

		if (myClosedState == ClosedState.needToForceClose) {
			this.emergencyShutDown();
			return true;
		}
		
		if (!updateTheseAccounts.isEmpty() && !busy) {
			busy=true;
			updateAccount();                        
			return true;
		}

		if (!tellersEndingShift.isEmpty() && !busy){
			busy=true;
			payAndDismissTeller();
			return true;
		}
		
		if (!withdrawRequest.isEmpty() && !busy)  {
			busy=true;
			withdrawRestaurantMoney();          
			return true;
		}

		if (!depositRequest.isEmpty() && !busy)   {
			busy=true;
			depositRestaurantMoney();          
			return true;
		}

		if (!tellerNeedsAccount.isEmpty() && !busy) {
			busy=true;
			fetchCustomerAccount();                 
			return true;
		}

		return false;
	}


	//ACTIONS///////////////////////



	private void withdrawRestaurantMoney(){                              //withdrawing restaurant money
		for(int i=0;i<restaurantAccounts.size(); i++){
			if(withdrawRequest.get(0).getRestaurant()==restaurantAccounts.get(i).getRestaurant()){
				int moneyInAction = restaurantAccounts.get(i).getMoneyInAccount()- withdrawRequest.get(0).getCashAmount();

				if(moneyInAction<0){
					moneyInAction = restaurantAccounts.get(i).getMoneyInAccount();
					restaurantAccounts.get(i).setMoneyInAccount(0);
					withdrawRequest.get(0).getRestaurant().msgBankSentMoney(moneyInAction);
	                AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "I'm sorry, this restaurant's account only had "+ (moneyInAction)+" dollars.");
	                AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Here is your money, account balance is now 0.");

				}
				else if(moneyInAction>=0){
					restaurantAccounts.get(i).setMoneyInAccount(moneyInAction);
					withdrawRequest.get(0).getRestaurant().msgBankSentMoney(withdrawRequest.get(0).getCashAmount());
	                AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Here is the money requested by restaurant.");

	                AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "This restaurant's balance is now " + (restaurantAccounts.get(i).getMoneyInAccount())+" dollars.");

				}
			}
		}
		//  -search for restaurant in accounts
		//  -withdraw money
		//  -message restaurant the requested money

		withdrawRequest.remove(0);
		busy=false;
	}

	private void depositRestaurantMoney(){            //depositing restaurant money
		for(int i=0;i<restaurantAccounts.size(); i++){
			if(depositRequest.get(0).getRestaurant()==restaurantAccounts.get(i).getRestaurant()){
				int moneyInAction = restaurantAccounts.get(i).getMoneyInAccount()+ depositRequest.get(0).getCashAmount();
				restaurantAccounts.get(i).setMoneyInAccount(moneyInAction);
			}
		}

		//  -search for restaurant in accounts
		//  -deposit money
        AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Money from restaurant was succesfully deposited.");
		depositRequest.remove(0);
		busy=false; 
	}

	private void updateAccount(){                      //updating person account
		for(int i=0; i<storedAccounts.size(); i++){
			if (storedAccounts.get(i).getCust()==updateTheseAccounts.get(0).getCust()){
				storedAccounts.get(i).setMoneyInAccount(updateTheseAccounts.get(0).getMoneyInAccount());
			}
		}
        AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", (updateTheseAccounts.get(0).getCust().getAgentName())+"'s account was succesfully updated.");
		updateTheseAccounts.remove(0);
		busy=false; 
	}

	private void fetchCustomerAccount(){                          //send teller requested account
		for(int i=0; i<storedAccounts.size(); i++){
			if (storedAccounts.get(i).getCust()==tellerNeedsAccount.get(0).getP()){
				Account tempAccount = storedAccounts.get(i);
		        AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Here is the account requested.");

				tellerNeedsAccount.get(0).getT().msgHereIsTheAccount(tempAccount);
			}
		}
		tellerNeedsAccount.remove(0);
		busy=false; 
	}


	private void payAndDismissTeller(){                          //end the tellers shift and pay him
		for(int i=0; i<currentTellers.size(); i++){
			if(currentTellers.get(i)==tellersEndingShift.get(0)){
				currentTellers.remove(i);
		        AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "Thank you for working today, here is your payment of 120 dollars");
			}
		}
		tellersEndingShift.get(0).getControllerId().msgAddMoney(120);
		listOfCounters.get(tellersEndingShift.get(0).getCounter()).setUnoccupied();
		tellersEndingShift.get(0).stopAgent();
		tellersEndingShift.remove(0);
		busy=false;
	}


	public void msgFillAnyOpening(                  //allow only 8 tellers to be hired, in two 4 hr shifts
		BlockingData<Integer> timeSlotIndex,
		BlockingData<PuppetType> jobType) {
		if(timeSlot1 !=4 || timeSlot2 !=4){	
			if(timeSlot1 > timeSlot2){
				timeSlot2++;
				timeSlotIndex.unblock(0);
				jobType.unblock(PuppetType.bankTeller);
			}
			
			else if (timeSlot1==timeSlot2){
				timeSlot1++;
				timeSlotIndex.unblock(1);
				jobType.unblock(PuppetType.bankTeller);
				
			}
		}
	
		else {
			jobType.unblock(null);
			timeSlotIndex.unblock(null);
		}
	
	}
	
	public void msgFillSpecifiedOpening(BlockingData<Integer> timeSlotIndex, PuppetType jobType) {
		
	}

	@Override
	public void msgUpdateTime(final long time) {
		enqueMutation(new Mutation() {
			public void apply() {
				if ( (myClosedState != ClosedState.forceClosed) ) {
					// bank is open every day
					// checks shift times
					for (int shiftIndex = 0; shiftIndex < Building.getNumberOfShifts(); shiftIndex++) {
						if (time % Broadcaster.DAY_HOURS == Building.getStartHour(shiftIndex)) {
							// Only start this shift if we have at least the minimum workers needed
							myClosedState=ClosedState.open;
						}
						if (time % Broadcaster.DAY_HOURS == Building.getEndHour(shiftIndex)) {
							// TODO close, not forced
							myClosedState=ClosedState.closed;
							for(int i = 0; i<currentTellers.size();i++){
								currentTellers.get(i).msgLeaveBuilding();
							}
						}
					}
				}
			}
		});
	}

	
	private void emergencyShutDown() {
		if (myClosedState == ClosedState.needToForceClose) {
			myClosedState = ClosedState.forceClosed;
			for (int i=0; i<currentTellers.size(); i++){
				currentTellers.get(i).msgLeaveBuilding();
			}
        AlertLog.getInstance().logInfo(AlertTag.BANK, "Bank", "The bank is being forced to shut down.");

		}
	}

	@Override
	public void msgClearAllOpenings() {
		// TODO is the following correct?
		timeSlot1 = 0;
		timeSlot2 = 0;
	}


	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}





	
	
	
}
