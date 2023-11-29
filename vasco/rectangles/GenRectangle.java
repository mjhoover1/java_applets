package vasco.rectangles;

import java.awt.Color;

/* $Id: GenRectangle.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.GenElement;

abstract public class GenRectangle extends DRectangle implements GenElement {
	public GenRectangle(DRectangle r) {
		super(r.x, r.y, r.width, r.height);
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.yellow);
		draw(g);
	}

	@Override
	public abstract void drawElementNext(DrawingTarget g);

	@Override
	public abstract int pauseMode();

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

}
