package city.bank.gui;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import astar.Moveable;


import city.gui.CityGui;
import city.gui.Gui;

public class TellerGui implements Gui{
	private static final int GRID_SQUARE_SIZE=BankGui.GRID_SQUARE_SIZE;
	private static final int SIZE=16;
	private static final int SPEED=4;

	private final Moveable agent;
	
	private int r,c;
	private int rDest,cDest;
	private boolean moving;
	
	public TellerGui(Moveable p){
		agent=p;
		r=0;
		c=0;
		rDest=r;
		cDest=c;
		moving=false;
	}
	
	public synchronized void updatePosition() {
		if(r<rDest)
			r+=SPEED;
		else if(r>rDest)
			r-=SPEED;

		if(c<cDest)
			c+=SPEED;
		else if(c>cDest)
			c-=SPEED;		
		
		if(r==rDest && c==cDest){
			if(moving){
				moving=false;
				agent.msgFinishedStep();
			}
		}	
	}
	
	public synchronized void setDestination(int row,int col){
		moving=true;
		rDest=row*GRID_SQUARE_SIZE;
		cDest=col*GRID_SQUARE_SIZE;		
	}
	
	public synchronized void setPosition(int x,int y){
		r=x*GRID_SQUARE_SIZE;
		c=y*GRID_SQUARE_SIZE;
	}
	
	public synchronized void draw(Graphics2D g2){
		g2.setColor(Color.MAGENTA);
		int off=(GRID_SQUARE_SIZE-SIZE)/2;
		g2.fillRect(c+off,r+off,16,16);
	}
}
