package vasco.points;

/* $Id: GenericPRbucket.java,v 1.2 2007/10/28 15:38:17 jagan Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import java.awt.*;
import java.util.*;

abstract class GenericPRbucket extends PointStructure implements MaxDecompIface, BucketIface {

	class PRbucketNode {
		int NODETYPE;
		Vector points;
		PRbucketNode SON[] = new PRbucketNode[4];

		PRbucketNode(int type) {
			NODETYPE = type;
			points = new Vector();
			for (int i = 0; i < 4; i++)
				SON[i] = null;
		}

		boolean isIn(DPoint p) {
			for (int i = 0; i < points.size(); i++) {
				DPoint s = (DPoint) points.elementAt(i);
				if (s.equals(p))
					return true;
			}
			return false;
		}

		void addPoint(DPoint p) {
			if (!isIn(p))
				points.addElement(p);
		}

		void deletePoint(DPoint p) {
			for (int i = 0; i < points.size(); i++) {
				DPoint s = (DPoint) points.elementAt(i);
				if (s.equals(p)) {
					points.removeElementAt(i);
					return;
				}
			}
		}

	}

	PRbucketNode ROOT;
	int maxDecomp;
	int maxBucketSize;

	GenericPRbucket(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
		maxDecomp = md;
		maxBucketSize = b;
	}

	public void reInit(Choice ao) {
		super.reInit(ao);
		new MaxDecomp(topInterface, 9, this);
		ao.addItem("Nearest");
		ao.addItem("Within");
	}

	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	public boolean Insert(DPoint p) {
		boolean[] ok = new boolean[1];
		ok[0] = true;
		ROOT = insert(p, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width, wholeCanvas.height, maxDecomp, ok);
		if (!ok[0])
			Delete(p);
		return ok[0];
	}

	public void Delete(DPoint p) {
		if (ROOT == null)
			return;

		PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
		ROOT = delete(prin.Query(new QueryObject(p)), ROOT, wholeCanvas.x + wholeCanvas.width / 2,
				wholeCanvas.y + wholeCanvas.height / 2, wholeCanvas.width, wholeCanvas.height);
	}

	public void DeleteDirect(Drawable d) {
		if (ROOT == null)
			return;
		ROOT = delete((DPoint) d, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width, wholeCanvas.height);
	}

	public SearchVector Search(QueryObject q, int mode) {
		SearchVector res = new SearchVector();
		searchVector = new Vector();
		search(ROOT, q, wholeCanvas, mode, res);
		return res;
	}

	public Drawable NearestFirst(QueryObject p) {
		if (ROOT == null)
			return null;
		PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
		return prin.Query(p);
	}

	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
			prin.Query(p, v);
		}
		return v;
	}

	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
			prin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	public Drawable[] NearestRange(QueryObject p, double dist) {
		PRbucketIncNearest near = new PRbucketIncNearest(ROOT);
		return near.Query(p, dist);
	}

	public void drawContents(DrawingTarget g, Rectangle view) { // view ignored
		drawC(ROOT, g, wholeCanvas.x, wholeCanvas.y, wholeCanvas.width, wholeCanvas.height);
	}

	/* ---- interface implementation ------ */
	public int getBucket() {
		return maxBucketSize;
	}

	public void setBucket(int b) {
		maxBucketSize = b;
		reb.rebuild();
	}

	public int getMaxDecomp() {
		return maxDecomp;
	}

	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

	/* -------------- private methods -------------------- */

	int PRCompare(DPoint P, double X, double Y) {
		if (P.x < X)
			return P.y < Y ? SW : NW;
		else
			return P.y < Y ? SE : NE;
	}

	Vector searchVector;

	private void search(PRbucketNode R, QueryObject qu, DRectangle block, int mode, SearchVector v) {
		v.addElement(new SVElement(new YellowBlock(block, R == null || R.NODETYPE == BLACK), searchVector));

		if (R == null)
			return;

		if (R.NODETYPE == BLACK) {
			v.addElement(new SVElement(new GreenPoints(R.points), searchVector));
			for (int i = 0; i < R.points.size(); i++) {
				DPoint p = (DPoint) R.points.elementAt(i);
				drawableInOut(qu, p, mode, v, searchVector);
			}
			return;
		}

		DRectangle ne = new DRectangle(block.x + block.width / 2, block.y + block.height / 2, block.width / 2,
				block.height / 2);
		DRectangle se = new DRectangle(block.x + block.width / 2, block.y, block.width / 2, block.height / 2);
		DRectangle nw = new DRectangle(block.x, block.y + block.height / 2, block.width / 2, block.height / 2);
		DRectangle sw = new DRectangle(block.x, block.y, block.width / 2, block.height / 2);

		if (qu.intersects(ne))
			searchVector.addElement(ne);
		if (qu.intersects(se))
			searchVector.addElement(se);
		if (qu.intersects(nw))
			searchVector.addElement(nw);

		if (qu.intersects(sw))
			search(R.SON[SW], qu, sw, mode, v);
		if (qu.intersects(nw)) {
			searchVector.removeElementAt(searchVector.size() - 1);
			search(R.SON[NW], qu, nw, mode, v);
		}
		if (qu.intersects(se)) {
			searchVector.removeElementAt(searchVector.size() - 1);
			search(R.SON[SE], qu, se, mode, v);
		}
		if (qu.intersects(ne)) {
			searchVector.removeElementAt(searchVector.size() - 1);
			search(R.SON[NE], qu, ne, mode, v);
		}
	}

	abstract PRbucketNode insert(DPoint p, PRbucketNode R, double X, double Y, double lx, double ly, int md,
			boolean[] ok);

	private PRbucketNode delete(DPoint P, PRbucketNode R, double X, double Y, double lx, double ly) {
		if (R == null)
			return R;
		if (R.NODETYPE == BLACK) {
			R.deletePoint(P);
			if (R.points.size() == 0)
				return null;
		} else {
			int Q = PRCompare(P, X, Y);
			R.SON[Q] = delete(P, R.SON[Q], X + XF[Q] * lx, Y + YF[Q] * ly, lx / 2, ly / 2);
			int sum = 0;
			for (int i = 0; i < 4; i++) {
				if (R.SON[i] != null && R.SON[i].NODETYPE == GRAY)
					return R;
				if (R.SON[i] != null)
					sum += R.SON[i].points.size();
			}
			if (sum <= maxBucketSize) {
				PRbucketNode newR = new PRbucketNode(BLACK);
				for (int i = 0; i < 4; i++) {
					if (R.SON[i] != null)
						for (int j = 0; j < R.SON[i].points.size(); j++)
							newR.addPoint((DPoint) R.SON[i].points.elementAt(j));
				}
				return newR;
			}
		}

		return R;
	}

	private void drawC(PRbucketNode r, DrawingTarget g, double minx, double miny, double wx, double wy) {
		if (!g.visible(new DRectangle(minx, miny, wx, wy)))
			return;

		g.setColor(Color.black);
		g.drawRect(minx, miny, wx, wy);

		if (r == null)
			return;

		g.setColor(Color.red);
		if (r.NODETYPE == BLACK)
			for (int i = 0; i < r.points.size(); i++) {
				DPoint p = (DPoint) r.points.elementAt(i);
				p.draw(g);
			}
		else {
			if (r.NODETYPE == GRAY) {
				drawC(r.SON[0], g, minx, miny + wy / 2, wx / 2, wy / 2);
				drawC(r.SON[1], g, minx + wx / 2, miny + wy / 2, wx / 2, wy / 2);
				drawC(r.SON[2], g, minx, miny, wx / 2, wy / 2);
				drawC(r.SON[3], g, minx + wx / 2, miny, wx / 2, wy / 2);
			}
		}
	}

	// ----------------------- Incremental Nearest -----------------

	abstract class PRbucketQueueElement {
		double key;

		PRbucketQueueElement(double k) {
			key = k;
		}
	}

	class PRQINode extends PRbucketQueueElement {
		PRbucketNode r;
		DRectangle block;

		PRQINode(double k, PRbucketNode rr, DRectangle b) {
			super(k);
			r = rr;
			block = b;
		}
	}

	class PRQLeaf extends PRbucketQueueElement {
		DPoint pnt;

		PRQLeaf(double k, DPoint p) {
			super(k);
			pnt = p;
		}
	}

	class PRbucketQueue {
		Vector v;

		PRbucketQueue() {
			v = new Vector();
		}

		void Enqueue(PRbucketQueueElement qe) {
			v.addElement(qe);
			for (int i = v.size() - 1; i > 0; i--) {
				PRbucketQueueElement q1 = (PRbucketQueueElement) v.elementAt(i - 1);
				PRbucketQueueElement q2 = (PRbucketQueueElement) v.elementAt(i);
				if (q1.key > q2.key) {
					v.setElementAt(q2, i - 1);
					v.setElementAt(q1, i);
				}
			}
		}

		PRbucketQueueElement Dequeue() {
			PRbucketQueueElement q = (PRbucketQueueElement) v.elementAt(0);
			v.removeElementAt(0);
			return q;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		Vector makeVector() {
			Vector r = new Vector();
			for (int i = 0; i < v.size(); i++) {
				PRbucketQueueElement q = (PRbucketQueueElement) v.elementAt(i);
				if (q instanceof PRQLeaf)
					r.addElement(new GreenPoints(((PRQLeaf) q).pnt));
				else
					r.addElement(new QueueBlock(((PRQINode) q).block));
			}
			return r;
		}

	}

	class PRbucketIncNearest {

		PRbucketQueue q;

		PRbucketIncNearest(PRbucketNode rt) {
			q = new PRbucketQueue();
			q.Enqueue(new PRQINode(0, rt, wholeCanvas));
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

		DPoint[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
			int counter = 1;
			Vector pts = new Vector();

			while (!q.isEmpty()) {
				PRbucketQueueElement element = q.Dequeue();

				if (element instanceof PRQLeaf) {
					PRQLeaf l = (PRQLeaf) element;
					if (nrelems-- <= 0 || qu.distance(l.pnt) > dist)
						break;
					pts.addElement(l.pnt);
					ret.addElement(new NNElement(new NNDrawable(l.pnt, counter++), l.key, q.makeVector()));
				} else {
					PRQINode in = (PRQINode) element;
					ret.addElement(new NNElement(new YellowBlock(in.block, false), in.key, q.makeVector()));
					if (in.r == null) {
						// noop
					} else if (in.r.NODETYPE == BLACK) {
						for (int i = 0; i < in.r.points.size(); i++) {
							DPoint pt = (DPoint) in.r.points.elementAt(i);
							if (qu.distance(pt) >= qu.distance(in.block))
								q.Enqueue(new PRQLeaf(qu.distance(pt), pt));
						}
					} else if (in.r.NODETYPE == GRAY) {
						for (int i = 0; i < 4; i++) {
							// if (in.r.SON[i] != null) {
							DRectangle dr = new DRectangle(in.block.x + xf[i] * in.block.width,
									in.block.y + yf[i] * in.block.height, in.block.width / 2, in.block.height / 2);
							q.Enqueue(new PRQINode(qu.distance(dr), in.r.SON[i], dr));
						}
					}
				}
			}
			DPoint[] ar = new DPoint[pts.size()];
			pts.copyInto(ar);
			return ar;
		}
	}
}
