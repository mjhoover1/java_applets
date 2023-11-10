package vasco.common;

/* $Id: YellowPolygon.java,v 1.1 2007/10/29 01:19:51 jagan Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;

public class YellowPolygon implements GenElement {
  DPolygon[] l;

  private static DPolygon[] storeLines(Vector src) {
    DPolygon[] rec = new DPolygon[src.size()];
    src.copyInto(rec);
    return rec;
  }

  YellowPolygon(DPolygon r) {
    l = new DPolygon[1];
    l[0] = r;
  }

  YellowPolygon(Vector v) {
    l = storeLines(v);
  }


  public void fillElementFirst(DrawingTarget g) { };

  public void fillElementNext(DrawingTarget g) { };

  public void drawElementFirst(DrawingTarget g) {
    g.setColor(Color.yellow);
    for (int i = 0; i < l.length; i++) {
    	l[i].draw(g);
      //g.drawLine(l[i].p1.x, l[i].p1.y, l[i].p2.x, l[i].p2.y);
    }
  };

  public void drawElementNext(DrawingTarget g) {
    drawElementFirst(g);
  };

  public int pauseMode() {
    return BASIC;
  }

}

