package city.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import city.Person;
import city.bus.Bus;

public class BusGui implements Gui{
	private static final int GRID_SQUARE_SIZE=8;
	private static final int SPEED=4;
	private static final int BUS_LENGTH=14;
	private static final int BUS_WIDTH=8;

	private final Bus agent; 
	
	private int r,c;
	private int rDest,cDest;
	private boolean moving;
	private int cRow,cCol;
	private int pRow,pCol;
	
	public BusGui(Bus b){
		moving=false;
		agent=b;
		r=c=rDest=cDest=cRow=cCol=pRow=pCol=0;
	}

	public synchronized void updatePosition() {
		if(r<rDest){
			r+=SPEED;
		}
		else if(r>rDest){
			r-=SPEED;
		}

		if(c<cDest){
			c+=SPEED;
		}
		else if(c>cDest){
			c-=SPEED;	
		}

		if(r==rDest && c==cDest){
			if(moving){
				moving=false;
				agent.msgFinishedStep();
			}
		}
	}

	public synchronized void setDestination(int row, int col){
		moving=true;
		rDest=row*GRID_SQUARE_SIZE;
		cDest=col*GRID_SQUARE_SIZE;
		pRow=cRow;
		cRow=row;
		pCol=cCol;
		cCol=col;
	}
	
	public synchronized void setPosition(int row,int col){
		r=rDest=row*GRID_SQUARE_SIZE;
		c=cDest=col*GRID_SQUARE_SIZE;
		pRow=cRow;
		cRow=row;
		pCol=cCol;
		cCol=col;
	}
	
	public synchronized void draw(Graphics2D g2){
		g2.setColor(Color.YELLOW);
		if(cRow>pRow){
			g2.fillRect(c+4,r+1,BUS_WIDTH,BUS_LENGTH);
			g2.setColor(Color.CYAN);
			g2.fillRect(c+5,r+10,6,4);
		}
		else if(cRow<pRow){
			g2.fillRect(c+4,r+1,BUS_WIDTH,BUS_LENGTH);
			g2.setColor(Color.CYAN);
			g2.fillRect(c+5,r+2,6,4);
		}
		else if(cCol>pCol){
			g2.fillRect(c+1,r+4,BUS_LENGTH,BUS_WIDTH);
			g2.setColor(Color.CYAN);
			g2.fillRect(c+10,r+5,4,6);
		}
		else if(cCol<pCol){
			g2.fillRect(c+1,r+4,BUS_LENGTH,BUS_WIDTH);
			g2.setColor(Color.CYAN);
			g2.fillRect(c+2,r+5,4,6);
		}
	}
}
