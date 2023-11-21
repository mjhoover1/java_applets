package vasco.points;
/* $Id: GenPRkdbuck.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;
import java.util.*;

public abstract class GenPRkdbuck extends GenericPRkdbucket {

  public GenPRkdbuck(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
    super(can, b, md, p, r);
  }

  public void reInit(JComboBox ao) {
    super.reInit(ao);
  }

  boolean insert (DPoint p, PRkdbucketNode r, boolean xcoord, double cx, double cy, 
		  double sx, double sy, int md) {
    boolean ok = md > 0;

    if (r.nodetype == GRAY) {
      if (xcoord) {
	if (p.x > cx)  // x coordinate
	  ok = insert(p, r.right, !xcoord, cx + sx / 4, cy, sx / 2, sy, md - 1) && ok;
	else 
	  ok = insert(p, r.left, !xcoord, cx - sx / 4, cy, sx / 2, sy, md - 1) && ok;
      
      } else { // y coordinate
	if (p.y > cy) 
	  ok = insert(p, r.right, !xcoord, cx, cy + sy / 4, sx, sy / 2, md - 1) && ok;
	else 
	  ok = insert(p, r.left, !xcoord, cx, cy - sy / 4, sx, sy / 2, md - 1) && ok;
      }
      return ok;
    }
    /* not gray */

    if (r.isIn(p))
      return ok;  // node already exists
    r.addPoint(p);
    if (r.points.size() > maxBucketSize) {
      r.nodetype = GRAY;
      r.left = new PRkdbucketNode(WHITE);
      r.right = new PRkdbucketNode(WHITE);
      Vector pts = r.points;
      r.points = new Vector();
      for (int i = 0; i < pts.size(); i++) {
	DPoint pt = (DPoint)pts.elementAt(i);
	ok = insert(pt, r, xcoord, cx, cy, sx, sy, md) && ok;
      }
    }
    return ok;
  }

}

