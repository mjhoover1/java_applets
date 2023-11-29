package vasco.rectangles;

import java.awt.Color;

/* $Id: GreenRect.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.GenElement;

public class GreenRect extends DRectangle implements GenElement {

	GreenRect(DRectangle r) {
		super(r.x, r.y, r.width, r.height);
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.green);
		g.drawRect(x, y, width, height);
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
		drawElementFirst(g);
	}

	@Override
	public int pauseMode() {
		return BASIC;
	}

}
