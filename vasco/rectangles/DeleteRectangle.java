package vasco.rectangles;

/* $Id: DeleteRectangle.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.DPoint;
import vasco.common.DRectangle;

class DeleteRectangle extends DRectangle {
	DeleteRectangle(double xx, double yy) {
		super(xx, yy, 0, 0);
	}

	DeleteRectangle(DPoint p) {
		super(p.x, p.y, 0, 0);
	}

	DPoint getPoint() {
		return new DPoint(x, y);
	}
}
