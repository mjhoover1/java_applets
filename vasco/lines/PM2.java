package vasco.lines;
/* $Id: PM2.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import java.awt.*;

public class PM2 extends PM23 {

  public PM2(DRectangle can, int mb, TopInterface p, RebuildTree r) {
    super(can, mb, p, r);
  }

  public String getName() {
    return "PM2 Quadtree";
  }

    public boolean orderDependent() {
        return false;
    }

  boolean Insert(QEdgeList P, QNode R, int md) {
    QEdgeList L;
    boolean ok = true;

    L = ClipLines(P, R.SQUARE);
    if (L == null)
      return ok;
    if (R.NODETYPE != GRAY) {
      L = MergeLists(L, R.DICTIONARY);
      if (PM2Check(L, R.SQUARE) || md < 0) {
	if (md < 0)
	  ok = false;
	R.DICTIONARY = L;
	return ok;
      } else {
	SplitPMNode(R);
      }
    }
    for (int i = 0; i < 4; i++) {
      ok = Insert(L, R.SON[i], md - 1) && ok;
    }
    return ok;
  }

}




