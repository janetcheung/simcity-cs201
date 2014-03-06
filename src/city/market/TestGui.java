package city.market;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.PuppetInterface;
import agent.Agent;
import agent.Agent.BlockingData;
import city.Building;
import city.Person;
import city.Puppet;
import city.Puppet.PuppetType;
import city.market.Market.Food;
import city.restaurant.TestFrame.MyWorker;

public class TestGui extends JFrame implements ActionListener, KeyListener {
	
	/*
	public static void main(String[] args) {
        TestGui gui = new TestGui();
        gui.frame.addKeyListener(gui);
    }
    */
	
	/*
	private final ZoomedFrame frame;
	private final Market market;
	*/
	
	//private MarketClerk clerk;
	
	
	public TestGui() {

	    market.msgReleaseFromForceClose();
		
		AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Shift 0 begins: Current time is " + Building.getStartHour(0));
		
		// Add auto-assigned worker to the restaurant
		BlockingData<Integer> timeSlotIndexHolder1 = new BlockingData<Integer>();
		BlockingData<Puppet.PuppetType> jobTypeHolder1 = new BlockingData<Puppet.PuppetType>();
		market.msgFillAnyOpening(timeSlotIndexHolder1, jobTypeHolder1);
		if (timeSlotIndexHolder1.get() == null) {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "All the clerk slots are full");
		}
		else {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Hired a " + jobTypeHolder1.get() + " for shift " + timeSlotIndexHolder1.get());
			clerks.add(new TestClerk(timeSlotIndexHolder1.get()));
		}
		
		BlockingData<Integer> timeSlotIndexHolder2 = new BlockingData<Integer>();
		BlockingData<Puppet.PuppetType> jobTypeHolder2 = new BlockingData<Puppet.PuppetType>();
		market.msgFillAnyOpening(timeSlotIndexHolder2, jobTypeHolder2);
		if (timeSlotIndexHolder2.get() == null) {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "All the clerk slots are full");
		}
		else {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Hired a " + jobTypeHolder2.get() + " for shift " + timeSlotIndexHolder2.get());
			clerks.add(new TestClerk(timeSlotIndexHolder2.get()));
		}
		
		BlockingData<Integer> timeSlotIndexHolder3 = new BlockingData<Integer>();
		BlockingData<Puppet.PuppetType> jobTypeHolder3 = new BlockingData<Puppet.PuppetType>();
		market.msgFillAnyOpening(timeSlotIndexHolder3, jobTypeHolder3);
		if (timeSlotIndexHolder3.get() == null) {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "All the clerk slots are full");
		}
		else {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Hired a " + jobTypeHolder3.get() + " for shift " + timeSlotIndexHolder3.get());
			clerks.add(new TestClerk(timeSlotIndexHolder3.get()));
		}
		
		BlockingData<Integer> timeSlotIndexHolder4 = new BlockingData<Integer>();
		BlockingData<Puppet.PuppetType> jobTypeHolder4 = new BlockingData<Puppet.PuppetType>();
		market.msgFillAnyOpening(timeSlotIndexHolder4, jobTypeHolder4);
		if (timeSlotIndexHolder4.get() == null) {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "All the clerk slots are full");
		}
		else {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Hired a " + jobTypeHolder4.get() + " for shift " + timeSlotIndexHolder4.get());
			clerks.add(new TestClerk(timeSlotIndexHolder4.get()));
		}
		
		market.msgUpdateTime(Building.getStartHour(0));
		/*
		for (MyWorker w : workers) {
			if (w.shift == 0) {
				setup.role = w.type;
				BlockingData<Puppet> puppetHolder = new BlockingData<Puppet>();
				r.msgSpawnPuppet(puppetHolder, "Worker", setup);
				puppetHolder.get();
			}
		}
		*/
		
		/*
		frame = new ZoomedFrame();
		frame.setVisible(true);
		
		//market = new Market("market",0,0,0,0,0,0,0,0);
		market.startAgent();
		frame.setBuilding(market);
		*/
		
		Puppet.Setup setup = new Puppet.Setup();
		setup.master = new Person("dummy",null,0,0,new Person.Setup());
        /*
        setup.money = 20.0;
        setup.shoppingList = new HashMap<String, Integer>();
    	{
    		setup.shoppingList.put("Steak", 2);
    		setup.shoppingList.put("Chicken", 2);
    		setup.shoppingList.put("Salad", 2);
    		setup.shoppingList.put("Pizza", 2);
    	}
    	*/
		
        setup.role = PuppetType.markClerk;
        BlockingData<PuppetInterface> blocker1 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker1, "clerk1", setup);
        blocker1.get();
        
        setup.role = PuppetType.markClerk;
        BlockingData<PuppetInterface> blocker2 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker2, "clerk2", setup);
        blocker2.get();
        
        setup.role = PuppetType.markClerk;
        BlockingData<PuppetInterface> blocker3 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker3, "clerk3", setup);
        blocker3.get();
        
        setup.role = PuppetType.markClerk;
        BlockingData<PuppetInterface> blocker4 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker4, "clerk4", setup);
        blocker4.get();
        
        //clerk = (MarketClerk)blocker4.get();
		
        
        Puppet.Setup setup1 = new Puppet.Setup();
        setup1.master = new Person("dummy",null,0,0,new Person.Setup());
        setup1.money = 20.0;
        setup1.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
    	{
    		setup1.foodsToBuyQuantities[5] = 2;
    	}
        setup1.role = PuppetType.customer;
        BlockingData<PuppetInterface> blocker5 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker5, "cust1", setup1);
        blocker5.get();
        
        Puppet.Setup setup2 = new Puppet.Setup();
        setup2.master = new Person("dummy",null,0,0,new Person.Setup());
        setup2.money = 20.0;
        setup2.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
    	{
    		setup2.foodsToBuyQuantities[5] = 2;
    	}
        setup2.role = PuppetType.customer;
        BlockingData<PuppetInterface> blocker6 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker6, "cust2", setup2);
        blocker6.get();
        
        Puppet.Setup setup3 = new Puppet.Setup();
        setup3.master = new Person("dummy",null,0,0,new Person.Setup());
        setup3.money = 20.0;
        setup3.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
    	{
    		setup3.foodsToBuyQuantities[5] = 2;
    	}
        setup3.role = PuppetType.customer;
        BlockingData<PuppetInterface> blocker7 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker7, "cust3", setup3);
        blocker7.get();
        
        Puppet.Setup setup4 = new Puppet.Setup();
        setup4.master = new Person("dummy",null,0,0,new Person.Setup());
        setup4.money = 20.0;
        setup4.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
    	{
    		setup4.foodsToBuyQuantities[5] = 2;
    	}
        setup4.role = PuppetType.customer;
        BlockingData<PuppetInterface> blocker8 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker8, "cust4", setup4);
        blocker8.get();
        
        Puppet.Setup setup5 = new Puppet.Setup();
        setup5.master = new Person("dummy",null,0,0,new Person.Setup());
        setup5.money = 20.0;
        setup5.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
    	{
    		setup5.foodsToBuyQuantities[5] = 2;
    	}
        setup5.role = PuppetType.customer;
        BlockingData<PuppetInterface> blocker9 = new BlockingData<PuppetInterface>();
        market.msgSpawnPuppet(blocker9, "cust5", setup5);
        blocker9.get();
        
        //clerk.msgLeaveBuilding();
	}
	


	@Override
	public void keyTyped(KeyEvent e) {
		Puppet.Setup setup = new Puppet.Setup();
        setup.master = new Person("dummy",null,0,0,new Person.Setup());
        setup.money = random.nextInt(100);
        String customerName = "markCust";
        String clerkName = "markClerk";
        
		
		// TODO Auto-generated method stub
		char key = e.getKeyChar();
		/*
		if (key == 'q') {
			// Check whether host socket has a worker
			System.out.println("Telling clerk4 to leave");
			clerk.msgLeaveBuilding();
		}
		*/
		
		if (key == 'f') {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Forcing market to close");
			market.msgForceClose();
		}
		if (key == 'o') {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Allowing market to open");
			market.msgReleaseFromForceClose();
		}
		
		if (key == 'h') {
			BlockingData<Integer> timeSlotIndexHolder = new BlockingData<Integer>();
			BlockingData<Puppet.PuppetType> jobTypeHolder = new BlockingData<Puppet.PuppetType>();
			market.msgFillAnyOpening(timeSlotIndexHolder, jobTypeHolder);
			if (timeSlotIndexHolder.get() == null) {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "All the clerk slots are full");
			}
			else {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Hired a " + jobTypeHolder.get() + " for shift " + timeSlotIndexHolder.get());
				clerks.add(new TestClerk(timeSlotIndexHolder.get()));
			}
		}
		
		if (key == 's') {
			// Go to next shift stage
			switch (shiftStage) {
				case 0:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Shift 0 begins: Current time is " + Building.getStartHour(0));
					market.msgUpdateTime(Building.getStartHour(0));
					for (TestClerk clerk : clerks) {
						if (clerk.shift == 0) {
							setup.role = PuppetType.markClerk;
							BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
							market.msgSpawnPuppet(puppetHolder, clerkName, setup);
							puppetHolder.get();
						}
					}
					shiftStage++;
					break;
				case 1:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Shift 0 ends: Current time is " + Building.getEndHour(0));
					market.msgUpdateTime(Building.getEndHour(0));
					shiftStage++;
					break;
				case 2:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Shift 1 starts: Current time is " + Building.getStartHour(1));
					// Don't send the following message currently, because end hour for 0 is same as start hour for 1
					//r.msgUpdateTime(Building.getStartHour(1));
					for (TestClerk clerk : clerks) {
						if (clerk.shift == 1) {
							setup.role = PuppetType.markClerk;
							BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
							market.msgSpawnPuppet(puppetHolder, clerkName, setup);
							puppetHolder.get();
						}
					}
					shiftStage++;
					break;
				case 3:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Shift 1 ends: Current time is " + Building.getEndHour(1));
					market.msgUpdateTime(Building.getEndHour(1));
					shiftStage = 0;
					break;
			}
		}
		
		
		if (key == 'p') {
//			if (paused) {
//				AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Unpausing!");
//				market.msgRecursiveResume();
//			}
//			else {
//				AlertLog.getInstance().logMessage(AlertTag.TESTER, "MarketTestGui", "Pausing!");
//				market.msgRecursivePause();
//			}
			paused = !paused;
		}
		
		if (key == 'c') {
			setup.foodsToBuyQuantities = new int[Food.getNumberOfFoods()];
	    	{
	    		setup.foodsToBuyQuantities[random.nextInt(Market.Food.getNumberOfFoods())] = random.nextInt(10) + 1;
	    	}
			
			// Add customer to the market
			setup.role = PuppetType.customer;
	        BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
	        market.msgSpawnPuppet(puppetHolder, customerName, setup);
			puppetHolder.get();
		}
	}


	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		animationPanel.repaint();
	}
	
	
	static Market market;
	static ArrayList<TestClerk> clerks;
	static int shiftStage;
	//static KeyListener booby;
	static JPanel animationPanel;
	static boolean paused;
	Random random = new Random();
	
	public static class TestClerk {
		public PuppetType type = PuppetType.markClerk;
		public int shift;
		public TestClerk(int shift) {
			this.shift = shift;
		}
	}
	
	public static void main(String[] args) {
		paused = false;
		
		clerks = new ArrayList<TestClerk>();
		shiftStage = 1;
		
		market=new Market("Restaurant",0,0,0,0,0,0,0,0);
		//gui.setBuilding(r);
	    market.startAgent();
		
		TestGui gui = new TestGui();
		gui.addKeyListener(gui);
		
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setBounds(500, 50, 620, 620);
		gui.getContentPane().setLayout(new GridLayout(1,1));
		gui.setVisible(true);
	    
	    animationPanel=new AnimationPanel();
		
		gui.add(animationPanel);
		
		(new Timer(20,gui)).start();
	}
	
	public static class AnimationPanel extends JPanel {
		public AnimationPanel() {
			this.setBounds(0, 0, 620, 620);
		}
		@Override
		public void paintComponent(Graphics g) {
			market.getGui().draw((Graphics2D)g);
		}
	}
	
	
}
