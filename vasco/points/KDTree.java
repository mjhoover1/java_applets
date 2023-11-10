package vasco.points;

/* $Id: KDTree.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import java.awt.*;
import java.util.*;

public class KDTree extends PointStructure {

	// ----------- public methods -----------

	public KDTree(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
	}

	public void reInit(Choice ao) {
		super.reInit(ao);
		ao.addItem("Nearest");
		ao.addItem("Within");
	}

	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	public String getName() {
		return "k-d Tree";
	}

	public boolean orderDependent() {
		return true;
	}

	public boolean Insert(DPoint qu) {
		ROOT = insert(new KDNode(qu), ROOT);
		return true; // always ok
	}

	public void Delete(DPoint qu) {
		if (ROOT == null)
			return;
		KDIncNearest ptin = new KDIncNearest(ROOT);
		DPoint ptn = ptin.Query(new QueryObject(qu));
		ROOT = delete(ptn, ROOT);
	}

	public void DeleteDirect(Drawable qu) {
		if (ROOT == null)
			return;
		ROOT = delete((DPoint) qu, ROOT);
	}

	public SearchVector Search(QueryObject query, int mode) {
		SearchVector res = new SearchVector();
		searchVector = new Vector();
		search(ROOT, query, mode, wholeCanvas, res);
		return res;
	}

	public Drawable[] NearestRange(QueryObject p, double dist) {
		KDIncNearest near = new KDIncNearest(ROOT);
		return near.Query(p, dist);
	}

	public Drawable NearestFirst(QueryObject p) {
		if (ROOT != null) {
			KDIncNearest ptin = new KDIncNearest(ROOT);
			return ptin.Query(p);
		} else
			return null;
	}

	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			KDIncNearest ptin = new KDIncNearest(ROOT);
			ptin.Query(p, v);
		}
		return v;
	}

	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			KDIncNearest ptin = new KDIncNearest(ROOT);
			ptin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	public void drawContents(DrawingTarget g, Rectangle view) {
		drawC(ROOT, g, wholeCanvas.x, wholeCanvas.y, wholeCanvas.x + wholeCanvas.width,
				wholeCanvas.y + wholeCanvas.height);
	}

	/* ----------- private procedures ------------- */

	class KDNode {
		final int NRDIRS = 2;

		char DISC;
		DPoint pnt;

		KDNode SON[] = new KDNode[NRDIRS];

		KDNode(DPoint p) {
			pnt = p;
			SON[0] = SON[1] = null;
		}

		int SonType(KDNode P) {
			if (SON[0] == P)
				return 0;
			else
				return 1;
		}
	}

	// 2-d version of K-D operations
	KDNode ROOT;

	private int KDCompare(KDNode P, KDNode Q) {
		// 0 = left, 1 = right;

		if (Q.DISC == 'X')
			return (P.pnt.x < Q.pnt.x) ? 0 : 1;
		else
			return (P.pnt.y < Q.pnt.y) ? 0 : 1;
	}

	private char NextDisc(KDNode P) {
		return (P.DISC == 'X') ? 'Y' : 'X';
	}

	private KDNode insert(KDNode P, KDNode R) {
		int Q = -1;
		KDNode F = null, T;

		if (R == null) {
			R = P;
			P.DISC = 'X';
		} else {
			T = R;
			while (T != null && !(P.pnt.equals(T.pnt))) {
				F = T;
				Q = KDCompare(P, T);
				T = T.SON[Q];
			}
			if (T == null) {
				F.SON[Q] = P;
				P.DISC = NextDisc(F);
			}
		}
		return R;
	}

	Vector searchVector;

	private void search(KDNode R, QueryObject searchRect, int mode, DRectangle block, SearchVector res) {
		res.addElement(new SVElement(new YellowBlock(block, R == null || (R.SON[0] == null && R.SON[1] == null)),
				searchVector));

		if (R == null)
			return;

		res.addElement(new SVElement(new GreenPoints(R.pnt), searchVector));
		drawableInOut(searchRect, R.pnt, mode, res, searchVector);

		if (R.SON[0] == null && R.SON[1] == null)
			return;

		if (R.DISC == 'X') {
			DRectangle west = new DRectangle(block.x, block.y, R.pnt.x - block.x, block.height);
			DRectangle east = new DRectangle(R.pnt.x, block.y, block.x + block.width - R.pnt.x, block.height);
			if (!searchRect.intersects(west))
				search(R.SON[1], searchRect, mode, east, res);
			else if (!searchRect.intersects(east))
				search(R.SON[0], searchRect, mode, west, res);
			else {
				searchVector.addElement(east);
				search(R.SON[0], searchRect, mode, west, res);
				searchVector.removeElementAt(searchVector.size() - 1);
				search(R.SON[1], searchRect, mode, east, res);
			}
		} else {
			DRectangle north = new DRectangle(block.x, R.pnt.y, block.width, block.y + block.height - R.pnt.y);
			DRectangle south = new DRectangle(block.x, block.y, block.width, R.pnt.y - block.y);

			if (!searchRect.intersects(south))
				search(R.SON[1], searchRect, mode, north, res);
			else if (!searchRect.intersects(north))
				search(R.SON[0], searchRect, mode, south, res);
			else {
				searchVector.addElement(north);
				search(R.SON[0], searchRect, mode, south, res);
				searchVector.removeElementAt(searchVector.size() - 1);
				search(R.SON[1], searchRect, mode, north, res);
			}
		}
	}

	private KDNode delete(DPoint p, KDNode R) {
		KDNode N, F, P;

		P = R;
		while (P != null && !(P.pnt.equals(p))) {
			if (P.DISC == 'X')
				P = (P.pnt.x <= p.x) ? P.SON[1] : P.SON[0];
			else
				P = (P.pnt.y <= p.y) ? P.SON[1] : P.SON[0];
		}

		N = KDDelete1(P, R);
		F = FindFather(P, R, null);
		if (F == null)
			R = N;
		else
			F.SON[F.SonType(P)] = N;
		return R;
	}

	private KDNode KDDelete1(KDNode P, KDNode ROOT) {
		KDNode F, R;
		char D;

		if (P.SON[0] == null && P.SON[1] == null) {
			return null;
		} else {
			D = P.DISC;
		}
		if (P.SON[1] == null) {
			P.SON[1] = P.SON[0];
			P.SON[0] = null;
		}
		R = FindDMinimum(P.SON[1], D);

		F = FindFather(R, P.SON[1], P);
		F.SON[F.SonType(R)] = KDDelete1(R, ROOT);
		R.SON[0] = P.SON[0];
		R.SON[1] = P.SON[1];
		R.DISC = P.DISC;
		return R;
	}

	private KDNode MinNode(KDNode A, KDNode B, char D) {
		if (A == null)
			return B;
		if (B == null)
			return A;

		if (D == 'X')
			return (A.pnt.x < B.pnt.x) ? A : B;
		else
			return (A.pnt.y < B.pnt.y) ? A : B;
	}

	private KDNode FindDMinimum(KDNode P, char D) {
		KDNode nd, nd2;

		if (P == null)
			return null;

		if (P.DISC == D) {
			nd = FindDMinimum(P.SON[0], D);
			return MinNode(P, nd, D);
		} else {
			nd = FindDMinimum(P.SON[0], D);
			nd2 = FindDMinimum(P.SON[1], D);
			return MinNode(P, MinNode(nd, nd2, D), D);
		}
	}

	private KDNode FindFather(KDNode P, KDNode R, KDNode F) {
		if (P == R)
			return F;
		else
			return (FindFather(P, R.SON[KDCompare(P, R)], R));
	}

	private void drawC(KDNode r, DrawingTarget g, double minx, double miny, double maxx, double maxy) {
		if (!g.visible(new DRectangle(minx, miny, maxx - minx, maxy - miny)))
			return;

		if (r == null) {
			return;
		}

		if (r.SON[0] != null || r.SON[1] != null) {
			g.setColor(Color.black);
			if (r.DISC == 'X')
				g.drawLine(r.pnt.x, miny, r.pnt.x, maxy);
			else
				g.drawLine(minx, r.pnt.y, maxx, r.pnt.y);
		}
		g.setColor(Color.red);
		r.pnt.draw(g);

		if (r.DISC == 'X') {
			drawC(r.SON[0], g, minx, miny, r.pnt.x, maxy);
			drawC(r.SON[1], g, r.pnt.x, miny, maxx, maxy);
		} else {
			drawC(r.SON[0], g, minx, miny, maxx, r.pnt.y);
			drawC(r.SON[1], g, minx, r.pnt.y, maxx, maxy);
		}
	}

	// ----------------------- Incremental Nearest -----------------

	class KDQueueElement {
		double key;
		KDNode r;
		DRectangle box;
		DPoint pnt;
		boolean isElem;

		KDQueueElement(double k, DPoint p) {
			key = k;
			pnt = p;
			isElem = true;
		}

		KDQueueElement(double k, KDNode p, DRectangle b) {
			key = k;
			r = p;
			box = b;
			isElem = false;
		}
	}

	class KDQueue {
		Vector v;

		KDQueue() {
			v = new Vector();
		}

		void Enqueue(KDQueueElement qe) {
			v.addElement(qe);
			for (int i = v.size() - 1; i > 0; i--) {
				KDQueueElement q1 = (KDQueueElement) v.elementAt(i - 1);
				KDQueueElement q2 = (KDQueueElement) v.elementAt(i);
				if (q1.key > q2.key) {
					v.setElementAt(q2, i - 1);
					v.setElementAt(q1, i);
				}
			}
		}

		KDQueueElement Dequeue() {
			KDQueueElement q = (KDQueueElement) v.elementAt(0);
			v.removeElementAt(0);
			return q;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		Vector makeVector() {
			Vector r = new Vector();
			for (int i = 0; i < v.size(); i++) {
				KDQueueElement q = (KDQueueElement) v.elementAt(i);
				if (q.isElem)
					r.addElement(new GreenPoints(q.pnt));
				else
					r.addElement(new QueueBlock(q.box));
			}
			return r;
		}

	}

	class KDIncNearest {
		KDQueue q;

		KDIncNearest(KDNode rt) {
			q = new KDQueue();
			q.Enqueue(new KDQueueElement(0, rt, wholeCanvas));
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
			Vector pts = new Vector();
			DRectangle b;
			int counter = 1;

			while (!q.isEmpty()) {
				KDQueueElement e = q.Dequeue();
				if (e.isElem) {
					if (nrelems-- <= 0 || qu.distance(e.pnt) > dist)
						break;
					pts.addElement(e.pnt);
					ret.addElement(new NNElement(new NNDrawable(e.pnt, counter++), e.key, q.makeVector()));
				} else {
					ret.addElement(new NNElement(new YellowBlock(e.box, false), e.key, q.makeVector()));
					q.Enqueue(new KDQueueElement(qu.distance(e.r.pnt), e.r.pnt));

					if (e.r.SON[0] != null) {
						if (e.r.DISC == 'X')
							b = new DRectangle(e.box.x, e.box.y, e.r.pnt.x - e.box.x, e.box.height);
						else
							b = new DRectangle(e.box.x, e.box.y, e.box.width, e.r.pnt.y - e.box.y);
						q.Enqueue(new KDQueueElement(qu.distance(b), e.r.SON[0], b));
					}
					if (e.r.SON[1] != null) {
						if (e.r.DISC == 'X')
							b = new DRectangle(e.r.pnt.x, e.box.y, e.box.x + e.box.width - e.r.pnt.x, e.box.height);
						else
							b = new DRectangle(e.box.x, e.r.pnt.y, e.box.width, e.box.y + e.box.height - e.r.pnt.y);
						q.Enqueue(new KDQueueElement(qu.distance(b), e.r.SON[1], b));
					}

				}
			}
			DPoint[] ar = new DPoint[pts.size()];
			pts.copyInto(ar);
			return ar;
		}
	}
}
