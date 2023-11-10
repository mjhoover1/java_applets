package vasco.rectangles;

/* $Id: RectTree.java,v 1.3 2007/10/28 15:38:19 jagan Exp $ */
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class RectTree extends GenRectTree {

	public RectTree(DRectangle can, int md, TopInterface p, RebuildTree r) {
		super(can, md, 1, p, r);
	}

	public String getName() {
		return "Rectangle Quadtree";
	}

	public boolean orderDependent() {
		return false;
	}

}
