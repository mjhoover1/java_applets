package vasco.drawable;

import java.awt.Color;

import vasco.common.DrawingTarget;

public class DrawableOut extends GenDrawable {
	public DrawableOut(Drawable r) {
		super(r);
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
		g.setColor(Color.magenta);
//		drawable.draw(g);
		drawable.directDraw(Color.magenta, g);
	}

	@Override
	public int pauseMode() {
		return FAIL;
	}
}
