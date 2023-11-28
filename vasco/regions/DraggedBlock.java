package vasco.regions;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.*; // import java.awt.*;

public class DraggedBlock{
  public static final int QUAD_NODE = 0;
  public static final int SELECTED_AREA = 1;

  public static final int COPY_MODE = 0;
  public static final int UNION_MODE = 1;
  public static final int MOVE_MODE = 2;
 
  int [][]block;
  int [][]backBlock;
  int mode;
  int relX, relY;
  boolean firstDragged;
  Point orgP;

  Rectangle rec;
  Point pos;

  public DraggedBlock(Grid grid, Rectangle rec, Point pos, Point mPos, int mode){
 
    this.block = grid.get(rec);
    this.mode = mode;
    this.orgP = new Point(rec.x, rec.y);
    this.firstDragged = false;
    relX = pos.x - mPos.x;
    relY = pos.y - mPos.y;
    if (mode == COPY_MODE)
      this.backBlock = grid.get(rec);
    else{
      this.backBlock = new int[rec.width][rec.height];
      for(int x = 0; x < rec.width; x++)
	for(int y = 0; y < rec.height; y++)
	  this.backBlock[x][y] = 0;    
    }
    this.rec = new Rectangle(0, 0, rec.width, rec.height);
    this.pos = pos;

  }

  public Point getPos(){
    return pos;
  }

  public int[][] getBlock(){
    return block;
  }

  public Rectangle getGrdRect(Point p){
    return new Rectangle(p.x, p.y, rec.width, rec.height);
  }


  private void insertBlock(Point pos, Rectangle rec, RegionStructure pstruct, int [][]block){
    int x, y;

    for(x = 0; x < rec.width; x++){
      for(y = 0; y < rec.height; y++){
	if (block[x][y] == 1) 
	  pstruct.Insert(new Point(rec.x + pos.x + x, rec.y + pos.y + y));
	else if(block[x][y] == 0)  // it may be -1 meaning out of grid
	  pstruct.Delete(new Point(rec.x + pos.x + x, rec.y + pos.y + y));
      } // for y
    } // for x
  }

  public void restoreBackgrnd(Grid grid, Point gCor, RegionStructure pstruct){
    insertBlock(pos, rec, pstruct, backBlock);
  }

  public void rollBack(RegionStructure pstruct){
    insertBlock(pos, rec, pstruct, backBlock);
    insertBlock(orgP, rec, pstruct, block);
    pos = orgP;
  } 

  public void move(Grid grid, Point gCor, RegionStructure pstruct, Image dragObj){ 
    gCor.x = gCor.x + relX;
    gCor.y = gCor.y + relY;

    // restore the background
    if (firstDragged){
      insertBlock(orgP, rec, pstruct, backBlock);
      if (mode == UNION_MODE){
	grid.drawContents(dragObj, getGrdRect(orgP), block);
      }

      firstDragged = false;      
    }
    else{
      insertBlock(pos, rec, pstruct, backBlock);
      if (mode == UNION_MODE){
	grid.drawContents(dragObj, getGrdRect(gCor), block);
      }
    }

    // get the new background
    pos = gCor;
    backBlock = grid.get(new Rectangle(pos.x, pos.y, rec.width, rec.height));

    //insert the dragging block
    if (mode == UNION_MODE){
      int [][]unionBlock = grid.get(new Rectangle(pos.x, pos.y, rec.width, rec.height), block);
      insertBlock(pos, rec, pstruct, unionBlock); 
    }
    else
      insertBlock(pos, rec, pstruct, block);
  } 

  public HistoryElmInterface finish(Grid grid){
    if (mode == UNION_MODE){
      int [][]unionBlock = grid.get(new Rectangle(pos.x, pos.y, rec.width, rec.height), block);
      return new InsertBlockCell(new Rectangle(pos.x, pos.y, rec.width, rec.height), unionBlock);
    }


    return new InsertBlockCell(new Rectangle(pos.x, pos.y, rec.width, rec.height), block);
  }

}







