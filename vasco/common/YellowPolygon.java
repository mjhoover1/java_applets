package vasco.common;

import java.awt.Color;
import java.util.Vector;

public class YellowPolygon implements GenElement {
	DPolygon[] l;

	private static DPolygon[] storeLines(Vector src) {
		DPolygon[] rec = new DPolygon[src.size()];
		src.copyInto(rec);
		return rec;
	}

	YellowPolygon(DPolygon r) {
		l = new DPolygon[1];
		l[0] = r;
	}

	YellowPolygon(Vector v) {
		l = storeLines(v);
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.yellow);
		for (DPolygon element : l) {
			element.draw(g);
			// g.drawLine(l[i].p1.x, l[i].p1.y, l[i].p2.x, l[i].p2.y);
		}
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
