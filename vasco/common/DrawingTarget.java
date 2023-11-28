/* $Id: DrawingTarget.java,v 1.4 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;

public interface DrawingTarget {
    public void addMouseListener(MouseListener ml);
    public void addMouseMotionListener(MouseMotionListener ml);
    public void removeMouseListener(MouseListener ml);
    public void removeMouseMotionListener(MouseMotionListener ml);

    public void changeHelp(int mask, String b1, String b2, String b3);

    public Rectangle getView();
    public Rectangle getOrig();

  public void drawRect(double xx, double yy, double ww, double hh);
  

  public void fillRect(double xx, double yy, double ww, double hh);

  public void fillPoly(DPolygon p);

  public void fillOval(double xx, double yy, int ww, int hh);

  public void drawOval(double x, double y, int width, int height);

  public void drawArc(double x, double y, double w, double h, int sA, int rA);
  public void directDrawArc(Color c, double x, double y, double w, double h, 
			    int sA, int rA);

  public void drawOval(DPoint p, double width, double height);
  public void fillOval(DPoint p, double width, double height);
    // real sizable oval

  public void drawLine(double x1, double y1, double x2, double y2);

  public void directRect(Color c, double x, double y, double w, double h);

  public void directThickRect(Color c, double xx, double yy, double ww,
		              double hh, int thick);
  public void directFillRect(Color c, double x, double y, double w, double h);

  public void directLine(Color c, double x1, double y1, double x2, double y2);

  public void directFillOval(Color c, double x, double y, int w, int h);
  public void directDrawOval(Color c, double x, double y, int w, int h);

  public void setColor(Color c);

  public void redraw();
  
  public void drawString(String s, double x, double y);
  public void drawString(String s, double x, double y, Font f);
  public void directDrawString(String s, double x, double y);

  Point adjustPoint(Point p);
  DPoint transPointT(Point p);
  //  DPoint transPointT(int x, int y);
  Point transPoint(double x, double y);

  boolean visible(DRectangle r);
  boolean visible(Rectangle r);

}
