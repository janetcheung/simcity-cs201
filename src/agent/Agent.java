package agent;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import trace.AlertLog;
import trace.AlertTag;

public abstract class Agent{
	private static final List<Agent> agentList=new ArrayList<Agent>();
	private static boolean J_UNIT=false;
	
	private final AgentSem newMutation;
	private final List<Mutation> mutations; // queue of mutations to be applied
	private final AgentThread thread; // this agent's thread
	private final String agentName;
	
	public Agent(String name){
		newMutation=new AgentSem(1);
		mutations=new ArrayList<Mutation>();
		thread=new AgentThread();
		agentName=name;
		synchronized(agentList){
			agentList.add(this);
		}
	}
	
	public String getAgentName(){
		return agentName;
	}
	
	public void startAgent(){
		if(!thread.running()){
			thread.start();
		}
	}
	
	public void stopAgent(){
		thread.stopThread();
		newMutation.release();
	}
	
	public final boolean threadRunning() {
		return thread.running();
	}
	
	/* adds a mutation to the mutation queue, will be applied by the AgentThread */
	protected final void enqueMutation(Mutation m){
		if(J_UNIT){
			m.apply();
		}
		else{
			synchronized(mutations){
				mutations.add(m);
			}
			/* release the sem */
			newMutation.release();
		}
	}
	
	/* the scheduler */
	protected abstract boolean pickAndExecuteAction();
	
	public boolean callScheduler(){
		if(J_UNIT){
			return pickAndExecuteAction();
		}
		return false;
	}
	
	protected abstract void destructor();
	
	public final void msgPauseAgent(){
		thread.pauseThread();
	}
	
	public final void msgResumeAgent(){
		thread.resumeThread();
	}
	
	public final void msgWakeUp(){
		newMutation.release();
	}
	
	/* delay the calling thread for some milis milliseconds */
	public static void delay(long milis){
		try{
			Thread.sleep(milis);
		}
		catch(InterruptedException e){
			
		}
		catch(Exception e){
			System.err.println("An unexpected exception has occurred in Agent...");
			e.printStackTrace();
		}
	}
	
	public static void setJUNIT(boolean b){
		J_UNIT=b;
	}
	
	public static void killAllAgents(){
		synchronized(agentList){
			for(Agent a:agentList){
				a.stopAgent();
			}
		}
	}
	
	public static void pauseAllAgents(){
		synchronized(agentList){
			for(Agent a:agentList){
				a.msgPauseAgent();
			}
		}
	}
	
	public static void resumeAllAgents(){
		synchronized(agentList){
			for(Agent a:agentList){
				a.msgResumeAgent();
			}
		}
	}
	
	/* a Mutation is basically a function that modifies data in the Agent. you should use anonymous classes
	 * in your message methods to define Mutations, and enqueue them with enqueMutation at the end of the
	 * message method. there are only a few exceptions to this, one of which is releasing a semaphore that
	 * has paused this agent's thread. the message method in that case should release the semaphore directly
	 * instead of adding a Mutation that does it (or else this agent's thread will be paused forever!) 
	 * apply is called by this agent's thread and does the actual writing of data. this whole mechanism 
	 * may seem scary, but if you guys take a moment to wrap your heads around it, we will not have to think 
	 * about thread safety (concurrent modification errors) for the rest of this project */
	protected interface Mutation{
		public void apply();
	}
	
	/* our Agent Thread */
	private class AgentThread extends Thread{
		private final AgentSem resume;
		private volatile boolean paused;
		private volatile boolean alive;
		
		private AgentThread(){
			resume=new AgentSem(0);
			alive=false;
			paused=false;
		}
		
		public void run(){
			alive=true;
			while(alive){
				/* if our Mutation queue is not empty, we have mutations to apply (data to write) 
				 * and possibly actions to execute, so we will get past this acquire */
				newMutation.acquire();
				/* do-while loops always run at least once */
				do{
					/* for pausing */
					if(paused){
						resume.acquire();
					}
					
					List<Mutation> copy;
					
					/* copy all mutations to new list so we can quickly get out of the critical section */
					synchronized(mutations){
						copy=new ArrayList<Mutation>(mutations);
						/* clear them */
						mutations.clear();
					}
					
					/* apply all accumulated mutations (write all the data that needs to be written) in the queue */
					for(Mutation m:copy){
						m.apply();
					}
					
					/* now call the scheduler once */
				}
				while(alive && pickAndExecuteAction());
				/* if it returned true, repeat, applying any more mutations that might have accumulated while we were
				 * executing an action. if it returned false, go back to while(alive), and if more mutations accumulated
				 * while we were executing that last action, we will get past newMutation again, the mutations will be 
				 * applied rinse and repeat. the power of this is that there are only 1 piece of data in an agent that
				 * that can be written to by threads other than the agent's own thread, the list of mutations, which is 
				 * protected by critical sections (so thread safe). */
			}
			
			synchronized(agentList){
				agentList.remove(Agent.this);
			}
			
			destructor();
			
			AlertLog.getInstance().logMessage(AlertTag.AGENT,getAgentName(),"Destroyed.");
		}
		
		public synchronized boolean running(){
        	return alive;
        }
		
		public synchronized void stopThread(){
			alive=false;
		}
		
		public synchronized void pauseThread(){
			paused=true;
		}
		
		public synchronized void resumeThread(){
			if(paused){
				paused=false;
				resume.release();
			}
		}
	}
	
	/* you should use this class instead of Semaphore unless you really like typing try/catch blocks 
	 * over and over again (ie you hate yourself). if you want the sem to do something other than 
	 * nothing (rare) in the event of an InterruptedException, use exceptionAcquire with try/catch block */
	public static class AgentSem extends Semaphore{
		public AgentSem(int permits){
			super(permits);
		}
		
		public AgentSem(int permits,boolean fair){
			super(permits,fair);
		}
		
		public void acquire(){
			try{
				super.acquire();
			}
			catch(InterruptedException e){
				
			}
			catch(Exception e){
				System.err.println("An unexpected exception has occurred in Agent...");
				e.printStackTrace();
			}
		}
		
		public void exceptionAcquire() throws InterruptedException{
			super.acquire();
		}
		
		public void release(){
			super.release();
		}
	}
	
	public static class BlockingData<T>{
		private final AgentSem blocker;
		private volatile T data;
		private boolean blocked;
		
		public BlockingData(){
			blocker=new AgentSem(0);
			data=null;
			blocked=true;
		}
		
		public T get(){
			if(blocked){
				blocker.acquire();
			}
			return data;
		}
		
		public synchronized void block(){
			blocked=true;
		}
		
		public synchronized void unblock(T d){
			data=d;
			if(blocked){
				blocked=false;
				blocker.release();
			}
		}
	}
}
