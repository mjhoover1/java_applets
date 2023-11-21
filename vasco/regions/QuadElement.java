package vasco.regions;
import vasco.common.*;

import javax.swing.*; // import java.awt.*;
import java.util.*;
  
public class QuadElement extends  ConvertGenElement{
  
  public static final int DRAW_ALL = 0;
  public static final int DRAW_NONLEAF = 1;

  Grid grid;
  Node node;
  int mode;

  // grid row, sCol, and eCol
  public QuadElement(Grid grid, Node node, int mode){
    this.grid = grid;
    this.node = node;
    this.mode = mode;
  } 

  
  public void fillElementFirst(DrawingTarget g){
  }

  public void fillElementNext(DrawingTarget g){
  }

  public void drawElementFirst(DrawingTarget g){
    switch(mode){
    case DRAW_ALL:
      node.display(g, 0);
      node.display(g, 1);
      break;
    case DRAW_NONLEAF:
      node.display(g, 1);
      break;
    }
  }

  public void drawElementNext(DrawingTarget g){
  }

}

