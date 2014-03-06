package city;

import java.util.Random;

import city.Puppet.PuppetType;
import city.apartment.Apartment;
import city.bus.BusStop;
import city.bus.interfaces.BusStopInterface;
import city.gui.PersonGui;
import city.market.Market.Food;
import UnitTestingCommon.interfaces.BuildingInterface;
import UnitTestingCommon.interfaces.PersonInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import astar.Moveable;
import astar.MovementManager;

public class Person extends Moveable implements PersonInterface{
	private static final int PERSON_SQUARE_MAX=2;
	private static final int PERSON_SIZE=1;
	private static final int CAR_SQUARE_MAX=1;
	private static final int CAR_SIZE=2;
	private static final int HUNGER_TIME_MULTIPLIER=1;
	
	private final PersonGui gui;
	private final Random generator;
	
	/* attributes */
	private final BuildingInterface home;
	private final BuildingInterface workplace;
	private final BuildingInterface bank;
	private final BuildingInterface[] markets;
	private final BuildingInterface[] restaurants;
	private final BusStop[] busStops;
	private final PuppetType job;
	private final int apartmentRoomNumber;
	private final int workStartTime;
	private final int workStopTime;
	private final int hungerThresh;
	private final double moneyLow;
	private final double moneyHigh;
	private final int[] foodInventory;
	
	/* state variables */
	private Task task;
	private long currentTime;
	private int hungerLevel;
	private double money;
	private double accountBalance;
	private boolean waitingForPuppetToLeave;
	private boolean hasCar;
	private boolean paidRentThisMonth;
	private boolean workedToday;
	private boolean triedBankToday;
	private final boolean[] triedMarketToday;
	private boolean boughtNewGroceries;
	private BusState busState;
	private BusStop stopDest;
	private PuppetInterface currentPuppet;
	private BuildingInterface dest;

	public Person(String name,MovementManager m,int r,int c,Setup s){
		super(name,m,r,c);
		gui=new PersonGui(this,s.hasCar);
		gui.setPosition(r,c);
		generator=new Random();
		
		home=s.home;
		apartmentRoomNumber=s.apartmentRoomNumber;
		workplace=s.workplace;
		bank=s.bank;
		markets=s.markets;
		restaurants=s.restaurants;
		busStops=s.busStops;
		job=s.job;
		workStartTime=s.workStartTime;
		workStopTime=s.workStopTime;
		hungerThresh=s.hungerThresh;
		moneyLow=s.moneyLow;
		moneyHigh=s.moneyHigh;
		foodInventory = new int[Food.getNumberOfFoods()];	// starts empty
		for (int i=0;i<Food.getNumberOfFoods();i++) {
			foodInventory[i]=0;
		}
		task=Task.doNothing;
		currentTime=s.currentTime;
		hungerLevel=0;
		money=s.startingMoney;
		accountBalance=s.startingBalance;
		waitingForPuppetToLeave=false;
		hasCar=s.hasCar;
		paidRentThisMonth=false;
		workedToday=false;
		triedBankToday=false;
		triedMarketToday=new boolean[]{false,false,false};
		boughtNewGroceries=false;
		busState=BusState.none;
		currentPuppet=null;
		dest=null;
		stopDest=null;
	}
	
	public PersonGui getGui(){
		return gui;
	}
	
	protected boolean pickAndExecuteAction(){
		if(currentTime%(Broadcaster.WEEK_DAYS*Broadcaster.DAY_HOURS)==0){
			if(home.getClass()==Apartment.class && !paidRentThisMonth){
				paidRentThisMonth=true;
				money-=100.0;
				((Apartment)home).msgTenantPayingRent(this,100.0);
				return true;
			}
		}
		
		if(waitingForPuppetToLeave){
			return false;
		}
		
		if(busState==BusState.waitingAtBusStop || busState==BusState.ridingBus){
			return false;
		}
		
		final long time=currentTime%Broadcaster.DAY_HOURS;
		
		if(currentPuppet==null){
			if(!workedToday &&time>=workStartTime && time<workStopTime){
				if(task!=Task.goToWork){
					task=Task.goToWork;
					dest=workplace;
					actGetTo(dest);
					return true;
				}
			}
			
			if(task==Task.doNothing){
				if(!triedBankToday && money<=moneyLow && accountBalance>0.0){
					task=Task.withdrawMoney;
					dest=bank;
					final BlockingData<Boolean> closed=new BlockingData<Boolean>();
					dest.msgAskIfBuildingIsClosed(closed);
					if(closed.get()){
						task=Task.doNothing;
						dest=null;
						triedBankToday=true;
						return true;
					}
					actGetTo(dest);
					return true;
				}
					
				if(!triedBankToday && money>=moneyHigh){
					task=Task.depositMoney;
					dest=bank;
					final BlockingData<Boolean> closed=new BlockingData<Boolean>();
					dest.msgAskIfBuildingIsClosed(closed);
					if(closed.get()){
						task=Task.doNothing;
						dest=null;
						triedBankToday=true;
						return true;
					}
					actGetTo(dest);
					return true;
				}
				
				if(hungerLevel>=hungerThresh){
					task=Task.eatFood;
					dest=restaurants[generator.nextInt(restaurants.length)];
					final BlockingData<Boolean> closed=new BlockingData<Boolean>();
					dest.msgAskIfBuildingIsClosed(closed);
					if(closed.get()){
						dest=home;
					}
					actGetTo(dest);
					return true;
				}
				
				int rand=generator.nextInt(this.triedMarketToday.length);
				if(!triedMarketToday[rand] && needsToBuyFood()){
					task=Task.buyFood;
					dest=markets[rand];
					final BlockingData<Boolean> closed=new BlockingData<Boolean>();
					dest.msgAskIfBuildingIsClosed(closed);
					if(closed.get()){
						task=Task.doNothing;
						dest=null;
						triedMarketToday[rand]=true;
						return true;
					}
					actGetTo(dest);
					return true;
				}
				
				task=Task.goHome;
				dest=home;
				actGetTo(dest);
				return true;
			}
			
			return false;
		}
		
		if(!workedToday &&time>=workStartTime && time<workStopTime){
			if(dest!=workplace){
				currentPuppet.msgLeaveBuilding();
				waitingForPuppetToLeave=true;
				return true;
			}
		}
		
		return false;
	}
	
	public void msgUpdateTime(final long time){
		enqueMutation(new Mutation(){
			public void apply(){
				currentTime=time;
				if(time%6==0){
					hungerLevel++;
					triedBankToday=false;
					for(int i=0;i<triedMarketToday.length;i++){
						triedMarketToday[i]=false;
					}
				}
				if(time%Broadcaster.DAY_HOURS==0){
					workedToday=false;
				}
			}
		});
	}
	
	public void msgAddMoney(final double amount){
		enqueMutation(new Mutation(){
			public void apply(){
				money+=amount;
			}
		});
	}
	
	public void msgUpdateMoney(final double amount){
		enqueMutation(new Mutation(){
			public void apply(){
				money=amount;
			}
		});
	}
	
	public void msgUpdateAccountBalance(final double balance){
		enqueMutation(new Mutation(){
			public void apply(){
				accountBalance=balance;
			}
		});
	}
	
	public void msgAteFood(){
		enqueMutation(new Mutation(){
			public void apply(){
				hungerLevel=0;
			}
		});
	}
	
	public void msgPutAwayGroceries(){
		enqueMutation(new Mutation(){
			public void apply(){
				boughtNewGroceries=false;
			}
		});
	}
	
	public void msgUpdateInventory(final int[] quantities){
		enqueMutation(new Mutation(){
			public void apply(){
				for (int i=0;i<Food.getNumberOfFoods();i++){
					if(quantities[i]>foodInventory[i]){
						boughtNewGroceries=true;
					}
					foodInventory[i]=quantities[i];
				}				
			}
		});
	}
	
	public void msgPuppetLeftBuilding(){
		enqueMutation(new Mutation(){
			public void apply(){
				waitingForPuppetToLeave=false;
				currentPuppet=null;
			}
		});
	}
	
	public void msgRideBus(){
		enqueMutation(new Mutation(){
			public void apply(){
				busState=BusState.ridingBus;
			}
		});
	}
	
	public void msgBusArrived(final BlockingData<Boolean> gettingOff,final BusStopInterface busStops2){
		enqueMutation(new Mutation(){
			public void apply(){
				for(int i=0;i<busStops2.getLength();i++){
					if(busStops2.getNearBuilding(i)==dest){
						gettingOff.unblock(true);
						Person.this.removeFromManager();
						Person.this.addToManager(PERSON_SQUARE_MAX,PERSON_SIZE,busStops2.getBuildingRow(),busStops2.getBuildingCol());
						gui.setPosition(busStops2.getBuildingRow(),busStops2.getBuildingCol());
						setNewDestination(dest.getEntrance(false).r,dest.getEntrance(false).c);
						busState=BusState.none;
						stopDest=null;
						return;
					}
				}
				gettingOff.unblock(false);
			}
		});
	}

	protected void actMoveStep(int r,int c){
		gui.setDestination(r,c);
	}
	
	protected void actReactToBlockedRoute(){
		actWaitRandomAndTryAgain();
	}
	
	private void actGetTo(BuildingInterface dest2){
		if(hasCar){
			setNewDestination(dest2.getEntrance(true).r,dest2.getEntrance(true).c);
			return;
		}
		if(generator.nextBoolean()){
			BusStop nearest=nearestBusStop();
			for(int i=0;i<nearest.getLength();i++){
				if(nearest.getNearBuilding(i)==dest2){
					setNewDestination(dest2.getEntrance(false).r,dest2.getEntrance(false).c);
					return;
				}
			}
			busState=BusState.goingToBusStop;
			stopDest=nearest;
			setNewDestination(nearest.getBuildingRow(),nearest.getBuildingCol());
			return;
		}
		setNewDestination(dest2.getEntrance(false).r,dest2.getEntrance(false).c);
	}
	
	protected void actArrivedAtDestination(){
		if(busState==BusState.goingToBusStop){
			busState=BusState.waitingAtBusStop;
			stopDest.msgWaitingForBus(this);
			return;
		}
		
		final BlockingData<PuppetInterface> puppet=new BlockingData<PuppetInterface>();
		final Puppet.Setup pkg;
		
		if(task==Task.goToWork && dest==workplace){
			task=Task.doNothing;
			workedToday=true;
			pkg=buildPuppetSetupPackage(job);
			dest.msgSpawnPuppet(puppet,getAgentName()+"p",pkg);
			currentPuppet=puppet.get();
			return;
		}
		
		if(task==Task.depositMoney && dest==bank){
			task=Task.doNothing;
			pkg=buildPuppetSetupPackage(PuppetType.customer);
			pkg.withdrawingTrueDepositingFalse=false;
			dest.msgSpawnPuppet(puppet,getAgentName()+"p",pkg);
			currentPuppet=puppet.get();
			return;
		}
		
		if(task==Task.withdrawMoney && dest==bank){
			task=Task.doNothing;
			pkg=buildPuppetSetupPackage(PuppetType.customer);
			pkg.withdrawingTrueDepositingFalse=true;
			dest.msgSpawnPuppet(puppet,getAgentName()+"p",pkg);
			currentPuppet=puppet.get();
			return;
		}
		
		if(task==Task.buyFood){
			task=Task.doNothing;
			pkg=buildPuppetSetupPackage(PuppetType.customer);
			pkg.foodsToBuyQuantities=new int[foodInventory.length];
			for(int i=0;i<foodInventory.length;i++){
				pkg.foodsToBuyQuantities[i]=0;
			}
			for(int i=0;i<5;i++){
				pkg.foodsToBuyQuantities[generator.nextInt(foodInventory.length)]++;
			}
			dest.msgSpawnPuppet(puppet,getAgentName()+"p",pkg);
			currentPuppet=puppet.get();
			return;
		}
		
		if(task==Task.eatFood){
			task=Task.doNothing;
			if(dest==home){
				pkg=buildPuppetSetupPackage(PuppetType.resident);
			}
			else if(dest!=null){
				pkg=buildPuppetSetupPackage(PuppetType.customer);
			}
			else{
				return;
			}
			pkg.minutesMealDuration=hungerLevel*HUNGER_TIME_MULTIPLIER;
			dest.msgSpawnPuppet(puppet,getAgentName()+"p",pkg);
			currentPuppet=puppet.get();
			return;
		}
		
		if(task==Task.goHome && dest==home){
			task=Task.doNothing;
			pkg=buildPuppetSetupPackage(PuppetType.resident);
			dest.msgSpawnPuppet(puppet,getAgentName()+"p",pkg);
			currentPuppet=puppet.get();
			return;
		}
	}
	
	private Puppet.Setup buildPuppetSetupPackage(PuppetType role){
		Puppet.Setup pkg=new Puppet.Setup();
		pkg.master=this;
		pkg.role=role;
		pkg.money=money;
		pkg.boughtNewGroceries=this.boughtNewGroceries;
		pkg.foodInventory=new int[foodInventory.length];
		for(int i=0;i<foodInventory.length;i++){
			pkg.foodInventory[i]=foodInventory[i];
		}
		pkg.apartmentRoomNumber=apartmentRoomNumber;
		return pkg;
	}
	
	private BusStop nearestBusStop(){
		int dr=this.getRow()-busStops[0].getBuildingRow();
		int dc=this.getCol()-busStops[0].getBuildingCol();
		double minDist=(double)(dr*dr+dc*dc);
		int minIndex=0;
		for(int i=1;i<busStops.length;i++){
			dr=this.getRow()-busStops[i].getBuildingRow();
			dc=this.getCol()-busStops[i].getBuildingCol();
			double dist=(double)(dr*dr+dc*dc);
			if(dist<minDist){
				minDist=dist;
				minIndex=i;
			}
		}
		return busStops[minIndex];
	}
	
	private boolean needsToBuyFood(){
		for(int i=0;i<foodInventory.length;i++){
			if(foodInventory[i]<=0){
				return true;
			}
		}
		return false;
	}
	
	private enum BusState{
		none,goingToBusStop,waitingAtBusStop,ridingBus;
	}
	
	private enum Task{
		doNothing,goHome,goToWork,depositMoney,withdrawMoney,eatFood,buyFood;
	}
	
	public static class Setup{
		public BuildingInterface home;
		public BuildingInterface workplace;
		public BuildingInterface bank;
		public BuildingInterface[] markets;
		public BuildingInterface[] restaurants;
		public BusStop[] busStops;
		public PuppetType job;
		public int apartmentRoomNumber;
		public int workStartTime;
		public int workStopTime;
		public int hungerThresh;
		public double moneyLow;
		public double moneyHigh;
		
		public double startingMoney;
		public double startingBalance;
		public boolean hasCar;
		public long currentTime;
	}

	protected void destructor(){
		// TODO Auto-generated method stub
		
	}
}
