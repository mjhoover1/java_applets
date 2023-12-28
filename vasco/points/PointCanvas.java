package vasco.points;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
/* $Id: PointCanvas.java,v 1.3 2007/10/28 15:38:18 jagan Exp $ */
// import java.awt.Choice;
// import java.awt.Color;
// import java.awt.Panel;
// import java.awt.Point;
// import java.awt.event.InputEvent;
// import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

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
import vasco.common.RebuildTree;
import vasco.common.SearchThread;
import vasco.common.SearchVector;
import vasco.common.SpatialStructure;
import vasco.common.TopInterface;
import vasco.common.fileSelector;
import vasco.drawable.Drawable;
import vasco.lines.LineCanvas;
import vasco.lines.LineStructure;
import vasco.points.randy.ApproximateVoronoiDiagram;
import vasco.points.randy.DelaunayTriangulation;
import vasco.points.randy.Indexable;
import vasco.points.randy.VoronoiDiagram;
import vasco.regions.RasterStructure;
import vasco.regions.RegionCanvas;
import vasco.regions.RegionStructure;

// ------------- Point Canvas -------------------

public class PointCanvas extends GenericCanvas implements FileIface {
	PointStructure[] pstrs;
	public SpatialStructure pstruct;
	boolean usingLines = false;
	boolean usingRegions = false;

	public DRectangle can;
	public DrawingTarget dt;
	public JPanel animp;
	public TopInterface ti;
	
	Drawable lastClosest; // Represents the last closest Point


	PointCanvas(DRectangle can, DrawingTarget dt, DrawingTarget overview, JPanel animp, TopInterface ti) {
		super(can, dt, overview, animp, ti);
		this.can = can;
		this.dt = dt;
		this.animp = animp;
		this.ti = ti;

		pstrs = new PointStructure[16];

		opFeature = new OpFeature[10];
		opFeature[0] = new OpFeature("Insert", OPFEATURE_INSERT, "Click to insert a new point.", "Insert new point", "",
				"", InputEvent.BUTTON1_MASK);
		opFeature[1] = new OpFeature("Move", OPFEATURE_MOVE,
				"Click and drag to move the closest point to its new location.", "Move existing point", "", "",
				InputEvent.BUTTON1_MASK);
		opFeature[2] = new OpFeature("Delete", OPFEATURE_DELETE, "Click to erase the nearest point.",
				"Delete nearest point", "", "", InputEvent.BUTTON1_MASK);

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
				"Click repeatedly to specify vertices of a query path.  Click middle button to snap to the first vertex. Click right button to insert the last vertex.",
				"Input new query path vertex", "Snap to first vertex", "Input final query path vertex",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
		opf[4] = new OpFeature("Sector", QueryObject.QO_SECTOR, "Define a rooted sector as the query object.",
				"Input root of query sector", "Input starting angle", "Input extent of sector",
				InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);

		opFeature[3] = new OpFeatures("Overlap", OPFEATURE_WINDOW, opf, withinStats);
		opFeature[4] = new OpFeatures("Nearest", OPFEATURE_NEAREST, opf, withinStats);
		opFeature[5] = new OpFeatures("Within", OPFEATURE_WITHIN, opf, withinStats);
		opFeature[6] = new OpFeature("Nearest Site", OPFEATURE_NEAREST_SITE,
				"Click to find the nearest site to the mouse.", "Find nearest site", "", "", InputEvent.BUTTON1_MASK);
		opFeature[7] = new OpFeature("Line Index", OPFEATURE_LINE_INDEX,
				"Click to construct an index on the lines of the structure", "Construct index", "", "",
				InputEvent.BUTTON1_MASK);
		opFeature[8] = new OpFeature("Vertex Index", OPFEATURE_VERTEX_INDEX,
				"Click to use the vertices of the Voronoi Diagram as data points instead of the sites",
				"Construct index", "", "", InputEvent.BUTTON1_MASK);
		opFeature[9] = new OpFeature("Nearest Vertex", OPFEATURE_NEAREST_SITE,
				"Click to find the nearest vertex to the mouse.", "Find nearest vertex", "", "",
				InputEvent.BUTTON1_MASK);
	}

	@Override
	public void initStructs() {
		pstrs[0] = new KDTree(wholeCanvas, topInterface, this);
		pstrs[1] = new MXTree(wholeCanvas, 9, topInterface, this);
		pstrs[2] = new PR(wholeCanvas, 9, topInterface, this);
		pstrs[3] = new PTTree(wholeCanvas, topInterface, this);
		pstrs[4] = new PRkd(wholeCanvas, 18, topInterface, this);
		pstrs[5] = new PRbucket(wholeCanvas, 3, 9, topInterface, this);
		pstrs[6] = new PRkdbucket(wholeCanvas, 3, 18, topInterface, this);
		pstrs[7] = new PMR(wholeCanvas, 3, 9, topInterface, this);
		pstrs[8] = new PMRkd(wholeCanvas, 3, 18, topInterface, this);
		pstrs[9] = new RangeTree(wholeCanvas, topInterface, this);
		pstrs[10] = new PriorityTree(wholeCanvas, topInterface, this);
		pstrs[11] = new PointRTree(wholeCanvas, topInterface, this);
		pstrs[12] = new PKTree(wholeCanvas, 8, 5, topInterface, this);
		pstrs[13] = new VoronoiDiagram(wholeCanvas, topInterface, this, this);
		pstrs[14] = new ApproximateVoronoiDiagram(wholeCanvas, .5, 3, topInterface, this, this);
		pstrs[15] = new DelaunayTriangulation(wholeCanvas, topInterface, this);
	}

	@Override
	public int getAppletType() {
		return ColorHelp.POINT_APPLET;
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
		return new PointFileSelector(this, op, topInterface);
	}

	@Override
	public void clear() {
		super.clear();
		
		((DrawingCanvas) offscrG).clearOvals(); // Added to remove last yellow rectangle
		
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
			DPoint pt = (DPoint) historyList.elementAt(i);
			if (pt instanceof DeletePoint) {
				DPoint min = (DPoint) ret.elementAt(0);
				double dist = pt.distance(min);

				for (int j = 1; j < ret.size(); j++) {
					DPoint p = (DPoint) ret.elementAt(j);
					double d = pt.distance(p);
					if (d < dist) {
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

	@Override
	public String[] stringsOut() {
		Vector in = vectorOut();
		String[] out = new String[in.size()];
		for (int i = 0; i < in.size(); i++) {
			DPoint er = (DPoint) in.elementAt(i);
			out[i] = new String(er.x + " " + er.y);
		}
		return out;
	}

	@Override
	public void vectorIn(Vector p) { /* vector of points */
		pstruct.MessageStart();
		pstruct.Clear();
		historyList = p;
		for (int i = 0; i < p.size(); i++) {
			DPoint pt = (DPoint) p.elementAt(i);
			pstruct.Insert(pt);
		}
		pstruct.MessageEnd();
		redraw();
	}

	/* ----- drawing utilities ----------- */

	@Override
	public void drawContents(DrawingTarget g) {
		if (pstruct != null && g != null)
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
	protected void search(QueryObject s, DrawingTarget[] off) {
		SearchVector v;
		v = pstruct.Search(s, searchMode);
		if (runningThread == null) {
			runningThread = new SearchThread(v, s, this, off);
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

	void updateFromParams() {
		updateFromParams(null);
	}

	boolean updateFromParams(DPoint newIns) {
		boolean ok = true;
		pstruct.MessageStart();
		pstruct.Clear();
		for (int i = 0; i < historyList.size(); i++) {
			DPoint p = (DPoint) historyList.elementAt(i);
			if (p instanceof DeletePoint)
				pstruct.Delete(p);
			else if (!pstruct.Insert(p) && p.equals(newIns)) {
				ok = false;
				break;
			}
		}
		pstruct.MessageEnd();
		return ok;
	}

	@Override
	protected int getSearchModeMask() {
		return SEARCHMODE_CONTAINS;
	}

	@Override
	protected int getAllowedOverlapQueryObjects() {
		return QueryObject.QO_RECTANGLE | QueryObject.QO_POLYGON | QueryObject.QO_SECTOR;
	}

	GenericCanvas handler = null;

	public void setHandler(GenericCanvas handler) {
		this.handler = handler;
	}

	public void useLineStruct(LineStructure p, JComboBox<String> ops) {
		LineCanvas lc = new LineCanvas(can, dt, overview, animp, ti);
		lc.pstruct = p;
		setHandler(lc);
		pstruct = p;
		// TODO Figure out if this needs to be changed to removeAllItems
		// Temporarily remove item listeners to prevent triggering events
	    // ItemListener[] listeners = ops.getItemListeners();
	    // for (ItemListener listener : listeners) {
	    //     ops.removeItemListener(listener);
	    // }

		// ops.removeAllItems();
		// pstruct.reInit(ops);
		
	    // // Re-add item listeners
	    // for (ItemListener listener : listeners) {
	    //     ops.addItemListener(listener);
	    // }
		ops.removeAll();
		pstruct.reInit(ops);
		setHelp();
		rebuild();
	}

	public void useRegionStruct(RebuildTree r, JComboBox<String> ops) {
		RegionCanvas lc = new RegionCanvas(can, dt, overview, animp, ti);
		RegionStructure p = new RasterStructure(this, can, dt, ti, r, lc.grid);
		lc.pstruct = p;
		setHandler(lc);
		pstruct = p;
		// TODO Figure out if this needs to be changed to removeAllItems
		// Temporarily remove item listeners to prevent triggering events
	    // ItemListener[] listeners = ops.getItemListeners();
	    // for (ItemListener listener : listeners) {
	    //     ops.removeItemListener(listener);
	    // }

		// ops.removeAllItems();
		// pstruct.reInit(ops);
		
	    // // Re-add item listeners
	    // for (ItemListener listener : listeners) {
	    //     ops.addItemListener(listener);
	    // }
		ops.removeAll();
		pstruct.reInit(ops);
		setHelp();
		rebuild();
	}

	@Override
	public void setTree(int i, JComboBox<String> ops) {
		setHandler(null);
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
			else if (getCurrentOperation() == OPFEATURE_WINDOW && lastWindow != null) {
				lastWindow = adjustSearchRectangle(lastWindow);
				search(lastWindow, allDrawingTargets);
			}
		}
		redraw();
	}

	// ---------------- MouseListener && MouseMotionListener -------------

	Drawable lastDelete;
	int lastInsert; // index to pts vector

	@Override
	public void mouseEntered(MouseEvent me) {
		System.out.println("mouseEntered");

		if (handler != null) {
			handler.mouseEntered(me);
		} else {
			super.mouseEntered(me);
			lastDelete = null;
		}
	}

	@Override
	public void mouseExited(MouseEvent me) {
		System.out.println("mouseExited");

		if (handler != null) {
			handler.mouseExited(me);
		} else {
			super.mouseExited(me);
			if (lastDelete != null) {
//				lastDelete.directDraw(Color.red, offscrG);
				lastDelete = null;
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		System.out.println("mouseMoved");
		
		if (handler != null) {
			handler.mouseMoved(me);
		} else {
			super.mouseMoved(me);
			int op = getCurrentOperation();
			DPoint p = offscrG.transPointT(offscrG.adjustPoint(me.getPoint()));
			QueryObject qo = new QueryObject(p);

			if (op == OPFEATURE_DELETE || op == OPFEATURE_MOVE) {
				Drawable b = pstruct.NearestFirst(qo);
				
		        // Check if the current nearest drawable object is different from the last one
		        if (lastClosest == null || !lastClosest.equals(b)) {
		            // Only update if there is a change in the nearest object
		            if (b != null) {
		        		((DrawingCanvas) offscrG).clearOvals(Color.orange); // Added to remove last yellow rectangle
		                b.directDraw(Color.orange, offscrG);
		                lastDelete = b;
		            } else {
		                if (lastDelete != null) {
		                    lastDelete.directDraw(Color.red, offscrG);
		                }
		                lastDelete = null;
		            }
		            System.out.println("Drawable object updated");
		        }
		        lastClosest = b;
		        redraw();
			}
			if (op == OPFEATURE_WITHIN || op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST) {
				redraw();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		System.out.println("mouseClicked");
		if (handler != null) {
			handler.mouseClicked(me);
		} else {
			if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
				return; // operation doesn't use this mouse button
			super.mouseClicked(me);
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		System.out.println("mousePressed");
		if (handler != null) {
			handler.mousePressed(me);
		} else {
			if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
				return; // operation doesn't use this mouse button

			super.mousePressed(me);

			Point scrCoord = offscrG.adjustPoint(me.getPoint());
			DPoint p = offscrG.transPointT(scrCoord);
			QueryObject qo = new QueryObject(p);
			lastP = scrCoord;

			int op = getCurrentOperation();

			if (op == OPFEATURE_INSERT) {
				pstruct.MessageStart();
				if (pstruct.Insert(p))
					historyList.addElement(p);
				pstruct.MessageEnd();
				redraw();
			}

			if (op == OPFEATURE_MOVE) {
				lastInsert = -1;
				DPoint dp = (DPoint) pstruct.NearestFirst(qo);
				if (dp != null) {
					for (int i = historyList.size() - 1; i >= 0; i--) {
						if (((DPoint) (historyList.elementAt(i))).equals(dp))
							lastInsert = i;
					}
				}
				redraw();
			}

			if (op == OPFEATURE_DELETE) {
				DPoint dp = (DPoint) pstruct.NearestFirst(qo);
				pstruct.MessageStart();
				pstruct.Delete(p);
				historyList.addElement(new DeletePoint(p));
				lastDelete = null;
				pstruct.MessageEnd();
				((DrawingCanvas) offscrG).clearOval(dp);
				if (!((DrawingCanvas) offscrG).RedOvalsLeft()) {
					((DrawingCanvas) offscrG).clearOvals();
				}
				mouseMoved(me);
				redraw();
			}

			mouseDragged(me);
		}
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		System.out.println("mouseDragged");
		if (handler != null) {
			handler.mouseDragged(me);
		} else {
			if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
				return; // operation doesn't use this mouse button
			super.mouseDragged(me);
			// System.out.println("IN");
			Point scrCoord = offscrG.adjustPoint(me.getPoint());
			DPoint p = offscrG.transPointT(scrCoord);
			DPoint last = offscrG.transPointT(lastP);
			int op = getCurrentOperation();

			if (op == OPFEATURE_MOVE) {
				if (lastInsert >= 0) {
					if (pstruct.orderDependent()) {
						DPoint previous = (DPoint) historyList.elementAt(lastInsert);
						historyList.setElementAt(p, lastInsert);
						if (!updateFromParams(p)) {
							p = previous;
							historyList.setElementAt(previous, lastInsert);
							updateFromParams();
						}
						((DrawingCanvas) offscrG).clearOval(previous); // Added to remove last yellow oval
					} else {
						pstruct.MessageStart();
						pstruct.DeleteDirect((DPoint) historyList.elementAt(lastInsert));
						if (!pstruct.Insert(p)) {
							pstruct.Insert((DPoint) historyList.elementAt(lastInsert));
							p = (DPoint) historyList.elementAt(lastInsert);
						} else
							historyList.setElementAt(p, lastInsert);
						pstruct.MessageEnd();
					}
					lastDelete = pstruct.NearestFirst(new QueryObject(p));
	        		((DrawingCanvas) offscrG).clearOvals(Color.orange); // Added to remove last yellow oval
					lastDelete.directDraw(Color.orange, offscrG);
				}
				redraw();
			}
			if (op == OPFEATURE_WITHIN || op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST) {
				redraw();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		System.out.println("mouseReleased");
		if (handler != null) {
			handler.mouseReleased(me);
		} else {
			if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
				return; // operation doesn't use this mouse button
			super.mouseReleased(me);
			Point scrCoord = offscrG.adjustPoint(me.getPoint());
			DPoint p = offscrG.transPointT(scrCoord);
			// if (tree.runningThread != null)
			// return true;
			int op = getCurrentOperation();

			if (op == OPFEATURE_MOVE) {
				if (lastDelete != null)
//					lastDelete.directDraw(Color.red, offscrG);
				lastDelete = null;
				lastInsert = -1;
				redraw();
			}
		}
	}
	// ----------------------

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (handler != null) {
			handler.itemStateChanged(ie);
		} else {
			super.itemStateChanged(ie);
			if (getCurrentOperation() == OPFEATURE_LINE_INDEX) {
				if (pstruct instanceof Indexable) {
					Indexable in = (Indexable) pstruct;
					in.openLinesIndex();
					setHelp();
					redraw();
				}
			} else if (getCurrentOperation() == OPFEATURE_VERTEX_INDEX) {
				if (pstruct instanceof VoronoiDiagram) {
					VoronoiDiagram vd = (VoronoiDiagram) pstruct;
					Vector pts = new Vector();
					Vector lines = vd.getAllLines();
					Iterator it = lines.iterator();
					while (it.hasNext()) {
						DLine l = (DLine) it.next();
						if (this.can.contains(l.p1) && !pts.contains(l.p1))
							pts.add(l.p1);
						if (this.can.contains(l.p2) && !pts.contains(l.p2))
							pts.add(l.p2);
					}
					pstruct.Clear();
					this.historyList.clear();
					it = pts.iterator();
					while (it.hasNext()) {
						DPoint p = (DPoint) it.next();
						pstruct.Insert(p);
						this.historyList.add(p);
					}
					redraw();
				}
			}
		}
	}
}
