/* $Id: QueueBlock.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.Color;
import java.awt.Rectangle;

public class QueueBlock extends DRectangle implements GenElement {

	public QueueBlock(double xx, double yy, double w, double h) {
		super(xx, yy, w, h);
	}

	public QueueBlock(DRectangle r) {
		super(r.x, r.y, r.width, r.height);
	}

	public QueueBlock(Rectangle r) {
		super(r.x, r.y, r.width, r.height);
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
		g.setColor(Color.cyan);
		g.fillRect(x, y, width, height);
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
	}

	@Override
	public int pauseMode() {
		return BASIC;
	}

}
