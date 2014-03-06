package city.gui;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public abstract class BuildingGui implements ActionListener{
	public static final int FRAME_DELAY=AnimationPanel.FRAME_DELAY;
	
	private final List<Gui> guis;
	private final Timer timer;
	private boolean paused;
	public BuildingGui(){
		guis=new ArrayList<Gui>();
		timer=new Timer(FRAME_DELAY,this);
		timer.start();
		paused = false;
	}

	public final synchronized void actionPerformed(ActionEvent e){
		if (!paused) {
			synchronized(guis){
				for(Gui g:guis){
					g.updatePosition();
				}
			}
		}
	}
	
	public final void addGui(Gui g){
		synchronized(guis){
			guis.add(g);
		}
	}
	
	public final void removeGui(Gui g){
		synchronized(guis){
			guis.remove(g);
		}
	}
	
	protected final void drawGuis(Graphics2D g2){
		synchronized(guis){
			for(Gui g:guis){
				g.draw(g2);
			}
		}
	}
	
	public abstract void draw(Graphics2D g2);
	
    public void setTimerDelay(int s){
    	if(s>=1 && s<=5){
    		timer.setDelay(FRAME_DELAY-3*(s-1));
    	}
    }
    
    public synchronized void pause() {
    	paused = true;
    }
    public synchronized void resume() {
    	paused = false;
    }
}
