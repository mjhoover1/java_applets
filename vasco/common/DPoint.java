/* $Id: DPoint.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;

import java.awt.Color;
import java.awt.Point;

import javax.swing.*; // import java.awt.*;

/**
 * Class representing a drawable point in a 2D space. This class is used to
 * define a point with x and y coordinates and provides methods for various
 * geometric calculations and operations.
 */
public class DPoint implements Drawable {
	public double x, y; // X and Y coordinates of the point

	/**
	 * Default constructor creating a point at the origin (0,0).
	 */
	public DPoint() {
		this(0.0, 0.0);
	}

	/**
	 * Copy constructor creating a point with the same coordinates as another point.
	 *
	 * @param p The point to copy coordinates from.
	 */
	public DPoint(DPoint p) {
		this(p.x, p.y);
	}

	/**
	 * Constructor creating a point with specified x and y coordinates.
	 *
	 * @param x The x-coordinate of the point.
	 * @param y The y-coordinate of the point.
	 */
	public DPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Checks if this point is equal to another point.
	 *
	 * @param p The point to compare with.
	 * @return true if both points have the same coordinates, false otherwise.
	 */
	public boolean equals(DPoint p) {
		return p != null && p.x == x && p.y == y;
	}

	/**
	 * Calculates the distance between this point and another DPoint, storing the
	 * result in the provided array. This method is useful when you want to reuse an
	 * existing array for performance reasons, such as in tight loops or recursive
	 * calls, to avoid frequent array allocation.
	 *
	 * @param p    The other DPoint to which the distance is calculated.
	 * @param keys An array where the calculated distance will be stored. The
	 *             distance is stored in the first element of the array.
	 * @return The same array passed as the parameter with the calculated distance
	 *         stored in the first element. This return allows for chaining
	 *         operations or direct use.
	 */
	public double[] distance(DPoint p, double[] keys) {
		keys[0] = distance(p);
		return keys;
	}

	/**
	 * Calculates the Euclidean distance between this point and another DPoint.
	 *
	 * @param p The other point to which the distance is calculated.
	 * @return The distance between this point and point p.
	 */
	public double distance(DPoint p) {
		return Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
	}

	/**
	 * Calculates the Euclidean distance between this point and a standard AWT
	 * Point.
	 *
	 * @param p The AWT Point to which the distance is calculated.
	 * @return The distance between this point and the AWT Point.
	 */
	public double distance(Point p) {
		return Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
	}

	/**
	 * Calculates the minimum distance between this point and a DPath (line
	 * segment). Iterates through each edge of the path to find the minimum
	 * distance.
	 *
	 * @param l The DPath (line segment) to calculate the distance to.
	 * @return The minimum distance between this point and the DPath.
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
	 * Calculates the minimum distance between this point and a DPath (path of
	 * connected lines), storing the result in the provided array. This method
	 * iterates over each edge in the path and finds the minimum distance.
	 *
	 * @param l   The DPath to calculate the distance to.
	 * @param min An array where the calculated distance will be stored. The minimum
	 *            distance is stored in the first element.
	 * @return The same array passed as the parameter with the minimum distance
	 *         stored in the first element.
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
	 * Calculates the distance between this point and a DRectangle. Utilizes the
	 * distance(DRectangle, double[]) method for the calculation.
	 *
	 * @param r The DRectangle to calculate the distance to.
	 * @return The distance between this point and the DRectangle.
	 */
	public double distance(DRectangle r) {
		double[] retVal = new double[2];
		distance(r, retVal);
		return retVal[0];
	}

	/**
	 * Calculates the distance between this point and a DLine. Utilizes the
	 * distance(DLine, double[]) method for the calculation.
	 *
	 * @param l The DLine to calculate the distance to.
	 * @return The distance between this point and the DLine.
	 */
	public double distance(DLine l) {
		double[] retVal = new double[2];
		distance(l, retVal);
		return retVal[0];
	}

    /**
     * Calculates the distance between this point and a DLine, storing the result in the provided array.
     * This method considers various cases such as the point being on the line, the line being vertical, etc.
     *
     * @param l    The DLine to calculate the distance to.
     * @param dist An array where the calculated distance will be stored. 
     *             The distance is stored in the first element of the array.
     * @return The same array passed as the parameter with the calculated distance stored 
     *         in the first element.
     */
	public double[] distance(DLine l, double[] dist) {
		if (this.equals(l.p1) || this.equals(l.p2)) {
			dist[0] = dist[1] = 0;
			return dist;
		}

        dist[1] = 0; // The second element is not used in this context

        // Calculate the vector components of the line
		double vx, vy, vxp, vyp, t;
		vx = l.p2.x - l.p1.x;
		vy = l.p2.y - l.p1.y;

        // Special case handling for zero-length lines or vertical lines
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

        // General case: Compute the perpendicular distance from the point to the line
		vxp = vy;
		vyp = -vx;
		t = (x + vxp / vyp * (l.p1.y - y) - l.p1.x) / (vx - vy * vxp / vyp);
		if (t < 0 || t > 1) {
			dist[0] = (Math.min(distance(l.p1), distance(l.p2)));
		} else {
			double d1 = x - (l.p1.x + t * vx);
			double d2 = y - (l.p1.y + t * vy);
			dist[0] = Math.sqrt(d1 * d1 + d2 * d2);
		}
		return dist;
	}

    /**
     * Calculates the distance between this point and a DRectangle, storing the result in the provided array.
     * Considers whether the point is inside the rectangle and calculates the nearest edge distance accordingly.
     *
     * @param r      The DRectangle to calculate the distance to.
     * @param retVal An array where the calculated distance will be stored. 
     *               The distance is stored in the first element of the array.
     * @return The same array passed as the parameter with the calculated distance stored 
     *         in the first element.
     */
	public double[] distance(DRectangle r, double[] retVal) {
        // Implementation for calculating the distance from the point to the rectangle
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

    /**
     * Calculates the distance between this point and a DPolygon.
     * Returns the minimum distance from the point to any edge of the polygon.
     *
     * @param g The DPolygon to calculate the distance to.
     * @return The minimum distance between this point and the DPolygon.
     */
	public double distance(DPolygon g) {
		double[] k = new double[2];
		distance(g, k);
		return k[0];
	}

    /**
     * Calculates the distance between this point and a DPolygon, storing the result in the provided array.
     * Determines whether the point is inside the polygon and calculates the nearest edge distance if outside.
     *
     * @param g    The DPolygon to calculate the distance to.
     * @param keys An array where the calculated distance will be stored. 
     *             The minimum distance is stored in the first element.
     * @return The same array passed as the parameter with the calculated distance stored 
     *         in the first element.
     */
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

		arc = new DLine(this, outside); // draw an arc to outside the box

		for (int i = g.Size() - 1; i >= 0; --i) {
			edge = g.Edge(i);
			thisdist = distance(edge);
			if (dist > thisdist)
				dist = thisdist;
			if (arc.intersects(edge))
				intersections++;
		}

		if (intersections % 2 != 0) {
			keys[0] = 0; // inside
			keys[1] = dist;
		} else
			keys[0] = dist;

		return keys;
	}

	// ---------------------------------------
    // Constant representing the point size for drawing
	protected final int PS = 6;

	/**
	 * Draws the point on a given DrawingTarget. Implements the method from the
	 * Drawable interface.
	 *
	 * @param g The DrawingTarget on which to draw the point.
	 */
	public void draw(DrawingTarget g) {
		g.fillOval(x, y, PS, PS);
	}

	/**
	 * Draws the point on a given DrawingTarget with a specific color. Directly
	 * draws the point bypassing any buffering mechanisms.
	 *
	 * @param c The color to draw the point.
	 * @param g The DrawingTarget on which to draw the point.
	 */
	public void directDraw(Color c, DrawingTarget g) {
		g.directFillOval(c, x, y, PS, PS);
	}

	/**
	 * Gets the bounding rectangle of the point. Since a point has no area, the
	 * bounding rectangle is effectively a point itself.
	 *
	 * @return A DRectangle representing the bounding box of this point.
	 */
	public DRectangle getBB() {
		return new DRectangle(x, y, 0, 0);
	}

	/**
	 * Checks if the point has an area. Always returns false as a point does not
	 * cover an area.
	 *
	 * @return false, indicating the point has no area.
	 */
	public boolean hasArea() {
		return false;
	}

	/**
	 * Provides a string representation of the point.
	 *
	 * @return A string representing the coordinates of the point.
	 */
	public String toString() {
		return "DPoint: [" + x + ", " + y + "];";
	}

	// Constants defining classification types of a point relative to a line segment
	final static int InBetween = 0; // Indicates the point is between the line segment's endpoints
	final static int BeforeFirst = 1; // Indicates the point is before the first endpoint of the line segment
	final static int AfterSecond = 2; // Indicates the point is after the second endpoint of the line segment

	/**
	 * Classifies this point's position relative to a line segment. Determines
	 * whether the point is within the line segment, before the first endpoint, or
	 * after the second endpoint.
	 *
	 * @param l The DLine (line segment) against which the point is classified.
	 * @return An integer representing the classification: InBetween if the point
	 *         lies within the segment, BeforeFirst if the point is closer to the
	 *         first endpoint, AfterSecond if the point is closer to the second
	 *         endpoint.
	 */
	public int classify(DLine l)
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
		double denom = lk.x * lk.x + lk.y * lk.y;
		if (denom < CommonConstants.accuracy) {
			return InBetween;  // The line segment is very small, treat the point as being within it
		} else {
			double t = -(kj.x * lk.x + kj.y * lk.y) / denom;
			if (t < -CommonConstants.accuracy)
				return BeforeFirst; // The point is closer to the first endpoint
			if (t - 1 > CommonConstants.accuracy)
				return AfterSecond; // The point is closer to the second endpoint
			return InBetween; // The point lies within the segment
		}
	}

	/**
	 * Draws a buffer around the point. The buffer is drawn as an oval centered on
	 * the point with the specified distance as the radius.
	 *
	 * @param c    The color to draw the buffer.
	 * @param dt   The DrawingTarget on which to draw the buffer.
	 * @param dist The distance from the point to the edge of the buffer.
	 */
	public void drawBuffer(Color c, DrawingTarget dt, double dist) {
		dt.setColor(c);
		dt.drawOval(this, dist, dist);
	}

	// ----------------------- intersections -------------
    /**
     * Checks if the point intersects with a given rectangle.
     * Essentially, it checks if the point is contained within the rectangle.
     *
     * @param r The DRectangle to check intersection with.
     * @return true if the point is inside the rectangle, false otherwise.
     */
	public boolean intersects(DRectangle r) {
		return r.contains(this);
	}

}
