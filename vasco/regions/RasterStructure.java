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

public class RasterStructure extends RegionStructure {

	Raster raster;

	public RasterStructure(GenericCanvas rc, DRectangle can, DrawingTarget dt, TopInterface p, RebuildTree r, Grid g) {
		super(rc, can, dt, p, r, g, ARRAY_MODE);
		raster = new Raster(g);
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
		addItemIfNotExists(ao, "To chain");
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
		return "Raster";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

	@Override
	public boolean Insert(Point p) {
		if (!raster.setColor(p.x, p.y, 1))
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
		if (!raster.setColor(p.x, p.y, 0))
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
		raster.drawContents(g, view);
	}

	@Override
	public void drawContents(DrawingTarget g) {
		raster.drawContents(g, null);
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
		Node root;

		root = (new RasterToQuadTree(grid)).convert(cv);

		return root;
	}

	@Override
	public ChainCode convertToChainCode(ConvertVector cv) {
		return null;
	}

	@Override
	public BinaryArray convertToBinaryArray(ConvertVector cv) {
		BinaryArray array;

		array = (new RasterToArray(grid)).convert(cv);

		return array;
	}

	@Override
	public Raster convertToRaster(ConvertVector cv) {
		return null;
	}

}
