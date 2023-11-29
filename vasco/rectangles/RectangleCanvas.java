package vasco.rectangles;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/* $Id: RectangleCanvas.java,v 1.3 2007/10/28 15:38:19 jagan Exp $ */
import vasco.common.ColorHelp;
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.FileIface;
import vasco.common.GenericCanvas;
import vasco.common.MouseDisplay;
import vasco.common.NearestThread;
import vasco.common.QueryObject;
import vasco.common.SearchThread;
import vasco.common.SearchVector;
import vasco.common.Tools;
import vasco.common.TopInterface;
import vasco.common.fileSelector;
import vasco.drawable.Drawable;

// ------------- P Canvas -------------------

/**
 * The RectangleCanvas class represents a canvas for manipulating rectangles. It
 * extends the GenericCanvas class and implements the FileIface and ItemListener
 * interfaces.
 */
public class RectangleCanvas extends GenericCanvas implements FileIface, ItemListener {

	RectangleStructure[] pstrs; // Array to hold different rectangle structures
	public RectangleStructure pstruct; // Current rectangle structure

	/**
	 * Constructs a RectangleCanvas with specified parameters.
	 *
	 * @param r        Initial dimensions of the canvas.
	 * @param dt       Drawing target.
	 * @param overview Overview drawing target.
	 * @param animp    Animation panel.
	 * @param ti       Top-level interface.
	 */
	public RectangleCanvas(DRectangle r, DrawingTarget dt, DrawingTarget overview, JPanel animp, TopInterface ti) {
		super(r, dt, overview, animp, ti);
		pstrs = new RectangleStructure[7];

		// Initialize operation features
		// Initialize operation features for rectangle manipulation.
		opFeature = new OpFeature[11];
		int index = 0;
		opFeature[index++] = new OpFeature("Insert", OPFEATURE_INSERT, "Click and drag to insert a new rectangle.",
				"Insert new rectangle", "", "", InputEvent.BUTTON1_MASK);

		opFeature[index++] = new OpFeature("Move", OPFEATURE_MOVE,
				"Click and drag to move the closest rectangle to a new location.", "Move existing rectangle", "", "",
				InputEvent.BUTTON1_MASK);

		opFeature[index++] = new OpFeature("Move vertex", OPFEATURE_MOVEVERTEX,
				"Click near vertex and drag to resize rectangle.", "Move existing vertex", "", "",
				InputEvent.BUTTON1_MASK);

		opFeature[index++] = new OpFeature("Move edge", OPFEATURE_MOVEEDGE,
				"Click near edge and drag to resize rectangle.", "Move existing edge", "", "", InputEvent.BUTTON1_MASK);

		opFeature[index++] = new OpFeature("Show bintree", OPFEATURE_BINTREES,
				"Click near a quadtree node. The binary trees stored in the node will be displayed.",
				"Show bintree for nearest node", "", "", InputEvent.BUTTON1_MASK);

		opFeature[index++] = new OpFeature("Delete", OPFEATURE_DELETE, "Click inside a rectangle to erase it.",
				"Delete nearest line", "", "", InputEvent.BUTTON1_MASK);

		OpFeature[] opf = new OpFeature[5];

		// Initialize operation features for query objects.
		opf[0] = new OpFeature("Point", QueryObject.QO_POINT,
				"Specify a query point to find all items within given distance from it.", "Enter query point", "", "",
				InputEvent.BUTTON1_MASK);
		opf[1] = new OpFeature("Rectangle", QueryObject.QO_RECTANGLE,
				"Specify a query rectangle to find all items within given distance from it.",
				"Specify a query rectangle", "", "", InputEvent.BUTTON1_MASK);
		opf[2] = new OpFeature("Polygon", QueryObject.QO_POLYGON,
				"Click repeatedly to specify vertices of a query polygon.  Click right button to close the polygon.",
				"Input new query polygon vertex", "", "Input final query polygon vertex",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON3_MASK);
		opf[3] = new OpFeature("Path", QueryObject.QO_PATH,
				"Click repeatedly to specify vertices of a query path.  Click middle button to snap to the first vertex. Click right button to insert the last vertex.",
				"Input new query path vertex", "Snap to first vertex", "Input final query path vertex",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
		opf[4] = new OpFeature("Sector", QueryObject.QO_SECTOR, "Define a rooted sector as the query object.",
				"Input root of query sector", "Input starting angle", "Input extent of sector",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);

		// Add features for overlap, nearest, and within operations
		opFeature[index++] = new OpFeatures("Overlap", OPFEATURE_WINDOW, opf, withinStats);
		opFeature[index++] = new OpFeatures("Nearest", OPFEATURE_NEAREST, opf, withinStats);
		opFeature[index++] = new OpFeatures("Within", OPFEATURE_WITHIN, opf, withinStats);

		// New Operations for the LOOSE Quadtree
		opFeature[index++] = new OpFeature("Motion Insensitivity", OPFEATURE_MOTIONSENSITIVITY,
				"Move a rectangle as much as the structure permits ", "Move an existing rectangle", "",
				"Show containment in next level", InputEvent.BUTTON1_MASK | InputEvent.BUTTON3_MASK);

		opFeature[index++] = new OpFeature("Show Quadtree", OPFEATURE_SHOWQUAD, "Show containment of a rectangle", "",
				"", "", 0);
	}

	/**
	 * Initializes the RectangleStructure array.
	 */
	@Override
	public synchronized void initStructs() {
		// Initialize various RectangleStructure implementations.
		pstrs[0] = new CIFTree(wholeCanvas, 9, topInterface, this);
		pstrs[1] = new RectTree(wholeCanvas, 9, topInterface, this);
		pstrs[2] = new BucketRect(wholeCanvas, 9, 3, topInterface, this);
		pstrs[3] = new PMRRectTree(wholeCanvas, 9, 3, topInterface, this);
		pstrs[4] = new PMRkd(wholeCanvas, 18, 3, topInterface, this);
		pstrs[5] = new RectangleRTree(wholeCanvas, topInterface, this);
		pstrs[6] = new LOOSETree(wholeCanvas, 9, 2.0, topInterface, this);
	}

	/**
	 * Gets the type of the applet.
	 *
	 * @return The applet type.
	 */
	@Override
	public int getAppletType() {
		return ColorHelp.RECTANGLE_APPLET;
	}

	/**
	 * Gets the count of available structures.
	 *
	 * @return The count of structures.
	 */
	@Override
	public int getStructCount() {
		return pstrs.length;
	}

	/**
	 * Gets the name of the structure at the specified index.
	 *
	 * @param i The index of the structure.
	 * @return The name of the structure.
	 */
	@Override
	public String getStructName(int i) {
		return pstrs[i].getName();
	}

	/**
	 * Gets the name of the current rectangle structure.
	 *
	 * @return The name of the current rectangle structure.
	 */
	@Override
	public String getCurrentName() {
		return pstruct.getName();
	}

	/**
	 * Gets the file selector for a specific operation.
	 *
	 * @param op Operation for which the file selector is needed.
	 * @return RectangleFileSelector for the specified operation.
	 */
	@Override
	public fileSelector getFileSelector(String op) {
		return new RectangleFileSelector(this, op, topInterface);
	}

	/**
	 * Clears the canvas.
	 */
	@Override
	public void clear() {
		super.clear();
		pstruct.MessageStart();
		pstruct.Clear();
		pstruct.MessageEnd();
	}

	/**
	 * Gets the name of the current operation.
	 *
	 * @return The name of the current operation.
	 */
	@Override
	protected String getCurrentOperationName() {
		return pstruct.getCurrentOperation();
	}

	/* ------------- file load / save ------------ */

	/**
	 * Converts the history list of rectangles to a vector.
	 *
	 * @return The vector representation of the history list.
	 */
	@Override
	public Vector vectorOut() {
		Vector ret = new Vector();
		for (int i = 0; i < historyList.size(); i++) {
			DRectangle pt = (DRectangle) historyList.elementAt(i);
			if (pt instanceof DeleteRectangle) {
				double[] dist = new double[2];
				DRectangle min = (DRectangle) ret.elementAt(0);
				DPoint searchFor = new DPoint(pt.x, pt.y);
				searchFor.distance(min, dist);

				for (int j = 1; j < ret.size(); j++) {
					double[] d = new double[2];
					DRectangle p = (DRectangle) ret.elementAt(j);
					searchFor.distance(p, d);
					if (d[0] < dist[0] || (d[0] == dist[0] && d[1] < dist[1])) {
						dist = d;
						min = p;
					}
				}
				ret.removeElement(min);
			} else
				ret.addElement(pt);
		}
		return ret;
	}

	/**
	 * Converts the history list of rectangles to an array of strings.
	 *
	 * @return The array of strings representing the history list.
	 */
	@Override
	public String[] stringsOut() {
		Vector in = vectorOut();
		String[] out = new String[in.size()];
		for (int i = 0; i < in.size(); i++) {
			DRectangle er = (DRectangle) in.elementAt(i);
			out[i] = new String(er.x + " " + er.y + " " + er.width + " " + er.height);
		}
		return out;
	}

	/**
	 * Converts a vector of extended points to rectangle structures and updates the
	 * canvas.
	 *
	 * @param p The vector of extended points.
	 */
	@Override
	public void vectorIn(Vector p) { /* vector of extended points */
		pstruct.MessageStart();
		pstruct.Clear();
		historyList = p;
		for (int i = 0; i < p.size(); i++) {
			DRectangle pt = (DRectangle) p.elementAt(i);
			pstruct.Insert(pt);
		}
		pstruct.MessageEnd();
		redraw();
	}

	/* ----- drawing utilities ----------- */

	/**
	 * Draws the contents on the given drawing target.
	 *
	 * @param g The drawing target.
	 */
	@Override
	public void drawContents(DrawingTarget g) {
		pstruct.drawContents(g, g.getView());
	}

	/**
	 * Draws the grid on the given drawing target.
	 *
	 * @param g The drawing target.
	 */
	@Override
	public void drawGrid(DrawingTarget g) {
		super.drawGrid(g);
		if (gridOn)
			pstruct.drawGrid(g, gridLevel);
	}

	/* --------------- operations on structures ------------------- */

	/**
	 * Searches for objects based on the specified query.
	 *
	 * @param s   The query object.
	 * @param off The drawing targets.
	 */
	@Override
	protected void search(QueryObject s, DrawingTarget[] off) {
		SearchVector v;
		v = pstruct.Search(s, searchMode);
		if (runningThread == null) {
			runningThread = new SearchThread(v, s, this, off);
		}
	}

	/**
	 * Finds the nearest objects based on the specified query.
	 *
	 * @param p    The query object.
	 * @param dist The distance.
	 * @param off  The drawing targets.
	 */
	@Override
	protected void nearest(QueryObject p, double dist, DrawingTarget[] off) {
		SearchVector v;
		v = pstruct.Nearest(p, dist);
		if (runningThread == null) {
			if (withinStats.getBlend())
				runningThread = new NearestThread(v, p, this, dist, off, 255, 0, 0, 0, 255, 0);
			else
				runningThread = new NearestThread(v, p, this, dist, off);
		}
	}

	/**
	 * Gets the allowed overlap query objects for spatial queries.
	 *
	 * @return The bitmask representing the allowed overlap query objects.
	 */
	@Override
	protected int getAllowedOverlapQueryObjects() {
		return QueryObject.QO_RECTANGLE | QueryObject.QO_POLYGON | QueryObject.QO_LINE | QueryObject.QO_PATH
				| QueryObject.QO_POINT | QueryObject.QO_SECTOR;
	}

	/**
	 * Gets the search mode mask for spatial queries.
	 *
	 * @return The bitmask representing the search mode mask.
	 */
	@Override
	protected int getSearchModeMask() {
		return SEARCHMODE_CONTAINS | SEARCHMODE_OVERLAPS | SEARCHMODE_CROSSES | SEARCHMODE_ISCONTAINED;
	}

	/* --------------- operations on structures ------------------- */
	/**
	 * Checks if an element at a given index is present in the specified array.
	 *
	 * @param index The index of the element to check.
	 * @param test  The array to check against.
	 * @return True if the element at the specified index is in the array;
	 *         otherwise, false.
	 */
	boolean isChange(int index, int[] test) { // to test if element 'i' from 'lines' is in array 'test'
		if (test == null)
			return false;
		for (int element : test)
			if (index == element)
				return true;
		return false;
	}

	/**
	 * Updates the canvas based on the provided parameters.
	 */
	void updateFromParams() {
		updateFromParams(null);
	}

	/**
	 * Updates the canvas based on the provided parameters and a test array.
	 *
	 * @param test The test array.
	 * @return True if the update is successful, false otherwise.
	 */
	boolean updateFromParams(int[] test) {
		pstruct.MessageStart();
		pstruct.Clear();
		for (int i = 0; i < historyList.size(); i++) {
			DRectangle p = (DRectangle) historyList.elementAt(i);
			if (p instanceof DeleteRectangle)
				pstruct.Delete(((DeleteRectangle) p).getPoint());
			else {
				if (!pstruct.Insert(p) && isChange(i, test))
					return false;
			}
		}
		pstruct.MessageEnd();
		return true;
	}

	/**
	 * Sets the help information based on the current operation and search mode.
	 * Overrides the method in the superclass.
	 */
	@Override
	protected void setHelp() {
		super.setHelp();
		String help = "";
		if (getCurrentOperation() == OPFEATURE_WINDOW) {
			if (searchMode == 0) {
				help += "No rectangles are returned.";
			} else {
				Vector<String> hs = new Vector<>();
				if ((searchMode & SEARCHMODE_CONTAINS) != 0)
					hs.addElement("completely inside the query area");
				if ((searchMode & SEARCHMODE_ISCONTAINED) != 0)
					hs.addElement("completely containing the query area");
				if ((searchMode & SEARCHMODE_CROSSES) != 0)
					hs.addElement("intersecting the query area, but no vertex is part of the intersection");
				if ((searchMode & SEARCHMODE_OVERLAPS) != 0)
					hs.addElement("intersecting the query area and at least one vertex is part of the intersection");

				help += "Rectangles that are ";
				for (int j = 0; j < hs.size(); j++) {
					help += (String) hs.elementAt(j);
					if (j < hs.size() - 2)
						help += ", ";
					else if (j == hs.size() - 2)
						help += " or ";
				}
				help += " are returned.";
			}
			topInterface.getHelpArea().append("\n" + Tools.formatHelp(help, topInterface.getHelpArea().getColumns()));
		}
	}

	/**
	 * Sets the tree for the specified index.
	 *
	 * @param i   The index.
	 * @param ops The choice of operations.
	 */
	@Override
	public void setTree(int i, JComboBox<String> ops) {
		pstruct = pstrs[i];
//		System.out.println("pstruct " + pstruct);
//		System.out.println("ops " + ops);
		ops.removeAll();
//		System.out.println("count " + ops.getItemCount());
		pstruct.reInit(ops);
//		System.out.println("After count " + ops.getItemCount());
//		System.out.println("pstruct " + pstruct);
		try {
			String op = (String) ops.getSelectedItem();
			if (op == null) {
				op = "Insert";
			}
			ops.setSelectedItem(op);
		} catch (Exception e) {
		}

		setHelp();
		rebuild();
	}

	/**
	 * Rebuilds the canvas.
	 */
	@Override
	public void rebuild() {
		updateFromParams();
		if (runningThread != null) {
			terminate();
			if ((getCurrentOperation() == OPFEATURE_NEAREST || getCurrentOperation() == OPFEATURE_WITHIN)
					&& lastNear != null)
				nearest(lastNear, withinStats.getDist(), allDrawingTargets);
			else if (getCurrentOperation() == OPFEATURE_WINDOW && lastWindow != null)
				search(lastWindow, allDrawingTargets);
		}
		redraw();
	}

	// ---------------- MouseListener && MouseMotionListener -------------

	/**
	 * Represents a moveable rectangle along with its index in the history list.
	 */
	class moveRectangles {
		DRectangle rect; // joint in the first vertex
		int index; // index in the history list
	}

	moveRectangles[] lastMove; // Array to store information about the last moved rectangles.
	Drawable lastDelete; // Represents the last deleted drawable object.
	// DRectangle lastInsert;
	DPoint lastBintree; // Represents the last clicked point in a binary tree.
	DPoint lastMoveVertex; // Represents the last moved vertex point.

	DLine lastMoveEdge; // Represents the last moved edge.
	int lastMoveEdgeIndex; // Represents the index of the last moved edge.
	// Constants representing cardinal directions.
	final static int NORTH = 0;
	final static int SOUTH = 1;
	final static int EAST = 2;
	final static int WEST = 3;

	int lastInsert; // Represents the index of the last inserted rectangle in the history list.
	int lastEvent = MOUSE_MOVED; // Represents the type of the last mouse event.

	/**
	 * Handles the mouse entered event on the canvas.
	 *
	 * @param me The MouseEvent object.
	 */
	@Override
	public void mouseEntered(MouseEvent me) {
		super.mouseEntered(me);
		debugPrint("mouseEntered");
		lastDelete = null;
		lastBintree = null;
	}

	/**
	 * Handles the mouse exited event on the canvas.
	 *
	 * @param me The MouseEvent object.
	 */
	@Override
	public void mouseExited(MouseEvent me) {
		super.mouseExited(me);
		debugPrint("mouseExited");
		if (lastDelete != null) {
			lastDelete.directDraw(Color.red, offscrG);
		}
		lastDelete = null;
	}

	/**
	 * Handles the mouse moved event on the canvas.
	 *
	 * @param me The MouseEvent object.
	 */
	@Override
	public void mouseMoved(MouseEvent me) {

		if (lastEvent != MOUSE_RELEASED && lastEvent != MOUSE_MOVED)
			return;
		super.mouseMoved(me);

		debugPrint("mouseMoved");

		lastEvent = MOUSE_MOVED;
		DPoint p = offscrG.transPointT(offscrG.adjustPoint(me.getPoint()));
		int op = getCurrentOperation();

		if (op == OPFEATURE_MOVEEDGE) {
			lastInsert = -1;
			DRectangle fP = (DRectangle) pstruct.NearestFirst(new QueryObject(p));
			if (fP != null) {
				for (int i = historyList.size() - 1; i >= 0; i--) {
					if (((DRectangle) (historyList.elementAt(i))).equals(fP))
						lastInsert = i;
				}
			}

			double d1, d2, d3, d4;
			d1 = p.distance(fP.Nside());
			d2 = p.distance(fP.Sside());
			d3 = p.distance(fP.Wside());
			d4 = p.distance(fP.Eside());
			DLine nearest = null;
			if (d1 <= d2 && d1 <= d3 && d1 <= d4) {
				nearest = fP.Nside();
				lastMoveEdgeIndex = NORTH;
			}
			if (d2 <= d1 && d2 <= d3 && d2 <= d4) {
				nearest = fP.Sside();
				lastMoveEdgeIndex = SOUTH;
			}
			if (d3 <= d1 && d3 <= d2 && d3 <= d4) {
				nearest = fP.Wside();
				lastMoveEdgeIndex = WEST;
			}
			if (d4 <= d1 && d4 <= d2 && d4 <= d3) {
				nearest = fP.Eside();
				lastMoveEdgeIndex = EAST;
			}
			if (!nearest.equals(lastMoveEdge)) {
				redraw();
				nearest.directDraw(Color.orange, offscrG);
			}
			lastMoveEdge = nearest;
		}

		if (op == OPFEATURE_MOVEVERTEX) {
			lastInsert = -1;
			DRectangle fP = (DRectangle) pstruct.NearestFirst(new QueryObject(p));
			if (fP != null) {
				for (int i = historyList.size() - 1; i >= 0; i--) {
					if (((DRectangle) (historyList.elementAt(i))).equals(fP))
						lastInsert = i;
				}
			}

			double d1, d2, d3, d4;
			d1 = p.distance(fP.SWcorner());
			d2 = p.distance(fP.NWcorner());
			d3 = p.distance(fP.SEcorner());
			d4 = p.distance(fP.NEcorner());
			DPoint nearest = null;
			if (d1 <= d2 && d1 <= d3 && d1 <= d4) {
				nearest = fP.SWcorner();
			}
			if (d2 <= d1 && d2 <= d3 && d2 <= d4) {
				nearest = fP.NWcorner();
			}
			if (d3 <= d1 && d3 <= d2 && d3 <= d4) {
				nearest = fP.SEcorner();
			}
			if (d4 <= d1 && d4 <= d2 && d4 <= d3) {
				nearest = fP.NEcorner();
			}
			if (!nearest.equals(lastMoveVertex)) {
				redraw();
				nearest.directDraw(Color.orange, offscrG);
			}
			lastMoveVertex = nearest;
		}

		if (op == OPFEATURE_DELETE || op == OPFEATURE_MOVE || op == OPFEATURE_MOTIONSENSITIVITY) {

			if (lastDelete != null) {
				lastDelete.directDraw(Color.red, offscrG);
				lastDelete = null;
			}

			Drawable b = pstruct.NearestFirst(new QueryObject(p));
			if (b != null) {
				b.directDraw(Color.orange, offscrG);
				lastDelete = b;
			} else
				lastDelete = null;
		}

		if (op == OPFEATURE_SHOWQUAD) {

			Drawable b = pstruct.NearestFirst(new QueryObject(p));

			if (lastDelete != null && lastDelete.equals(b)) {
				return;
			}

			redraw();
			DRectangle quad = pstruct.EnclosingQuadBlock((DRectangle) b, false);

			if (quad != null) {
				offscrG.directThickRect(Color.blue, quad.x, quad.y, quad.width, quad.height, 2);
				quad = pstruct.expand(quad);
				offscrG.directThickRect(Color.green, quad.x, quad.y, quad.width, quad.height, 2);
			}

			DRectangle nr = (DRectangle) b;
			offscrG.directThickRect(Color.orange, nr.x, nr.y, nr.width, nr.height, 1);
			lastDelete = b;
		}

		if (op == OPFEATURE_BINTREES && pstruct instanceof CIFTree) {
			DPoint b = ((CIFTree) pstruct).NearestMXCIF(p);
			if (b != null) {
				if (!b.equals(lastBintree)) {
					offscrG.redraw();
					b.directDraw(Color.orange, offscrG);
					lastBintree = b;
				}
			} else
				lastBintree = null;
		}
	}

	/**
	 * Handles the mouse clicked event on the canvas.
	 *
	 * @param me The MouseEvent object.
	 */
	@Override
	public void mouseClicked(MouseEvent me) {

		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button

		super.mouseClicked(me);
	}

	/**
	 * Handles mouse events on the canvas.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mousePressed(MouseEvent me) {

		if ((lastEvent != MOUSE_RELEASED && lastEvent != MOUSE_MOVED)
				|| ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0))
			return; // operation doesn't use this mouse button

		super.mousePressed(me);
		debugPrint("mousePressed");
		lastEvent = MOUSE_PRESSED;
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);

		int op = getCurrentOperation();
		if (op == OPFEATURE_INSERT) {
			lastP = scrCoord;
		}

		// Implementation for delete operation
		if (op == OPFEATURE_DELETE) {
			pstruct.MessageStart();
			pstruct.Delete(p);
			historyList.addElement(new DeleteRectangle(p));
			lastDelete = null;
			pstruct.MessageEnd();
		}

		// Implementation for Bintrees operation
		if (op == OPFEATURE_BINTREES && pstruct instanceof CIFTree) {
			redraw();
			((CIFTree) pstruct).drawBintree(p, offscrG);
			offscrG.redraw();
			return;
		}

		// Implementation for motionsensitivity operation
		if (op == OPFEATURE_MOTIONSENSITIVITY && me.getButton() == MouseEvent.BUTTON3) {

			Drawable b = pstruct.NearestFirst(new QueryObject(p));

			if (lastDelete == null || !lastDelete.equals(b)) {
				redraw();
			}

			if (b != null) {
				DRectangle quad = pstruct.EnclosingQuadBlock((DRectangle) b, true);
				if (quad != null) {
					offscrG.directThickRect(Color.blue, quad.x, quad.y, quad.width, quad.height, 2);
					quad = pstruct.expand(quad);
					offscrG.directThickRect(Color.green, quad.x, quad.y, quad.width, quad.height, 2);
				}
				DRectangle bb = (DRectangle) b;
				offscrG.directThickRect(Color.orange, bb.x, bb.y, bb.width, bb.height, 2);
			}
			lastDelete = null;
			return;
		}

		// Implementation for move or motionsensitivity operation
		if (op == OPFEATURE_MOVE || op == OPFEATURE_MOTIONSENSITIVITY) {

			lastMove = null;
			if (lastDelete != null) {
				DRectangle dr = (DRectangle) lastDelete;
				lastMove = new moveRectangles[1];
				lastMove[0] = new moveRectangles();
				lastMove[0].rect = dr; // lastMove[0] is the moved line

				for (int j = historyList.size() - 1; j >= 0; j--) {
					if (lastMove[0].rect.equals(historyList.elementAt(j))) {
						lastMove[0].index = j;
						break;
					}
				}
			}
		}

		redraw();
		mouseDragged(me);
	}

	int global_count_1 = 0;
	int global_count_2 = 0;

	/**
	 * Performs adjustments for loosening the position of rectangles.
	 *
	 * @param newloc The new location of the rectangle.
	 */
	public void opLooseness(DPoint newloc) {

		if (!(pstruct instanceof LOOSETree))
			return;

		DRectangle nr = new DRectangle(newloc.x - lastMove[0].rect.width / 2, newloc.y - lastMove[0].rect.height / 2,
				lastMove[0].rect.width, lastMove[0].rect.height);

		if (lastMove[0].rect == null)
			return;

		global_count_2++;

		if (global_count_2 < 3)
			return;

		global_count_2 = 0;

		DRectangle quad1 = pstruct.EnclosingQuadBlock(lastMove[0].rect, false);
		DRectangle quad = pstruct.expand(quad1);

		if (!quad.contains(nr)) {

			if (quad.x > nr.x)
				nr.x = quad.x;
			if (quad.y > nr.y)
				nr.y = quad.y;

			if (quad.x + quad.width < nr.x + nr.width) {
				nr.x = quad.x + quad.width - nr.width;
			}

			if (quad.y + quad.height < nr.y + nr.height) {
				nr.y = quad.y + quad.height - nr.height;
			}

		}

		if (!(wholeCanvas.contains(nr)))
			return;

		if (pstruct.ReplaceRectangles(lastMove[0].rect, nr)) {
			redraw();
			lastMove[0].rect = nr;
			offscrG.directThickRect(Color.orange, nr.x, nr.y, nr.width, nr.height, 1);
			if (quad1 != null && quad != null) {

				offscrG.directThickRect(Color.blue, quad1.x, quad1.y, quad1.width, quad1.height, 2);

				offscrG.directThickRect(Color.green, quad.x, quad.y, quad.width, quad.height, 2);
			}
		}

		lastDelete = null;
		historyList.setElementAt(lastMove[0].rect, lastMove[0].index);
		return;
	}

	/**
	 * Moves the rectangle to a new location, considering constraints.
	 *
	 * @param newloc The new location of the rectangle.
	 */
	private void looseMoveRectangle(DPoint newloc) {

		DRectangle nr = new DRectangle(newloc.x - lastMove[0].rect.width / 2, newloc.y - lastMove[0].rect.height / 2,
				lastMove[0].rect.width, lastMove[0].rect.height);

		if (!(pstruct instanceof LOOSETree) || !(wholeCanvas.contains(nr)) || (lastMove[0].rect == null)
				|| lastMove[0].rect.equals(nr))
			return;

		global_count_1++;

		if (global_count_1 < 3)
			return;

		global_count_1 = 0;

		if (!(pstruct.ReplaceRectangles(lastMove[0].rect, nr))) {
			pstruct.DeleteDirect(lastMove[0].rect);
			pstruct.Insert(nr);
		}

		DRectangle quad = pstruct.EnclosingQuadBlock(nr, false);
		redraw();

		if (quad != null) {

			offscrG.directThickRect(Color.blue, quad.x, quad.y, quad.width, quad.height, 2);

			quad = pstruct.expand(quad);

			offscrG.directThickRect(Color.green, quad.x, quad.y, quad.width, quad.height, 2);
		}

		offscrG.directThickRect(Color.orange, nr.x, nr.y, nr.width, nr.height, 1);

		lastMove[0].rect = nr;
		lastDelete = null;
		historyList.setElementAt(lastMove[0].rect, lastMove[0].index);
		return;
	}

	/**
	 * Moves the rectangle to a new location.
	 *
	 * @param newloc The new location of the rectangle.
	 */
	private void moveRectangle(DPoint newloc) {

		DRectangle nr = new DRectangle(newloc.x - lastMove[0].rect.width / 2, newloc.y - lastMove[0].rect.height / 2,
				lastMove[0].rect.width, lastMove[0].rect.height);

		DRectangle[] oldset = new DRectangle[lastMove.length];
		for (int i = 0; i < lastMove.length; i++)
			oldset[i] = lastMove[i].rect;

		if (wholeCanvas.contains(nr)) {
			lastMove[0].rect = nr;
			if (!pstruct.orderDependent()) {
				pstruct.MessageStart();
				for (int i = 0; i < lastMove.length; i++)
					pstruct.DeleteDirect(oldset[i]);
				for (int i = 0; i < lastMove.length; i++)
					if (!pstruct.Insert(lastMove[i].rect)) {
						for (int k = i - 1; k >= 0; k--)
							pstruct.DeleteDirect(lastMove[k].rect);
						for (int k = 0; k < lastMove.length; k++) {
							pstruct.Insert(oldset[k]);
							lastMove[k].rect = oldset[k];
						}
						break;
					}
				pstruct.MessageEnd();
				for (moveRectangles element : lastMove)
					historyList.setElementAt(element.rect, element.index);
			} else {
				int[] test = new int[lastMove.length];
				for (int i = 0; i < lastMove.length; i++) {
					historyList.setElementAt(lastMove[i].rect, lastMove[i].index);
					test[i] = lastMove[i].index;
				}
				if (!updateFromParams(test)) {
					for (int i = 0; i < lastMove.length; i++) {
						lastMove[i].rect = oldset[i];
						historyList.setElementAt(lastMove[i].rect, lastMove[i].index);
					}
					updateFromParams();
				}
			}
			redraw();
			lastMove[0].rect.directDraw(Color.orange, offscrG);
			if (pstruct instanceof LOOSETree) { // Draw Centroid
				DRectangle bb = lastMove[0].rect;
				DPoint pnt = new DPoint(bb.x + bb.width / 2, bb.y + bb.height / 2);
				pnt.directDraw(Color.orange, offscrG);
				DRectangle quad = pstruct.EnclosingQuadBlock(lastMove[0].rect, false);

				if (quad != null) {

					offscrG.directThickRect(Color.blue, quad.x, quad.y, quad.width, quad.height, 2);

					quad = pstruct.expand(quad);

					offscrG.directThickRect(Color.green, quad.x, quad.y, quad.width, quad.height, 2);
				}
			}
			lastDelete = lastMove[0].rect;

		}
	}

	/**
	 * Handles mouse drag events on the canvas.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseDragged(MouseEvent me) {

		debugPrint("mouseDragged");

		if ((lastEvent != MOUSE_DRAGGED && lastEvent != MOUSE_PRESSED)
				|| ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0))
			return; // operation doesn't use this mouse button

		super.mouseDragged(me);
		lastEvent = MOUSE_DRAGGED;
		int op = getCurrentOperation();
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);

		// Implementation for move edge operation
		if (op == OPFEATURE_MOVEEDGE) {
			DRectangle fP = (DRectangle) historyList.elementAt(lastInsert);
			DRectangle newest = null;
			if (lastMoveEdgeIndex == NORTH) {
				newest = p.y - fP.y > 0 ? new DRectangle(fP.x, fP.y, fP.width, p.y - fP.y) : fP;
			}
			if (lastMoveEdgeIndex == SOUTH) {
				newest = fP.height + fP.y - p.y > 0 ? new DRectangle(fP.x, p.y, fP.width, fP.height + fP.y - p.y) : fP;
			}
			if (lastMoveEdgeIndex == WEST) {
				newest = fP.width + fP.x - p.x > 0 ? new DRectangle(p.x, fP.y, fP.width + fP.x - p.x, fP.height) : fP;
			}
			if (lastMoveEdgeIndex == EAST) {
				newest = p.x - fP.x > 0 ? new DRectangle(fP.x, fP.y, p.x - fP.x, fP.height) : fP;
			}

			if (pstruct.orderDependent()) {
				historyList.setElementAt(newest, lastInsert);
				int[] nwst = new int[1];
				nwst[0] = lastInsert;
				if (!updateFromParams(nwst)) {
					historyList.setElementAt(fP, lastInsert);
					updateFromParams();
				} else {
					fP = newest;
				}
			} else {
				pstruct.MessageStart();
				pstruct.DeleteDirect((DRectangle) historyList.elementAt(lastInsert));
				if (!pstruct.Insert(newest)) {
					pstruct.Insert(fP);
					fP = (DRectangle) historyList.elementAt(lastInsert);
				} else {
					fP = newest;
					historyList.setElementAt(fP, lastInsert);
				}
				pstruct.MessageEnd();
			}
			redraw();
			fP.directDraw(Color.orange, offscrG);
			lastDelete = fP;
		}

		// Implementation for move vertex operation
		if (op == OPFEATURE_MOVEVERTEX) {
			DRectangle fP = (DRectangle) historyList.elementAt(lastInsert);
			double d1, d2, d3, d4;
			d1 = p.distance(fP.SWcorner());
			d2 = p.distance(fP.NWcorner());
			d3 = p.distance(fP.SEcorner());
			d4 = p.distance(fP.NEcorner());
			DRectangle newest = null;
			if (d1 <= d2 && d1 <= d3 && d1 <= d4) {
				newest = new DRectangle(p.x, p.y, fP.width - p.x + fP.x, fP.height - p.y + fP.y);
			}
			if (d2 <= d1 && d2 <= d3 && d2 <= d4) {
				newest = new DRectangle(p.x, fP.y, fP.width - p.x + fP.x, p.y - fP.y);
			}
			if (d3 <= d1 && d3 <= d2 && d3 <= d4) {
				newest = new DRectangle(fP.x, p.y, p.x - fP.x, fP.height - p.y + fP.y);
			}
			if (d4 <= d1 && d4 <= d2 && d4 <= d3) {
				newest = new DRectangle(fP.x, fP.y, p.x - fP.x, p.y - fP.y);
			}

			if (pstruct.orderDependent()) {
				historyList.setElementAt(newest, lastInsert);
				int[] nwst = new int[1];
				nwst[0] = lastInsert;
				if (!updateFromParams(nwst)) {
					historyList.setElementAt(fP, lastInsert);
					updateFromParams();
				} else {
					fP = newest;
				}
			} else {
				pstruct.MessageStart();
				pstruct.DeleteDirect((DRectangle) historyList.elementAt(lastInsert));
				if (!pstruct.Insert(newest)) {
					pstruct.Insert(fP);
					fP = (DRectangle) historyList.elementAt(lastInsert);
				} else {
					fP = newest;
					historyList.setElementAt(fP, lastInsert);
				}
				pstruct.MessageEnd();
			}
			redraw();
			fP.directDraw(Color.orange, offscrG);
			lastDelete = fP;
		}

		// Implementation for insert operation
		if (op == OPFEATURE_INSERT) {
			DPoint last = offscrG.transPointT(lastP);

			// if (tree.runningThread != null)
			// return true;

			offscrG.redraw();
			offscrG.directRect(Color.orange, Math.min(last.x, p.x), Math.min(last.y, p.y), Math.abs(p.x - last.x),
					Math.abs(last.y - p.y));
		}

		if (op == OPFEATURE_MOTIONSENSITIVITY && lastMove != null && lastMove.length != 0) {
			looseMoveRectangle(p);
		}

		if (op == OPFEATURE_MOVE && lastMove != null && lastMove.length != 0) {
			moveRectangle(p);
		}

	}

	/**
	 * Handles mouse release events on the canvas.
	 *
	 * @param e The mouse event.
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
		debugPrint("mouseReleased");

		if (lastEvent != MOUSE_PRESSED && lastEvent != MOUSE_DRAGGED)
			return;

		if (getCurrentOperation() == OPFEATURE_MOVE && (pstruct instanceof LOOSETree)) {
			redraw();
		}

		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button
		super.mouseReleased(me);

		lastEvent = MOUSE_RELEASED;
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);
		// if (tree.runningThread != null)
		// return true;

		int op = getCurrentOperation();
		// Implementation for insert operation
		if (op == OPFEATURE_INSERT) {
			if (lastP.x == scrCoord.x || lastP.y == scrCoord.y) {
				redraw();
				return;
			}

			DPoint last = offscrG.transPointT(lastP);
			DRectangle r = new DRectangle(Math.min(last.x, p.x), Math.min(last.y, p.y),
					Math.max(p.x, last.x) - Math.min(last.x, p.x), Math.max(last.y, p.y) - Math.min(last.y, p.y));
			pstruct.MessageStart();
			if (pstruct.Insert(r))
				historyList.addElement(r);
			pstruct.MessageEnd();
			redraw();

		}

//      if (op.equals("Move")) {
//  	moveRectangle(p);
//          lastDelete = null;
//          if (lastInsert != null) {
//              historyList.addElement(lastInsert);
//              redraw();
//          }
//      }
	}
	// ----------------------

}
