package vasco.drawable;

import java.awt.Color;

import vasco.common.DrawingTarget;
import vasco.common.GenElement;

abstract public class GenDrawable implements GenElement {
	Drawable drawable;

	GenDrawable(Drawable r) {
		drawable = r;
	}

	void draw(DrawingTarget g) {
		drawable.draw(g);
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.yellow);
		drawable.draw(g);
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
