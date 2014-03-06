package city.gui;

import java.awt.*;
import java.awt.event.*;

import javax.sound.midi.*;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import trace.AlertLevel;
import trace.AlertLog;
import trace.AlertTag;
import trace.TraceFrame;
import agent.Agent;
import agent.Agent.BlockingData;
import astar.MovementManager;
import city.Broadcaster.BroadcastReceiver;
import city.Building;
import city.Person;
import city.Person.Setup;
import city.Puppet.PuppetType;
import city.Broadcaster;
import city.apartment.Apartment;
import city.bank.Bank;
import city.bus.Bus;
import city.bus.BusStop;
import city.house.House;
import city.market.Market;
import city.restaurant.Restaurant;

public class CityGui extends JFrame implements ActionListener,ChangeListener,MouseListener,BroadcastReceiver{
	//        private static final int WINDOWX=1125;
	//        private static final int WINDOWY=840;
	public static final int CITY_GRID_SQUARE=8;
	private static final int WINDOWX=1633;
	private static final int WINDOWY=840;

	public static final int PERSON_SPAWN_R=68;
	public static final int PERSON_SPAWN_C=21;
	public static final int CAR_SPAWN_R=1;
	public static final int CAR_SPAWN_C=97;

	//Slider frames per second
	private static final int TIME_MIN=1;
	private static final int TIME_MAX=5;
	private static final int TIME_INIT=1;

	//Agent related
	private final MovementManager movementManager;
	private final List<MyPerson> persons;
	private final Bus[] buses;

	private final Bank bank;
	private final House[] houses;
	private final Market[] markets;
	private final Apartment[] apartments;
	private final Restaurant[] restaurants;
	private final BusStop[] busStops;
	private final List<House> houseList;
	private final List<Apartment> apartmentList;
	private final List<Restaurant> restaurantList;
	private final List<Market> marketList;

	private final Broadcaster time;

	// Display related
	private final AnimationPanel animationPanel;
	private final ZoomedPanel zoomedPanel;
	private final TraceFrame traceFrame;
	private final CityPanel cityPanel;
	private final JPanel scenarioPanel;
	private final JPanel infoPanel;
	private final JPanel timePanel;

	private final JLabel infoLabel;
	private final JLabel timeLabel;

	private final JButton[] scenarios;
	private final JButton musicToggle;
	private final JCheckBox allowCollisionBox;
	private Sequencer sequencer;

	private boolean[] aptOccupied;
	private boolean playing;
	private long currentTime;
	private String currentSong;

	private class MyPerson {
		private final Person person;
		private final String job;
		private final String workplace;
		private final JButton button;

		public MyPerson(Person p, String j, String w, JButton b){
			person = p;
			job = j;
			workplace = w;
			button = b;

		}
	}
	public static void main(String[] args) {
		CityGui gui = new CityGui();
		gui.setTitle("District 39");
		gui.setVisible(true);
		gui.setResizable(false);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public CityGui(){
		animationPanel=new AnimationPanel();
		currentTime=0;
		zoomedPanel=new ZoomedPanel();
		traceFrame=new TraceFrame();
		scenarioPanel=new JPanel();
		cityPanel=new CityPanel();
		infoPanel=new JPanel();
		timePanel=new JPanel();

		infoLabel=new JLabel();
		timeLabel=new JLabel();

		musicToggle=new JButton("Turn Music off");
		musicToggle.addActionListener(this);

		try{
			sequencer=MidiSystem.getSequencer();
		}catch(MidiUnavailableException e){
			e.printStackTrace();
		}

		currentSong="nightInput";
		scenarios=new JButton[]{
				new JButton("Populate city"),
				};

		allowCollisionBox=new JCheckBox("Allow Collisions");
		allowCollisionBox.setAlignmentX(CENTER_ALIGNMENT);
		allowCollisionBox.addActionListener(this);

		for (JButton j:scenarios){
			j.addActionListener(this);
		}
		initializeDisplay();

		time=new Broadcaster("World Time");
		time.startAgent();
		time.msgAddHighPriorityReceiver(this);

		persons=new ArrayList<MyPerson>();

		marketList=new ArrayList<Market>();
		apartmentList=new ArrayList<Apartment>();
		houseList=new ArrayList<House>();
		restaurantList=new ArrayList<Restaurant>();

		bank=new Bank("Bank",51,31,46,26,44,24,55,35);
		time.msgAddHighPriorityReceiver(bank);
		bank.startAgent();

		zoomedPanel.setBuilding(bank);

		markets=new Market[]{
				new Market("Market 0",11,53,9,46,7,44,15,58),
				new Market("Market 1",88,53,87,49,84,44,92,58),
				new Market("Market 2",28,23,32,32,27,21,35,35)};

		for(Market m:markets){
			time.msgAddHighPriorityReceiver(m);
			marketList.add(m);
			m.startAgent();                
		}

		restaurants=new Restaurant[]{
				new Restaurant("Restaurant 0",69,13,72,9,0.0,bank,markets,zoomedPanel, 68,7,75,14),
				new Restaurant("Restaurant 1",88,73,88,68,0.0,bank,markets,zoomedPanel, 85,67,92,74),
				new Restaurant("Restaurant 2",69,29,72,32,0.0,bank,markets,zoomedPanel, 68,28,75,35),
				new Restaurant("Restaurant 3",88,13,89,9,0.0,bank,markets,zoomedPanel, 85,7,92,14),
				new Restaurant("Restaurant 4",88,29,89,32,0.0,bank,markets,zoomedPanel, 85,28,92,35)};

		for(Restaurant r:restaurants){
			time.msgAddHighPriorityReceiver(r);
			restaurantList.add(r);
			r.startAgent();
			bank.msgCreateAccountRestaurant(r,5000);
		}

		houses=new House[]{
				new House("House 0", 13,69,14,65,12,64,15,70),
				new House("House 1", 13,79,14,75,12,74,15,80),
				new House("House 2", 13,89,14,85,12,84,15,90),

				new House("House 3", 26,49,24,45,24,44,27,50),
				new House("House 4", 26,59,24,55,24,54,27,60),
				new House("House 5", 26,69,24,65,24,64,27,70),
				new House("House 6", 26,79,24,75,24,74,27,80),
				new House("House 7", 26,89,24,85,24,84,27,90),

				new House("House 8", 33,49,34,45,32,44,35,50),
				new House("House 9", 33,59,34,55,32,54,35,60),
				new House("House 10",33,69,34,65,32,64,35,70),
				new House("House 11",33,79,34,75,32,74,35,80),
				new House("House 12",33,89,34,85,32,84,35,90),

				new House("House 13",46,49,44,45,44,44,47,50),
				new House("House 14",46,59,44,55,44,54,47,60),
				new House("House 15",46,69,44,65,44,64,47,70),
				new House("House 16",46,79,44,75,44,74,47,80),
				new House("House 17",46,89,44,85,44,84,47,90),

				new House("House 18",53,49,54,45,52,44,55,50),
				new House("House 19",53,59,54,55,52,54,55,60),
				new House("House 20",53,69,54,65,52,64,55,70),
				new House("House 21",53,79,54,75,52,74,55,80),
				new House("House 22",53,89,54,85,52,84,55,90),

				new House("House 23",66,49,64,45,64,44,67,50),
				new House("House 24",66,59,64,55,64,54,67,60),
				new House("House 25",66,69,64,65,64,64,67,70),
				new House("House 26",66,79,64,75,64,74,67,80),
				new House("House 27",66,89,64,85,64,84,67,90),

				new House("House 28",73,49,74,45,72,44,75,50),
				new House("House 29",73,59,74,55,72,54,75,60),
				new House("House 30",73,69,74,65,72,64,75,70),
				new House("House 31",73,79,74,75,72,74,75,80),
				new House("House 32",73,89,74,85,72,84,75,90),

				new House("House 33",86,89,84,85,84,84,87,90)};

		for (House h:houses){
			time.msgAddHighPriorityReceiver(h);
			houseList.add(h);
			h.startAgent();
		}

		apartments=new Apartment[]{
				new Apartment("Apartment 0",13,16,9,9,7,7,14,18),
				new Apartment("Apartment 1",13,26,9,32,7,24,14,35),
				new Apartment("Apartment 2",26,13,32,9,24,7,35,14),
				new Apartment("Apartment 3",46,13,52,9,44,7,55,14)};

		aptOccupied=new boolean[]{false,
				false,
				false,
				false};

		for(Apartment a:apartments){
			time.msgAddHighPriorityReceiver(a);
			apartmentList.add(a);
			a.startAgent();
		}

		Building[] nearest0={markets[0],houses[0],houses[1],houses[2],houses[3],houses[4],houses[5],houses[6],houses[7],houses[8],
				houses[9],houses[10],houses[11],houses[12],houses[13],houses[14],houses[15],houses[16],houses[17]};

		Building[] nearest1={markets[1],restaurants[1],houses[18],houses[19],houses[20],houses[21],houses[22],
				houses[23],houses[24],houses[25],houses[26],houses[27],houses[28],houses[29],
				houses[30],houses[31],houses[32],houses[33]};

		Building[] nearest2={restaurants[0],restaurants[2],restaurants[3],restaurants[4]};

		Building[] nearest3={bank,markets[2],apartments[0],apartments[1],apartments[2],apartments[3]};

		busStops=new BusStop[]{
				new BusStop("Stop 0",6,85,3,85,nearest0),
				new BusStop("Stop 1",93,85,95,84,nearest1),
				new BusStop("Stop 2",76,21,78,20,nearest2),
				new BusStop("Stop 3",19,6,18,3,nearest3)};

		for(BusStop b:busStops){
			b.startAgent();
		}

		movementManager=new MovementManager("movementManager",100,100,"mapLayout.txt","mapDirections.txt","mapCosts.txt");
		movementManager.startAgent();


		buses=new Bus[]{
				new Bus("Bus 0",0,movementManager,busStops[0].getBuildingRow(),busStops[0].getBuildingCol(),busStops),
				new Bus("Bus 1",1,movementManager,busStops[1].getBuildingRow(),busStops[1].getBuildingCol(),busStops),
				new Bus("Bus 2",2,movementManager,busStops[2].getBuildingRow(),busStops[2].getBuildingCol(),busStops),
				new Bus("Bus 3",3,movementManager,busStops[3].getBuildingRow(),busStops[3].getBuildingCol(),busStops)};

		for(int i=0;i<buses.length;i++){
			CityGui.this.animationPanel.addGui(buses[i].getGui());
			buses[i].getGui().setPosition(busStops[i].getStopRow(),busStops[i].getStopCol());
			movementManager.msgAddUnit(buses[i],1,2,busStops[i].getStopRow(),busStops[i].getStopCol());
			buses[i].startAgent();
		}

	}

	private void initializeDisplay() {
		JPanel rightPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel contentPane = new JPanel();
		JPanel sliderPanel = new JPanel();
		JPanel scenarioPanel = new JPanel();
		JPanel logPanel = new JPanel(); //Placeholder until we actually integrate log
		JPanel guiPanel = new JPanel();

		playNightSong();
		playing=true;

		//Set up Gui
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension GUISIZE = new Dimension (200, 800);
		int GUISIZEX = 210;
		int GUISIZEY = WINDOWY;

		//                setBounds(50, 50, WINDOWX, WINDOWY);
		setPreferredSize(new Dimension(WINDOWX,WINDOWY));
		setMaximumSize(new Dimension(WINDOWX,WINDOWY));
		setMinimumSize(new Dimension(WINDOWX,WINDOWY));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		guiPanel.add(cityPanel);
		guiPanel.add(timePanel);
		guiPanel.add(sliderPanel);
		guiPanel.add(infoPanel);
		guiPanel.add(bottomPanel);
		guiPanel.add(logPanel);

		animationPanel.addMouseListener(this);
		animationPanel.setPreferredSize(new Dimension(800,800));
		animationPanel.setMaximumSize(new Dimension(800,800));
		animationPanel.setMinimumSize(new Dimension(800,800));

		zoomedPanel.setPreferredSize(new Dimension(605,605));
		zoomedPanel.setMaximumSize(new Dimension(605,605));
		zoomedPanel.setMinimumSize(new Dimension(605,605));

		//                rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.X_AXIS));
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setPreferredSize(new Dimension(1405,1405));
		rightPanel.setMinimumSize(new Dimension(1405,1405));
		rightPanel.setMaximumSize(new Dimension(1405,1405));
		//
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		rightPanel.add(animationPanel,c);
		//                rightPanel.add(animationPanel);
		c.gridx=1;
		c.gridy=0;
		c.fill=GridBagConstraints.VERTICAL;
		rightPanel.add(zoomedPanel,c);
		//                rightPanel.add(zoomedPanel);

		//Separation of Gui and Animations 
		JSplitPane splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setDividerLocation(GUISIZEX);
		contentPane.add(splitPane);

		//City Panel is where the user can control the animation, add people, and try nonnorms
		//Animation Panel is where all the animation will occur

		splitPane.setRightComponent(rightPanel);
		splitPane.setLeftComponent(guiPanel);
		cityPanel.setMaximumSize(new Dimension(GUISIZEX, GUISIZEY/2));
		cityPanel.setMinimumSize(new Dimension(GUISIZEX, GUISIZEY/2));
		cityPanel.setPreferredSize(new Dimension(GUISIZEX, GUISIZEY/2));

		//Date Panel
		timePanel.setMaximumSize(new Dimension(GUISIZEX,50));
		timePanel.setMinimumSize(new Dimension(GUISIZEX,50));
		timePanel.setPreferredSize(new Dimension(GUISIZEX,50));
		timePanel.setBorder(BorderFactory.createTitledBorder("Date and Time"));
		timePanel.add(timeLabel);
		timeLabel.setText("<html><i>W: "+0+" D: "+0+" H: "+0+"</i></html>");

		//Speed of day/night cycle
		JSlider timeSlider = new JSlider(JSlider.HORIZONTAL, TIME_MIN, TIME_MAX, TIME_INIT);
		timeSlider.addChangeListener(this);
		timeSlider.setMajorTickSpacing(4);
		timeSlider.setMinorTickSpacing(1);
		timeSlider.setPaintTicks(true);
		timeSlider.setPaintLabels(true);

		sliderPanel.setMaximumSize(new Dimension(GUISIZEX,70));
		sliderPanel.setMinimumSize(new Dimension(GUISIZEX,70));
		sliderPanel.setPreferredSize(new Dimension(GUISIZEX,70));
		sliderPanel.setBorder(BorderFactory.createTitledBorder("Speed of Day/Night cycle"));
		sliderPanel.add(timeSlider);

		//Scenario Panel
		//		scenarioPanel.add(allowCollisionBox);
		//		scenarioPanel.setLayout(new GridLayout(5,3));
		//		scenarioPanel.setMaximumSize(new Dimension(250,500));
		//		scenarioPanel.setMinimumSize(new Dimension(250,500));
		//		scenarioPanel.setPreferredSize(new Dimension(250,500));
		//		scenarioPanel.add(musicToggle);
		//		for(JButton s: scenarios){
		//			scenarioPanel.add(s);
		//		}


		//Info Panel
		infoPanel.setMaximumSize(new Dimension(GUISIZEX,80));
		infoPanel.setMinimumSize(new Dimension(GUISIZEX,80));
		infoPanel.setPreferredSize(new Dimension(GUISIZEX,80));
		infoPanel.setBorder(BorderFactory.createTitledBorder("Information Panel"));
		infoPanel.add(infoLabel);

		//Scenario Panel
//		scenarioPanel.add(allowCollisionBox);
		scenarioPanel.setBorder(BorderFactory.createTitledBorder("Scenarios"));
		scenarioPanel.setLayout(new GridLayout(7,1));
		scenarioPanel.setVisible(true);
		scenarioPanel.setMaximumSize(new Dimension(GUISIZEX,150));
		scenarioPanel.setMinimumSize(new Dimension(GUISIZEX,150));
		scenarioPanel.setPreferredSize(new Dimension(GUISIZEX,150));
		for(JButton s: scenarios){
			scenarioPanel.add(s);
		}
		scenarioPanel.add(musicToggle);
		bottomPanel.add(scenarioPanel);

		//Trace Log
		traceFrame.setMaximumSize(new Dimension(605,400));
		traceFrame.setMinimumSize(new Dimension(605,400));
		traceFrame.setPreferredSize(new Dimension(605,400));
		traceFrame.setVisible(true);
		traceFrame.getTracePanel().showAlertsWithTag(AlertTag.CONSOLE);
		AlertLog.getInstance().addAlertListener(traceFrame.getTracePanel());
	}


	public void playDaySong(){
		try {
			sequencer.setSequence(MidiSystem.getSequence(new FileInputStream("music/day.mid")));        
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(InvalidMidiDataException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		try{
			sequencer.open();
		}catch (MidiUnavailableException e){
			e.printStackTrace();
		}
		sequencer.start();
	}

	public void playNightSong(){
		try {
			sequencer.setSequence(MidiSystem.getSequence(new FileInputStream("music/kk_lovesong.mid")));        
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(InvalidMidiDataException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		try{
			sequencer.open();
		}catch (MidiUnavailableException e){
			e.printStackTrace();
		}
		sequencer.start();
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(allowCollisionBox)){
			if (allowCollisionBox.isSelected()){
				//TODO allow Collisions
			}
			else{
				//TODO Don't allow collisions
			}
		}

		//Populate City
		if (e.getSource().equals(scenarios[0])){
			//If filling the city, disable adding a person
			populateCity();
			scenarios[0].setEnabled(false);
			cityPanel.addPersonBtn.setEnabled(false);
		}


		if (e.getSource() == musicToggle){
			if (playing){
				playing=false;
				sequencer.stop();
				musicToggle.setText("Turn Music On");
			}
			else{
				playing=true;
				musicToggle.setText("Turn Music Off");
				if (currentSong.equals("dayInput")){
					playDaySong();
				}
				else{
					playNightSong();
				}
			}
		}
	}
	public void updateDatePanel(long t, String day){
		//                timeLabel.setText("<html><i>Date: " + time.getTime()+ ", " + t + "</i></html>");
		timePanel.validate();
	}

	public void updateInfoPanel(MyPerson p){
		infoLabel.setText("<html><b> Name: "+ p.person.getAgentName() + "<br>Job: "+p.job+"<br>Workplace: "+p.workplace+"</b></html>");
		infoPanel.revalidate();
	}
	
	public void populateCity(){
		cityPanel.addPerson("Robber 1",500.0,"Bank Robber",true,true);
		cityPanel.addPerson("Robber 2",500.0,"Bank Robber",false,true);
		Random generator=new Random();
		for(int i=0;i<300;i++){
			String job;
			switch(generator.nextInt(7)){
			case 0:
				job="Bank Teller";
			break;
			case 1:
				job="Market Teller";
			break;
			case 2:
				job="Unemployed";
			break;
			default:
				job="Rest: (Any)";
			break;
			}
			cityPanel.addPerson("p"+i,500.0,job,generator.nextBoolean(),generator.nextInt()%3==0);
		}
		revalidate();
	}

	///////ChangeListener methods///////
	public void stateChanged(ChangeEvent e) {
		JSlider slider=(JSlider)e.getSource();
		if(!slider.getValueIsAdjusting()){
			int speed=slider.getValue();
			Broadcaster.setMinuteMilis(100*(slider.getMaximum()-speed+1));
			animationPanel.setTimerDelay(speed);
			bank.getGui().setTimerDelay(speed);
			for(Market m:markets){
				m.getGui().setTimerDelay(speed);
			}
			for(Restaurant r:restaurants){
				r.getGui().setTimerDelay(speed);
			}
			for(House h:houses){
				h.getGui().setTimerDelay(speed);
			}
		}
	}
	/////////MouseListener methods///////
	public void mouseClicked(MouseEvent e){
		if (bank.getRect().contains(e.getX(),e.getY())){
			zoomedPanel.setBuilding(bank);
			animationPanel.setSelection(bank.getRect());
			if(!zoomedPanel.isVisible()){
				zoomedPanel.setVisible(true);
			}
		}

		for (Restaurant r: restaurants){
			if (r.getRect().contains(e.getX(),e.getY())){
				zoomedPanel.setBuilding(r);
				animationPanel.setSelection(r.getRect());
				if(!zoomedPanel.isVisible()){
					zoomedPanel.setVisible(true);
				}
			}        
		}

		for(Market m:markets){
			if (m.getRect().contains(e.getX(),e.getY())){
				zoomedPanel.setBuilding(m);
				animationPanel.setSelection(m.getRect());
				if(!zoomedPanel.isVisible()){
					zoomedPanel.setVisible(true);
				}
			}        
		}

		for(House h:houses){
			if (h.getRect().contains(e.getX(),e.getY())){
				zoomedPanel.setBuilding(h);
				animationPanel.setSelection(h.getRect());
				if(!zoomedPanel.isVisible()){
					zoomedPanel.setVisible(true);
				}
			}        
		}

		for(Apartment a:apartments){
			if (a.getRect().contains(e.getX(),e.getY())){
				zoomedPanel.setBuilding(a);
				animationPanel.setSelection(a.getRect());
				if(!zoomedPanel.isVisible()){
					zoomedPanel.setVisible(true);
				}
			}        
		}
	}

	public void mouseEntered(MouseEvent e){
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e){
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e){
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e){
		// TODO Auto-generated method stub

	}


	private class CityPanel extends JPanel implements ActionListener{
		private static final int gridLayoutX=1;
		private static final int gridLayoutY=2;
		private static final int gridLayoutGAP=10;

		private final Random generator;

		String type;        
		private JTextField moneyPanel;
		private JTextField namePanel;
		private JPanel transportationPanel;
		private JPanel housingPanel;
		private JPanel addPersonPanel;
		private JRadioButton apartmentBtn;
		private JRadioButton houseBtn;
		private JRadioButton carBtn;
		private JRadioButton busBtn;
		private JRadioButton walkBtn;
		private JButton addPersonBtn;
		private final ButtonGroup housingButtonGroup;
		private final ButtonGroup transportationButtonGroup;

		private JComboBox<String> jobBox;
		public JScrollPane scrollPane;
		private JPanel view;

		private final List<JButton> peopleList;
		private MyPerson selectedPerson;

		public CityPanel(){
			generator=new Random();

			peopleList = new ArrayList<JButton>();
			selectedPerson=null;

			setLayout(new GridLayout(1,2,0,0));
			addPersonPanel = new JPanel();
			addPersonPanel.setLayout(new BoxLayout(addPersonPanel,BoxLayout.Y_AXIS));
			addPersonPanel.setMaximumSize(addPersonPanel.getPreferredSize());
			scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			view = new JPanel();
			view.setMaximumSize(view.getPreferredSize());
			//////Creation of a person//////
			//name
			namePanel = new JTextField();
			namePanel.setColumns(20);
			namePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(Color.BLACK),"<html><font size=-2>Name</font></html>"));
			namePanel.setMaximumSize(namePanel.getPreferredSize());
			addPersonPanel.add(namePanel);

			//money
			moneyPanel = new JTextField();
			moneyPanel.setColumns(20);
			moneyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(Color.BLACK),"<html><font size=-2>Money</font></html>"));
			moneyPanel.setMaximumSize(moneyPanel.getPreferredSize());
			addPersonPanel.add(moneyPanel);

			//housing
			housingButtonGroup = new ButtonGroup();
			housingPanel = new JPanel();
			housingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(Color.BLACK),"<html><font size=-2>Housing</font></html>"));
			addPersonPanel.add(housingPanel);

			apartmentBtn = new JRadioButton("<html><font size=-2>Apartment</font></html>");
			apartmentBtn.addActionListener(this);
			apartmentBtn.setEnabled(true);
			housingButtonGroup.add(apartmentBtn);
			housingPanel.add(apartmentBtn);

			houseBtn = new JRadioButton("<html><font size=-2>House</font></html>");
			houseBtn.setSelected(true);
			houseBtn.addActionListener(this);
			housingButtonGroup.add(houseBtn);
			housingPanel.add(houseBtn);
			housingPanel.setMaximumSize(new Dimension(210,100));

			//transportation
			transportationButtonGroup = new ButtonGroup();
			transportationPanel = new JPanel();
			transportationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(Color.BLACK),"<html><font size=-2>Transportation</font></html>"));
			addPersonPanel.add(transportationPanel);

			walkBtn = new JRadioButton("<html><font size=-2>Walk/Ride Bus</font.</html>");
			walkBtn.setSelected(true);
			walkBtn.addActionListener(this);
			transportationButtonGroup.add(walkBtn);
			transportationPanel.add(walkBtn);

			carBtn = new JRadioButton("<html><font size=-2>Car</font></html>");
			carBtn.setEnabled(true);
			carBtn.addActionListener(this);
			transportationButtonGroup.add(carBtn);
			transportationPanel.add(carBtn);
			transportationPanel.setMaximumSize(new Dimension(210,100));

			jobBox = new JComboBox();
			jobBox.setMaximumSize(new Dimension(200,jobBox.getPreferredSize().height));
			jobBox.addItem("Bank Teller");
			jobBox.addItem("Bank Robber");
			jobBox.addItem("Rest: (Any)");
			jobBox.addItem("Rest: Host");
			jobBox.addItem("Rest: Cook");
			jobBox.addItem("Rest: Cashier");
			jobBox.addItem("Waiter (Any)");
			jobBox.addItem("Waiter Regular");
			jobBox.addItem("Waiter Special");
			jobBox.addItem("Market Teller");
			jobBox.addItem("Unemployed");
			jobBox.addActionListener(this);
			addPersonPanel.add(jobBox);

			addPersonBtn = new JButton("Add Person");
			addPersonBtn.addActionListener(this);
			addPersonBtn.setAlignmentX(CENTER_ALIGNMENT);
			addPersonPanel.add(addPersonBtn);
			addPersonBtn.setMaximumSize(new Dimension(210,40));

			addPersonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
			add(addPersonPanel);

			view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
			scrollPane.setViewportView(view);
			scrollPane.setMaximumSize(new Dimension(WINDOWX/4, WINDOWY/5));
			scrollPane.setMinimumSize(new Dimension(WINDOWX/4, WINDOWY/5));
			scrollPane.setPreferredSize(new Dimension(WINDOWX/4, WINDOWY/5));
			add(scrollPane);

		}

		public void showInfo(JButton button){
			if (selectedPerson!=null){
				selectedPerson.person.getGui().toggleSelected();
			}
			for (MyPerson p: persons){
				if (p.button==button){
					CityGui.this.updateInfoPanel(p);
					p.person.getGui().toggleSelected();
					selectedPerson=p;
					break;
				}
			}
		}

		public Building getNextJob(String job,PuppetType[] t,Integer[] s){
			BlockingData<Integer> timeSlot=new BlockingData<Integer>();
			BlockingData<PuppetType> jeeb=new BlockingData<PuppetType>();
			Building result;
			switch(job){
			case "Bank Teller":
				bank.msgFillAnyOpening(timeSlot,jeeb);
				s[0]=timeSlot.get();
				t[0]=jeeb.get();
				if(t[0]!=null && s[0]!=null){
					return bank;
				}
				break;
			case "Bank Robber":
				t[0]=PuppetType.bankRobber;
				return bank;
			case "Market Teller":
				for(int i=0;i<markets.length;i++){
					marketList.get(0).msgFillAnyOpening(timeSlot,jeeb);
					s[0]=timeSlot.get();
					t[0]=PuppetType.markClerk;
					result=marketList.remove(0);
					marketList.add((Market)result);
					if(s[0]!=null && t[0]!=null){
						System.err.println(result.getAgentName()+" "+s[0]);
						return result;
					}
				}
				break;
			case "Rest: Host":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.restHost);
					s[0]=timeSlot.get();
					t[0]=PuppetType.restHost;
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null){
						return result;
					}
				}
				break;
			case "Waiter Regular":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.restRegWaiter);
					s[0]=timeSlot.get();
					t[0]=PuppetType.restRegWaiter;
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null){
						return result;
					}
				}
				break;
			case "Waiter Special":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.restStandWaiter);
					s[0]=timeSlot.get();
					t[0]=PuppetType.restStandWaiter;
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null){
						return result;
					}
				}
				break;
			case "Waiter (Any)":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.restAnyWaiter);
					s[0]=timeSlot.get();
					t[0]=PuppetType.restAnyWaiter;
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null){
						return result;
					}
				}
				break;
			case "Rest: Cook":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.restCook);
					s[0]=timeSlot.get();
					t[0]=PuppetType.restCook;
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null){
						return result;
					}
				}
				break;
			case "Rest: Cashier":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.restCashier);
					s[0]=timeSlot.get();
					t[0]=PuppetType.restCashier;
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null){
						return result;
					}
				}
				break;
			case "Rest: (Any)":
				for(int i=0;i<restaurants.length;i++){
					restaurantList.get(0).msgFillAnyOpening(timeSlot,jeeb);
					s[0]=timeSlot.get();
					t[0]=jeeb.get();
					result=restaurantList.remove(0);
					restaurantList.add((Restaurant)result);
					if(s[0]!=null && t[0]!=null){
						return result;
					}
				}
				break;
			}
			return null;
		}

		public House getNextHouse(Integer[] s){
			BlockingData<Integer> timeSlot=new BlockingData<Integer>();
			houseList.get(0).msgFillSpecifiedOpening(timeSlot,PuppetType.resident);
			s[0]=timeSlot.get();
			House temp=houseList.remove(0);
			houseList.add(temp);
			if(s[0]!=null){
				return temp;
			}
			return null;
		}

		public Apartment getNextApartment(Integer[] s){
			BlockingData<Integer> timeSlot=new BlockingData<Integer>();
			apartmentList.get(0).msgFillSpecifiedOpening(timeSlot, PuppetType.resident);
			s[0]=timeSlot.get();
			Apartment temp=apartmentList.remove(0);
			apartmentList.add(temp);
			if(s[0]!=null){
				return temp;
			}
			return null;
		}

		//Used for adding a person manually//
		public void addPerson(String name,double money,String job,boolean house,boolean car){

			Person.Setup s=new Setup();
			Integer[] homeSlot={null};
			Integer[] jobSlot={null};
			PuppetType[] jeeb={null};

			JButton button=new JButton(name);

			if(house){
				for(int i=0;i<houses.length;i++){
					s.home=getNextHouse(homeSlot);
					if(s.home!=null){
						break;
					}
				}
				if(s.home==null){
					AlertLog.getInstance().logError(AlertTag.CONSOLE, "Console", "There are no houses left! Sucks.");
					return;
				}
			}
			else{
				for(int i=0;i<apartments.length;i++){
					s.home=getNextApartment(homeSlot);
					if(s.home!=null){
						s.apartmentRoomNumber=homeSlot[0];
						break;
					}
				}
				if(s.home==null){
					AlertLog.getInstance().logError(AlertTag.CONSOLE, "Console", "There are no apartments left! Sucks.");
					return;
				}
			}

			s.workplace=getNextJob(job,jeeb,jobSlot);
			if(s.workplace==null && !job.equals("Unemployed")){
				AlertLog.getInstance().logError(AlertTag.CONSOLE, "Console", "There are no Jobs of that type available.");
				return;
			}
			s.bank=bank;
			s.markets=markets;
			s.restaurants=restaurants;
			s.busStops=busStops;
			s.job=jeeb[0];
			if(job.equals("Bank Robber")){
				s.workStartTime=20;
				s.workStopTime=23;
			}
			else if(!job.equals("Unemployed")){
				s.workStartTime=Building.getStartHour(jobSlot[0]);
				s.workStopTime=Building.getEndHour(jobSlot[0]);
			}
			s.hungerThresh=5;
			s.moneyLow=100;
			s.moneyHigh=1000;
			s.startingMoney=money;
			s.startingBalance=1000;
			s.hasCar=car;
			s.currentTime=currentTime;

			Person p;

			if(car){
				p=new Person("name",movementManager,CAR_SPAWN_R,CAR_SPAWN_C,s);
				p.getGui().setPosition(CAR_SPAWN_R,CAR_SPAWN_C);
				animationPanel.addGui(p.getGui());
				movementManager.msgAddUnit(p,1,2,CAR_SPAWN_R,CAR_SPAWN_C);
				p.startAgent();
			}
			else{
				p=new Person("name",movementManager,PERSON_SPAWN_R,PERSON_SPAWN_C,s);
				p.getGui().setPosition(PERSON_SPAWN_R,PERSON_SPAWN_C);
				animationPanel.addGui(p.getGui());
				movementManager.msgAddUnit(p,2,1,PERSON_SPAWN_R,PERSON_SPAWN_C);
				p.startAgent();
			}

			time.msgAddNormalReceiver(p);

			if(s.workplace==null){
				persons.add(new MyPerson(p,job,"Unemployed",button));
			}
			else{
				persons.add(new MyPerson(p,job,s.workplace.getAgentName(),button));
			}

			//Adding a button for the person
			JPanel panel=new JPanel();
			JLabel label=new JLabel (name);

			Dimension paneSize=scrollPane.getSize();
			Dimension buttonSize=new Dimension(paneSize.width-20,(int)(paneSize.height/10));

			button.setBackground(Color.white);
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.setVisible(true);

			button.addActionListener(this);

			panel.setName(name);
			panel.add(label);

			showInfo(button);

			peopleList.add(button);
			view.add(button);

			revalidate();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addPersonBtn){
				//Adding a person
				boolean moneyIsNumeric = true;
				try {
					double d = Double.parseDouble(moneyPanel.getText());
				}
				catch (NumberFormatException nfe) {  
					moneyIsNumeric = false;
				}
				if (!namePanel.getText().equals("")&& !moneyPanel.getText().equals("") && moneyIsNumeric){
					PuppetType job=null;
					String jobString = (String) jobBox.getSelectedItem();
					double money = new Double(moneyPanel.getText());
					boolean hasCar = carBtn.isSelected();
					boolean hasHouse = houseBtn.isSelected();
					addPerson(namePanel.getText(),money,jobString,hasHouse,hasCar);
					//If adding a person, disable fillCity()
					scenarios[0].setEnabled(false);
					revalidate();
				}
				else {
					AlertLog.getInstance().logError(AlertTag.CONSOLE, "Console", "Please complete all attributes.  Money field must be numeric.");
				}
			}
			else{
				for(JButton pl:peopleList){
					if(e.getSource()==pl){
						showInfo(pl);
					}
				}
			}
		}
	}

	public void msgUpdateTime(long time){
		currentTime=time;
		long hours=time%Broadcaster.DAY_HOURS;
		long day=(time/Broadcaster.DAY_HOURS)%Broadcaster.WEEK_DAYS;

		timeLabel.setText("<html><i>W: "+(time/(Broadcaster.WEEK_DAYS*Broadcaster.DAY_HOURS))%Broadcaster.MONTH_WEEKS+" D: "+(time/Broadcaster.DAY_HOURS)%Broadcaster.WEEK_DAYS+" H: "+time%Broadcaster.DAY_HOURS+"</i></html>");
			if (time%6==0){
				animationPanel.setBackgroundColor(hours,day);
			}
			if ((hours==6)||(hours==18)){
				toggleMusic();
			}
		}

	public void toggleMusic(){
		if (currentSong.equals("dayInput")){
			currentSong="nightInput";
			sequencer.stop();
			if (playing){
				playNightSong();
			}
		}
		else if (currentSong == "nightInput"){
			currentSong = "dayInput";
			sequencer.stop();
			if (playing){
				playDaySong();
			}
		}
	}
}
