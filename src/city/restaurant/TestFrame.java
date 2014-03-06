package city.restaurant;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

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
import city.bank.Bank;
import city.market.Market;


public class TestFrame extends JFrame implements ActionListener, KeyListener {
	
	
	
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {
		Puppet.Setup setup = new Puppet.Setup();
        setup.master = new Person("dummy",null,0,0,new Person.Setup());
        setup.money = 20.0;
        setup.minutesMealDuration = 20;
        String customerName = "Impatient Flake";
        // Following actually get set depending on what's in the puppetName
        //setup.foodPreference = null;
        //setup.foodPreferenceIsStrong = true;
        //setup.isFlake = true;
        //setup.minutesWaitingPatience = 50;
      
        char key = e.getKeyChar();
		if (key == 'q') {
			// Add auto-assigned worker to the restaurant
			BlockingData<Integer> timeSlotIndexHolder = new BlockingData<Integer>();
			BlockingData<Puppet.PuppetType> jobTypeHolder = new BlockingData<Puppet.PuppetType>();
			r.msgFillAnyOpening(timeSlotIndexHolder, jobTypeHolder);
			if (timeSlotIndexHolder.get() == null) {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "All the worker slots are full");
			}
			else {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Hired a worker of type " + jobTypeHolder.get() + " for shift " + timeSlotIndexHolder.get());
				workers.add(new MyWorker(jobTypeHolder.get(), timeSlotIndexHolder.get()));
			}
	    }
		if (key == '1') {
			// Hire host
			hireSpecifiedWorker(setup, PuppetType.restHost);
	    }
		if (key == '2') {
			// Hire cook
			hireSpecifiedWorker(setup, PuppetType.restCook);
	    }
		if (key == '3') {
			// Hire cashier
			hireSpecifiedWorker(setup, PuppetType.restCashier);
	    }
		if (key == '4') {
			// Hire regular waiter
			hireSpecifiedWorker(setup, PuppetType.restRegWaiter);
	    }
		if (key == '5') {
			// Hire revolving stand waiter
			hireSpecifiedWorker(setup, PuppetType.restStandWaiter);
	    }
		if (key == '6') {
			// Hire any waiter
			hireSpecifiedWorker(setup, PuppetType.restAnyWaiter);
	    }
		if (key == 'w') {
			// Add customer to the restaurant
			setup.role = PuppetType.customer;
	        BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
	        r.msgSpawnPuppet(puppetHolder, customerName, setup);
			puppetHolder.get();
		}		
		if (key == 'p') {
			// Go to next shift stage
			switch (shiftStage) {
				case 0:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Shift 0 begins: Current time is " + Building.getStartHour(0));
					r.msgUpdateTime(Building.getStartHour(0));
					for (MyWorker w : workers) {
						if (w.shift == 0) {
							setup.role = w.type;
							BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
							r.msgSpawnPuppet(puppetHolder, "Worker", setup);
							puppetHolder.get();
						}
					}
					shiftStage++;
					break;
				case 1:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Shift 0 ends: Current time is " + Building.getEndHour(0));
					r.msgUpdateTime(Building.getEndHour(0));
					shiftStage++;
					break;
				case 2:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Shift 1 starts: Current time is " + Building.getStartHour(1));
					// Don't send the following message currently, because end hour for 0 is same as start hour for 1
					//r.msgUpdateTime(Building.getStartHour(1));
					for (MyWorker w : workers) {
						if (w.shift == 1) {
							setup.role = w.type;
							BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
							r.msgSpawnPuppet(puppetHolder, "Worker", setup);
							puppetHolder.get();
						}
					}
					shiftStage++;
					break;
				case 3:
					AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Shift 1 ends: Current time is " + Building.getEndHour(1));
					r.msgUpdateTime(Building.getEndHour(1));
					shiftStage = 0;
					break;
			}
		}
		if (key == 'k') {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Forcing restaurant to close");
			r.msgForceClose();
		}
		if (key == 'l') {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "Allowing restaurant to open", "Allowing restaurant to open");
			r.msgReleaseFromForceClose();
		}
		if (key == 'm') {
			if (!paused) {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Unpausing!");
				Agent.pauseAllAgents();
				r.getGui().pause();
			}
			else {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Pausing!");
				Agent.resumeAllAgents();
				r.getGui().resume();
			}
			paused = !paused;
		}
            
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		animationPanel.repaint();
	}
	
	
	
	
	
	
	
	public static void hireSpecifiedWorker(Puppet.Setup s, PuppetType type) {
		BlockingData<Integer> timeSlotIndexHolder = new BlockingData<Integer>();
		r.msgFillSpecifiedOpening(timeSlotIndexHolder, type);
		if (timeSlotIndexHolder.get() == null) {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "All the " + type + " slots are full");
		}
		else {
			AlertLog.getInstance().logMessage(AlertTag.TESTER, "RestaurantTestFrame", "Hired a " + type + " for shift " + timeSlotIndexHolder.get());
			workers.add(new MyWorker(type, timeSlotIndexHolder.get()));
		}
	}
	
	
	
	
	static Restaurant r;
	static ArrayList<MyWorker> workers;
	static int shiftStage;
	static JPanel animationPanel;
	static boolean paused;
	
	public static class MyWorker {
		public PuppetType type;
		public int shift;
		public MyWorker(PuppetType type, int shift) {
			this.type = type;
			this.shift = shift;
		}
	}
	
	public static void main(String[] args) {
		
		paused = false;
		
		workers = new ArrayList<MyWorker>();
		shiftStage = 0;
		
		TestFrame tf = new TestFrame();
		tf.addKeyListener(tf);
		
		tf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tf.setBounds(500, 50, 620, 620);
		tf.getContentPane().setLayout(new GridLayout(1,1));
		tf.setVisible(true);
		
		//String name,int pr,int pc,int cr,int cc,double startingCash,Bank cityBank,Market[] cityMarkets,int tlr,int tlc,int brr,int brc
		r=new Restaurant("Restaurant",0,0,0,0,1000.0,null,null,null,0,0,0,0);
	    r.startAgent();
	    
	    animationPanel=new AnimationPanel();
		
		tf.add(animationPanel);
		
		(new Timer(20,tf)).start();
	}
	
	
	
	public static class AnimationPanel extends JPanel {
		public AnimationPanel() {
			this.setBounds(0, 0, 620, 620);
		}
		@Override
		public void paintComponent(Graphics g) {
			r.getGui().draw((Graphics2D)g);
		}
	}
	
	
	
}
