package UnitTestingCommon.interfaces;


public interface ApartmentInterface extends BuildingInterface {
	
	
	public void msgRenterLeft(final RenterInterface r);
	public void msgTenantPayingRent(final PersonInterface p, final double rent);
	
	
	
}