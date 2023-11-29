package vasco.lines;

import java.awt.Color;
import java.util.Vector;

/* $Id: MagentaLines.java,v 1.1 2007/10/29 01:19:53 jagan Exp $ */
import vasco.common.DLine;
import vasco.common.DrawingTarget;
import vasco.common.GenElement;

public class MagentaLines implements GenElement {
	DLine[] l;

	private static DLine[] storeLines(Vector src) {
		DLine[] rec = new DLine[src.size()];
		src.copyInto(rec);
		return rec;
	}

	public MagentaLines(DLine r) {
		l = new DLine[1];
		l[0] = r;
	}

	public MagentaLines(Vector v) {
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
		g.setColor(Color.magenta);
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
