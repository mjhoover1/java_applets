package vasco.rectangles;
/* $Id: RNode.java,v 1.2 2007/10/28 15:38:19 jagan Exp $ */
import java.util.Vector;

import vasco.common.CommonConstants;
import vasco.common.DRectangle;

// ---------- RectQuadtree Node -----

class RNode implements CommonConstants {
  RNode son[];
  Vector r; // vector of 'DRectangle's for bucket version
  int NODETYPE;

  RNode() {
    son = new RNode[4];
    son[0] = son[1] = son[2] = son[3] = null;
    r = new Vector();
    NODETYPE = WHITE;
  }

  void addRect(DRectangle t) {
    r.addElement(t);
  }

  int find(DRectangle p) {
    for (int i = 0; i < r.size(); i++) {
      DRectangle s = (DRectangle)r.elementAt(i);
      if (s.equals(p))
        return i;
    }
    return -1;
  }
  
  boolean isIn(DRectangle p) {
    return find(p) >= 0;
  }

  void deleteRect(DRectangle t) {
    int i = find(t);
    if (i >= 0)
      r.removeElementAt(i);
  }

}

