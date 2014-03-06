package city.bank;

import java.util.Timer;
import java.util.TimerTask;







import trace.AlertLog;
import trace.AlertTag;
import UnitTestingCommon.interfaces.BankRobberInterface;
import astar.MovementManager;
import city.Building;
import city.Person;
import city.Puppet;
import city.bank.gui.BankGui;
import city.bank.gui.BankRobberGui;

public class BankRobber extends Puppet{

	//The bank robber is a crafty thief. He sneaks in late at night when no one is in the bank and loots the safe.

	private BankRobberGui robGui;
	private Person         controllerId;
	private BankGui mainGui;

	Timer stealing = new Timer();

	private int luckOfTheDraw;
	private int breakIn;
	private int moneyStolen = 1000;
	private enum bankRobberStates {Entering,MovingToSafe, PickingLock, Looting, GrabMoney, Leaving, Exit, Left};
	private bankRobberStates state = bankRobberStates.Entering;

	public BankRobber(String name, MovementManager m, Building b, Setup s) {
		super(name, m, b, s);
		// TODO Auto-generated constructor stub

		this.controllerId=s.master;

		robGui= new BankRobberGui(this);
	}

	public BankRobberGui getGui(){
		return robGui;
	}

	public void setMainGui(BankGui mainGui) {
		this.mainGui = mainGui;
	}


	public void msgBrokeSafe(){
		enqueMutation(new Mutation(){
			public void apply(){                                                    //robber breaks into safe
				luckOfTheDraw=(int) (Math.random()*4);
				if(luckOfTheDraw==0){
					moneyStolen=50;
				}
				else if (luckOfTheDraw==1){
					moneyStolen=100;
				}
				else if (luckOfTheDraw==2){
					moneyStolen=500;
				}
				else if (luckOfTheDraw==3){
					moneyStolen=1000;
				}
				AlertLog.getInstance().logMessage(AlertTag.BANK_ROBBER, getAgentName(), "Broke the safe!");
				state=bankRobberStates.Looting;

			}
		});
	}

	public void msgFailedToGetIn(){
		enqueMutation(new Mutation(){
			public void apply(){                                                    //robber fails to breaks into safe
				AlertLog.getInstance().logMessage(AlertTag.BANK_ROBBER, getAgentName(), "Damn! Couldn't get in.");
				moneyStolen=0;
				state=bankRobberStates.Leaving;
				BankRobber.this.setNewDestination(1, 28);

			}
		});
	}

	@Override
	public void msgLeaveBuilding() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean pickAndExecuteAction() {
		// TODO Auto-generated method stub

		if(state==bankRobberStates.Entering){
			state=bankRobberStates.MovingToSafe;
			this.setNewDestination(5, 15);
			return true;
		}


		if(state==bankRobberStates.Looting){
			state=bankRobberStates.GrabMoney;
			this.setNewDestination(3, 15);
			return true;
		}


		if(state==bankRobberStates.Leaving){
			state=bankRobberStates.Exit;

			return true;
		}


		return false;
	}

	@Override
	protected void actMoveStep(int r, int c) {
		// TODO Auto-generated method stub
		robGui.setDestination(r, c);
	}

	@Override
	protected void actReactToBlockedRoute() {
		// TODO Auto-generated method stub
		this.actWaitRandomAndTryAgain();
	}

	@Override
	protected void actArrivedAtDestination() {
		// TODO Auto-generated method stub
		if(state==bankRobberStates.MovingToSafe){
			state=bankRobberStates.PickingLock;
			pickTheLock();
		}

		if(state==bankRobberStates.GrabMoney){
			state=bankRobberStates.Leaving;
			this.setNewDestination(1, 28);
		}

		if(state==bankRobberStates.Exit){
			state=bankRobberStates.Left;
			mainGui.removeGui(robGui);
			EscapeTheScene();
		}
	}

	private void EscapeTheScene(){
		if(moneyStolen==0){
			AlertLog.getInstance().logMessage(AlertTag.BANK_ROBBER, getAgentName(), "I'll get them next time...");

		}
		else{
			AlertLog.getInstance().logMessage(AlertTag.BANK_ROBBER, getAgentName(), "Made of with " + (moneyStolen) + " dollars!!");
			controllerId.msgAddMoney(moneyStolen);
		}
		getMaster().msgPuppetLeftBuilding();
		this.removeFromManager();
		this.stopAgent();
	}

	private void pickTheLock(){

		stealing.schedule(new TimerTask() {


			public void run(){
				//finishOrder
				breakIn=(int) (Math.random()*2);    
				if(breakIn==0){
					BankRobber.this.msgBrokeSafe();
				}
				else{
					BankRobber.this.msgFailedToGetIn();
				}

			}


		},
		2000);

	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
}
