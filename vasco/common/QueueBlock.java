/* $Id: QueueBlock.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.*; // import java.awt.*;

public class QueueBlock extends DRectangle implements GenElement {

  public QueueBlock(double xx, double yy, double w, double h) {
    super(xx, yy, w, h);
  }

  public QueueBlock(DRectangle r) {
    super(r.x, r.y, r.width, r.height);
  }

  public QueueBlock(Rectangle r) {
    super(r.x, r.y, r.width, r.height);
  }

  public void fillElementFirst(DrawingTarget g) {
    g.setColor(Color.cyan);
    g.fillRect(x, y, width, height);
  }

  public void fillElementNext(DrawingTarget g) {
  }

  public void drawElementFirst(DrawingTarget g) {
    g.setColor(Color.green);
    g.drawRect(x, y, width, height);
  }

  public void drawElementNext(DrawingTarget g) {};

  public int pauseMode() {
    return BASIC;
  }

}
