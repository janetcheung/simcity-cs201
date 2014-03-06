package city.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import astar.Moveable;


public class PersonGui implements Gui{
	public static final int GRID_SQUARE_SIZE=8;
	private static final int SIZE=4;
	private static final int PERSON_SPEED=1;
	private static final int CAR_SPEED=4;
	private static final int CAR_LENGTH=10;
	private static final int CAR_WIDTH=6;
	private static final Color SELECTED_COLOR=Color.MAGENTA;
	private static final Color PERSON_COLOR=Color.CYAN;
	private static final Color CAR_COLOR=Color.GRAY;

	private final Moveable agent;
	private int r,c;
	private int rDest,cDest;
	private Color color;
	private boolean moving;
	private boolean hasCar;
	private int speed;
	private int cRow,cCol;
	private int pRow,pCol;
	

	public PersonGui(Moveable p,boolean car){
		agent=p;
		rDest=r;
		cDest=c;
		color=car?CAR_COLOR:PERSON_COLOR;
		moving=false;
		hasCar=car;
		speed=car?CAR_SPEED:PERSON_SPEED;
		r=c=rDest=cDest=cRow=cCol=pRow=pCol=0;
	}
	
	public synchronized void updatePosition(){
		if(r<rDest)
			r+=speed;
		else if(r>rDest)
			r-=speed;

		if(c<cDest)
			c+=speed;
		else if(c>cDest)
			c-=speed;		
		
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
	
	public synchronized void toggleSelected(){
		if (color.equals(SELECTED_COLOR)){
			color=hasCar?CAR_COLOR:PERSON_COLOR;
		}
		else{
			color=SELECTED_COLOR;
		}
	}
	
	public synchronized void draw(Graphics2D g2){
		g2.setColor(color);
		if(hasCar){
			if(cRow>pRow){
				g2.fillRect(c+5,r+3,CAR_WIDTH,CAR_LENGTH);
				g2.setColor(Color.CYAN);
				g2.fillRect(c+6,r+8,4,4);
			}
			else if(cRow<pRow){
				g2.fillRect(c+5,r+3,CAR_WIDTH,CAR_LENGTH);
				g2.setColor(Color.CYAN);
				g2.fillRect(c+6,r+4,4,4);
			}
			else if(cCol>pCol){
				g2.fillRect(c+3,r+5,CAR_LENGTH,CAR_WIDTH);
				g2.setColor(Color.CYAN);
				g2.fillRect(c+8,r+6,4,4);
			}
			else if(cCol<pCol){
				g2.fillRect(c+3,r+5,CAR_LENGTH,CAR_WIDTH);
				g2.setColor(Color.CYAN);
				g2.fillRect(c+4,r+6,4,4);
			}
		}
		else{
			int off=(GRID_SQUARE_SIZE-SIZE)/2;
			g2.fillRect(c+off,r+off,SIZE,SIZE);
		}
	}
	
	public synchronized void setCar(boolean car){
		hasCar=car;
		speed=car?CAR_SPEED:PERSON_SPEED;
	}
}
