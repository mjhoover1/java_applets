package vasco.regions;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import vasco.common.ColorHelp;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingCanvas;
import vasco.common.DrawingTarget;
import vasco.common.FileIface;
import vasco.common.GenericCanvas;
import vasco.common.MouseDisplay;
import vasco.common.QueryObject;
import vasco.common.TopInterface;
import vasco.common.fileSelector;
import vasco.drawable.Drawable;

/*
1.  Still left to do the following conversions:
  a.  chain code to raster, region quadtree (need to fix), and array
  b.  array to chain code
  c.  raster to chain code
2.  <DONE> Load and save
3.  Popescu tree visualizer
4.  <DONE> Mouse help
5.  <DONE> Multicolor does not seem practical to implement as code relies on
    the types of nodes being black, white, or gray rather than integers.

*/

public class RegionCanvas extends GenericCanvas implements FileIface, ItemListener {

	// CentralMenu centralmenu;

	private RegionStructure[] pstrs;
	public RegionStructure pstruct;

	MyCursor cursor;
	HistoryList history = new HistoryList(historyList);

	public Grid grid;

	public DraggedBlock draggedObj = null;

	private int gridSize = 0;
	private Rectangle orig;

	protected static final int FULL_REDRAW = 0;
	protected static final int OFFSCR_REDRAW = 1;
	protected static final int DRAG_REDRAW = 2;

	protected int redrawMode = FULL_REDRAW;

	protected Image dragScr;
	protected Image dragObj;

	protected SelectedRect sRect = new SelectedRect();

	protected Vector cb = null; // connected blocks

	private int sIndex = -1;
	private JComboBox sChoice = null;
	
	private Point currentMousePosition;
	
//	private boolean newStruct = false;


	public RegionCanvas(DRectangle can, DrawingTarget dt, DrawingTarget over, JPanel animp, TopInterface ti) {// ,
																												// CentralMenu
																												// centralmenu)
																												// {
		/* initialize the canvas */
		super(can, dt, over, animp, ti);

		cursor = new MyCursor();
		// this.centralmenu = centralmenu;

		orig = offscrG.getOrig();

		grid = new Grid(orig, offscrG, 512, 5);
		gridSize = (int) (orig.width / Math.pow(2, gridLevel));
		/* initalize data structures */
		pstrs = new RegionStructure[3];

		/* set the help text */
		opFeature = new OpFeature[10];
		opFeature[0] = new OpFeature("Insert", OPFEATURE_INSERT, "Click to insert a new data point(s).", "Insert", "",
				"", InputEvent.BUTTON1_MASK);

		opFeature[1] = new OpFeature("Move", OPFEATURE_MOVE,
				"Click and drag to move the selected area to its new location (Cut/Paste).", "Move", "", "",
				InputEvent.BUTTON1_MASK);

		opFeature[2] = new OpFeature("U Move", OPFEATURE_UMOVE,
				"Click and drag to move the selected area to its new location (Cut/Union Paste).", "U Move", "", "",
				InputEvent.BUTTON1_MASK);

		opFeature[3] = new OpFeature("Copy", OPFEATURE_COPY,
				"Click and drag to move the selected area to its new location (Copy/Paste).", "Copy", "", "",
				InputEvent.BUTTON1_MASK);

		opFeature[4] = new OpFeature("Delete", OPFEATURE_DELETE, "Click to delete an existing data point(s)", "Delete",
				"", "", InputEvent.BUTTON1_MASK);

		opFeature[5] = new OpFeature("Select", OPFEATURE_SELECT, "Click and drag to select an area.", "Delete", "", "",
				InputEvent.BUTTON1_MASK);

		opFeature[6] = new OpFeature("To raster", OPFEATURE_TO_RASTER, "Convert to raster data structure.", "To raster",
				"", "", InputEvent.BUTTON1_MASK);

		opFeature[7] = new OpFeature("To array", OPFEATURE_TO_ARRAY, "Convert to array data structure.", "To array", "",
				"", InputEvent.BUTTON1_MASK);

		opFeature[8] = new OpFeature("To chain", OPFEATURE_TO_CHAIN, "Convert to chain code data structure.",
				"To chain", "", "", InputEvent.BUTTON1_MASK);

		opFeature[9] = new OpFeature("To quadtree", OPFEATURE_TO_QUADTREE, "Convert to quadtree data structure.",
				"To quadtree", "", "", InputEvent.BUTTON1_MASK);

	}

	/************** GeneralCanvas Base Class **************/

	@Override
	public void initStructs() {
		/* init the data structures */
		pstrs[0] = new RegionQuad(this, wholeCanvas, offscrG, topInterface, this, grid);
		pstrs[1] = new ArrayStructure(this, wholeCanvas, offscrG, topInterface, this, grid);
		pstrs[2] = new RasterStructure(this, wholeCanvas, offscrG, topInterface, this, grid);
		// pstrs[3] = new ChainCodeStructure(this, wholeCanvas, offscrG, topInterface,
		// this, grid);
	}

	@Override
	public String getCurrentOperationName() {
		return pstruct.getCurrentOperation();
	}

	@Override
	public OpFeature getCurrentOpFeature() {
		return super.getCurrentOpFeature();
	}

	@Override
	public int getSearchModeMask() {
		return 0;
	}

	@Override
	public int getAllowedOverlapQueryObjects() {
		return 0;
	}

	@Override
	public void nearest(QueryObject p, double dist, DrawingTarget[] off) {
	}

	@Override
	public void search(QueryObject s, DrawingTarget[] off) {
	}

	@Override
	public void setHelp() {
		super.setHelp();
	}

	public void debug(String s, int x, int y) {
		offscrG.setColor(Color.black);
		offscrG.drawString(s, x, y);
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		Object obj = ie.getSource();

		// if the last operation is a to chain operation
		if (pstruct.getOperation().equals("To chain")) {
			rebuild();
			redraw();
		}

		if (pstruct.si != null) {
			pstruct.si.dispose();
			pstruct.si = null;
		}

		if (obj instanceof JComboBox) {

			String op = getCurrentOperationName();

			pstruct.setOperation(op);

			cb = null;

			if (op.equals("To quadtree") || op.equals("To array") || op.equals("To chain") || op.equals("To raster")) {
				/* set the operation type */
				pstruct.setOperation(op);

				polyRange = new Vector();

				if (runningThread != null)
					terminate();

				if (op.equals("To chain")) {
					runningThread = null;
					cb = ConnectedBlocks.find(grid);
					redraw();
					if (selectCBlock(-1, null, cb)) {
						cb = null; // user not requested to choose a connected block
						convert(op);
					}
					return;
				}

				convert(op);
			} else
				super.itemStateChanged(ie);
		}
	}

	@Override
	public int getAppletType() {
		return ColorHelp.REGION_APPLET;
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
	public void clear() {
		super.clear();
		pstruct.MessageStart();
		pstruct.Clear();
		history = new HistoryList(new Vector());
		pstruct.MessageEnd();
	}

	@Override
	public fileSelector getFileSelector(String op) {
		return new RegionFileSelector(this, op, topInterface);
	}

	
	@Override
	public void drawContents(DrawingTarget g) {
	    pstruct.drawContents(g, g.getView());
	}

	@Override
	public void drawGrid(DrawingTarget g) {
		super.drawGrid(g);
		if (gridOn) {
			pstruct.drawGrid(g, gridLevel);
		}

	}

	public boolean selectCBlock(int i, JComboBox<String> ops, Vector cb) {
		int no, x, index;

		// find the number of valid connected blocks
		no = 0;
		index = 0;
		for (x = 0; x < cb.size(); x++)
			if (((CBlock) cb.elementAt(x)).valid) {
				index = x;
				no++;
			}

		if (no == 0) {
			if (ops != null) // only if I am changing the region structure
				switchTree(i, ops);
			pstruct.Clear();
			redraw();
			return true;
		} else if (no == 1) {
			if (ops != null) // only if I am changing the region structure
				switchTree(i, ops);
			pstruct.Clear();
			CBlock b = (CBlock) cb.elementAt(index);
			for (x = 0; x < b.v.size(); x++)
				pstruct.Insert((Point) b.v.elementAt(x));
			redraw();
			return true;
		}

		// inform the user
		new InfoBox("Please, select a connected block. Move the cursor over a connected one and then click on it.");

		return false;
	}

	public void switchTree(int i, JComboBox<String> ops) {
		cb = null;
		sChoice = null;

		if (pstruct != null && pstruct.si != null) {
			pstruct.si.dispose();
			pstruct.si = null;
		}

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
//	        setHelp();  // Call setHelp only if JComboBox is not empty
	    }
	    
		if (runningThread != null) {
			terminate();
			runningThread = null;
		}

		// if it is not a chain code
		if (i != 3)
			rebuild();
	}

	@Override
	public void setTree(int i, JComboBox<String> ops) {
		// if it is chain code
		if (i == 3 && sChoice == null) {

			if (pstruct.si != null) {
				pstruct.si.dispose();
				pstruct.si = null;
			}

			cb = ConnectedBlocks.find(grid);

			if (selectCBlock(i, ops, cb))
				return;
			else {
				sIndex = i;
				sChoice = ops;
				redraw();
				return;
			}
		}

		switchTree(i, ops);

	}

	@Override
	public void setGrid(int i) {
		int oldRes, oldCellSize;
		int[][] oldGrid;

		if ((i >= 1) && (i != gridLevel)) {
			oldRes = gridLevel;
			oldCellSize = grid.cellSize;
			gridLevel = i;
			// super.setGrid(i);
			oldGrid = pstruct.grid.setGrid(i);
			pstruct.Clear();
			gridSize = (int) (orig.width / Math.pow(2, gridLevel));

			historyList = history.switchGrid(oldRes, oldCellSize, oldGrid, grid);
			rebuild();
		
	        // Use paintImmediately to update the canvas immediately
	        SwingUtilities.invokeLater(() -> {
	        	((DrawingCanvas) offscrG).paintImmediately(orig);
//	            ((DrawingCanvas) offscrG).paintImmediately(((DrawingCanvas) offscrG).getBounds());
//	            ((DrawingCanvas) overview).paintImmediately(((DrawingCanvas) overview).getBounds());
	        });
		}
	}

	@Override
	public void setGrid(boolean b) {
		super.setGrid(b);
		pstruct.grid.setGrid(b);
		redraw();
	}

	@Override
	public QueryObject adjustSearchRectangle(QueryObject s) {
		return super.adjustSearchRectangle(s);
	}

	/************** Rebuild Interface **************/
	@Override
	public void rebuild() {
		pstruct.Clear();
		updateFromParams();
		redraw();
	}

	/************** FileIFace Interface **************/
	@Override
	public Vector vectorOut() {
		Vector ret = new Vector();
		HistoryElmInterface elm;

		for (int i = 0; i < history.size(); i++) {
			elm = history.elementAt(i);
			elm.save(ret);
		}

		return ret;
	}

	@Override
	public void vectorIn(Vector p) {
		pstruct.MessageStart();
		pstruct.Clear();
		history = new HistoryList(new Vector());
		// read the resolution first
		setGrid(((Integer) p.elementAt(0)).intValue());
		// then read the data points
		for (int i = 1; i < p.size(); i++) {
			Point pt = (Point) p.elementAt(i);
			if (pstruct.Insert(pt)) {
				history.add(new InsertBlockCell(new Rectangle(pt.x, pt.y, 1, 1), 1));
			}
		}
		pstruct.MessageEnd();
		redraw();
	}

	@Override
	public String[] stringsOut() {
		Vector in = vectorOut();
		String[] out = new String[in.size() + 1];

		// print out the resulation first
		out[0] = new String("" + grid.res);
		// then print out the data points
		for (int i = 0; i < in.size(); i++) {
			Point er = (Point) in.elementAt(i);
			out[i + 1] = new String(er.y + " " + er.x);
		}
		return out;
	}

	@Override
	public boolean testCoordinates(DPoint c) {
		return super.testCoordinates(c);
	}

	@Override
	public DPoint randomDPoint() {
		return super.randomDPoint();
	}

	public Point randomPoint() {
		return new Point((int) (Math.pow(2, grid.res) * Math.random()), (int) (Math.pow(2, grid.res) * Math.random()));
	}

	void updateFromParams() {
		updateFromParams(null);
	}

	@Override
	public void undo() {
		history.undo();
		rebuild();
	}

	boolean updateFromParams(Point newIns) {
		HistoryElmInterface elm;

		pstruct.MessageStart();
		pstruct.Clear();
		for (int i = 0; i < history.size(); i++)
			history.elementAt(i).build(pstruct);
		pstruct.MessageEnd();

		return true;
	}

	public void convert(String op) {
		ConvertVector v;

		// start the conversion
		polyRange = new Vector();

		if (runningThread != null)
			terminate();

		if (op.equals("To chain")) {
			v = new DemoBuffer(grid, pstruct);
			v.constructRegion(0, 100);
			v.activateRegion();
		} else
			v = new ConvertVector(grid, pstruct);

		if (runningThread == null)
			runningThread = new ConvertThread(v, null, this, allDrawingTargets);

		setHelp();
		redraw();
	}

	/************** Mouse Interface **************/
	Drawable lastDelete;
	int lastInsert; // index to pts vector

	@Override
	public void mouseEntered(MouseEvent me) {

	}

	@Override
	public void mouseExited(MouseEvent me) {
		// System.out.println("Mouse Exited");
		redrawMode = OFFSCR_REDRAW;
		redraw();
		cursor.move(offscrG, null);
		cursor.move(overview, null);
		((DrawingCanvas) offscrG).clearRectangles();
		((DrawingCanvas) offscrG).clearLines();
	}

	/**
	 * Handles the mouse moved event within the canvas.
	 * This method is synchronized to ensure thread safety during event handling.
	 *
	 * @param me The MouseEvent object that contains information about the mouse movement.
	 */
	@Override
	synchronized public void mouseMoved(MouseEvent me) {
	    CursorStyle cs;

	    // If a conversion operation is in progress, the mouse cursor display is bypassed.
	    if (runningThread != null)
	        return;

	    // Adjusts the mouse point coordinates to the offscreen graphics context.
	    Point mCor = offscrG.adjustPoint(me.getPoint());
	    // Translates the mouse coordinates from screen to the actual canvas coordinates.
	    Point rCor = transMouseToScr(mCor);
	    // Translates the canvas coordinates to grid coordinates.
	    Point gCor = transScrToGrid(mCor);

	    // Retrieves the current operation mode.
	    int op = getCurrentOperation();
	    
	    // System.out.println("Mouse Coordinates in Canvas: " + mCor.x + " " + mCor.y);
	    // System.out.println("Translated Screen Coordinates: " + rCor.x + " " + rCor.y);
	    // System.out.println("Translated Grid Coordinates: " + gCor.x + " " + gCor.y);
	    // System.out.println("Current Operation: " + op);

	    // Checks if the user is interacting with a connected block.
	    if (cb != null) {
	        CBlock b;

	        cs = new CursorStyle();
	        // If the mouse is over a valid connected block, updates the cursor style.
	        if (((b = ConnectedBlocks.inBlock(cb, grid, gCor.x, gCor.y)) != null) && b.valid)
	            cs.add(new PolygonCursor(b.p, Colors.SELECTED_CELL));

	        // If the cursor style has changed, triggers a redraw of the canvas.
	        if (cursor.isDifferentCursor(cs)) {
	            redrawMode = OFFSCR_REDRAW;
	            redraw();

	            cursor.move(offscrG, cs);
	            cursor.move(overview, cs);
	        }
	        // System.out.println("Mouse is over a connected block: " + b);
	    } else {
	        // If the cursor style is different based on the current operation and mouse location, triggers a redraw.
	    	boolean isCursorDifferent = cursor.isDifferentCursor(pstruct.mouseMoved(rCor.x, rCor.y, mCor.x, mCor.y, op, sRect));
	        // System.out.println("Is Cursor Different: " + isCursorDifferent);	        
	        if (isCursorDifferent) {
	            redrawMode = OFFSCR_REDRAW;
	            redraw();
	            cs = pstruct.mouseMoved(rCor.x, rCor.y, mCor.x, mCor.y, op, sRect);
	            cursor.move(offscrG, cs);
	            cursor.move(overview, cs);
	        }
	    }
	    // Update the current mouse position
	    currentMousePosition = offscrG.adjustPoint(me.getPoint());
	    
	    // Trigger a redraw to update the highlight
	    redraw();
	}

	@Override
	public void redraw() {
		switch (redrawMode) {

		case OFFSCR_REDRAW:
			offscrG.redraw();
			redrawMode = FULL_REDRAW;
			redraw(overview);
			break;

		case DRAG_REDRAW:
			Rectangle r = grid.getScreenCoor(draggedObj.getPos());
			((DrawingCanvas) offscrG).drawImg(dragScr, 3, 3);
			((DrawingCanvas) offscrG).drawImg(dragObj, r.x + 3, r.y + 3);
			pstruct.drawNonLeafNodes(offscrG);

			offscrG.redraw();
			redraw(overview);
			redrawMode = FULL_REDRAW;
			break;

		default:
//			if (newStruct) {
//				((DrawingCanvas) offscrG).clearRectangles();
//				newStruct = false;
//			}
			drawBackground(offscrG);
			if (runningThread != null)
				runningThread.refill();
			drawGrid(offscrG);
			drawContents(offscrG);
			if (runningThread != null)
				runningThread.redraw();
			offscrG.redraw();

			redraw(overview);
			break;
		} // switch

		// draw the selected area
		if (sRect.selected) {
			CursorStyle cs = new CursorStyle();
			cs = pstruct.mouseSelect(sRect.get(), 12);
			cursor.move(offscrG, cs);
			cursor.move(overview, cs);
		}
	}

	@Override
	synchronized public void mouseClicked(MouseEvent me) {
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button

		super.mouseClicked(me);
	}

	@Override
	synchronized public void mousePressed(MouseEvent me) {
		Rectangle selected, s;
		CursorStyle cs;
		boolean isConversion = true;

		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return; // operation doesn't use this mouse button

		Point mCor = offscrG.adjustPoint(me.getPoint());
		Point gCor = transScrToGrid(mCor);
		Rectangle sScrRect = null;

		// offscrG.directDrawString(" " + gCor.x + " "+ gCor.y, 10, 10);
		/* different operation modes */
		int op = getCurrentOperation();
		selected = pstruct.getSelected();

		if (cb != null) {
			CBlock b;
			// mouse is clicked on a connected block
			if (((b = ConnectedBlocks.inBlock(cb, grid, gCor.x, gCor.y)) != null) && b.valid) {

				// if user trying to set the structure type to chain code
				if (sChoice != null) {
					setTree(sIndex, sChoice);
					isConversion = false;
				}

				// delete the old structure
				pstruct.Clear();
				// insert only the selected ones
				for (int x = 0; x < b.v.size(); x++)
					pstruct.Insert((Point) b.v.elementAt(x));

				cb = null;
				redraw();

				if (isConversion) {
					convert(pstruct.getOperation());
				}
			}
			return;
		}

		if (sRect.selected) {
			s = sRect.get();
			if (s.contains(gCor.x, gCor.y) && op != OPFEATURE_SELECT) {
				selected = s;
			} else {
				sRect = new SelectedRect();
			}
		}

		if (selected == null) {
			sRect = new SelectedRect();
			return;
		}

		if (!sRect.selected && (op == OPFEATURE_INSERT || op == OPFEATURE_DELETE || op == OPFEATURE_SELECT)) {
			/* selecting an area */
			if (selected.width == 1 && selected.height == 1) {
				sRect = new SelectedRect();
				sRect.selected = true;
				sRect.ul = new Point(selected.x, selected.y);
				sRect.lr = new Point(selected.x, selected.y);

				if (cursor.isDifferentCursor(pstruct.mouseSelect(sRect.get(), op))) {
					redrawMode = OFFSCR_REDRAW;
					redraw();
					cs = pstruct.mouseSelect(sRect.get(), op);
					cursor.move(offscrG, cs);
					cursor.move(overview, cs);
				}
			} else
				sRect = new SelectedRect();
		} else
			sRect = new SelectedRect();

		switch (op) {

		case OPFEATURE_INSERT:
			if (!sRect.selected) {
				if (pstruct.isValidMove(selected, op)) {
					pstruct.Insert(selected);
					history.add(new InsertBlockCell(selected, 1));
					redraw();
					mouseMoved(me);
				} else {
					sScrRect = grid.getScreenCoor(selected);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, offscrG);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, overview);
				}
			}
			break;

		case OPFEATURE_DELETE:
			if (!sRect.selected) {
				if (pstruct.isValidMove(selected, op)) {
					pstruct.Delete(selected);
					history.add(new InsertBlockCell(selected, 0));
					lastDelete = null;
					redraw();
					mouseMoved(me);
				} else {
					sScrRect = grid.getScreenCoor(selected);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, offscrG);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, overview);
				}
			}
			break;

		case OPFEATURE_UMOVE:
		case OPFEATURE_COPY:
		case OPFEATURE_MOVE:
			switch (op) {
			case OPFEATURE_UMOVE:
				draggedObj = new DraggedBlock(grid, selected, new Point(selected.x, selected.y), gCor,
						DraggedBlock.UNION_MODE);
				break;
			case OPFEATURE_COPY:
				draggedObj = new DraggedBlock(grid, selected, new Point(selected.x, selected.y), gCor,
						DraggedBlock.COPY_MODE);
				break;
			case OPFEATURE_MOVE:
				draggedObj = new DraggedBlock(grid, selected, new Point(selected.x, selected.y), gCor,
						DraggedBlock.MOVE_MODE);
				break;
			}

			dragObj = ((DrawingCanvas) offscrG).createImage(selected.width * grid.cellSize + 1,
					selected.height * grid.cellSize + 1);
			grid.drawContents(dragObj, selected, draggedObj.getBlock());

			if (op != OPFEATURE_COPY)
				pstruct.Delete(selected);

			dragScr = ((DrawingCanvas) offscrG).createImage(512, 512);
			drawBackground(offscrG);
			pstruct.drawContents(offscrG);
			((DrawingCanvas) offscrG).paint(dragScr.getGraphics());

			if (op != OPFEATURE_COPY)
				history.add(new DeleteBlockCell(selected));
			break;

		}
		// pstruct.MessageEnd();
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return;

		CursorStyle cs;
		Point mCor = offscrG.adjustPoint(me.getPoint());
		Point gCor = transScrToGrid(mCor);

		int op = getCurrentOperation();

		if (sChoice != null)
			return;

		/* selecting an area */
		if (sRect.selected && (op == OPFEATURE_INSERT || op == OPFEATURE_DELETE || op == OPFEATURE_SELECT)) {
			sRect.lr = new Point(gCor.x, gCor.y);

			if (cursor.isDifferentCursor(pstruct.mouseSelect(sRect.get(), op))) {
				redrawMode = OFFSCR_REDRAW;
				redraw();
				cs = pstruct.mouseSelect(sRect.get(), op);
				cursor.move(offscrG, cs);
				cursor.move(overview, cs);
			}

			return;
		}

		switch (op) {

		case OPFEATURE_UMOVE:
		case OPFEATURE_COPY:
		case OPFEATURE_MOVE:
			if (draggedObj != null && !gCor.equals(draggedObj.getPos())) {
				draggedObj.move(grid, gCor, pstruct, dragObj);
				redrawMode = DRAG_REDRAW;
				redraw();
			}
			break;
		} // swicth
	}

	@Override
	synchronized public void mouseReleased(MouseEvent me) {
		if ((getCurrentOpFeature().buttonMask & MouseDisplay.getMouseButtons(me)) == 0)
			return;

		Point mCor = offscrG.adjustPoint(me.getPoint());
		Point gCor = transScrToGrid(mCor);
		Rectangle sScrRect = null;

		int op = getCurrentOperation();

		if (sChoice != null)
			return;

		switch (op) {

		case OPFEATURE_INSERT:
			if (sRect.selected && cursor.getValid()) {
				if (pstruct.isValidMove(sRect.get(), op)) {
					pstruct.Insert(sRect.get());
					history.add(new InsertBlockCell(sRect.get(), 1));
					redraw();
					mouseMoved(me);
				} else {
					sScrRect = grid.getScreenCoor(sRect.get());
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, offscrG);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, overview);
				}
			}
			break;

		case OPFEATURE_DELETE:
			if (sRect.selected && cursor.getValid()) {
				if (pstruct.isValidMove(sRect.get(), op)) {
					pstruct.Delete(sRect.get());
					history.add(new InsertBlockCell(sRect.get(), 0));
					redraw();
					mouseMoved(me);
				} else {
					sScrRect = grid.getScreenCoor(sRect.get());
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, offscrG);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, overview);
				}
			}
			break;

		case OPFEATURE_UMOVE:
		case OPFEATURE_COPY:
		case OPFEATURE_MOVE:
			if (draggedObj != null) {
				if (pstruct.isValidMove(null, op)) {
					draggedObj.move(grid, gCor, pstruct, dragObj);
					history.add(draggedObj.finish(grid));
					redraw();
				} else {
					draggedObj.rollBack(pstruct);
					history.add(draggedObj.finish(grid));
					redraw();
					sScrRect = grid.getScreenCoor(draggedObj.getGrdRect(gCor));
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, offscrG);
					cursor.showInvalid(sScrRect, Colors.SELECTED_CELL, overview);
				}
				draggedObj = null;
			}
			break;
		} // switch

		if (op != OPFEATURE_SELECT)
			sRect = new SelectedRect();
	}

	/*************************************/
	public Point transScrToGrid(Point scrC) {
		Rectangle target = offscrG.getView();

		int x = orig.width * (scrC.x - target.x) / target.width + orig.x;
		int y = orig.height * (scrC.y - target.y) / target.height + orig.y;

		return new Point(x / gridSize, y / gridSize);
	}

	/* useful if the user zoomed the region canvas */
	public Point transMouseToScr(Point scrC) {
		Rectangle target = offscrG.getView();

		int x = orig.width * (scrC.x - target.x) / target.width + orig.x;
		int y = orig.height * (scrC.y - target.y) / target.height + orig.y;

		return new Point(x, y);
	}

	public Point transGridToScr(Point grid) {
		Rectangle target = offscrG.getView();
		/*
		 * return new Point( (int)Math.round(target.width * (grid.x - orig.x) /
		 * (double)orig.width + target.x), (int)Math.round(target.height * (grid.y -
		 * orig.y) / (double)orig.height + target.y));
		 */
		return new Point(grid.x * gridSize - target.x, grid.y * gridSize - target.y);
	}

}
