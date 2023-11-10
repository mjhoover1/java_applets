/* $Id: DLine.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;
import java.awt.*;

public class DLine implements Drawable {
	public DPoint p1, p2;
	DRectangle bbox;

	public DLine(DRectangle r) {
		this(new DPoint(r.x, r.y), new DPoint(r.x + r.width, r.y + r.height));
	}

	public DLine(DPoint pt1, DPoint pt2) {
		p1 = pt1;
		p2 = pt2;
		bbox = new DRectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
	}

	public DLine(double x1, double y1, double x2, double y2) {
		this(new DPoint(x1, y1), new DPoint(x2, y2));
	}

	public boolean equals(DLine l) {
		return l != null && ((p1.equals(l.p1) && p2.equals(l.p2)) || (p1.equals(l.p2) && p2.equals(l.p1)));
	}

	public double distance(DPoint p) {
		return p.distance(this);
	}

	public double[] distance(DPoint p, double[] d) {
		p.distance(this, d);
		return d;
	}

	public double[] distance(DLine p, double[] d) {
		d[0] = distance(p);
		return d;
	}

	public double distance(DLine l2) {
//     --------
//
		switch (classify(l2)) {
		case Parallel: {
			int classif1 = p1.classify(l2);
			if (classif1 == DPoint.InBetween)
				return p1.distance(l2);
			int classif2 = p2.classify(l2);
			if (classif2 == DPoint.InBetween)
				return p2.distance(l2);
			if (classif1 == classif2) {
				return (classif1 == DPoint.BeforeFirst ? distance(l2.p1) : distance(l2.p2));
			} else {
				return distance(l2.p1);
			}
		}
		case NotParallel: {
			double d1 = p1.distance(l2);
			double d2 = p2.distance(l2);
			double d3 = distance(l2.p1);
			double d4 = distance(l2.p2);
			if (d1 > d2)
				d1 = d2;
			if (d3 > d4)
				d3 = d4;
			return d1 < d3 ? d1 : d3;
		}
		case Intersecting: {
			return 0.0;
		}
		}
		return -1.0; // Should never happen
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

	public double[] distance(DRectangle r, double[] key) {
		key[0] = distance(r);
		key[1] = 0;
		return key;
	}

	public double distance(DRectangle r) {
		if (r.contains(p1) || r.contains(p2))
			return 0.0;

		double dmin = distance(r.Nside());
		if (dmin == 0.0)
			return 0.0;
		double d = distance(r.Wside());
		if (d == 0.0)
			return 0.0;
		if (d < dmin)
			dmin = d;
		d = distance(r.Eside());
		if (d == 0.0)
			return 0.0;
		if (d < dmin)
			dmin = d;
		d = distance(r.Sside());
		if (d < dmin)
			dmin = d;
		return dmin;
	}

	public double[] distance(DPolygon g, double[] k) {
		k[0] = distance(g);
		k[1] = 0;
		return k;
	}

	public double distance(DPolygon g)
//     ----------------
//
//  Returns Euclidean distance between l and g
//
	{
		double thisdist;
		double dist = Double.MAX_VALUE;
		DLine edge;

		DPoint c1 = g.LastCorner();
		DPoint c2 = g.FirstCorner();
		for (int i = g.Size(); i > 0; i--) {
			edge = new DLine(c1, c2);
			thisdist = distance(edge);
			if (dist > thisdist)
				dist = thisdist;
			if (dist == 0.0)
				return dist;
			c1 = c2;
			c2 = g.NextCorner();
		}

		// now check for wholly enclosed line
		if (g.contains(p1))
			return 0.0;

		return dist;
	}

	public boolean intersects(DPoint p) {
		return distance(p) == 0;
	}

	public boolean intersects(DLine m) {
		DPoint lk = new DPoint(p1.x - p2.x, p1.y - p2.y);
		DPoint nm = new DPoint(m.p1.x - m.p2.x, m.p1.y - m.p2.y);
		DPoint mk = new DPoint(m.p2.x - p2.x, m.p2.y - p2.y);
		double det = nm.x * lk.y - nm.y * lk.x;

		if (Math.abs(det) > 1.e-10) {
			double detinv = 1.0 / det;
			double s = (nm.x * mk.y - nm.y * mk.x) * detinv;
			double t = (lk.x * mk.y - lk.y * mk.x) * detinv;
			if (s < 0.0 || s > 1.0 || t < 0.0 || t > 1.0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public boolean intersects(DRectangle r) {
		return r.intersects(this);
	}

	public void draw(DrawingTarget g) {
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	public void directDraw(Color c, DrawingTarget g) {
		g.directLine(c, p1.x, p1.y, p2.x, p2.y);
	}

	public DRectangle getBB() {
		return bbox;
	}

	public boolean hasArea() {
		return false;
	}

	public String toString() {
		return "DLine: [" + p1.toString() + ", " + p2.toString() + "]";
	}

	final static int Parallel = 0;
	final static int NotParallel = 1;
	final static int Intersecting = 2;

	int classify(DLine m)
//          --------
//
//  Classifies a line segment against another. (pp. 50)
//  Returns:
//      Parallel     if the two line segments are parallel;
//      NotParallel  if the two line segments do not intersect but
//                   are not parallel either or
//      Intersecting if the two line segments do intersect
//
	{
		final DPoint zeropt = new DPoint(0, 0);
		DPoint lk = new DPoint(p1.x - p2.x, p1.y - p2.y);
		DPoint nm = new DPoint(m.p1.x - m.p2.x, m.p1.y - m.p2.y);
		DPoint mk = new DPoint(m.p2.x - p2.x, m.p2.y - p2.y);
		double det = nm.x * lk.y - nm.y * lk.x;

		if (Math.abs(det) > CommonConstants.accuracy) {
			double detinv = 1.0 / det;
			double s = (nm.x * mk.y - nm.y * mk.x) * detinv;
			double t = (lk.x * mk.y - lk.y * mk.x) * detinv;
			if (s < -CommonConstants.accuracy || s - 1 > CommonConstants.accuracy || t < -CommonConstants.accuracy
					|| t - 1 > CommonConstants.accuracy) {
				return NotParallel;
			} else {
				return Intersecting;
			}
		} else {
			// parallel
			if (nm.x == 0 && nm.y == 0)
				nm = lk; // m degenerate, use k
			double intdiff = nm.x * mk.y - nm.y * mk.x;
			if (Math.abs(intdiff) < CommonConstants.accuracy) {
				// collinear; check if bounding boxes intersect
				DRectangle rk = new DRectangle(p1, p2);
				DRectangle rm = new DRectangle(m.p1, m.p2);
				if (rk.intersects(rm))
					return Intersecting;
				else
					return Parallel;
			} else
				return Parallel;
		}
	}

	public void drawBuffer(Color c, DrawingTarget dt, double dist) {

		double dx, dy, esize;
		dt.setColor(c);
		esize = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
		dy = (p2.y - p1.y) / esize;
		dx = (p2.x - p1.x) / esize;

		dt.drawLine(p1.x + dy * dist, p1.y - dx * dist, p2.x + dy * dist, p2.y - dx * dist);
		dt.drawLine(p1.x - dy * dist, p1.y + dx * dist, p2.x - dy * dist, p2.y + dx * dist);

		double rangle;
		int angle;
		DPoint pt1, pt2;
		if (p1.y > p2.y || p1.y == p2.y && p1.x > p2.x) {
			rangle = (p1.x - p2.x == 0 && p1.y - p2.y == 0) ? 0 : Math.atan2(p1.y - p2.y, p1.x - p2.x);
			pt1 = p2;
			pt2 = p1;
		} else {
			rangle = (p2.x - p1.x == 0 && p2.y - p1.y == 0) ? 0 : Math.atan2(p2.y - p1.y, p2.x - p1.x);
			pt1 = p1;
			pt2 = p2;
		}

		angle = (int) Math.round(rangle / Math.PI * 180); // -180 - +180

		dt.drawArc(pt1.x - dist, pt1.y - dist, 2 * dist, 2 * dist, 90 - angle, 180);
		dt.drawArc(pt2.x - dist, pt2.y - dist, 2 * dist, 2 * dist, -90 - angle, 180);
	}

}
