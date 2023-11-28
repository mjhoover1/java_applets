package vasco.rectangles;

/**
 * PMRkd - PMR Rectangle k-d Tree
 *
 * This class represents a PMR Rectangle k-d Tree for storing and querying rectangles in two-dimensional space.
 *
 * @version $Id: PMRkd.java,v 1.2 2004/11/20 22:38:48 brabec Exp $
 */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;
import vasco.drawable.*;

/**
 * PMRkd class represents a PMR Rectangle k-d Tree for storing and querying rectangles in two-dimensional space.
 */
class PMRkd extends RectangleStructure implements BucketIface, MaxDecompIface {

	int maxBucketSize;
	RNode ROOT;
	int maxDecomp;

    /**
     * Constructs a PMRkd tree with the given parameters.
     *
     * @param can The canvas representing the space.
     * @param md  The maximum decomposition level.
     * @param bs  The maximum bucket size.
     * @param p   The parent interface.
     * @param r   The rebuild tree.
     */
	public PMRkd(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
		maxDecomp = md;
		maxBucketSize = bs;
	}

	/**
	 * Reinitializes the PMR Rectangle k-d Tree with the given choice.
	 *
	 * @param c The choice used for reinitialization.
	 */
	public void reInit(JComboBox c) {
		super.reInit(c);
		ROOT = null;
		new MaxDecomp(topInterface, 18, this);
		new Bucket(topInterface, "Splitting Threshold", this);
	}

	/**
	 * Clears the PMR Rectangle k-d Tree.
	 */
	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	/**
	 * Indicates whether the order of insertion is dependent.
	 *
	 * @return True if order is dependent, false otherwise.
	 */
	public boolean orderDependent() {
		return true;
	}

	/**
	 * Inserts a rectangle into the PMR Rectangle k-d Tree.
	 *
	 * @param t The rectangle to insert.
	 * @return True if the insertion was successful, false otherwise.
	 */
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

	/**
	 * Deletes a point from the PMR Rectangle k-d Tree.
	 *
	 * @param qu The point to delete.
	 */
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

	/**
	 * Deletes a drawable object directly from the PMR Rectangle k-d Tree.
	 *
	 * @param qu The drawable object to delete.
	 */
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

	/**
	 * Finds the nearest neighbor to a query object.
	 *
	 * @param qu The query object.
	 * @return A SearchVector containing the nearest neighbor.
	 */
	public SearchVector Nearest(QueryObject qu) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
			mxin.Query(qu, v);
		}
		return v;
	}

	/**
	 * Finds the nearest neighbor to a query object within a specified distance.
	 *
	 * @param qu   The query object.
	 * @param dist The maximum distance to consider.
	 * @return A SearchVector containing the nearest neighbor(s) within the specified distance.
	 */
	public SearchVector Nearest(QueryObject qu, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
			mxin.Query(qu, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	/**
	 * Finds the nearest neighbor to a query object and returns it directly.
	 *
	 * @param p The query object.
	 * @return The nearest neighbor as a Drawable object.
	 */
	public Drawable NearestFirst(QueryObject p) {
		if (ROOT == null)
			return null;
		PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
		return mxin.Query(p);
	}

	/**
	 * Finds all objects within a specified range of a query object.
	 *
	 * @param p    The query object.
	 * @param dist The maximum distance to consider.
	 * @return An array of Drawable objects within the specified range.
	 */
	public Drawable[] NearestRange(QueryObject p, double dist) {
		if (ROOT == null)
			return null;
		PMRkdIncNearest mxin = new PMRkdIncNearest(ROOT);
		return mxin.Query(p, dist);
	}

	/**
	 * Searches the PMR Rectangle k-d Tree for objects matching the query object and mode.
	 *
	 * @param query The query object.
	 * @param mode  The search mode.
	 * @return A SearchVector containing the matching objects.
	 */
	public SearchVector Search(QueryObject query, int mode) {
		processedRectangles = new Vector();
		SearchVector sv = new SearchVector();
		searchVector = new Vector();
		search(ROOT, query, true, wholeCanvas, sv, mode);
		return sv;
	}

	/**
	 * Draws the contents of the PMR Rectangle k-d Tree on a drawing target within the specified view.
	 *
	 * @param g    The drawing target.
	 * @param view The view rectangle.
	 */
	public void drawContents(DrawingTarget g, Rectangle view) {
		if (ROOT != null)
			drawC(ROOT, g, true, wholeCanvas, view);
	}

	/**
	 * Gets the name of the PMR Rectangle k-d Tree.
	 *
	 * @return The name of the PMR Rectangle k-d Tree.
	 */
	public String getName() {
		return "PMR Rectangle k-d Tree";
	}

	/* ---------------- interface implementation ---------- */
    /**
     * Retrieves the maximum decomposition level.
     *
     * @return The maximum decomposition level.
     */
	public int getMaxDecomp() {
		return maxDecomp;
	}

    /**
     * Sets the maximum decomposition level.
     *
     * @param b The maximum decomposition level to set.
     */
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

    /**
     * Retrieves the maximum bucket size.
     *
     * @return The maximum bucket size.
     */
	public int getBucket() {
		return maxBucketSize;
	}


    /**
     * Sets the maximum bucket size.
     *
     * @param b The maximum bucket size to set.
     */
	public void setBucket(int b) {
		maxBucketSize = b;
		reb.rebuild();
	}

	// ---------------- private methods --------------------

	Vector searchVector;
	Vector processedRectangles;

    /**
     * This private method is used to search for rectangles within a specified query area.
     *
     * @param r      The root node of the k-d tree.
     * @param query  The query object representing the search area.
     * @param xcoord A boolean indicating whether the search is based on x-coordinate.
     * @param block  The current block representing the search area.
     * @param v      The search vector to store the results.
     * @param mode   The search mode.
     */
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

		if (xcoord) {             // Code for x-coordinate search
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
		} else {             // Code for y-coordinate search
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

    /**
     * Inserts a rectangle into the k-d tree.
     *
     * @param p      The rectangle to insert.
     * @param r      The root node of the k-d tree.
     * @param xcoord A boolean indicating whether the insertion is based on x-coordinate.
     * @param block  The current block representing the search area.
     * @param md     The maximum depth for insertion.
     * @return True if the insertion was successful, false otherwise.
     */
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

    /**
     * Deletes a rectangle from the k-d tree.
     *
     * @param del    The rectangle to delete.
     * @param r      The root node of the k-d tree.
     * @param xcoord A boolean indicating whether the deletion is based on x-coordinate.
     * @param block  The current block representing the search area.
     */
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

	/**
	 * Draws the given RNode in a drawing target using the specified parameters.
	 *
	 * @param r       The RNode to draw.
	 * @param g       The drawing target.
	 * @param xcoord  Indicates whether to draw based on x-coordinates or y-coordinates.
	 * @param block   The DRectangle representing the block to draw within.
	 * @param view    The Rectangle representing the view.
	 */
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

    /**
     * This class represents a queue element for the k-d tree search.
     */
	abstract class PMRkdQueueElement {
		double[] keys;

		PMRkdQueueElement(double[] k) {
			keys = k;
		}
	}

	
	/**
	 * Represents a leaf node in the PMRkdQueue.
	 */
	class PMRkdQLeaf extends PMRkdQueueElement {
		DRectangle rect;

		PMRkdQLeaf(double[] k, DRectangle p) {
			super(k);
			rect = p;
		}
	}

	/**
	 * Represents an internal node in the PMRkdQueue.
	 */
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

	/**
	 * Represents a queue for PMRkdQueueElements.
	 */
	class PMRkdQueue {
		Vector v;

		PMRkdQueue() {
			v = new Vector();
		}

	    /**
	     * Enqueues a PMRkdQueueElement in the queue.
	     *
	     * @param qe The PMRkdQueueElement to enqueue.
	     */
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

	    /**
	     * Gets the first element in the queue.
	     *
	     * @return The first element in the queue.
	     */
		PMRkdQueueElement First() {
			PMRkdQueueElement q = (PMRkdQueueElement) v.elementAt(0);
			return q;
		}

	    /**
	     * Deletes the first element in the queue.
	     */
		void DeleteFirst() {
			v.removeElementAt(0);
		}

	    /**
	     * Dequeues and returns the first element in the queue.
	     *
	     * @return The dequeued element.
	     */
		PMRkdQueueElement Dequeue() {
			PMRkdQueueElement q = (PMRkdQueueElement) v.elementAt(0);
			v.removeElementAt(0);
			return q;
		}

	    /**
	     * Checks if the queue is empty.
	     *
	     * @return True if the queue is empty, false otherwise.
	     */
		boolean isEmpty() {
			return (v.size() == 0);
		}

	    /**
	     * Creates a Vector representation of the queue elements.
	     *
	     * @return A Vector containing queue elements as GreenRect or QueueBlock objects.
	     */
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

	/**
	 * This class represents a utility for performing incremental nearest neighbor queries on a k-d tree.
	 */
	class PMRkdIncNearest {

		PMRkdQueue q;

	    /**
	     * Constructs a new PMRkdIncNearest instance with the given root RNode.
	     *
	     * @param rt The root RNode of the k-d tree.
	     */
		PMRkdIncNearest(RNode rt) {
			q = new PMRkdQueue();
			double[] zero = { 0, 0 };
			q.Enqueue(new PMRkdQINode(zero, rt, wholeCanvas, true));
		}

	    /**
	     * Performs a nearest neighbor query using the given QueryObject.
	     *
	     * @param qu The QueryObject representing the query.
	     * @return The nearest neighbor DRectangle found, or null if none is found.
	     */
		DRectangle Query(QueryObject qu) {
			DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
			return (ar.length == 0) ? null : ar[0];
		}

	    /**
	     * Performs a nearest neighbor query using the given QueryObject and stores the result in the given SearchVector.
	     *
	     * @param qu The QueryObject representing the query.
	     * @param v  The SearchVector to store the query result.
	     */
		void Query(QueryObject qu, SearchVector v) {
			Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
		}

	    /**
	     * Performs a nearest neighbor query using the given QueryObject and maximum distance.
	     *
	     * @param qu   The QueryObject representing the query.
	     * @param dist The maximum distance for the query.
	     * @return An array of DRectangles representing the query results.
	     */
		DRectangle[] Query(QueryObject qu, double dist) {
			return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
		}

	    /**
	     * Private helper method for performing the nearest neighbor query.
	     *
	     * @param qu      The QueryObject representing the query.
	     * @param ret     The SearchVector to store the query result.
	     * @param dist    The maximum distance for the query.
	     * @param nrelems The maximum number of query results to retrieve.
	     * @return An array of DRectangles representing the query results.
	     */
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
