package vasco.rectangles;

/* $Id: GenericRectTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import java.util.*;
import java.awt.*;
import vasco.drawable.*;
// --------- Rect Quadtree ---------
/**
 * Represents a generic rectangular quadtree structure.
 * This abstract class provides a framework for quadtrees that manage rectangular objects.
 */
abstract class GenericRectTree extends RectangleStructure implements BucketIface, MaxDecompIface {

    RNode ROOT; // Root node of the quadtree
    int maxBucketSize; // Maximum number of items a node can hold before splitting
    int maxDecomp; // Maximum depth of the quadtree

    // Arrays for calculating child node positions
	final double xf[] = { 0, 0.5, 0, 0.5 };
	final double yf[] = { 0.5, 0.5, 0, 0 };

    /**
     * Constructs a new GenericRectTree.
     *
     * @param can Canvas area within which the quadtree operates.
     * @param md  Maximum depth of the quadtree.
     * @param bs  Bucket size for the nodes of the quadtree.
     * @param p   Interface for higher-level operations and interactions.
     * @param r   Utility for rebuilding tree structures.
     */
	public GenericRectTree(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = new RNode();
		maxDecomp = md;
		maxBucketSize = bs;
	}

    /**
     * Clears the quadtree, resetting it to its initial state.
     */
	public void Clear() {
		super.Clear();
		ROOT = new RNode();
	}

	/**
	 * Reinitializes the quadtree and updates its settings based on the provided choice component. 
	 * This method is typically called to reset or update the configuration of the quadtree.
	 *
	 * @param c The choice component containing configuration options for the quadtree.
	 */
	public void reInit(Choice c) {
		super.reInit(c);
		new MaxDecomp(topInterface, 9, this);
	}

	/**
	 * Inserts a rectangle into the quadtree. If insertion fails, it attempts to delete the rectangle.
	 *
	 * @param toIns The rectangle to insert.
	 * @return True if the insertion was successful, false otherwise.
	 */
	public boolean Insert(DRectangle toIns) {

		/* check for intersection somehow */
		boolean res;
		res = localInsert(ROOT, toIns, wholeCanvas, maxDecomp);
		if (!res)
			localDelete(toIns, ROOT, wholeCanvas);
		return res;
	}

	/**
	 * Deletes a rectangle from the quadtree based on a specified point.
	 *
	 * @param toDel The point indicating which rectangle to delete.
	 */
	public void Delete(DPoint toDel) {
		if (ROOT != null) {
			RectIncNearest kdin = new RectIncNearest(ROOT);
			DRectangle mx = kdin.Query(new QueryObject(toDel));
			if (mx != null)
				localDelete(mx, ROOT, wholeCanvas);
		}
	}

	/**
	 * Directly deletes a drawable rectangle from the quadtree.
	 *
	 * @param d The drawable rectangle to delete.
	 */
	public void DeleteDirect(Drawable d) {
		if (ROOT != null && d != null) {
			localDelete((DRectangle) d, ROOT, wholeCanvas);
		}
	}

	/**
	 * Searches the quadtree based on a query object and a specified mode.
	 *
	 * @param r    The query object for the search.
	 * @param mode The mode of the search.
	 * @return A SearchVector containing the search results.
	 */
	public SearchVector Search(QueryObject r, int mode) {
		searchVector = new Vector();
		SearchVector sv = new SearchVector();
		processedRectangles = new Vector();
		localSearch(ROOT, r, wholeCanvas, sv, mode);
		// System.out.println("Found " + g.size() + " rectangle(s)");
		return sv;
	}

	/**
	 * Finds the nearest rectangle to a query point.
	 *
	 * @param p The query object.
	 * @return A SearchVector with the nearest rectangle.
	 */
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		RectIncNearest mxin = new RectIncNearest(ROOT);
		mxin.Query(p, v);
		return v;
	}
	
	/**
	 * Finds all rectangles within a specified distance from a query point.
	 *
	 * @param p    The query object.
	 * @param dist The distance within which to search.
	 * @return A SearchVector with rectangles within the specified distance.
	 */
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		RectIncNearest mxin = new RectIncNearest(ROOT);
		mxin.Query(p, v, dist, Integer.MAX_VALUE);
		return v;
	}

	/**
	 * Finds the first nearest rectangle to a query point.
	 *
	 * @param p The query object.
	 * @return The nearest rectangle as a Drawable object.
	 */
	public Drawable NearestFirst(QueryObject p) {
		RectIncNearest mxin = new RectIncNearest(ROOT);
		return mxin.Query(p);
	}

	/**
	 * Finds all rectangles within a specified distance range from a query point.
	 *
	 * @param p    The query object.
	 * @param dist The distance range within which to search.
	 * @return An array of Drawable objects representing the rectangles.
	 */
	public Drawable[] NearestRange(QueryObject p, double dist) {
		RectIncNearest mxin = new RectIncNearest(ROOT);
		return mxin.Query(p, dist);
	}

	/**
	 * Draws the contents of the quadtree.
	 *
	 * @param gg    The drawing target for rendering the structure.
	 * @param view  The rectangular area of the view in which the content is drawn.
	 */
	public void drawContents(DrawingTarget gg, Rectangle view) {
		drawC(ROOT, gg, wholeCanvas, view);
	}

	/* ---------------- interface implementation ---------- */
	/**
	 * Gets the maximum decomposition depth of the quadtree.
	 *
	 * @return The maximum depth of decomposition.
	 */
	public int getMaxDecomp() {
		return maxDecomp;
	}

	/**
	 * Sets the maximum decomposition depth of the quadtree and rebuilds it.
	 *
	 * @param b The new maximum depth of decomposition.
	 */
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

	/**
	 * Gets the maximum number of rectangles a bucket in the quadtree can hold.
	 *
	 * @return The maximum bucket size.
	 */
	public int getBucket() {
		return maxBucketSize;
	}

	/**
	 * Sets the maximum bucket size for the quadtree nodes and rebuilds the tree.
	 *
	 * @param b The new maximum bucket size.
	 */
	public void setBucket(int b) {
		maxBucketSize = b;
		reb.rebuild();
	}

	/**
	 * Inserts a rectangle into a specific node of the quadtree.
	 *
	 * @param q     The node where the rectangle is to be inserted.
	 * @param r     The rectangle to be inserted.
	 * @param block The area of the node.
	 * @param md    The maximum depth for insertion.
	 * @return True if insertion is successful, false otherwise.
	 */
	abstract boolean localInsert(RNode q, DRectangle r, DRectangle block, int md);

	/**
	 * Locally deletes a rectangle from a specific node of the quadtree.
	 *
	 * @param todel The rectangle to delete.
	 * @param r     The node from which the rectangle is to be deleted.
	 * @param block The area of the node.
	 */
	void localDelete(DRectangle todel, RNode r, DRectangle block) {
		if (r.NODETYPE == BLACK) {
			r.deleteRect(todel);
			if (r.r.size() == 0)
				r.NODETYPE = WHITE;
			return;
		}
		// do nothing if white (happens only in case of empty tree)
		if (r.NODETYPE == GRAY) {
			for (int i = 0; i < 4; i++) {
				DRectangle dr = new DRectangle(block.x + xf[i] * block.width, block.y + yf[i] * block.height,
						block.width / 2, block.height / 2);
				if (todel.intersects(dr)) {
					// System.out.println("deleting from :" + i);
					localDelete(todel, r.son[i], dr);
				}
			}

			Vector children = new Vector();
			for (int i = 0; i < 4; i++) {
				if (r.son[i].NODETYPE == GRAY)
					return;
				for (int j = 0; j < r.son[i].r.size(); j++)
					if (!children.contains(r.son[i].r.elementAt(j)))
						children.addElement(r.son[i].r.elementAt(j));
			}
			if (children.size() <= maxBucketSize) {
				r.son[0] = r.son[1] = r.son[2] = r.son[3] = null;
				if (children.size() == 0)
					r.NODETYPE = WHITE;
				else {
					r.NODETYPE = BLACK;
					r.r = children;
				}
			}
		}
	}

	// ----- SEARCH ------

	Vector searchVector;
	boolean overlaps;
	Vector processedRectangles;

	/**
	 * Searches the quadtree based on a query object and mode, starting from a specific node.
	 *
	 * @param r       The node to start the search from.
	 * @param newrect The query object used for searching.
	 * @param block   The area of the node.
	 * @param v       The vector to store search results.
	 * @param mode    The mode of the search.
	 */
	void localSearch(RNode r, QueryObject newrect, DRectangle block, SearchVector v, int mode) {
		v.addElement(new SVElement(new YellowBlock(block, r.NODETYPE != GRAY), searchVector));

		switch (r.NODETYPE) {
		case BLACK:
			int i;
			for (int j = 0; j < r.r.size(); j++) {
				for (i = 0; i < processedRectangles.size(); i++)
					if (((DRectangle) (processedRectangles.elementAt(i))).equals(r.r.elementAt(j)))
						// was reported already?
						break;
				if (i == processedRectangles.size()) {
					drawableInOut(newrect, (DRectangle) r.r.elementAt(j), mode, v, searchVector);
					processedRectangles.addElement(r.r.elementAt(j));
				}
			}
			break;
		case GRAY:
			for (i = 3; i >= 0; i--) {
				DRectangle dr = new DRectangle(block.x + xf[i] * block.width, block.y + yf[i] * block.height,
						block.width / 2, block.height / 2);
				if (newrect.intersects(dr))
					searchVector.addElement(dr);
			}
			for (i = 0; i < 4; i++) {
				DRectangle dr = new DRectangle(block.x + xf[i] * block.width, block.y + yf[i] * block.height,
						block.width / 2, block.height / 2);
				if (newrect.intersects(dr)) {
					searchVector.removeElementAt(searchVector.size() - 1);
					localSearch(r.son[i], newrect, dr, v, mode);
				}
			}
			break;
		}
	}

	/**
	 * Draws the contents of a specific node of the quadtree.
	 *
	 * @param r       The node whose contents are to be drawn.
	 * @param g       The drawing target for rendering.
	 * @param block   The area of the node.
	 * @param view    The view area where the node's contents are visible.
	 */
	void drawC(RNode r, DrawingTarget g, DRectangle block, Rectangle view) {
		if (!g.visible(block))
			return;

		if (r == null)
			return;

		g.setColor(Color.black);
		block.draw(g);

		g.setColor(Color.red);
		for (int i = 0; i < r.r.size(); i++) {
			DRectangle rect = (DRectangle) r.r.elementAt(i);
			rect.draw(g);
		}
		for (int i = 0; i < 4; i++) {
			drawC(r.son[i], g, new DRectangle(block.x + xf[i] * block.width, block.y + yf[i] * block.height,
					block.width / 2, block.height / 2), view);
		}
	}

	// -----------------------------------------
	/**
	 * Manages a queue for the incremental nearest neighbor search in the quadtree.
	 */
	class RectIncNearest {

	    /**
	     * Base class for elements in the search queue.
	     */
		class RectQueueElement {
	        double[] keys; // Array storing key values for sorting in the queue

	        /**
	         * Constructs a new queue element with specified keys.
	         *
	         * @param k The keys used for sorting in the search queue.
	         */
			RectQueueElement(double[] k) {
				keys = k;
			}
		}

	    /**
	     * Represents a leaf node in the search queue, holding a rectangle.
	     */
		class RectQLeaf extends RectQueueElement {
	        DRectangle rect; // The rectangle associated with this queue element

	        /**
	         * Constructs a leaf element for the queue.
	         *
	         * @param k    The keys used for sorting in the search queue.
	         * @param p    The rectangle associated with this leaf element.
	         */
			RectQLeaf(double[] k, DRectangle p) {
				super(k);
				rect = p;
			}
		}

	    /**
	     * Represents an internal node in the search queue.
	     */
		class RectQINode extends RectQueueElement {
	        RNode r;         // The quadtree node associated with this queue element
	        DRectangle block; // The area covered by the quadtree node

	        /**
	         * Constructs an internal node element for the queue.
	         *
	         * @param k    The keys used for sorting in the search queue.
	         * @param p    The quadtree node associated with this element.
	         * @param b    The area covered by the quadtree node.
	         */
			RectQINode(double[] k, RNode p, DRectangle b) {
				super(k);
				r = p;
				block = b;
			}
		}

	    /**
	     * Manages the priority queue for the incremental nearest neighbor search.
	     */
		class RectQueue {

	        Vector v; // Vector to store elements in the queue

	        /**
	         * Constructs a new empty search queue.
	         */
			RectQueue() {
				v = new Vector();
			}
			
	        /**
	         * Adds an element to the queue and sorts it based on keys.
	         *
	         * @param qe The queue element to add.
	         */
			void Enqueue(RectQueueElement qe) {
				v.addElement(qe);
				for (int i = v.size() - 1; i > 0; i--) {
					RectQueueElement q1 = (RectQueueElement) v.elementAt(i - 1);
					RectQueueElement q2 = (RectQueueElement) v.elementAt(i);

					if (q1.keys[0] == q2.keys[0] && q1.keys[1] == q2.keys[1] && q1 instanceof RectQLeaf
							&& q2 instanceof RectQLeaf) {
						RectQLeaf ql1 = (RectQLeaf) q1;
						RectQLeaf ql2 = (RectQLeaf) q2;
						if (ql1.rect.x > ql2.rect.x || (ql1.rect.x == ql2.rect.x && ql1.rect.y > ql2.rect.y)
								|| (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y
										&& ql1.rect.width > ql2.rect.width)
								|| (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y
										&& ql1.rect.width == ql2.rect.width && ql1.rect.height > ql2.rect.height)) {
							v.setElementAt(q2, i - 1);
							v.setElementAt(q1, i);
						}
					}

					if (q1.keys[0] > q2.keys[0] || (q1.keys[0] == q2.keys[0]
							&& ((q1.keys[1] > q2.keys[1] && q1 instanceof RectQLeaf && q2 instanceof RectQLeaf)
									|| (q1 instanceof RectQLeaf && !(q2 instanceof RectQLeaf))))) {
						v.setElementAt(q2, i - 1);
						v.setElementAt(q1, i);
					}

					/*
					 * if (q1.key[0] == q2.key[0] &&) { if (q1 instanceof RectQLeaf && q2 instanceof
					 * RectQINode) { v.setElementAt(q2, i - 1); v.setElementAt(q1, i); } else if (q1
					 * instanceof RectQLeaf && q2 instanceof RectQLeaf) { RectQLeaf ql1 =
					 * (RectQLeaf)q1; RectQLeaf ql2 = (RectQLeaf)q2; if (ql1.rect.x > ql2.rect.x ||
					 * (ql1.rect.x == ql2.rect.x && ql1.rect.y > ql2.rect.y) || (ql1.rect.x ==
					 * ql2.rect.x && ql1.rect.y == ql2.rect.y && ql1.rect.width > ql2.rect.width) ||
					 * (ql1.rect.x == ql2.rect.x && ql1.rect.y == ql2.rect.y && ql1.rect.width ==
					 * ql2.rect.width && ql1.rect.height > ql2.rect.height)) { v.setElementAt(q2, i
					 * - 1); v.setElementAt(q1, i); } } } if (q1.key > q2.key) { v.setElementAt(q2,
					 * i - 1); v.setElementAt(q1, i); }
					 */
				}
			}

	        /**
	         * Returns the first element in the queue.
	         *
	         * @return The first queue element.
	         */
			RectQueueElement First() {
				RectQueueElement q = (RectQueueElement) v.elementAt(0);
				return q;
			}

	        /**
	         * Removes the first element from the queue.
	         */
			void DeleteFirst() {
				v.removeElementAt(0);
			}

	        /**
	         * Removes and returns the first element from the queue.
	         *
	         * @return The dequeued element.
	         */
			RectQueueElement Dequeue() {
				RectQueueElement q = (RectQueueElement) v.elementAt(0);
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
	         * Creates a vector representation of the queue elements.
	         *
	         * @return A vector containing representations of the queue elements.
	         */
			Vector makeVector() {
				Vector r = new Vector();
				for (int i = 0; i < v.size(); i++) {
					RectQueueElement q = (RectQueueElement) v.elementAt(i);
					if (q instanceof RectQLeaf)
						r.addElement(new GreenRect(((RectQLeaf) q).rect));
					else
						r.addElement(new QueueBlock(((RectQINode) q).block));
				}
				return r;
			}
		}

	    RectQueue q; // The queue used for the search

	    /**
	     * Constructs a new search for incremental nearest neighbors.
	     *
	     * @param rt The root of the quadtree to search.
	     */
		RectIncNearest(RNode rt) {
			q = new RectQueue();
			double[] zero = { 0, 0 };
			q.Enqueue(new RectQINode(zero, rt, wholeCanvas));
		}

	    /**
	     * Queries the nearest rectangle to a given query object.
	     *
	     * @param qu The query object.
	     * @return The nearest rectangle, or null if none found.
	     */
		DRectangle Query(QueryObject qu) {
			DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
			return (ar.length == 0) ? null : ar[0];
		}

	    /**
	     * Queries the nearest rectangle to a given query object and stores the results in a provided vector.
	     *
	     * @param qu The query object.
	     * @param v  The vector to store search results.
	     */
		void Query(QueryObject qu, SearchVector v) {
			Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
		}

	    /**
	     * Queries rectangles within a given distance from a query object.
	     *
	     * @param qu   The query object.
	     * @param dist The maximum distance for the search.
	     * @return An array of rectangles within the given distance.
	     */
		DRectangle[] Query(QueryObject qu, double dist) {
			return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
		}

		/**
		 * Performs a detailed query to find rectangles near the query object, considering a maximum distance and a limit on the number of elements.
		 *
		 * @param qu       The query object used for searching.
		 * @param ret      The SearchVector where detailed results of the search (including intermediate steps) are stored.
		 * @param dist     The maximum distance within which rectangles are considered near the query object.
		 * @param nrelems  The maximum number of nearest rectangles to return.
		 * @return         An array of DRectangle objects that are within the specified distance from the query object.
		 */
		private DRectangle[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
		    Vector rect = new Vector(); // Vector to store found rectangles
		    int counter = 1; // Counter for assigning sequence numbers to found rectangles

			while (!q.isEmpty()) {
		        RectQueueElement element = q.Dequeue(); // Dequeue the next element from the queue

				if (element instanceof RectQLeaf) {
		            // Process leaf node
					RectQLeaf ql = (RectQLeaf) element;
		            // Remove duplicate elements from the queue
					while (!q.isEmpty() && q.First() instanceof RectQLeaf
							&& ql.rect.equals(((RectQLeaf) q.First()).rect))
						q.DeleteFirst();
		            // Check distance and element count constraints
					if (nrelems-- <= 0 || qu.distance(ql.rect) > dist)
						break;
		            rect.addElement(ql.rect); // Add the rectangle to the results
		            // Add detailed search step to the results vector
					ret.addElement(new NNElement(new NNDrawable(ql.rect, counter++), ql.keys[0], q.makeVector()));
				} else {
		            // Process internal node
					RectQINode ql = (RectQINode) element;
					ret.addElement(new NNElement(new YellowBlock(ql.block, false), ql.keys[0], q.makeVector()));
		            // Enqueue children of the internal node based on node type
					if (ql.r.NODETYPE == BLACK) {
		                // For BLACK nodes, enqueue their contained rectangles
						for (int i = 0; i < ql.r.r.size(); i++) {
							DRectangle a = (DRectangle) ql.r.r.elementAt(i);
							if (qu.distance(a) >= qu.distance(ql.block))
								q.Enqueue(new RectQLeaf(qu.distance(a, new double[2]), a));
						}
					} else if (ql.r.NODETYPE == GRAY) {
		                // For GRAY nodes, enqueue their children nodes
						for (int i = 0; i < 4; i++)
							if (ql.r.son[i] != null) {
								DRectangle n = new DRectangle(ql.block.x + xf[i] * ql.block.width,
										ql.block.y + yf[i] * ql.block.height, ql.block.width / 2, ql.block.height / 2);
								q.Enqueue(new RectQINode(qu.distance(n, new double[2]), ql.r.son[i], n));
							}
					}
				}
			}
		    // Convert the vector of found rectangles to an array
			DRectangle[] ar = new DRectangle[rect.size()];
			rect.copyInto(ar);
			return ar;
		}
	}
}
