package city.house;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import city.Broadcaster;
import city.Building;
import city.Puppet;
import city.house.gui.ResidentGui;
import city.market.Market.Food;
import astar.GridCell;
import astar.MovementManager;

public class Resident extends Puppet{
	private final ResidentGui gui;
	private final Random generator;
	private final int eatTimerDuration;
	private final int[] foodInventory;
	private final boolean boughtNewGroceries;
	
	private boolean ateFood;
	private boolean groceriesPutAway;
	
	private Task task;
	private int index;

	public Resident(String name,MovementManager m,Building b,Setup s){
		super(name,m,b,s);
		gui=new ResidentGui(this);
		gui.setPosition(Dest.spawn.row,Dest.spawn.col);
		generator=new Random();
		eatTimerDuration=s.minutesMealDuration;
		foodInventory=s.foodInventory;
		boughtNewGroceries=s.boughtNewGroceries;
		ateFood=false;
		groceriesPutAway=false;
		task=Task.none;
		index=-1;
	}
	
	public ResidentGui getGui(){
		return gui;
	}
	
	private House getHouse(){
		return (House)getBuilding();
	}

	protected boolean pickAndExecuteAction(){
		if(index==-1){
			index=0;
			
			if(task==Task.leaveHouse){
				setNewDestination(task.getDest(0).row,task.getDest(0).col);
				return true;
			}
			
			if(task==Task.putAwayGroceries){
				setNewDestination(task.getDest(0).row,task.getDest(0).col);
				return true;
			}
			
			if(task==Task.eatFood){
				setNewDestination(task.getDest(0).row,task.getDest(0).col);
				return true;
			}
			
			if(task==Task.cleanHouse){
				setNewDestination(task.getDest(0).row,task.getDest(0).col);
				return true;
			}
			
			if(task==Task.none){
				setNewDestination(task.getDest(0).row,task.getDest(0).col);
				return true;
			}
		}
		
		if(task==Task.none && boughtNewGroceries && !groceriesPutAway){
			groceriesPutAway=true;
			task=Task.putAwayGroceries;
			index=-1;
			return true;
		}
		
		if(task==Task.none && eatTimerDuration>0 && !ateFood){
			ateFood=true;
			task=Task.eatFood;
			index=-1;
			return true;
		}
		
		return false;
	}
	
	public void msgLeaveBuilding(){
		enqueMutation(new Mutation(){
			public void apply(){
				task=Task.leaveHouse;
				index=-1;
			}
		});
	}
	
	public void msgHouseDirty(){
		enqueMutation(new Mutation(){
			public void apply(){
				task=Task.cleanHouse;
				index=-1;
			}
		});
	}

	protected void actMoveStep(int r,int c){
		gui.setDestination(r,c);
	}

	protected void actReactToBlockedRoute(){
		this.actWaitRandomAndTryAgain();
	}

	protected void actArrivedAtDestination(){
		if(index>=0 && index<task.length()-1){
			if(task==Task.eatFood){
				if(task.getDest(index)==Dest.fridge){
					actChooseFood();
					return;
				}
				
				if(task.getDest(index)==Dest.stove){
					delay(5*Broadcaster.getMinuteMillis());
					index++;
					setNewDestination(task.getDest(index).row,task.getDest(index).col);
					return;
				}
				
				if(task.getDest(index)==Dest.table){
					delay(eatTimerDuration*Broadcaster.getMinuteMillis());
					index++;
					setNewDestination(task.getDest(index).row,task.getDest(index).col);
					return;
				}
				
				return;
			}
			
			if(task==Task.cleanHouse){
				delay(Broadcaster.getMinuteMillis());
				index++;
				setNewDestination(task.getDest(index).row,task.getDest(index).col);
				return;
			}
			
			return;
		}
		
		if(index==task.length()-1){
			if(task==Task.leaveHouse){
				stopAgent();
				return;
			}
			
			if(task==Task.putAwayGroceries){
				getMaster().msgPutAwayGroceries();
				delay(Broadcaster.getMinuteMillis());
				task=Task.none;
				index=-1;
				return;
			}
			
			if(task==Task.eatFood){
				delay(2*Broadcaster.getMinuteMillis());
				task=Task.none;
				index=-1;
				return;
			}
			
			if(task==Task.cleanHouse){
				delay(500);
				task=Task.none;
				index=-1;
				return;
			}
		}
	}
	
	private void actChooseFood(){
		final List<Food> food=new ArrayList<Food>();
		for(int i=0;i<foodInventory.length;i++){
			if(foodInventory[i]>0){
				food.add(Food.getFood(i));
			}
		}
		if(!food.isEmpty()){
			delay(Broadcaster.getMinuteMillis());
			Food f=food.get(generator.nextInt(food.size()));
			foodInventory[f.index]--;
			getMaster().msgUpdateInventory(foodInventory);
			delay(Broadcaster.getMinuteMillis());
			index++;
			setNewDestination(task.getDest(index).row,task.getDest(index).col);
			return;
		}
		task=Task.none;
		index=-1;
		return;
	}
	
	private enum Task{
		none(Dest.bed),
		putAwayGroceries(Dest.fridge),
		eatFood(Dest.fridge,Dest.stove,Dest.table,Dest.sink),
		cleanHouse(Dest.clean0,Dest.clean1,Dest.clean2,Dest.clean3,Dest.clean4),
		leaveHouse(Dest.spawn);
		
		private final Dest[] chain;
		
		private Task(Dest...d){
			chain=d;
		}
		
		public Dest getDest(int i){
			return chain[i];
		}
		
		public int length(){
			return chain.length;
		}
	}
	
	private enum Dest{
		spawn(28,22),table(11,8),fridge(4,27),stove(4,20),sink(4,15),bed(24,6),
		clean0(3,3),clean1(27,2),clean2(27,12),clean3(4,17),clean4(17,22);
		
		public final int row;
		public final int col;
		
		private Dest(int r,int c){
			row=r;
			col=c;
		}
	}

	protected void destructor(){
		getMaster().msgPuppetLeftBuilding();
		((House)getBuilding()).msgRemoveResident(this);
	}
}
