/**
 * The LOOSETree class represents a Loose Quadtree structure used for spatial indexing.
 * It implements the MaxDecompIface and LoosenessFactorIface interfaces.
 *
 * @version $Id: LOOSETree.java,v 1.1 2007/10/29 01:19:57 jagan Exp $
 */
package vasco.rectangles;

import vasco.common.*;
import java.util.*;
import javax.swing.*; // import java.awt.*;
import java.awt.event.*;
import vasco.drawable.*;

public class LOOSETree extends RectangleStructure implements MaxDecompIface, LoosenessFactorIface {

	int maxDecomp;
	double loosenessfactor;
	LOOSEcnode ROOT;

	/**
	 * Constructs a new LOOSETree with the specified parameters.
	 *
	 * @param can   The bounding rectangle for the tree.
	 * @param md    The maximum decomposition level.
	 * @param loose The looseness factor.
	 * @param p     The top-level interface.
	 * @param r     The rebuild tree.
	 */
	public LOOSETree(DRectangle can, int md, double loose, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
		maxDecomp = md;
		loosenessfactor = loose;
	}

	/**
	 * Clears the LOOSETree.
	 */
	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	/**
	 * Checks if the order of operations is dependent.
	 * 
	 * @return true if order-dependent, false otherwise
	 */
	public boolean orderDependent() {
		return false;
	}

	/**
	 * Reinitializes the object with the given choice.
	 * 
	 * @param c The choice object
	 */
	public void reInit(Choice c) {
//		System.out.println("c " + c.getItemCount());
		super.reInit(c);
//		System.out.println("after c " + c.getItemCount());
//		System.out.print("this is " + this);
		new MaxDecomp(topInterface, 9, this);
		new LoosenessFactor(topInterface, 2.0, this);
		availOps.addItem("Motion Insensitivity");
		availOps.addItem("Show Quadtree");
//		System.out.print("availOps is " + availOps);
	}

	/**
	 * Inserts a rectangle into the data structure.
	 * 
	 * @param P The rectangle to insert
	 * @return true if successful, false otherwise
	 */
	public boolean Insert(DRectangle P) {
		boolean[] res = new boolean[1];
		ROOT = insert(P, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width / 2, wholeCanvas.height / 2, maxDecomp, res);

		if (!res[0]) {
			delete(P, ROOT);
			ROOT = CompactTree(ROOT);
		}

		return res[0];
	}

	/**
	 * Replaces rectangles in the data structure.
	 * 
	 * @param OldRect The old rectangle to replace
	 * @param NewRect The new rectangle to replace with
	 * @return true if successful, false otherwise
	 */
	public boolean ReplaceRectangles(DRectangle OldRect, DRectangle NewRect) {

		if (ROOT == null)
			return false;

		boolean result = LOOSEReplaceRectangles(ROOT, OldRect, NewRect, wholeCanvas.x + wholeCanvas.width / 2,
				wholeCanvas.y + wholeCanvas.height / 2, wholeCanvas.width / 2, wholeCanvas.height / 2);
		ROOT = CompactTree(ROOT);

		return result;
	}

	/**
	 * Finds the enclosing quad block for a given rectangle.
	 * 
	 * @param OldRect   The rectangle
	 * @param nextLevel Whether to consider the next level
	 * @return The enclosing quad block
	 */
	public DRectangle EnclosingQuadBlock(DRectangle OldRect, boolean nextLevel) {

		if (ROOT == null)
			return null;

		return QuadBlockContaining(ROOT, OldRect, wholeCanvas.x + wholeCanvas.width / 2,
				wholeCanvas.y + wholeCanvas.height / 2, wholeCanvas.width / 2, wholeCanvas.height / 2, nextLevel);
	}

	/**
	 * Deletes a point from the data structure.
	 * 
	 * @param qu The point to delete
	 */
	public void Delete(DPoint qu) {
		if (ROOT == null)
			return;

		LOOSEIncNearest kdin = new LOOSEIncNearest(ROOT);
		DRectangle mx = kdin.Query(new QueryObject(qu));
		delete(mx, ROOT);
		ROOT = CompactTree(ROOT);
	}

	/**
	 * Deletes a drawable object directly.
	 * 
	 * @param d The drawable object to delete
	 */
	public void DeleteDirect(Drawable d) {
		if (ROOT == null)
			return;
		delete((DRectangle) d, ROOT);
		ROOT = CompactTree(ROOT);
	}

	/**
	 * Searches for objects that match the query.
	 * 
	 * @param r    The query object
	 * @param mode The search mode
	 * @return A search vector containing matching objects
	 */
	public SearchVector Search(QueryObject r, int mode) {
		SearchVector res = new SearchVector();
		searchVector = new Vector();
		findAll(r, ROOT, wholeCanvas, res, mode);
		return res;
	}

	/**
	 * Finds the nearest object to a query point.
	 * 
	 * @param p The query object
	 * @return A search vector containing the nearest object
	 */
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
			mxin.Query(p, v);
		}
		return v;
	}

	/**
	 * Finds objects within a specified distance of a query point.
	 * 
	 * @param p    The query object
	 * @param dist The maximum distance
	 * @return A search vector containing matching objects
	 */
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
			mxin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	/**
	 * Finds the nearest point using a LOOSE query.
	 * 
	 * @param qu The query point
	 * @return The nearest point
	 */
	public DPoint NearestMXLOOSE(DPoint qu) {
		if (ROOT == null)
			return null;
		LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
		return mxin.LOOSEQuery(qu).pnt;
	}

	/**
	 * Finds the nearest drawable object using a LOOSE query.
	 * 
	 * @param qu The query object
	 * @return The nearest drawable object
	 */
	public Drawable NearestFirst(QueryObject qu) {
		if (ROOT == null)
			return null;
		LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
		return mxin.Query(qu);
	}

	/**
	 * Finds drawable objects within a specified distance of a query object.
	 * 
	 * @param qu   The query object
	 * @param dist The maximum distance
	 * @return An array of drawable objects matching the query
	 */
	public Drawable[] NearestRange(QueryObject qu, double dist) {
		if (ROOT == null)
			return null;
		LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
		return mxin.Query(qu, dist);
	}

	/**
	 * Gets the name of the LOOSETree.
	 *
	 * @return The name of the tree.
	 */
	public String getName() {
		return "Loose Quadtree";
	}

	/**
	 * Draws the contents of the LOOSETree.
	 *
	 * @param g    The drawing target.
	 * @param view The view rectangle.
	 */
	public void drawContents(DrawingTarget g, Rectangle view) {
		drawC(ROOT, g, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width / 2, wholeCanvas.height / 2, view);
		drawR(ROOT, g, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width / 2, wholeCanvas.height / 2, view);
	}
	/* ---------------- interface implementation ---------- */

	/**
	 * Gets the looseness factor of the LOOSETree.
	 *
	 * @return The looseness factor.
	 */
	public double getLoosenessFactor() {
		return loosenessfactor;
	}

	/**
	 * Sets the looseness factor of the LOOSETree.
	 *
	 * @param b The new looseness factor to set.
	 */
	public void setLoosenessFactor(double b) {
		loosenessfactor = b;
		reb.rebuild();
	}

	/* ---------------- interface implementation ---------- */

	/**
	 * Gets the maximum decomposition level of the LOOSETree.
	 *
	 * @return The maximum decomposition level.
	 */
	public int getMaxDecomp() {
		return maxDecomp;
	}

	/**
	 * Sets the maximum decomposition level of the LOOSETree.
	 *
	 * @param b The new maximum decomposition level to set.
	 */
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

	// ----------------- private ----------------
	/**
	 * Represents a vector of DRectangles.
	 */
	class DRectVector {
		private Vector hiddenVector;

		/**
		 * Constructs a new DRectVector.
		 */
		DRectVector() {
			hiddenVector = new Vector();
		}

		/**
		 * Returns the size of the DRectVector.
		 * 
		 * @return The size of the vector
		 */
		public int size() {
			return hiddenVector.size();
		}

		/**
		 * Sets a DRectangle at the specified index.
		 * 
		 * @param r The DRectangle to set
		 * @param i The index at which to set the DRectangle
		 */
		public void set(DRectangle r, int i) {
			hiddenVector.setElementAt(r, i);
		}

		/**
		 * Appends a DRectangle to the end of the DRectVector.
		 * 
		 * @param d The DRectangle to append
		 */
		public void append(DRectangle d) {
			hiddenVector.addElement(d);
		}

		/**
		 * Gets a DRectangle at the specified index.
		 * 
		 * @param i The index of the DRectangle to get
		 * @return The DRectangle at the specified index
		 */
		public DRectangle get(int i) {
			return (DRectangle) hiddenVector.elementAt(i);
		}

		/**
		 * Removes a DRectangle at the specified index.
		 * 
		 * @param i The index of the DRectangle to remove
		 */
		public void remove(int i) {
			hiddenVector.removeElementAt(i);
		}
	}

	/**
	 * Constants representing different axes.
	 */
	static final int XAXIS = 0;
	static final int YAXIS = 1;
	static final int BOTH = 2;

	/**
	 * Arrays representing scaling factors for x and y coordinates.
	 */
	static final double xf[] = { -1, 1, -1, 1 };
	static final double yf[] = { 1, 1, -1, -1 };

	static final double vf[] = { -1, 1 };

	/**
	 * Returns the index of the other axis. If V is XAXIS, returns YAXIS, and vice versa.
	 *
	 * @param V The input axis (XAXIS or YAXIS).
	 * @return The index of the other axis.
	 */
	int OtherAxis(int V) {
		return 1 - V;
	}

	/**
	 * Returns the opposite direction index. If V is XAXIS, returns YAXIS, and vice versa.
	 *
	 * @param V The input direction (XAXIS or YAXIS).
	 * @return The opposite direction index.
	 */
	int OpDirection(int V) {
		return 1 - V;
	}

	/**
	 * Searches for the old Rectangle. If the enclosing quadtree block of the old
	 * rectangle can still contain the NewRect, switches OldRect for NewRect and
	 * returns true. No need to change the structure. Otherwise, returns false.
	 *
	 * @param T       The LOOSEcnode representing a node in the LOOSE data structure.
	 * @param OldRect The old rectangle to be replaced.
	 * @param NewRect The new rectangle to replace the old one.
	 * @param CX      The center x-coordinate of the enclosing quadtree block.
	 * @param CY      The center y-coordinate of the enclosing quadtree block.
	 * @param LX      The half-width of the enclosing quadtree block.
	 * @param LY      The half-height of the enclosing quadtree block.
	 * @return True if OldRect was replaced by NewRect, false otherwise.
	 */
	public boolean LOOSEReplaceRectangles(LOOSEcnode T, DRectangle OldRect, DRectangle NewRect, double CX, double CY,
			double LX, double LY) {

		if (T == null)
			return false;

		DRectangle Box = new DRectangle(CX - loosenessfactor * LX, CY - loosenessfactor * LY, 2 * loosenessfactor * LX,
				2 * loosenessfactor * LY);

		if (T.BIN[YAXIS] != null)
			for (int t = 0; t < T.BIN[YAXIS].rv.size(); t++) {
				if (OldRect.equals(T.BIN[YAXIS].rv.get(t))) {
					if (Box.contains(NewRect)) {
						T.BIN[YAXIS].rv.remove(t);
						T.BIN[YAXIS].rv.append(NewRect);
						return true;
					} else
						return false;
				}
			}

		for (int i = 0; i < T.NRDIRS; ++i) {
			if (T.SON[i] == null)
				continue;
			boolean t = LOOSEReplaceRectangles(T.SON[i], OldRect, NewRect, CX + xf[i] * LX / 2, CY + yf[i] * LY / 2,
					LX / 2, LY / 2);
			if (t)
				return t;
		}

		return false;
	}

	/**
	 * Deletes a rectangle represented by OldRect from the LOOSEcnode T.
	 *
	 * @param OldRect The rectangle to be deleted.
	 * @param T       The LOOSEcnode representing a node in the LOOSE data structure.
	 */
	public void delete(DRectangle OldRect, LOOSEcnode T) {

		if (T == null)
			return;

		if (T.BIN[YAXIS] != null)
			for (int t = 0; t < T.BIN[YAXIS].rv.size(); t++)
				if (OldRect.equals(T.BIN[YAXIS].rv.get(t)))
					T.BIN[YAXIS].rv.remove(t);

		for (int i = 0; i < T.NRDIRS; ++i) {
			if (T.SON[i] == null)
				continue;
			delete(OldRect, T.SON[i]);
		}

		return;
	}

	/**
	 * Expands the given DRectangle by the specified looseness factor.
	 *
	 * @param in The input DRectangle to expand
	 * @return A new DRectangle expanded by the looseness factor
	 */
	public DRectangle expand(DRectangle in) {
		return new DRectangle(in.x - (loosenessfactor - 1) * in.width / 2, in.y - (loosenessfactor - 1) * in.height / 2,
				loosenessfactor * in.width, loosenessfactor * in.height);
	}

	/**
	 * Finds the QuadBlock containing the specified DRectangle within the
	 * LOOSEcnode.
	 *
	 * @param T         The LOOSEcnode to search in
	 * @param OldRect   The DRectangle to find
	 * @param CX        The center X-coordinate of the current block
	 * @param CY        The center Y-coordinate of the current block
	 * @param LX        The width of the current block
	 * @param LY        The height of the current block
	 * @param nextLevel Indicates whether to consider the next level
	 * @return The DRectangle containing the OldRect or null if not found
	 */
	public DRectangle QuadBlockContaining(LOOSEcnode T, DRectangle OldRect, double CX, double CY, double LX, double LY,
			boolean nextLevel) {

		if (T == null)
			return null;
		boolean axes = false;

		DRectangle Box = new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY);

		if (T.BIN[YAXIS] != null)
			for (int t = 0; t < T.BIN[YAXIS].rv.size(); t++) {
				if (OldRect.equals(T.BIN[YAXIS].rv.get(t))) {

					if (!nextLevel)
						return Box; // If nextLevel is required?

					DPoint p = new DPoint(OldRect.x + OldRect.width / 2, OldRect.y + OldRect.height / 2);

					for (int i = 0; i < T.NRDIRS; ++i) {
						DRectangle tt = new DRectangle(CX + xf[i] * LX / 2 - LX / 2, CY + yf[i] * LY / 2 - LY / 2, LX,
								LY);
						if (tt.contains(p))
							return tt;
					}

					return null;
				}
			}

		for (int i = 0; i < T.NRDIRS; ++i) {
			if (T.SON[i] == null)
				continue;
			DRectangle t = QuadBlockContaining(T.SON[i], OldRect, CX + xf[i] * LX / 2, CY + yf[i] * LY / 2, LX / 2,
					LY / 2, nextLevel);
			if (t != null)
				return t;
		}

		return null;
	}

	/**
	 * Compacts the LOOSEcnode tree by removing empty nodes.
	 *
	 * @param T The root of the LOOSEcnode tree
	 * @return The compacted LOOSEcnode tree
	 */
	public LOOSEcnode CompactTree(LOOSEcnode T) {

		if (T == null)
			return T;

		boolean deleteIt = true;

		for (int i = 0; i < T.NRDIRS; ++i) {
			if (T.SON[i] == null)
				continue;
			T.SON[i] = CompactTree(T.SON[i]);
			if (T.SON[i] != null)
				deleteIt = false;
		}

		if (deleteIt & T.BIN[0] != null && T.BIN[0].rv.size() > 0)
			deleteIt = false;
		if (deleteIt & T.BIN[1] != null && T.BIN[1].rv.size() > 0)
			deleteIt = false;

		if (deleteIt)
			return null;
		else
			return T;

	}

	/**
	 * Compares a DRectangle with the given center coordinates.
	 *
	 * @param P  The DRectangle to compare
	 * @param CX The center X-coordinate
	 * @param CY The center Y-coordinate
	 * @return The comparison result (SW, NW, SE, or NE)
	 */
	int LOOSECompare(DRectangle P, double CX, double CY) {
		if (P.x + P.width / 2 < CX)
			return (P.y + P.height / 2 < CY) ? SW : NW;
		else
			return (P.y + P.height / 2 < CY) ? SE : NE;
	}

	/**
	 * Compares a DRectangle with a center value along a specified axis.
	 *
	 * @param P  The DRectangle to compare
	 * @param CV The center value
	 * @param V  The axis (XAXIS or YAXIS)
	 * @return The comparison result (BOTH or LEFT)
	 */
	int BINCompare(DRectangle P, double CV, int V) {
		if (V == XAXIS) {
			if (P.x <= CV && CV < P.x + P.width)
				return BOTH;
			else
				return LEFT;
		} else if (P.y <= CV && CV < P.y + P.height)
			return BOTH;
		else
			return LEFT;
	}

	/**
	 * Inserts a DRectangle into the LOOSEcnode tree.
	 *
	 * @param P   The DRectangle to insert
	 * @param R   The root of the LOOSEcnode tree
	 * @param CX  The center X-coordinate of the current block
	 * @param CY  The center Y-coordinate of the current block
	 * @param LX  The width of the current block
	 * @param LY  The height of the current block
	 * @param md  The maximum depth of insertion
	 * @param res An array to store the result of insertion
	 * @return The root of the updated LOOSEcnode tree
	 */
	LOOSEcnode insert(DRectangle P, LOOSEcnode R, double CX, double CY, double LX, double LY, int md, boolean[] res) {

		LOOSEcnode T;
		int Q;
		int DX, DY;

		if (R == null)
			R = new LOOSEcnode();
		T = R;
		DX = BINCompare(P, CX, XAXIS);
		DY = BINCompare(P, CY, YAXIS);
		DRectangle Box;

		double RR = (P.width > P.height) ? P.width : P.height;

		while (LX * loosenessfactor > RR) {

			Q = LOOSECompare(P, CX, CY);

			Box = new DRectangle(CX + xf[Q] * LX / 2 - loosenessfactor * LX / 2,
					CY + yf[Q] * LY / 2 - loosenessfactor * LY / 2, loosenessfactor * LX, loosenessfactor * LY);

			if (Box.contains(P)) {

				if (T.SON[Q] == null)
					T.SON[Q] = new LOOSEcnode();
				T = T.SON[Q];
				LX /= 2;
				CX += xf[Q] * LX;
				LY /= 2;
				CY += yf[Q] * LY;
				DX = BINCompare(P, CX, XAXIS);
				DY = BINCompare(P, CY, YAXIS);
				md--;

			} else {
				break;
			}
		}

		InsertAxis(P, T, CY, LY, YAXIS);
		res[0] = md > 0;
		return R;
	}

	/**
	 * Inserts a DRectangle into the specified LOOSEbnode.
	 *
	 * @param P  The DRectangle to insert
	 * @param R  The LOOSEcnode containing the LOOSEbnode
	 * @param CV The center value of the axis
	 * @param LV The length value of the axis
	 * @param V  The axis (XAXIS or YAXIS)
	 */
	void InsertAxis(DRectangle P, LOOSEcnode R, double CV, double LV, int V) {

		LOOSEbnode T;
		int D;

		T = R.BIN[V];
		if (T == null)
			T = R.BIN[V] = new LOOSEbnode();
		T.rv.append(P);
	}

	Vector searchVector;

	/**
	 * Finds all DRectangles within the specified search rectangle in the LOOSEcnode
	 * tree.
	 *
	 * @param searchRect The search rectangle
	 * @param R          The root of the LOOSEcnode tree
	 * @param block      The block represented by the LOOSEcnode
	 * @param v          The SearchVector to store the results
	 * @param mode       The search mode
	 */
	void findAll(QueryObject searchRect, LOOSEcnode R, DRectangle block, SearchVector v, int mode) {
		// searchRect ... external rectangle to be compared with others stored in the
		// quadtree
		// R ... root; CX, CY ... center of region of size LX, LY
		int Q;
		final double xf[] = { 0, 0.5, 0, 0.5 };
		final double yf[] = { 0.5, 0.5, 0, 0 };

		if (!searchRect.intersects(block))
			return;

		v.addElement(new SVElement(new YellowBlock(block, R == null), searchVector));

		if (R == null)
			return;

		// rectangleInOut(cur, searchRect.toRectangle(), mode, v, searchVector);

		CrossAll(searchRect, R.BIN[YAXIS], block.y + block.height / 2, block.height / 2, YAXIS, v, mode);
		CrossAll(searchRect, R.BIN[XAXIS], block.x + block.width / 2, block.width / 2, XAXIS, v, mode);

		for (Q = 3; Q >= 0; Q--) {
			DRectangle dr = new DRectangle(block.x + block.width * xf[Q], block.y + block.height * yf[Q],
					block.width / 2, block.height / 2);
			searchVector.addElement(dr);
		}
		for (Q = 0; Q < 4; Q++) {
			DRectangle dr = new DRectangle(block.x + block.width * xf[Q], block.y + block.height * yf[Q],
					block.width / 2, block.height / 2);
			searchVector.removeElementAt(searchVector.size() - 1);
			findAll(searchRect, R.SON[Q], dr, v, mode);
		}
	}

	/**
	 * Recursively searches for DRectangles within the specified axis in a
	 * LOOSEbnode.
	 *
	 * @param P    The search rectangle
	 * @param R    The LOOSEbnode
	 * @param CV   The center value of the axis
	 * @param LV   The length value of the axis
	 * @param V    The axis (XAXIS or YAXIS)
	 * @param v    The SearchVector to store the results
	 * @param mode The search mode
	 */
	void CrossAll(QueryObject P, LOOSEbnode R, double CV, double LV, int V, SearchVector v, int mode) {
		int D;
		if (R == null)
			return;
		for (int i = 0; i < R.rv.size(); i++) {
			drawableInOut(P, R.rv.get(i), mode, v, searchVector);
		}
		D = BINCompare(P.getBB(), CV, V);
		LV /= 2;
		if (D == BOTH) {
			CrossAll(P, R.SON[LEFT], CV - LV, LV, V, v, mode);
			CrossAll(P, R.SON[RIGHT], CV + LV, LV, V, v, mode);
		} else
			CrossAll(P, R.SON[D], CV + vf[D] * LV, LV, V, v, mode);
	}

	// ---- drawing routines
	/**
	 * Draws the rectangles stored in a LOOSEbnode using a drawing target.
	 *
	 * @param r The LOOSEbnode containing the rectangles to be drawn
	 * @param g The drawing target
	 */
	void drawRectangles(LOOSEbnode r, DrawingTarget g) {
		if (r == null)
			return;
		DRectVector t = r.rv;
		for (int i = 0; i < t.size(); i++)
			t.get(i).draw(g);
		drawRectangles(r.SON[0], g);
		drawRectangles(r.SON[1], g);
	}

	/**
	 * Recursively draws the rectangles stored in a LOOSEcnode tree.
	 *
	 * @param r    The root of the LOOSEcnode tree
	 * @param g    The drawing target
	 * @param CX   The center X-coordinate of the current block
	 * @param CY   The center Y-coordinate of the current block
	 * @param LX   The width of the current block
	 * @param LY   The height of the current block
	 * @param view The view rectangle for visibility check
	 */
	void drawR(LOOSEcnode r, DrawingTarget g, double CX, double CY, double LX, double LY, Rectangle view) {

		if (!g.visible(new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY)))
			return;

		if (r == null)
			return;

		for (int i = 0; i < r.NRDIRS; i++) {
			drawR(r.SON[i], g, CX + xf[i] * LX / 2, CY + yf[i] * LY / 2, LX / 2, LY / 2, view);
		}

		// Draw Rectangles in RED
		g.setColor(Color.red);
		drawRectangles(r.BIN[0], g);
		drawRectangles(r.BIN[1], g);
	}

	/**
	 * Draws the LOOSEcnode tree including axes if present, within the specified
	 * view rectangle.
	 *
	 * @param r    The root of the LOOSEcnode tree
	 * @param g    The drawing target
	 * @param CX   The center X-coordinate of the current block
	 * @param CY   The center Y-coordinate of the current block
	 * @param LX   The width of the current block
	 * @param LY   The height of the current block
	 * @param view The view rectangle for visibility check
	 */
	void drawC(LOOSEcnode r, DrawingTarget g, double CX, double CY, double LX, double LY, Rectangle view) {

		if (!g.visible(new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY)))
			return;

		if (r == null)
			return;

		boolean drawAxes = false;

		for (int i = 0; i < r.NRDIRS; i++) {

			if (r.SON[i] != null)
				drawAxes = true;

			drawC(r.SON[i], g, CX + xf[i] * LX / 2, CY + yf[i] * LY / 2, LX / 2, LY / 2, view);
		}

		if (drawAxes)
			for (int i = 0; i < r.NRDIRS; i++) {
				g.setColor(Color.black);
				g.drawRect(CX + xf[i] * LX / 2 - LX / 2, CY + yf[i] * LY / 2 - LY / 2, LX, LY);
			}
	}

	/**
	 * Draws the binary tree representing the LOOSEcnode tree within the specified
	 * view rectangle.
	 *
	 * @param p  The point in wholeCanvas coordinates
	 * @param dt The drawing target
	 */
	void drawBintree(DPoint p, DrawingTarget dt) {
		// p .. in wholeCanvas coordinates;
		// to locate the apropriate bintree find the nearest node in the quadtree to 'p'
		if (ROOT == null)
			return;
		LOOSEIncNearest kdin = new LOOSEIncNearest(ROOT);
		NearestINode mx = kdin.LOOSEQuery(p);

		dt.setColor(Color.blue);
		mx.pnt.draw(dt);

		new BinTreeFrame(mx.cnode, dt);
	}

	// ----------------------------------------------

	/**
	 * Inner class representing a frame for displaying the binary tree of
	 * LOOSEcnode.
	 */
	class BinTreeFrame extends Frame implements ActionListener {
		ScrollPane sp;
		Button close;

		/**
		 * Inner class representing a canvas for drawing the binary tree of LOOSEcnode.
		 */
		class BinTreeCanvas extends JPanel {
			LOOSEcnode cn;
			DrawingTarget dt;
			int bintreecounter;

			/**
			 * Constructor for the BinTreeCanvas class.
			 *
			 * @param c  The LOOSEcnode to draw
			 * @param w  The width of the canvas
			 * @param h  The height of the canvas
			 * @param dt The drawing target
			 */
			BinTreeCanvas(LOOSEcnode c, int w, int h, DrawingTarget dt) {
				setSize(w, h);
				cn = c;
				this.dt = dt;
				bintreecounter = 1;
				dt.setColor(Color.blue);
				drawBinRectangles(cn.BIN[0], dt);
				drawBinRectangles(cn.BIN[1], dt);
			}

			/**
			 * Paint method to draw the binary tree on the canvas.
			 *
			 * @param g The graphics object for drawing
			 */
			public void paint(Graphics g) {
				bintreecounter = 1;
				g.setColor(Color.blue);
				g.drawString("x axis bintree", 10, 10);
				g.drawString("y axis bintree", 10, getSize().height / 2);
				drawBinTree(cn.BIN[0], g, getSize().width / 2, 10, getDepth(cn.BIN[0], 0), 1.0);
				drawBinTree(cn.BIN[1], g, getSize().width / 2, getSize().height / 2, getDepth(cn.BIN[1], 0), 1.0);
			}

			/**
			 * Recursively draws rectangles within the binary tree represented by
			 * LOOSEbnode. This method traverses the left subtree, draws rectangles from the
			 * current node, and then traverses the right subtree.
			 *
			 * @param r  The root of the binary tree (LOOSEbnode)
			 * @param dt The drawing target
			 */
			void drawBinRectangles(LOOSEbnode r, DrawingTarget dt) {
				if (r == null)
					return;

				if (r.SON[0] != null) {
					drawBinRectangles(r.SON[0], dt);
				}
				for (int i = 0; i < r.rv.size(); i++) {
					DRectangle cr = r.rv.get(i);
					dt.drawString(String.valueOf(bintreecounter++), cr.x + cr.width / 2, cr.y + cr.height / 2,
							new Font("Helvetica", Font.PLAIN, 20));
				}
				if (r.SON[1] != null) {
					drawBinRectangles(r.SON[1], dt);
				}
			}

			/**
			 * Recursively draws the binary tree represented by LOOSEbnode within the
			 * specified Graphics object.
			 *
			 * @param r      The root of the binary tree (LOOSEbnode)
			 * @param g      The Graphics object for drawing
			 * @param curx   The current X-coordinate
			 * @param cury   The current Y-coordinate
			 * @param depth  The depth of the current node in the tree
			 * @param factor A scaling factor for drawing
			 */
			void drawBinTree(LOOSEbnode r, Graphics g, int curx, int cury, int depth, double factor) {
				if (r == null)
					return;

				if (r.SON[0] != null) {
					drawBinTree(r.SON[0], g, curx - (int) (10 * Math.pow(2, depth - 2) / factor), cury + 30, depth - 1,
							factor);
					g.drawLine(curx, cury, curx - (int) (10 * Math.pow(2, depth - 2) / factor), cury + 30);
				}
				if (r.rv.size() > 0) {
					String s = "";
					g.fillOval(curx - 3, cury - 3, 6, 6);
					for (int i = 0; i < r.rv.size(); i++) {
						s += String.valueOf(bintreecounter++) + "; ";
					}
					g.drawString(s, curx, cury - 3);
				} else {
					g.drawOval(curx - 3, cury - 3, 6, 6);
				}
				if (r.SON[1] != null) {
					drawBinTree(r.SON[1], g, curx + (int) (10 * Math.pow(2, depth - 2) / factor), cury + 30, depth - 1,
							factor);
					g.drawLine(curx, cury, curx + (int) (10 * Math.pow(2, depth - 2) / factor), cury + 30);
				}
			}
		}

		/**
		 * Recursively calculates the depth of a binary tree rooted at the specified
		 * LOOSEbnode.
		 *
		 * @param r   The root of the binary tree (LOOSEbnode)
		 * @param cur The current depth in the tree
		 * @return The maximum depth of the binary tree
		 */
		int getDepth(LOOSEbnode r, int cur) {
			if (r == null)
				return cur;
			return Math.max(getDepth(r.SON[0], cur + 1), getDepth(r.SON[1], cur + 1));
		}

		/**
		 * Calculates the total width of a binary tree at a given depth.
		 *
		 * @param depth The depth of the binary tree
		 * @return The total width of the binary tree at the specified depth
		 */
		int getTreeWidth(int depth) {
			return (int) (20 * (Math.pow(2, depth - 1) - 1));
		}

		/**
		 * Constructor for the BinTreeFrame class, which represents a frame for
		 * displaying the binary tree of LOOSEcnode.
		 *
		 * @param cn The LOOSEcnode to display in the binary tree
		 * @param dt The drawing target
		 */
		BinTreeFrame(LOOSEcnode cn, DrawingTarget dt) {
			super("Bintrees");
			setLayout(new BorderLayout());
			int width = Math.max(getTreeWidth(getDepth(cn.BIN[0], 0)), getTreeWidth(getDepth(cn.BIN[1], 0)));
			int height = 30 * (getDepth(cn.BIN[0], 0) + getDepth(cn.BIN[1], 0));

			BinTreeCanvas bt = new BinTreeCanvas(cn, width, height, dt);
			sp = new ScrollPane();
			sp.setSize(300, 300);
			add("Center", sp);
			sp.add(bt);
			close = new Button("Close");
			close.addActionListener(this);
			add("South", close);
			pack();
			show();
		}

		/**
		 * Action performed when the close button is clicked.
		 *
		 * @param e The ActionEvent
		 */
		public void actionPerformed(ActionEvent e) {
			Button src = (Button) e.getSource();
			if (src == close)
				dispose();
		}

	}

	// ---------------- inc nearest -----------------

	/**
	 * Represents an element in the LOOSE (Line Oriented Object Spatial Query
	 * Environment) queue.
	 */
	class LOOSEQueueElement {
		double[] keys;

		/**
		 * Constructs a LOOSEQueueElement with the specified keys.
		 *
		 * @param k The keys associated with the element.
		 */
		LOOSEQueueElement(double[] k) {
			keys = k;
		}
	}

	/**
	 * Represents a leaf element in the LOOSE queue with additional rectangle data.
	 */
	class LOOSEQLeaf extends LOOSEQueueElement {
		DRectangle rec;

		/**
		 * Constructs a LOOSEQLeaf with keys and a rectangle.
		 *
		 * @param k  The keys associated with the element.
		 * @param dr The rectangle data associated with the leaf.
		 */
		LOOSEQLeaf(double[] k, DRectangle dr) {
			super(k);
			rec = dr;
		}
	}

	/**
	 * Represents an internal node in the LOOSE queue with associated tree node and
	 * block data.
	 */
	class LOOSEQINode extends LOOSEQueueElement {
		LOOSEcnode r; // represents tree node
		DRectangle block;

		/**
		 * Constructs a LOOSEQINode with keys, a tree node, and block data.
		 *
		 * @param k The keys associated with the element.
		 * @param p The tree node represented by the internal node.
		 * @param b The block (rectangle) data associated with the internal node.
		 */
		LOOSEQINode(double[] k, LOOSEcnode p, DRectangle b) {
			super(k);
			r = p;
			block = b;
		}
	}

	/**
	 * Represents an element in the LOOSE queue used for nearest neighbor queries
	 * with additional node and point data.
	 */
	class NearestINode extends LOOSEQLeaf {
		LOOSEcnode cnode;
		DPoint pnt;

		/**
		 * Constructs a NearestINode with keys, a cnode (tree node), and a point.
		 *
		 * @param k  The keys associated with the element.
		 * @param cn The tree node (cnode) associated with the nearest neighbor.
		 * @param p  The point associated with the nearest neighbor.
		 */
		NearestINode(double[] k, LOOSEcnode cn, DPoint p) {
			super(k, null);
			cnode = cn;
			pnt = p;
		}
	}

	/**
	 * The LOOSEIncNearest class represents a component for performing incremental
	 * nearest neighbor queries on a LOOSE (Line Oriented Object Spatial Query
	 * Environment) data structure.
	 */
	class LOOSEIncNearest {

		/**
		 * The LOOSEQueue class represents a priority queue used in LOOSEIncNearest for
		 * managing elements.
		 */
		class LOOSEQueue {
			Vector v;

			/**
			 * Constructs an empty LOOSEQueue.
			 */
			LOOSEQueue() {
				v = new Vector();
			}

			/**
			 * Enqueues a LOOSEQueueElement into the queue.
			 *
			 * @param qe The LOOSEQueueElement to enqueue.
			 */
			void Enqueue(LOOSEQueueElement qe) {
				v.addElement(qe);
				for (int i = v.size() - 1; i > 0; i--) {
					LOOSEQueueElement q1 = (LOOSEQueueElement) v.elementAt(i - 1);
					LOOSEQueueElement q2 = (LOOSEQueueElement) v.elementAt(i);
					if (q1.keys[0] > q2.keys[0] || (q1.keys[0] == q2.keys[0]
							&& ((q1.keys[1] > q2.keys[1] && q1 instanceof LOOSEQLeaf && q2 instanceof LOOSEQLeaf)
									|| (q1 instanceof LOOSEQLeaf && !(q2 instanceof LOOSEQLeaf))))) {
						v.setElementAt(q2, i - 1);
						v.setElementAt(q1, i);
					}
				}
			}

			/**
			 * Retrieves the first element in the queue.
			 *
			 * @return The first LOOSEQueueElement in the queue.
			 */
			LOOSEQueueElement First() {
				LOOSEQueueElement q = (LOOSEQueueElement) v.elementAt(0);
				return q;
			}

			/**
			 * Deletes the first element in the queue.
			 */
			void DeleteFirst() {
				v.removeElementAt(0);
			}

			/**
			 * Dequeues and retrieves the first element in the queue.
			 *
			 * @return The first LOOSEQueueElement in the queue.
			 */
			LOOSEQueueElement Dequeue() {
				LOOSEQueueElement q = (LOOSEQueueElement) v.elementAt(0);
				v.removeElementAt(0);
				return q;
			}

			/**
			 * Enqueues elements from a binary tree into the queue.
			 *
			 * @param qu The QueryObject used for distance calculations.
			 * @param r  The root of the binary tree to enqueue.
			 */
			void EnqueueBinTree(QueryObject qu, LOOSEbnode r) {
				if (r == null)
					return;
				for (int i = 0; i < r.rv.size(); i++) {
					double[] dist = new double[2];
					qu.distance(r.rv.get(i), dist);
					Enqueue(new LOOSEQLeaf(dist, r.rv.get(i)));
				}
				for (int i = 0; i < 2; i++)
					EnqueueBinTree(qu, r.SON[i]);
			}

			/**
			 * Checks if the queue is empty.
			 *
			 * @return true if the queue is empty, false otherwise.
			 */
			boolean isEmpty() {
				return (v.size() == 0);
			}

			/**
			 * Creates a vector from the queue elements for visualization.
			 *
			 * @return A Vector containing elements suitable for visualization.
			 */
			Vector makeVector() {
				Vector r = new Vector();
				for (int i = 0; i < v.size(); i++) {
					LOOSEQueueElement q = (LOOSEQueueElement) v.elementAt(i);
					if (q instanceof LOOSEQLeaf)
						r.addElement(new GreenRect(((LOOSEQLeaf) q).rec));
					else
						r.addElement(new QueueBlock(expand(((LOOSEQINode) q).block)));
				}
				return r;
			}
		}

		LOOSEQueue q;

		/**
		 * Constructs a LOOSEIncNearest instance with the specified root node.
		 *
		 * @param rt The root node of the LOOSE data structure.
		 */
		LOOSEIncNearest(LOOSEcnode rt) {
			q = new LOOSEQueue();
			double[] dist = new double[2];
			dist[0] = dist[1] = 0.0;
			q.Enqueue(new LOOSEQINode(dist, rt, wholeCanvas));
		}

		/**
		 * Performs a nearest neighbor query using the specified QueryObject.
		 *
		 * @param qu The QueryObject used for the nearest neighbor query.
		 * @return The nearest rectangle to the query point.
		 */
		DRectangle Query(QueryObject qu) {
			DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
			return (ar.length == 0) ? null : ar[0];
		}

		/**
		 * Performs a query using the specified QueryObject and SearchVector.
		 *
		 * @param qu The QueryObject used for the query.
		 * @param v  The SearchVector to populate with query results.
		 */
		void Query(QueryObject qu, SearchVector v) {
			Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
		}

		/**
		 * Performs a query using the specified QueryObject and distance threshold.
		 *
		 * @param qu   The QueryObject used for the query.
		 * @param dist The maximum distance threshold for the query.
		 * @return An array of rectangles that satisfy the query conditions.
		 */
		DRectangle[] Query(QueryObject qu, double dist) {
			return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
		}

		/**
		 * Expands a rectangle by a looseness factor.
		 *
		 * @param in The input rectangle.
		 * @return The expanded rectangle.
		 */
		DRectangle expand(DRectangle in) {
			return new DRectangle(in.x - (loosenessfactor - 1) * in.width / 2,
					in.y - (loosenessfactor - 1) * in.height / 2, loosenessfactor * in.width,
					loosenessfactor * in.height);
		}

		/**
		 * Performs a query using the specified QueryObject, SearchVector, distance
		 * threshold, and maximum number of elements.
		 *
		 * @param qu      The QueryObject used for the query.
		 * @param ret     The SearchVector to populate with query results.
		 * @param dist    The maximum distance threshold for the query.
		 * @param nrelems The maximum number of elements to retrieve.
		 * @return An array of rectangles that satisfy the query conditions.
		 */
		DRectangle[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {

			Vector rect = new Vector();
			final double xf[] = { 0, .5, 0, .5 };
			final double yf[] = { .5, .5, 0, 0 };
			int counter = 1;

			while (!q.isEmpty()) {
				LOOSEQueueElement element = q.Dequeue();
				if (element instanceof LOOSEQLeaf) {
					LOOSEQLeaf ql = (LOOSEQLeaf) element;
					if (nrelems-- <= 0 || qu.distance(ql.rec) > dist)
						break;
					rect.addElement(ql.rec);
					ret.addElement(new NNElement(new NNDrawable(ql.rec, counter++), ql.keys[0], q.makeVector()));
				} else {
					LOOSEQINode in = (LOOSEQINode) element;
					ret.addElement(new NNElement(new YellowBlock(expand(in.block), false), in.keys[0], q.makeVector()));
					q.EnqueueBinTree(qu, in.r.BIN[0]);
					q.EnqueueBinTree(qu, in.r.BIN[1]);

					for (int i = 0; i < 4; i++)
						if (in.r.SON[i] != null) {
							DRectangle dr1 = new DRectangle(in.block.x + xf[i] * in.block.width,
									in.block.y + yf[i] * in.block.height, in.block.width / 2, in.block.height / 2);
							DRectangle dr2 = new DRectangle(
									in.block.x + xf[i] * in.block.width - (loosenessfactor - 1) * in.block.width / 4,
									in.block.y + yf[i] * in.block.height - (loosenessfactor - 1) * in.block.height / 4,
									loosenessfactor * in.block.width / 2, loosenessfactor * in.block.height / 2);
							double[] keys = new double[2];
							qu.distance(dr2, keys);
							q.Enqueue(new LOOSEQINode(keys, in.r.SON[i], dr1));
						}
				}
			}
			DRectangle[] ar = new DRectangle[rect.size()];
			rect.copyInto(ar);
			return ar;
		}

		/**
		 * Performs a nearest neighbor query using the specified QueryObject and query
		 * point.
		 *
		 * @param qu The QueryObject used for the query.
		 * @param dp The query point as a DPoint.
		 * @return The nearest NearestINode to the query point, which is a part of the
		 *         LOOSE data structure.
		 */
		NearestINode LOOSEQuery(DPoint qu) {

			/*
			 * Notice how the INN algorithm has to be changed in order to work on
			 * LooseQuadtrees
			 */

			final double xf[] = { 0, .5, 0, .5 };
			final double yf[] = { .5, .5, 0, 0 };

			while (!q.isEmpty()) {
				LOOSEQueueElement element = q.Dequeue();

				if (element instanceof NearestINode) {
					return (NearestINode) element;
				} else {
					LOOSEQINode in = (LOOSEQINode) element;
					double[] lkeys = new double[2];
					DPoint dp = new DPoint(in.block.x + in.block.width / 2, in.block.y + in.block.height / 2);
					qu.distance(dp, lkeys);
					q.Enqueue(new NearestINode(lkeys, in.r, dp));
					for (int i = 0; i < 4; i++)
						if (in.r.SON[i] != null) {
							DRectangle dr1 = new DRectangle(in.block.x + xf[i] * in.block.width,
									in.block.y + yf[i] * in.block.height, in.block.width / 2, in.block.height / 2);
							DRectangle dr2 = new DRectangle(
									in.block.x + xf[i] * in.block.width - (loosenessfactor - 1) * in.block.width / 4,
									in.block.y + yf[i] * in.block.height - (loosenessfactor - 1) * in.block.height / 4,
									loosenessfactor * in.block.width / 2, loosenessfactor * in.block.height / 2);
							double[] keys = new double[2];
							qu.distance(dr2, keys);
							q.Enqueue(new LOOSEQINode(keys, in.r.SON[i], dr1));
						}
				}
			}
			return null;

		}

	}

	/**
	 * The LOOSEbnode class represents a node in the LOOSE data structure, specifically used in the binary tree.
	 */
	class LOOSEbnode {
		LOOSEbnode[] SON;
		DRectVector rv;

	    /**
	     * Constructs an empty LOOSEbnode.
	     */
		LOOSEbnode() {
			SON = new LOOSEbnode[2];
			SON[0] = SON[1] = null;
			rv = new DRectVector();
		}
	}

	/**
	 * The LOOSEcnode class represents a node in the LOOSE data structure, specifically used in the quadtree.
	 */
	class LOOSEcnode {
		final int NRDIRS = 4;

		LOOSEbnode BIN[] = new LOOSEbnode[2]; // indexed by 'x' and 'y' coordinates
		LOOSEcnode SON[] = new LOOSEcnode[NRDIRS];

	    /**
	     * Constructs an empty LOOSEcnode.
	     */
		LOOSEcnode() {
			BIN[0] = BIN[1] = null;
			SON[0] = SON[1] = SON[2] = SON[3] = null;
		}
	}
}
