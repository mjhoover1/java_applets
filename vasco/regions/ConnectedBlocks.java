package vasco.regions;
import javax.swing.*; // import java.awt.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

public class ConnectedBlocks{
  public static final int NORTH = 0;
  public static final int EAST = 1;
  public static final int SOUTH = 2;
  public static final int WEST = 3;

  public static final int FINDALL_MODE = 0;
  public static final int VERIFY_MODE = 1;

  public ConnectedBlocks(){
  }

  public static CBlock inBlock(Vector v, Grid g, int x, int y){
    Rectangle r = g.getScreenCoor(new Point(x, y));

    for(int i = 0; i < v.size(); i++)
      if (((CBlock)v.elementAt(i)).p.contains(r.x + 1, r.y + 1))
	return (CBlock)v.elementAt(i);

    return null;
  }

  public static void addBlock(Vector v, Grid g, int x, int y){
    CBlock cb = new CBlock();
    Rectangle r;
    int dir = EAST;
    int cX = x, cY = y;

    r = g.getScreenCoor(new Point(cX, cY));
    cb.p.addPoint(r.x, r.y);
    cb.v.addElement(new Point(x, y));

    while(!(cX == x && cY == y && dir == NORTH)){
      r = g.getScreenCoor(new Point(cX, cY));
      switch(dir){
      case NORTH:
	cb.p.addPoint(r.x, r.y);
	if (g.getColor(cX, cY - 1) == 1){
	  if (g.getColor(cX - 1, cY - 1) == 1){
	    cX = cX - 1;
	    cY = cY - 1;
	    dir = WEST;
	  }
	  else{
	    cY = cY - 1;
	    dir = NORTH;
	  }
	}
	else{
	  dir = EAST;
	}
	break;
      case EAST:
	cb.p.addPoint(r.x + r.width, r.y);
	if (g.getColor(cX + 1, cY) == 1){
	  if (g.getColor(cX + 1, cY - 1) == 1){
	    cX = cX + 1;
	    cY = cY - 1;
	    dir = NORTH;
	  }
	  else{
	    cX = cX + 1;
	    dir = EAST;
	  }
	}
	else{
	  dir = SOUTH;
	}
	break;
      case WEST:
	cb.p.addPoint(r.x, r.y + r.height);
	if (g.getColor(cX - 1, cY) == 1){
	  if (g.getColor(cX - 1, cY + 1) == 1){
	    cX = cX - 1;
	    cY = cY + 1;
	    dir = SOUTH;
	  }
	  else{
	    cX = cX - 1;
	    dir = WEST;
	  }
	}
	else{
	  dir = NORTH;
	}
	break;
      case SOUTH:
	cb.p.addPoint(r.x + r.width, r.y + r.height);
	if (g.getColor(cX, cY + 1) == 1){
	  if (g.getColor(cX + 1, cY + 1) == 1){
	    cX = cX + 1;
	    cY = cY + 1;
	    dir = EAST;
	  }
	  else{
	    cY = cY + 1;
	    dir = SOUTH;
	  }

	}
	else{
	  dir = WEST;
	}
			       
	break;
      } 

    }

    r = g.getScreenCoor(new Point(x, y));
    cb.p.addPoint(r.x, r.y);
    v.addElement(cb);
  }

  public static Vector find(Grid g){
    return find(g, FINDALL_MODE);
  }

  public static Vector find(Grid g, int mode){
    int x, y;
    Vector v = new Vector();
    CBlock cb;

    for(x = 0; x < g.cellCount; x++){
      for(y = 0; y < g.cellCount; y++){
	if ((cb = inBlock(v, g, x, y)) != null){
	  if (g.getColor(x, y) == 1){
	    if (mode != VERIFY_MODE) 
	      cb.v.addElement(new Point(x, y));
	  }
	  else{
	    cb.valid = false;
	    if (mode == VERIFY_MODE) return null; 
	  }
	}
	else{
	  if (g.getColor(x, y) == 1){
	    if (mode == VERIFY_MODE && v.size() > 0) return null;
	    addBlock(v, g, x, y);
	  }	  
	} // if cb
      } // for y
    } // for x

    return v;
  }
}
