package vasco.lines;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: GenericLine.java,v 1.2 2007/10/28 15:38:15 jagan Exp $ */
import vasco.common.DLine;
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

public abstract class GenericLine extends LineStructure implements MaxDecompIface {
	// allows duplicate lines

	int maxDecomp;

	QNode ROOT;

	public GenericLine(DRectangle can, int md, TopInterface p, RebuildTree r) {
		super(can, p, r);
		maxDecomp = md;
	}

	@Override
	public void reInit(JComboBox<String> ops) {
		super.reInit(ops);
		new MaxDecomp(topInterface, 9, this);
	}

	/* ---------------- interface implementation ---------- */

	@Override
	public int getMaxDecomp() {
		return maxDecomp;
	}

	@Override
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}
	// -------------------------------------------------------

	@Override
	public void Clear() {
		super.Clear();
		QSquare s = new QSquare();
		QNode newroot = new QNode();
		DPoint pt = new DPoint(wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2);

		s.CENTER = pt;
		s.LEN = wholeCanvas.width;
		newroot.SQUARE = s;
		newroot.NODETYPE = BLACK;
		newroot.SON[0] = newroot.SON[1] = newroot.SON[2] = newroot.SON[3] = null;
		newroot.DICTIONARY = null;
		newroot.level = 0;

		ROOT = newroot;
	}

	@Override
	public boolean Insert(DLine l) {
		DLine nw = remakeDLine(l);
		QEdgeList nl = new QEdgeList(nw.p1, nw.p2);
		boolean res = Insert(nl, ROOT, maxDecomp);
		if (!res) {
			Delete(nl, ROOT);
			deletePoint(nw.p1);
			deletePoint(nw.p2);
		}
		return res;
	}

	@Override
	public void Delete(DPoint p) {
		LineIncNearest kdin = new LineIncNearest(ROOT);
		DLine mx = kdin.Query(new QueryObject(p));
		DeleteDirect(mx);
	}

	@Override
	public void DeleteDirect(Drawable dl) {
		DLine mx = (DLine) dl;
		if (mx != null) {
			Drawable[] range = NearestRange(new QueryObject(mx.p1), 0);
			if (range != null) {
				int i;
				for (i = 0; i < range.length; i++)
					if (mx.equals((DLine) range[i]))
						break;
				if (i != range.length) {
					Delete(new QEdgeList(mx), ROOT);
					deletePoint(mx.p1);
					deletePoint(mx.p2);
				}
			}
		}
	}

	@Override
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		LineIncNearest mxin = new LineIncNearest(ROOT);
		mxin.Query(p, v);
		return v;
	}

	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		LineIncNearest mxin = new LineIncNearest(ROOT);
		mxin.Query(p, v, dist, Integer.MAX_VALUE);
		return v;
	}

	@Override
	public Drawable NearestFirst(QueryObject p) {
		LineIncNearest mxin = new LineIncNearest(ROOT);
		return mxin.Query(p);
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		LineIncNearest mxin = new LineIncNearest(ROOT);
		return mxin.Query(p, dist);
	}

	@Override
	public SearchVector Search(QueryObject r, int mode) {
		SearchVector sv = new SearchVector();
		searchVector = new Vector();
		processed = new Vector();
		findLines2(r, ROOT, sv, mode);
		return sv;
	}

	@Override
	public void drawContents(DrawingTarget gg, Rectangle view) {
		drawContents(gg, view, Color.black);
	}

	public void drawContents(DrawingTarget gg, Rectangle view, Color c) {
		drawC(ROOT, gg, view, true, c);
		drawC(ROOT, gg, view, false, c);
	}

	// --- abstract protected methods ---------

	abstract boolean Insert(QEdgeList qe, QNode q, int md);

	abstract void Delete(QEdgeList q, QNode r);

	// ------------------------------------------------------

	class QEdgeList {
		QLine DATA;
		QEdgeList NEXT;

		QEdgeList(DLine l) {
			QLine ln = new QLine();
			ln.P1 = l.p1;
			ln.P2 = l.p2;
			DATA = ln;
			NEXT = null;
		}

		QEdgeList(DPoint p1, DPoint p2) {
			QLine ln = new QLine();
			ln.P1 = p1;
			ln.P2 = p2;
			DATA = ln;
			NEXT = null;
		}

		QEdgeList(QLine l) {
			DATA = l;
			NEXT = null;
		}

		QEdgeList(QEdgeList q) {
			DATA = q.DATA;
			NEXT = null;
		}

		int length() {
			QEdgeList n = NEXT;
			int i;

			for (i = 1; n != null; n = n.NEXT, i++)
				;
			return i;
		}

		@Override
		public String toString() {
			String ret = "QEdgeList:";
			QEdgeList q = this;
			while (q != null) {
				ret += q.DATA.toString() + "; ";
				q = q.NEXT;
			}
			return ret;
		}

	}

	class QEdgeListRef {
		QEdgeList val;
	}

	class QLine {
		String NAME;
		QLine LSON, RSON;
		DPoint P1, P2;

		public boolean equals(QLine q) {
			return (q.P1.equals(P1) && q.P2.equals(P2)) || (q.P1.equals(P2) && q.P2.equals(P1));
		}

		@Override
		public String toString() {
			return "QLine [" + P1.toString() + ", " + P2.toString() + "]";
		}

	}

	class ExtendedQLine {
		boolean delete;
		QLine ql;

		ExtendedQLine(QLine q) {
			ql = q;
			delete = false;
		}

		ExtendedQLine(QLine q, boolean b) {
			ql = q;
			delete = b;
		}
	}

	class QNode {
		QSquare SQUARE;
		int NODETYPE;
		QNode SON[] = new QNode[4];
		QEdgeList DICTIONARY;
		int level;

		@Override
		public String toString() {
			return "QNode [" + (SQUARE == null ? "null" : SQUARE.toString()) + "], NodeType=" + NODETYPE
					+ "\n  Dictionary:" + (DICTIONARY == null ? "null" : DICTIONARY.toString());
		}

	}

	class QSquare {
		DPoint CENTER;
		double LEN;

		QSquare(DRectangle r) {
			CENTER = new DPoint(r.x + r.width / 2, r.y + r.height / 2);
			LEN = r.width;
		}

		QSquare() {
		}

		DRectangle toDRectangle() {
			return new DRectangle(CENTER.x - LEN / 2, CENTER.y - LEN / 2, LEN, LEN);
		}

		@Override
		public String toString() {
			return "SQUARE: " + CENTER.toString() + ", len: " + LEN;
		}

	}

	void drawC(QNode r, DrawingTarget g, Rectangle view, boolean doBackground, Color c) {
		if (r.SQUARE.LEN < wholeCanvas.width / view.width) {
			return;
		}
		DRectangle thisSquare = r.SQUARE.toDRectangle();

		if (!g.visible(thisSquare))
			return;

		if (doBackground) {
			g.setColor(c);
			g.drawRect(thisSquare.x, thisSquare.y, thisSquare.width, thisSquare.height);
		} else {
			QEdgeList l = r.DICTIONARY;

			g.setColor(Color.red);
			while (l != null) {
				g.drawLine(l.DATA.P1.x, l.DATA.P1.y, l.DATA.P2.x, l.DATA.P2.y);
				l = l.NEXT;
			}
		}
		for (int i = 0; i < 4; i++) {
			if (r.SON[i] != null)
				drawC(r.SON[i], g, view, doBackground, c);
		}
	}

	boolean ClipSquare(QLine q, QSquare s) {
		boolean res = PtOnBoundary(q.P1.x, q.P1.y, s) || PtOnBoundary(q.P2.x, q.P2.y, s)
				|| CSquare(q.P1.x, q.P1.y, q.P2.x, q.P2.y, s);
		return res;
	}

	QEdgeList ClipLines(QEdgeList l, QSquare r) {
		if (l == null) {
			return null;
		}
		if (ClipSquare(l.DATA, r))
			return (AddToList(l.DATA, ClipLines(l.NEXT, r)));
		return (ClipLines(l.NEXT, r));
	}

	boolean Possible_PM1R_Merge(QNode P) {
		return (P.SON[0].NODETYPE != GRAY || P.SON[1].NODETYPE != GRAY || P.SON[2].NODETYPE != GRAY
				|| P.SON[3].NODETYPE != GRAY);
	}

	void SplitPMNode(QNode P) {
		double XF[] = { -0.25, 0.25, -0.25, 0.25 };
		double YF[] = { 0.25, 0.25, -0.25, -0.25 };
		QNode Q;
		QSquare S;

		for (int i = 0; i < 4; i++) {
			Q = new QNode();
			Q.level = P.level + 1;
			P.SON[i] = Q;
			for (int j = 0; j < 4; j++)
				Q.SON[j] = null;
			Q.NODETYPE = BLACK;
			S = new QSquare();
			S.CENTER = new DPoint(P.SQUARE.CENTER.x + XF[i] * P.SQUARE.LEN, P.SQUARE.CENTER.y + YF[i] * P.SQUARE.LEN);
			S.LEN = 0.5 * P.SQUARE.LEN;
			Q.SQUARE = S;
			Q.DICTIONARY = null;
		}
		P.DICTIONARY = null;
		P.NODETYPE = GRAY;
	}

	boolean PtInSquare(DPoint p, QSquare S) {
		if (S.CENTER.x - S.LEN / 2 <= p.x && p.x <= S.CENTER.x + S.LEN / 2 && S.CENTER.y - S.LEN / 2 <= p.y
				&& p.y <= S.CENTER.y + S.LEN / 2)
			return true;
		else
			return false;
	}

	final int left = 1;
	final int right = 2;
	final int up = 4;
	final int down = 8;

	boolean PtOnBoundary(double x, double y, QSquare s) {
		return (s.CENTER.x - s.LEN / 2 <= x && x <= s.CENTER.x + s.LEN / 2
				&& (y == s.CENTER.y - s.LEN / 2 || y == s.CENTER.y + s.LEN / 2))
				|| (s.CENTER.y - s.LEN / 2 <= y && y <= s.CENTER.y + s.LEN / 2
						&& (x == s.CENTER.x - s.LEN / 2 || x == s.CENTER.x + s.LEN / 2));
	}

	boolean CSquare(double x1, double y1, double x2, double y2, QSquare S) {
		int code1 = ClipArea(x1, y1, S);
		int code2 = ClipArea(x2, y2, S);
		if (code1 == 0 || code2 == 0) // one vertex inside, line intersects
			return true;
		if (((code1 & code2) != 0) || (Math.abs(x1 - x2) < 1e-5 && Math.abs(y1 - y2) < 1e-5))
			return false;
		double mid_x = (x1 + x2) / 2;
		double mid_y = (y1 + y2) / 2;
		return CSquare(x1, y1, mid_x, mid_y, S) || CSquare(x2, y2, mid_x, mid_y, S);
	}

	int ClipArea(double x, double y, QSquare S) {

		int c = 0;
		if (x < S.CENTER.x - S.LEN / 2)
			c = left;
		else if (x >= S.CENTER.x + S.LEN / 2)
			c = right;
		if (y < S.CENTER.y - S.LEN / 2)
			c |= down;
		else if (y >= S.CENTER.y + S.LEN / 2)
			c |= up;
		return c;
	}

	// ----- QEdgeList functions not provided in the book

	QEdgeList MergeLists(QEdgeList l, QEdgeList r) {
		QEdgeList root = null;
		QEdgeList last = null;

		while (l != null) {
			last = root;
			root = new QEdgeList(l.DATA);
			if (last != null)
				root.NEXT = last;
			l = l.NEXT;
		}

		while (r != null) {
			last = root;
			root = new QEdgeList(r.DATA);
			if (last != null)
				root.NEXT = last;
			r = r.NEXT;
		}

		return root;
	}

	QEdgeList SetUnion(QEdgeList l, QEdgeList r) {
		QEdgeList root = null;
		QEdgeList last = null;
		QEdgeList left = l;

		while (l != null) {
			last = root;
			root = new QEdgeList(l.DATA);
			if (last != null)
				root.NEXT = last;
			l = l.NEXT;
		}

		while (r != null) {
			QEdgeList pt;
			for (pt = left; pt != null; pt = pt.NEXT) {
				if (r.DATA.equals(pt.DATA))
					break;
			}
			if (pt == null) {
				last = root;
				root = new QEdgeList(r.DATA);
				if (last != null)
					root.NEXT = last;
			}
			r = r.NEXT;
		}

		return root;
	}

	QEdgeList SetDifference(QEdgeList l, QEdgeList r) {
		QEdgeList root = null;
		QEdgeList last = null;
		QEdgeList left = l;
		QEdgeList pt;

		while (l != null) { // erase duplicate elements just once
			ext: for (pt = r; pt != null; pt = pt.NEXT) {
				if (l.DATA.equals(pt.DATA)) {
					for (QEdgeList loc = left; loc != l; loc = loc.NEXT)
						if (loc.DATA.equals(l.DATA)) {
							pt = null;
							break ext;
						}
					break;
				}
			}
			if (pt == null) {
				last = root;
				root = new QEdgeList(l.DATA);
				if (last != null)
					root.NEXT = last;
			}
			l = l.NEXT;
		}

		// while(r != null) {
		// for (pt = left; pt != null; pt = pt.NEXT) {
		// if (r.DATA.equals(pt.DATA))
		// break;
		// }
		// if (pt == null) {
		// last = root;
		// root = new QEdgeList(r.DATA);
		// if (last != null)
		// root.NEXT = last;
		// }
		// r = r.NEXT;
		// }

		return root;
	}

	QEdgeList AddToList(QLine q, QEdgeList l) {
		QEdgeList ln = new QEdgeList(q);
		return MergeLists(ln, l);
	}

	// -------------- Window Search -------------

	// void lineInOut(DLine l, DRectangle searchRect, int mode, SearchVector sv,
	// Vector searchVector) {
	// boolean isBlue = false;
	// if ((mode & SEARCHMODE_ISCONTAINED) != 0) // both points inside
	// isBlue = isBlue || (searchRect.contains(l.p1) && searchRect.contains(l.p2));
	// if ((mode & SEARCHMODE_CROSSES) != 0) // line intersects but no endpoints are
	// inside
	// isBlue = isBlue || (CSquare(l.p1.x, l.p1.y, l.p2.x, l.p2.y, searchRect) &&
	// !(searchRect.contains(l.p1) || searchRect.contains(l.p2)));
	// if ((mode & SEARCHMODE_OVERLAPS) != 0)
	// isBlue = isBlue || ((!searchRect.contains(l.p1) && searchRect.contains(l.p2))
	// ||
	// (searchRect.contains(l.p1) && !searchRect.contains(l.p2)));

	// if (isBlue)
	// sv.addElement(new SVElement(new LineIn(l), searchVector));
	// else
	// sv.addElement(new SVElement(new LineOut(l), searchVector));
	// }

	// boolean intersects(DRectangle q, QSquare s) {
	// double CX = s.CENTER.x;
	// double CY = s.CENTER.y;
	// double L = s.LEN / 2;

	// if (CX - L > q.x + q.width || q.x > CX + L ||
	// CY - L > q.y + q.height || q.y > CY + L)
	// return false;
	// else
	// return true;
	// }

	Vector searchVector;
	Vector processed;

	void findLines2(QueryObject searchRect, QNode r, SearchVector v, int mode) {
		DRectangle thisQuad = r.SQUARE.toDRectangle();

		v.addElement(new SVElement(new YellowBlock(thisQuad, r.NODETYPE != GRAY), searchVector));

		if (r.DICTIONARY == null) {
			for (int i = 3; i >= 0; i--)
				if (r.SON[i] != null && searchRect.intersects(r.SON[i].SQUARE.toDRectangle()))
					searchVector.addElement(r.SON[i].SQUARE.toDRectangle());

			for (int i = 0; i < 4; i++)
				if (r.SON[i] != null && searchRect.intersects(r.SON[i].SQUARE.toDRectangle())) {
					searchVector.removeElementAt(searchVector.size() - 1);
					findLines2(searchRect, r.SON[i], v, mode);
				}
		} else {
			Vector greenLines = new Vector();
			for (QEdgeList el = r.DICTIONARY; el != null; el = el.NEXT) {
				if (!processed.contains(el.DATA)) {
					greenLines.addElement(toLine(el.DATA));
				}
			}
			if (greenLines.size() >= 1)
				v.addElement(new SVElement(new GreenLines(greenLines), searchVector));

			for (QEdgeList el = r.DICTIONARY; el != null; el = el.NEXT) {
				if (!processed.contains(el.DATA)) {
					processed.addElement(el.DATA);
					drawableInOut(searchRect, toLine(el.DATA), mode, v, searchVector);
				}
			}

		}
	}

	DLine toLine(QLine r) {
		return new DLine(r.P1, r.P2);
	}

	Vector toVector(QEdgeList el) {
		Vector v = new Vector();
		for (; el != null; el = el.NEXT) {
			v.addElement(toLine(el.DATA));
		}
		return v;
	}

	// -------------- Nearest Neighbor -------------

	class LineIncNearest {

		class QueueElement {
			double key;

			QueueElement(double k) {
				key = k;
			}

			boolean compare(QueueElement e) {
				return false;
			}
		}

		class QueueLeaf extends QueueElement {
			DLine line;

			QueueLeaf(double k, double x1, double y1, double x2, double y2) {
				super(k);
				line = new DLine(x1, y1, x2, y2);
			}

			@Override
			boolean compare(QueueElement ql) {
				return (ql instanceof QueueLeaf) ? line.equals(((QueueLeaf) ql).line) : false;
			}
		}

		class QueueINode extends QueueElement {
			QNode r;
			DRectangle box;

			QueueINode(double k, QNode p, DRectangle b) {
				super(k);
				r = p;
				box = b;
			}

		}

		class Queue {
			Vector v;

			Queue() {
				v = new Vector();
			}

			void Enqueue(QueueElement qe) {
				v.addElement(qe);
				for (int i = v.size() - 1; i > 0; i--) {
					QueueElement q1 = (QueueElement) v.elementAt(i - 1);
					QueueElement q2 = (QueueElement) v.elementAt(i);

					if (q1.key == q2.key) {
						if (q1 instanceof QueueLeaf && q2 instanceof QueueINode) {
							v.setElementAt(q2, i - 1);
							v.setElementAt(q1, i);
						} else if (q1 instanceof QueueLeaf && q2 instanceof QueueLeaf) {
							QueueLeaf ql1 = (QueueLeaf) q1;
							QueueLeaf ql2 = (QueueLeaf) q2;
							double x1min = Math.min(ql1.line.p1.x, ql1.line.p2.x);
							double x1max = Math.max(ql1.line.p1.x, ql1.line.p2.x);
							double y1min = Math.min(ql1.line.p1.y, ql1.line.p2.y);
							double y1max = Math.max(ql1.line.p1.y, ql1.line.p2.y);
							double x2min = Math.min(ql2.line.p1.x, ql2.line.p2.x);
							double x2max = Math.max(ql2.line.p1.x, ql2.line.p2.x);
							double y2min = Math.min(ql2.line.p1.y, ql2.line.p2.y);
							double y2max = Math.max(ql2.line.p1.y, ql2.line.p2.y);
							if (x1min > x2min || (x1min == x2min && x1max > x2max)
									|| (x1min == x2min && x1max == x2max && y1min > y2min)
									|| (x1min == x2min && x1max == x2max && y1min == y2min && y1max > y2max)) {
								v.setElementAt(q2, i - 1);
								v.setElementAt(q1, i);
							}
						}
					}
					if (q1.key > q2.key) {
						v.setElementAt(q2, i - 1);
						v.setElementAt(q1, i);
					}
				}
			}

			QueueElement First() {
				QueueElement q = (QueueElement) v.elementAt(0);
				return q;
			}

			void DeleteFirst() {
				v.removeElementAt(0);
			}

			QueueElement Dequeue() {
				QueueElement q = (QueueElement) v.elementAt(0);
				v.removeElementAt(0);
				return q;
			}

			boolean isEmpty() {
				return (v.size() == 0);
			}

			Vector makeVector() {
				Vector r = new Vector();
				for (int i = 0; i < v.size(); i++) {
					QueueElement q = (QueueElement) v.elementAt(i);
					if (q instanceof QueueLeaf)
						r.addElement(new GreenLines(((QueueLeaf) q).line));
					else
						r.addElement(new QueueBlock(((QueueINode) q).box));
				}
				return r;
			}
		}

		Queue q;

		LineIncNearest(QNode rt) {
			q = new Queue();
			q.Enqueue(new QueueINode(0, rt, wholeCanvas));
		}

		DLine Query(QueryObject qu) {
			DLine[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
			return (ar.length == 0) ? null : ar[0];
		}

		void Query(QueryObject qu, SearchVector v) {
			Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
		}

		DLine[] Query(QueryObject qu, double dist) {
			return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
		}

		DLine[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
			Vector lns = new Vector();
			DRectangle b;
			int counter = 1;

			while (!q.isEmpty()) {

				QueueElement e = q.Dequeue();
				if (e instanceof QueueLeaf) {
					while (!q.isEmpty() && e.compare(q.First()))
						q.DeleteFirst();
					QueueLeaf l = (QueueLeaf) e;
					if (nrelems-- <= 0 || qu.distance(l.line) > dist)
						break;
					lns.addElement(l.line);

					ret.addElement(
							new NNElement(new NNDrawable(((QueueLeaf) e).line, counter++), e.key, q.makeVector()));
				} else {
					ret.addElement(new NNElement(new YellowBlock(((QueueINode) e).box, false), e.key, q.makeVector()));
				}

				if (e instanceof QueueINode) {
					QueueINode qi = (QueueINode) e;
					if (qi.r.NODETYPE != GRAY) {
						for (QEdgeList l = qi.r.DICTIONARY; l != null; l = l.NEXT) {
							if (qu.distance(toLine(l.DATA)) >= qu.distance(qi.box))
								q.Enqueue(new QueueLeaf(qu.distance(toLine(l.DATA)), l.DATA.P1.x, l.DATA.P1.y,
										l.DATA.P2.x, l.DATA.P2.y));
						}
					} else
						for (int i = 0; i < 4; i++) {
							if (qi.r.SON[i] != null) {
								b = qi.r.SON[i].SQUARE.toDRectangle();
								q.Enqueue(new QueueINode(qu.distance(b), qi.r.SON[i], b));
							}
						}
				}
			}
			DLine[] als = new DLine[lns.size()];
			lns.copyInto(als);
			return als;
		}
	}

}
