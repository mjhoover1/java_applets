package vasco.regions;

import javax.swing.*; // import java.awt.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
 
public class InsertBlockCell implements HistoryElmInterface{
  public Rectangle block;
  public Vector jobs;
  public int [][]content;

  public InsertBlockCell(Rectangle block, int [][]content){
    this.block = block;
    this.content = new int[block.width][block.height];
    for(int x = 0; x < block.width; x++)
      for(int y = 0; y < block.height; y++)
	this.content[x][y] = content[x][y];
    jobs = null;
  } 

  public InsertBlockCell(Rectangle block, int color){
    this.block = block;
    content = new int[block.width][block.height];
    for(int x = 0; x < block.width; x++)
      for(int y = 0; y < block.height; y++)
	content[x][y] = color;
    jobs = null;
  } 

  public void build(RegionStructure pstruct){
    for(int x = 0; x < block.width; x++)
      for(int y = 0; y < block.height; y++){
	if (content[x][y] == 1)
	  pstruct.Insert(new Point(block.x + x, block.y + y));
	else
	  pstruct.Delete(new Point(block.x + x, block.y + y));
      }
  }

  public void save(Vector v){
    int size;
    Point p;
    
    for(int x = 0; x < block.width; x++){
      for(int y = 0; y < block.height; y++){
	p = new Point(block.x + x, block.y + y);
	if (content[x][y] == 1)
	  v.addElement(p);
	else{
	  size = v.size();
	  for(int j = 0; j < size;){
	    if (((Point)v.elementAt(j)).equals(p)){
	      v.removeElementAt(j);
	      size--;
	    }
	    else
	      j++;
	  } // for
	} // else
      } // for y
  } //for x
}


  public Vector switchGrid(int oldRes, int oldCellSize, 
			   int[][] oldGrid, Grid newGrid){
    Vector v = new Vector();
    int diff, x, y, z, xx, yy;
    boolean del = true;

    if (oldRes < newGrid.res){
      diff = (int)(Math.pow(2, newGrid.res - oldRes)); 
      v.addElement(new InsertBlockCell(new Rectangle(block.x * diff, 
					      block.y * diff,
					      diff * block.width,  
					      diff * block.height), 1));
      /* for deleted ones */
      for(x = block.x; x < block.x + block.width; x++)
	for(y = block.y; y < block.y + block.height; y++)
	  if (oldGrid[y][x] == 0)
	    v.addElement(new InsertBlockCell(new Rectangle(x * diff, 
							   y * diff, 
							   diff, 
		 					   diff), 0));
    } // if 
    else{
      diff = (int)(Math.pow(2, oldRes - newGrid.res)); 
      Point p = newGrid.getGridCoor(new Point(block.x * oldCellSize,
					      block.y * oldCellSize)); 
      int w = block.width / diff; if (w == 0) w = 1;
      int h = block.height / diff; if (h == 0) h = 1;
      v.addElement(new InsertBlockCell(new Rectangle(p.x,
						     p.y, 
						     w,
						     h), 1)); 
       
      /* for deleted ones */
      for (x = 0; x < w; x++){
	for (y = 0; y < h; y++){
	  for(xx = 0; xx < diff; xx++){
	    for(yy = 0; yy < diff; yy++){
	      if (oldGrid[block.y + y * diff + yy]
		  [block.x + x * diff + xx] == 1)
		break;
	    } // for yy
	    if (yy != diff)
	      break;
	  } // for xx

	  if (xx == diff){
	    p = newGrid.getGridCoor(new Point(
					(block.x + x * diff) * oldCellSize,
					(block.y + y * diff) * oldCellSize)); 
	    v.addElement(new DeleteBlockCell(new Rectangle(p.x, 
							   p.y, 
							   1, 
		 					   1)));
	  } // if
		
	} // for y
      } // for x
    }  // if

    return v;
  }

}

















