package vasco.regions;
import java.awt.*;
import java.util.*;
 
public class DeleteBlockCell implements HistoryElmInterface{
  Rectangle block;

  public DeleteBlockCell(Rectangle block){
    this.block = block;
  }

  public void build(RegionStructure pstruct){
    for(int x = block.x; x < block.x + block.width; x++)
      for(int y = block.y; y < block.y + block.height; y++)
	pstruct.Delete(new Point(x, y));
  }

  public void save(Vector v){
    int size;
    Point p;
    
    for(int x = 0; x < block.width; x++){
      for(int y = 0; y < block.height; y++){
	p = new Point(block.x + x, block.y + y);
	size = v.size();
	for(int j = 0; j < size;){
	  if (((Point)v.elementAt(j)).equals(p)){
	    v.removeElementAt(j);
	    size--;
	  }
	  else
	    j++;
	} // for
      } // for y
    } // for x
  }

  public Vector switchGrid(int oldRes, int oldCellSize, 
			   int[][] oldGrid, Grid newGrid){
    return new Vector();
  }
}
