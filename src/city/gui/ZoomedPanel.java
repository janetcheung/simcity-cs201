package city.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import agent.Agent.BlockingData;
import city.Building;
import city.bank.Bank;
import city.house.House;
import city.market.Market;
import city.market.Market.Food;
import city.restaurant.Restaurant;

public class ZoomedPanel extends JPanel implements ActionListener{
	public static final int FRAME_DELAY=AnimationPanel.FRAME_DELAY;

	private static final int zoomedWINDOWX=605;
	private static final int zoomedWINDOWY=840;

	private final JPanel animationPanel;
	private final JPanel guiPanel;
	private final JPanel blankPanel;
	private final RestaurantPanel restaurantPanel;
	private final BankPanel bankPanel;
	private final MarketPanel marketPanel;

	private Building currentBuilding;

	public ZoomedPanel(){	
		setBounds(100,100,zoomedWINDOWX,zoomedWINDOWY);
		setVisible(true);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		currentBuilding=null;

		animationPanel=new JPanel(){
			public void paintComponent(Graphics g){
				synchronized(ZoomedPanel.this){
					if(currentBuilding!=null){
						currentBuilding.getGui().draw((Graphics2D)g);
					}
					else{
						g.setColor(Color.WHITE);
						g.drawRect(0, 0, zoomedWINDOWX, zoomedWINDOWY);
					}
				}
			}
		};


		animationPanel.setMinimumSize(new Dimension(zoomedWINDOWX,615));
		animationPanel.setMaximumSize(new Dimension(zoomedWINDOWX,615));
		animationPanel.setPreferredSize(new Dimension(zoomedWINDOWX,615));
		c.gridx = 0;
		c.gridy = 0;
		c.fill=GridBagConstraints.HORIZONTAL;
		add(animationPanel,c);

		guiPanel=new JPanel();
		guiPanel.setMinimumSize(new Dimension(zoomedWINDOWX, zoomedWINDOWY-615));
		guiPanel.setMaximumSize(new Dimension(zoomedWINDOWX, zoomedWINDOWY-615));
		guiPanel.setPreferredSize(new Dimension(zoomedWINDOWX, zoomedWINDOWY-615));
		guiPanel.setLayout(new CardLayout());
		c.gridx=0;
		c.gridy=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		add(guiPanel,c);

		new Timer(FRAME_DELAY,this).start();

		//Building Panels
		restaurantPanel=new RestaurantPanel();
		guiPanel.add(restaurantPanel, "RestaurantPanel");

		bankPanel=new BankPanel();
		guiPanel.add(bankPanel, "BankPanel");

		marketPanel=new MarketPanel();
		guiPanel.add(marketPanel, "MarketPanel");

		blankPanel=new JPanel();
		guiPanel.add(blankPanel, "BlankPanel");

		CardLayout g=(CardLayout)guiPanel.getLayout();
		g.show(guiPanel, "BlankPanel");
	}

	public void actionPerformed(ActionEvent e){
		animationPanel.repaint();
	}

	public synchronized void setBuilding(Building g){
		CardLayout c=(CardLayout)(guiPanel.getLayout());
		currentBuilding=g;

		if (currentBuilding.getClass()==Restaurant.class){
			((Restaurant)currentBuilding).msgZoomedPanelDisplay();
			c.show(guiPanel, "RestaurantPanel");
		}
		else{
			if (currentBuilding==null){
				c.show(guiPanel, "BankPanel");
			}
			if (currentBuilding.getClass()==Market.class){
				c.show(guiPanel, "MarketPanel");
			}
			if (currentBuilding.getClass()==Bank.class){
				c.show(guiPanel, "BankPanel");
			}
			if ((currentBuilding.getClass()==House.class)){
				//			(currentBuilding.getClass()==Apartment.class)
				c.show(guiPanel, "BlankPanel");
			}
		}
	}

	private class MarketPanel extends JPanel implements ActionListener{
		private JCheckBox shutDown;

		MarketPanel(){
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			shutDown=new JCheckBox("Shut Down Market for Renovations");
			shutDown.addActionListener(this);
			shutDown.setAlignmentX(CENTER_ALIGNMENT);
			shutDown.setEnabled(true);
			add(shutDown);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			BlockingData<Boolean> closed=new BlockingData<Boolean>();			
			currentBuilding.msgAskIfBuildingIsClosed(closed);

			if(e.getSource()==shutDown){
				if (shutDown.isSelected()){
					currentBuilding.msgForceClose();
				}
				else{
					currentBuilding.msgReleaseFromForceClose();
				}		
			}
		}
	}

	private class RestaurantPanel extends JPanel implements ActionListener{
		private JCheckBox shutDown;
		private JComboBox foodChoices;
		private JPanel inventoryPanel;
		private JPanel guiPanel;
		private JButton incrementFood;
		private List<JLabel> foods;

		RestaurantPanel(){
			setLayout(new GridBagLayout());
			//			setLayout(new GridLayout(1,2));
			GridBagConstraints c=new GridBagConstraints();

			inventoryPanel=new JPanel();
			inventoryPanel.setLayout(new GridLayout(8,2));
			c.gridx=0;
			c.gridy=0;
			c.fill=GridBagConstraints.HORIZONTAL;
			add(inventoryPanel,c);

			guiPanel=new JPanel();
			guiPanel.setPreferredSize(new Dimension(zoomedWINDOWX/5,zoomedWINDOWY));
			//			guiPanel.setBorder(BorderFactory.createDashedBorder(Color.BLUE));
			guiPanel.setLayout(new GridBagLayout());
			c.gridx=1;
			c.gridy=0;
			c.fill=GridBagConstraints.VERTICAL;
			add(guiPanel,c);

			shutDown=new JCheckBox("Shut Down Restaurant");
			shutDown.addActionListener(this);
			shutDown.setAlignmentX(CENTER_ALIGNMENT);
			shutDown.setEnabled(true);
			c.gridx=0;
			c.gridy=0;
			c.fill=GridBagConstraints.HORIZONTAL;
			guiPanel.add(shutDown,c);

			foodChoices=new JComboBox();
			foodChoices.addActionListener(this);
			if (foodChoices !=null){
				for (int i=0; i<Market.Food.getNumberOfFoods();i++){
					foodChoices.addItem(Market.Food.getFood(i));				
				}
			}
			c.gridx=0;
			c.gridy=1;
			c.fill=GridBagConstraints.HORIZONTAL;
			guiPanel.add(foodChoices,c);

			incrementFood=new JButton("Add 1 food to inventory");
			incrementFood.addActionListener(this);
			incrementFood.setAlignmentX(CENTER_ALIGNMENT);
			c.gridx=0;
			c.gridy=2;
			c.fill=GridBagConstraints.HORIZONTAL;
			guiPanel.add(incrementFood,c);

			foods=new ArrayList<JLabel>();
			addFoodPanel();

		}

		public void msgUpdateFood(int[] quantities){
			GridBagConstraints c= new GridBagConstraints();

			for (int i=0; i<foodChoices.getItemCount(); i++){
				foods.get(i).setText("<html><font size=-2>"+ foodChoices.getItemAt(i) +": "+quantities[i]+"</font></html>");
				//				c.gridx=i%5;
				//				c.gridy=i%2;
				//				c.fill=GridBagConstraints.HORIZONTAL;
				//				inventoryPanel.add(foods,c);
			}
		}

		public void addFoodPanel(){
			GridBagConstraints c= new GridBagConstraints();
			for (int i=0; i<foodChoices.getItemCount(); i++){
				foods.add(new JLabel("<html><font size=-2>"+ foodChoices.getItemAt(i)+": 10<br></font></html>"));
				c.gridx=i%5;
				c.gridy=i%2;
				c.fill=GridBagConstraints.HORIZONTAL;
				inventoryPanel.add(foods.get(i),c);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			BlockingData<Boolean> closed=new BlockingData<Boolean>();	

			if (currentBuilding!=null){
				currentBuilding.msgAskIfBuildingIsClosed(closed);
			}
			//Actual Gui Controls
			if(e.getSource()==shutDown){
				if (shutDown.isSelected()){
					currentBuilding.msgForceClose();
				}
				else{
					currentBuilding.msgReleaseFromForceClose();
				}
			}
			if (e.getSource()==incrementFood){
				int foodIndex=foodChoices.getSelectedIndex();
//				System.err.println("foodChoices: " + foodChoices.getSelectedIndex());
				((Restaurant)currentBuilding).msgIncrementFoodInventory(foodIndex);
			}
		}
	}
	private class BankPanel extends JPanel implements ActionListener{
		private JCheckBox shutDown;

		BankPanel(){
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			shutDown=new JCheckBox("Shut Down Bank for Renovations");
			shutDown.addActionListener(this);
			shutDown.setAlignmentX(CENTER_ALIGNMENT);
			shutDown.setEnabled(true);
			add(shutDown);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			BlockingData<Boolean> closed=new BlockingData<Boolean>();			
			currentBuilding.msgAskIfBuildingIsClosed(closed);

			if(e.getSource()==shutDown){
				if (shutDown.isSelected()){
					currentBuilding.msgForceClose();
				}
				else{
					currentBuilding.msgReleaseFromForceClose();
				}		
			}
		}
	}
	public void msgUpdateFood(int[] newInventory) {
		if (currentBuilding!=null){
			if (currentBuilding.getClass()==Restaurant.class){
				restaurantPanel.msgUpdateFood(newInventory);
			}
			revalidate();
		}
	}
}
