/* $Id: DPath.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.Color;

//============================================================================
//
//  PATH
//
//============================================================================
import vasco.drawable.Drawable;

public class DPath implements Drawable {

	@Override
	public boolean hasArea() {
		return false;
	}

	@Override
	public void draw(DrawingTarget g) {
		for (int i = 0; i < Size() - 1; i++) {
			(new DLine(border[i], border[i + 1])).draw(g);
		}
	}

	@Override
	public void directDraw(Color c, DrawingTarget g) {
		for (int i = 0; i < Size() - 1; i++) {
			(new DLine(border[i], border[i + 1])).directDraw(c, g);
		}
	}

	private DPoint[] border; // vector of border points
	private DRectangle boundingbox; // bounding rectangle of polygon

	public int Size() {
		return border.length;
	} // Returns the number of vertices

	public DPoint vertex(int i) {
		return border[i]; // Returns i'th vertex
	}

	DLine Edge(int i) {
		return new DLine(border[i], border[i + 1]);
	}

	DPoint[] getborder() {
		return border;
	} // Returns a pointer to the
	// Array of points that
	// defines its border

	public DPath() {
		// -------
		//
		// Builds a null path

		border = new DPoint[0];
		boundingbox = new DRectangle(0, 0, 0, 0);
	}

	public DPath(DPath g) {
		// -------
		//
		border = new DPoint[g.Size()];
		for (int i = 0; i < g.Size(); i++)
			border[i] = g.border[i];
		boundingbox = g.boundingbox;
		// no need to check semantic constraints on the border, as they would
		// have been enforced when the copied polygon was originally created.
	}

	public DPath(DPoint[] vec) {
		// -------
		//
		int sz = vec.length;

		if (sz < 2) {
			System.err.println("DPath shorter than 2 vertices");
		}

		double xmax, ymax, xmin, ymin;
		int i; // loop variables

		border = new DPoint[sz];
		xmax = vec[0].x;
		xmin = vec[0].x;
		ymax = vec[0].y;
		ymin = vec[0].y;
		for (i = 0; i < sz; i++) {
			border[i] = vec[i];
			xmax = xmax > vec[i].x ? xmax : vec[i].x;
			xmin = xmin < vec[i].x ? xmin : vec[i].x;
			ymax = ymax > vec[i].y ? ymax : vec[i].y;
			ymin = ymin < vec[i].y ? ymin : vec[i].y;
		}
		boundingbox = new DRectangle(xmin, ymin, xmax - xmin, ymax - ymin);

		// assert( non_self_intersecting(*this) ); semantic constraints

	}

	@Override
	public DRectangle getBB() {
		// ---------------------
		//
		return boundingbox;
	}

	public boolean equals(DPath g) {
		int direction = 0; // do we look the same direction on both
		// polygons, or different ones?
		int firstmatch; // position of first match of points
		boolean reverse;

		if (Size() != g.Size())
			return false; // sizes aren't the same

		if (vertex(0).equals(g.vertex(0)))
			reverse = false;
		else if (vertex(0).equals(g.vertex(g.Size() - 1)))
			reverse = true;
		else
			return false;

		// check in direction indicated
		for (int i = 0; i < Size(); i++)
			if (!vertex(reverse ? Size() - i - 1 : i).equals(g.vertex(i)))
				return false; // a point didn't match
		return true; // all points matched

	}
	// ------------ INTERSECTS ---------------------

	public boolean intersects(DPoint p) {
		return distance(p) == 0;
	}

	public boolean intersects(DLine l) {
		// ----------
		//
		// Tells if line segment l intersects polygon g
		//
		DLine edge;

		if (!getBB().intersects(l))
			return false; // no intersection is possible

		for (int i = Size() - 2; i >= 0; --i) {
			edge = Edge(i);
			if (l.intersects(edge))
				return true; // we have an intersection
		}

		return false;
	}

	@Override
	public boolean intersects(DRectangle r) {
		// ----------
		//
		// Tell if a Rectangle intersects a Polygon
		//
		DLine edge;

		if (!r.intersects(getBB()))
			return false; // no intersection is possible

		if (r.contains(border[0]))
			return true; // At least one vertex of g is inside r

		// first check to see if any part of the border intersects the rectangle

		for (int i = Size() - 2; i >= 0; --i) {
			edge = Edge(i);
			if (r.intersects(edge))
				return true; // we have an intersection
		}

		return false;
	}

	// -------------- DISTANCE ----------------

	@Override
	public double distance(DPoint p) {
		return p.distance(this);
	}

	@Override
	public double[] distance(DPoint p, double[] k) {
		p.distance(this, k);
		return k;
	}

	@Override
	public double distance(DLine l) {
		return l.distance(this);
	}

	@Override
	public double[] distance(DLine l, double[] k) {
		l.distance(this, k);
		return k;
	}

	// distance to another path should not be necessary
	public double distance(DPath l) {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < Size() - 1; i++) {
			double d = Edge(i).distance(l);
			if (d < min)
				min = d;
		}
		return min;
	}

	public double[] distance(DPath l, double[] min) {
		double[] keys = new double[2];

		Edge(0).distance(l, min);
		for (int i = 1; i < Size() - 1; i++) {
			Edge(i).distance(l, keys);
			if (keys[0] < min[0] || (keys[0] == min[0] && keys[1] < min[1])) {
				min[0] = keys[0];
				min[1] = keys[1];
			}
		}

		min[1] = 0;
		return min;
	}

	@Override
	public double distance(DRectangle l) {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < Size() - 1; i++) {
			double d = Edge(i).distance(l);
			if (d < min)
				min = d;
		}
		return min;
	}

	@Override
	public double[] distance(DRectangle l, double[] min) {
		double[] keys = new double[2];

		Edge(0).distance(l, min);
		for (int i = 1; i < Size() - 1; i++) {
			Edge(i).distance(l, keys);
			if (keys[0] < min[0] || (keys[0] == min[0] && keys[1] < min[1])) {
				min[0] = keys[0];
				min[1] = keys[1];
			}
		}
		min[1] = 0;
		return min;
	}

	public double distance(DPolygon l) {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < Size() - 1; i++) {
			double d = Edge(i).distance(l);
			if (d < min)
				min = d;
		}
		return min;
	}

	public double[] distance(DPolygon l, double[] min) {
		double[] keys = new double[2];

		Edge(0).distance(l, min);
		for (int i = 1; i < Size() - 1; i++) {
			Edge(i).distance(l, keys);
			if (keys[0] < min[0] || (keys[0] == min[0] && keys[1] < min[1])) {
				min[0] = keys[0];
				min[1] = keys[1];
			}
		}
		min[1] = 0;
		return min;
	}

	@Override
	public void drawBuffer(Color c, DrawingTarget dt, double dist) {
		// empty till the real algorithm is developed
	}

}
