package vasco.points;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: MXTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
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

public class MXTree extends PointStructure implements MaxDecompIface {
	static final int NRDIRS = 4;

	public MXTree(DRectangle can, int md, TopInterface ti, RebuildTree r) {
		super(can, ti, r);
		maxDecomp = md;
		canvasWidth = (int) Math.pow(2, maxDecomp);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		new MaxDecomp(topInterface, 9, this);
		ao.addItem("Nearest");
		ao.addItem("Within");
	}

	@Override
	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	@Override
	public String getName() {
		return "MX Quadtree";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

	@Override
	public boolean Insert(DPoint p) {
		boolean[] ok = new boolean[1];
		MXNode mx = new MXNode();
		mx.point = p;
		Point mxp = toMXPoint(p);
		ROOT = insert(mx, mxp.x, mxp.y, ROOT, canvasWidth, maxDecomp, ok);
		return ok[0];
	}

	@Override
	public void Delete(DPoint p) {
		if (ROOT == null)
			return;

		MXIncNearest kdin = new MXIncNearest(ROOT);
		Point mx = toMXPoint(kdin.QueryReal(new QueryObject(p)));
		ROOT = delete(mx.x, mx.y, ROOT, canvasWidth);
	}

	@Override
	public void DeleteDirect(Drawable p) {
		if (ROOT == null)
			return;
		Point mx = toMXPoint((DPoint) p);
		ROOT = delete(mx.x, mx.y, ROOT, canvasWidth);
	}

	@Override
	public SearchVector Search(QueryObject q, int mode) {
		SearchVector res = new SearchVector();
		searchVector = new Vector();
		search(ROOT, q, canvasWidth / 2, canvasWidth / 2, canvasWidth, mode, res);
		return res;
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		MXIncNearest near = new MXIncNearest(ROOT);
		return near.Query(p, dist);
	}

	public class MXDPoint extends DPoint {
		DPoint filled;

		MXDPoint(DPoint p, DPoint filled) {
			super(p.x, p.y);
			this.filled = filled;
		}

		@Override
		public void directDraw(Color c, DrawingTarget g) {
			g.setColor(c);
			g.directDrawOval(c, x, y, PS, PS);
			filled.directDraw(c, g);
		}
	}

	@Override
	public Drawable NearestFirst(QueryObject p) {
		if (ROOT == null)
			return null;
		MXIncNearest mxin = new MXIncNearest(ROOT);
		return mxin.QueryReal(p);
	}

	@Override
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			MXIncNearest mxin = new MXIncNearest(ROOT);
			mxin.Query(p, v);
		}
		return v;
	}

	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			MXIncNearest mxin = new MXIncNearest(ROOT);
			mxin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	@Override
	public void drawContents(DrawingTarget g, Rectangle view) {
		drawC(ROOT, g, 0, 0, canvasWidth);
	}

	/* ---- interface implementation ------ */

	@Override
	public int getMaxDecomp() {
		return maxDecomp;
	}

	@Override
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		canvasWidth = (int) Math.pow(2, maxDecomp);
		reb.rebuild();
	}

	/* ------------ private methods ------------ */

	private class MXNode implements CommonConstants {
		int NODETYPE;
		MXNode SON[] = new MXNode[NRDIRS];
		DPoint point; // for coordination with other data structures

		MXNode() {
			NODETYPE = BLACK;
			for (int i = 0; i < NRDIRS; i++)
				SON[i] = null;
		}

		MXNode(int type) {
			NODETYPE = type;
			for (int i = 0; i < NRDIRS; i++)
				SON[i] = null;
		}
	}

	MXNode ROOT;
	int maxDecomp;
	int canvasWidth;

	double toMXdist(double dist) {
		return canvasWidth * dist / wholeCanvas.width; // assume square
	}

	Point toMXPoint(DPoint p) {
		return new Point((int) (canvasWidth * (p.x - wholeCanvas.x) / wholeCanvas.width),
				(int) (canvasWidth * (p.y - wholeCanvas.y) / wholeCanvas.height));
	}

	Rectangle toMXRectangle(DRectangle r) {
		Point p1 = toMXPoint(new DPoint(r.x, r.y));
		Point p2 = toMXPoint(new DPoint(r.x + r.width, r.y + r.height));
		return new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
	}

	DPoint toDPoint(Point p) {
		return new DPoint(wholeCanvas.x + wholeCanvas.width * p.x / canvasWidth,
				wholeCanvas.y + wholeCanvas.height * p.y / canvasWidth);
	}

	DRectangle toDRectangle(Rectangle r) {
		DPoint p1 = toDPoint(new Point(r.x, r.y));
		DPoint p2 = toDPoint(new Point(r.x + r.width, r.y + r.height));
		return new DRectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
	}

	// ---------------------------------------------------------------------------------

	private int MXCompare(int X, int Y, int W) {
		if (X < W)
			return Y < W ? SW : NW;
		else
			return Y < W ? SE : NE;
	}

	Vector searchVector;

	private void search(MXNode R, QueryObject qu, double cx, double cy, int w, int mode, SearchVector v) {
		v.addElement(new SVElement(
				new YellowBlock(toDRectangle(new Rectangle((int) (cx - w / 2.0), (int) (cy - w / 2.0), w, w)),
						R == null || R.NODETYPE == BLACK),
				searchVector));
		if (R == null)
			return;

		if (R.NODETYPE == BLACK) {
			DPoint pnt = toDPoint(new Point((int) (cx - w / 2), (int) (cy - w / 2)));
			v.addElement(new SVElement(new GreenPoints(pnt), searchVector));
			drawableInOut(qu, pnt, mode, v, searchVector);
			return;
		}

		DRectangle ne = new DRectangle(toDRectangle(new Rectangle((int) cx, (int) cy, w / 2, w / 2)));
		DRectangle se = new DRectangle(toDRectangle(new Rectangle((int) cx, (int) (cy - w / 2.0), w / 2, w / 2)));
		DRectangle nw = new DRectangle(toDRectangle(new Rectangle((int) (cx - w / 2.0), (int) cy, w / 2, w / 2)));
		DRectangle sw = new DRectangle(
				toDRectangle(new Rectangle((int) (cx - w / 2.0), (int) (cy - w / 2.0), w / 2, w / 2)));

		if (qu.intersects(ne))
			searchVector.addElement(ne);
		if (qu.intersects(se))
			searchVector.addElement(se);
		if (qu.intersects(nw))
			searchVector.addElement(nw);

		if (qu.intersects(sw))
			search(R.SON[SW], qu, cx - w / 4.0, cy - w / 4.0, w / 2, mode, v);
		if (qu.intersects(nw)) {
			searchVector.removeElementAt(searchVector.size() - 1);
			search(R.SON[NW], qu, cx - w / 4.0, cy + w / 4.0, w / 2, mode, v);
		}
		if (qu.intersects(se)) {
			searchVector.removeElementAt(searchVector.size() - 1);
			search(R.SON[SE], qu, cx + w / 4.0, cy - w / 4.0, w / 2, mode, v);
		}
		if (qu.intersects(ne)) {
			searchVector.removeElementAt(searchVector.size() - 1);
			search(R.SON[NE], qu, cx + w / 4.0, cy + w / 4.0, w / 2, mode, v);
		}
	}

	private MXNode insert(MXNode P, int X, int Y, MXNode R, int W, int md, boolean[] ok) {
		// operates in integer coordinates <0 .. W>
		MXNode T;
		int Q;
		ok[0] = true;

		if (W == 1)
			return P;
		else if (R == null)
			R = new MXNode(GRAY);
		T = R;
		W /= 2;
		Q = MXCompare(X, Y, W);
		while (W > 1 && md > 1) {
			if (T.SON[Q] == null)
				T.SON[Q] = new MXNode(GRAY);
			T = T.SON[Q];
			X %= W;
			Y %= W;
			W /= 2;
			Q = MXCompare(X, Y, W);
			md--;
		}
		if (T.SON[Q] == null)
			T.SON[Q] = P;
		else
			ok[0] = false;
		return R;
	}

	private MXNode delete(int X, int Y, MXNode R, int W) {
		int Q, QF = -1;
		MXNode F, T, temp;

		if (R == null || W == 1)
			return null;
		T = R;
		F = null;
		do {
			W /= 2;
			Q = MXCompare(X, Y, W);
			if (!(T.SON[CQuad(Q)] == null && T.SON[OpQuad(Q)] == null && T.SON[CCQuad(Q)] == null)) {
				F = T;
				QF = Q;
			}
			T = T.SON[Q];
			X %= W;
			Y %= W;
		} while (W != 1 && T != null);

		if (T == null)
			return R;

		T = (F == null) ? R : F.SON[QF];
		Q = NW;
		while (T.NODETYPE == GRAY) {
			while (T.SON[Q] == null) {
				Q = CQuad(Q);
			}
			temp = T.SON[Q];
			T.SON[Q] = null;
			T = temp;
		}
		if (F == null)
			R = null;
		else
			F.SON[QF] = null;

		return R;
	}

	private void drawC(MXNode r, DrawingTarget g, int minx, int miny, int w) {
		DRectangle dr = toDRectangle(new Rectangle(minx, miny, w, w));
		if (!g.visible(dr))
			return;

		g.setColor(Color.black);
		dr.draw(g);

		if (r == null)
			return;

		if (r.NODETYPE == BLACK) {
			g.setColor(Color.red);
			DPoint drp = new DPoint(dr.x, dr.y);
			drp.draw(g);
			g.drawOval(r.point.x, r.point.y, 6, 6);
		} else {
			if (r.NODETYPE == GRAY) {
				drawC(r.SON[0], g, minx, miny + w / 2, w / 2);
				drawC(r.SON[1], g, minx + w / 2, miny + w / 2, w / 2);
				drawC(r.SON[2], g, minx, miny, w / 2);
				drawC(r.SON[3], g, minx + w / 2, miny, w / 2);
			}
		}
	}

	// ----------------------- Incremental Nearest -----------------

	class MXQueueElement {
		double key;
		MXNode r;
		double cx, cy;
		double w;
		boolean isElem;

		MXQueueElement(double k, MXNode p, double x, double y) {
			key = k;
			r = p;
			cx = x;
			cy = y;
			isElem = true;
		}

		MXQueueElement(double k, MXNode p, double x, double y, double ww) {
			key = k;
			r = p;
			cx = x;
			cy = y;
			w = ww;
			isElem = false;
		}
	}

	class MXQueue {
		Vector v;

		MXQueue() {
			v = new Vector();
		}

		void Enqueue(MXQueueElement qe) {
			v.addElement(qe);
			for (int i = v.size() - 1; i > 0; i--) {
				MXQueueElement q1 = (MXQueueElement) v.elementAt(i - 1);
				MXQueueElement q2 = (MXQueueElement) v.elementAt(i);
				if (q1.key > q2.key) {
					v.setElementAt(q2, i - 1);
					v.setElementAt(q1, i);
				}
			}
		}

		MXQueueElement Dequeue() {
			MXQueueElement q = (MXQueueElement) v.elementAt(0);
			v.removeElementAt(0);
			return q;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		Vector makeVector() {
			Vector r = new Vector();
			for (int i = 0; i < v.size(); i++) {
				MXQueueElement q = (MXQueueElement) v.elementAt(i);
				if (q.isElem)
					r.addElement(new GreenPoints(new DPoint(q.cx, q.cy)));
				else
					r.addElement(new QueueBlock(new DRectangle(q.cx - q.w / 2, q.cy - q.w / 2, q.w, q.w)));
			}
			return r;
		}

	}

	class MXIncNearest implements CommonConstants {

		MXQueue q;

		MXIncNearest(MXNode rt) {
			q = new MXQueue();
			q.Enqueue(new MXQueueElement(0, rt, wholeCanvas.width / 2, wholeCanvas.height / 2, wholeCanvas.width));
		}

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

		DPoint[] Query(QueryObject dqu, SearchVector ret, double dist, int nrelems) {
			// distance in MX coordinate system [0,0]-[2^n-1, 2^n-1]
			Vector pts = new Vector();
			final double XF[] = { -4, 4, -4, 4 };
			final double YF[] = { 4, 4, -4, -4 };
			int counter = 1;
			// DPoint dqu = new DPoint(qu.x, qu.y);

			while (!q.isEmpty()) {
				MXQueueElement element = q.Dequeue();
				if (element.isElem) {
					DPoint epnt = new DPoint(element.cx, element.cy);
					if (nrelems-- <= 0 || dqu.distance(epnt) > dist)
						break;
					pts.addElement(epnt);
					ret.addElement(new NNElement(new NNDrawable(new DPoint(element.cx, element.cy), counter++),
							element.key, q.makeVector()));
				} else {
					ret.addElement(
							new NNElement(
									new YellowBlock(new DRectangle(element.cx - element.w / 2,
											element.cy - element.w / 2, element.w, element.w), false),
									element.key, q.makeVector()));
					if (element.r.NODETYPE == BLACK) {
						q.Enqueue(new MXQueueElement(
								dqu.distance(new DPoint(element.cx - element.w / 2.0, element.cy - element.w / 2.0)),
								element.r, element.cx - element.w / 2, element.cy - element.w / 2));
					} else if (element.r.NODETYPE == GRAY) {
						for (int i = 0; i < 4; i++)
							if (element.r.SON[i] != null)
								q.Enqueue(new MXQueueElement(
										dqu.distance(new DRectangle(element.cx + element.w / XF[i] - element.w / 4.0,
												element.cy + element.w / YF[i] - element.w / 4.0, element.w / 2,
												element.w / 2)),
										element.r.SON[i], element.cx + element.w / XF[i],
										element.cy + element.w / YF[i], element.w / 2));
					}
				}
			}
			DPoint[] ar = new DPoint[pts.size()];
			pts.copyInto(ar);
			return ar;
		}

		MXDPoint QueryReal(QueryObject dqu) {
			// distance in MX coordinate system [0,0]-[2^n-1, 2^n-1]
			final double XF[] = { -4, 4, -4, 4 };
			final double YF[] = { 4, 4, -4, -4 };
			// DPoint dqu = new DPoint(qu.x, qu.y);

			while (!q.isEmpty()) {
				MXQueueElement element = q.Dequeue();
				if (element.isElem) {
					return new MXDPoint(element.r.point, new DPoint(element.cx, element.cy));
				} else {
					if (element.r.NODETYPE == BLACK) {
						q.Enqueue(new MXQueueElement(dqu.distance(element.r.point), element.r,
								element.cx - element.w / 2, element.cy - element.w / 2));

					} else if (element.r.NODETYPE == GRAY) {
						for (int i = 0; i < 4; i++)
							if (element.r.SON[i] != null)
								q.Enqueue(new MXQueueElement(
										dqu.distance(new DRectangle(element.cx + element.w / XF[i] - element.w / 4.0,
												element.cy + element.w / YF[i] - element.w / 4.0, element.w / 2,
												element.w / 2)),
										element.r.SON[i], element.cx + element.w / XF[i],
										element.cy + element.w / YF[i], element.w / 2));
					}
				}
			}
			return null;
		}
	}

}
