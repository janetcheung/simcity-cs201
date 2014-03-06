package UnitTestingCommon.interfaces;



public interface MarketSignInSysInterface {
	// messages
	public abstract void msgImAvailable(final MarketClerkInterface marketClerk);
	
	public abstract void msgWaitingInLine(final MarketCustomerInterface mCustomer);
	
	public abstract void msgAddClerk(final MarketClerkInterface marketClerk);
	
	public abstract void msgRemoveClerk(final MarketClerkInterface marketClerk);
	
	
	public abstract void msgCustomerEntered(final MarketCustomerInterface marketCustomer);
	
	public abstract void msgRemoveCustomer(final MarketCustomerInterface marketCustomer);
}
