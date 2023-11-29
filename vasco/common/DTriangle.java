/**
 *
 */
package vasco.common;

import java.util.ArrayList;

import vasco.points.randy.Site;

public class DTriangle extends DPolygon implements java.lang.Comparable {
	public DTriangle[] adjacencies = new DTriangle[3];
	public DPoint circumcircleCenter;
	public double circumcircleRadius;

	public DTriangle(Site[] pts) throws Exception {
		super(pts);
		if (pts.length != 3)
			throw new Exception("Not the right number of vertices for a triangle");
		resortPoints();
		computeCircumcircle();
	}

	public DTriangle(Site[] pts, DTriangle[] adjacencies) throws Exception {
		this(pts);
		this.adjacencies = adjacencies;
	}

	public ArrayList getDrawableLines(DPoint[] fixedPoints) {
		ArrayList toReturn = new ArrayList();
		for (int i = 0; i < 3; i++) {
			boolean dodraw = true;
			for (DPoint fixedPoint : fixedPoints) {
				if (border[i].equals(fixedPoint) || border[(i + 1) % 3].equals(fixedPoint))
					dodraw = false;
			}
			if (dodraw)
				toReturn.add(new DLine(border[i], border[(i + 1) % 3]));
		}
		return toReturn;
	}

	public void draw(DrawingTarget g, DPoint[] fixedPoints) {
		for (int i = 0; i < 3; i++) {
			boolean dodraw = true;
			for (DPoint fixedPoint : fixedPoints) {
				if (border[i].equals(fixedPoint) || border[(i + 1) % 3].equals(fixedPoint))
					dodraw = false;
			}
			if (dodraw)
				(new DLine(border[i], border[(i + 1) % 3])).draw(g);
		}
	}

	public void addAllEdges() {
		for (int i = 0; i < this.border.length; i++) {
			Site s1 = (Site) this.border[i];
			for (int j = i + 1; j < this.border.length; j++) {
				Site s2 = (Site) this.border[j];
				s1.addEdge(s2);
				s2.addEdge(s1);
			}
		}
	}

	public void removeAllEdges() {
		for (int i = 0; i < this.border.length; i++) {
			Site s1 = (Site) this.border[i];
			for (int j = i + 1; j < this.border.length; j++) {
				Site s2 = (Site) this.border[j];
				s1.removeEdge(s2);
				s2.removeEdge(s1);
			}
		}
	}

	public DTriangleEdge getEdge(Integer edge) {
		if (edge == null)
			return null;
		else {
			int e = edge.intValue();
			if (e == 1)
				return new DTriangleEdge((Site) border[0], (Site) border[1]);
			else if (e == 2)
				return new DTriangleEdge((Site) border[0], (Site) border[2]);
			else
				return new DTriangleEdge((Site) border[1], (Site) border[3]);
		}
	}

	private void resortPoints() {
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 3; j++) {
				if (comparePoints(border[i], border[j]) == 1) {
					DPoint tmp = border[i];
					border[i] = border[j];
					border[j] = tmp;
				}
			}
		}
	}

	public void setAdjacency(int index, DTriangle tri) {
		// System.out.println(toString()+" adjacency "+index+" <- "+tri);
		this.adjacencies[index] = tri;
	}

	public DTriangle getAdjacency(int index) {
		return this.adjacencies[index];
	}

	@Override
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof DTriangle) {
			DTriangle curr = (DTriangle) o;
			int i1 = comparePoints(border[0], curr.border[0]);
			if (i1 != 0)
				return i1;
			else {
				int i2 = comparePoints(border[1], curr.border[1]);
				if (i2 != 0)
					return i2;
				else {
					return comparePoints(border[2], curr.border[2]);
				}
			}
		} else
			return -1;
	}

	public int comparePoints(DPoint dp1, DPoint dp2) {
		return new DPointWrapper(dp1).compareTo(dp2);
	}

	public boolean circumcircleContains(DPoint p) {
		if (circumcircleCenter.distance(p) < circumcircleRadius)
			return true;
		else
			return false;
	}

	private void computeCircumcircle() {
		double amag = border[0].x * border[0].x + border[0].y * border[0].y;
		double bmag = border[1].x * border[1].x + border[1].y * border[1].y;
		double cmag = border[2].x * border[2].x + border[2].y * border[2].y;

		double[] avals = { border[0].x, border[0].y, 1, border[1].x, border[1].y, 1, border[2].x, border[2].y, 1 };
		double a = det(avals);

		double[] Sxvals = { amag, border[0].y, 1, bmag, border[1].y, 1, cmag, border[2].y, 1 };
		double Sx = .5 * det(Sxvals);

		double[] Syvals = { border[0].x, amag, 1, border[1].x, bmag, 1, border[2].x, cmag, 1 };
		double Sy = .5 * det(Syvals);

		double[] bvals = { border[0].x, border[0].y, amag, border[1].x, border[1].y, bmag, border[2].x, border[2].y,
				cmag };
		double b = det(bvals);

		circumcircleCenter = new DPoint(Sx / a, Sy / a);
		circumcircleRadius = Math.sqrt(b / a + (Sx * Sx + Sy * Sy) / (a * a));
	}

	public DPoint[] getBorder() {
		return border;
	}

	private double det(double pt[]) {
		return pt[0] * (pt[5] * pt[7] - pt[4] * pt[8]) - pt[1] * (pt[6] * pt[5] - pt[3] * pt[8])
				+ pt[2] * (pt[6] * pt[4] - pt[3] * pt[7]);
	}

	public Integer getSharedEdge(DTriangle tri) {
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 3; j++) {
				if (tri.containsEdgeInOrder(border[i], border[j])) {
					if (i == 0 && j == 1)
						return new Integer(0);
					if (i == 0 && j == 2)
						return new Integer(1);
					if (i == 1 && j == 2)
						return new Integer(2);
				}
			}
		}
		return null;
	}

	public boolean containsEdgeInOrder(DPoint p1, DPoint p2) {// we're guaranteed that p1 comes before p2 in the array,
																// so...
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 3; j++) {
				if (comparePoints(p1, border[i]) == 0 && comparePoints(p2, border[j]) == 0)
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		// return
		// "{("+border[0].x+","+border[0].y+"),("+border[1].x+","+border[1].y+"),("+border[2].x+","+border[2].y+")}";
		return "{" + border[0].x + "," + border[1].x + "," + border[2].x + "}";
	}

	public DPoint getMidpoint() {
		return new DPoint((border[0].x + border[1].x + border[2].x) / 3, (border[0].y + border[1].y + border[2].y) / 3);
	}

	public void drawCircumcircle(DrawingTarget dt) {
		dt.drawOval(circumcircleCenter.x, circumcircleCenter.y, 2 * (int) circumcircleRadius,
				2 * (int) circumcircleRadius);
	}
}