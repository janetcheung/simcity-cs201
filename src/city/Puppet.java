package city;

import UnitTestingCommon.interfaces.BuildingInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import astar.Moveable;
import astar.MovementManager;

public abstract class Puppet extends Moveable implements PuppetInterface{
	private final Person master;
	private final BuildingInterface facility;
	
	public Puppet(String name,MovementManager m,BuildingInterface b,Puppet.Setup s) {
		super(name,m,0,0);
		facility=b;
		master=s.master;
	}
	
	protected final Person getMaster(){
		return master;
	}
	
	protected final BuildingInterface getBuilding(){
		return facility;
	}
	
	public abstract void msgLeaveBuilding();
	
	public enum PuppetType{
		resident,
		customer,
		bankTeller,bankRobber,
		markClerk,
		restHost,restRegWaiter,restStandWaiter,restAnyWaiter,restCook,restCashier,
	}
	
	
	
	public static class Setup {
		public Person master;
		public PuppetType role;
		public double money;
		public int minutesMealDuration;
		public boolean withdrawingTrueDepositingFalse;
		public int[] foodsToBuyQuantities;
		public boolean foodPreferenceIsStrong;
		public String foodPreference;
		public boolean isFlake;
		public int minutesWaitingPatience;
		public int apartmentRoomNumber;
		public int[] foodInventory;
		public boolean boughtNewGroceries;
		// More fields might need to be added here
		
		
		public void copyTo(Setup s2) {
			s2.master = master;
			s2.role = role;
			s2.money = money;
			s2.minutesMealDuration = minutesMealDuration;
			s2.withdrawingTrueDepositingFalse = withdrawingTrueDepositingFalse;
			//s2.shoppingList = shoppingList;
			s2.foodsToBuyQuantities = foodsToBuyQuantities;
			s2.foodPreferenceIsStrong = foodPreferenceIsStrong;
			s2.foodPreference = foodPreference;
			s2.isFlake = isFlake;
			s2.minutesWaitingPatience = minutesWaitingPatience;
			s2.apartmentRoomNumber = apartmentRoomNumber;
			s2.foodInventory = foodInventory;
			s2.boughtNewGroceries = boughtNewGroceries;
		}
		
	}
}
