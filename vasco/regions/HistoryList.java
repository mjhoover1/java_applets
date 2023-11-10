package vasco.regions;
import vasco.common.*;
import java.util.*;

public class HistoryList{
  Vector history;

  public HistoryList(Vector history){
    this.history = history;
  }

  public Vector getHistory(){
    return history;
  }

  public void setHistory(Vector history){
    this.history = history;
  }

  public void add(HistoryElmInterface elm){
    history.addElement(elm);
  }
 
  public Vector switchGrid(int oldRes, int oldCellSize, 
			   int[][] oldGrid, Grid newGrid){
    Vector v;
    Vector old = history;
    HistoryElmInterface elm;
    int x, y;

    history = new Vector();
    for(x = 0; x < old.size(); x++){
      v = ((HistoryElmInterface)old.elementAt(x)).
	  switchGrid(oldRes, oldCellSize, oldGrid, newGrid);
      for(y = 0; y < v.size(); y++) 
	add((HistoryElmInterface)v.elementAt(y));
    }
 
    return history;
  }

  public HistoryElmInterface elementAt(int index){
    return (HistoryElmInterface)history.elementAt(index);
  }

  public int size(){
    return history.size();
  }

  public void undo(){
    if (history.size() > 0)
      history.removeElementAt(history.size() - 1);
  }

}
