package vasco.lines;

/* $Id: CyanLines.java,v 1.1 2007/10/29 01:19:53 jagan Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;

public class CyanLines implements GenElement {
	DLine[] l;

	private static DLine[] storeLines(Vector src) {
		DLine[] rec = new DLine[src.size()];
		src.copyInto(rec);
		return rec;
	}

	public CyanLines(DLine r) {
		l = new DLine[1];
		l[0] = r;
	}

	public CyanLines(Vector v) {
		l = storeLines(v);
	}

	public void fillElementFirst(DrawingTarget g) {
	};

	public void fillElementNext(DrawingTarget g) {
	};

	public void drawElementFirst(DrawingTarget g) {
		g.setColor(Color.cyan);
		for (int i = 0; i < l.length; i++) {
			g.drawLine(l[i].p1.x, l[i].p1.y, l[i].p2.x, l[i].p2.y);
		}
	};

	public void drawElementNext(DrawingTarget g) {
		drawElementFirst(g);
	};

	public int pauseMode() {
		return BASIC;
	}

}
