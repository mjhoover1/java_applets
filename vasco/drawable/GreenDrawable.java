package vasco.drawable;

import vasco.common.*;
import java.awt.*;

public class GreenDrawable extends GenDrawable {

	public GreenDrawable(Drawable r) {
		super(r);
	}

	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.green);
		drawable.draw(g);
	};

	public void drawElementNext(DrawingTarget g) {
		drawElementFirst(g);
	};

	public int pauseMode() {
		return BASIC;
	}

}
