package city;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import agent.Agent;

public class Broadcaster extends Agent{
	private static final ThreadSafeTicks ticks=new ThreadSafeTicks();
	private static final MinuteMillis minute=new MinuteMillis();
	
	public static final long DAY_HOURS=24;
	public static final long WEEK_DAYS=7;
	public static final long MONTH_WEEKS=4;
	private static final long HOUR_MINUTES=40;
	
	private final Timer timer;
	private final List<BroadcastReceiver> highPriorityReceivers;
	private final List<BroadcastReceiver> normalReceivers;
	private long elapsed;
	private boolean update;
	
	public static long getTicks(){
		// Returns milliseconds since the beginning of the program
		return ticks.getTicks();
	}
	
	public static long getMinuteMillis(){
		return minute.get();
	}
	
	public static void setMinuteMilis(long m){
		minute.set(m);
	}
	
	public Broadcaster(String name){
		super(name);
		timer=new Timer();
		highPriorityReceivers=new ArrayList<BroadcastReceiver>();
		normalReceivers=new ArrayList<BroadcastReceiver>();
		elapsed=0;
		update=true;
	}
	
	protected boolean pickAndExecuteAction(){
		if(update){
			update=false;
			
			for(BroadcastReceiver r:highPriorityReceivers){
				r.msgUpdateTime(elapsed);
			}
			
			for(BroadcastReceiver r:normalReceivers){
				r.msgUpdateTime(elapsed);
			}
			
			elapsed++;
			
			timer.schedule(new TimerTask(){
				public void run(){
					Broadcaster.this.msgUpdateTime();
				}
			},HOUR_MINUTES*getMinuteMillis());
			
			return true;
		}
		return false;
	}
	
	private void msgUpdateTime(){
		enqueMutation(new Mutation(){
			public void apply(){
				update=true;
			}
		});
	}
	
	public void msgAddHighPriorityReceiver(final BroadcastReceiver r){
		enqueMutation(new Mutation(){
			public void apply(){
				normalReceivers.add(r);
			}
		});
	}
	
	public void msgRemoveHighPriorityReceiver(final BroadcastReceiver r){
		enqueMutation(new Mutation(){
			public void apply(){
				normalReceivers.remove(r);
			}
		});
	}
	
	public void msgAddNormalReceiver(final BroadcastReceiver r){
		enqueMutation(new Mutation(){
			public void apply(){
				normalReceivers.add(r);
			}
		});
	}
	
	public void msgRemoveNormalReceiver(final BroadcastReceiver r){
		enqueMutation(new Mutation(){
			public void apply(){
				normalReceivers.remove(r);
			}
		});
	}
	
	public void msgGetTime(final BlockingData<Date> date){
		enqueMutation(new Mutation(){
			public void apply(){
				date.unblock(new Date(elapsed,elapsed/DAY_HOURS,elapsed%DAY_HOURS));
			}
		});
	}
	
	public static class Date{
		public final long time;
		public final long day;
		public final long hour;
		
		public Date(long t,long d,long h){
			time=t;
			day=d;
			hour=h;
		}
	}
	
	private static class ThreadSafeTicks{
		public synchronized long getTicks(){
			return System.currentTimeMillis();
		}
	}
	
	private static class MinuteMillis{
		private long millis;
		
		public MinuteMillis(){
			millis=500;
		}
		
		public synchronized long get(){
			return millis;
		}
		
		public synchronized void set(long m){
			millis=m;
		}
	}
	
	public static interface BroadcastReceiver{
		public void msgUpdateTime(long time);
	}

	@Override
	protected void destructor() {
		// TODO Auto-generated method stub
		
	}
}
