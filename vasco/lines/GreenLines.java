package vasco.lines;
/* $Id: GreenLines.java,v 1.2 2007/10/28 15:38:16 jagan Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.util.*;

public class GreenLines implements GenElement {
  DLine[] l;

  private static DLine[] storeLines(Vector src) {
    DLine[] rec = new DLine[src.size()];
    src.copyInto(rec);
    return rec;
  }

  public GreenLines(DLine r) {
    l = new DLine[1];
    l[0] = r;
  }

  public GreenLines(Vector v) {
    l = storeLines(v);
  }


  public void fillElementFirst(DrawingTarget g) { };

  public void fillElementNext(DrawingTarget g) { };

  public void drawElementFirst(DrawingTarget g) {
    g.setColor(Color.green);
    for (int i = 0; i < l.length; i++) {
      g.drawLine(l[i].p1.x, l[i].p1.y, l[i].p2.x, l[i].p2.y);
    }
  };

  public void drawElementNext(DrawingTarget g) {
    drawElementFirst(g);
  };

  public int pauseMode() {
    return BASIC;
  }

}
