package vasco.rectangles;

/* $Id: GreenRect.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import java.awt.*;

public class GreenRect extends DRectangle implements GenElement {

	GreenRect(DRectangle r) {
		super(r.x, r.y, r.width, r.height);
	}

	public void fillElementFirst(DrawingTarget g) {
	};

	public void fillElementNext(DrawingTarget g) {
	};

	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.green);
		g.drawRect(x, y, width, height);
	};

	public void drawElementNext(DrawingTarget g) {
		drawElementFirst(g);
	};

	public int pauseMode() {
		return BASIC;
	}

}
