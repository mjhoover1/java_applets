package vasco.lines;
/* $Id: PMbucket.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import java.awt.*;


public class PMbucket extends GenericBucket {

  public PMbucket(DRectangle can, int mb, int bs, TopInterface p, RebuildTree r) {
    super(can, mb, bs, p, r);
  }

  public String getName() {
    return "Bucket PM Quadtree";
  }

    public boolean orderDependent() {
        return false;
    }

  public void reInit(Choice c) {
    super.reInit(c);
    new Bucket(topInterface, "Bucket Capacity", this);
  }

  boolean Insert(QEdgeList P, QNode R, int md) {
    QEdgeList L;
    boolean ok = md > 0 || true;  // always ok even if capacity exceeded

    L = ClipLines(P, R.SQUARE);
    if (L == null)
      return ok;
    if (R.NODETYPE != GRAY) {
      L = MergeLists(L, R.DICTIONARY);
      if (L.length() <= maxBucketSize || md < 0 ) {
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
