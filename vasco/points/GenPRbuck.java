package vasco.points;
/* $Id: GenPRbuck.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;


public abstract class GenPRbuck extends GenericPRbucket {

  public GenPRbuck(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
    super(can, b, md, p, r);
  }
  
  public void reInit(Choice ao) {
    super.reInit(ao);
  }

  PRbucketNode insert (DPoint p, PRbucketNode R, 
			double X, double Y, double lx, double ly, int md, boolean[] ok) {
    PRbucketNode T, U;
    int Q;
    ok[0] = ok[0] && md > 0;


    if (R == null) {
      T = new PRbucketNode(BLACK);
      T.addPoint(p);
      return T;
    }

    if (R.NODETYPE == BLACK) {
      if (R.points.size() < maxBucketSize)
	R.addPoint(p);
      else {
	Vector pts = R.points;
	R = new PRbucketNode(GRAY);
	for (int i = 0; i < pts.size(); i++) {
	  DPoint pt = (DPoint)pts.elementAt(i);
	  insert(pt, R, X, Y, lx, ly, md, ok);
	}
	insert(p, R, X, Y, lx, ly, md, ok);
      }
      return R;
    }
    T = R;
    Q = PRCompare(p, X, Y);
    while(T.SON[Q] != null && T.SON[Q].NODETYPE == GRAY) {
      T = T.SON[Q];
      X += XF[Q] * lx;
      lx /= 2;
      Y += YF[Q] * ly;
      ly /= 2;
      Q = PRCompare(p, X, Y);
      md--;
    }
    if (T.SON[Q] == null) {
      T.SON[Q] = new PRbucketNode(BLACK);
      T.SON[Q].addPoint(p);
      return R; 
    }

    U = T;
    T = T.SON[Q];
    X += XF[Q] * lx;
    lx /= 2;
    Y += YF[Q] * ly;
    ly /= 2;
    md--;

    if (T.NODETYPE == BLACK)
      if (T.isIn(p))
	return R;
      else if (T.points.size() < maxBucketSize) {
	T.addPoint(p);
	return R;
      } else {
	Vector pts = T.points;
	U.SON[Q] = T = new PRbucketNode(GRAY);
	for (int i = 0; i < pts.size(); i++) {
	  DPoint s = (DPoint)pts.elementAt(i);
	  Q = PRCompare(s, X, Y);
	  if (T.SON[Q] == null)
	    T.SON[Q] = new PRbucketNode(BLACK);
	  T.SON[Q].addPoint(s);
	}
	insert(p, T, X, Y, lx, ly, md, ok);
      }
    return R;
  }

}
