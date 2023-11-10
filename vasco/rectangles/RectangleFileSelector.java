package vasco.rectangles;

/* $Id: RectangleFileSelector.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import java.awt.*;
import java.util.*;
import vasco.common.*;

class RectangleFileSelector extends fileSelector {

	RectangleFileSelector(FileIface rc, String act, TopInterface ti) { // action = { SAVE | LOAD }
		super(rc, "RECTANGLES", "Data format:\n<x> <y> <width> <height>", act, ti);
	}

	protected String genRandom(int nr) {
		String s = "";
		for (int i = 0; i < nr; i++) {
			DPoint p1 = rcanvas.randomDPoint();
			DPoint p2 = rcanvas.randomDPoint();
			s += Math.min(p1.x, p2.x) + " " + Math.min(p1.y, p2.y) + " " + Math.abs(p1.x - p2.x) + " "
					+ Math.abs(p1.y - p2.y) + " ";
		}
		return s;
	}

	protected void formVector(String s, Vector v) {
		double x, y, width, height;
		StringTokenizer st = new StringTokenizer(s);
		try {
			while (st.hasMoreTokens()) {
				x = new Double(st.nextToken()).doubleValue();
				y = new Double(st.nextToken()).doubleValue();
				width = new Double(st.nextToken()).doubleValue();
				height = new Double(st.nextToken()).doubleValue();
				if (rcanvas.testCoordinates(new DPoint(x, y))
						&& rcanvas.testCoordinates(new DPoint(x + width, y + height)))
					v.addElement(new DRectangle(x, y, width, height));
			}
		} catch (Exception e) {
			System.out.println("RectangleLineSelector: formVector : " + e.toString());
			v = null;
		}
		;
		if (v != null) {
			rcanvas.vectorIn(v);
		}
	}
}
