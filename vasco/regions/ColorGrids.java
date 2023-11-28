package vasco.regions;
import vasco.common.*;

import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.awt.Point;
import java.util.*;
  
public class ColorGrids implements GenElement{

  Point point; /* active grid */
  int color;
  Node node;
  static Grid grid = null;

  ColorGrids(){
    point = null;
    grid = null;
    node = null;
    color = -1;
  }

  ColorGrids(Grid g, Point p, int c, Node n){
    grid = g;
    point = p;
    color = c;
    node = n;
  }

  public Point getPoint(){
    return point;
  }
 
  public void setGrid(Grid g){
    grid = g;
  }

  public void setPoint(Point p){
    point = p;
  }

  public void fillElementFirst(DrawingTarget g){
  } 
 
  public void fillElementNext(DrawingTarget g){
  }   
 
  public void drawElementFirst(DrawingTarget g){
    if (node != null){
      node.completeNode(0, 0, 512, 0);
      node.display(g, 0); /* display leafs */
      grid.display(g);
      node.display(g, 1); /* display non-leafs */
      /* display the active grid cell */
      g.setColor(Color.green);      
      g.drawRect(point.x * grid.cellSize + 1, 
		 point.y * grid.cellSize + 1, 
		 grid.cellSize - 2, grid.cellSize - 2);
    } 
  
  }

  public void drawElementNext(DrawingTarget g){
  }
 
  public int pauseMode(){
    return 0;
  }

}
