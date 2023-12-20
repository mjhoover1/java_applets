package vasco.rectangles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/* $Id: CIFTree.java,v 1.3 2003/09/05 16:33:12 brabec Exp $ */
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
 * CIFTree is an implementation of a quadtree structure for managing rectangular
 * objects. It supports operations like insertion, deletion, searching, and
 * nearest neighbor queries.
 */
public class CIFTree extends RectangleStructure implements MaxDecompIface {

	int maxDecomp; // Maximum decomposition level of the tree
	CIFcnode ROOT; // Root node of the CIF quadtree

	/**
	 * Constructor for creating a CIFTree instance.
	 *
	 * @param can The canvas or area within which the rectangles are managed.
	 * @param md  Maximum decomposition level for the tree.
	 * @param p   Interface for callback on tree operations.
	 * @param r   Instance of RebuildTree for managing tree rebuilds.
	 */
	public CIFTree(DRectangle can, int md, TopInterface p, RebuildTree r) {
		super(can, p, r);
		ROOT = null;
		maxDecomp = md;
	}

	/**
	 * Clears the CIFTree, removing all rectangles.
	 */
	@Override
	public void Clear() {
		super.Clear();
		ROOT = null;
	}

	/**
	 * Checks if the insertion order of rectangles affects the tree structure.
	 *
	 * @return boolean indicating if the tree is order dependent.
	 */
	@Override
	public boolean orderDependent() {
		return false;
	}

	/**
	 * Reinitializes the CIFTree with a new choice of rectangles.
	 *
	 * @param c Choice of rectangles to initialize the tree.
	 */
	@Override
	public void reInit(JComboBox<String> c) {
		super.reInit(c);
		new MaxDecomp(topInterface, 9, this);
//		JComboBox<String> availOps = c;
		addItemIfNotExists(availOps, "Show bintree");
	}

	/**
	 * Inserts a rectangle into the CIFTree.
	 *
	 * @param P The rectangle to be inserted.
	 * @return boolean indicating if the insertion was successful.
	 */
	@Override
	public boolean Insert(DRectangle P) {
		boolean[] res = new boolean[1];
		ROOT = insert(P, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width / 2, wholeCanvas.height / 2, maxDecomp, res);
		if (!res[0])
			ROOT = delete(P, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
					wholeCanvas.width / 2, wholeCanvas.height / 2);
		return res[0];
	}

	/**
	 * Deletes a rectangle from the CIFTree based on a query point.
	 *
	 * @param qu The query point used to determine the rectangle to delete.
	 */
	@Override
	public void Delete(DPoint qu) {
		if (ROOT == null)
			return;

		CIFIncNearest kdin = new CIFIncNearest(ROOT);
		DRectangle mx = kdin.Query(new QueryObject(qu));
		ROOT = delete(mx, ROOT, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width / 2, wholeCanvas.height / 2);
	}

	/**
	 * Directly deletes a specified rectangle from the CIFTree.
	 *
	 * @param d The rectangle to be deleted.
	 */
	@Override
	public void DeleteDirect(Drawable d) {
		if (ROOT == null)
			return;
		ROOT = delete((DRectangle) d, ROOT, wholeCanvas.x + wholeCanvas.width / 2,
				wholeCanvas.y + wholeCanvas.height / 2, wholeCanvas.width / 2, wholeCanvas.height / 2);
	}

	/**
	 * Searches the CIFTree for rectangles intersecting with a given query object.
	 *
	 * @param r    The query object defining the search area.
	 * @param mode The search mode (e.g., intersect, contain).
	 * @return SearchVector containing search results.
	 */
	@Override
	public SearchVector Search(QueryObject r, int mode) {
		SearchVector res = new SearchVector();
		searchVector = new Vector();
		findAll(r, ROOT, wholeCanvas, res, mode);
		return res;
	}

	/**
	 * Finds the nearest rectangle(s) to a given query point.
	 *
	 * @param p The query point.
	 * @return SearchVector containing the nearest rectangle(s).
	 */
	@Override
	public SearchVector Nearest(QueryObject p) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			CIFIncNearest mxin = new CIFIncNearest(ROOT);
			mxin.Query(p, v);
		}
		return v;
	}

	/**
	 * Finds the nearest rectangle(s) to a query point within a specified distance.
	 *
	 * @param p    The query point.
	 * @param dist The maximum distance within which to search.
	 * @return SearchVector containing the nearest rectangle(s).
	 */
	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		SearchVector v = new SearchVector();
		if (ROOT != null) {
			CIFIncNearest mxin = new CIFIncNearest(ROOT);
			mxin.Query(p, v, dist, Integer.MAX_VALUE);
		}
		return v;
	}

	/**
	 * Finds the nearest point in the CIFTree to a given query point.
	 *
	 * @param qu The query point.
	 * @return The nearest point in the CIFTree.
	 */
	public DPoint NearestMXCIF(DPoint qu) {
		if (ROOT == null)
			return null;
		CIFIncNearest mxin = new CIFIncNearest(ROOT);
		return mxin.CIFQuery(qu).pnt;
	}

	/**
	 * Retrieves the first rectangle nearest to a given query object.
	 *
	 * @param qu The query object.
	 * @return The nearest rectangle.
	 */
	@Override
	public Drawable NearestFirst(QueryObject qu) {
		if (ROOT == null)
			return null;
		CIFIncNearest mxin = new CIFIncNearest(ROOT);
		return mxin.Query(qu);
	}

	/**
	 * Retrieves all rectangles within a certain range of a query object.
	 *
	 * @param qu   The query object.
	 * @param dist The search range distance.
	 * @return Array of rectangles within the specified range.
	 */
	@Override
	public Drawable[] NearestRange(QueryObject qu, double dist) {
		if (ROOT == null)
			return null;
		CIFIncNearest mxin = new CIFIncNearest(ROOT);
		return mxin.Query(qu, dist);
	}

	/**
	 * Returns the name of the tree structure.
	 *
	 * @return A string representing the name of the structure.
	 */
	@Override
	public String getName() {
		return "MX-CIF Quadtree";
	}

	/**
	 * Draws the contents of the CIFTree on a given drawing target within a
	 * specified view.
	 *
	 * @param g    The drawing target.
	 * @param view The view rectangle within which to draw.
	 */
	@Override
	public void drawContents(DrawingTarget g, Rectangle view) {
		drawC(ROOT, g, wholeCanvas.x + wholeCanvas.width / 2, wholeCanvas.y + wholeCanvas.height / 2,
				wholeCanvas.width / 2, wholeCanvas.height / 2, view);
	}

	/* ---------------- interface implementation ---------- */

	/**
	 * Gets the maximum decomposition level of the quadtree.
	 *
	 * @return The maximum decomposition level.
	 */
	@Override
	public int getMaxDecomp() {
		return maxDecomp;
	}

	/**
	 * Sets the maximum decomposition level of the quadtree and rebuilds the tree.
	 *
	 * @param b The new maximum decomposition level.
	 */
	@Override
	public void setMaxDecomp(int b) {
		maxDecomp = b;
		reb.rebuild();
	}

	// ----------------- private ----------------
	// Inner class representing a vector of rectangles
	class DRectVector {
		private Vector hiddenVector; // Internal vector storing the rectangles

		DRectVector() {
			hiddenVector = new Vector();
		}

		public int size() {
			return hiddenVector.size();
		}

		public void set(DRectangle r, int i) {
			hiddenVector.setElementAt(r, i);
		}

		public void append(DRectangle d) {
			hiddenVector.addElement(d);
		}

		public DRectangle get(int i) {
			return (DRectangle) hiddenVector.elementAt(i);
		}

		public void remove(int i) {
			hiddenVector.removeElementAt(i);
		}
	}

	// Constants representing axes and quadrant types
	static final int XAXIS = 0;
	static final int YAXIS = 1;
	static final int BOTH = 2;

	static final double xf[] = { -1, 1, -1, 1 };
	static final double yf[] = { 1, 1, -1, -1 };

	static final double vf[] = { -1, 1 };

	// Helper methods for tree manipulation
	int OtherAxis(int V) {
		return 1 - V;
	}

	int OpDirection(int V) {
		return 1 - V;
	}

	// Implementation details for comparing rectangles with a given point
	int CIFCompare(DRectangle P, double CX, double CY) {
		if (P.x + P.width / 2 < CX)
			return (P.y + P.height / 2 < CY) ? SW : NW;
		else
			return (P.y + P.height / 2 < CY) ? SE : NE;
	}

	// Implementation details for binary comparison of rectangles with a value
	int BINCompare(DRectangle P, double CV, int V) {
		if (V == XAXIS) {
			if (P.x <= CV && CV < P.x + P.width)
				return BOTH;
			else
				return (CV < P.x + P.width / 2) ? RIGHT : LEFT;
		} else if (P.y <= CV && CV < P.y + P.height)
			return BOTH;
		else
			return (CV < P.y + P.height / 2) ? RIGHT : LEFT;
	}

	// Recursive method for inserting a rectangle into the quadtree
	CIFcnode insert(DRectangle P, CIFcnode R, double CX, double CY, double LX, double LY, int md, boolean[] res) {
		CIFcnode T;
		int Q;
		int DX, DY;

		if (R == null)
			R = new CIFcnode();
		T = R;
		DX = BINCompare(P, CX, XAXIS);
		DY = BINCompare(P, CY, YAXIS);

		while (DX != BOTH && DY != BOTH) {
			Q = CIFCompare(P, CX, CY);
			if (T.SON[Q] == null)
				T.SON[Q] = new CIFcnode();
			T = T.SON[Q];
			LX /= 2;
			CX += xf[Q] * LX;
			LY /= 2;
			CY += yf[Q] * LY;
			DX = BINCompare(P, CX, XAXIS);
			DY = BINCompare(P, CY, YAXIS);
			md--;
		}
		if (DX == BOTH)
			InsertAxis(P, T, CY, LY, YAXIS);
		else
			InsertAxis(P, T, CX, LX, XAXIS);

		res[0] = md > 0;
		return R;
	}

	// Helper method for inserting a rectangle along a specific axis
	void InsertAxis(DRectangle P, CIFcnode R, double CV, double LV, int V) {

		CIFbnode T;
		int D;

		T = R.BIN[V];
		if (T == null)
			T = R.BIN[V] = new CIFbnode();
		D = BINCompare(P, CV, V);
		while (D != BOTH) {
			if (T.SON[D] == null)
				T.SON[D] = new CIFbnode();
			T = T.SON[D];
			LV /= 2;
			CV += vf[D] * LV;
			D = BINCompare(P, CV, V);
		}
		T.rv.append(P);
	}

	// Recursive method for deleting a rectangle from the quadtree
	CIFcnode delete(DRectangle P, CIFcnode R, double CX, double CY, double LX, double LY) {
		CIFcnode T, FT, RB, tempc;
		CIFbnode B, FB, tempb, TB;
		int Q, QF = -1;
		int D, DF = -1;
		int V;
		double CV, LV;

		if (R == null)
			return null;
		T = R;
		FT = null;
		while (BINCompare(P, CX, V = XAXIS) != BOTH && BINCompare(P, CY, V = YAXIS) != BOTH) {
			Q = CIFCompare(P, CX, CY);
			if (T.SON[Q] == null)
				return R;
			if (T.SON[CQuad(Q)] != null || T.SON[OpQuad(Q)] != null || T.SON[CCQuad(Q)] != null || T.BIN[XAXIS] != null
					|| T.BIN[YAXIS] != null) {
				FT = T;
				QF = Q;
			}
			T = T.SON[Q];
			LX /= 2;
			CX += xf[Q] * LX;
			LY /= 2;
			CY += yf[Q] * LY;
		}
		V = OtherAxis(V);
		RB = T;
		FB = null;
		B = T.BIN[V];
		CV = V == XAXIS ? CX : CY;
		LV = V == XAXIS ? LX : LY;
		D = BINCompare(P, CV, V);
		while (B != null && D != BOTH) {
			if (B.SON[OpDirection(D)] != null || B.rv.size() != 0) {
				FB = B;
				DF = D;
			}
			B = B.SON[D];
			LV /= 2;
			CV += vf[D] * LV;
			D = BINCompare(P, CV, V);
		}
		if (B == null) // no rectangle at all
			return R;

		del_loop: {
			for (int t = 0; t < B.rv.size(); t++) {
				if (P.equals(B.rv.get(t))) {
					B.rv.remove(t);
					break del_loop;
				}
			}
			return R;
		}

		if (B.rv.size() > 0)
			return R; // elements left in the list
		else if (B.SON[LEFT] == null && B.SON[RIGHT] == null) {
			TB = FB == null ? RB.BIN[V] : FB.SON[DF];
			D = LEFT;
			while (TB != B) {
				if (TB.SON[D] == null)
					D = OpDirection(D);
				tempb = TB.SON[D];
				TB.SON[D] = null;
				TB = tempb;
			}
			if (FB != null)
				FB.SON[DF] = null;
			else {
				RB.BIN[V] = null;
				if (RB.BIN[OtherAxis(V)] != null || RB.SON[NW] != null || RB.SON[NE] != null || RB.SON[SW] != null
						|| RB.SON[SE] != null)
					return R;
				T = FT == null ? R : FT.SON[QF];
				while (T != null) {
					for (Q = 0; Q < 4; Q++) {
						if (T.SON[Q] != null)
							break;
					}
					if (Q < 4) {
						tempc = T.SON[Q];
						T.SON[Q] = null;
						T = tempc;
					} else {
						T = null;
						// System.err.println("WARNING: maybe shouldn't run through here");
					}
				}
				if (FT == null)
					R = null;
				else
					FT.SON[QF] = null;
			}
		}
		return R;
	}

	Vector searchVector; // Vector used during search operations

	// Method for finding all rectangles that meet certain criteria
	void findAll(QueryObject searchRect, CIFcnode R, DRectangle block, SearchVector v, int mode) {
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

	// Helper method for crossing all rectangles in a node
	void CrossAll(QueryObject P, CIFbnode R, double CV, double LV, int V, SearchVector v, int mode) {
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
	 * Draws all rectangles stored in a given CIF binary node.
	 *
	 * @param r The binary node whose rectangles are to be drawn.
	 * @param g The drawing target to render the rectangles on.
	 */
	void drawRectangles(CIFbnode r, DrawingTarget g) {
		if (r == null)
			return;
		DRectVector t = r.rv;
		for (int i = 0; i < t.size(); i++)
			t.get(i).draw(g);
		drawRectangles(r.SON[0], g);
		drawRectangles(r.SON[1], g);
	}

	/**
	 * Recursively draws the CIF tree structure, starting from a given node.
	 *
	 * @param r    The CIF node to start drawing from.
	 * @param g    The drawing target.
	 * @param CX   The x-coordinate of the center of the area represented by the
	 *             node.
	 * @param CY   The y-coordinate of the center of the area represented by the
	 *             node.
	 * @param LX   Half the width of the area represented by the node.
	 * @param LY   Half the height of the area represented by the node.
	 * @param view The viewable area for rendering (used to optimize drawing).
	 */
	void drawC(CIFcnode r, DrawingTarget g, double CX, double CY, double LX, double LY, Rectangle view) {
		if (!g.visible(new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY)))
			return;

		g.setColor(Color.black);
		g.drawRect(CX - LX, CY - LY, 2 * LX, 2 * LY);

		if (r == null)
			return;

		g.setColor(Color.red);
		drawRectangles(r.BIN[0], g);
		drawRectangles(r.BIN[1], g);

		for (int i = 0; i < r.NRDIRS; i++) {
			drawC(r.SON[i], g, CX + xf[i] * LX / 2, CY + yf[i] * LY / 2, LX / 2, LY / 2, view);
		}
	}

	/**
	 * Draws the binary tree structure within the CIF tree, for a specific point in
	 * the canvas.
	 *
	 * @param p  The point in the canvas coordinates.
	 * @param dt The drawing target.
	 */
	void drawBintree(DPoint p, DrawingTarget dt) {
		// p .. in wholeCanvas coordinates;
		// to locate the apropriate bintree find the nearest node in the quadtree to 'p'
		if (ROOT == null)
			return;
		CIFIncNearest kdin = new CIFIncNearest(ROOT);
		NearestINode mx = kdin.CIFQuery(p);

		dt.setColor(Color.blue);
		mx.pnt.draw(dt);

		new BinTreeFrame(mx.cnode, dt);
	}

	// ----------------------------------------------
	/**
	 * The `BinTreeFrame` class extends Frame and is responsible for displaying the
	 * binary trees within the CIF tree structure. It includes interactive elements
	 * to visualize and navigate through the binary tree representations.
	 */
	class BinTreeFrame extends JFrame implements ActionListener {
		// Implementation of the frame for displaying binary trees
		JScrollPane sp;
		JButton close;

		/**
		 * The `BinTreeCanvas` inner class extends Canvas and is used to draw the binary
		 * tree structure of a CIF tree node. It displays both x and y axis binary
		 * trees.
		 */
		class BinTreeCanvas extends JPanel {
			CIFcnode cn;
			DrawingTarget dt;
			int bintreecounter;

			/**
			 * Constructs a BinTreeCanvas instance.
			 *
			 * @param c  The CIFcnode representing the root of the binary tree to be
			 *           displayed.
			 * @param w  The width of the canvas.
			 * @param h  The height of the canvas.
			 * @param dt The drawing target for rendering the binary tree.
			 */
			BinTreeCanvas(CIFcnode c, int w, int h, DrawingTarget dt) {
				setPreferredSize(new Dimension(w, h)); // setSize(w, h);
				cn = c;
				this.dt = dt;
				bintreecounter = 1;
				dt.setColor(Color.blue);
				drawBinRectangles(cn.BIN[0], dt);
				drawBinRectangles(cn.BIN[1], dt);
			}
			
			/**
			 * Paints the binary tree on the canvas.
			 *
			 * @param g The graphics context used for drawing.
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				bintreecounter = 1;
				g.setColor(Color.blue);
				g.drawString("x axis bintree", 10, 10);
				g.drawString("y axis bintree", 10, getSize().height / 2);
				drawBinTree(cn.BIN[0], g, getSize().width / 2, 10, getDepth(cn.BIN[0], 0), 1.0);
				drawBinTree(cn.BIN[1], g, getSize().width / 2, getSize().height / 2, getDepth(cn.BIN[1], 0), 1.0);
			}

//			/**
//			 * Paints the binary tree on the canvas.
//			 *
//			 * @param g The graphics context used for drawing.
//			 */
//			@Override
//			public void paint(Graphics g) {
//				bintreecounter = 1;
//				g.setColor(Color.blue);
//				g.drawString("x axis bintree", 10, 10);
//				g.drawString("y axis bintree", 10, getSize().height / 2);
//				drawBinTree(cn.BIN[0], g, getSize().width / 2, 10, getDepth(cn.BIN[0], 0), 1.0);
//				drawBinTree(cn.BIN[1], g, getSize().width / 2, getSize().height / 2, getDepth(cn.BIN[1], 0), 1.0);
//			}

			/**
			 * Recursively draws the rectangles contained in the binary nodes.
			 *
			 * @param r  The current binary node to draw rectangles from.
			 * @param dt The drawing target.
			 */
			void drawBinRectangles(CIFbnode r, DrawingTarget dt) {
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
			 * Recursively draws the binary tree structure.
			 *
			 * @param r      The root node of the binary tree.
			 * @param g      The graphics context.
			 * @param curx   The current x position in the canvas.
			 * @param cury   The current y position in the canvas.
			 * @param depth  The depth of the node in the tree.
			 * @param factor A scaling factor for the drawing.
			 */
			void drawBinTree(CIFbnode r, Graphics g, int curx, int cury, int depth, double factor) {
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
		 * Calculates the depth of a binary tree.
		 *
		 * @param r   The root node of the binary tree.
		 * @param cur The current depth (initially 0).
		 * @return The maximum depth of the tree.
		 */
		int getDepth(CIFbnode r, int cur) {
			if (r == null)
				return cur;
			return Math.max(getDepth(r.SON[0], cur + 1), getDepth(r.SON[1], cur + 1));
		}

		/**
		 * Calculates the required width to display a binary tree of a given depth.
		 *
		 * @param depth The depth of the binary tree.
		 * @return The width needed to display the tree.
		 */
		int getTreeWidth(int depth) {
			return (int) (20 * (Math.pow(2, depth - 1) - 1));
		}

		/**
		 * Constructor for BinTreeFrame. Initializes and displays the frame for binary
		 * tree visualization.
		 *
		 * @param cn The CIFcnode whose binary trees are to be visualized.
		 * @param dt The drawing target for rendering.
		 */
		BinTreeFrame(CIFcnode cn, DrawingTarget dt) {
			super("Bintrees");
			setLayout(new BorderLayout());
			int width = Math.max(getTreeWidth(getDepth(cn.BIN[0], 0)), getTreeWidth(getDepth(cn.BIN[1], 0)));
			int height = 30 * (getDepth(cn.BIN[0], 0) + getDepth(cn.BIN[1], 0));

			BinTreeCanvas bt = new BinTreeCanvas(cn, width, height, dt);
			sp = new JScrollPane();
			sp.setPreferredSize(new Dimension(300, 300));  // sp.setSize(300, 300);
			add("Center", sp);
			sp.setViewportView(bt);
//			sp.add(bt);
			close = new JButton("Close");
			close.addActionListener(this);
			add("South", close);
			pack();
			setVisible(true); // show();
		}

		/**
		 * Handles action events for the frame, such as closing the window.
		 *
		 * @param e The action event.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton src = (JButton) e.getSource();
			if (src == close)
				dispose();
		}

	}

	// ---------------- inc nearest -----------------

	/**
	 * Represents an abstract element in a priority queue used for nearest neighbor
	 * queries in a CIF tree structure.
	 */
	class CIFQueueElement {
		double[] keys;

		/**
		 * Constructs a CIFQueueElement with specified key values.
		 *
		 * @param k The key values used for priority ordering in the queue.
		 */
		CIFQueueElement(double[] k) {
			keys = k;
		}
	}

	/**
	 * A specific type of CIFQueueElement representing a leaf node containing a
	 * rectangle.
	 */
	class CIFQLeaf extends CIFQueueElement {
		DRectangle rec;

		/**
		 * Constructs a CIFQLeaf with specified key values and a rectangle.
		 *
		 * @param k  The key values for the queue.
		 * @param dr The rectangle contained in the leaf node.
		 */
		CIFQLeaf(double[] k, DRectangle dr) {
			super(k);
			rec = dr;
		}
	}

	/**
	 * Represents an internal node in the priority queue, associated with a CIF tree
	 * node.
	 */
	class CIFQINode extends CIFQueueElement {
		CIFcnode r; // Represents the associated CIF tree node
		DRectangle block; // The spatial block covered by this tree node

		/**
		 * Constructs a CIFQINode with specified key values, a CIF tree node, and its
		 * spatial block.
		 *
		 * @param k The key values for the queue.
		 * @param p The associated CIF tree node.
		 * @param b The spatial block covered by this tree node.
		 */
		CIFQINode(double[] k, CIFcnode p, DRectangle b) {
			super(k);
			r = p;
			block = b;
		}
	}

	/**
	 * Specialized CIFQLeaf representing the nearest node in a CIF tree for a given
	 * query point.
	 */
	class NearestINode extends CIFQLeaf {
		CIFcnode cnode; // The closest CIF tree node to the query point
		DPoint pnt; // The query point

		/**
		 * Constructs a NearestINode with specified key values, a CIF tree node, and a
		 * query point.
		 *
		 * @param k  The key values for the queue.
		 * @param cn The closest CIF tree node to the query point.
		 * @param p  The query point.
		 */
		NearestINode(double[] k, CIFcnode cn, DPoint p) {
			super(k, null);
			cnode = cn;
			pnt = p;
		}
	}

	/**
	 * Implements incremental nearest neighbor queries in a CIF tree using a
	 * priority queue.
	 */
	class CIFIncNearest {

		/**
		 * Priority queue to manage the exploration of CIF tree nodes during nearest
		 * neighbor queries.
		 */
		class CIFQueue {
			Vector v;

			CIFQueue() {
				v = new Vector();
			}

			/**
			 * Adds a new element to the queue, maintaining the priority order.
			 *
			 * @param qe The element to be added to the queue.
			 */
			void Enqueue(CIFQueueElement qe) {
				v.addElement(qe);
				for (int i = v.size() - 1; i > 0; i--) {
					CIFQueueElement q1 = (CIFQueueElement) v.elementAt(i - 1);
					CIFQueueElement q2 = (CIFQueueElement) v.elementAt(i);
					if (q1.keys[0] > q2.keys[0] || (q1.keys[0] == q2.keys[0]
							&& ((q1.keys[1] > q2.keys[1] && q1 instanceof CIFQLeaf && q2 instanceof CIFQLeaf)
									|| (q1 instanceof CIFQLeaf && !(q2 instanceof CIFQLeaf))))) {
						v.setElementAt(q2, i - 1);
						v.setElementAt(q1, i);
					}
				}
			}

			/**
			 * Returns the first element in the queue without removing it.
			 *
			 * @return The first element in the queue.
			 */
			CIFQueueElement First() {
				CIFQueueElement q = (CIFQueueElement) v.elementAt(0);
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
			 * @return The removed first element.
			 */
			CIFQueueElement Dequeue() {
				CIFQueueElement q = (CIFQueueElement) v.elementAt(0);
				v.removeElementAt(0);
				return q;
			}

			/**
			 * Enqueues all rectangles in a binary tree that are within the query object's
			 * range.
			 *
			 * @param qu The query object.
			 * @param r  The root of the binary tree.
			 */
			void EnqueueBinTree(QueryObject qu, CIFbnode r) {
				if (r == null)
					return;
				for (int i = 0; i < r.rv.size(); i++) {
					double[] dist = new double[2];
					qu.distance(r.rv.get(i), dist);
					Enqueue(new CIFQLeaf(dist, r.rv.get(i)));
				}
				for (int i = 0; i < 2; i++)
					EnqueueBinTree(qu, r.SON[i]);
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
			 * Creates a vector representation of the queue's current state.
			 *
			 * @return A vector representing the current elements in the queue.
			 */
			Vector makeVector() {
				Vector r = new Vector();
				for (int i = 0; i < v.size(); i++) {
					CIFQueueElement q = (CIFQueueElement) v.elementAt(i);
					if (q instanceof CIFQLeaf)
						r.addElement(new GreenRect(((CIFQLeaf) q).rec));
					else
						r.addElement(new QueueBlock(((CIFQINode) q).block));
				}
				return r;
			}
		}

		CIFQueue q;

		/**
		 * Initializes the priority queue with the root of a CIF tree.
		 *
		 * @param rt The root of the CIF tree.
		 */
		CIFIncNearest(CIFcnode rt) {
			q = new CIFQueue();
			double[] dist = new double[2];
			dist[0] = dist[1] = 0.0;
			q.Enqueue(new CIFQINode(dist, rt, wholeCanvas));
		}

		/**
		 * Performs a query to find the nearest rectangle to a given query object.
		 *
		 * @param qu The query object.
		 * @return The nearest rectangle to the query object, or null if none found.
		 */
		DRectangle Query(QueryObject qu) {
			DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
			return (ar.length == 0) ? null : ar[0];
		}

		/**
		 * Overloaded method to perform a nearest neighbor query and store intermediate
		 * search results.
		 *
		 * @param qu The query object.
		 * @param v  A vector to store intermediate search results.
		 */
		void Query(QueryObject qu, SearchVector v) {
			Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
		}

		/**
		 * Performs a query to find all rectangles within a specified distance of a
		 * query object.
		 *
		 * @param qu   The query object.
		 * @param dist The maximum distance to consider.
		 * @return An array of rectangles within the specified distance of the query
		 *         object.
		 */
		DRectangle[] Query(QueryObject qu, double dist) {
			return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
		}

		/**
		 * Performs a comprehensive nearest neighbor query with constraints on distance
		 * and number of elements.
		 *
		 * @param qu      The query object.
		 * @param ret     A vector to store intermediate search results.
		 * @param dist    The maximum distance to consider.
		 * @param nrelems The maximum number of elements to return.
		 * @return An array of rectangles meeting the specified constraints.
		 */
		DRectangle[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
			Vector rect = new Vector();
			final double xf[] = { 0, .5, 0, .5 };
			final double yf[] = { .5, .5, 0, 0 };
			int counter = 1;

			while (!q.isEmpty()) {
				CIFQueueElement element = q.Dequeue();
				if (element instanceof CIFQLeaf) {
					CIFQLeaf ql = (CIFQLeaf) element;
					if (nrelems-- <= 0 || qu.distance(ql.rec) > dist)
						break;
					rect.addElement(ql.rec);
					ret.addElement(new NNElement(new NNDrawable(ql.rec, counter++), ql.keys[0], q.makeVector()));
				} else {
					CIFQINode in = (CIFQINode) element;
					ret.addElement(new NNElement(new YellowBlock(in.block, false), in.keys[0], q.makeVector()));
					q.EnqueueBinTree(qu, in.r.BIN[0]);
					q.EnqueueBinTree(qu, in.r.BIN[1]);

					for (int i = 0; i < 4; i++)
						if (in.r.SON[i] != null) {
							DRectangle dr = new DRectangle(in.block.x + xf[i] * in.block.width,
									in.block.y + yf[i] * in.block.height, in.block.width / 2, in.block.height / 2);
							double[] keys = new double[2];
							qu.distance(dr, keys);
							q.Enqueue(new CIFQINode(keys, in.r.SON[i], dr));
						}
				}
			}
			DRectangle[] ar = new DRectangle[rect.size()];
			rect.copyInto(ar);
			return ar;
		}

		/**
		 * Performs a query to find the nearest internal node in the CIF tree to a query
		 * point.
		 *
		 * @param qu The query point.
		 * @return The nearest internal node to the query point.
		 */
		NearestINode CIFQuery(DPoint qu) {
			final double xf[] = { 0, .5, 0, .5 };
			final double yf[] = { .5, .5, 0, 0 };

			while (!q.isEmpty()) {
				CIFQueueElement element = q.Dequeue();

				if (element instanceof NearestINode) {
					return (NearestINode) element;
				} else {
					CIFQINode in = (CIFQINode) element;
					double[] lkeys = new double[2];
					DPoint dp = new DPoint(in.block.x + in.block.width / 2, in.block.y + in.block.height / 2);
					qu.distance(dp, lkeys);
					q.Enqueue(new NearestINode(lkeys, in.r, dp));
					for (int i = 0; i < 4; i++)
						if (in.r.SON[i] != null) {
							DRectangle dr = new DRectangle(in.block.x + xf[i] * in.block.width,
									in.block.y + yf[i] * in.block.height, in.block.width / 2, in.block.height / 2);
							double[] keys = new double[2];
							qu.distance(dr, keys);
							q.Enqueue(new CIFQINode(keys, in.r.SON[i], dr));
						}
				}
			}
			return null;

		}
	}

	/**
	 * Represents a binary node in a Cartesian-Interval Forest (CIF) tree. Each
	 * CIFbnode can have two children (SON) and holds a collection of rectangles
	 * (DRectVector).
	 */
	class CIFbnode {
		CIFbnode[] SON; // Children of the binary node
		DRectVector rv; // Collection of rectangles

		/**
		 * Constructs a CIFbnode with no children and an empty collection of rectangles.
		 */
		CIFbnode() {
			SON = new CIFbnode[2];
			SON[0] = SON[1] = null; // Initializing children to null
			rv = new DRectVector(); // Initializing the collection of rectangles
		}
	}

	/**
	 * Represents a Cartesian-Interval Forest (CIF) tree node. Each CIFcnode can
	 * have a binary node for each axis (x and y) and up to four children.
	 */
	class CIFcnode {
		final int NRDIRS = 4; // Number of directions (quadrants) in the CIF tree

		CIFbnode BIN[] = new CIFbnode[2]; // Binary nodes for 'x' and 'y' coordinates
		CIFcnode SON[] = new CIFcnode[NRDIRS]; // Initializing children for four directions

		/**
		 * Constructs a CIFcnode with no binary nodes and no children.
		 */
		CIFcnode() {
			BIN[0] = BIN[1] = null;
			SON[0] = SON[1] = SON[2] = SON[3] = null;
		}
	}

}
