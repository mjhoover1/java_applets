/* $Id: DPoint.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;
import java.awt.*;

public class DPoint implements Drawable {
  public double x, y;

  public DPoint() {
    this(0.0, 0.0);
  }

  public DPoint(DPoint p) {
    this(p.x, p.y);
  }

  public DPoint(double x, double y) {
    this.x = x; this.y = y;
  }

  public boolean equals(DPoint p) {
    return p != null && p.x == x && p.y == y;
  }

  public double[] distance(DPoint p, double[] keys) {
    keys[0] = distance(p);
    return keys;
  }

  public double distance(DPoint p) {
    return Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
  }

  public double distance(Point p) {
    return Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
  }

    public double distance(DPath l) {
	double min = Double.MAX_VALUE;

	for (int i = 0; i < l.Size() - 1; i++) {
	    double d = distance(l.Edge(i));
	    if (d < min)
		min = d;
	}
	return min;
    }

    public double[] distance(DPath l, double[] min) {
	double[] keys = new double[2];

	distance(l.Edge(0), min);
	for (int i = 1; i < l.Size() - 1; i++) {
	    distance(l.Edge(i), keys);
	    if (keys[0] < min[0] || (keys[0] == min[0] && keys[1] < min[1])) {
		min[0] = keys[0];
		min[1] = keys[1];
	    }
	}
	min[1] = 0;
	return min;
    }


  public double distance(DRectangle r) {
    double[] retVal = new double[2];
    distance(r, retVal);
    return retVal[0];
  }

  public double distance(DLine l) {
    double[] retVal = new double[2];
    distance(l, retVal);
    return retVal[0];
  }
 

  public double[] distance(DLine l, double[] dist) {
      if (this.equals(l.p1) || this.equals(l.p2)) {
	  dist[0] = dist[1] = 0;
	  return dist;
      }

    dist[1] = 0;

    double vx, vy, vxp, vyp, t;
    vx = l.p2.x - l.p1.x;
    vy = l.p2.y - l.p1.y;
    
    if (vx == 0.0 && vy == 0.0) {
      dist[0] = distance(l.p1);
      return dist;
    }
    if (vx == 0) {
      if (Math.min(l.p1.y, l.p2.y) < y && y < Math.max(l.p1.y, l.p2.y)) {
        dist[0] = Math.abs(l.p1.x - x);
        return dist;
      } else {
        dist[0] = (Math.min(distance(l.p1), distance(l.p2)));
        return dist;
      }
    }

    vxp = vy;
    vyp = -vx;
    t = (x + vxp/vyp * (l.p1.y - y) - l.p1.x) / (vx - vy*vxp/vyp);
    if (t < 0 || t > 1) {
      dist[0] = (Math.min(distance(l.p1), distance(l.p2)));
    } else {
      double d1 = x - (l.p1.x + t*vx);
      double d2 = y - (l.p1.y + t*vy);
      dist[0] = Math.sqrt(d1*d1 + d2*d2);
    }
    return dist;
  }


  public double[] distance(DRectangle r, double[] retVal) {
    if (r.contains(this)) {
      retVal[0] = 0.0;
      retVal[1] = Math.min(x - r.x, Math.min(r.x + r.width - x, Math.min(y - r.y, r.y + r.height - y)));
      return retVal;
    }
    if (x < r.x && r.y <= y && y <= r.y + r.height) {
      retVal[0] = (r.x - x);
      return retVal;
    }
    if (r.x + r.width < x && r.y <= y && y <= r.y + r.height) {
      retVal[0] = (x - r.x - r.width);
      return retVal;
    }
    if (y < r.y && r.x <= x && x <= r.x + r.width) {
      retVal[0] = (r.y - y);
      return retVal;
    }
    if (r.y + r.height < y && r.x <= x && x <= r.x + r.width) {
      retVal[0] = (y - r.y - r.height);
      return retVal;
    }

    double d1 = distance(new DPoint(r.x, r.y));
    double d2 = distance(new DPoint(r.x, r.y + r.height));
    double d3 = distance(new DPoint(r.x + r.width, r.y));
    double d4 = distance(new DPoint(r.x + r.width, r.y + r.height));

    retVal[0] = Math.min(d1, Math.min(d2, Math.min(d3, d4)));

    return retVal;
  }

    public double distance(DPolygon g) {
	double[] k = new double[2];
	distance(g, k);
	return k[0];
    }

  public double[] distance(DPolygon g, double[] keys) {
//     --------
//  
//  Distance from a point to a polygon.
//

   double thisdist;
   double dist = Double.MAX_VALUE;
   int intersections = 0;
   DLine arc, edge;

   double gx = g.getBB().x;
   double gy = g.getBB().y;

   DPoint outside = new DPoint(gx - Math.abs(gx), gy - Math.abs(gy));

   arc = new DLine(this, outside);	// draw an arc to outside the box

   for (int i = g.Size()-1; i >= 0; --i) {
      edge = g.Edge (i);
      thisdist = distance(edge);
      if (dist>thisdist)  dist = thisdist;
      if (arc.intersects(edge)) intersections++;
   }

   if (intersections % 2 != 0) {
     keys[0] = 0;   // inside
     keys[1] = dist;
   } else 
     keys[0] = dist;

   return keys;
  }

  //---------------------------------------

  protected final int PS = 6;

  public void draw(DrawingTarget g) {
    g.fillOval(x, y, PS, PS);
  }

  public void directDraw(Color c, DrawingTarget g) {
    g.directFillOval(c, x, y, PS, PS);
  }

  public DRectangle getBB() {
    return new DRectangle(x, y, 0, 0);
  }

  public boolean hasArea(){
    return false;
  }

  public String toString() {
    return "DPoint: [" + x + ", " + y + "];";
  }

    final static int InBetween = 0;
    final static int BeforeFirst = 1;
    final static int AfterSecond = 2;
    
    public int classify (DLine l)
//               --------
//
//  Classifies a point against a line segment (pp. 47)
//  Returns:
//      InBetween    if the closest point to p of l is inside the segment
//      BeforeFirst  if l.p1 is the closest point to p
//      AfterSecond  if l.p2 is the closest point to p
//
{
   DPoint kj = new DPoint(l.p1.x - x, l.p1.y - y);
   DPoint lk = new DPoint(l.p2.x - l.p1.x, l.p2.y - l.p1.y);
   double denom = lk.x*lk.x + lk.y*lk.y;
   if (denom < CommonConstants.accuracy) {
      return InBetween;
   }
   else {
      double t = - (kj.x*lk.x + kj.y * lk.y) / denom;
      if (t < -CommonConstants.accuracy) return BeforeFirst;
      if (t-1 > CommonConstants.accuracy) return AfterSecond;
      return InBetween;
   }
}

    public void drawBuffer(Color c, DrawingTarget dt, double dist) {
	dt.setColor(c);
	dt.drawOval(this, dist, dist);
    }

    // ----------------------- intersections -------------

    public boolean intersects(DRectangle r) {
	return r.contains(this);
    }

}
