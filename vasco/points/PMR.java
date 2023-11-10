package vasco.points;
/* $Id: PMR.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;

public class PMR extends GenericPRbucket {

  public PMR(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
    super(can, b, md, p, r);
  }

  public void reInit(Choice ao) {
    super.reInit(ao);
    new Bucket(topInterface, "Splitting Threshold", this);
  }   

  public String getName() {
    return "PMR Quadtree";
  }

    public boolean orderDependent() {
        return true;
    }

  PRbucketNode insert (DPoint p, PRbucketNode R, 
			double X, double Y, double lx, double ly, int md, boolean[] ok) {
    PRbucketNode T, U;
    int Q;

    if (R == null) {
      T = new PRbucketNode(BLACK);
      T.addPoint(p);
      return T;
    }

    if (R.NODETYPE == BLACK) {
      R.addPoint(p);
      if (R.points.size() > maxBucketSize) {
	md--;
	Vector pts = R.points;
	R = new PRbucketNode(GRAY);
	for (int i = 0; i < pts.size(); i++) {
	  DPoint pt = (DPoint)pts.elementAt(i);
	  Q = PRCompare(pt, X, Y);
	  if (R.SON[Q] == null) 
	    R.SON[Q] = new PRbucketNode(BLACK);
	  R.SON[Q].addPoint(pt);
	}
      }
      ok[0] = ok[0] && md > 0;
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

    md--;

    if (T.SON[Q] == null) {
      ok[0] = ok[0] && md > 0;
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

    if (T.NODETYPE == BLACK)
      if (!T.isIn(p)) {
	T.addPoint(p);
	if (T.points.size() > maxBucketSize) {
	  md--;
	  Vector pts = T.points;
	  U.SON[Q] = T = new PRbucketNode(GRAY);
	  for (int i = 0; i < pts.size(); i++) {
	    DPoint s = (DPoint)pts.elementAt(i);
	    Q = PRCompare(s, X, Y);
	    if (T.SON[Q] == null)
	      T.SON[Q] = new PRbucketNode(BLACK);
	    T.SON[Q].addPoint(s);
	  }
	}
      }
    ok[0] = ok[0] && md > 0;
    return R;
  }
}
