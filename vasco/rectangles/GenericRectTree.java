package vasco.rectangles;
/* $Id: GenericRectTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import java.util.*;
import java.awt.*;
import vasco.drawable.*;
// --------- Rect Quadtree ---------

abstract class GenericRectTree extends RectangleStructure implements BucketIface, MaxDecompIface {

  RNode ROOT;
  int maxBucketSize;
    int maxDecomp;

  final double xf[] = {0, 0.5, 0, 0.5};
  final double yf[] = {0.5, 0.5, 0, 0};

  public GenericRectTree(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
    super(can, p, r);
    ROOT = new RNode();
    maxDecomp = md;
    maxBucketSize = bs;
  }

  public void Clear() {
      super.Clear();
    ROOT = new RNode();
  }

  public void reInit(Choice c) {
    super.reInit(c);
    new MaxDecomp(topInterface, 9, this);
  }

  public boolean Insert(DRectangle toIns) {

    /* check for intersection somehow */
    boolean res;
    res = localInsert(ROOT, toIns, wholeCanvas, maxDecomp);
    if (!res)
      localDelete(toIns, ROOT, wholeCanvas);
    return res;
  }

  public void Delete(DPoint toDel) {
    if (ROOT != null) {
      RectIncNearest kdin = new RectIncNearest(ROOT);
      DRectangle mx = kdin.Query(new QueryObject(toDel));
      if (mx != null)
	localDelete(mx, ROOT, wholeCanvas);
    }
  }

  public void DeleteDirect(Drawable d) {
    if (ROOT != null && d != null) {
	localDelete((DRectangle)d, ROOT, wholeCanvas);
    }
  }

  public SearchVector Search (QueryObject r, int mode) {
    searchVector = new Vector();
    SearchVector sv = new SearchVector();
    processedRectangles = new Vector();
    localSearch(ROOT, r, wholeCanvas, sv, mode);
    //    System.out.println("Found " + g.size() + " rectangle(s)");
    return sv;
  }

  public SearchVector Nearest(QueryObject p) {
    SearchVector v = new SearchVector();
    RectIncNearest mxin = new RectIncNearest(ROOT);
    mxin.Query(p, v);
    return v;
  }

  public SearchVector Nearest(QueryObject p, double dist) {
    SearchVector v = new SearchVector();
    RectIncNearest mxin = new RectIncNearest(ROOT);
    mxin.Query(p, v, dist, Integer.MAX_VALUE);
    return v;
  }


  public Drawable NearestFirst(QueryObject p) {
    RectIncNearest mxin = new RectIncNearest(ROOT);
    return mxin.Query(p);
  }

    
  public Drawable[] NearestRange(QueryObject p, double dist) {
    RectIncNearest mxin = new RectIncNearest(ROOT);
    return mxin.Query(p, dist);
  }

  public void drawContents(DrawingTarget gg, Rectangle view) {
    drawC(ROOT, gg, wholeCanvas, view);
  }

  /* ---------------- interface implementation ---------- */

  public int getMaxDecomp() {
    return maxDecomp;
  }

  public void setMaxDecomp(int b) {
    maxDecomp = b;
    reb.rebuild();
  }

  public int getBucket() {
    return maxBucketSize;
  }

  public void setBucket(int b) {
    maxBucketSize = b;
    reb.rebuild();
  }

 abstract  boolean localInsert(RNode q, DRectangle r, DRectangle block, int md);


  void localDelete(DRectangle todel, RNode r, DRectangle block) {
    if (r.NODETYPE == BLACK) {
      r.deleteRect(todel);
      if (r.r.size() == 0)
	r.NODETYPE = WHITE;
      return;
    }
    // do nothing if white (happens only in case of empty tree)
    if (r.NODETYPE == GRAY) {
      for (int i=0; i < 4; i++) {
	DRectangle dr = new DRectangle(block.x + xf[i] * block.width,
				       block.y + yf[i] * block.height,
				       block.width / 2, block.height / 2);
	if (todel.intersects(dr)) {
	  //	  System.out.println("deleting from :" + i);
	  localDelete(todel, r.son[i], dr);
	}
      }

      Vector children = new Vector();
      for (int i = 0; i < 4; i++) {
	if (r.son[i].NODETYPE == GRAY)
	  return;
	for (int j = 0; j < r.son[i].r.size(); j++)
	  if (!children.contains(r.son[i].r.elementAt(j)))
	    children.addElement(r.son[i].r.elementAt(j));
      }
      if (children.size() <= maxBucketSize) {
	r.son[0] = r.son[1] = r.son[2] = r.son[3] = null;
	if (children.size() ==  0) 
	  r.NODETYPE = WHITE;
	else {
	  r.NODETYPE = BLACK;
	  r.r = children;
	}
      }
    }
  }

  // ----- SEARCH ------

  Vector searchVector;
  boolean overlaps;
  Vector processedRectangles;

  void localSearch(RNode r, QueryObject newrect, DRectangle block, SearchVector v, int mode) {
    v.addElement(new SVElement(new YellowBlock(block, r.NODETYPE != GRAY), searchVector));

    switch (r.NODETYPE) {
    case BLACK:
      int i;
      for (int j = 0; j < r.r.size(); j++) {
	for (i = 0; i < processedRectangles.size(); i++)
	  if ( ((DRectangle)(processedRectangles.elementAt(i))).equals(r.r.elementAt(j)) ) 
	       // was reported already?
	    break;
	if (i == processedRectangles.size()) {
	  drawableInOut(newrect, (DRectangle)r.r.elementAt(j), mode, v, searchVector);
	  processedRectangles.addElement(r.r.elementAt(j));
	}
      }
      break;
    case GRAY:
      for (i=3; i >= 0; i--) {
	DRectangle dr = new DRectangle(block.x + xf[i] * block.width,
				       block.y + yf[i] * block.height,
				       block.width / 2, block.height / 2);
	if (newrect.intersects(dr))
	  searchVector.addElement(dr);
      }
      for (i=0; i < 4; i++) {
	DRectangle dr = new DRectangle(block.x + xf[i] * block.width,
				       block.y + yf[i] * block.height,
				       block.width / 2, block.height / 2);
	if (newrect.intersects(dr)) {
	  searchVector.removeElementAt(searchVector.size() - 1);
	  localSearch(r.son[i], newrect, dr, v, mode);
	}
      }
      break;
    }
  }


  void drawC(RNode r, DrawingTarget g, DRectangle block, Rectangle view) {
    if (!g.visible(block))
      return;

    if (r == null)
      return;


    g.setColor(Color.black);
    block.draw(g);

    g.setColor(Color.red);
    for (int i = 0; i < r.r.size(); i++) {
      DRectangle rect = (DRectangle)r.r.elementAt(i);
      rect.draw(g);
    }
    for (int i = 0; i < 4; i++) {
      drawC(r.son[i], g, new DRectangle(block.x + xf[i] * block.width, block.y + yf[i] * block.height, 
					block.width / 2, block.height / 2), view);
    }
  }


  // -----------------------------------------
  class RectIncNearest {

    class RectQueueElement {
      double[] keys;

      RectQueueElement(double[] k) {
	keys = k;
      }
    }


    class RectQLeaf extends RectQueueElement {
      DRectangle rect;

      RectQLeaf(double[] k, DRectangle p) {
	super(k);
	rect = p;
      }
    }
    
    class RectQINode extends RectQueueElement {
      RNode r;
      DRectangle block;

      RectQINode(double[] k, RNode p, DRectangle b) {
	super(k);
	r = p;
	block = b;
      }
    }

    class RectQueue {


      Vector v;

      RectQueue() {
	v = new Vector();
      }

      void Enqueue(RectQueueElement qe) {
	v.addElement(qe);
	for (int i = v.size() - 1; i > 0; i--) {
	  RectQueueElement q1 = (RectQueueElement)v.elementAt(i - 1);
	  RectQueueElement q2 = (RectQueueElement)v.elementAt(i);

	  if (q1.keys[0] == q2.keys[0] && q1.keys[1] == q2.keys[1] && 
	      q1 instanceof RectQLeaf && q2 instanceof RectQLeaf) {
	      RectQLeaf ql1 = (RectQLeaf)q1;
	      RectQLeaf ql2 = (RectQLeaf)q2;
	      if (ql1.rect.x > ql2.rect.x ||
		  (ql1.rect.x == ql2.rect.x && ql1.rect.y > ql2.rect.y) ||
		  (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y &&
		   ql1.rect.width > ql2.rect.width) ||
		  (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y &&
		   ql1.rect.width == ql2.rect.width && ql1.rect.height > ql2.rect.height)) {
		  v.setElementAt(q2, i - 1);
		  v.setElementAt(q1, i);
	      }
	  }

	  if (q1.keys[0] > q2.keys[0] || 
	      (q1.keys[0] == q2.keys[0] && 
	       ((q1.keys[1] > q2.keys[1] && q1 instanceof RectQLeaf && q2 instanceof RectQLeaf) || 
		(q1 instanceof RectQLeaf && !(q2 instanceof RectQLeaf))))) {
	      v.setElementAt(q2, i - 1);
	      v.setElementAt(q1, i);
	  }

	  /*
	  if (q1.key[0] == q2.key[0] &&) {
	      if (q1 instanceof RectQLeaf && q2 instanceof RectQINode) {
		  v.setElementAt(q2, i - 1);
		  v.setElementAt(q1, i);
	      } else if (q1 instanceof RectQLeaf && q2 instanceof RectQLeaf)  {
		  RectQLeaf ql1 = (RectQLeaf)q1;
		  RectQLeaf ql2 = (RectQLeaf)q2;
		  if (ql1.rect.x > ql2.rect.x ||
		      (ql1.rect.x == ql2.rect.x && ql1.rect.y > ql2.rect.y) ||
		      (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y &&
		       ql1.rect.width > ql2.rect.width) ||
		      (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y &&
		       ql1.rect.width == ql2.rect.width && ql1.rect.height > ql2.rect.height)) {
		      v.setElementAt(q2, i - 1);
		      v.setElementAt(q1, i);
		  }
	      }
	  }
	  if (q1.key > q2.key) {
	      v.setElementAt(q2, i - 1);
	      v.setElementAt(q1, i);
	  }
	  */
	}
      }

      RectQueueElement First() {
	RectQueueElement q = (RectQueueElement)v.elementAt(0);
	return q;
      }

      void DeleteFirst() {
	v.removeElementAt(0);
      }

      RectQueueElement Dequeue() {
	RectQueueElement q = (RectQueueElement)v.elementAt(0);
	v.removeElementAt(0);
	return q;
      }
  
      boolean isEmpty() {
	return (v.size() == 0);
      }

      Vector makeVector() {
	Vector r = new Vector();
	for (int i = 0; i < v.size(); i++) {
	  RectQueueElement q = (RectQueueElement)v.elementAt(i);
	  if (q instanceof RectQLeaf)
	    r.addElement(new GreenRect(((RectQLeaf)q).rect));
	  else
	    r.addElement(new QueueBlock(((RectQINode)q).block));
	}
	return r;
      }
    }

      RectQueue q;

      RectIncNearest(RNode rt) {
	  q = new RectQueue();
	  double[] zero = {0,0};
	  q.Enqueue(new RectQINode(zero, rt, wholeCanvas));
      }

      DRectangle Query(QueryObject qu) {
	  DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
	  return (ar.length == 0) ? null : ar[0];
      }
      
      void Query(QueryObject qu, SearchVector v) {
	  Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
      }

      DRectangle[] Query(QueryObject qu, double dist) {
	  return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
      }	

      private DRectangle[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
	  Vector rect = new Vector();
	  int counter = 1;

	  while(!q.isEmpty()) {
	      RectQueueElement element = q.Dequeue();

	      if (element instanceof RectQLeaf) {
		  RectQLeaf ql = (RectQLeaf)element;
		  while (!q.isEmpty() && 
			 q.First() instanceof RectQLeaf && 
			 ql.rect.equals(((RectQLeaf)q.First()).rect))
		      q.DeleteFirst();
		  if (nrelems-- <= 0 || qu.distance(ql.rect) > dist)
		      break;
		  rect.addElement(ql.rect);
		  ret.addElement(new NNElement(new NNDrawable(ql.rect, counter++), 
					       ql.keys[0], q.makeVector()));
	      } else {
		  RectQINode ql = (RectQINode)element;
		  ret.addElement(new NNElement(new YellowBlock(ql.block, false), 
					       ql.keys[0], q.makeVector()));
		  if (ql.r.NODETYPE == BLACK) {
		      for (int i = 0; i < ql.r.r.size(); i++) {
			  DRectangle a = (DRectangle)ql.r.r.elementAt(i);
			  if (qu.distance(a) >= qu.distance(ql.block))
			      q.Enqueue(new RectQLeaf(qu.distance(a, new double[2]), a));
		      }
		  } else if (ql.r.NODETYPE == GRAY) {
		      for (int i = 0; i < 4; i++) 
			  if (ql.r.son[i] != null) {
			      DRectangle n = new DRectangle(ql.block.x + xf[i] * ql.block.width,
							    ql.block.y + yf[i] * ql.block.height,
							    ql.block.width / 2, ql.block.height / 2);
			      q.Enqueue(new RectQINode(qu.distance(n, new double[2]), ql.r.son[i], n));
			  }
		  }
	      }
	  }
	  DRectangle[] ar = new DRectangle[rect.size()];
	  rect.copyInto(ar);
	  return ar;
      }
  }
}






