package vasco.points;
import java.awt.Color;
import java.awt.Rectangle;
/* $Id: RangeTree.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
// import java.awt.Choice;
// import java.awt.Color;
// import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.JComboBox;

import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.QSortAlgorithm;
import vasco.common.QueryObject;
import vasco.common.RebuildTree;
import vasco.common.SVElement;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.common.YellowBlock;
import vasco.drawable.Drawable;

public class RangeTree extends PointStructure {

  class Node1d {
    Node1d LEFT, RIGHT;
    double midrange;
    DPoint point;

    Node1d() {
      LEFT = RIGHT = null;
      point = null;
    }
  }

  class Node2d {
    Node1d range_tree;
    double midrange;
    Node2d LEFT, RIGHT;
    DPoint point;

    Node2d() {
      range_tree = null;
      LEFT = RIGHT = null;
      point = null;
    }
  }


  Node2d ROOT;
  Vector pts;

  public RangeTree(DRectangle can, TopInterface p, RebuildTree r) {
    super(can, p, r);
  }

  public void reInit(JComboBox ao) {
    super.reInit(ao);
  }

    public boolean orderDependent() {
        return false;
    }

  public void Clear() {
    super.Clear();
    ROOT = null;
    pts = new Vector();
  }

  public void MessageEnd() {
    ROOT = createTrees(pts);
    super.MessageEnd();
  }

  public boolean Insert(DPoint p) {
    pts.addElement(p);
    return true;
  }

  public void Delete(DPoint qu) {
      Drawable d = NearestFirst(new QueryObject(qu));
      if (d != null)
	  pts.removeElement(d);
  }

    public void DeleteDirect(Drawable d) {
	for (int i = 0; i < pts.size(); i++) 
	    if (((DPoint)pts.elementAt(i)).equals((DPoint) d)) {
		pts.removeElementAt(i);
		return;
	    }
    }

  public Drawable NearestFirst(QueryObject qu) {
      if (pts.size() == 0)
	  return null;

      DPoint min = (DPoint)pts.elementAt(0);
      double dist = qu.distance(min);

      for (int j = 1; j < pts.size(); j++) {
	  DPoint p = (DPoint)pts.elementAt(j);
	  double d = qu.distance(p);
	  if (d < dist) {
	      dist = d;
	      min = p;
	  }
      }
      return min;
  }

  public SearchVector Nearest(QueryObject p) {
      System.err.println("Range Tree: Nearest Range not available");
    return null;
  }

  public SearchVector Nearest(QueryObject p, double dist) {
      System.err.println("Range Tree: Nearest not available");
    return null;
  }

  public Drawable[] NearestRange(QueryObject p, double dist) {
      System.err.println("Range Tree: Nearest Range not available");
      return null;
  }

  public void drawContents(DrawingTarget g, Rectangle view) {
    if (ROOT != null)
      drawVerticalLines(ROOT, 0, g, wholeCanvas.x, wholeCanvas.x + wholeCanvas.width);
    for (int i = 0; i < pts.size(); ++i) {
      g.setColor(Color.red);
      DPoint p = (DPoint)pts.elementAt(i);
      p.draw(g);
    }
  }

  public String getName() {
    return "2d Range Tree";
  }

  //-------------------------------------

  void drawVerticalLines(Node2d r, int label, DrawingTarget g, double left, double right) {
    if (!g.visible(new DRectangle(left, wholeCanvas.y, right, wholeCanvas.y + wholeCanvas.height)))
       return;

     if (r.point != null || label > 3)
        return;
     for (int i = 0; i < 4 - label; i++) {
       g.setColor(Color.black);
       g.drawLine(r.midrange - i, wholeCanvas.y, r.midrange - i, wholeCanvas.y + wholeCanvas.height);
       g.drawLine(r.midrange + i, wholeCanvas.y, r.midrange + i, wholeCanvas.y + wholeCanvas.height);
     }
     //     drawHorizontalLines(r.range_tree, 4, g, left, right);
     g.setColor(Color.black);
     g.drawString(String.valueOf(label), r.midrange-10, 20);
     drawVerticalLines(r.LEFT, label + 1, g, left, r.midrange);
     drawVerticalLines(r.RIGHT, label + 1, g, r.midrange, right);
  }

   void Gather(Node2d r, Vector arr) {
    if (r.point != null) {
      arr.addElement(r.point);
      return;
    }
    Gather(r.LEFT, arr);
    Gather(r.RIGHT, arr);
  }

   void Create1dTree(Node2d r) {
    Vector arr = new Vector();

    Gather(r, arr);

    YComparable[] py = new YComparable[arr.size()];
    for (int i = 0; i < arr.size(); i++)
	py[i] = new YComparable((DPoint)arr.elementAt(i));
    QSortAlgorithm.sort(py);
    for (int i = 0; i < arr.size(); i++)
	arr.setElementAt(py[i].p, i);

    /*
    for (int i = 0; i < arr.size() - 1; i++)
      for (int j = 0; j < arr.size() - 1; j++)
	if (compareToY((DPoint)arr.elementAt(j), (DPoint)arr.elementAt(j+1)) > 0) {
	  DPoint p;
	  p = (DPoint)arr.elementAt(j+1);
	  arr.setElementAt(arr.elementAt(j), j+1);
	  arr.setElementAt(p, j);
	}
	
    */
    Node1d oldar[];
    Node1d newar[];
    double oldminar[];
    double oldmaxar[];
    double newminar[];
    double newmaxar[];

    oldar = new Node1d[arr.size()];
    oldminar = new double[arr.size()];
    oldmaxar = new double[arr.size()];

    for (int i=0; i < arr.size(); i++) {
      oldar[i] = new Node1d();
      oldar[i].point = (DPoint)arr.elementAt(i);
      oldar[i].midrange = oldminar[i] = oldmaxar[i] = oldar[i].point.y;
    }
    for (int i = 1; i < arr.size() -1; i++) {
	oldar[i].LEFT = oldar[i - 1];
	oldar[i].RIGHT = oldar[i + 1];
    }
    
    oldar[0].LEFT = oldar[arr.size() - 1].RIGHT = null;
    if (arr.size() >= 2) {
      oldar[0].RIGHT = oldar[1];
      oldar[arr.size() - 1].LEFT = oldar[arr.size() - 2];
    }

    while (oldar.length != 1) {
      newar = new Node1d[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
      newminar = new double[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
      newmaxar = new double[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
      if (oldar.length % 2 != 0) {
	newar[newar.length - 1] = oldar[oldar.length - 1];
	newminar[newminar.length - 1] = oldminar[oldminar.length - 1];
	newmaxar[newmaxar.length - 1] = oldmaxar[oldmaxar.length - 1];
      }
      for (int i = 0; i < oldar.length / 2; i++) {
	newar[i] = new Node1d();
	newar[i].LEFT = oldar[2*i];
	newar[i].RIGHT = oldar[2*i + 1];
	newar[i].midrange = (oldmaxar[2*i] + oldminar[2*i+1]) / 2;
	newminar[i] = oldminar[2*i];
	newmaxar[i] = oldmaxar[2*i + 1];      
      }
      oldar = newar;
      oldmaxar = newmaxar;
      oldminar = newminar;
    }
    r.range_tree = oldar[0];
  }

   Node2d createTrees(Vector arr) {
    if (arr.size() == 0)
      return null; // too small

    XComparable[] px = new XComparable[arr.size()];
    for (int i = 0; i < arr.size(); i++)
	px[i] = new XComparable((DPoint)arr.elementAt(i));
    QSortAlgorithm.sort(px);
    for (int i = 0; i < arr.size(); i++)
	arr.setElementAt(px[i].p, i);

    /*
    // bubble sort to be substituted when sort becomes part of jdk
    for (int i = 0; i < arr.size() - 1; i++)  
      for (int j = 0; j < arr.size() - 1; j++)
	if (compareToX((DPoint)arr.elementAt(j), (DPoint)arr.elementAt(j+1)) > 0) {
	  DPoint p;
	  p = (DPoint)arr.elementAt(j+1);
	  arr.setElementAt(arr.elementAt(j), j+1);
	  arr.setElementAt(p, j);
	}
    */
    Node2d oldar[];
    Node2d newar[];
    double oldminar[];
    double oldmaxar[];
    double newminar[];
    double newmaxar[];

    oldar = new Node2d[arr.size()];
    oldminar = new double[arr.size()];
    oldmaxar = new double[arr.size()];

    for (int i=0; i < arr.size(); i++) {
      oldar[i] = new Node2d();
      oldar[i].point = (DPoint)arr.elementAt(i);
      oldar[i].midrange = oldminar[i] = oldmaxar[i] = oldar[i].point.x;
      Create1dTree(oldar[i]);
    }

    oldar[0].LEFT = oldar[arr.size() - 1].RIGHT = null;
    if (arr.size() > 1) {
      oldar[0].RIGHT = oldar[1];
      oldar[arr.size() - 1].LEFT = oldar[arr.size() - 2];
    }
    for (int i=1; i < arr.size() - 1; i++) {
      oldar[i].LEFT = oldar[i - 1];
      oldar[i].RIGHT = oldar[i + 1];
    }

    while (oldar.length != 1) {
      newar = new Node2d[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
      newminar = new double[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
      newmaxar = new double[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];

      if (oldar.length % 2 != 0) {
	newar[newar.length - 1] = oldar[oldar.length - 1];
	newminar[newminar.length - 1] = oldminar[oldminar.length - 1];
	newmaxar[newmaxar.length - 1] = oldmaxar[oldmaxar.length - 1];
      }
      for (int i = 0; i < oldar.length / 2; i++) {
	newar[i] = new Node2d();
	newar[i].LEFT = oldar[2*i];
	newar[i].RIGHT = oldar[2*i + 1];
	newar[i].midrange = (oldmaxar[2*i] + oldminar[2*i+1]) / 2;
	newminar[i] = oldminar[2*i];
	newmaxar[i] = oldmaxar[2*i + 1];
	Create1dTree(newar[i]);
      }
      oldar = newar;
      oldmaxar = newmaxar;
      oldminar = newminar;
    }
    //------------------ 2d-tree created
    return oldar[0];
  }

  //-------------------- search ----------------------------

  public SearchVector Search(QueryObject searchable, int mode) {
    DRectangle searchRect = searchable.getBB();
    double LX = searchRect.x;
    double LY = searchRect.y;
    double RX = searchRect.x + searchRect.width;
    double RY = searchRect.y + searchRect.height;

    Node2d T = ROOT;
    SearchVector v = new SearchVector();
    Vector searchVector = new Vector();

    double maxleft, minright;
    double maxright = wholeCanvas.x + wholeCanvas.width;
    double minleft = wholeCanvas.x;

    if (T == null)
      return v;

    maxleft = minright = T.midrange;


    while (T.point == null) {
      v.addElement(new SVElement(new YellowBlock(minleft, wholeCanvas.y, maxright, wholeCanvas.y + wholeCanvas.height, false), 
				 searchVector));

      maxleft = minright = T.midrange;

      if (RX < T.midrange) {
	maxright = T.midrange;
	T = T.LEFT;
      } else if (T.midrange < LX) {
	minleft = T.midrange;
	T = T.RIGHT;
      }
      else
	break;
    }
    if (T.point != null) {
      drawableInOut(searchable, T.point, mode, v, searchVector);
    } else {

      Node2d Q = T;
      T = T.LEFT;
      while (T.point == null) {
	searchVector.addElement(new DRectangle(minright, wholeCanvas.y, 
					       maxright - minright, wholeCanvas.y + wholeCanvas.height));
	searchVector.addElement(new DRectangle(minleft, wholeCanvas.y, 
					       maxleft - minleft,  wholeCanvas.y + wholeCanvas.height));
	v.addElement(new SVElement(new YellowBlock(minleft, wholeCanvas.y, 
						   maxleft - minleft,  wholeCanvas.y + wholeCanvas.height, false), 
				   searchVector));
	searchVector.removeElementAt(searchVector.size() - 1);

	if (LX <= T.midrange) {
	  searchVector.addElement(new DRectangle(minleft, wholeCanvas.y, 
						 T.midrange - minleft,  wholeCanvas.y + wholeCanvas.height));
	  searchVector.addElement(new DRectangle(T.midrange, wholeCanvas.y, 
						 maxleft - T.midrange,  wholeCanvas.y + wholeCanvas.height));
	  v.addElement(new SVElement(new YellowBlock(T.midrange, wholeCanvas.y, 
						     maxleft - T.midrange,  wholeCanvas.y + wholeCanvas.height, true), 
				     searchVector));
	  searchVector.removeElementAt(searchVector.size() - 1);


	  searchVector.addElement(new DRectangle(minright, wholeCanvas.y, 
						 maxright - minright,  wholeCanvas.y + wholeCanvas.height));

	  v.addElement(new SVElement(new GreenPoints(greenPoints(T.RIGHT.range_tree, LY, RY)),
				     searchVector));

	  Search1d(searchable, T.RIGHT.range_tree, mode, v, searchVector);
	  searchVector.removeElementAt(searchVector.size() - 1);
	  maxleft = T.midrange;
	  T = T.LEFT;
	} else {
	  searchVector.removeElementAt(searchVector.size() - 1);
	  minleft = T.midrange;
	  T = T.RIGHT;
	}
      }
      v.addElement(new SVElement(new YellowBlock(minleft, wholeCanvas.y, 
						 maxleft - minleft, wholeCanvas.y + wholeCanvas.height, true), 
				 searchVector));
      drawableInOut(searchable, T.point, mode, v, searchVector);

      //      searchVector.removeElementAt(searchVector.size() - 1);

      double leftboundary = T.midrange;

      T = Q.RIGHT;
      while (T.point == null) {
	v.addElement(new SVElement(new YellowBlock(minright, wholeCanvas.y, 
						   maxright - minright, wholeCanvas.y + wholeCanvas.height, false), 
				   searchVector));
 	if (T.midrange <= RX) {
	  searchVector.addElement(new DRectangle(T.midrange, wholeCanvas.y, 
						 maxright - T.midrange, wholeCanvas.y + wholeCanvas.height));
	  v.addElement(new SVElement(new YellowBlock(minright, wholeCanvas.y, 
						     T.midrange - minright, wholeCanvas.y + wholeCanvas.height, true), 
				     searchVector));

	  v.addElement(new SVElement(new GreenPoints(greenPoints(T.LEFT.range_tree, 
								 searchRect.y, 
								 searchRect.y + searchRect.height)),
				     searchVector));

	  Search1d(searchable, T.LEFT.range_tree, mode, v, searchVector);
	  minright = T.midrange;
	  T = T.RIGHT;
	} else {
	  maxright = T.midrange;
	  T = T.LEFT;
	}
      }
      drawableInOut(searchable, T.point, mode, v, searchVector);

      v.addElement(new SVElement(new YellowBlock(minright, wholeCanvas.y, 
						 maxright - minright, wholeCanvas.y + wholeCanvas.height, true), 
				 searchVector));

    }
    return v;

  }

   Vector greenPoints(Node1d r, double low, double hi) {
     Vector gr = new Vector();
    if (r == null)
      return null;

    while (r.point == null) 
      r = r.LEFT;
    while (r != null) {
      gr.addElement(r.point);
      r = r.RIGHT;
    }
    return gr;
  }

  void Search1d(QueryObject searchable, Node1d t, int mode, SearchVector v, Vector searchVector) {
    DRectangle searchRect = searchable.getBB();
    if (t == null)
      return;

    Node1d tmp = t;
    while (t.point == null) {
      t = searchRect.y <= t.midrange ? t.LEFT : t.RIGHT;
      //      System.out.println("   descending: " + t.midrange);
    }

    Vector red1 = new Vector();
    while (tmp.point == null)
      tmp = tmp.LEFT;
    while (tmp != t) {
      red1.addElement(tmp.point);
      tmp = tmp.RIGHT;
    }
    v.addElement(new SVElement(new RedPoints(red1), searchVector));

    while (t != null && t.midrange <= searchRect.y + searchRect.height) {
      drawableInOut(searchable, t.point, mode, v, searchVector);
      t = t.RIGHT;
    }

    Vector red2 = new Vector();
    while (t != null) {
      red2.addElement(t.point);
      t = t.RIGHT;
    }
    v.addElement(new SVElement(new RedPoints(red2), searchVector));
  }

}

