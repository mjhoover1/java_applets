package vasco.points;
/* $Id: GenericPRkdbucket.java,v 1.2 2007/10/28 15:38:17 jagan Exp $ */
import java.awt.Choice;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;

import vasco.common.BucketIface;
import vasco.common.CommonConstants;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.MaxDecomp;
import vasco.common.MaxDecompIface;
import vasco.common.NNElement;
import vasco.common.QueryObject;
import vasco.common.QueueBlock;
import vasco.common.RebuildTree;
import vasco.common.SVElement;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.common.YellowBlock;
import vasco.drawable.Drawable;
import vasco.drawable.NNDrawable;

public abstract class GenericPRkdbucket extends PointStructure implements MaxDecompIface, BucketIface {

  class PRkdbucketNode implements CommonConstants {
    int nodetype;
    Vector points;
    PRkdbucketNode left, right;

    PRkdbucketNode(int type) {
      nodetype = type;
      points = new Vector();
      left = right = null;
    }

    boolean isIn(DPoint p) {
      for (int i = 0; i < points.size(); i++) {
	DPoint s = (DPoint)points.elementAt(i);
	if (s.equals(p))
	  return true;
      }
      return false;
    }
 
    void addPoint(DPoint p) {
      if (!isIn(p))
	points.addElement(p);
    }

    void deletePoint(DPoint p) {
      for (int i = 0; i < points.size(); i++) {
	DPoint s = (DPoint)points.elementAt(i);
	if (s.equals(p)) {
	  nodetype = WHITE;
	  points.removeElementAt(i);
	  return;
	}
      }
    }
  }


  PRkdbucketNode ROOT;
  int maxBucketSize;
  int maxDecomp;

  public GenericPRkdbucket(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
    super(can, p, r);
    ROOT = null;
    maxBucketSize = b;
    maxDecomp = md;
  }

  public void reInit(Choice ao) {
    super.reInit(ao);
    new MaxDecomp(topInterface, 18, this);
    ao.addItem("Nearest");
    ao.addItem("Within");
  }

  public void Clear() {
    super.Clear();
    ROOT = null;
  }

  public abstract String getName();

  public boolean Insert(DPoint qu) {
    boolean res = true;
    if (ROOT == null) {
      ROOT = new PRkdbucketNode(WHITE);
      ROOT.addPoint(qu);
    } else
      res = insert(qu, ROOT, true, wholeCanvas.x + wholeCanvas.width / 2, 
		   wholeCanvas.y + wholeCanvas.height / 2, 
		   wholeCanvas.width, wholeCanvas.height, maxDecomp);
    if (!res)
      Delete(qu);
    return res;
  }

  public void Delete(DPoint qu) {
    if (ROOT == null) 
      return;

    PRkdbucketIncNearest prin = new PRkdbucketIncNearest(ROOT);
    DPoint p = prin.Query(new QueryObject(qu));
    if (p != null) {
      if (ROOT.isIn(p)) {
	ROOT.deletePoint(p);
	if (ROOT.points.size() == 0)
	  ROOT = null;
      } else
	delete(p, ROOT, true, wholeCanvas.x + wholeCanvas.width / 2, 
	       wholeCanvas.y + wholeCanvas.height / 2, 
	       wholeCanvas.width, wholeCanvas.height);
    }
  }

    public void DeleteDirect(Drawable d) {
	DPoint p = (DPoint)d;
	if (p != null) {
	    if (ROOT.isIn(p)) {
		ROOT.deletePoint(p);
		if (ROOT.points.size() == 0)
		    ROOT = null;
	    } else
		delete(p, ROOT, true, wholeCanvas.x + wholeCanvas.width / 2, 
		      wholeCanvas.y + wholeCanvas.height / 2,
		      wholeCanvas.width, wholeCanvas.height);
	}
    }

  public SearchVector Search(QueryObject query, int mode) {
    SearchVector res = new SearchVector();
    searchVector = new Vector();
    search(ROOT, query, true, wholeCanvas, mode, res);
    return res;
  }

  public Drawable NearestFirst(QueryObject p) {
    if (ROOT == null) 
      return null;
    PRkdbucketIncNearest prbuckin = new PRkdbucketIncNearest(ROOT);
    return  prbuckin.Query(p);
  }

  public SearchVector Nearest(QueryObject p) {
      SearchVector v = new SearchVector();
      if (ROOT != null) {
	  PRkdbucketIncNearest prbuckin = new PRkdbucketIncNearest(ROOT);
	  prbuckin.Query(p, v);
      }
      return v;
  }

  public SearchVector Nearest(QueryObject p, double dist) {
      SearchVector v = new SearchVector();
      if (ROOT != null) {
	  PRkdbucketIncNearest prbuckin = new PRkdbucketIncNearest(ROOT);
	  prbuckin.Query(p, v, dist, Integer.MAX_VALUE);
      }
      return v;
  }

  public Drawable[] NearestRange(QueryObject p, double dist) {
      PRkdbucketIncNearest near = new PRkdbucketIncNearest(ROOT);
      return near.Query(p, dist);
  }

  public void drawContents(DrawingTarget g, Rectangle view) {
    if (ROOT != null)
      drawC(ROOT, g, true, wholeCanvas.x + wholeCanvas.width / 2, 
	    wholeCanvas.y + wholeCanvas.height / 2, 
	    wholeCanvas.width, wholeCanvas.height);
  }

  /* ---- interface implementation ------ */

  public int getBucket() {
    return maxBucketSize;
  }

  public void setBucket(int b) {
    maxBucketSize = b;
    reb.rebuild();
  }

  public int getMaxDecomp() {
    return maxDecomp;
  }

  public void setMaxDecomp(int b) {
    maxDecomp = b;
    reb.rebuild();
  }


  /* ----------------- private methods ------------------ */

  Vector searchVector;


  private void search(PRkdbucketNode r, QueryObject query, boolean xcoord, DRectangle block, int mode, SearchVector res) {
    res.addElement(new SVElement(new YellowBlock(block, r == null || r.nodetype != GRAY), searchVector));

    if (r == null) 
      return;

    if (r.nodetype != GRAY) {
      res.addElement(new SVElement(new GreenPoints(r.points), searchVector));

      for (int i = 0; i < r.points.size(); i++) {
	DPoint p = (DPoint)r.points.elementAt(i);
	drawableInOut(query, p, mode, res, searchVector);
      }

      return;
    }

    if (xcoord) {
      DRectangle west = new DRectangle(block.x, block.y, block.width / 2, block.height);
      DRectangle east = new DRectangle(block.x + block.width / 2, block.y, block.width / 2, block.height);
      if (!query.intersects(west))
	search(r.right, query, !xcoord, east, mode, res);
      else if (!query.intersects(east))
	search(r.left, query, !xcoord, west, mode, res);
      else {
	searchVector.addElement(east);
	search(r.left, query, !xcoord, west, mode, res);
	searchVector.removeElementAt(searchVector.size() - 1);
	search(r.right, query, !xcoord, east, mode, res);
      }
    } else {
      DRectangle north = new DRectangle(block.x, block.y + block.height / 2, block.width, block.height / 2);
      DRectangle south = new DRectangle(block.x, block.y, block.width, block.height / 2);
      if (!query.intersects(south))
	search(r.right, query, !xcoord, north, mode, res);
      else if (!query.intersects(north))
	search(r.left, query, !xcoord, south,  mode, res);
      else {
	searchVector.addElement(north);
	search(r.left, query, !xcoord, south, mode, res);
	searchVector.removeElementAt(searchVector.size() - 1);
	search(r.right, query, !xcoord, north, mode, res);
      }
    }
  }




  abstract boolean insert (DPoint p, PRkdbucketNode r, boolean xcoord, double cx, double cy, 
			   double sx, double sy, int md);


  private void delete(DPoint del, PRkdbucketNode r, boolean xcoord, 
		      double cx, double cy, double sx, double sy) {
    if (xcoord) {
      if (del.x > cx) {
	if (r.right.isIn(del))
	  r.right.deletePoint(del);
	else
	  delete(del, r.right, !xcoord, cx + sx / 4, cy, sx / 2, sy);
      } else {
	if (r.left.isIn(del))
	  r.left.deletePoint(del);
	else
	  delete(del, r.left, !xcoord, cx - sx / 4, cy, sx / 2, sy);
      }
    } else { // y coordinate
      if (del.y > cy) {
	if (r.right.isIn(del))
	  r.right.deletePoint(del);
	else
	  delete(del, r.right, !xcoord, cx, cy + sy / 4, sx, sy / 2);
      }	else {
	if (r.left.isIn(del)) 
	  r.left.deletePoint(del);
	else
	  delete(del, r.left, !xcoord, cx, cy - sy / 4, sx, sy / 2);
      }
    }

    if (r.left.nodetype != GRAY && r.right.nodetype != GRAY &&
	r.left.points.size() + r.right.points.size() <= maxBucketSize) {
      r.points = new Vector();
      r.nodetype = (r.left.points.size() + r.right.points.size() == maxBucketSize) ? 
	BLACK : WHITE;
      for (int i = 0; i < r.left.points.size(); i++) {
	DPoint p = (DPoint)r.left.points.elementAt(i);
	r.points.addElement(p);
      }
      for (int i = 0; i < r.right.points.size(); i++) {
	DPoint p = (DPoint)r.right.points.elementAt(i);
	r.points.addElement(p);
      }
      r.left = r.right = null;
    }

  }


  private void drawC(PRkdbucketNode r, DrawingTarget g, boolean xcoord, double cx, double cy, 
		     double sx, double sy) {
    if (!g.visible(new DRectangle(cx - sx / 2, cy - sy / 2, sx, sy)))
      return;

    /*    if (r == null) {
	  return;
	  }
	  */
    if (r.nodetype != GRAY) {
      g.setColor(Color.red);
      for (int i = 0; i < r.points.size(); i++) {
	DPoint p = (DPoint)r.points.elementAt(i);
	p.draw(g);
      } 
    } else {
      g.setColor(Color.black);
      if (xcoord) 
	g.drawLine(cx, cy - sy / 2, cx, cy + sy / 2);
      else
	g.drawLine(cx - sx / 2, cy, cx + sx / 2, cy);

      if (xcoord) {
	drawC(r.left, g, !xcoord, cx - sx/4, cy, sx / 2, sy);
	drawC(r.right, g, !xcoord, cx + sx/4, cy, sx / 2, sy);
      } else {
	drawC(r.left, g, !xcoord, cx, cy - sy/4, sx, sy/2);
	drawC(r.right, g, !xcoord, cx, cy + sy/4, sx, sy/2);
      }
    }
  }

  // ----------------------- Incremental Nearest -----------------


  class PRkdbucketQueueElement {
    double key;
    PRkdbucketNode r;
    DPoint pnt;
    DRectangle block;
    boolean xcoord;
    boolean isElem;

    PRkdbucketQueueElement(double k, DPoint p) {
      key = k;	
      pnt = p;
      isElem = true;
    }

    PRkdbucketQueueElement(double k, PRkdbucketNode p, DRectangle b, boolean coor) {
      key = k;	
      r = p;
      block = b;
      isElem = false;
      xcoord = coor;
    }
  }

  class PRkdbucketQueue {
    Vector v;

    PRkdbucketQueue() {
      v = new Vector();
    }

    void Enqueue(PRkdbucketQueueElement qe) {
      v.addElement(qe);
      for (int i = v.size() - 1; i > 0; i--) {
	PRkdbucketQueueElement q1 = (PRkdbucketQueueElement)v.elementAt(i - 1);
	PRkdbucketQueueElement q2 = (PRkdbucketQueueElement)v.elementAt(i);
	if (q1.key > q2.key) {
	  v.setElementAt(q2, i - 1);
	  v.setElementAt(q1, i);
	}
      }
    }

    PRkdbucketQueueElement Dequeue() {
      PRkdbucketQueueElement q = (PRkdbucketQueueElement)v.elementAt(0);
      v.removeElementAt(0);
      return q;
    }
  
    boolean isEmpty() {
      return (v.size() == 0);
    }

    Vector makeVector() {
      Vector r = new Vector();
      for (int i = 0; i < v.size(); i++) {
	PRkdbucketQueueElement q = (PRkdbucketQueueElement)v.elementAt(i);
	if (q.isElem)
	  r.addElement(new GreenPoints(q.pnt));
	else
	  r.addElement(new QueueBlock(q.block));
      }
      return r;
    }

  }


  class PRkdbucketIncNearest {
    PRkdbucketQueue q;

    PRkdbucketIncNearest(PRkdbucketNode rt) {
      q = new PRkdbucketQueue();
      q.Enqueue(new PRkdbucketQueueElement(0, rt, wholeCanvas, true));
    }

    //   DPoint Query(DPoint qu) {
    //     while(!q.isEmpty()) {
    //       PRkdbucketQueueElement element = q.Dequeue();

    //       if (element.isElem) {
    // 	return element.pnt;
    // 	// return element here
    //       } else if (element.r.nodetype != GRAY) {
    //         for (int i = 0; i < element.r.points.size(); i++) {
    //           DPoint pt = (DPoint)element.r.points.elementAt(i);
    // 	if (qu.distance(pt) >= qu.distance(element.block))
    // 	  q.Enqueue(new PRkdbucketQueueElement(qu.distance(pt), pt));
    // 	}
    //       } else if (element.r.nodetype == GRAY) {
    // 	if (element.xcoord) {
    // 	  if (element.r.left != null) {
    // 	    DRectangle dr = new DRectangle(element.block.x, element.block.y, 
    // 					   element.block.width / 2, element.block.height);
    // 	    q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), 
    // 						 element.r.left, dr, !element.xcoord));
    // 	  }
    // 	  if (element.r.right != null) {
    // 	    DRectangle dr = new DRectangle(element.block.x + element.block.width / 2, 
    // 					   element.block.y, 
    // 					   element.block.width / 2, element.block.height);
    // 	    q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), element.r.right, dr, !element.xcoord));
    // 	  }
    // 	} else { // ycoord
    // 	  if (element.r.left != null) {
    // 	    DRectangle dr = new DRectangle(element.block.x, element.block.y, 
    // 					   element.block.width, element.block.height / 2);
    // 	    q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), element.r.left, dr, !element.xcoord));
    // 	  }
    // 	  if (element.r.right != null) {
    // 	    DRectangle dr = new DRectangle(element.block.x, 
    // 					   element.block.y + element.block.height / 2, 
    // 					   element.block.width, element.block.height / 2);
    // 	    q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), element.r.right, !element.xcoord));
    // 	  }
    // 	}
    //        }
    //     }
    //     return null;
    //   }

    //---------------------------------------------
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
	  int counter = 1;
	  while(!q.isEmpty()) {
	      PRkdbucketQueueElement element = q.Dequeue();

	      if (element.isElem) {
		  if (nrelems-- <= 0 || qu.distance(element.pnt) > dist)
		      break;
		  pts.addElement(element.pnt);
		  ret.addElement(new NNElement(new NNDrawable(element.pnt, counter++), element.key, 
					       q.makeVector()));
	      } else {
		  ret.addElement(new NNElement(new YellowBlock(element.block, false), element.key, 
					       q.makeVector()));

		  if (element.r == null) {
		      // noop
		  } else if (element.r.nodetype != GRAY) {
		      for (int i = 0; i < element.r.points.size(); i++) {
			  DPoint pt = (DPoint)element.r.points.elementAt(i);
			  if (qu.distance(pt) >= qu.distance(element.block))
			      q.Enqueue(new PRkdbucketQueueElement(qu.distance(pt), pt));
		      }
		  } else if (element.r.nodetype == GRAY) {
		      DRectangle dr;
		      if (element.xcoord) {
			  //	    if (element.r.left != null) {
			  dr = new DRectangle(element.block.x, element.block.y, 
					      element.block.width / 2, 
					      element.block.height);
			  q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), 
							       element.r.left, dr, 
							       !element.xcoord));
			  //	    }
			  //	    if (element.r.right != null) {
			  dr = new DRectangle(element.block.x + element.block.width / 2, 
					      element.block.y, 
					      element.block.width / 2, element.block.height);
			  q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), element.r.right, dr, !element.xcoord));
			  //	    }
		      } else { // ycoord
			  //	    if (element.r.left != null) {
			  dr = new DRectangle(element.block.x, element.block.y, 
					      element.block.width, element.block.height / 2);
			  q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), element.r.left, dr, !element.xcoord));
			  //	    }
			  //	    if (element.r.right != null) {
			  dr = new DRectangle(element.block.x, 
					      element.block.y + element.block.height / 2, 
					      element.block.width, element.block.height / 2);
			  q.Enqueue(new PRkdbucketQueueElement(qu.distance(dr), element.r.right, dr, !element.xcoord));
			  //	    }
		      }

		  }
	      }
	  }
	  DPoint[] ar = new DPoint[pts.size()];
	  pts.copyInto(ar);
	  return ar;
      }
  }
  



}

