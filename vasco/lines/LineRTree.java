package vasco.lines;

/* $Id: LineRTree.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import java.awt.*;

public class LineRTree extends LineStructure {
	RTree rt;

	public LineRTree(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
		rt = new RTree(can, 3, 6, p, r);
	}

	public void reInit(Choice ops) {
		super.reInit(ops);
		rt.reInit(ops);
	}

	public boolean orderDependent() {
		return rt.orderDependent();
	}

	public boolean Insert(DLine r) {
		return rt.Insert(remakeDLine(r));
	}

	public void Delete(DPoint p) {
		DLine dl = (DLine) rt.NearestFirst(new QueryObject(p));
		DeleteDirect(dl);
	}

	public void DeleteDirect(Drawable mx) {
		DLine dl = (DLine) mx;
		if (dl != null) {
			rt.DeleteDirect(mx);
			deletePoint(dl.p1);
			deletePoint(dl.p2);
		}
	}

	public void MessageStart() {
		// super.MessageStart();
		rt.MessageStart();
	};

	public void Clear() {
		super.Clear();
		rt.Clear();
	}

	public void MessageEnd() {
		// super.MessageEnd();
		rt.MessageEnd();
	};

	public SearchVector Search(QueryObject r, int mode) {
		return rt.Search(r, mode);
	}

	public SearchVector Nearest(QueryObject p) {
		return rt.Nearest(p);
	}

	public SearchVector Nearest(QueryObject p, double dist) {
		return rt.Nearest(p, dist);
	}

	public Drawable NearestFirst(QueryObject p) {
		return rt.NearestFirst(p);
	}

	public Drawable[] NearestRange(QueryObject p, double dist) {
		return rt.NearestRange(p, dist);
	}

	public void drawContents(DrawingTarget g, Rectangle view) {
		rt.drawContents(g, view);
	}

	public void drawGrid(DrawingTarget g, int level) {
		rt.drawGrid(g, level);
	};

	public String getName() {
		return rt.getName();
	}

}
