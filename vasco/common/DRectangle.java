/* $Id: DRectangle.java,v 1.2 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.*;
import vasco.drawable.*;

public class DRectangle implements Drawable, ArealObject {

	public double x, y, width, height;

	public DRectangle() {
		this(0, 0, 0, 0);
	}

	public DRectangle(DRectangle r) {
		this(r.x, r.y, r.width, r.height);
	}

	public DRectangle(DPoint p1, DPoint p2) {
		this(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
	}

	public DRectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DRectangle) {
			DRectangle r = (DRectangle) obj;
			return (x == r.x) && (y == r.y) && (width == r.width) && (height == r.height);
		}
		return false;
	}

	public DPoint SWcorner() {
		return new DPoint(x, y);
	}

	public DPoint SEcorner() {
		return new DPoint(x + width, y);
	}

	public DPoint NWcorner() {
		return new DPoint(x, y + height);
	}

	public DPoint NEcorner() {
		return new DPoint(x + width, y + height);
	}

	public DLine Nside() {
		return new DLine(x, y + height, x + width, y + height);
	}

	public DLine Sside() {
		return new DLine(x, y, x + width, y);
	}

	public DLine Wside() {
		return new DLine(x, y, x, y + height);
	}

	public DLine Eside() {
		return new DLine(x + width, y, x + width, y + height);
	}

	// -------------- INTERSECTS --------------------
	public boolean intersects(DLine dl) {
		return CSquare(dl.p1.x, dl.p1.y, dl.p2.x, dl.p2.y);
	}

	public boolean intersects(DRectangle r) {
		return !((r.x + r.width <= x) || (r.y + r.height <= y) || (r.x >= x + width) || (r.y >= y + height));
	}

	public boolean intersects(Rectangle r) {
		return !((r.x + r.width <= x) || (r.y + r.height <= y) || (r.x >= x + width) || (r.y >= y + height));
	}

	public boolean intersects(DPoint p) {
		return contains(p);
	}

	// ------------- CONTAINS ----------------

	public boolean contains(DPoint p) {
		return (p.x >= this.x) && ((p.x - this.x) <= this.width) && (p.y >= this.y) && ((p.y - this.y) <= this.height);
	}

	public boolean contains(DRectangle b) {
		return contains(new DPoint(b.x, b.y)) && contains(new DPoint(b.x + b.width, b.y))
				&& contains(new DPoint(b.x, b.y + b.height)) && contains(new DPoint(b.x + b.width, b.y + b.height));
	}

	public boolean contains(DLine dl) {
		return contains(dl.p1) && contains(dl.p2);
	}

	public boolean contains(DPolygon g) {
		return contains(g.getBB());
	}

	public boolean contains(DPath p) {
		for (int i = 0; i < p.Size() - 1; i++)
			if (!contains(p.Edge(i)))
				return false;
		return true;
	}

	// -------------------------------------

	public double getArea() {
		return width * height;
	}

	public double getPerimeter() {
		return 2 * width + 2 * height;
	}

	public DRectangle union(DRectangle r) {
		double x1 = Math.min(x, r.x);
		double x2 = Math.max(x + width, r.x + r.width);
		double y1 = Math.min(y, r.y);
		double y2 = Math.max(y + height, r.y + r.height);
		return new DRectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public DRectangle intersection(DRectangle r) {
		double x1 = Math.max(x, r.x);
		double x2 = Math.min(x + width, r.x + r.width);
		double y1 = Math.max(y, r.y);
		double y2 = Math.min(y + height, r.y + r.height);
		return new DRectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public double[] distance(DPoint p, double[] d) {
		p.distance(this, d);
		return d;
	}

	public double distance(DPoint p) {
		return p.distance(this);
	}

	public double distance(DLine l) {
		return l.distance(this);
	}

	public double[] distance(DLine r, double[] k) {
		r.distance(this, k);
		return k;
	}

	public double distance(DRectangle r) {
		if (intersects(r))
			return 0;

		double min = Math.min(r.distance(new DPoint(x, y)), r.distance(new DPoint(x + width, y)));
		min = Math.min(min, r.distance(new DPoint(x, y + height)));
		min = Math.min(min, r.distance(new DPoint(x + width, y + height)));
		min = Math.min(min, distance(new DPoint(r.x, r.y)));
		min = Math.min(min, distance(new DPoint(r.x + r.width, r.y)));
		min = Math.min(min, distance(new DPoint(r.x, r.y + r.height)));
		min = Math.min(min, distance(new DPoint(r.x + r.width, r.y + r.height)));

		return min;

	}

	public double[] distance(DRectangle r, double[] k) {
		k[0] = distance(r);
		// k[1] = 0;
		k[1] = r.x + r.y + x + y; // arbitrary code to recognize identical rectangles
		// occasionally it can indicate non-identical rectangles to be identical too
		return k;
	}

	public double[] distance(DPolygon r, double[] k) {
		k[0] = distance(r);
		k[1] = x + y + r.FirstCorner().x + r.FirstCorner().y;
		return k;
	}

	public double distance(DPolygon g) {
//     ----------------
//
//  Returns Euclidean distance between r and g
//

		double thisdist;
		double dist = Double.MAX_VALUE;
		DLine edge;
		DPoint c1 = g.LastCorner();
		DPoint c2 = g.FirstCorner();

		// Check if one vertex of the polygon is inside rectangle

		if (contains(c1) || contains(c2))
			return 0.0;

		// first check to see if any part of the border intersects the rectangle

		for (int i = g.Size(); i > 0; --i) {
			edge = new DLine(c1, c2);
			thisdist = distance(edge);
			if (dist > thisdist)
				dist = thisdist;
			if (dist == 0.0)
				return dist;
			c1 = c2;
			c2 = g.NextCorner();
		}

		// if it passes through the above loop, no border line intersects
		// the rectangle. Now we need to check for point in polygon to
		// eliminate wholly enclosing case

		DPoint justamincorner = SWcorner();
		if (g.contains(justamincorner))
			return 0.0;

		return dist;
	}

	// ----------- intersection block --------------------

	// --------------------

	private final int left = 1;
	private final int right = 2;
	private final int up = 4;
	private final int down = 8;

	private boolean CSquare(double x1, double y1, double x2, double y2) {
		boolean done;
		int code1, code2;
		double m;

		done = false;
		while (!done) {
			code1 = ClipArea(x1, y1);
			code2 = ClipArea(x2, y2);

			if (code1 + code2 == 0)
				return true;
			if ((code1 & code2) != 0)
				return false;
			if (code1 == 0) {
				double p;
				int c;
				p = x1;
				x1 = x2;
				x2 = p;
				p = y1;
				y1 = y2;
				y2 = p;
				c = code1;
				code1 = code2;
				code2 = c;
			}
			m = (y2 - y1) / (x2 - x1); // test zero division
			if ((code1 & left) != 0) {
				y1 += (this.x - x1) * m;
				x1 = this.x;
			} else {
				if ((code1 & right) != 0) {
					y1 += (this.x + this.width - x1) * m;
					x1 = this.x + this.width;
				} else {
					if ((code1 & down) != 0) {
						x1 += (this.y - y1) / m;
						y1 = this.y;
					} else {
						if ((code1 & up) != 0) {
							x1 += (this.y + this.height - y1) / m;
							y1 = this.y + this.height;
						}
					}
				}
			}
		}
		return true;
	}

	private int ClipArea(double xx, double yy) {
		int c = 0;
		if (xx < this.x)
			c = left;
		else if (xx > this.x + this.width)
			c = right;
		if (yy < this.y)
			c |= down;
		else if (yy > this.y + this.height)
			c |= up;
		return c;
	}

	// ---------------------------------

	public void draw(DrawingTarget g) {
		g.drawRect(x, y, width, height);
	}

	public void directDraw(Color c, DrawingTarget g) {
		g.directRect(c, x, y, width, height);
	}

	public DRectangle getBB() {
		return this;
	}

	public boolean hasArea() {
		return true;
	}

	public String toString() {
		return getClass().getName() + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
	}

	public void drawBuffer(Color c, DrawingTarget dt, double dist) {
		dt.setColor(c);
		dt.drawLine(x - dist, y, x - dist, y + height);
		dt.drawLine(x + width + dist, y, x + width + dist, y + height);
		dt.drawLine(x, y - dist, x + width, y - dist);
		dt.drawLine(x, y + height + dist, x + width, y + height + dist);
		dt.drawArc(x - dist, y - dist, 2 * dist, 2 * dist, 180, -90);
		dt.drawArc(x - dist, y + height - dist, 2 * dist, 2 * dist, 180, 90);
		dt.drawArc(x + width - dist, y - dist, 2 * dist, 2 * dist, 0, 90);
		dt.drawArc(x + width - dist, y + height - dist, 2 * dist, 2 * dist, 0, -90);
	}

}
