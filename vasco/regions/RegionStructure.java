package vasco.regions;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

import vasco.common.CommonConstants;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.GenericCanvas;
import vasco.common.RebuildTree;
import vasco.common.SpatialStructure;
import vasco.common.TopInterface;

/**
 * The RegionStructure class represents an abstract spatial structure for handling regions in a graphics application.
 * It provides methods for managing regions, inserting, deleting, and selecting elements within the structure.
 * This class serves as the base class for specific region structures like QuadTreeRegionStructure, ArrayRegionStructure, and ChainRegionStructure.
 */
abstract public class RegionStructure extends SpatialStructure implements CommonConstants {

	public static final int QUAD_MODE = 0;
	public static final int ARRAY_MODE = 1;
	public static final int CHAIN_MODE = 2;

	protected static final String DEFAULT_OPERATION = "Insert";

	protected Grid grid;
	protected Node root;
	protected String operation;
	protected Rectangle selected = null;
	protected CursorStyle cs;
	protected int mode;
	protected GenericCanvas rCanvas;

	public StructureBox si = null;
	public DrawingTarget dt;

    /**
     * Initializes a new RegionStructure with the specified parameters.
     *
     * @param rCanvas The GenericCanvas associated with this region structure.
     * @param can     The DRectangle representing the canvas.
     * @param dt      The DrawingTarget for rendering.
     * @param ti      The TopInterface.
     * @param r       The RebuildTree instance.
     * @param g       The Grid instance.
     * @param m       The mode of the region structure (QUAD_MODE, ARRAY_MODE, or CHAIN_MODE).
     */
	RegionStructure(GenericCanvas rCanvas, DRectangle can, DrawingTarget dt, TopInterface ti, RebuildTree r, Grid g,
			int m) {
		super(can, ti, r);
		this.rCanvas = rCanvas;
		grid = g;
		root = new Node(null, 0, 0, 512, 0, -1, 0, true);
		operation = DEFAULT_OPERATION;
		cs = null;
		mode = m;
		this.dt = dt;
	}

	@Override
	public void Clear() {
		root = new Node(null, 0, 0, 512, 0, -1, 0, true);
		grid.Clear();
	}

	protected void setOperation(String operation) {
		this.operation = operation;
	}

	protected String getOperation() {
		return operation;
	}

	/************** SpatialStructure **************/
	@Override
	public void reInit(JComboBox<String> ops) {
		super.reInit(ops);
		JComboBox<String> availOps = ops;
		addItemIfNotExists(availOps, "Insert");
		addItemIfNotExists(availOps, "Move");
		addItemIfNotExists(availOps, "Delete");
		addItemIfNotExists(availOps, "U Move");
		addItemIfNotExists(availOps, "Copy");
		addItemIfNotExists(availOps, "Select");
	}

	@Override
	public boolean Insert(Point p) {
		/* insert it to the grid */
		if (!grid.setColor(p.x, p.y, 1))
			return false;
		/* insert it to the quadtree */
		return Node.insert(root, grid.grdToScr(p), 1, grid.cellSize);
	}

	public boolean Insert(Rectangle r) {
		for (int x = 0; x < r.width; x++)
			for (int y = 0; y < r.height; y++)
				Insert(new Point(r.x + x, r.y + y));
		return true;
	}

	public boolean merge(Node node) {
		boolean result = false;
		if (node == null)
			return false;
		result = node.merge();
		if (result)
			merge(node.parent);
		return result;
	}

	public boolean insert(Node node, Point p, int color) {
		if ((node == null) || !node.inside(p))
			return false;

		/* if it is a leaf node */
		if (node.isLeaf) {
			if (node.color == color)
				return false;
			if (node.size > grid.cellSize) {
				node.split();
				return (insert(node.child[0], p, color) || insert(node.child[1], p, color)
						|| insert(node.child[2], p, color) || insert(node.child[3], p, color));
			} else {
				if (color == 1)
					node.color = 1;
				else
					node.color = 0;

				merge(node.parent);
				return true;
			}
		} else {
			/* if it is a non-leaf node */
			return (insert(node.child[0], p, color) || insert(node.child[1], p, color)
					|| insert(node.child[2], p, color) || insert(node.child[3], p, color));
		}
	}

	@Override
	public boolean Delete(Point p) {
		/* insert it to the grid */
		if (!grid.setColor(p.x, p.y, 0))
			return false;
		/* insert it to the quadtree */
		return Node.insert(root, grid.grdToScr(p), 0, grid.cellSize);
	}

	public boolean Delete(Rectangle r) {
		for (int x = 0; x < r.width; x++)
			for (int y = 0; y < r.height; y++)
				Delete(new Point(r.x + x, r.y + y));
		return true;
	}

	@Override
	public void drawGrid(DrawingTarget g, int level) {
		if (level == 0 || !grid.gridOn)
			return;
		g.setColor(Color.lightGray);
		double canvasWidth = Math.min(wholeCanvas.width, wholeCanvas.height);
		double add = canvasWidth / (int) Math.pow(2, level);
		for (double line = add; line < canvasWidth; line += add) {
			g.drawLine(wholeCanvas.x, wholeCanvas.y + line, wholeCanvas.x + wholeCanvas.width, wholeCanvas.y + line);
			g.drawLine(wholeCanvas.x + line, wholeCanvas.y, wholeCanvas.x + line, wholeCanvas.y + wholeCanvas.height);
		}
	}

	public void drawContents(DrawingTarget g) {
		/* first display leafs */
		root.display(g, 0);
		/* display grid */
		if (grid.gridOn)
			drawGrid(g, grid.res);
		/* display non-leafs */
	}

	public void drawNonLeafNodes(DrawingTarget g) {
		root.display(g, 1);
	}

	@Override
	public void drawContents(DrawingTarget g, Rectangle view) {
		/* first display leafs */
		root.display(g, 0);
		/* display grid */
		if (grid.gridOn)
			drawGrid(g, grid.res);
		/* display non-leafs */
		root.display(g, 1);
	}

	public int search(Point p) {
		return search(root, p);
	}

	public int search(Node node, Point p) {
		int c;
		if ((node == null) || !node.inside(p))
			return -1;

		/* if it is a leaf node */
		if (node.isLeaf)
			return node.color;

		/* if it is a non-leaf node */
		c = search(node.child[0], p);
		if (c != -1)
			return c;
		c = search(node.child[1], p);
		if (c != -1)
			return c;
		c = search(node.child[2], p);
		if (c != -1)
			return c;
		c = search(node.child[3], p);
		if (c != -1)
			return c;

		return -1;
	}

	public Node quadSearch(Node node, Point p, int size, Vector<Node> nearest, double dist) {
		Node n;
		double d;

		if (node == null)
			return null;

		if (!node.inside(p))
			return null;

		Rectangle r = new Rectangle(node.x + (node.size / 2) - (size / 2), node.y + (node.size / 2) - (size / 2), size,
				size);

		/* calculate the distance from the center to the Point p */
		d = Math.pow(node.x + (node.size / 2) - p.x, 2) + Math.pow(node.y + (node.size / 2) - p.y, 2);
		if (dist == -1) {
			nearest.addElement(node);
			dist = d;
		} else {
			if (d < dist) {
				nearest.setElementAt(node, 0);
				dist = d;
			}
		}

		if (r.contains(p))
			return node;

		/* if it is a non-leaf node */
		n = quadSearch(node.child[0], p, size, nearest, dist);
		if (n != null)
			return n;
		n = quadSearch(node.child[1], p, size, nearest, dist);
		if (n != null)
			return n;
		n = quadSearch(node.child[2], p, size, nearest, dist);
		if (n != null)
			return n;
		n = quadSearch(node.child[3], p, size, nearest, dist);
		if (n != null)
			return n;

		return null;
	}

	public abstract void convert(ConvertVector v);

	public abstract Node convertToQuadTree(ConvertVector cv);

	public abstract ChainCode convertToChainCode(ConvertVector cv);

	public abstract BinaryArray convertToBinaryArray(ConvertVector cv);

	public abstract Raster convertToRaster(ConvertVector cv);

	public CursorStyle mouseSelect(Rectangle rec, int op) {
		boolean valid;

		cs = new CursorStyle();
		Rectangle rc = grid.getScreenCoor(new Point(rec.x, rec.y));
		rc.width = grid.cellSize * rec.width;
		rc.height = grid.cellSize * rec.height;

		valid = true;
		if (op == 12)
			cs.add(new ValidGridCursor(new Rectangle(rc.x + 1, rc.y + 1, rc.width - 2, rc.height - 2),
					Colors.SELECTED_AREA, valid));
		else
			cs.add(new ValidGridCursor(new Rectangle(rc.x + 1, rc.y + 1, rc.width - 2, rc.height - 2),
					Colors.SELECTED_CELL, valid));

		cs.setValid(valid);
		return cs;
	}

	public boolean isValidMove(Rectangle rec, int op) {

		if (mode == CHAIN_MODE) {
			Vector blocks = null;
			int[][] backBlock = null;
			int x, y;
			switch (op) {
			case 1: // insert
				backBlock = grid.get(rec);
				for (x = 0; x < rec.width; x++)
					for (y = 0; y < rec.height; y++)
						grid.setColor(rec.x + x, rec.y + y, 1);
				blocks = ConnectedBlocks.find(grid, ConnectedBlocks.VERIFY_MODE);
				for (x = 0; x < rec.width; x++)
					for (y = 0; y < rec.height; y++)
						grid.setColor(rec.x + x, rec.y + y, backBlock[x][y]);

				if (blocks == null)
					return false;
				break;
			case 2: // delete
				backBlock = grid.get(rec);
				for (x = 0; x < rec.width; x++)
					for (y = 0; y < rec.height; y++)
						grid.setColor(rec.x + x, rec.y + y, 0);
				blocks = ConnectedBlocks.find(grid, ConnectedBlocks.VERIFY_MODE);
				for (x = 0; x < rec.width; x++)
					for (y = 0; y < rec.height; y++)
						grid.setColor(rec.x + x, rec.y + y, backBlock[x][y]);

				if (blocks == null)
					return false;
				break;
			case 3: // move
			case 10: // umove
			case 11: // copy
				if ((blocks = ConnectedBlocks.find(grid, ConnectedBlocks.VERIFY_MODE)) == null)
					return false;
				break;
			case 12: // select
				break;
			}
		}

		return true;
	}

	public CursorStyle mouseMoved(int rx, int ry, int x, int y, int op, SelectedRect sRect) {
		cs = new CursorStyle();
		Point gridC = grid.getGridCoor(new Point(x, y));
		Rectangle rc = grid.getScreenCoor(gridC);
		Vector nearest = new Vector();
		Node nNode = null;
		int size = grid.getCellSize();
		boolean isValid = false;
		Node node = null;

		if (sRect.selected && op != 12) {
			Rectangle s = sRect.get();
			if (s.contains(gridC.x, gridC.y)) {
				selected = s;
				return mouseSelect(s, op);
			}
		}

		if (op != 12 && mode == QUAD_MODE) {
			node = quadSearch(root, new Point(rx, ry), size, nearest, -1);

			/* nearest block */
			if (nearest.size() > 0) {
				nNode = (Node) nearest.elementAt(0);
				cs.add(new ValidGridCursor(new Rectangle(nNode.x + (nNode.size / 2) - (size / 2),
						nNode.y + (nNode.size / 2) - (size / 2), size, size), Colors.NEAREST_NODE));
			}
		}

		if (node == null) {
			/* if the grid is already set */
			if ((grid.getColor(gridC.x, gridC.y) != 0 && op == 1)
					|| (grid.getColor(gridC.x, gridC.y) == 0 && op == 2)) {
				isValid = false;
				selected = null;
			} else {
				isValid = true;
				selected = new Rectangle(gridC.x, gridC.y, 1, 1);
			}

			if (op == 12)
				cs.add(new ValidGridCursor(new Rectangle(rc.x + 1, rc.y + 1, rc.width - 2, rc.height - 2),
						Colors.SELECTED_AREA, isValid));
			else
				cs.add(new ValidGridCursor(new Rectangle(rc.x + 1, rc.y + 1, rc.width - 2, rc.height - 2),
						Colors.SELECTED_CELL, isValid));
			cs.setValid(isValid);
		} else {
			Point sp, ep;

			if ((node.isBlack() && op == 1) || (node.isWhite() && op == 2)) {
				isValid = false;
				selected = null;
			} else {
				isValid = true;
				sp = grid.getRGridCoor(node.x + 1, node.y + 1);
				ep = grid.getRGridCoor(node.x + node.size - 1, node.y + node.size - 1);
				selected = new Rectangle(sp.x, sp.y, ep.x - sp.x + 1, ep.y - sp.y + 1);
			}

			cs.add(new ValidGridCursor(new Rectangle(node.x + 1, node.y + 1, node.size - 2, node.size - 2),
					Colors.SELECTED_CELL, isValid));
		}
	    // System.out.println("Selected Rectangle: left " + sRect.lr + " selected " + sRect.selected + " right " + sRect.ul);
	    // System.out.println("Operation: " + op);
	    // System.out.println("Mode: " + mode);
	    // System.out.println("Node is null: " + (node == null));
	    // System.out.println("Grid color at selected point: " + grid.getColor(gridC.x, gridC.y));
	    // System.out.println("Is Valid Move: " + isValid);

		return cs;
	}

//	private void processGridCellHighlighting(Point gridC, Rectangle rc, int op) {
//		boolean isValid;
//		if ((grid.getColor(gridC.x, gridC.y) != 0 && op == 1) || (grid.getColor(gridC.x, gridC.y) == 0 && op == 2)) {
//			isValid = false;
//			selected = null;
//		} else {
//			isValid = true;
//			selected = new Rectangle(gridC.x, gridC.y, 1, 1);
//		}
//
//		Color color = (op == 12) ? Colors.SELECTED_AREA : Colors.SELECTED_CELL;
//		cs.add(new ValidGridCursor(new Rectangle(rc.x + 1, rc.y + 1, rc.width - 2, rc.height - 2), color, isValid));
//		cs.setValid(isValid);
//	}

	public Rectangle getSelected() {
		return selected;
	}

}
