/* $Id: PR.java,v 1.2 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.util.Vector;

public class PR {
  final int maxBucketSize = 1;

  final int BLACK = 0;
  final int WHITE = 1;
  final int GRAY  = 2;

  final int NW = 0;
  final int NE = 1;
  final int SW = 2;
  final int SE = 3;

  final double XF[] = {0, 0.5, 0, 0.5};
  final double YF[] = {0.5, 0.5, 0, 0};

  //-------------------------------------

  public PR(DRectangle can) { // origin - left bottom corner
    PRroot = null;
    wholeCanvas = can;
  }

  public DPoint Insert(DPoint p) {
    if (PRroot == null) {
      PRroot = new PRNode(p);
      return PRroot.qn;
    } else
      return insert(p, PRroot, wholeCanvas.x, wholeCanvas.y, wholeCanvas.width, wholeCanvas.height, null, 0);
  }	

  public void Delete(DPoint p) {
    PRroot = delete(p, PRroot, wholeCanvas.x, wholeCanvas.y, wholeCanvas.width, wholeCanvas.height);
  }

  public DPoint NearestPoint(DPoint src) {
    if (PRroot == null || src == null)
      return null;
    PRIncNearest pr = new PRIncNearest(PRroot);
    return pr.Query(src);
  }

  //-------------------------------------

  class PRNode {
    int refcount;
    DPoint qn;
    int ntype;
    PRNode[] sons;

    PRNode() {
      sons = new PRNode[4];
      ntype = GRAY;
      sons[0] = sons[1] = sons[2] = sons[3] = null;
    }

    PRNode(DPoint p) {
      sons = new PRNode[4];
      qn = new DPoint(p.x, p.y);
      ntype = BLACK;
      sons[0] = sons[1] = sons[2] = sons[3] = null;
      refcount = 1;
    }

  }

  PRNode PRroot;
  DRectangle wholeCanvas;
  

  // ------------------- incremental nearest --------------------

  class PRIncNearest {
    class PRQueueElement {
      double key;

      PRQueueElement(double k) {
	key = k;
      }
    }

    class PRQueueLeaf extends PRQueueElement {
      DPoint p;
      
      PRQueueLeaf(double k, DPoint p) {
	super(k);
	this.p = p;
      }
    }

    class PRQueueINode extends PRQueueElement {
      PRNode r;
      DRectangle block;

      PRQueueINode(double k, PRNode p, DRectangle r) {
	super(k);
	this.r = p;
	block = r;
      }
    }

    class PRQueue {
      Vector v;

      PRQueue() {
	v = new Vector();
      }

      void Enqueue(PRQueueElement qe) {
	v.addElement(qe);
	for (int i = v.size() - 1; i > 0; i--) {
	  PRQueueElement q1 = (PRQueueElement)v.elementAt(i - 1);
	  PRQueueElement q2 = (PRQueueElement)v.elementAt(i);
	  if (q1.key > q2.key) {
	    v.setElementAt(q2, i - 1);
	    v.setElementAt(q1, i);
	  }
	}
      }

      PRQueueElement Dequeue() {
	PRQueueElement q = (PRQueueElement)v.elementAt(0);
	v.removeElementAt(0);
	return q;
      }
  
      boolean isEmpty() {
	return (v.size() == 0);
      }
    }

 
    PRQueue q;

    PRIncNearest(PRNode rt) {
      q = new PRQueue();
      q.Enqueue(new PRQueueINode(0, rt, wholeCanvas));
    }

    DPoint Query(DPoint query) {

    while(!q.isEmpty()) {
      PRQueueElement element = q.Dequeue();
      if (element instanceof PRQueueLeaf) {
	PRQueueLeaf leaf = (PRQueueLeaf)element;
	return leaf.p;
      } else {
	PRQueueINode inode = (PRQueueINode)element;
	if (inode.r.ntype == BLACK) {
	  if (query.distance(inode.r.qn) >= query.distance(inode.block))
	    q.Enqueue(new PRQueueLeaf(query.distance(inode.r.qn), inode.r.qn));
	} else if (inode.r.ntype == GRAY) {
	  for (int i = 0; i < 4; i++) 
	    if (inode.r.sons[i] != null) {
	      DRectangle newblock = new DRectangle(inode.block.x + XF[i] * inode.block.width, 
						   inode.block.y + YF[i] * inode.block.height, 
						   inode.block.width / 2, inode.block.height / 2);
	      q.Enqueue(new PRQueueINode(query.distance(newblock), inode.r.sons[i], newblock));
	    }
	}
      }
    }
    return null;
    }
  } // PRIncNearest

  // -----------------------------------------------------------------------------------------------------

  int PRCompare(DPoint P, double X, double Y) {
    if (P.x < X) 
      return P.y < Y ? SW : NW;
    else
      return P.y < Y ? SE : NE;
  }

  DPoint insert (DPoint p, PRNode R, double X, double Y, double lx, double ly, PRNode parent, int parQ) {
    if (R.ntype == BLACK) {
      if (R.qn.equals(p)) {
	R.refcount++;
	return R.qn;
      } else {
	PRNode pn = new PRNode();
	if (parent == null)
	  PRroot = pn;
	else
	  parent.sons[parQ] = pn;
	pn.sons[PRCompare(R.qn, X + lx/2, Y + ly/2)] = R;
	return insert(p, pn, X, Y, lx, ly, parent, parQ);
      }
    }

    if (R.ntype == GRAY) {
      int q = PRCompare(p, X + lx/2, Y + ly/2);
      if (R.sons[q] == null) {
	R.sons[q] = new PRNode(p);
	return R.sons[q].qn;
      } else
	return insert(p, R.sons[q], X + XF[q] * lx, Y + YF[q] * ly, lx / 2, ly / 2, R, q);
    }
    return null; // never
  }

  PRNode delete(DPoint P, PRNode R, double X, double Y, double lx, double ly) {
    if (R == null)
      return R;
    if (R.ntype == BLACK) {
      if (R.qn.equals(P)) {
	if (R.refcount > 1) {
	  R.refcount--;
	  return R;
	} else 
	  return null;
      }
    } else {
      int Q = PRCompare(P, X + lx/2, Y + ly/2);
      R.sons[Q] = delete(P, R.sons[Q], X + XF[Q] * lx, Y + YF[Q] * ly, lx/2, ly / 2);
      int sum = 0;
      for (int i = 0; i < 4; i++) {
	if (R.sons[i] != null && R.sons[i].ntype == GRAY)
	  return R;
	if (R.sons[i] != null && R.sons[i].ntype == BLACK)
	  sum++;
      }
      if (sum <= 1) {
	PRNode newR = null;
	for (int i = 0; i < 4; i++) {
	  if (R.sons[i] != null && R.sons[i].ntype == BLACK)
	    newR = R.sons[i];
	}
	return newR;
      }
    }

    return R;
  }

  public static void main (String[] in) {
    System.out.println("PR: running the test routine");
    PR tree = new PR(new DRectangle(0,0,512,512));
    for (int i = 0; i < 300; i++)
      tree.Insert(new DPoint(511 * Math.random(), 511*Math.random()));
    for (int i = 0; i < 300; i++) {
      DPoint near = tree.NearestPoint(new DPoint(511 * Math.random(), 511*Math.random()));
      tree.Delete(near);
    }
  }
}
