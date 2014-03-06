package city.apartment;

import java.util.ArrayList;
import java.util.HashMap;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.ApartmentInterface;
import UnitTestingCommon.interfaces.PersonInterface;
import UnitTestingCommon.interfaces.PuppetInterface;
import UnitTestingCommon.interfaces.RenterInterface;
import astar.GridCell;
import astar.MovementManager;
import city.Building;
import city.Person;
import city.Puppet;
import city.Puppet.PuppetType;
import city.Puppet.Setup;
import city.apartment.gui.ApartmentGui;

public class Apartment extends Building implements ApartmentInterface {

	
	
	// PARAMETERS
	
	public static final GridCell locEntrance = new GridCell(14, 0);
	public static final GridCell locExit = new GridCell(15, 0);
	public static final GridCell[] locRoomEntrances = {
		new GridCell(13, 5),
		new GridCell(13, 12),
		new GridCell(13, 19),
		new GridCell(13, 26),
		new GridCell(16, 26),
		new GridCell(16, 19),
		new GridCell(16, 12),
		new GridCell(16, 5)		
	};
	
	// These offsets are with respect to the room entrance loc for rooms that are above the apartment entrance
	// For rooms that are below the apartment entrance, the *r-values* of these offsets must be INVERTED
	public static final GridCell offsetFridgeAccess = new GridCell(-2, -1);
	public static final GridCell offsetStoveAccess = new GridCell(-4, -1);
	public static final GridCell offsetTableAccess = new GridCell(-4, 1);
	public static final GridCell offsetToilet = new GridCell(-12, -2);
	public static final GridCell offsetBed = new GridCell(-11, 1);
	
	//public static final GridCell offsetStoveFromAccess = new GridCell(0, -1);
	//public static final GridCell offsetTableFromAccess = new GridCell(0, 1);
	
	public static final int maxRoomOccupants = 2;
	
	
	
	
	
	
	// ENUMS AND CLASSES
	
	private class Room {
		public final GridCell locEntrance;
		public HashMap<String,Integer> fridgeInventory;
		public int occupants;
		public Room(GridCell locEntrance) {
			this.locEntrance = locEntrance;
			fridgeInventory = new HashMap<String,Integer>();
			occupants = 0;
		}
	}
	
	
	
	
	
	
	
	// DATA
	
	private final Room[] rooms;
	private final ArrayList<RenterInterface> currentRenters;
	private PersonInterface landlord;	// Just assigned to the first person to live in apartment
	
	
	
	
	
	
	
	
	
	
	
	
	// CONSTRUCTOR //////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Apartment(String name,int pr,int pc,int cr,int cc,int tlr,int tlc,int brr,int brc) {
		super(name,pr,pc,cr,cc,new MovementManager(name+" MovementManager",insideNumRows,insideNumCols,"apartmentLayout.txt"),tlr,tlc,brr,brc,new ApartmentGui());
		rooms = new Room[locRoomEntrances.length];
		for (int a = 0; a < locRoomEntrances.length; a++) {
			rooms[a] = new Room(locRoomEntrances[a]);
		}
		currentRenters = new ArrayList<RenterInterface>();
		landlord = null;
	}

	
	
	@Override
	protected void destructor() {
		// do nothing
	}
	
	
	
	
	
	
	
	
	
	
	
	
	// MESSAGES

	@Override
	public void msgFillAnyOpening(final BlockingData<Integer> roomNumber, final BlockingData<PuppetType> jobType) {
		// A room number of null indicates there is no room for him
		enqueMutation(new Mutation() {
			public void apply() {
				int indexRoomWithFewestOccupants = -1;
				int fewestOccupants = maxRoomOccupants;
				for (int index = 0; index < rooms.length; index++) {
					if (rooms[index].occupants < fewestOccupants) {
						indexRoomWithFewestOccupants = index;
						fewestOccupants = rooms[index].occupants;
					}
				}
				if (fewestOccupants < maxRoomOccupants) {
					rooms[indexRoomWithFewestOccupants].occupants++;
					roomNumber.unblock(indexRoomWithFewestOccupants);
					jobType.unblock(PuppetType.resident);
				}
				else {
					roomNumber.unblock(null);
					jobType.unblock(null);
				}
			}
		});
	}
	@Override
	public void msgFillSpecifiedOpening(final BlockingData<Integer> roomNumber, final PuppetType jobType) {
		// A room number of null indicates there is no room for him
		if (jobType == PuppetType.resident) {
			Apartment.this.msgFillAnyOpening(roomNumber, new BlockingData<PuppetType>());
		}
		else {
			AlertLog.getInstance().logMessage(AlertTag.APT, getAgentName(), "Error: tyring to place non-resident at apartment!");
		}
	}
	@Override
	public void msgClearAllOpenings() {
		enqueMutation(new Mutation(){
			public void apply(){
				for (Room r : rooms) {
					r.occupants = 0;
					r.fridgeInventory.clear();
					landlord = null;
				}
			}
		});
	}
	
	
	@Override
	public void msgSpawnPuppet(final BlockingData<PuppetInterface> result, final String name, final Setup s) {
		enqueMutation(new Mutation(){
			public void apply(){
				/* 6 tasks:
				 * 		1. Instantiate
				 * 		2. Add to movement manager
				 * 		3. Add Puppet gui to gui panel
				 * 		4. Add Puppet to currentRenters list
				 * 		5. Return Puppet pointer to its Person master
				 * 		6. Start Puppet thread
				 */
				if (s.role == PuppetType.resident) {
					Renter renter = new Renter(name, Apartment.this.getMovementManager(), Apartment.this, s);
					Apartment.this.getMovementManager().msgAddUnit(renter, 1, 1, locEntrance.r, locEntrance.c);		
					gui.addGui(renter.getGui());
					currentRenters.add(renter);
					result.unblock(renter);
					renter.startAgent();
				}
				else {
					AlertLog.getInstance().logError(AlertTag.APT, getAgentName(), "Trying to spawn non-resident Puppet at apartment!");
				}
			}
		});
	}

	@Override
	public void msgUpdateTime(final long time) {}
	
	public void msgRenterLeft(final RenterInterface r) {
		enqueMutation(new Mutation() {
			public void apply() {
				gui.removeGui(r.getGui());
				currentRenters.remove(r);
			}
		});
	}
	
	public void msgTenantPayingRent(final PersonInterface p, final double rent) {
		enqueMutation(new Mutation() {
			public void apply() {
				if (landlord == null) {
					landlord = p;
				}
				if (p != landlord) {
					AlertLog.getInstance().logInfo(AlertTag.APT, getAgentName(), "Tenant " + p.getAgentName() + " pays $" + rent + " rent to landlord " + landlord.getAgentName() + ".");
					p.msgAddMoney(rent);
				}
			}
		});
	}

	
	
	
	
	
	
	
	// SCHEDULER
	
	protected boolean pickAndExecuteAction() {
		return false;
	}












	

	
}
