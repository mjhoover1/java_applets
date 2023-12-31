package vasco.lines;
/* $Id: PMR.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import java.awt.*;

public class PMR extends GenericBucket {

  public PMR(DRectangle can, int mb, int bs, TopInterface p, RebuildTree r) {
    super(can, mb, bs, p, r);
  }

  public void reInit(Choice c) {
    super.reInit(c);
    new Bucket(topInterface, "Splitting Threshold", this);
  }

  public String getName() {
    return "PMR Quadtree";
  }

    public boolean orderDependent() {
        return true;
    }

  // ---- private ----------


  boolean Insert(QEdgeList P, QNode R, int md) {
    QEdgeList L;
    boolean ok = md > 0;

    L = ClipLines(P, R.SQUARE);
    if (L == null)
      return ok;
    if (R.NODETYPE != GRAY) {
      L = MergeLists(L, R.DICTIONARY);
      if (L.length() <= maxBucketSize) {
	R.DICTIONARY = L;
	return ok;
      } else {
	SplitPMNode(R);
	for (int i = 0; i < 4; i++)
	  R.SON[i].DICTIONARY = ClipLines(L, R.SON[i].SQUARE);
	return ok && md > 1; // this block adds one level
      }
    }
    for (int i = 0; i < 4; i++) {
      ok = Insert(L, R.SON[i], md - 1) && ok;
    }
    return ok;
  }


}


