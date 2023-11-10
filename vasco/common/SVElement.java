/* $Id: SVElement.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.util.*;

public class SVElement extends AnimElement {
  DRectangle[] cyan;

  private static DRectangle[] storeRect(Vector src) {
    DRectangle[] rec = new DRectangle[src.size()];
    src.copyInto(rec);
    return rec;
  }

  public SVElement(GenElement e, Vector c) {
    ge = e;
    cyan = storeRect(c);
  }

  void drawCyan(DrawingTarget g) {
    g.setColor(Color.cyan);
    for (int i = 0; i < cyan.length; i++)
      g.fillRect(cyan[i].x, cyan[i].y, cyan[i].width, cyan[i].height);
  }

}
