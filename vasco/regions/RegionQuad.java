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

public class RegionQuad extends RegionStructure {

	public RegionQuad(GenericCanvas rc, DRectangle can, DrawingTarget dt, TopInterface p, RebuildTree r, Grid g) {
		super(rc, can, dt, p, r, g, QUAD_MODE);
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
		addItemIfNotExists(ao, "To raster");
		addItemIfNotExists(ao, "To chain");
		addItemIfNotExists(ao, "To array");
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
		return "Region Tree";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

	@Override
	public boolean Insert(Point p) {
		return super.Insert(p);
	}

	@Override
	public boolean Delete(Point p) {
		return super.Delete(p);
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
		super.drawContents(g, view);
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
		return null;
	}

	@Override
	public ChainCode convertToChainCode(ConvertVector cv) {
		ChainCode code = null;

		if (si == null)
			si = new StructureBox("Chain Code", 7, 40);
		code = (new QuadTreeToChain(root, grid, si)).convert(cv);

		return code;
	}

	@Override
	public BinaryArray convertToBinaryArray(ConvertVector cv) {
		BinaryArray array;

		array = (new QuadTreeToArray(root, grid)).convert(cv);

		return null;
	}

	@Override
	public Raster convertToRaster(ConvertVector cv) {
		Raster raster;

		raster = (new QuadTreeToRaster(root, grid)).convert(cv);

		return raster;
	}
}
