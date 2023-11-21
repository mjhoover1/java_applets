package vasco.lines;
/* $Id: GenericBucket.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

public abstract class GenericBucket extends GenericLine implements BucketIface {
  int maxBucketSize;

  public GenericBucket (DRectangle can, int mb, int bs, TopInterface p, RebuildTree r) {
    super(can, mb, p, r);
    maxBucketSize = bs;
  }

  public void reInit(JComboBox c) {
    super.reInit(c);
  }


  /* ---- interface implementation ------ */
  public int getBucket() {
    return maxBucketSize;
  }

  public void setBucket(int b) {
    maxBucketSize = b;
    reb.rebuild();
  }

  // ---- private ----------


  void Delete(QEdgeList P, QNode R) {
    QEdgeListRef L = new QEdgeListRef();

    L.val = ClipLines(P, R.SQUARE);
    if (L.val == null)
      return;
    if (R.NODETYPE == GRAY) {
      for (int i = 0; i < 4; i++) {
	Delete(L.val, R.SON[i]);
      }
      if (Possible_PM1R_Merge(R)) {
	L.val = null;
	if (TryToMergePMR(R, R, L)) {
	  R.DICTIONARY = L.val;
	  R.NODETYPE = BLACK;
	  R.SON[0] = R.SON[1] = R.SON[2] = R.SON[3] = null;
	}	
      }
    } else {
      R.DICTIONARY = SetDifference(R.DICTIONARY, L.val);
    }
  }

  boolean TryToMergePMR(QNode P, QNode R, QEdgeListRef L) {
    if (P.NODETYPE != GRAY) {
      L.val = SetUnion(L.val, P.DICTIONARY);
      return (true);
    } else {
      return (TryToMergePMR(P.SON[0], R, L) &&
              TryToMergePMR(P.SON[1], R, L) &&
              TryToMergePMR(P.SON[2], R, L) &&
              TryToMergePMR(P.SON[3], R, L) &&
              L.val.length() <= maxBucketSize);
    }
  }

}


