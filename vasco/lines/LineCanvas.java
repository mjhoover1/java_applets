package vasco.lines;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/* $Id: LineCanvas.java,v 1.6 2007/10/28 15:38:16 jagan Exp $ */
import vasco.common.ColorHelp;
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingCanvas;
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

public class LineCanvas extends GenericCanvas implements FileIface, ItemListener {
	class DeletePoint extends DLine {
		DeletePoint(double xx, double yy) {
			super(xx, yy, 0, 0);
		}

		DeletePoint(DPoint p) {
			super(p, new DPoint(0, 0));
		}

		public DPoint getPoint() {
			return p1;
		}

		@Override
		public String toString() {
			return "DeletePoint" + p1.toString();
		}

	}

	class DeleteLine extends DLine {
		DeleteLine(double x1, double y1, double x2, double y2) {
			super(x1, y1, x2, y2);
		}

		DeleteLine(DLine p) {
			super(p.p1, p.p2);
		}

		@Override
		public String toString() {
			return "DeleteLine" + super.toString();
		}

	}

	LineStructure[] pstrs;
	public LineStructure pstruct;
	Drawable lastClosest; // Represents the last closest Point


	public LineCanvas(DRectangle can, DrawingTarget dt, DrawingTarget over, JPanel animp, TopInterface ti) {
		super(can, dt, over, animp, ti);
		pstrs = new LineStructure[6];

		opFeature = new OpFeature[9];
		opFeature[0] = new OpFeature("Insert", OPFEATURE_INSERT,
				"Click and drag to insert a new line. To snap endpoints: Keep finger on control when depressing and releasing mouse.",
				"Insert new line", "", "", InputEvent.BUTTON1_MASK);

		opFeature[1] = new OpFeature("Move", OPFEATURE_MOVE,
				"Click and drag to move the nearest line along with all the lines that share its endpoints.",
				"Move existing line", "", "", InputEvent.BUTTON1_MASK);

		opFeature[2] = new OpFeature("Move vertex", OPFEATURE_MOVEVERTEX,
				"Click and drag to move the nearest vertex along with all lines that share this vertex.  Press Control while dragging to snap to the nearest vertex.",
				"Move existing vertex (use Control to snap)", "", "", InputEvent.BUTTON1_MASK);

		opFeature[3] = new OpFeature("Move collection", OPFEATURE_MOVECOLLECTION, "Click and drag to move all lines.",
				"Move all lines", "", "", InputEvent.BUTTON1_MASK);
		opFeature[4] = new OpFeature("Rotate collection", OPFEATURE_ROTATECOLLECTION,
				"Click and drag to rotate all lines.", "Rotate all lines", "", "", InputEvent.BUTTON1_MASK);

		opFeature[5] = new OpFeature("Delete", OPFEATURE_DELETE, "Click to erase the nearest line.",
				"Delete nearest line", "", "", InputEvent.BUTTON1_MASK);

		OpFeature[] opf = new OpFeature[5];
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
				"Click repeatedly to specify vertices of a query path. Click middle button to snap to the first vertex.  Click right button to insert the last vertex.",
				"Input new query path vertex", "Snap to first vertex", "Input final query path vertex",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
		opf[4] = new OpFeature("Sector", QueryObject.QO_SECTOR, "Define a rooted sector as the query object.",
				"Input root of query sector", "Input starting angle", "Input extent of sector",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);

		opFeature[6] = new OpFeatures("Overlap", OPFEATURE_WINDOW, opf, withinStats);
		opFeature[7] = new OpFeatures("Nearest", OPFEATURE_NEAREST, opf, withinStats);
		opFeature[8] = new OpFeatures("Within", OPFEATURE_WITHIN, opf, withinStats);
	}

	@Override
	public void initStructs() {
		pstrs[0] = new PM1(wholeCanvas, 9, topInterface, this);
		pstrs[1] = new PM2(wholeCanvas, 9, topInterface, this);
		pstrs[2] = new PM3(wholeCanvas, 9, topInterface, this);
		pstrs[3] = new PMR(wholeCanvas, 9, 3, topInterface, this);
		pstrs[4] = new PMbucket(wholeCanvas, 9, 3, topInterface, this);
		pstrs[5] = new LineRTree(wholeCanvas, topInterface, this);
	}

	@Override
	public int getAppletType() {
		return ColorHelp.LINE_APPLET;
	}

	@Override
	public int getStructCount() {
		return pstrs.length;
	}

	@Override
	public String getStructName(int i) {
		return pstrs[i].getName();
	}

	@Override
	public String getCurrentName() {
		return pstruct.getName();
	}

	@Override
	public fileSelector getFileSelector(String op) {
		return new LineFileSelector(this, op, topInterface);
	}

	@Override
	public void clear() {
		super.clear();
		((DrawingCanvas) offscrG).clearOvals(); // Added to remove last yellow rectangle
		((DrawingCanvas) offscrG).clearLines(); // Added to remove last yellow rectangle
		pstruct.MessageStart();
		pstruct.Clear();
		pstruct.MessageEnd();
	}

	@Override
	protected String getCurrentOperationName() {
		return pstruct.getCurrentOperation();
	}

	/* ------------- file load / save ------------ */

	@Override
	public Vector vectorOut() {
		Vector ret = new Vector();
		for (int i = 0; i < historyList.size(); i++) {
			DLine pt = (DLine) historyList.elementAt(i);
			if (pt instanceof DeleteLine) {
				for (int j = 0; j < ret.size(); j++) {
					if (((DLine) ret.elementAt(j)).equals(pt)) {
						ret.removeElementAt(j);
						break;
					}
				}
				// System.out.println(((DeleteLine)pt).toString());
			} else if (pt instanceof DeletePoint) {
				DLine min = (DLine) ret.elementAt(0);
				DPoint searchFor = ((DeletePoint) pt).getPoint();
				double[] dist = new double[2];
				searchFor.distance(min, dist);

				for (int j = 1; j < ret.size(); j++) {
					DLine p = (DLine) ret.elementAt(j);
					double[] d = new double[2];
					searchFor.distance(p, d);
					if (d[0] < dist[0] || (d[0] == dist[0] && d[1] < dist[1])) {
						dist = d;
						min = p;
					}
				}
				ret.removeElement(min);
			} else {
				ret.addElement(pt);
				// System.out.println(pt.toString());
			}
		}
		return ret;
	}

	@Override
	public String[] stringsOut() {
		Vector in = vectorOut();
		String[] out = new String[in.size()];
		for (int i = 0; i < in.size(); i++) {
			DLine er = (DLine) in.elementAt(i);
			out[i] = new String(er.p1.x + " " + er.p1.y + " " + er.p2.x + " " + er.p2.y);
		}
		return out;
	}

	@Override
	public void vectorIn(Vector p) { /* vector of Lines */
		pstruct.MessageStart();
		pstruct.Clear();
		historyList = p;
		for (int i = 0; i < p.size(); i++) {
			DLine pt = (DLine) p.elementAt(i);
			pstruct.Insert(pt);
		}
		pstruct.MessageEnd();
		redraw();
	}

	/* ----- drawing utilities ----------- */

	@Override
	public void drawContents(DrawingTarget g) {
		pstruct.drawContents(g, g.getView());
	}

	@Override
	public void drawGrid(DrawingTarget g) {
		super.drawGrid(g);
		if (gridOn)
			pstruct.drawGrid(g, gridLevel);
	}

	/* --------------- operations on structures ------------------- */

	@Override
	protected void search(QueryObject searchRect, DrawingTarget[] off) {
		SearchVector v;
		v = pstruct.Search(searchRect, searchMode);
		if (runningThread == null) {
			runningThread = new SearchThread(v, searchRect, this, off);
		}
	}

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

	@Override
	protected int getAllowedOverlapQueryObjects() {
		return QueryObject.QO_RECTANGLE | QueryObject.QO_POLYGON | QueryObject.QO_LINE | QueryObject.QO_PATH
				| QueryObject.QO_SECTOR;
	}

	@Override
	protected int getSearchModeMask() {
		return SEARCHMODE_CONTAINS | SEARCHMODE_OVERLAPS | SEARCHMODE_CROSSES;
	}

	/* --------------- operations on structures ------------------- */

	boolean isChange(int index, int[] test) { // to test if element 'i' from 'lines' is in array 'test'
		if (test == null)
			return false;
		for (int element : test)
			if (index == element)
				return true;
		return false;
	}

	void updateFromParams() {
		updateFromParams(null);
	}

	boolean updateFromParams(int[] test) {
		boolean ok = true;
		pstruct.MessageStart();
		pstruct.Clear();
		for (int i = 0; i < historyList.size(); i++) {
			DLine p = (DLine) historyList.elementAt(i);
			if (p instanceof DeletePoint)
				pstruct.Delete(((DeletePoint) p).getPoint());
			else if (p instanceof DeleteLine)
				pstruct.DeleteDirect(p);
			else {
				if (!pstruct.Insert(p) && isChange(i, test)) {
					ok = false;
					break;
				}
			}

		}
		pstruct.MessageEnd();
		return ok;
	}

	@Override
	protected void setHelp() {
		super.setHelp();
		String help = "";
		if (getCurrentOperation() == OPFEATURE_WINDOW) {
			Vector hs = new Vector();
			if ((searchMode & SEARCHMODE_CONTAINS) != 0)
				hs.addElement("are completely inside the query area");
			if ((searchMode & SEARCHMODE_CROSSES) != 0)
				hs.addElement("intersect the query area, but don't have its endpoints inside");
			if ((searchMode & SEARCHMODE_OVERLAPS) != 0)
				hs.addElement("intersect the query area and have one endpoint inside");

			help += "Lines that ";
			for (int j = 0; j < hs.size(); j++) {
				help += (String) hs.elementAt(j);
				if (j < hs.size() - 2)
					help += ", ";
				else if (j == hs.size() - 2)
					help += " or ";
			}
			help += " are returned.";

			if (searchMode == 0)
				help = "No lines are returned.";
			topInterface.getHelpArea().append("\n" + Tools.formatHelp(help, topInterface.getHelpArea().getColumns()));
		}
	}

	@Override
	public void setTree(int i, JComboBox<String> ops) {
	    pstruct = pstrs[i];
	    // Temporarily remove item listeners to prevent triggering events
	    ItemListener[] listeners = ops.getItemListeners();
	    for (ItemListener listener : listeners) {
	        ops.removeItemListener(listener);
	    }

	    ops.removeAllItems();
	    pstruct.reInit(ops);

	    // Re-add item listeners
	    for (ItemListener listener : listeners) {
	        ops.addItemListener(listener);
	    }

	    // Set the selected item only if there are items in the JComboBox
	    if (ops.getItemCount() > 0) {
	        String op = (String) ops.getSelectedItem();
	        if (op == null) {
	            op = "Insert";
	        }
	        ops.setSelectedItem(op);
	        setHelp();  // Call setHelp only if JComboBox is not empty
	    }

	    rebuild();
	}

	@Override
	public void rebuild() {
		pstruct.Clear();
		updateFromParams();
		if (runningThread != null) {
			terminate();
			if ((getCurrentOperation() == OPFEATURE_NEAREST || getCurrentOperation() == OPFEATURE_WITHIN)
					&& lastNear != null)
				nearest(lastNear, withinStats.getDist(), allDrawingTargets);
			else if ((getCurrentOperation() == OPFEATURE_WINDOW) && lastWindow != null)
				search(lastWindow, allDrawingTargets);
		}
		redraw();
	}

	// ---------------- MouseListener && MouseMotionListener -------------

	DPoint lastInsert;
	DPoint moveStart;
	Drawable lastDelete;

	class moveLines {
		DLine line; // joint in first vertex
		int index; // indeces
	}

	moveLines[] lastMove;
	// DLine[] lastMoveV;

	@Override
	public void mouseEntered(MouseEvent me) {
		System.out.println("mouseEntered");

		super.mouseEntered(me);
		lastDelete = null;
	}

	@Override
	public void mouseExited(MouseEvent me) {
		System.out.println("mouseExited");

		super.mouseExited(me);
		if (lastDelete != null) {
			lastDelete.directDraw(Color.red, offscrG);
			lastDelete = null;
		}
		if (getCurrentOperation() == OPFEATURE_INSERT && lastInsert != null) {
			redraw();
			lastInsert = null;
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		System.out.println("mouseMoved");
		super.mouseMoved(me);
		DPoint p = offscrG.transPointT(offscrG.adjustPoint(me.getPoint()));
		int op = getCurrentOperation();

		if (op == OPFEATURE_INSERT || op == OPFEATURE_MOVEVERTEX) {
			DPoint fP = pstruct.NearestPoint(p);
			if (lastInsert != null && (!lastInsert.equals(fP)))
				redraw();
			
	        // Check if the current nearest drawable object is different from the last one
	        if (lastClosest == null || !lastClosest.equals(fP)) {
	            // Only update if there is a change in the nearest object

				if (fP != null) {
	        		((DrawingCanvas) offscrG).clearOvals(Color.orange); // Added to remove last yellow rectangle
					fP.directDraw(Color.orange, offscrG);
				}
				lastInsert = fP;
	            System.out.println("Drawable object updated");
	        }
	        lastClosest = fP;
		
			redraw();
		}

		if (op == OPFEATURE_MOVECOLLECTION || op == OPFEATURE_ROTATECOLLECTION) {
			lastInsert = p;
		}

		if (op == OPFEATURE_DELETE || op == OPFEATURE_MOVE) {
			Drawable b = pstruct.NearestFirst(new QueryObject(p));
			mouseExited(me);
			if (b != null) {
				b.directDraw(Color.orange, offscrG);
				lastDelete = b;
			} else {
				lastDelete = null;
			}
			redraw();
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		System.out.println("mouseClicked");
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button
		super.mouseClicked(me);
	}

	@Override
	public void mousePressed(MouseEvent me) {
		System.out.println("mousePressed");
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button

		super.mousePressed(me);

		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);

		moveStart = p;

		int op = getCurrentOperation();

		if (op == OPFEATURE_INSERT) {
			if (me.isControlDown()) {
				DPoint fP = pstruct.NearestPoint(p);
				if (fP != null)
					scrCoord = offscrG.transPoint(fP.x, fP.y);
			}
			lastP = scrCoord;
			redraw();
		}

		if (op == OPFEATURE_MOVE) {
			lastMove = null;
			if (lastDelete != null) {
				DLine dl = (DLine) lastDelete;
				pstruct.MessageStart();
				pstruct.DeleteDirect(dl);
				Drawable[] drw1 = pstruct.NearestRange(new QueryObject(dl.p1), 0);
				Drawable[] drw2 = pstruct.NearestRange(new QueryObject(dl.p2), 0);
				pstruct.Insert(dl);
				pstruct.MessageEnd();
				lastMove = new moveLines[((drw1 != null) ? drw1.length : 0) + ((drw2 != null) ? drw2.length : 0) + 1];
				lastMove[0] = new moveLines();
				lastMove[0].line = dl; // lastMove[0] is the moved line

				int k = 1;
				if (drw1 != null) {
					for (Drawable element : drw1) {
						lastMove[k] = new moveLines();
						lastMove[k].line = (DLine) (element);
						k++;
					}
				}
				if (drw2 != null) {
					for (Drawable element : drw2) {
						lastMove[k] = new moveLines();
						lastMove[k].line = (DLine) (element);
						k++;
					}
				}

				for (int i = 0; i < lastMove.length; i++) {
					if (i != 0 && (lastMove[i].line.p2.equals(dl.p1) || lastMove[i].line.p2.equals(dl.p2)))
						lastMove[i].line = new DLine(lastMove[i].line.p2, lastMove[i].line.p1);
					// swap p1 and p2 so that p1 is the shared vertex
					for (int j = historyList.size() - 1; j >= 0; j--) {
						if (lastMove[i].line.equals((DLine) historyList.elementAt(j))) {
							lastMove[i].index = j;
							break;
						}
					}
				}
			}
			redraw();
		}

		if (op == OPFEATURE_MOVEVERTEX) {
			lastMove = null;
			if (lastInsert != null) {
				Drawable[] drw = pstruct.NearestRange(new QueryObject(lastInsert), 0);
				if (drw != null && drw.length != 0) {
					lastMove = new moveLines[drw.length];
					for (int i = 0; i < drw.length; i++) {
						lastMove[i] = new moveLines();
						lastMove[i].line = (DLine) (drw[i]);
						for (int j = historyList.size() - 1; j >= 0; j--)
							if (lastMove[i].line.equals((DLine) historyList.elementAt(j))) {
								lastMove[i].index = j;
								break;
							}

					}
				}
			}
			redraw();
		}

		if (op == OPFEATURE_MOVECOLLECTION || op == OPFEATURE_ROTATECOLLECTION) {
			lastMove = null;
			if (lastInsert != null) {
				Drawable[] drw = pstruct.NearestRange(new QueryObject(lastInsert), 9999);
				System.out.println("lines collected: " + drw.length);
				if (drw != null && drw.length != 0) {
					lastMove = new moveLines[drw.length];
					for (int i = 0; i < drw.length; i++) {
						lastMove[i] = new moveLines();
						lastMove[i].line = (DLine) (drw[i]);
						for (int j = historyList.size() - 1; j >= 0; j--)
							if (lastMove[i].line.equals((DLine) historyList.elementAt(j))) {
								lastMove[i].index = j;
								break;
							}

					}
				}
			}
			redraw();
		}

		if (op == OPFEATURE_DELETE) {
			pstruct.MessageStart();
			pstruct.Delete(p);
			historyList.addElement(new DeletePoint(p));
			lastDelete = null;
			pstruct.MessageEnd();
			redraw();
		}

		mouseDragged(me); // experimental call to assure if the mouse is clicked, it's also dragged
	}

	@Override
	synchronized public void mouseDragged(MouseEvent me) {
		System.out.println("mouseDragged");
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button
		super.mouseDragged(me);
		int op = getCurrentOperation();
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);

		if (op == OPFEATURE_INSERT) {
			DPoint last = offscrG.transPointT(lastP);
			offscrG.redraw();

			DPoint fP = pstruct.NearestPoint(p);
			// if (lastInsert != null && (!lastInsert.equals(fP)))
			// redraw();
//			if (fP != null) {
//				fP.directDraw(Color.orange, offscrG);
//			}
			lastInsert = fP;

			offscrG.directLine(Color.orange, last.x, last.y, p.x, p.y);
			redraw();
		}

		if (op == OPFEATURE_MOVE && lastMove != null && lastMove.length != 0) {
			double transX = p.x - moveStart.x;
			double transY = p.y - moveStart.y;
			moveStart = p;
			DPoint np1 = new DPoint(lastMove[0].line.p1.x + transX, lastMove[0].line.p1.y + transY);
			DPoint np2 = new DPoint(lastMove[0].line.p2.x + transX, lastMove[0].line.p2.y + transY);

			DLine[] oldset = new DLine[lastMove.length];
			for (int i = 0; i < lastMove.length; i++)
				oldset[i] = lastMove[i].line;

			if (wholeCanvas.contains(np1) && wholeCanvas.contains(np2)) {
				for (int i = 1; i < lastMove.length; i++) {
					if (lastMove[i].line.p1.equals(lastMove[0].line.p1))
						lastMove[i].line = new DLine(np1, lastMove[i].line.p2);
					else if (lastMove[i].line.p1.equals(lastMove[0].line.p2))
						lastMove[i].line = new DLine(np2, lastMove[i].line.p2);
					else if (lastMove[i].line.p2.equals(lastMove[0].line.p1))
						lastMove[i].line = new DLine(lastMove[i].line.p1, np1);
					else if (lastMove[i].line.p2.equals(lastMove[0].line.p2))
						lastMove[i].line = new DLine(lastMove[i].line.p1, np2);
					else
						System.err.println("Move vertex: vertex no endpoint of line");
				}
				lastMove[0].line = new DLine(np1, np2);
				if (!pstruct.orderDependent()) {
					pstruct.MessageStart();
					for (int i = 0; i < lastMove.length; i++)
						pstruct.DeleteDirect(oldset[i]);
					for (int i = 0; i < lastMove.length; i++)
						if (!pstruct.Insert(lastMove[i].line)) {
							for (int k = i - 1; k >= 0; k--)
								pstruct.DeleteDirect(lastMove[k].line);
							for (int k = 0; k < lastMove.length; k++) {
								pstruct.Insert(oldset[k]);
								lastMove[k].line = oldset[k];
							}
							break;
						}
					pstruct.MessageEnd();
					for (moveLines element : lastMove)
						historyList.setElementAt(element.line, element.index);
				} else {
					int[] test = new int[lastMove.length];
					for (int i = 0; i < lastMove.length; i++) {
						historyList.setElementAt(lastMove[i].line, lastMove[i].index);
						test[i] = lastMove[i].index;
					}
					if (!updateFromParams(test)) {
						for (int i = 0; i < lastMove.length; i++) {
							lastMove[i].line = oldset[i];
							historyList.setElementAt(lastMove[i].line, lastMove[i].index);
						}
						updateFromParams();
					}
				}
//				redraw();
				lastMove[0].line.directDraw(Color.orange, offscrG);
				lastDelete = lastMove[0].line;
			}
			redraw();
		}

		if (op == OPFEATURE_MOVEVERTEX && lastMove != null && lastMove.length != 0) {
			if (me.isControlDown()) {
				DPoint fP = pstruct.NearestPoint(p);
				if (fP != null)
					p = fP;
			}

			DLine[] oldset = new DLine[lastMove.length];
			for (int i = 0; i < lastMove.length; i++) {
				oldset[i] = lastMove[i].line;
				if (lastMove[i].line.p1.equals(lastInsert))
					lastMove[i].line = new DLine(p, lastMove[i].line.p2);
				else if (lastMove[i].line.p2.equals(lastInsert))
					lastMove[i].line = new DLine(lastMove[i].line.p1, p);
				else {
					System.err.println(lastInsert.toString());
					System.err.println("Move vertex: vertex no endpoint of line");
				}
			}
			if (!pstruct.orderDependent()) {
				pstruct.MessageStart();
				for (int i = 0; i < lastMove.length; i++)
					pstruct.DeleteDirect(oldset[i]);
				for (int i = 0; i < lastMove.length; i++)
					if (!pstruct.Insert(lastMove[i].line)) {
						for (int k = i - 1; k >= 0; k--)
							pstruct.DeleteDirect(lastMove[k].line);
						for (int k = 0; k < lastMove.length; k++) {
							pstruct.Insert(oldset[k]);
							lastMove[k].line = oldset[k];
						}
						p = lastInsert;
						break;
					}
				pstruct.MessageEnd();
				for (moveLines element : lastMove)
					historyList.setElementAt(element.line, element.index);
			} else {
				int[] test = new int[lastMove.length];
				for (int i = 0; i < lastMove.length; i++) {
					historyList.setElementAt(lastMove[i].line, lastMove[i].index);
					test[i] = lastMove[i].index;
				}
				if (!updateFromParams(test)) {
					for (int i = 0; i < lastMove.length; i++) {
						lastMove[i].line = oldset[i];
						historyList.setElementAt(lastMove[i].line, lastMove[i].index);
					}
					System.out.println("update fails");
					p = lastInsert;
					updateFromParams();
				}
			}
			lastInsert = p;
			p.directDraw(Color.orange, offscrG);
			redraw();
		}

		if (op == OPFEATURE_MOVECOLLECTION && lastMove != null && lastMove.length != 0) {
			double transX = p.x - moveStart.x;
			double transY = p.y - moveStart.y;
			moveStart = p;

			DLine[] oldset = new DLine[lastMove.length];
			for (int i = 0; i < lastMove.length; i++)
				oldset[i] = lastMove[i].line;

			boolean allvalid = true;
			for (int i = 0; i < lastMove.length; i++) {
				DPoint p1 = new DPoint(lastMove[i].line.p1.x + transX, lastMove[i].line.p1.y + transY);
				DPoint p2 = new DPoint(lastMove[i].line.p2.x + transX, lastMove[i].line.p2.y + transY);

				lastMove[i].line = new DLine(p1, p2);
				if (!wholeCanvas.contains(p1) || !wholeCanvas.contains(p2)) {
					allvalid = false;
					break;
				}
			}

			if (allvalid) {
				if (!pstruct.orderDependent()) {
					pstruct.MessageStart();
					for (int i = 0; i < lastMove.length; i++)
						pstruct.DeleteDirect(oldset[i]);
					for (int i = 0; i < lastMove.length; i++)
						if (!pstruct.Insert(lastMove[i].line)) {
							for (int k = i - 1; k >= 0; k--)
								pstruct.DeleteDirect(lastMove[k].line);
							for (int k = 0; k < lastMove.length; k++) {
								pstruct.Insert(oldset[k]);
								lastMove[k].line = oldset[k];
							}
							break;
						}
					pstruct.MessageEnd();
					for (moveLines element : lastMove)
						historyList.setElementAt(element.line, element.index);
				} else {
					int[] test = new int[lastMove.length];
					for (int i = 0; i < lastMove.length; i++) {
						historyList.setElementAt(lastMove[i].line, lastMove[i].index);
						test[i] = lastMove[i].index;
					}
					if (!updateFromParams(test)) {
						for (int i = 0; i < lastMove.length; i++) {
							lastMove[i].line = oldset[i];
							historyList.setElementAt(lastMove[i].line, lastMove[i].index);
						}
						updateFromParams();
					}
				}
//				redraw();
			} else {
				for (int i = 0; i < lastMove.length; i++)
					lastMove[i].line = oldset[i];
			}
			redraw();
		}

		if (op == OPFEATURE_ROTATECOLLECTION && lastMove != null && lastMove.length != 0) {

			double angleDiff = (lastInsert.y > p.y) ? -Math.PI / 90 : Math.PI / 90;
			lastInsert = p;

			DLine[] oldset = new DLine[lastMove.length];
			for (int i = 0; i < lastMove.length; i++)
				oldset[i] = lastMove[i].line;

			boolean allvalid = true;
			for (int i = 0; i < lastMove.length; i++) {

				DPoint tp1 = new DPoint(lastMove[i].line.p1.x - moveStart.x, lastMove[i].line.p1.y - moveStart.y);
				DPoint p1 = new DPoint(moveStart.x + (tp1.x * Math.cos(angleDiff) + tp1.y * Math.sin(angleDiff)),
						moveStart.y + (-tp1.x * Math.sin(angleDiff) + tp1.y * Math.cos(angleDiff)));
				DPoint tp2 = new DPoint(lastMove[i].line.p2.x - moveStart.x, lastMove[i].line.p2.y - moveStart.y);
				DPoint p2 = new DPoint(moveStart.x + (tp2.x * Math.cos(angleDiff) + tp2.y * Math.sin(angleDiff)),
						moveStart.y + (-tp2.x * Math.sin(angleDiff) + tp2.y * Math.cos(angleDiff)));

				lastMove[i].line = new DLine(p1, p2);
				if (!wholeCanvas.contains(p1) || !wholeCanvas.contains(p2)) {
					allvalid = false;
					break;
				}
			}

			if (allvalid) {
				if (!pstruct.orderDependent()) {
					pstruct.MessageStart();
					for (int i = 0; i < lastMove.length; i++)
						pstruct.DeleteDirect(oldset[i]);
					for (int i = 0; i < lastMove.length; i++)
						if (!pstruct.Insert(lastMove[i].line)) {
							for (int k = i - 1; k >= 0; k--)
								pstruct.DeleteDirect(lastMove[k].line);
							for (int k = 0; k < lastMove.length; k++) {
								pstruct.Insert(oldset[k]);
								lastMove[k].line = oldset[k];
							}
							break;
						}
					pstruct.MessageEnd();
					for (moveLines element : lastMove)
						historyList.setElementAt(element.line, element.index);
				} else {
					int[] test = new int[lastMove.length];
					for (int i = 0; i < lastMove.length; i++) {
						historyList.setElementAt(lastMove[i].line, lastMove[i].index);
						test[i] = lastMove[i].index;
					}
					if (!updateFromParams(test)) {
						for (int i = 0; i < lastMove.length; i++) {
							lastMove[i].line = oldset[i];
							historyList.setElementAt(lastMove[i].line, lastMove[i].index);
						}
						updateFromParams();
					}
				}
//				redraw();
			} else {
				for (int i = 0; i < lastMove.length; i++)
					lastMove[i].line = oldset[i];
			}
			redraw();
		}

	}

	@Override
	public void mouseReleased(MouseEvent me) {
		System.out.println("mouseReleased");
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button
		super.mouseReleased(me);
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);

		int op = getCurrentOperation();

		if (op == OPFEATURE_MOVEVERTEX) {
			redraw();
			lastInsert = null;
		}

		if (op == OPFEATURE_INSERT) {
			DPoint last = offscrG.transPointT(lastP);
			pstruct.MessageStart();
			if (me.isControlDown()) {
				DPoint fP = pstruct.NearestPoint(p);
				if (fP != null)
					p = fP;
			}

			DLine l;
			if (last.x < p.x || (last.x == p.x && last.y < p.y))
				l = new DLine(last.x, last.y, p.x, p.y);
			else
				l = new DLine(p.x, p.y, last.x, last.y);

			// DLine l = new DLine(last.x, last.y, p.x, p.y);
			if (!(last.x == p.x && last.y == p.y) && pstruct.Insert(l))
				historyList.addElement(l);
			else {
				String errMesg = (last.x == p.x && last.y == p.y) ? "Isolated vertex rejected"
						: "Subdivision too deep, line rejected";
				// line rejected message
				Tools.errorMessage(errMesg);
			}

			pstruct.MessageEnd();
			((DrawingCanvas) offscrG).clearLines();
			redraw();
		}

	}
//----------------------

}
