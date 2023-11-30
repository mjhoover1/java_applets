package vasco.points;

import java.awt.Rectangle;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: PointRTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.QueryObject;
import vasco.common.RTree;
import vasco.common.RebuildTree;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;

public class PointRTree extends PointStructure {
	RTree rt;

	public PointRTree(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
		rt = new RTree(can, 3, 6, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ops) {
		super.reInit(ops);
		rt.reInit(ops);
		addItemIfNotExists(ops, "Nearest");
		addItemIfNotExists(ops, "Within");
	}

	@Override
	public boolean orderDependent() {
		return rt.orderDependent();
	}

	@Override
	public boolean Insert(DPoint r) {
		return rt.Insert(r);
	}

	@Override
	public void Delete(DPoint p) {
		rt.Delete(p);
	}

	@Override
	public void DeleteDirect(Drawable p) {
		rt.DeleteDirect(p);
	}

	@Override
	public void MessageStart() {
		// super.MessageStart();
		rt.MessageStart();
	}

	@Override
	public void Clear() {
		super.Clear();
		rt.Clear();
	}

	@Override
	public void MessageEnd() {
		// super.MessageEnd();
		rt.MessageEnd();
	}

	@Override
	public SearchVector Search(QueryObject r, int mode) {
		return rt.Search(r, mode);
	}

	@Override
	public SearchVector Nearest(QueryObject p) {
		return rt.Nearest(p);
	}

	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
		return rt.Nearest(p, dist);
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		return rt.NearestRange(p, dist);
	}

	@Override
	public Drawable NearestFirst(QueryObject p) {
		return rt.NearestFirst(p);
	}

	@Override
	public void drawContents(DrawingTarget g, Rectangle view) {
		rt.drawContents(g, view);
	}

	@Override
	public void drawGrid(DrawingTarget g, int level) {
		rt.drawGrid(g, level);
	}

	@Override
	public String getName() {
		return rt.getName();
	}

}
