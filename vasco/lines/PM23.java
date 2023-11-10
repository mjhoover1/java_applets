package vasco.lines;
/* $Id: PM23.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import java.awt.*;

public abstract class PM23 extends GenericLine {

  public PM23(DRectangle can, int mb, TopInterface p, RebuildTree r) {
    super(can, mb, p, r);
  }


  void Delete(QEdgeList P, QNode R) {
    QEdgeListRef L = new QEdgeListRef();

    L.val = ClipLines(P, R.SQUARE);
    if (L.val == null)
      return;
    if (R.NODETYPE == GRAY) {
      for (int i = 0; i < 4; i++) {
	Delete(L.val, R.SON[i]);
      }
      if (Possible_PM23_Merge(R)) {
	L.val = null;
	if (TryToMergePM23(R, R, L)) {
	  R.DICTIONARY = L.val;
	  R.NODETYPE = BLACK;
	  R.SON[0] = R.SON[1] = R.SON[2] = R.SON[3] = null;
	}	
      }
    } else
      R.DICTIONARY = SetDifference(R.DICTIONARY, L.val);
  }

  boolean Possible_PM23_Merge(QNode P) {
    return (P.SON[0].NODETYPE != GRAY && 
	    P.SON[1].NODETYPE != GRAY && 
	    P.SON[2].NODETYPE != GRAY && 
	    P.SON[3].NODETYPE != GRAY);
  }

  boolean TryToMergePM23(QNode P, QNode R, QEdgeListRef L) {
    
    for (int i = 0; i < 4; i++) {
      L.val = SetUnion(L.val, P.SON[i].DICTIONARY);
    }
    return PM2Check(L.val, P.SQUARE);
  }

  boolean PM2Check(QEdgeList l, QSquare S) {
    if (l == null)
      return true;
    if (l.DATA.P1 == l.DATA.P2) //Compare 'pointers'
      return (l.NEXT == null);

    if (PtInSquare(l.DATA.P1, S) && PtInSquare(l.DATA.P2, S))
      return false;
    if (SharePM2Vertex(l.DATA.P1, l, S) || SharePM2Vertex(l.DATA.P2, l, S))
      return true;
    else
      return false;
  }

  boolean SharePM2Vertex(DPoint P, QEdgeList l, QSquare S) {
    if (l == null)
      return true;
    if (P == l.DATA.P1)
      return (!PtInSquare(l.DATA.P2, S) && SharePM2Vertex(P, l.NEXT, S));
    if (P == l.DATA.P2)
       return (!PtInSquare(l.DATA.P1, S) && SharePM2Vertex(P, l.NEXT, S));
    return false;
  }
}
