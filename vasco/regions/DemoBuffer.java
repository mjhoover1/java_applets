package vasco.regions;

import vasco.common.*;
 
import java.awt.*;
import java.util.*;

public class DemoBuffer extends ConvertVector{

  int start, size;

  public DemoBuffer(Grid grid, RegionStructure pstruct){
    super();
    this.grid = grid;
    this.pstruct = pstruct;
    active = 0;
    r = null;
    start = 0;
    size = 0;
  }

  public void constructRegion(){
  }

  public void constructRegions(int res){
  }

  public void constructRegion(int start, int size){
    this.start = start;
    this.size = size;
  }

  protected void activateRegion(){
    pstruct.MessageStart();
    sv = new Vector();
    permenant = new Vector();
    pstruct.convert(this);
    pstruct.MessageEnd();
  }

  protected void activateRegion(int start){
    this.start = start;
    activateRegion();
    
  }

  public boolean inActiveRegion(int x){
    return true;
  }

  public boolean inActiveRegion(int x, int y){
    return true;
  }

  public void addElement(AnimElement e){
    if (sv.size() >= start && sv.size() <= (start + size))
      sv.addElement(e);
    else
      sv.addElement(null);
  }

  public AnimElement elementAt(int i){
    AnimElement anim = (AnimElement)sv.elementAt(i);

    if (i < start || i > (start + size))
      activateRegion(i);
    anim = (AnimElement)sv.elementAt(i);

    return anim;
  } 
}

 
