package vasco.points;
/* $Id: PTTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;
import java.util.*;

public class PTTree extends PointStructure {

  PTNode ROOT;
  final int NRDIRS = 4;

  public PTTree(DRectangle can, TopInterface p, RebuildTree r) {
    super(can, p, r);
    ROOT = null;
  }

  public void reInit(JComboBox ao) {
    super.reInit(ao);
    ao.addItem("Nearest");
    ao.addItem("Within");
  }

  public void Clear() {
    super.Clear();
    ROOT = null;
  }

  public String getName() {
    return "Point Quadtree";
  }

    public boolean orderDependent() {
        return true;
    }

  public boolean Insert(DPoint p) {
    ROOT = insert(p, ROOT);
    return true;
  }

  public void Delete(DPoint p) {
    if (ROOT == null)
      return;
    PTIncNearest ptin = new PTIncNearest(ROOT);
    DPoint ptn = ptin.Query(new QueryObject(p));
    ROOT = delete(ptn, ROOT);
  }

    public void DeleteDirect(Drawable d) {
	if (ROOT == null)
	    return;
	ROOT = delete((DPoint)d, ROOT);
    }


  public SearchVector Search(QueryObject query, int mode) {
    SearchVector res = new SearchVector();
    searchVector = new Vector();
    search(ROOT, query, wholeCanvas, mode, res);
    return res;
  }

  public Drawable NearestFirst(QueryObject p) {
    if (ROOT != null) {
      PTIncNearest ptin = new PTIncNearest(ROOT);
      return ptin.Query(p);
    } else
      return null;
  }

  public Drawable[] NearestRange(QueryObject p, double dist) {
      PTIncNearest near = new PTIncNearest(ROOT);
      return near.Query(p, dist);
  }

  public SearchVector Nearest(QueryObject p) {
    SearchVector v = new SearchVector();
    if (ROOT != null) {
      PTIncNearest ptin = new PTIncNearest(ROOT);
      ptin.Query(p, v);
    }
    return v;
  }

  public SearchVector Nearest(QueryObject p, double dist) {
    SearchVector v = new SearchVector();
    if (ROOT != null) {
      PTIncNearest ptin = new PTIncNearest(ROOT);
      ptin.Query(p, v, dist, Integer.MAX_VALUE);
    }
    return v;
  }

  public void drawContents(DrawingTarget g, Rectangle view) {
    drawC(ROOT, g, wholeCanvas.x, wholeCanvas.y, wholeCanvas.x + wholeCanvas.width, wholeCanvas.y + wholeCanvas.height);
  }

  // ----------------------------------------------

  class PTNode {

    DPoint pnt;
    PTNode SON[] = new PTNode[NRDIRS];

    PTNode(DPoint p) {
      pnt = p;
      for (int i = 0; i < NRDIRS; i++) {
	SON[i] = null;
      }
    }

    int SonType(PTNode S) {
      for (int i = 0; i < NRDIRS; i++) {
	if (SON[i] == S) {
	  return i;
	}
      }
      return -1;
    }

  }


  /* ------------ private methods --------------- */

  private int PTCompare(DPoint P, DPoint R) {
    if (P.x < R.x) 
      return (P.y < R.y) ? SW : NW;
    else 
      return (P.y < R.y) ? SE : NE;
  }

  Vector searchVector;


  private void search(PTNode R, QueryObject qu, DRectangle block, int mode, SearchVector v) {
    v.addElement(new SVElement(new YellowBlock(block, R == null || (R.SON[0] == null && R.SON[1] == null && 
								    R.SON[2] == null && R.SON[3] == null)), 
			       searchVector));

    if (R == null) {
      return;
    }
    
    v.addElement(new SVElement(new GreenPoints(R.pnt), searchVector));
    drawableInOut(qu, R.pnt, mode, v, searchVector);

    if (R.SON[0] == null && R.SON[1] == null && R.SON[2] == null && R.SON[3] == null)
      return;

    DRectangle nw = new DRectangle(block.x, R.pnt.y, R.pnt.x - block.x, block.y + block.height - R.pnt.y);
    DRectangle ne = new DRectangle(R.pnt.x, R.pnt.y, block.x + block.width - R.pnt.x, block.y + block.height - R.pnt.y);
    DRectangle sw = new DRectangle(block.x, block.y, R.pnt.x - block.x, R.pnt.y - block.y);
    DRectangle se = new DRectangle(R.pnt.x, block.y, block.x + block.width - R.pnt.x, R.pnt.y - block.y);

    if (qu.intersects(ne))
      searchVector.addElement(ne);
    if (qu.intersects(se))
      searchVector.addElement(se);
    if (qu.intersects(nw))
      searchVector.addElement(nw);
    // ---
    if (qu.intersects(sw)) {
      search(R.SON[SW], qu, sw, mode, v);
    }

    if (qu.intersects(nw)) {
      searchVector.removeElementAt(searchVector.size() - 1);
      search(R.SON[NW], qu, nw, mode, v);
    }
    if (qu.intersects(se)) {
      searchVector.removeElementAt(searchVector.size() - 1);
      search(R.SON[SE], qu, se, mode, v);
    }
    if (qu.intersects(ne)) {
      searchVector.removeElementAt(searchVector.size() - 1);
      search(R.SON[NE], qu, ne, mode, v);
    }
  }

  private PTNode insert(DPoint p, PTNode R) {
    return insert(new PTNode(p), R);
  }

  private PTNode insert(PTNode P, PTNode R) {
    PTNode T, F = null;
    int Q = SE;

    if (R == null) {
      return P;
    } else {
      T = R;
      while (T != null && !P.pnt.equals(T.pnt)) {
	F = T;
	Q = PTCompare(P.pnt, T.pnt);
	T = T.SON[Q];
      }
      if (T == null) {
	F.SON[Q] = P;
      }
    }
    return R;
  }

  private int NrSons(PTNode P) {
    int counter = 0;
    for (int i = 0; i < NRDIRS; i++) {
      if (P.SON[i] != null) {
	counter++;
      }
    }
    return counter;
  }
      
  private PTNode FindFather(PTNode P, PTNode R) {
    PTNode F = null;
    int Q;

    if (R == null) {
      return null;
    }
    while (R != P) {
      F = R;
      Q = PTCompare(P.pnt, R.pnt);
      R = R.SON[Q];
    }
    return F;
  }

  private PTNode NonEmptySon(PTNode P) {
    for (int i = 0; i < NRDIRS; i++) {
      if (P.SON[i] != null) {
	return P.SON[i];
      }
    }
    return null;
  }

  private DPoint FindCandidate(PTNode P, int Q) {
    if (P == null) {
      switch(Q){
      case SW: return new DPoint(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
      case NW: return new DPoint(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
      case SE: return new DPoint(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
      case NE: return new DPoint(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
      }
      return null;
    } else {
      while (P.SON[OpQuad(Q)] != null) {
	P = P.SON[OpQuad(Q)];
      }
      return P.pnt;
    }
  }

  private int FindBest(PTNode P) {
    double min = Double.POSITIVE_INFINITY;
    int quad = -1;
    double res;
    DPoint J;

    for (int i = 0; i < NRDIRS; i++) {
      J = FindCandidate(P.SON[i], i);
      res = Math.abs(J.x - P.pnt.x) + Math.abs(J.y - P.pnt.y);
      if (res < min) {
	min = res;
	quad = i;
      }
    }
    return quad;
  }


  private PTNode delete(DPoint p, PTNode R) {
    PTNode J, T, P;
    int Q;

    for (P = R; P != null && !P.pnt.equals(p); P = P.SON[PTCompare(p, P.pnt)]);

    if (P == null || R == null)
      return R;

    if (NrSons(P) <= 1) {
      J = FindFather(P, R);
      if (J == null) {
	R = NonEmptySon(P);
      } else {
	J.SON[J.SonType(P)] = NonEmptySon(P);
      }
      return R;
    } else {
      Q = FindBest(P);
      J = P.SON[Q];
      P.pnt = J.pnt;
      AdjQuad(CQuad(Q), OpQuad(Q), CQuad(Q), P, P);
      AdjQuad(CCQuad(Q), OpQuad(Q), CCQuad(Q), P, P);
      if (PTCompare(P.SON[Q].pnt, P.pnt) != Q) {
	T = P.SON[Q];
	P.SON[Q] = null;
	InsertQuadrant(T, P);
      } else {
	NewRoot(Q, P.SON[Q], P, P);
      }
      /* find the best replacement */

    }
    return R;
  }

  private void AdjQuad(int Q, int D, int S, PTNode F, PTNode R) {
    PTNode T;
    
    T = F.SON[S];
    if (T == null) {
      return;
    } else {
      if (PTCompare(T.pnt, R.pnt) == Q) {
	AdjQuad(Q, D, OpQuad(Q), T, R);
	AdjQuad(Q, D, D, T, R);
      } else {
	F.SON[S] = null;
	InsertQuadrant(T, R);
      }
    }
  }

  private void NewRoot(int Q, PTNode S, PTNode R, PTNode F) {
    if (S.SON[OpQuad(Q)] == null) {
      InsertQuadrant(S.SON[CQuad(Q)], R);
      InsertQuadrant(S.SON[CCQuad(Q)], R);
      if (R != F) {
	F.SON[OpQuad(Q)] = S.SON[Q];
      } else {
	F.SON[Q] = S.SON[Q];
      }
    } else {
      AdjQuad(Q, CQuad(Q), CQuad(Q), S, R);
      AdjQuad(Q, CCQuad(Q), CCQuad(Q), S, R);
      if (PTCompare(S.SON[OpQuad(Q)].pnt, R.pnt) != Q) {
	F = S.SON[OpQuad(Q)];
	S.SON[OpQuad(Q)] = null;
	InsertQuadrant(F, R);
      } else {
	NewRoot(Q, S.SON[OpQuad(Q)], R, S);
      }
    }
  }

  private void InsertQuadrant(PTNode src, PTNode dest) {
    if (src == null)
      return;
    for (int i = 0; i < NRDIRS; i++) {
      InsertQuadrant(src.SON[i], dest);
    }
    for (int i = 0; i < NRDIRS; i++) {
      src.SON[i] = null;
    }
    insert(src, dest);  /* dest always non-null */
  }


  private void drawC(PTNode r, DrawingTarget g, double minx, double miny, double maxx, double maxy) {
    if (!g.visible(new DRectangle(minx, miny, maxx - minx, maxy - miny)))
      return;

    g.setColor(Color.black);
    g.drawRect(minx, miny, maxx - minx, maxy - miny);

    if (r == null) {
      return;
    }
 
    if (r.SON[0] != null || r.SON[1] != null || r.SON[2] != null || r.SON[3] != null) {
      drawC(r.SON[0], g, minx, r.pnt.y, r.pnt.x, maxy);
      drawC(r.SON[1], g, r.pnt.x, r.pnt.y, maxx, maxy);
      drawC(r.SON[2], g, minx, miny, r.pnt.x, r.pnt.y);
      drawC(r.SON[3], g, r.pnt.x, miny, maxx, r.pnt.y);
    }

    g.setColor(Color.red);
    r.pnt.draw(g);

  }

  // ----------------------- Incremental Nearest -----------------

  class PTQueueElement {
    double key;
    DPoint pnt;
    PTNode r;
    DRectangle box;
    boolean isElem;

    PTQueueElement(double k, PTNode p) {
      key = k;	
      pnt = p.pnt;
      isElem = true;
    }

    PTQueueElement(double k, PTNode p, DRectangle b) {
      key = k;	
      r = p;
      box = b;
      isElem = false;
    }
  }

  class PTQueue {
    Vector v;

    PTQueue() {
      v = new Vector();
    }

    void Enqueue(PTQueueElement qe) {
      v.addElement(qe);
      for (int i = v.size() - 1; i > 0; i--) {
	PTQueueElement q1 = (PTQueueElement)v.elementAt(i - 1);
	PTQueueElement q2 = (PTQueueElement)v.elementAt(i);
	if (q1.key > q2.key) {
	  v.setElementAt(q2, i - 1);
	  v.setElementAt(q1, i);
	}
      }
    }

    PTQueueElement Dequeue() {
      PTQueueElement q = (PTQueueElement)v.elementAt(0);
      v.removeElementAt(0);
      return q;
    }
  
    boolean isEmpty() {
      return (v.size() == 0);
    }

    Vector makeVector() {
      Vector r = new Vector();
      for (int i = 0; i < v.size(); i++) {
	PTQueueElement q = (PTQueueElement)v.elementAt(i);
	if (q.isElem)
	  r.addElement(new GreenPoints(q.pnt));
	else
	  r.addElement(new QueueBlock(q.box));
      }
      return r;
    }

  }


  class PTIncNearest {
    PTQueue q;

    PTIncNearest(PTNode rt) {
      q = new PTQueue();
      q.Enqueue(new PTQueueElement(0, rt, wholeCanvas));
    }

    DPoint Query(QueryObject qu) {
      DPoint[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
      return (ar.length == 0) ? null : ar[0];
    }

      void Query(QueryObject qu, SearchVector v) {
          Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
      }

      DPoint[] Query(QueryObject qu, double dist) {
          return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
      }

    DPoint[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
	Vector pts = new Vector();
      DRectangle b;
      int counter = 1;

      while(!q.isEmpty()) {
	PTQueueElement e = q.Dequeue();

	if (e.isElem) {
	    if (nrelems-- <= 0 || qu.distance(e.pnt) > dist)
		break;
	    pts.addElement(e.pnt);
	    ret.addElement(new NNElement(new NNDrawable(e.pnt, counter++), e.key, q.makeVector()));
	} else {
	  ret.addElement(new NNElement(new YellowBlock(e.box, false), e.key, q.makeVector()));

	  q.Enqueue(new PTQueueElement(qu.distance(e.r.pnt), e.r));

	  if (e.r.SON[0] != null) {
	    b = new DRectangle(e.box.x, e.r.pnt.y, e.r.pnt.x - e.box.x, e.box.y + e.box.height - e.r.pnt.y);
	    q.Enqueue(new PTQueueElement(qu.distance(b), e.r.SON[0], b));
	  }
	  if (e.r.SON[1] != null) {
	    b = new DRectangle(e.r.pnt.x, e.r.pnt.y, e.box.x + e.box.width - e.r.pnt.x, e.box.y + e.box.height - e.r.pnt.y);
	    q.Enqueue(new PTQueueElement(qu.distance(b), e.r.SON[1], b));
	  }
	  if (e.r.SON[2] != null) {
	    b = new DRectangle(e.box.x, e.box.y, e.r.pnt.x - e.box.x, e.r.pnt.y - e.box.y);
	    q.Enqueue(new PTQueueElement(qu.distance(b), e.r.SON[2], b)); 
	  }
	  if (e.r.SON[3] != null) {
	    b = new DRectangle(e.r.pnt.x, e.box.y, e.box.x + e.box.width - e.r.pnt.x, e.r.pnt.y - e.box.y);
	    q.Enqueue(new PTQueueElement(qu.distance(b), e.r.SON[3], b));
	  }

	}
      }
      DPoint[] ar = new DPoint[pts.size()];
      pts.copyInto(ar);
      return ar;
    }
  }
  
} /* class */

 

