package vasco.drawable;

import java.awt.Color;

import vasco.common.DrawingTarget;

public class GreenDrawable extends GenDrawable {

	public GreenDrawable(Drawable r) {
		super(r);
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.green);
		drawable.draw(g);
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
