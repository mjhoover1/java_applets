package vasco.rectangles;

/* $Id: PMRkd.java,v 1.2 2004/11/20 22:38:48 brabec Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;
import vasco.drawable.*;

class PMRkd extends RectangleStructure implements BucketIface, MaxDecompIface {

	int maxBucketSize;
	RNode ROOT;
	int maxDecomp;

	public PMRkd(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
		maxDecomp = md;
		maxBucketSize = bs;
	}

	public void reInit(Choice c) {
		super.reInit(c);
		ROOT = null;
		new MaxDecomp(topInterface, 18, this);
		new Bucket(topInterface, "Splitting Threshold", this);
	}

	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	public boolean orderDependent() {
		return true;
	}

	public boolean Insert(DRectangle t) {
		boolean res = true;
		if (ROOT == null) {
			ROOT = new RNode();
			ROOT.addRect(t);
		} else
			res = insert(t, ROOT, true, wholeCanvas, maxDecomp);
		if (!res) {
			if (ROOT != null && ROOT.isIn(t)) {
				ROOT.deleteRect(t);
				if (ROOT.r.size() == 0)
					ROOT = null;
			} else
				delete(t, ROOT, true, wholeCanvas);
		}
		return res;
	}

	public void Delete(DPoint qu) {
		if (ROOT != null) {
			PMRkdIncNearest kdin = new PMRkdIncNearest(ROOT);
			DRectangle p = kdin.Query(new QueryObject(qu));
			if (ROOT.isIn(p)) {
				ROOT.deleteRect(p);
				if (ROOT.r.size() == 0)
					ROOT = null;
			} else
				delete(p, ROOT, true, wholeCanvas);
		}
	}

	public void DeleteDirect(Drawable qu) {
		if (ROOT != null) {
			DRectangle p = (DRectangle) qu;
			if (ROOT.isIn(p)) {
				ROOT.deleteRect(p);
				if (ROOT.r.size() == 0)
					ROOT = null;
			} else
				delete(p, ROOT, true, wholeCanvas);
		}
	}

	public SearchVector Nearest(QueryObject qu) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
			mxin.Query(qu, v);
		}
		return v;
	}

	public SearchVector Nearest(QueryObject qu, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
			mxin.Query(qu, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	public Drawable NearestFirst(QueryObject p) {
		if (ROOT == null)
			return null;
		PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
		return mxin.Query(p);
	}

	public Drawable[] NearestRange(QueryObject p, double dist) {
		if (ROOT == null)
			return null;
		PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
		return mxin.Query(p, dist);
	}

	public SearchVector Search(QueryObject query, int mode) {
		processedRectangles = new Vector();
		SearchVector sv = new SearchVector();
		searchVector = new Vector();
		search(ROOT, query, true, wholeCanvas, sv, mode);
		return sv;
	}

	public void drawContents(DrawingTarget g, Rectangle view) {
		if (ROOT != null)
			drawC(ROOT, g, true, wholeCanvas, view);
	}

	public String getName() {
		return "PMR Rectangle k-d Tree";
	}

	/* ---------------- interface implementation ---------- */

	public int getMaxDecomp() {
		return maxDecomp;
	}

	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

	public int getBucket() {
		return maxBucketSize;
	}

	public void setBucket(int b) {
		maxBucketSize = b;
		reb.rebuild();
	}

	// ---------------- private methods --------------------

	Vector searchVector;
	Vector processedRectangles;

	private void search(RNode r, QueryObject query, boolean xcoord, DRectangle block, SearchVector v, int mode) {
		v.addElement(new SVElement(new YellowBlock(block, r == null || r.NODETYPE != GRAY), searchVector));

		if (r == null)
			return;

		if (r.NODETYPE != GRAY) {
			int i;
			for (int j = 0; j < r.r.size(); j++) {
				for (i = 0; i < processedRectangles.size(); i++)
					if (((DRectangle) (processedRectangles.elementAt(i))).equals(r.r.elementAt(j)))
						// was reported already?
						break;
				if (i == processedRectangles.size()) {
					drawableInOut(query, (DRectangle) r.r.elementAt(j), mode, v, searchVector);
					processedRectangles.addElement(r.r.elementAt(j));
				}
			}
			return;
		}

		if (xcoord) {
			DRectangle left = new DRectangle(block.x, block.y, block.width / 2, block.height);
			DRectangle right = new DRectangle(block.x + block.width / 2, block.y, block.width / 2, block.height);
			if (!query.intersects(left))
				search(r.son[RIGHT], query, !xcoord, right, v, mode);
			else if (!query.intersects(right))
				search(r.son[LEFT], query, !xcoord, left, v, mode);
			else {
				searchVector.addElement(right);
				search(r.son[LEFT], query, !xcoord, left, v, mode);
				searchVector.removeElementAt(searchVector.size() - 1);
				search(r.son[RIGHT], query, !xcoord, right, v, mode);
			}
		} else {
			DRectangle left = new DRectangle(block.x, block.y, block.width, block.height / 2);
			DRectangle right = new DRectangle(block.x, block.y + block.height / 2, block.width, block.height / 2);
			if (!query.intersects(left))
				search(r.son[RIGHT], query, !xcoord, right, v, mode);
			else if (!query.intersects(right))
				search(r.son[LEFT], query, !xcoord, left, v, mode);
			else {
				searchVector.addElement(right);
				search(r.son[LEFT], query, !xcoord, left, v, mode);
				searchVector.removeElementAt(searchVector.size() - 1);
				search(r.son[RIGHT], query, !xcoord, right, v, mode);
			}
		}
	}

	boolean insert(DRectangle p, RNode r, boolean xcoord, DRectangle block, int md) {
		boolean ok = md > 0;

		if (r.NODETYPE == WHITE) {
			r.addRect(p);
			r.NODETYPE = BLACK;
			return ok;
		}

		if (r.NODETYPE == GRAY) {
			if (xcoord) {
				if (p.x < block.x + block.width / 2) // x coordinate
					ok = insert(p, r.son[0], !xcoord, new DRectangle(block.x, block.y, block.width / 2, block.height),
							md - 1) && ok;
				if (p.x + p.width > block.x + block.width / 2)
					ok = insert(p, r.son[1], !xcoord,
							new DRectangle(block.x + block.width / 2, block.y, block.width / 2, block.height), md - 1)
							&& ok;

			} else { // y coordinate
				if (p.y + p.height > block.y + block.height / 2)
					ok = insert(p, r.son[1], !xcoord,
							new DRectangle(block.x, block.y + block.height / 2, block.width, block.height / 2), md - 1)
							&& ok;
				if (p.y < block.y + block.height / 2)
					ok = insert(p, r.son[0], !xcoord, new DRectangle(block.x, block.y, block.width, block.height / 2),
							md - 1) && ok;
			}
			return ok;
		}

		if (r.NODETYPE == BLACK) {
			r.addRect(p);
			if (r.r.size() > maxBucketSize) {
				ok = ok && md > 1;
				r.NODETYPE = GRAY;
				r.son[0] = new RNode();
				r.son[1] = new RNode();
				Vector pts = r.r;
				r.r = new Vector();
				for (int i = 0; i < pts.size(); i++) {
					DRectangle pt = (DRectangle) pts.elementAt(i);
					if (xcoord) {
						if (pt.x < block.x + block.width / 2) {
							r.son[0].addRect(pt);
							r.son[0].NODETYPE = BLACK;
						}
						if (pt.x + pt.width > block.x + block.width / 2) {
							r.son[1].addRect(pt);
							r.son[1].NODETYPE = BLACK;
						}
					} else {
						if (pt.y < block.y + block.height / 2) {
							r.son[0].addRect(pt);
							r.son[0].NODETYPE = BLACK;
						}
						if (pt.y + pt.height > block.y + block.height / 2) {
							r.son[1].addRect(pt);
							r.son[1].NODETYPE = BLACK;
						}
					}
				}
			}
		}
		return ok;
	}

	private void delete(DRectangle del, RNode r, boolean xcoord, DRectangle block) {
		if (xcoord) {
			if (del.x < block.x + block.width / 2) {
				if (r.son[0].isIn(del))
					r.son[0].deleteRect(del);
				else
					delete(del, r.son[0], !xcoord, new DRectangle(block.x, block.y, block.width / 2, block.height));
			}
			if (del.x + del.width > block.x + block.width / 2) {
				if (r.son[1].isIn(del))
					r.son[1].deleteRect(del);
				else
					delete(del, r.son[1], !xcoord,
							new DRectangle(block.x + block.width / 2, block.y, block.width / 2, block.height));
			}
		} else { // y coordinate
			if (del.y < block.y + block.height / 2) {
				if (r.son[0].isIn(del))
					r.son[0].deleteRect(del);
				else
					delete(del, r.son[0], !xcoord, new DRectangle(block.x, block.y, block.width, block.height / 2));
			}
			if (del.y + del.height > block.y + block.height / 2) {
				if (r.son[1].isIn(del))
					r.son[1].deleteRect(del);
				else
					delete(del, r.son[1], !xcoord,
							new DRectangle(block.x, block.y + block.height / 2, block.width, block.height / 2));
			}
		}

		if (r.son[0].NODETYPE != GRAY && r.son[1].NODETYPE != GRAY
				&& r.son[1].r.size() + r.son[1].r.size() <= maxBucketSize) {
			r.r = new Vector();
			r.NODETYPE = (r.son[0].r.size() + r.son[1].r.size() == maxBucketSize) ? BLACK : WHITE;
			for (int i = 0; i < r.son[0].r.size(); i++) {
				DRectangle p = (DRectangle) r.son[0].r.elementAt(i);
				r.addRect(p);
			}
			for (int i = 0; i < r.son[1].r.size(); i++) {
				DRectangle p = (DRectangle) r.son[1].r.elementAt(i);
				r.addRect(p);
			}
			r.son[0] = r.son[1] = null;
		}

	}

	private void drawC(RNode r, DrawingTarget g, boolean xcoord, DRectangle block, Rectangle view) {
		if (!g.visible(block))
			return;

		/*
		 * if (r == null) { return; }
		 */
		if (r.NODETYPE == BLACK || r.NODETYPE == WHITE) {
			g.setColor(Color.red);
			for (int i = 0; i < r.r.size(); i++) {
				DRectangle p = (DRectangle) r.r.elementAt(i);
				g.drawRect(p.x, p.y, p.width, p.height);
			}
		} else if (r.NODETYPE == GRAY) {
			g.setColor(Color.black);
			if (xcoord)
				g.drawLine(block.x + block.width / 2, block.y, block.x + block.width / 2, block.y + block.height);
			else
				g.drawLine(block.x, block.y + block.height / 2, block.x + block.width, block.y + block.height / 2);

			if (xcoord) {
				drawC(r.son[0], g, !xcoord, new DRectangle(block.x, block.y, block.width / 2, block.height), view);
				drawC(r.son[1], g, !xcoord,
						new DRectangle(block.x + block.width / 2, block.y, block.width / 2, block.height), view);
			} else {
				drawC(r.son[0], g, !xcoord, new DRectangle(block.x, block.y, block.width, block.height / 2), view);
				drawC(r.son[1], g, !xcoord,
						new DRectangle(block.x, block.y + block.height / 2, block.width, block.height / 2), view);
			}
		}
	}

	abstract class PMRkdQueueElement {
		double[] keys;

		PMRkdQueueElement(double[] k) {
			keys = k;
		}
	}

	class PMRkdQLeaf extends PMRkdQueueElement {
		DRectangle rect;

		PMRkdQLeaf(double[] k, DRectangle p) {
			super(k);
			rect = p;
		}
	}

	class PMRkdQINode extends PMRkdQueueElement {
		RNode r;
		DRectangle block;
		boolean xcoord;

		PMRkdQINode(double[] k, RNode p, DRectangle b, boolean coo) {
			super(k);
			r = p;
			block = b;
			xcoord = coo;
		}
	}

	class PMRkdQueue {
		Vector v;

		PMRkdQueue() {
			v = new Vector();
		}

		void Enqueue(PMRkdQueueElement qe) {
			v.addElement(qe);
			for (int i = v.size() - 1; i > 0; i--) {
				PMRkdQueueElement q1 = (PMRkdQueueElement) v.elementAt(i - 1);
				PMRkdQueueElement q2 = (PMRkdQueueElement) v.elementAt(i);

				if (q1.keys[0] == q2.keys[0] && q1.keys[1] == q2.keys[1] && q1 instanceof PMRkdQLeaf
						&& q2 instanceof PMRkdQLeaf) {
					PMRkdQLeaf ql1 = (PMRkdQLeaf) q1;
					PMRkdQLeaf ql2 = (PMRkdQLeaf) q2;
					if (ql1.rect.x > ql2.rect.x || (ql1.rect.x == ql2.rect.x && ql1.rect.y > ql2.rect.y)
							|| (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y && ql1.rect.width > ql2.rect.width)
							|| (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y && ql1.rect.width == ql2.rect.width
									&& ql1.rect.height > ql2.rect.height)) {
						v.setElementAt(q2, i - 1);
						v.setElementAt(q1, i);
					}
				}

				if (q1.keys[0] > q2.keys[0] || (q1.keys[0] == q2.keys[0]
						&& ((q1.keys[1] > q2.keys[1] && q1 instanceof PMRkdQLeaf && q2 instanceof PMRkdQLeaf)
								|| (q1 instanceof PMRkdQLeaf && !(q2 instanceof PMRkdQLeaf))))) {
					v.setElementAt(q2, i - 1);
					v.setElementAt(q1, i);
				}

				/*
				 * if (q1.key == q2.key) { if (q1 instanceof PMRkdQLeaf && q2 instanceof
				 * PMRkdQINode) { v.setElementAt(q2, i - 1); v.setElementAt(q1, i); } else if
				 * (q1 instanceof PMRkdQLeaf && q2 instanceof PMRkdQLeaf) { PMRkdQLeaf ql1 =
				 * (PMRkdQLeaf)q1; PMRkdQLeaf ql2 = (PMRkdQLeaf)q2; if (ql1.rect.x > ql2.rect.x
				 * || (ql1.rect.x == ql2.rect.x && ql1.rect.y > ql2.rect.y) || (ql1.rect.x ==
				 * ql2.rect.x && ql1.rect.y == ql2.rect.y && ql1.rect.width > ql2.rect.width) ||
				 * (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y && ql1.rect.width ==
				 * ql2.rect.width && ql1.rect.height == ql2.rect.height)) { v.setElementAt(q2, i
				 * - 1); v.setElementAt(q1, i); } } } if (q1.key > q2.key) { v.setElementAt(q2,
				 * i - 1); v.setElementAt(q1, i); }
				 */
			}
		}

		PMRkdQueueElement First() {
			PMRkdQueueElement q = (PMRkdQueueElement) v.elementAt(0);
			return q;
		}

		void DeleteFirst() {
			v.removeElementAt(0);
		}

		PMRkdQueueElement Dequeue() {
			PMRkdQueueElement q = (PMRkdQueueElement) v.elementAt(0);
			v.removeElementAt(0);
			return q;
		}

		boolean isEmpty() {
			return (v.size() == 0);
		}

		Vector makeVector() {
			Vector r = new Vector();
			for (int i = 0; i < v.size(); i++) {
				PMRkdQueueElement q = (PMRkdQueueElement) v.elementAt(i);
				if (q instanceof PMRkdQLeaf)
					r.addElement(new GreenRect(((PMRkdQLeaf) q).rect));
				else
					r.addElement(new QueueBlock(((PMRkdQINode) q).block));
			}
			return r;
		}

	}

	class PMRkdIncNearest {

		PMRkdQueue q;

		PMRkdIncNearest(RNode rt) {
			q = new PMRkdQueue();
			double[] zero = { 0, 0 };
			q.Enqueue(new PMRkdQINode(zero, rt, wholeCanvas, true));
		}

		DRectangle Query(QueryObject qu) {
			DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
			return (ar.length == 0) ? null : ar[0];
		}

		void Query(QueryObject qu, SearchVector v) {
			Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
		}

		DRectangle[] Query(QueryObject qu, double dist) {
			return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
		}

		private DRectangle[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
			Vector rect = new Vector();
			final double cf[] = { 0, 0.5 };
			int counter = 1;

			while (!q.isEmpty()) {
				PMRkdQueueElement element = q.Dequeue();

				if (element instanceof PMRkdQLeaf) {
					PMRkdQLeaf lf = (PMRkdQLeaf) element;
					while (!q.isEmpty() && (q.First() instanceof PMRkdQLeaf)
							&& lf.rect.equals(((PMRkdQLeaf) q.First()).rect))
						q.DeleteFirst();
					if (nrelems-- <= 0 || qu.distance(lf.rect) > dist)
						break;
					rect.addElement(lf.rect);
					ret.addElement(new NNElement(new NNDrawable(lf.rect, counter++), lf.keys[0], q.makeVector()));
				} else {
					PMRkdQINode in = (PMRkdQINode) element;
					ret.addElement(new NNElement(new YellowBlock(in.block, false), in.keys[0], q.makeVector()));
					if (in.r.NODETYPE != GRAY) {
						for (int i = 0; i < in.r.r.size(); i++) {
							DRectangle a = (DRectangle) in.r.r.elementAt(i);
							if (qu.distance(a) >= qu.distance(in.block))
								q.Enqueue(new PMRkdQLeaf(qu.distance(a, new double[2]), a));
						}
					} else if (in.r.NODETYPE == GRAY) {
						for (int i = 0; i < 2; i++)
							if (in.r.son[i] != null) {
								DRectangle dr = new DRectangle(in.block.x + (in.xcoord ? cf[i] * in.block.width : 0),
										in.block.y + (!in.xcoord ? cf[i] * in.block.height : 0),
										in.block.width / (in.xcoord ? 2 : 1), in.block.height / (!in.xcoord ? 2 : 1));

								q.Enqueue(new PMRkdQINode(qu.distance(dr, new double[2]), in.r.son[i], dr, !in.xcoord));
							}
					}
				}
			}
			DRectangle[] ar = new DRectangle[rect.size()];
			rect.copyInto(ar);
			return ar;
		}
	}
}
