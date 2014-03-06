package city.apartment;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.PuppetInterface;
import agent.Agent.BlockingData;
import city.Person;
import city.Puppet;
import city.Puppet.PuppetType;
import city.market.Market.Food;


public class TestFrame extends JFrame implements ActionListener, KeyListener {
	
	
	
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {
		Puppet.Setup setup = new Puppet.Setup();
        setup.master = new Person("dummy",null,0,0,new Person.Setup());
        String name = "RenterName";
        setup.role = PuppetType.resident;
        setup.boughtNewGroceries = (Math.random() > 0.5);
        setup.foodInventory = new int[Food.getNumberOfFoods()];
        for (int i = 0; i < Food.getNumberOfFoods(); i++) {
        	setup.foodInventory[i] = 0;
        }
        setup.foodInventory[Food.mustard.index] = 3;
        setup.foodInventory[Food.asparagus.index] = 3;
        setup.foodInventory[Food.bottleOfRum.index] = 3;
        setup.foodInventory[Food.butter.index] = 3;
        setup.minutesMealDuration = 10;
        
        
      
        char key = e.getKeyChar();
		if (key == '1') {
	        setup.apartmentRoomNumber = 0;
	    }
		if (key == '2') {
			setup.apartmentRoomNumber = 1;
	    }
		if (key == '3') {
			setup.apartmentRoomNumber = 2;
	    }
		if (key == '4') {
			setup.apartmentRoomNumber = 3;
	    }
		if (key == '5') {
			setup.apartmentRoomNumber = 4;
	    }
		if (key == '6') {
			setup.apartmentRoomNumber = 5;
	    }
		if (key == '7') {
			setup.apartmentRoomNumber = 6;
	    }
		if (key == '8') {
			setup.apartmentRoomNumber = 7;
	    }
		if (key == 'z') {
			// Auto-assign
			BlockingData<Integer> roomNumber = new BlockingData<Integer>();
			apartment.msgFillAnyOpening(roomNumber, new BlockingData<Puppet.PuppetType>());
			if (roomNumber.get() == null) {
				AlertLog.getInstance().logMessage(AlertTag.TESTER, "ApartmentTestFrame", "Apartment has no more room!");
			}
			else {
				// Spawn renter
				setup.apartmentRoomNumber = roomNumber.get();
				BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
		        apartment.msgSpawnPuppet(puppetHolder, name, setup);
		        currentRenters[setup.apartmentRoomNumber] = (Renter)puppetHolder.get();
			}
	    }
		
		if (key == '1' || key == '2' || key == '3' || key == '4' || key == '5' || key == '6' || key == '7' || key == '8') {
			// Spawn new renter
			BlockingData<PuppetInterface> puppetHolder = new BlockingData<PuppetInterface>();
	        apartment.msgSpawnPuppet(puppetHolder, name, setup);
	        currentRenters[setup.apartmentRoomNumber] = (Renter)puppetHolder.get();
		}
		
		// Commands for telling renters to leave
		if (key == 'q') {
			if (currentRenters[0] != null) {
				currentRenters[0].msgLeaveBuilding();
			}
			currentRenters[0] = null;
		}
		if (key == 'w') {
			if (currentRenters[1] != null) {
				currentRenters[1].msgLeaveBuilding();
			}
			currentRenters[1] = null;
		}
		if (key == 'e') {
			if (currentRenters[2] != null) {
				currentRenters[2].msgLeaveBuilding();
			}
			currentRenters[2] = null;
		}
		if (key == 'r') {
			if (currentRenters[3] != null) {
				currentRenters[3].msgLeaveBuilding();
			}
			currentRenters[3] = null;
		}
		if (key == 't') {
			if (currentRenters[4] != null) {
				currentRenters[4].msgLeaveBuilding();
			}
			currentRenters[4] = null;
		}
		if (key == 'y') {
			if (currentRenters[5] != null) {
				currentRenters[5].msgLeaveBuilding();
			}
			currentRenters[5] = null;
		}
		if (key == 'u') {
			if (currentRenters[6] != null) {
				currentRenters[6].msgLeaveBuilding();
			}
			currentRenters[6] = null;
		}
		if (key == 'i') {
			if (currentRenters[7] != null) {
				currentRenters[7].msgLeaveBuilding();
			}
			currentRenters[7] = null;
		}
		
		
            
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		animationPanel.repaint();
	}
	
	
	
	
	
	
	
	static Apartment apartment;
	static JPanel animationPanel;
	static boolean paused  = false;
	static final Renter[] currentRenters = { null, null, null, null, null, null, null, null };
	
	
	public static void main(String[] args) {
		
		TestFrame tf = new TestFrame();
		tf.addKeyListener(tf);
		
		tf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tf.setBounds(500, 50, 605, 620);
		tf.setVisible(true);
		
		//String name,int pr,int pc,int cr,int cc,int tlr,int tlc,int brr,int brc
		apartment = new Apartment("TestApartment", 0, 0, 0, 0, 0, 0, 0, 0);
	    apartment.startAgent();
	    
	    animationPanel=new AnimationPanel();
		
		tf.add(animationPanel);
		
		(new Timer(20, tf)).start();
	}
	
	
	
	public static class AnimationPanel extends JPanel {
		public AnimationPanel() {
			this.setBounds(0, 0, 620, 620);
		}
		@Override
		public void paintComponent(Graphics g) {
			apartment.getGui().draw((Graphics2D)g);
		}
	}
	
	
	
}
