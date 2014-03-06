package trace;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class TraceFrame extends JFrame{
	private static final int traceWINDOWX=605;
	private static final int traceWINDOWY=400;

	private TracePanel tracePanel;
	private ControlPanel controlPanel;

	public TraceFrame(){

		setTitle("Trace Log");
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		tracePanel=new TracePanel();

		controlPanel=new ControlPanel(tracePanel);
		controlPanel.setMinimumSize(new Dimension(200,traceWINDOWY));
		controlPanel.setMaximumSize(new Dimension(200, traceWINDOWY));
		controlPanel.setPreferredSize(new Dimension(200,traceWINDOWY));
		add(controlPanel);

		tracePanel.setMinimumSize(new Dimension(traceWINDOWX-200, traceWINDOWY));
		tracePanel.setMaximumSize(new Dimension(traceWINDOWX-200, traceWINDOWY));
		tracePanel.setPreferredSize(new Dimension(traceWINDOWX-200, traceWINDOWY));
		add(tracePanel);

	}
	public TracePanel getTracePanel(){
		return tracePanel;
	}

	private class ControlPanel extends JPanel {
		TracePanel tp;      

		private final JToggleButton messagesButton;              
		private final JToggleButton errorButton;
		private final JToggleButton astarButton;
		private final JToggleButton personButton;
		private final JToggleButton busButton;
		private final JToggleButton houseButton;
		private final JToggleButton aptButton;
		private final JToggleButton bankButton;
		private final JToggleButton markButton;
		private final JToggleButton restButton;
		private final JToggleButton agentButton;
		
		public ControlPanel(final TracePanel tracePanel) {
			this.tp = tracePanel;
			
			aptButton=new JToggleButton("Show Tag: APARTMENT");
			astarButton = new JToggleButton("Show Tag: ASTAR");	
			bankButton = new JToggleButton("Show Tag: BANK");	
			agentButton=new JToggleButton("Show Tag: BASE AGENT");
			busButton=new JToggleButton("Show Tag: BUS");
			errorButton = new JToggleButton("Show Level: ERRORS");
			houseButton=new JToggleButton("Show Tag: HOUSE");
			personButton= new JToggleButton("Show Tag: PERSON");
			restButton=new JToggleButton("Show Tag: RESTAURANT");
			markButton=new JToggleButton("Show Tag: MARKET");
			
			messagesButton = new JToggleButton("Show Level: MESSAGES");

			messagesButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (messagesButton.isSelected()){
						tracePanel.showAlertsWithLevel(AlertLevel.MESSAGE);
						messagesButton.setText("Hide Level: MESSAGES");
					}
					else{
						tracePanel.hideAlertsWithLevel(AlertLevel.MESSAGE);
						messagesButton.setText("Show Level: MESSAGES");
					}
				}
			});
			errorButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (errorButton.isSelected()){
						tracePanel.showAlertsWithLevel(AlertLevel.ERROR);
						errorButton.setText("Hide Level: ERRORS");
					}
					else{
						tracePanel.hideAlertsWithLevel(AlertLevel.ERROR);
						errorButton.setText("Show Level: ERRORS");
					}
				}
			});
			
				astarButton.addItemListener(new ItemListener(){
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (astarButton.isSelected()){
							tracePanel.showAlertsWithTag(AlertTag.ASTAR);
							astarButton.setText("Hide Tag: ASTAR");
						}
						else{
							tracePanel.hideAlertsWithTag(AlertTag.ASTAR);
							astarButton.setText("Show Tag: ASTAR");
						}
					}
				});
			
		////////Bank Tags////////
			bankButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (bankButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.BANK);
						tracePanel.showAlertsWithTag(AlertTag.BANK_TELLER);
						tracePanel.showAlertsWithTag(AlertTag.BANK_CUSTOMER);
						tracePanel.showAlertsWithTag(AlertTag.BANK_ROBBER);

						bankButton.setText("Hide Tag: BANK");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.BANK);
						tracePanel.hideAlertsWithTag(AlertTag.BANK_TELLER);
						tracePanel.hideAlertsWithTag(AlertTag.BANK_CUSTOMER);
						tracePanel.hideAlertsWithTag(AlertTag.BANK_ROBBER);	
						
						bankButton.setText("Show Tag: BANK");
					}
				}
			});
			
			////////Restaurant Tags///////
			restButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (restButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.REST);
						tracePanel.showAlertsWithTag(AlertTag.REST_CUSTOMER);
						tracePanel.showAlertsWithTag(AlertTag.REST_COOK);
						tracePanel.showAlertsWithTag(AlertTag.REST_CASHIER);
						tracePanel.showAlertsWithTag(AlertTag.REST_WAITER);

						restButton.setText("Hide Tag: RESTAURANT");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.REST);
						tracePanel.hideAlertsWithTag(AlertTag.REST_CUSTOMER);
						tracePanel.hideAlertsWithTag(AlertTag.REST_COOK);
						tracePanel.hideAlertsWithTag(AlertTag.REST_CASHIER);
						tracePanel.hideAlertsWithTag(AlertTag.REST_WAITER);
						
						restButton.setText("Show Tag: RESTAURANT");
					}
				}
			});
			/////Person Tags/////
			personButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (personButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.PERSON);
						personButton.setText("Hide Tag: PERSON");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.PERSON);
						personButton.setText("Show Tag: PERSON");
					}
				}
			});
			
			/////Bus Tags////
			busButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (busButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.BUS_STOP);
						tracePanel.showAlertsWithTag(AlertTag.BUS);
						busButton.setText("Hide Tag: BUS");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.BUS_STOP);
						tracePanel.hideAlertsWithTag(AlertTag.BUS);
						busButton.setText("Show Tag: BUS");
					}
				}
			});
			//////House//////
			houseButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (houseButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.HOUSE);
						houseButton.setText("Hide Tag: HOUSE");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.HOUSE);
						houseButton.setText("Show Tag: HOUSE");
					}
				}
			});
			
			//////Apartment//////
			aptButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (aptButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.APT);
						aptButton.setText("Hide Tag: APARTMENT");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.APT);
						aptButton.setText("Show Tag: APARTMENT");
					}
				}
			});
			
			//////Agent///////
			agentButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (agentButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.AGENT);
						agentButton.setText("Hide Tag: BASE AGENT");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.AGENT);
						agentButton.setText("Show Tag: BASE AGENT");
					}
				}
			});
			
			markButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (markButton.isSelected()){
						tracePanel.showAlertsWithTag(AlertTag.MARK);
						tracePanel.showAlertsWithTag(AlertTag.MARK_CLERK);
						tracePanel.showAlertsWithTag(AlertTag.MARK_CUSTOMER);

						markButton.setText("Hide Tag: MARKET");
					}
					else{
						tracePanel.hideAlertsWithTag(AlertTag.MARK);
						tracePanel.hideAlertsWithTag(AlertTag.MARK_CLERK);
						tracePanel.hideAlertsWithTag(AlertTag.BANK_CUSTOMER);
						
						markButton.setText("Show Tag: MARKET");
					}
				}
			});
			
			this.setLayout(new GridLayout(10,1));
//			this.add(messagesButton);
			this.add(aptButton);
			this.add(astarButton);
			this.add(bankButton);
			this.add(agentButton);
			this.add(busButton);
			this.add(errorButton);
			this.add(houseButton);
			this.add(markButton);
			this.add(personButton);
			this.add(restButton);

			this.setMinimumSize(new Dimension(50, 600));
			}
		}
	}
