package vasco.drawable;

import vasco.common.DrawingTarget;

public class NNDrawable extends GenDrawable {
	int counter;

	public NNDrawable(Drawable r, int ct) {
		super(r);
		counter = ct;
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
		drawable.draw(g);
		g.drawString(String.valueOf(counter), drawable.getBB().x + drawable.getBB().width / 2,
				drawable.getBB().y + drawable.getBB().height / 2);
	}

	@Override
	public int pauseMode() {
		return SUCCESS;
	}
}
