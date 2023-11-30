package vasco.regions;

import java.awt.Point;
import java.awt.Rectangle;

// import java.awt.*;
import javax.swing.JComboBox;

import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.GenericCanvas;
import vasco.common.QueryObject;
import vasco.common.RebuildTree;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;

public class ChainCodeStructure extends RegionStructure {

	ChainCode code;

	public ChainCodeStructure(GenericCanvas rc, DRectangle can, DrawingTarget dt, TopInterface p, RebuildTree r,
			Grid g) {
		super(rc, can, dt, p, r, g, CHAIN_MODE);
		code = null;
	}

	/************** RegionStructure **************/
	@Override
	public int search(Point p) {
		return super.search(p);
	}

	@Override
	public int search(Node node, Point p) {
		return super.search(node, p);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		addItemIfNotExists(ao, "To quadtree");
		addItemIfNotExists(ao, "To array");
		addItemIfNotExists(ao, "To raster");
	}

	@Override
	public void Clear() {
		super.Clear();
		root = new Node(null, 0, 0, 512, 0, -1, 0, true);
		/* clear the grid */
		grid.Clear();
	}

	@Override
	public String getName() {
		return "Chain code";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

	@Override
	public boolean Insert(Point p) {
		if (!grid.setColor(p.x, p.y, 1))
			return false;

		return true;
	}

	@Override
	public boolean Insert(Rectangle r) {
		for (int x = 0; x < r.width; x++)
			for (int y = 0; y < r.height; y++)
				Insert(new Point(r.x + x, r.y + y));
		return true;
	}

	@Override
	public boolean Delete(Point p) {
		if (!grid.setColor(p.x, p.y, 0))
			return false;

		return true;
	}

	@Override
	public boolean Delete(Rectangle r) {
		for (int x = 0; x < r.width; x++)
			for (int y = 0; y < r.height; y++)
				Delete(new Point(r.x + x, r.y + y));
		return true;
	}

	@Override
	public boolean Insert(Drawable r) {
		return true;
	}

	@Override
	public void Delete(DPoint p) {
	}

	@Override
	public void DeleteDirect(Drawable d) {
	}

	@Override
	public SearchVector Search(QueryObject r, int mode) {
		return null;
	}

	@Override
	public SearchVector Nearest(QueryObject p) {
		return null;
	}

	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		return null;
	}

	@Override
	public Drawable NearestFirst(QueryObject p) {
		return null;
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		return null;
	}

	@Override
	public void drawContents(DrawingTarget g, Rectangle view) {
		grid.drawContents(g);
	}

	@Override
	public void drawContents(DrawingTarget g) {
		grid.drawContents(g);
		/* display grid */
		if (grid.gridOn)
			drawGrid(g, grid.res);
	}

	@Override
	public void convert(ConvertVector cv) {
		if (operation.equals("To quadtree")) {
			convertToQuadTree(cv);
		} else if (operation.equals("To array")) {
			convertToBinaryArray(cv);
		} else if (operation.equals("To raster")) {
			convertToRaster(cv);
		} else if (operation.equals("To chain")) {
			convertToChainCode(cv);
		}
	}

	@Override
	public Node convertToQuadTree(ConvertVector cv) {

		/*
		 * Node root; root = (new ArrayToQuadTree(grid)).convert(cv); return root;
		 */

		return null;
	}

	@Override
	public ChainCode convertToChainCode(ConvertVector cv) {
		return null;
	}

	@Override
	public BinaryArray convertToBinaryArray(ConvertVector cv) {
		return null;
	}

	@Override
	public Raster convertToRaster(ConvertVector cv) {
		return null;
	}

}
