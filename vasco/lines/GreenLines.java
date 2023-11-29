package vasco.lines;

import java.awt.Color;
import java.util.Vector;

/* $Id: GreenLines.java,v 1.2 2007/10/28 15:38:16 jagan Exp $ */
import vasco.common.DLine;
import vasco.common.DrawingTarget;
import vasco.common.GenElement;

public class GreenLines implements GenElement {
	DLine[] l;

	private static DLine[] storeLines(Vector src) {
		DLine[] rec = new DLine[src.size()];
		src.copyInto(rec);
		return rec;
	}

	public GreenLines(DLine r) {
		l = new DLine[1];
		l[0] = r;
	}

	public GreenLines(Vector v) {
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
		g.setColor(Color.green);
		for (DLine element : l) {
			g.drawLine(element.p1.x, element.p1.y, element.p2.x, element.p2.y);
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
