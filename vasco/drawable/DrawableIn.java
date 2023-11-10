package vasco.drawable;

import java.awt.*;
import vasco.common.*;

public class DrawableIn extends GenDrawable {
	public DrawableIn(Drawable r) {
		super(r);
	}

	public void drawElementNext(DrawingTarget g) {
		g.setColor(Color.blue);
		drawable.draw(g);
	}

	public int pauseMode() {
		return SUCCESS;
	}
}
