package vasco.points;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: PriorityTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.NNElement;
import vasco.common.QSortAlgorithm;
import vasco.common.QueryObject;
import vasco.common.QueueBlock;
import vasco.common.RebuildTree;
import vasco.common.SVElement;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.common.YellowBlock;
import vasco.drawable.Drawable;
import vasco.drawable.NNDrawable;

public class PriorityTree extends PointStructure {

	class Node2d {
		DPoint heapMax;
		DPoint point;
		double midrange;
		Node2d LEFT, RIGHT;

		Node2d() {
			point = null;
			LEFT = RIGHT = null;
		}
	}

	Node2d ROOT;
	Vector pts;

	public PriorityTree(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		addItemIfNotExists(ao, "Nearest");
		addItemIfNotExists(ao, "Within");
	}

	@Override
	public void Clear() {
		super.Clear();
		ROOT = null;
		pts = new Vector();
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

	@Override
	public void MessageEnd() {
		ROOT = createTrees(pts);
		super.MessageEnd();
	}

	@Override
	public boolean Insert(DPoint p) {
		pts.addElement(p);
		return true;
	}

	@Override
	public void Delete(DPoint qu) {
		if (pts.size() == 0)
			return;

		DPoint min = (DPoint) pts.elementAt(0);
		double dist = min.distance(qu);

		for (int j = 1; j < pts.size(); j++) {
			DPoint p = (DPoint) pts.elementAt(j);
			double d = p.distance(qu);
			if (d < dist) {
				dist = d;
				min = p;
			}
		}
		pts.removeElement(min);
	}

	@Override
	public void DeleteDirect(Drawable d) {
		for (int i = 0; i < pts.size(); i++)
			if (((DPoint) pts.elementAt(i)).equals((DPoint) d)) {
				pts.removeElementAt(i);
				return;
			}
	}

	@Override
	public Drawable NearestFirst(QueryObject p) {
		if (ROOT == null)
			return null;
		PriorityIncNearest mxin = new PriorityIncNearest(ROOT);
		return mxin.Query(p);
	}

	@Override
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PriorityIncNearest mxin = new PriorityIncNearest(ROOT);
			mxin.Query(p, v);
		}
		return v;
	}

	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PriorityIncNearest mxin = new PriorityIncNearest(ROOT);
			mxin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		if (ROOT == null)
			return null;
		PriorityIncNearest mxin = new PriorityIncNearest(ROOT);
		return mxin.Query(p, dist);
	}

	@Override
	public String getName() {
		return "Priority Tree";
	}

	@Override
	public void drawContents(DrawingTarget g, Rectangle view) {
		if (ROOT == null)
			return;
		drawDecompLines(g, ROOT, wholeCanvas.x, wholeCanvas.x + wholeCanvas.width);
		return;
	}

	// -------------------------------------------------
	void drawDecompLines(DrawingTarget g, Node2d r, double minx, double maxx) {
		if (!g.visible(new DRectangle(minx, wholeCanvas.y, maxx, wholeCanvas.y + wholeCanvas.height))
				|| (r.heapMax == null))
			return;
		g.setColor(Color.red);
		r.heapMax.draw(g);

		g.setColor(Color.black);
		g.drawLine(minx, r.heapMax.y, maxx, r.heapMax.y); // horizontal
		if (r.point != null) {
			return;
		}
		g.drawLine(r.midrange, 0, r.midrange, r.heapMax.y); // vertical

		drawDecompLines(g, r.LEFT, minx, r.midrange);
		drawDecompLines(g, r.RIGHT, r.midrange, maxx);
	}

	void Gather(Node2d r, Vector arr) {
		if (r.point != null) {
			arr.addElement(r.point);
			return;
		}
		Gather(r.LEFT, arr);
		Gather(r.RIGHT, arr);
	}

	DPoint GetPoint(Node2d r, Vector assigned) {
		Vector subtree = new Vector();

		Gather(r, subtree);
		/*
		 * for (int i = 0; i < subtree.size() - 1; i++) for (int j = 0; j <
		 * subtree.size() - 1; j++) if (compareToY((DPoint)subtree.elementAt(j),
		 * (DPoint)subtree.elementAt(j+1)) < 0) { DPoint p; p =
		 * (DPoint)subtree.elementAt(j+1); subtree.setElementAt(subtree.elementAt(j),
		 * j+1); subtree.setElementAt(p, j); }
		 */

		YComparable[] py = new YComparable[subtree.size()];
		for (int i = 0; i < subtree.size(); i++)
			py[i] = new YComparable((DPoint) subtree.elementAt(i));
		QSortAlgorithm.sort(py);
		for (int i = 0; i < subtree.size(); i++)
			subtree.setElementAt(py[i].p, subtree.size() - i - 1);

		for (int i = 0; i < subtree.size(); i++) {
			if (!assigned.contains(subtree.elementAt(i))) {
				assigned.addElement(subtree.elementAt(i));
				return (DPoint) subtree.elementAt(i);
			}
		}
		return null;
	}

	void SetPoints(Node2d r, Vector assigned) {
		r.heapMax = GetPoint(r, assigned);
		if (r.point != null)
			return;
		SetPoints(r.LEFT, assigned);
		SetPoints(r.RIGHT, assigned);
	}

	Node2d createTrees(Vector arr) {
		if (arr.size() == 0)
			return null; // empty

		XComparable[] px = new XComparable[arr.size()];
		for (int i = 0; i < arr.size(); i++)
			px[i] = new XComparable((DPoint) arr.elementAt(i));
		QSortAlgorithm.sort(px);
		for (int i = 0; i < arr.size(); i++)
			arr.setElementAt(px[i].p, i);

		/*
		 * // bubble sort to be substituted when sort becomes part of jdk for (int i =
		 * 0; i < arr.size() - 1; i++) for (int j = 0; j < arr.size() - 1; j++) if
		 * (compareToX((DPoint)arr.elementAt(j), (DPoint)arr.elementAt(j+1)) > 0) {
		 * DPoint p; p = (DPoint)arr.elementAt(j+1); arr.setElementAt(arr.elementAt(j),
		 * j+1); arr.setElementAt(p, j); }
		 */

		Node2d oldar[];
		Node2d newar[];
		double oldminar[];
		double oldmaxar[];
		double newminar[];
		double newmaxar[];
		oldminar = new double[arr.size()];
		oldmaxar = new double[arr.size()];

		oldar = new Node2d[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			oldar[i] = new Node2d();
			oldar[i].point = (DPoint) arr.elementAt(i);
			oldar[i].midrange = oldminar[i] = oldmaxar[i] = oldar[i].point.x;
		}

		while (oldar.length != 1) {
			newar = new Node2d[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
			newminar = new double[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];
			newmaxar = new double[oldar.length / 2 + (oldar.length % 2 == 0 ? 0 : 1)];

			if (oldar.length % 2 != 0) {
				newar[newar.length - 1] = oldar[oldar.length - 1];
				newminar[newminar.length - 1] = oldminar[oldminar.length - 1];
				newmaxar[newmaxar.length - 1] = oldmaxar[oldmaxar.length - 1];
			}
			for (int i = 0; i < oldar.length / 2; i++) {
				newar[i] = new Node2d();
				newar[i].LEFT = oldar[2 * i];
				newar[i].RIGHT = oldar[2 * i + 1];
				newar[i].midrange = (oldmaxar[2 * i] + oldminar[2 * i + 1]) / 2;
				newminar[i] = oldminar[2 * i];
				newmaxar[i] = oldmaxar[2 * i + 1];
			}
			oldar = newar;
			oldmaxar = newmaxar;
			oldminar = newminar;
		}
		// ------------------ 2d-tree created
		SetPoints(oldar[0], new Vector());

		return oldar[0];
	}

	@Override
	public SearchVector Search(QueryObject searchable, int mode) {
		DRectangle searchRect = searchable.getBB();
		double BX = searchRect.x;
		double EX = searchRect.x + searchRect.width;
		double BY = searchRect.y;
		Node2d T = ROOT;
		Vector searchVector = new Vector();
		double minx = wholeCanvas.x;
		double maxx = wholeCanvas.x + wholeCanvas.width;
		SearchVector v = new SearchVector();
		double lastHeapMax = wholeCanvas.y + wholeCanvas.height;

		Node2d Q;

		if (T == null)
			return v;
		while (true) {
			if (T.heapMax != null) {
				searchVector.addElement(new DRectangle(minx, 0, maxx - minx, T.heapMax.y));
				DRectangle rec = new DRectangle(minx, T.heapMax.y, maxx - minx, lastHeapMax - T.heapMax.y);
				v.addElement(new SVElement(new YellowBlock(rec, true), searchVector));
				// pointInOut(T.heapMax, searchRect, v, searchVector);
				searchVector.removeElementAt(searchVector.size() - 1);
				lastHeapMax = T.heapMax.y;
			}

			/* Find nearest common ancestor */
			drawableInOut(searchable, T.heapMax, mode, v, searchVector);
			if (OUT_OF_RANGE_Y(T.heapMax, BY))
				return v;

			if (T.LEFT == null)
				return v; // leaf
			else if (EX < T.midrange) {
				maxx = T.midrange;
				T = T.LEFT;
			} else if (T.midrange < BX) {
				minx = T.midrange;
				T = T.RIGHT;
			} else
				break; /* Found nearest common ancestor */
		}

		searchVector.addElement(new DRectangle(T.midrange, 0, maxx - T.midrange, T.heapMax.y));

		/* Nonleaf node and must process subtrees */
		double oldmaxx = maxx;
		Q = T; /* Save value to process other subtree */
		T = T.LEFT;
		maxx = Q.midrange;
		while (true) {
			/* Process the left subtree */
			if (T.heapMax != null) {
				searchVector.addElement(new DRectangle(minx, 0, maxx - minx, T.heapMax.y));
				DRectangle rec = new DRectangle(minx, T.heapMax.y, maxx - minx, lastHeapMax - T.heapMax.y);
				v.addElement(new SVElement(new YellowBlock(rec, true), searchVector));
				// pointInOut(T.heapMax, searchRect, v, searchVector);
				searchVector.removeElementAt(searchVector.size() - 1);
				lastHeapMax = T.heapMax.y;
			}

			drawableInOut(searchable, T.heapMax, mode, v, searchVector);
			if (OUT_OF_RANGE_Y(T.heapMax, BY))
				break;

			if (T.LEFT == null)
				break; // leaf
			else if (BX <= T.midrange) {
				searchVector.addElement(new DRectangle(minx, 0, T.midrange - minx, T.heapMax.y));
				Y_SEARCH(T.RIGHT, searchable, T.midrange, maxx, T.heapMax.y, mode, v, searchVector);
				searchVector.removeElementAt(searchVector.size() - 1);
				maxx = T.midrange;
				T = T.LEFT;
			} else {
				minx = T.midrange;
				T = T.RIGHT;
			}
		}

		searchVector.removeElementAt(searchVector.size() - 1);
		T = Q.RIGHT;
		if (Q.heapMax != null)
			lastHeapMax = Q.heapMax.y;

		minx = Q.midrange;
		maxx = oldmaxx;
		while (true) {
			if (T.heapMax != null) {
				searchVector.addElement(new DRectangle(minx, 0, maxx - minx, T.heapMax.y));
				DRectangle rec = new DRectangle(minx, T.heapMax.y, maxx - minx, lastHeapMax - T.heapMax.y);
				v.addElement(new SVElement(new YellowBlock(rec, true), searchVector));
				// pointInOut(T.heapMax, searchRect, v, searchVector);
				searchVector.removeElementAt(searchVector.size() - 1);
				lastHeapMax = T.heapMax.y;
			}
			/* Process the right subtree */
			drawableInOut(searchable, T.heapMax, mode, v, searchVector);
			if (OUT_OF_RANGE_Y(T.heapMax, BY))
				return v;

			if (T.LEFT == null)
				return v; // leaf
			else if (T.midrange <= EX) {
				searchVector.addElement(new DRectangle(T.midrange, 0, maxx - T.midrange, T.heapMax.y));
				Y_SEARCH(T.LEFT, searchable, minx, T.midrange, T.heapMax.y, mode, v, searchVector);
				searchVector.removeElementAt(searchVector.size() - 1);
				minx = T.midrange;
				T = T.RIGHT;
			} else {
				maxx = T.midrange;
				T = T.LEFT;
			}
		}
	}

	void Y_SEARCH(Node2d T, QueryObject searchRect, double minx, double maxx, double height, int mode, SearchVector v,
			Vector searchVector) {
		/*
		 * Perform a one-dimensional range search for the semi-infinite interval |[BY :
		 * infinity]| in the priority search tree rooted at |T|.
		 */
		double BY = searchRect.getBB().y;

		if ((T == null) || (T.heapMax == null))
			return;

		searchVector.addElement(new DRectangle(minx, 0, maxx - minx, T.heapMax.y));
		DRectangle rec = new DRectangle(minx, T.heapMax.y, maxx - minx, height - T.heapMax.y);
		v.addElement(new SVElement(new YellowBlock(rec, true), searchVector));
		// pointInOut(T.heapMax, searchRect, v, searchVector);

		searchVector.removeElementAt(searchVector.size() - 1);

		if (T.heapMax == null)
			return;
		/* All relevant points have already been output */
		else if (T.heapMax.y < BY)
			return;
		/* y coordinate values of all points in the subtrees are too small */
		else {
			drawableInOut(searchRect, T.heapMax, mode, v, searchVector);
			searchVector.addElement(new DRectangle(T.midrange, 0, maxx - T.midrange, T.heapMax.y));
			Y_SEARCH(T.LEFT, searchRect, minx, T.midrange, T.heapMax.y, mode, v, searchVector);
			searchVector.removeElementAt(searchVector.size() - 1);
			Y_SEARCH(T.RIGHT, searchRect, T.midrange, maxx, T.heapMax.y, mode, v, searchVector);
		}
	}

	boolean OUT_OF_RANGE_Y(DPoint P, double BY) {
		/*
		 * Check if all relevant points have already been output or if the $y$
		 * coordinate values of all points in the subtrees are too small.
		 */
		return (P == null) ? true : P.y < BY;
	}

	// ----------------------- Incremental Nearest -----------------

	class PriorityQueueElement {
		double key;
		Node2d r;
		DPoint p;
		DRectangle box;
		boolean isElem;

		PriorityQueueElement(double k, DPoint pp) {
			key = k;
			p = pp;
			isElem = true;
		}

		PriorityQueueElement(double k, Node2d nd, DRectangle b) {
			key = k;
			r = nd;
			box = b;
			isElem = false;
		}
	}

	class PriorityQueue {
		Vector v;

		PriorityQueue() {
			v = new Vector();
		}

		void Enqueue(PriorityQueueElement qe) {
			v.addElement(qe);
			for (int i = v.size() - 1; i > 0; i--) {
				PriorityQueueElement q1 = (PriorityQueueElement) v.elementAt(i - 1);
				PriorityQueueElement q2 = (PriorityQueueElement) v.elementAt(i);
				if (q1.key > q2.key) {
					v.setElementAt(q2, i - 1);
					v.setElementAt(q1, i);
				}
			}
		}

		PriorityQueueElement Dequeue() {
			PriorityQueueElement q = (PriorityQueueElement) v.elementAt(0);
			v.removeElementAt(0);
			return q;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		Vector makeVector() {
			Vector r = new Vector();
			for (int i = 0; i < v.size(); i++) {
				PriorityQueueElement q = (PriorityQueueElement) v.elementAt(i);
				if (q.isElem)
					r.addElement(new GreenPoints(q.p));
				else
					r.addElement(new QueueBlock(q.box));
			}
			return r;
		}

	}

	class PriorityIncNearest {
		PriorityQueue q;

		PriorityIncNearest(Node2d rt) {
			q = new PriorityQueue();
			q.Enqueue(new PriorityQueueElement(0, rt, wholeCanvas));
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
				PriorityQueueElement e = q.Dequeue();
				if (e.isElem) {
					if (nrelems-- <= 0 || qu.distance(e.p) > dist)
						break;
					pts.addElement(e.p);
					ret.addElement(new NNElement(new NNDrawable(e.p, counter++), e.key, q.makeVector()));
				} else {
					ret.addElement(new NNElement(new YellowBlock(e.box, false), e.key, q.makeVector()));
					if (e.r.heapMax != null) {
						q.Enqueue(new PriorityQueueElement(qu.distance(e.r.heapMax), e.r.heapMax));
						if (e.r.LEFT != null) {
							b = new DRectangle(e.box.x, e.box.y, e.r.midrange - e.box.x, e.r.heapMax.y);
							q.Enqueue(new PriorityQueueElement(qu.distance(b), e.r.LEFT, b));
						}

						if (e.r.RIGHT != null) {
							b = new DRectangle(e.r.midrange, e.box.y, e.box.x + e.box.width - e.r.midrange,
									e.r.heapMax.y);
							q.Enqueue(new PriorityQueueElement(qu.distance(b), e.r.RIGHT, b));

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
