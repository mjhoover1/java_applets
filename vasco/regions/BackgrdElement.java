package vasco.regions;
import vasco.common.*;

import java.awt.*;
import java.util.*;
  
public class BackgrdElement extends  ConvertGenElement{
  
  Grid grid;
  Rectangle rec;
  Color color;

  // grid row, sCol, and eCol
  public BackgrdElement(Grid grid, Rectangle rec, Color color){
    this.grid = grid;
    this.rec = rec;
    this.color = color;
  } 

  
  public void fillElementFirst(DrawingTarget g){
  }

  public void fillElementNext(DrawingTarget g){
  }

  public void drawElementFirst(DrawingTarget g){
    // draw the background
    g.setColor(color);
    g.fillRect(rec.x, rec.y, rec.width, rec.height);
    // draw the grid
    if (grid.gridOn)
      grid.display(g);
  }

  public void drawElementNext(DrawingTarget g){
  }

}

