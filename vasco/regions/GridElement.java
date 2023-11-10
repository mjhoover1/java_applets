package vasco.regions;
import vasco.common.*;

import java.awt.*;
import java.util.*;
  
public class GridElement extends  ConvertGenElement{
  
  public static final int UNKNOWN_MODE = 0;
  public static final int RASTER_MODE = 1;
  public static final int QUAD_MODE = 2;

  Grid grid;
  int row;
  int col;
  Rectangle rec;
  int mode;

  public GridElement(Grid grid, Rectangle rec){
    this.grid = grid;
    this.rec = rec;
    row = 0;
    col = 0;
    mode = QUAD_MODE;
  }
 
  // grid row, sCol, and eCol
  public GridElement(Grid grid, int row, int col){
    this.grid = grid;
    this.row = row;
    this.col = col;
    rec = null;
    mode = RASTER_MODE;
  } 

  
  public void fillElementFirst(DrawingTarget g){
  }

  public void fillElementNext(DrawingTarget g){
  }

  public void drawElementFirst(DrawingTarget g){
    switch(mode){
    case RASTER_MODE:
      grid.drawContents(g, row, col);
      break;
    case QUAD_MODE:
      grid.drawContents(g, rec);
      break;
    default:
      break;
    }
  }

  public void drawElementNext(DrawingTarget g){
  }

}


