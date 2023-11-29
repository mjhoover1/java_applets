/* $Id: YellowBlock.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.Color;
import java.awt.Rectangle;

public class YellowBlock extends DRectangle implements GenElement {

	boolean leaf;

	public YellowBlock(double xx, double yy, double w, double h, boolean l) {
		super(xx, yy, w, h);
		leaf = l;
	}

	public YellowBlock(DRectangle r, boolean l) {
		super(r.x, r.y, r.width, r.height);
		leaf = l;
	}

	public YellowBlock(Rectangle r, boolean l) {
		super(r.x, r.y, r.width, r.height);
		leaf = l;
	}

	// ---------------------------------

	@Override
	public void fillElementFirst(DrawingTarget g) {
		g.setColor(Color.yellow);
		g.fillRect(x, y, width, height);
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
		if (leaf) {
			g.setColor(Color.lightGray);
			g.fillRect(x, y, width, height);
		}
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

	@Override
	public int pauseMode() {
		return BASIC;
	}

}
