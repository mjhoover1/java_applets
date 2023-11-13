/* $Id: DLine.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;
import java.awt.*;

/**
 * Class representing a drawable line segment in a 2D space. This class defines
 * a line with two endpoints and provides methods for various geometric
 * calculations and drawing operations.
 */
public class DLine implements Drawable {
	public DPoint p1, p2; // Endpoints of the line segment
	DRectangle bbox; // Bounding box of the line segment

	/**
	 * Constructor creating a line from the diagonal of a rectangle.
	 *
	 * @param r The rectangle from which the line is created.
	 */
	public DLine(DRectangle r) {
		this(new DPoint(r.x, r.y), new DPoint(r.x + r.width, r.y + r.height));
	}

	/**
	 * Constructor creating a line given two endpoints.
	 *
	 * @param pt1 The first endpoint of the line.
	 * @param pt2 The second endpoint of the line.
	 */
	public DLine(DPoint pt1, DPoint pt2) {
		p1 = pt1;
		p2 = pt2;
		bbox = new DRectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
	}

	/**
	 * Constructor creating a line given coordinates of two points.
	 *
	 * @param x1 The x-coordinate of the first point.
	 * @param y1 The y-coordinate of the first point.
	 * @param x2 The x-coordinate of the second point.
	 * @param y2 The y-coordinate of the second point.
	 */
	public DLine(double x1, double y1, double x2, double y2) {
		this(new DPoint(x1, y1), new DPoint(x2, y2));
	}

	/**
	 * Checks if this line is equal to another line. Lines are considered equal if
	 * they have the same endpoints, regardless of order.
	 *
	 * @param l The line to compare with.
	 * @return true if both lines have the same endpoints, false otherwise.
	 */
	public boolean equals(DLine l) {
		return l != null && ((p1.equals(l.p1) && p2.equals(l.p2)) || (p1.equals(l.p2) && p2.equals(l.p1)));
	}

	/**
	 * Calculates the distance between this line segment and a point.
	 *
	 * @param p The point to calculate the distance to.
	 * @return The distance between the line segment and the point.
	 */
	public double distance(DPoint p) {
		return p.distance(this);
	}

	/**
	 * Calculates the distance between this line segment and a point, storing the
	 * result in the provided array.
	 *
	 * @param p The point to calculate the distance to.
	 * @param d An array where the calculated distance will be stored.
	 * @return The same array passed as the parameter with the calculated distance
	 *         stored in it.
	 */
	public double[] distance(DPoint p, double[] d) {
		p.distance(this, d);
		return d;
	}

	/**
	 * Calculates the distance between this line segment and another line segment,
	 * storing the result in the provided array.
	 *
	 * @param p The other line segment to calculate the distance to.
	 * @param d An array where the calculated distance will be stored.
	 * @return The same array passed as the parameter with the calculated distance
	 *         stored in it.
	 */
	public double[] distance(DLine p, double[] d) {
		d[0] = distance(p);
		return d;
	}

	/**
	 * Calculates the distance between this line segment and another line segment.
	 * Takes into account different cases such as parallel lines, intersecting
	 * lines, etc.
	 *
	 * @param l2 The other line segment to calculate the distance to.
	 * @return The distance between the two line segments.
	 */
	public double distance(DLine l2) {
		// Implementation of distance calculation based on line classification
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

	/**
	 * Calculates the minimum distance between this line segment and a DPath (path
	 * of connected lines). Iterates through each edge of the path to find the
	 * minimum distance.
	 *
	 * @param l The DPath to calculate the distance to.
	 * @return The minimum distance between this line segment and the DPath.
	 */
	public double distance(DPath l) {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < l.Size() - 1; i++) {
			double d = distance(l.Edge(i));
			if (d < min)
				min = d;
		}
		return min;
	}

	/**
	 * Calculates the minimum distance between this line segment and a DPath,
	 * storing the result in the provided array. Iterates through each edge of the
	 * path to find and store the minimum distance.
	 *
	 * @param l   The DPath to calculate the distance to.
	 * @param min An array where the calculated minimum distance will be stored.
	 * @return The same array passed as the parameter with the calculated distance
	 *         stored in it.
	 */
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

	/**
	 * Calculates the distance between this line segment and a DRectangle, storing
	 * the result in the provided array.
	 *
	 * @param r   The DRectangle to calculate the distance to.
	 * @param key An array where the calculated distance will be stored.
	 * @return The same array passed as the parameter with the calculated distance
	 *         stored in it.
	 */
	public double[] distance(DRectangle r, double[] key) {
		key[0] = distance(r);
		key[1] = 0;
		return key;
	}

	/**
	 * Calculates the distance between this line segment and a DRectangle. Considers
	 * the edges and vertices of the rectangle to find the minimum distance.
	 *
	 * @param r The DRectangle to calculate the distance to.
	 * @return The minimum distance between this line segment and the DRectangle.
	 */
	public double distance(DRectangle r) {
		if (r.contains(p1) || r.contains(p2))
			return 0.0;

		// Implementation for calculating the distance from the line to the rectangle
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

    /**
     * Calculates the distance between this line segment and a DPolygon, storing the result in the provided array.
     *
     * @param g The DPolygon to calculate the distance to.
     * @param k An array where the calculated distance will be stored.
     * @return The same array passed as the parameter with the calculated distance stored in it.
     */
	public double[] distance(DPolygon g, double[] k) {
		k[0] = distance(g);
		k[1] = 0;
		return k;
	}

    /**
     * Calculates the distance between this line segment and a DPolygon.
     * Iterates through the edges of the polygon to find the minimum distance.
     *
     * @param g The DPolygon to calculate the distance to.
     * @return The minimum distance between this line segment and the DPolygon.
     */
	public double distance(DPolygon g)
//     ----------------
//
//  Returns Euclidean distance between l and g
//
	{
        // Implementation for calculating the distance from the line to the polygon
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

	/**
	 * Determines if this line segment intersects with a point.
	 *
	 * @param p The point to check for intersection.
	 * @return true if the line segment intersects with the point, false otherwise.
	 */
	public boolean intersects(DPoint p) {
		return distance(p) == 0;
	}

	/**
	 * Determines if this line segment intersects with another line segment.
	 *
	 * @param m The other line segment to check for intersection.
	 * @return true if the two line segments intersect, false otherwise.
	 */
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

    /**
     * Determines if this line segment intersects with a given rectangle.
     * The intersection is checked by determining if any part of the line
     * lies within the bounds of the rectangle or if the line's endpoints
     * touch or cross the edges of the rectangle.
     *
     * @param r The DRectangle to check for intersection with.
     * @return true if the line segment intersects with the rectangle, false otherwise.
     */
	public boolean intersects(DRectangle r) {
		return r.intersects(this);
	}

	/**
	 * Draws the line on a given DrawingTarget. Implements the method from the
	 * Drawable interface.
	 *
	 * @param g The DrawingTarget on which to draw the line.
	 */
	public void draw(DrawingTarget g) {
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

    /**
     * Draws the line directly onto a DrawingTarget with a specified color.
     * This method bypasses the usual styling and directly renders the line.
     *
     * @param c The color to use for drawing the line.
     * @param g The DrawingTarget on which to draw the line.
     */
	public void directDraw(Color c, DrawingTarget g) {
		g.directLine(c, p1.x, p1.y, p2.x, p2.y);
	}

    /**
     * Gets the bounding box of the line segment.
     * The bounding box is the smallest rectangle that completely contains the line segment.
     *
     * @return A DRectangle representing the bounding box of the line segment.
     */
	public DRectangle getBB() {
		return bbox;
	}

    /**
     * Determines whether the line segment has an area.
     * A line segment, being one-dimensional, does not cover any area.
     *
     * @return false as a line segment does not have an area.
     */
	public boolean hasArea() {
		return false;
	}

    /**
     * Provides a string representation of the line segment.
     * The string includes the coordinates of the two endpoints of the line.
     *
     * @return A string representing the line segment.
     */
	public String toString() {
		return "DLine: [" + p1.toString() + ", " + p2.toString() + "]";
	}

    // Constants for classifying relationships between two line segments
	final static int Parallel = 0;
	final static int NotParallel = 1;
	final static int Intersecting = 2;

	/**
	 * Classifies this line segment relative to another line segment. Determines if
	 * the lines are parallel, intersecting, or not parallel.
	 *
	 * @param m The other line segment to classify against.
	 * @return An integer constant indicating the classification result.
	 */
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

    /**
     * Draws a buffered representation of the line segment onto a DrawingTarget.
     * This method creates a visual buffer around the line segment with a specified thickness.
     * The buffer is visualized by drawing parallel lines on either side of the original line
     * and connecting their endpoints with semicircular arcs.
     *
     * @param c    The color to use for drawing the buffered line.
     * @param dt   The DrawingTarget on which to draw the buffered line.
     * @param dist The distance from the original line to the parallel lines, representing half the thickness of the buffer.
     */
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
