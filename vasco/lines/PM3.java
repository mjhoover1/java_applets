package vasco.lines;
/* $Id: PM3.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

public class PM3 extends PM23 {

  public PM3(DRectangle can, int mb, TopInterface p, RebuildTree r) {
    super(can, mb, p, r);
  }

  public String getName() {
    return "PM3 Quadtree";
  }

    public boolean orderDependent() {
        return false;
    }

  // ---- private ----------

  boolean Insert(QEdgeList P, QNode R, int md) {
    QEdgeList L;
    boolean ok = true;

    L = ClipLines(P, R.SQUARE);
    if (L == null)
      return ok;
    if (R.NODETYPE != GRAY) {
      L = MergeLists(L, R.DICTIONARY);
      if (PM3Check(L, R.SQUARE) || md < 0) {
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

  boolean PM3Check(QEdgeList l, QSquare S) {
    if (l == null)
      return true;
    if (l.DATA.P1 == l.DATA.P2) //Compare 'pointers'
      return (SharePM3Vertex(new DPoint(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), l.NEXT, S));

    if (PtInSquare(l.DATA.P1, S) && PtInSquare(l.DATA.P2, S))
      return false;
    if (PtInSquare(l.DATA.P1, S))
	return (SharePM3Vertex(l.DATA.P1, l.NEXT, S));
    if (PtInSquare(l.DATA.P2, S))
      return (SharePM3Vertex(l.DATA.P2, l.NEXT, S));
    return PM3Check(l.NEXT, S);
  }

  boolean SharePM3Vertex(DPoint P, QEdgeList l, QSquare S) {
    if (l == null)
      return true;
    if (P == l.DATA.P1)
      return (!PtInSquare(l.DATA.P2, S) && SharePM3Vertex(P, l.NEXT, S));
    if (P == l.DATA.P2)
       return (!PtInSquare(l.DATA.P1, S) && SharePM3Vertex(P, l.NEXT, S));
    return (!PtInSquare(l.DATA.P1, S) && !PtInSquare(l.DATA.P2, S) && SharePM3Vertex(P, l.NEXT, S));
  }
}

