/* $Id: CoordCanvas.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

class CoordCanvas extends Canvas {
  int Xsize, Ysize, rowToDisplay;
  String mess;

  CoordCanvas(int x, int row) {
    int h = 20; // guess
    setSize(x, h);
    Xsize = x; Ysize = h; rowToDisplay = row;
    mess = "";
  }

  void setMessage(String m) {
    mess = m;
    repaint();
  }

  public void paint(Graphics g) {
    g.setColor(Color.black);
    FontMetrics fm = g.getFontMetrics(g.getFont());
    int h = fm.getHeight();

    String begin = "[0, " + rowToDisplay + "]";
    String end = "[" + Xsize +", " + rowToDisplay + "]";

    int w = fm.stringWidth(end);
    g.drawString(begin, 0, h);
    g.drawString(end, Xsize - w, h);

    int messW = fm.stringWidth(mess);
    g.setColor(Color.red);
    g.drawString(mess, Xsize / 2 - messW / 2, h);

  }

}
