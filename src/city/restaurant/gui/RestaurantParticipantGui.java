package city.restaurant.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import city.gui.Gui;
import city.restaurant.RestaurantParticipantPuppet;
import astar.GridCell;


public abstract class RestaurantParticipantGui implements Gui{
	protected static final int GRID_SQUARE_SIZE=RestaurantGui.GRID_SQUARE_SIZE;
	protected static final int SIZE=16;
	private static final int SPEED=4;
	
	private final RestaurantParticipantPuppet agent;
		
	// r, c, rDest, and cDest are in pixel units
	protected int r, c;
	private int rDest, cDest;
	private int speed;
	private boolean moving;
	
	// CONSTRUCTOR ///////////////////////////////////////////////////////////////////////////
	
	public RestaurantParticipantGui(RestaurantParticipantPuppet p, GridCell startLoc){
		agent=p;
		r=rDest=startLoc.r*GRID_SQUARE_SIZE;
		c=cDest=startLoc.c*GRID_SQUARE_SIZE;
		speed=4;
		moving=false;
	}
	
	public synchronized void updatePosition(){
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

	public abstract void draw(Graphics2D g);

	public synchronized void setDestination(int row, int col){
		moving=true;
		rDest = row*GRID_SQUARE_SIZE;
		cDest = col*GRID_SQUARE_SIZE;
	}
	
	public synchronized void setPosition(int row,int col){
		this.r = row*GRID_SQUARE_SIZE;
		this.c = col*GRID_SQUARE_SIZE;
	}
}
