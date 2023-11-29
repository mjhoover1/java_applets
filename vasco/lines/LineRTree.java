package vasco.lines;

import java.awt.Rectangle;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: LineRTree.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.QueryObject;
import vasco.common.RTree;
import vasco.common.RebuildTree;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;

public class LineRTree extends LineStructure {
	RTree rt;

	public LineRTree(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
		rt = new RTree(can, 3, 6, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ops) {
		super.reInit(ops);
		rt.reInit(ops);
	}

	@Override
	public boolean orderDependent() {
		return rt.orderDependent();
	}

	@Override
	public boolean Insert(DLine r) {
		return rt.Insert(remakeDLine(r));
	}

	@Override
	public void Delete(DPoint p) {
		DLine dl = (DLine) rt.NearestFirst(new QueryObject(p));
		DeleteDirect(dl);
	}

	@Override
	public void DeleteDirect(Drawable mx) {
		DLine dl = (DLine) mx;
		if (dl != null) {
			rt.DeleteDirect(mx);
			deletePoint(dl.p1);
			deletePoint(dl.p2);
		}
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
	public Drawable NearestFirst(QueryObject p) {
		return rt.NearestFirst(p);
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
		return rt.NearestRange(p, dist);
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
