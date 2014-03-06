package city.bus.test;

import agent.Agent;
import astar.MovementManager;
import city.Person;
import city.bank.Bank;
import city.bus.Bus;
import city.bus.BusStop;
import junit.framework.TestCase;

public class BusUnitTests extends TestCase{

	Bus bus;
	BusStop[] stops;
	MovementManager m;
	city.Person.Setup s;
	Person andrew;
	Person sean;
	Person jacob;
	Person janet;
	Person michael;

	public void setUp() throws Exception{

		super.setUp();
		s = new city.Person.Setup();
		m = new MovementManager("Move", 0, 0);

		stops=new BusStop[3];
		bus = new Bus("BusTest", 0, m, 0, 0, stops);
		andrew = new Person("Andrew", m, 0, 0, s);
		sean = new Person("Sean", m, 0, 0, s);
		jacob = new Person("Jacob", m, 0, 0, s);
		janet = new Person("Janet", m , 0, 0, s);
		michael = new Person ("Michael",m,0,0,s);

		Agent.setJUNIT(true);



	}


	public void testCorrectPassengers(){

		System.out.println("*****TEST FOR CHECKING BUS PASSENGERS*****");
		assertEquals("Bus should have 0 people in it. It doesn't.",bus.getAmountOfPassengers(), 0);   

		bus.msgAddPassenger(andrew);

		assertTrue("Bus should have 1 people in it. It doesn't.", bus.getAmountOfPassengers()==1);



		bus.msgAddPassenger(jacob);
		assertTrue("Bus should have 2 people in it. It doesn't.", bus.getAmountOfPassengers()==2);
		assertFalse("Bus should not have 4 people in it. It does", bus.getAmountOfPassengers()==4);

		bus.msgAddPassenger(sean);
		assertTrue("Bus should have 3 people in it. It doesn't.", bus.getAmountOfPassengers()==3);
		assertFalse("Bus should not have 2 people in it. It does", bus.getAmountOfPassengers()==2);

		
		bus.msgAddPassenger(janet);
		assertTrue("Bus should have 4 people in it. It doesn't.", bus.getAmountOfPassengers()==4);
		assertFalse("Bus should not have 3 people in it. It does", bus.getAmountOfPassengers()==3);
		
		bus.msgAddPassenger(michael);
		assertTrue("Bus should have 5 people in it. It doesn't.", bus.getAmountOfPassengers()==5);
		assertFalse("Bus should not have 4 people in it. It does", bus.getAmountOfPassengers()==4);
		
		
	}



}
