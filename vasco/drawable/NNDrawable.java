package vasco.drawable;

import vasco.common.*;
import java.awt.*;

public class NNDrawable extends GenDrawable {
	int counter;

	public NNDrawable(Drawable r, int ct) {
		super(r);
		counter = ct;
	}

	public void drawElementNext(DrawingTarget g) {
		drawable.draw(g);
		g.drawString(String.valueOf(counter), drawable.getBB().x + drawable.getBB().width / 2,
				drawable.getBB().y + drawable.getBB().height / 2);
	}

	public int pauseMode() {
		return SUCCESS;
	}
}
