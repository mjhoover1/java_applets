package vasco.rectangles;
/* $Id: PMRRectTree.java,v 1.2 2004/11/20 22:38:48 brabec Exp $ */
import vasco.common.*;

import java.util.*;
import java.awt.*;


class PMRRectTree extends GenericRectTree {
  
    public PMRRectTree(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
    super(can, md, bs, p, r);
  }

    public void reInit(Choice c) {
    super.reInit(c);
    new Bucket(topInterface, "Splitting Threshold", this);
  }

    public String getName() {
    return "PMR Rectangle Quadtree";
  }

    public boolean orderDependent() {
        return true;
    }

  //------------------------------
  
  boolean localInsert(RNode q, DRectangle r, DRectangle block, int md) {
    boolean ok = true;
    if (q.NODETYPE == WHITE) {
      q.r.addElement(r);
      q.NODETYPE = BLACK;
      if (md < 0)
	ok = false;
      return ok;
    }
    if (q.NODETYPE == BLACK) {
      q.r.addElement(r);
      if (q.r.size() > maxBucketSize) {
	ok = ok && md >= 1;
	for (int i = 0; i < 4; i++)
	  q.son[i] = new RNode();
	q.NODETYPE = GRAY;
	Vector tmp = q.r;
	q.r = new Vector();
	for (int j = 0; j < tmp.size(); j++)
	  for (int i = 0; i < 4; i++) {
	    DRectangle cur = (DRectangle)tmp.elementAt(j);
	    if (cur.intersects(new DRectangle(block.x + block.width * xf[i],
					      block.y + block.height * yf[i],
					      block.width / 2, block.height / 2))) {
	      q.son[i].r.addElement(cur);
	      q.son[i].NODETYPE = BLACK;
	    }
	  }
	return ok;
      }
    }
    if (q.NODETYPE == GRAY) {
      for (int i = 0; i < 4; i++) {
	DRectangle dr = new DRectangle(block.x + block.width * xf[i],
				       block.y + block.height * yf[i],
				       block.width / 2, block.height / 2);
	if (r.intersects(dr)) {
	  //	  System.out.println("inserting to :" + i);
	  ok = localInsert(q.son[i], r, dr, md - 1) && ok;
	}
      }
      return ok;
    }

    return ok;
  }

}
