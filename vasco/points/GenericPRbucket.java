package vasco.points;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: GenericPRbucket.java,v 1.2 2007/10/28 15:38:17 jagan Exp $ */
import vasco.common.BucketIface;
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

/**
 * GenericPRbucket is an abstract class providing a skeletal implementation of a
 * PR Quadtree with bucketing. It implements interfaces for managing maximal
 * decomposition and bucketing operations in spatial data structures.
 */
abstract class GenericPRbucket extends PointStructure implements MaxDecompIface, BucketIface {

	/**
	 * Inner class representing a node in the PR Quadtree. Nodes can be of two
	 * types: black (containing points) or gray (subdivision nodes).
	 */
	class PRbucketNode {
		int NODETYPE; // Type of the node (BLACK or GRAY)
		Vector points; // Collection of points in the node
		PRbucketNode[] SON = new PRbucketNode[4]; // Array representing the four children of the node

		/**
		 * Constructor to create a new node of a given type.
		 *
		 * @param type The type of the node (BLACK or GRAY).
		 */
		PRbucketNode(int type) {
			NODETYPE = type;
			points = new Vector();
			for (int i = 0; i < 4; i++)
				SON[i] = null;
		}

		/**
		 * Checks if a given point is present in the node. This method iterates through
		 * the points stored in the node and checks if any point matches the given
		 * point. It uses the `equals` method of the `DPoint` class to compare points.
		 *
		 * @param p The point to check for presence in the node.
		 * @return true if the point is found in the node, false otherwise.
		 */
		boolean isIn(DPoint p) {
			for (int i = 0; i < points.size(); i++) {
				DPoint s = (DPoint) points.elementAt(i);
				if (s.equals(p))
					return true;
			}
			return false;
		}

		/**
		 * Adds a point to the node if it is not already present. This method first
		 * checks whether the point is already in the node using the `isIn` method. If
		 * the point is not present, it is added to the points vector.
		 *
		 * @param p The point to be added to the node.
		 */
		void addPoint(DPoint p) {
			if (!isIn(p))
				points.addElement(p);
		}

		/**
		 * Deletes a point from the node. It iterates through the points in the node and
		 * removes the first occurrence of the given point. The comparison is done using
		 * the `equals` method of the `DPoint` class.
		 *
		 * @param p The point to be removed from the node.
		 */
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

	PRbucketNode ROOT; // The root node of the PR Quadtree
	int maxDecomp; // Maximum decomposition level for the Quadtree
	int maxBucketSize; // Maximum size of the bucket for the Quadtree

	/**
	 * Constructs a new GenericPRbucket object. This constructor initializes the PR
	 * Quadtree with specified settings. The GenericPRbucket class represents a PR
	 * Quadtree with bucketing functionality, used for efficient spatial data
	 * management.
	 *
	 * @param can The bounding rectangle of the spatial area covered by the
	 *            Quadtree.
	 * @param b   The maximum number of points (bucket size) allowed in a leaf node
	 *            of the Quadtree.
	 * @param md  The maximum decomposition level of the Quadtree, which controls
	 *            the depth of tree subdivision.
	 * @param p   An interface instance for top-level operations and interactions
	 *            with the Quadtree.
	 * @param r   An instance of RebuildTree used to handle the rebuilding of the
	 *            tree structure when necessary.
	 */
	GenericPRbucket(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
		maxDecomp = md;
		maxBucketSize = b;
	}

	/**
	 * Reinitializes the PR Quadtree with new settings. This method resets the
	 * Quadtree and configures it based on the given choice settings. It also
	 * initializes the maximum decomposition using the provided parameters.
	 *
	 * @param ao The choice settings to configure the Quadtree.
	 */
	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		new MaxDecomp(topInterface, 9, this);
		addItemIfNotExists(ao, "Nearest");
		addItemIfNotExists(ao, "Within");
	}

	/**
	 * Clears the PR Quadtree, resetting it to its initial state. This method
	 * removes all points and resets the root of the Quadtree.
	 */
	@Override
	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	/**
	 * Inserts a point into the PR Quadtree. This method initiates the recursive
	 * insertion process starting from the root node. If the insertion fails, it
	 * attempts to delete the point to ensure consistency.
	 *
	 * @param p The point to be inserted.
	 * @return true if the point is successfully inserted, false otherwise.
	 */
	@Override
	public boolean Insert(DPoint p) {
		boolean[] ok = new boolean[1];
		ok[0] = true;
		ROOT = insert(p, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width, wholeCanvas.height, maxDecomp, ok);
		if (!ok[0])
			Delete(p);
		return ok[0];
	}

	/**
	 * Deletes a point from the PR Quadtree. This method finds the nearest point to
	 * the given point and then deletes it from the tree.
	 *
	 * @param p The point to be deleted.
	 */
	@Override
	public void Delete(DPoint p) {
		if (ROOT == null)
			return;

		PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
		ROOT = delete(prin.Query(new QueryObject(p)), ROOT, wholeCanvas.x + wholeCanvas.width / 2,
				wholeCanvas.y + wholeCanvas.height / 2, wholeCanvas.width, wholeCanvas.height);
	}

	/**
	 * Directly deletes a drawable object from the PR Quadtree. This method is
	 * particularly useful when the object to be deleted is known and does not
	 * require searching.
	 *
	 * @param d The drawable object to be deleted.
	 */
	@Override
	public void DeleteDirect(Drawable d) {
		if (ROOT == null)
			return;
		ROOT = delete((DPoint) d, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width, wholeCanvas.height);
	}

	/**
	 * Searches for points in the PR Quadtree that meet the criteria specified in
	 * the query object. Depending on the mode, this method can find points within a
	 * certain range or nearest to a given point.
	 *
	 * @param q    The query object containing search criteria.
	 * @param mode The search mode (e.g., nearest, within range).
	 * @return A SearchVector containing the search results.
	 */
	@Override
	public SearchVector Search(QueryObject q, int mode) {
		SearchVector res = new SearchVector();
		searchVector = new Vector();
		search(ROOT, q, wholeCanvas, mode, res);
		return res;
	}

	/**
	 * Finds the nearest point in the PR Quadtree to the specified query object.
	 * This is an incremental search, which means it returns the first nearest point
	 * found.
	 *
	 * @param p The query object used for the nearest search.
	 * @return The nearest drawable object to the query point, or null if the tree
	 *         is empty.
	 */
	@Override
	public Drawable NearestFirst(QueryObject p) {
		if (ROOT == null)
			return null;
		PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
		return prin.Query(p);
	}

	/**
	 * Performs a nearest neighbor search in the PR Quadtree based on the given
	 * query object. Returns a vector of search results, each element representing a
	 * point nearest to the query point.
	 *
	 * @param p The query object for the nearest neighbor search.
	 * @return A SearchVector containing points nearest to the query object.
	 */
	@Override
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
			prin.Query(p, v);
		}
		return v;
	}

	/**
	 * Searches for points in the PR Quadtree that are within a specified distance
	 * from the query point. Returns a vector of points that are within the given
	 * distance from the query point.
	 *
	 * @param p    The query object.
	 * @param dist The maximum distance from the query point.
	 * @return A SearchVector of points within the specified distance.
	 */
	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			PRbucketIncNearest prin = new PRbucketIncNearest(ROOT);
			prin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	/**
	 * Finds all points in the PR Quadtree that are within a specified distance
	 * range from the query point. Returns an array of drawable objects that meet
	 * the distance criteria.
	 *
	 * @param p    The query object.
	 * @param dist The distance range within which to find points.
	 * @return An array of Drawable objects within the specified distance range.
	 */
	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		PRbucketIncNearest near = new PRbucketIncNearest(ROOT);
		return near.Query(p, dist);
	}

	/**
	 * Draws the contents of the PR Quadtree onto a given drawing target. The view
	 * parameter is ignored in the current implementation.
	 *
	 * @param g    The drawing target on which to draw the tree contents.
	 * @param view The view rectangle (currently ignored).
	 */
	@Override
	public void drawContents(DrawingTarget g, Rectangle view) { // view ignored
		drawC(ROOT, g, wholeCanvas.x, wholeCanvas.y, wholeCanvas.width, wholeCanvas.height);
	}

	/* ---- interface implementation ------ */
	/**
	 * Retrieves the maximum bucket size of the Quadtree. This method provides
	 * access to the maximum number of points that can be contained within a leaf
	 * node of the Quadtree.
	 *
	 * @return The maximum bucket size.
	 */
	@Override
	public int getBucket() {
		return maxBucketSize;
	}

	/**
	 * Sets the maximum bucket size of the Quadtree. This method allows the
	 * configuration of the maximum number of points that can be contained within a
	 * leaf node of the Quadtree. After setting the new bucket size, the Quadtree is
	 * rebuilt.
	 *
	 * @param b The new maximum bucket size to set.
	 */
	@Override
	public void setBucket(int b) {
		maxBucketSize = b;
		reb.rebuild();
	}

	/**
	 * Retrieves the maximum decomposition level of the Quadtree. This method
	 * provides access to the maximum depth to which the Quadtree can be subdivided.
	 *
	 * @return The maximum decomposition level.
	 */
	@Override
	public int getMaxDecomp() {
		return maxDecomp;
	}

	/**
	 * Sets the maximum decomposition level of the Quadtree. This method allows the
	 * configuration of the maximum depth to which the Quadtree can be subdivided.
	 * After setting the new decomposition level, the Quadtree is rebuilt.
	 *
	 * @param b The new maximum decomposition level to set.
	 */
	@Override
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

	/* -------------- private methods -------------------- */

	/**
	 * Compares a point with a given x and y coordinate to determine its quadrant.
	 * This method is used to decide in which quadrant of a node a point should be
	 * placed.
	 *
	 * @param P The point to compare.
	 * @param X The x-coordinate to compare against.
	 * @param Y The y-coordinate to compare against.
	 * @return An integer representing the quadrant of the point.
	 */
	int PRCompare(DPoint P, double X, double Y) {
		if (P.x < X)
			return P.y < Y ? SW : NW;
		else
			return P.y < Y ? SE : NE;
	}

	Vector searchVector;

	/**
	 * Performs a recursive search in the PR Quadtree. This method is called
	 * internally to search for points within a specified area or for the nearest
	 * point to a given location.
	 *
	 * @param R     The current node in the PR Quadtree being searched.
	 * @param qu    The query object containing search criteria.
	 * @param block The bounding rectangle representing the area of the current
	 *              node.
	 * @param mode  The search mode.
	 * @param v     The vector where search results are accumulated.
	 */
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

	/**
	 * Inserts a point into the PR Quadtree. This method is abstract and should be
	 * implemented by subclasses to define the specific insertion behavior.
	 *
	 * @param p  The point to insert.
	 * @param R  The current node in the Quadtree.
	 * @param X  The x-coordinate of the node's center.
	 * @param Y  The y-coordinate of the node's center.
	 * @param lx The width of the node.
	 * @param ly The height of the node.
	 * @param md The maximum decomposition level.
	 * @param ok An array of boolean to indicate the success of the insertion.
	 * @return The modified node after insertion.
	 */
	abstract PRbucketNode insert(DPoint p, PRbucketNode R, double X, double Y, double lx, double ly, int md,
			boolean[] ok);

	/**
	 * Deletes a point from the PR Quadtree. This method is private and used
	 * internally to remove a point from the tree.
	 *
	 * @param P  The point to delete.
	 * @param R  The current node in the Quadtree.
	 * @param X  The x-coordinate of the node's center.
	 * @param Y  The y-coordinate of the node's center.
	 * @param lx The width of the node.
	 * @param ly The height of the node.
	 * @return The modified node after deletion.
	 */
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

	/**
	 * Draws the contents of the Quadtree. This method is private and used to
	 * recursively draw the nodes of the Quadtree onto a drawing target.
	 *
	 * @param r    The current node being drawn.
	 * @param g    The drawing target.
	 * @param minx The minimum x-coordinate of the drawing area.
	 * @param miny The minimum y-coordinate of the drawing area.
	 * @param wx   The width of the drawing area.
	 * @param wy   The height of the drawing area.
	 */
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
	/**
	 * Abstract class representing an element in a priority queue used for
	 * incremental nearest neighbor search in a PR Quadtree. This class serves as a
	 * base for different types of elements that can be enqueued in the search
	 * process, such as tree nodes or individual points. Each element is associated
	 * with a key value used to determine its priority in the queue.
	 */
	abstract class PRbucketQueueElement {
		// Key used for prioritizing this element in the queue
		double key;

		/**
		 * Constructs a new PRbucketQueueElement with a specified key. The key is used
		 * to determine the element's priority in the queue.
		 *
		 * @param k The key value associated with this queue element.
		 */
		PRbucketQueueElement(double k) {
			key = k;
		}
	}

	/**
	 * Represents a node in the incremental nearest neighbor search queue. Holds a
	 * reference to a PR Quadtree node and the corresponding rectangular block. This
	 * class is used to efficiently perform nearest neighbor searches in the
	 * Quadtree.
	 */
	class PRQINode extends PRbucketQueueElement {
		PRbucketNode r;
		DRectangle block;

		PRQINode(double k, PRbucketNode rr, DRectangle b) {
			super(k);
			r = rr;
			block = b;
		}
	}

	/**
	 * Represents a leaf in the incremental nearest neighbor search queue. Holds a
	 * DPoint object which is a candidate for the nearest neighbor search. This
	 * class is used in the process of finding the closest points in the Quadtree.
	 */
	class PRQLeaf extends PRbucketQueueElement {
		DPoint pnt;

		PRQLeaf(double k, DPoint p) {
			super(k);
			pnt = p;
		}
	}

	/**
	 * Represents a priority queue for the incremental nearest neighbor search in
	 * the PR Quadtree. This queue is used to efficiently process nodes and leaves
	 * during the search.
	 */
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

	/**
	 * Implements the incremental nearest neighbor search in the PR Quadtree. This
	 * class uses a priority queue to efficiently find the nearest neighbors to a
	 * given query point in the Quadtree.
	 */
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
