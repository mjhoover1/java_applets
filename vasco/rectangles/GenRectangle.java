package vasco.rectangles;
/* $Id: GenRectangle.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;

import java.awt.Color;

import javax.swing.*; // import java.awt.*;

abstract public class GenRectangle extends DRectangle implements GenElement {
    public GenRectangle(DRectangle r) {
    super(r.x, r.y, r.width, r.height);
  }

  public void drawElementFirst(DrawingTarget g) {
    g.setColor(Color.yellow);
    draw(g);
  }

  public abstract void drawElementNext(DrawingTarget g);

  public abstract int pauseMode();

  public void fillElementFirst(DrawingTarget g) {};
  public void fillElementNext(DrawingTarget g) {};

}
