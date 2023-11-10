package vasco.regions;

import vasco.common.*;
import vasco.drawable.*;
 
import java.awt.*;
import java.util.*;

public class ConvertVector extends SearchVector{
 
  protected Vector r;
  protected RegionStructure pstruct;
  protected Grid grid;
  protected int active;
  protected boolean partitioned;

  protected Vector permenant;

  public ConvertVector(){
    r = null;
    pstruct = null;
    grid = null;
    active = -1;
    partitioned = false;
    permenant = new Vector();
  }

  public ConvertVector(Grid g, RegionStructure s){
    super();
    r = new Vector();
    active = 0;
    grid = g; 
    pstruct = s;
    permenant = new Vector();
    constructRegions(6); 
    //activateRegion(active);
  }

  public int addPermenant(Object elm){
    permenant.addElement(elm);
    return permenant.size() - 1;
  }

  public Vector getPermenant(){
    return permenant;
  }

  public void constructRegion(){
    partitioned = false;

    if (r != null)
      r = new Vector();
    active = 0;
 
    r.addElement(new Rectangle(0, 0, 512, 512));
  }

  public void constructRegion(int start, int size){
  }
 
  public void constructRegions(int res){
    int add = (int)Math.pow(2, res);
    int count = 0;
    int x = 0;
 
    partitioned = true;

    /* clear the old partition */
    if (r != null)
      r = new Vector();
    active = 0;

    //r.addElement(new Rectangle(0, 0, 512, 512));
    while(x < 512){ 
      x = count * add; 
      r.addElement(new Rectangle(0, x, 512, add));
      count ++;
    } 
 
    activateRegion(active);
  }

  protected void activateRegion(){
  }

  protected void activateRegion(int i){
    if ((i < 0) || (i >= r.size()))
      return;
 
    active = i; 
    sv = new Vector(); 
    permenant = new Vector();
    pstruct.convert(this);
  }
 
  public boolean inActiveRegion(int x){
    return true;
  }

  public boolean inActiveRegion(int x, int y){
    Rectangle rec = (Rectangle)r.elementAt(active);
  
    if ((x >= rec.x) && (x < rec.x + rec.width) &&
	(y >= rec.y) && (y < rec.y + rec.height))
      return true;

    return false;
  }

  int findRegion(int x, int y){
    Rectangle rec;  
    /* find the region which contains the point */
    for (int i = 0; i < r.size(); i++){
      rec = (Rectangle)r.elementAt(i);
      
      if ((x >= rec.x) && (x < rec.x + rec.width) &&
	  (y >= rec.y) && (y < rec.y + rec.height))
	return i;
    }
  
    return -1;
  }
 
 
  public AnimElement elementAt(int i){
    AnimElement anim = (AnimElement)sv.elementAt(i);

    if (!partitioned)
      return anim;
 
    ColorGrids g = (ColorGrids)anim.ge; 
    Point p = grid.grdToScr(g.getPoint());
    if (inActiveRegion(p.x, p.y)){ 
      return anim;
    }   
    int region = findRegion(p.x, p.y);
    activateRegion(region);
    anim = (AnimElement)sv.elementAt(i);
 
    return anim;
  }
 

}

