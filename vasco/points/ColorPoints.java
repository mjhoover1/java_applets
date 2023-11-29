package vasco.points;

/* $Id: ColorPoints.java,v 1.2 2007/10/28 15:38:17 jagan Exp $ */
import java.util.Vector;

import vasco.common.DPoint;
import vasco.common.DrawingTarget;
import vasco.common.GenElement;

abstract public class ColorPoints implements GenElement {
	DPoint[] p;

	private static DPoint[] storePoints(Vector src) {
		DPoint[] rec = new DPoint[src.size()];
		src.copyInto(rec);
		return rec;
	}

	ColorPoints(Vector v) {
		p = storePoints(v);
	}

	ColorPoints(DPoint pt) {
		p = new DPoint[1];
		p[0] = pt;
	}

	abstract void setColor(DrawingTarget g);

	@Override
	public void drawElementFirst(DrawingTarget g) {
		setColor(g);
		for (DPoint element : p) {
			element.draw(g);
		}
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
		drawElementFirst(g);
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public int pauseMode() {
		return BASIC;
	}
}
