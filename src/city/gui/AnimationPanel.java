package city.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class AnimationPanel extends JPanel implements ActionListener{
	public static final int FRAME_DELAY=15;

	private static final int WINDOWX=800;
	private static final int WINDOWY=800;

	private final ImageIcon background=new ImageIcon("img/simplebackground.png");
	private final ImageIcon backgroundBuildings=new ImageIcon("img/simplebuildings.png");

	private final List<Gui> guis;
	private final Timer positionTimer;
	private final Timer displayTimer;

	private Rectangle2D.Double selection;
	private Color backgroundColor;
	private float alpha;

	public AnimationPanel (){
		setSize(WINDOWX,WINDOWY);
		setVisible(true);

		guis=new ArrayList<Gui>();

		positionTimer=new Timer(FRAME_DELAY,this);
		positionTimer.start();

		displayTimer=new Timer(FRAME_DELAY,this);
		displayTimer.start();

		alpha=0.6f;
		backgroundColor=new Color(0f,0f,0f,alpha);
		selection=null;
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource()==positionTimer){
			synchronized(guis){
				for(Gui gui:guis){
					gui.updatePosition();
				}
			}
		}
		if(e.getSource()==displayTimer){
			repaint();
		}
	}

	public void paintComponent(Graphics g){
		Graphics2D g2=(Graphics2D)g;

		background.paintIcon(null,g2,0,0);
		g2.setColor(backgroundColor);
		g2.fillRect(0,0,WINDOWX,WINDOWY);

		synchronized(guis){
			for(Gui gui: guis){
				gui.draw(g2);
			}
		}

		backgroundBuildings.paintIcon(null,g2,0,0);

		if(selection!=null){
			g2.setColor(Color.GREEN);
			g2.drawRect((int)selection.x,(int)selection.y,(int)selection.width,(int)selection.height);
		}
	}

	public void addGui(Gui g){
		synchronized(guis){
			guis.add(g);
		}
	}

	public void setTimerDelay(int s){
		if(s>=1 && s<=5){
			positionTimer.setDelay(FRAME_DELAY-3*(s-1));
		}
	}

	public void setSelection(Rectangle2D.Double s){
		selection=s;
	}

	public void setBackgroundColor(long n, long m){
		if (n==0)
			alpha=0.6f;
		if (n==6)
			alpha=0.3f;
		if (n==12)
			alpha=0.0f;
		if (n==18)
			alpha=0.3f;

		if (m==5||m==6){
			backgroundColor=new Color(1.0f,143.0f/255.0f,1.0f,alpha);
		}
		else{
		backgroundColor=new Color(0f,0f,0f,alpha);
		}
	}
}
