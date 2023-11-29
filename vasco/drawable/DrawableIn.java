package vasco.drawable;

import java.awt.Color;

import vasco.common.DrawingTarget;

public class DrawableIn extends GenDrawable {
	public DrawableIn(Drawable r) {
		super(r);
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
		g.setColor(Color.blue);
		drawable.draw(g);
	}

	@Override
	public int pauseMode() {
		return SUCCESS;
	}
}
