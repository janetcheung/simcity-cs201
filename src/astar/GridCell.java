package astar;

// Constant data structure that simply acts a package for r/c grid coordinate data
public class GridCell {
	public final int r, c;
	public GridCell(int r, int c) {
		this.r = r;
		this.c = c;
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GridCell)) {
            return false;
        }
        
        GridCell rhs = (GridCell) obj;
        return (this.r == rhs.r && this.c == rhs.c);
    }
	
	public GridCell add(GridCell rhs) {
		return new GridCell(this.r + rhs.r, this.c + rhs.c);
	}
	
	public GridCell minus(GridCell rhs) {
		return new GridCell(this.r - rhs.r, this.c - rhs.c);
	}
	
	public GridCell invertRow() {
		return new GridCell(-1*this.r, this.c);
	}
	public GridCell invertCol() {
		return new GridCell(this.r, -1*this.c);
	}
}