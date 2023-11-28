/* $Id: NNElement.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import java.util.*;
import vasco.drawable.*;

public class NNElement extends AnimElement {
  GenElement[] nq;
    double dist;

  private static GenElement[] storeGen(Vector src) {
    GenElement[] rec = new GenElement[src.size()];
    src.copyInto(rec);
    return rec;
  }

  public NNElement(GenElement e, double dist, Vector c) {
    ge = e;
    this.dist = dist;
    nq = storeGen(c);
  }

  void drawQueue(DrawingTarget g) {
    for (int i = 0; i < nq.length; i++) 
      nq[i].drawElementFirst(g);
  }
  
  void fillQueue(DrawingTarget g) {
    for (int i = 0; i < nq.length; i++) 
      nq[i].fillElementFirst(g);
  }

    boolean isElement() {
	return (ge instanceof NNDrawable);
    }

}
