package city.house;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import UnitTestingCommon.interfaces.PuppetInterface;
import astar.MovementManager;
import city.Building;
import city.Puppet;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.gui.BuildingGui;
import city.house.gui.HouseGui;

public class House extends Building{
	private static final int MAX_RESIDENTS=2;
	private static final int DIRTY_THRESH=10;
	private static final int spawnRow=28;
	private static final int spawnCol=22;
	
	private final Random generator;
	private final List<Resident> currentPuppets;
	
	private Integer numberOfResidents;
	private int dirtyLevel;
	private boolean alreadyMessaged;
	
	public House(String name,int pr,int pc,int cr,int cc,int tlr,int tlc,int brr,int brc){
		super(name,pr,pc,cr,cc,new MovementManager(name+" MovementManager",insideNumRows,insideNumCols,"houseLayout.txt"),tlr,tlc,brr,brc,new HouseGui());
		generator=new Random();
		currentPuppets=new ArrayList<Resident>();
		
		numberOfResidents=0;
		dirtyLevel=0;
		alreadyMessaged=false;
	}

	public boolean pickAndExecuteAction(){
		if(!currentPuppets.isEmpty() && !alreadyMessaged && dirtyLevel>=DIRTY_THRESH){
			alreadyMessaged=true;
			currentPuppets.get(generator.nextInt(currentPuppets.size()));
			return true;
		}
		
		return false;
	}
	
	public void msgSpawnPuppet(final BlockingData<PuppetInterface> result,final String name,final Setup s) {
		enqueMutation(new Mutation(){
			public void apply(){
				if(s.role==PuppetType.resident){
					alreadyMessaged=false;
					Resident r=new Resident(s.master.getAgentName()+"p",getMovementManager(),House.this,s);
					currentPuppets.add(r);
					gui.addGui(r.getGui());
					getMovementManager().msgAddUnit(r,1,1,spawnRow,spawnCol);
					r.startAgent();
					result.unblock(r);
					return;
				}
				result.unblock(null);
			}
		});
	}
	
	public void msgRemoveResident(final Resident r){
		enqueMutation(new Mutation(){
			public void apply(){
				currentPuppets.remove(r);
				gui.removeGui(r.getGui());
				getMovementManager().msgRemoveUnit(r);
			}
		});
	}

	public void msgFillAnyOpening(final BlockingData<Integer> timeSlotIndex,final BlockingData<PuppetType> jobType){
		enqueMutation(new Mutation(){
			public void apply(){
				if(numberOfResidents<MAX_RESIDENTS){
					numberOfResidents++;
					timeSlotIndex.unblock(numberOfResidents);
				}
				else{
					timeSlotIndex.unblock(null);
				}
				jobType.unblock(PuppetType.resident);
			}
		});
	}
	
	public void msgFillSpecifiedOpening(final BlockingData<Integer> timeSlotIndex,final PuppetType jobType) {
		enqueMutation(new Mutation(){
			public void apply(){
				if(jobType==PuppetType.resident){
					if(numberOfResidents<MAX_RESIDENTS){
						numberOfResidents++;
						timeSlotIndex.unblock(numberOfResidents);
						return;
					}
				}
				timeSlotIndex.unblock(null);
			}
		});
	}

	public void msgUpdateTime(long time){
		enqueMutation(new Mutation(){
			public void apply(){
				dirtyLevel++;
			}
		});
	}

	public void msgClearAllOpenings(){
		
	}

	protected void destructor(){
		
	}



}
