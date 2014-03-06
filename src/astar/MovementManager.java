package astar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import trace.AlertLog;
import trace.AlertTag;
import agent.Agent;

public class MovementManager extends Agent{
	private static final int GRID_SQUARE_MAX=3;
	private static final int BOTTOMLESS_PIT=0;
	
	private final int[][] staticGrid;
	private final int[][] dirGrid;
	private final int[][] costGrid;
	private final Unit[][] dynamicGrid;
	private final int rows,cols;
	private final List<Unit> units;
	
	public MovementManager(String name,int r,int c){
		super(name);
		staticGrid=new int[r][c];
		dirGrid=new int[r][c];
		costGrid=new int[r][c];
		dynamicGrid=new Unit[r][c];
		rows=r;
		cols=c;
		units=new ArrayList<Unit>();
		for(int i=0;i<r;i++){
			for(int j=0;j<c;j++){
				staticGrid[i][j]=0;
				dirGrid[i][j]=0;
				costGrid[i][j]=0;
				dynamicGrid[i][j]=null;
			}
		}
	}
	
	public MovementManager(String name,int r,int c,String statName){
		super(name);
		staticGrid=new int[r][c];
		dirGrid=new int[r][c];
		costGrid=new int[r][c];
		dynamicGrid=new Unit[r][c];
		rows=r;
		cols=c;
		units=new ArrayList<Unit>();
		FileInputStream inStat=null;
		
		try{
			inStat=new FileInputStream(statName);
		}
		catch(IOException e){
			AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Could not open "+statName+"!");
		}
		
		Scanner stat=new Scanner(inStat);
		
		for(int i=0;i<r;i++){
			for(int j=0;j<c;j++){
				staticGrid[i][j]=stat.nextInt();
				dirGrid[i][j]=0;
				costGrid[i][j]=0;
				dynamicGrid[i][j]=null;
			}
		}
		
		
		stat.close();
		
		try{
			if(inStat!=null){
				inStat.close();
			}
		}
		catch(IOException e){
			AlertLog.getInstance().logInfo(AlertTag.ASTAR, getAgentName(), "Could not close "+statName+"!");
		}
	}
	
	public MovementManager(String name,int r,int c,String statName,String dirName,String costName){
		super(name);
		staticGrid=new int[r][c];
		dirGrid=new int[r][c];
		costGrid=new int[r][c];
		dynamicGrid=new Unit[r][c];
		rows=r;
		cols=c;
		units=new ArrayList<Unit>();
		FileInputStream inStat=null;
		FileInputStream inDir=null;
		FileInputStream inCost=null;
		
		try{
			inStat=new FileInputStream(statName);
			inDir=new FileInputStream(dirName);
			inCost=new FileInputStream(costName);
		}
		catch(IOException e){
			AlertLog.getInstance().logError(AlertTag.ASTAR, getAgentName(), "Could not open file(s)!");
		}
		
		Scanner stat=new Scanner(inStat);
		Scanner dir=new Scanner(inDir);
		Scanner cost=new Scanner(inCost);
		
		for(int i=0;i<r;i++){
			for(int j=0;j<c;j++){
				staticGrid[i][j]=stat.nextInt();
				dirGrid[i][j]=dir.nextInt();
				costGrid[i][j]=cost.nextInt();
				dynamicGrid[i][j]=null;
			}
		}
		
		stat.close();
		dir.close();
		cost.close();
		
		try{
			if(inStat!=null){
				inStat.close();
			}
			if(inDir!=null){
				inDir.close();
			}
			if(inCost!=null){
				inCost.close();
			}
		}
		catch(IOException e){
			AlertLog.getInstance().logError(AlertTag.ASTAR, getAgentName(), "Could not close files(s)!");
		}
	}
	
	protected boolean pickAndExecuteAction(){
		for(Unit u:units){
			if(u.path.isEmpty()){
				actComputePath(u);
			}
		}
		
		boolean collisions=false;
		
		for(Unit u:units){
			if(u.ready){
				u.ready=false;
				
				if(u.pathIndex<u.path.size()){
					int nextRow=u.path.get(u.pathIndex).row;
					int nextCol=u.path.get(u.pathIndex).col;
					if(checkSurrounding(u,nextRow,nextCol) || checkSurrounding(u,u.pos.row,nextCol) || checkSurrounding(u,nextRow,u.pos.col)){
						u.path.clear();
						collisions=true;
						continue;
					}
					u.unfill();
					u.pos.row=nextRow;
					u.pos.col=nextCol;
					u.fill();
					u.pathIndex++;
					if(u.pathIndex==u.path.size()){
						u.unit.msgLastStep(u.pos.row,u.pos.col);
						continue;
					}
					u.unit.msgMoveStep(u.pos.row,u.pos.col);
				}
			}
		}
		
		return collisions;
	}
	
	public void msgAddUnit(final Moveable m,final int maxNum,final int size,final int r,final int c){
		enqueMutation(new Mutation(){
			public void apply(){
				for(Unit u:units){
					if(u.unit==m){
						//System.err.println(getAgentName()+": Warning: attempted to add already-added unit");
						AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to add already-added unit");
						return;
					}
				}
				if(r<0 || r>=rows || c<0 || c>=cols){
					//System.err.println(getAgentName()+": Warning: attempted to add unit to invalid grid square");
					AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to add unit at invalid grid square");
					return;
				}
				if(maxNum>=GRID_SQUARE_MAX || maxNum<=BOTTOMLESS_PIT){
					//System.err.println(getAgentName()+": Warning: attempted to add unit with invalid grid square max");
					AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to add unit with invalid grid square max");
					return;
				}
				if(staticGrid[r][c]>maxNum){
					//System.err.println(getAgentName()+": Warning: attempted to add unit to statically blocked square");
					AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to add unit to statically blocked square");
					return;
				}
				for(int i=0;i<size;i++){
					for(int j=0;j<size;j++){
						if(dynamicGrid[r+i][c+j]!=null){
							//System.err.println(getAgentName()+": Warning: attempted to add unit to already occupied square");
							AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to add unit to already occupied square");
							return;
						}
					}
				}
				//System.err.println(getAgentName()+": added "+m.getAgentName()+" "+maxNum+" "+size+" "+r+" "+c);
				Unit u=new Unit(m,maxNum,size,r,c);
				units.add(u);
				u.fill();
			}
		});
	}
	
	public void msgRemoveUnit(final Moveable m){
		enqueMutation(new Mutation(){
			public void apply(){
				for(int i=0;i<units.size();i++){
					Unit u=units.get(i);
					if(u.unit==m){
						u.unfill();
						units.remove(i);
						return;
					}
				}
				AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to remove non-existing unit");
			}
		});
	}
	
	public void msgMoveMeTo(final Moveable m,final int r,final int c){
		enqueMutation(new Mutation(){
			public void apply(){
				Unit u=null;
				for(Unit x:units){
					if(x.unit==m){
						u=x;
						break;
					}
				}
				if(u==null){
					AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to move non-existing unit");
					return;
				}
				if(r<0 || r>=rows || c<0 || c>=cols){
					//System.err.println(getAgentName()+": Warning: attempted to move unit to invalid grid location");
					AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to move unit to invalid grid location");
					return;
				}
				if (u.pos.row == r && u.pos.col == c) {
					u.unit.msgAlreadyAtDestination();
					return;
				}
				u.dest.row=r;
				u.dest.col=c;
				u.path.clear();
				u.ready=false;
			}
		});
	}
	
	public void msgReadyForNextMove(final Moveable m){
		enqueMutation(new Mutation(){
			public void apply(){
				for(Unit u:units){
					if(u.unit==m){
						u.ready=true;
						return;
					}
				}
				AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted ready non-existing unit");
			}
		});
	}
	
	public void msgGetMoveableAt(final BlockingData result,final int r,final int c){
		enqueMutation(new Mutation(){
			public void apply(){
				if(r<0 || r>=rows || c<0 || c>=cols){
					result.unblock(null);
					AlertLog.getInstance().logMessage(AlertTag.ASTAR, getAgentName(), "Warning: attempted to get unit at invalid grid location");
					return;
				}
				if(dynamicGrid[r][c]!=null){
					result.unblock(dynamicGrid[r][c].unit);
					return;
				}
				result.unblock(null);
			}
		});
	}
	
	private void actComputePath(final Unit u){
		final boolean[][] visited=new boolean[rows][cols];
		final Position[][] previous=new Position[rows][cols];
		
		for(int i=0;i<rows;i++){
			for(int j=0;j<cols;j++){
				visited[i][j]=false;
				previous[i][j]=null;
			}
		}
		
		final List<Cost> priority=new ArrayList<Cost>();
				
		priority.add(new Cost(0.0,u.pos.row,u.pos.col));
		Cost current=null;
		Cost[] moves=new Cost[8];
		boolean success=false;
		
		while(!priority.isEmpty()){
			double minCost=priority.get(0).cost;
			int minIndex=0;
			for(int i=1;i<priority.size();i++){
				if(priority.get(i).cost<minCost){
					minCost=priority.get(i).cost;
					minIndex=i;
				}
			}
			
			current=priority.get(minIndex);
			priority.remove(minIndex);
			visited[current.row][current.col]=true;
			
			if(current.row==u.dest.row && current.col==u.dest.col){
				success=true;
				break;
			}
			
			moves[0]=current.move(u,-1,0);
			moves[1]=current.move(u,1,0);
			moves[2]=current.move(u,0,-1);
			moves[3]=current.move(u,0,1);
			moves[4]=current.move(u,-1,-1);
			moves[5]=current.move(u,-1,1);
			moves[6]=current.move(u,1,-1);
			moves[7]=current.move(u,1,1);
			
			for(int i=0;i<8;i++){
				if(moves[i]!=null && (checkSurrounding(u,moves[i].row,moves[i].col) ||
						             checkSurrounding(u,current.row,moves[i].col) ||
						             checkSurrounding(u,moves[i].row,current.col))){
					moves[i]=null;
				}
			}
			
			for(int i=0;i<8;i++){
				if(moves[i]!=null && !visited[moves[i].row][moves[i].col]){
					boolean contains=false;
					for(int k=0;k<priority.size();k++){
						if(priority.get(k).row==moves[i].row && priority.get(k).col==moves[i].col){
							contains=true;
							if(moves[i].cost<priority.get(k).cost){
								priority.set(k,moves[i]);
								previous[moves[i].row][moves[i].col]=new Position(current.row,current.col);
							}
							break;
						}
					}
					if(!contains){
						priority.add(moves[i]);
						previous[moves[i].row][moves[i].col]=new Position(current.row,current.col);
					}
				}
			}
		}
		
		u.pathIndex=1;
		
		if(!success){
			u.path.add(new Position(current.row,current.col));
			u.unit.msgNoRouteToDestination();
			return;
		}
		
		Position c=new Position(current.row,current.col);
		while(c!=null){
			u.path.add(0,c);
			c=previous[c.row][c.col];
		}
		
		u.ready=true;
	}
	
	private boolean checkSurrounding(final Unit u,final int r,final int c){
		if(r>=u.pos.row-1 && r<=u.pos.row+1 && c>=u.pos.col-1 && c<=u.pos.col+1){
			for(int i=0;i<u.size;i++){
				for(int j=0;j<u.size;j++){
					if(dynamicGrid[r+i][j+c]!=null && dynamicGrid[r+i][j+c]!=u){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static class Position{
		public int row,col;
		
		public Position(int r,int c){
			row=r;
			col=c;
		}
	}
	
	private class Unit{
		public final Moveable unit;
		public final Position pos;
		public final Position dest;
		public final List<Position> path;
		public final int max;
		public final int size;
		
		public int pathIndex;
		private boolean ready;
		
		public Unit(Moveable m,int n,int s,int r,int c){
			unit=m;
			pos=new Position(r,c);
			dest=new Position(r,c);
			path=new ArrayList<Position>();
			max=n;
			size=s;
			pathIndex=0;
			ready=false;
		}
		
		public void fill(){
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++){
					if(staticGrid[pos.row+i][pos.col+j]!=BOTTOMLESS_PIT){
						dynamicGrid[pos.row+i][pos.col+j]=this;
					}
				}
			}
		}
		
		public void unfill(){
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++){
					dynamicGrid[pos.row+i][pos.col+j]=null;
				}
			}
		}
	}
	
	private class Cost{
		public final double cost;
		public final int row,col;
		
		public Cost(double cst,int r,int c){
			cost=cst;
			row=r;
			col=c;
		}
		
		public Cost move(final Unit u,final int dr,final int dc){
			final int newRow=row+dr;
			final int newCol=col+dc;
			if(newRow<0 || newRow+u.size-1>=rows || newCol<0 || newCol+u.size-1>=cols){
				return null;
			}
			int checkDir=0;
			for(int i=0;i<u.size;i++){
				for(int j=0;j<u.size;j++){
					int dir=dirGrid[newRow+i][newCol+j];
					if(checkDir==0 && dir!=0){
						checkDir=dir;
					}
					else if(dir!=checkDir && dir!=0){
						return null;
					}
				}
			}
			final double moveCost=Math.sqrt((double)(dr*dr+dc*dc))+(double)(costGrid[newRow][newCol]);
			int dir=0;
			for(int i=0;i<u.size;i++){
				for(int j=0;j<u.size;j++){
					dir=dirGrid[row+i][col+j];
					if(dir!=0){
						break;
					}
				}
			}
			for(int i=0;i<u.size;i++){
				for(int j=0;j<u.size;j++){
					if(staticGrid[newRow+i][newCol+j]>u.max || staticGrid[row+i][newCol+j]>u.max || staticGrid[newRow+i][col+j]>u.max){
						return null;
					}
				}
			}
			final double dRow=(double)(u.dest.row-newRow);
			final double dCol=(double)(u.dest.col-newCol);
			final double h=Math.sqrt(dRow*dRow+dCol*dCol)+(double)costGrid[u.dest.row][u.dest.col];
			switch(dir){
			case 0: return new Cost(cost+moveCost+h,newRow,newCol);
			case 1:
				if(dr==-1 && dc==0){
					return new Cost(cost+moveCost+h,newRow,newCol);
				}
			break;
			case 2:
				if(dr==1 && dc==0){
					return new Cost(cost+moveCost+h,newRow,newCol);
				}
			break;
			case 3:
				if(dr==0 && dc==1){
					return new Cost(cost+moveCost+h,newRow,newCol);
				}
			break;
			case 4:
				if(dr==0 && dc==-1){
					return new Cost(cost+moveCost+h,newRow,newCol);
				}
			break;
			}
			return null;
		}
	}

	protected void destructor(){
		// TODO Auto-generated method stub
	}
}
