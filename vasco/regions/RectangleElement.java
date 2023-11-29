package vasco.regions;

import java.awt.Color;
import java.awt.Rectangle;

import vasco.common.DrawingTarget;

public class RectangleElement extends ConvertGenElement {
	Rectangle r;
	Color color;
	boolean filled;
	int width;

	RectangleElement() {
		super();
		r = null;
		color = null;
		filled = false;
		width = 1;
		mCopy = true;
	}

	RectangleElement(Rectangle rect, Color c, int w, boolean f, boolean mcp) {
		super();
		r = rect;
		color = c;
		filled = f;
		width = w;
		mCopy = mcp;
	}

	@Override
	public boolean makeCopy() {
		return mCopy;
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(color);
		if (filled)
			g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
		else
			g.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);

	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

	@Override
	public int pauseMode() {
		return 0;
	}

}
