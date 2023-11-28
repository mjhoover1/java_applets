package vasco.regions;

import vasco.common.*;
import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.awt.Polygon;
import java.util.*;
   
public class PolygonCursor implements CursorStyleInterface{
  protected Color color;
  protected Polygon p;

  public PolygonCursor(Polygon p, Color color){
    this.p = p;
    this.color = color;
  }

  public void drawPoly(Color c, DrawingTarget dt, Polygon p){

    //    PathIterator i = p.getPathIterator(null);
    double []coor = new double[6];
    double []curr = new double[2];
    int type;

    boolean first = true;
    for (int x = 0; x < p.npoints; x++){
      if (first){
	coor[0] = p.xpoints[x]; 
	coor[1] = p.ypoints[x]; 
	first = false;
      }
      else{
	curr[0] = p.xpoints[x]; 
	curr[1] = p.ypoints[x]; 
 
	dt.directLine(color, (int)coor[0], (int)coor[1], 
		      (int)curr[0], (int)curr[1]);
  
	coor[0] = curr[0];
	coor[1] = curr[1]; 
      }
    }
 
  }

  public void display(DrawingTarget dt){
    drawPoly(color, dt, p);
    p.translate(1, 1);
    drawPoly(color, dt, p);
    p.translate(-1, -1);
  }
 
  public boolean equals(Object obj){
    if (obj == null)
      return false;

    if (obj instanceof PolygonCursor){
      PolygonCursor vgc = (PolygonCursor)obj;
      return (color.equals(vgc.color) && p.equals(vgc.p));
    }

    return false;
  }

}





