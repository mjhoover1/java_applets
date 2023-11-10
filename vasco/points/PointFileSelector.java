package vasco.points;

/* $Id: PointFileSelector.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
import java.util.StringTokenizer;
import java.util.Vector;

import vasco.common.DPoint;
import vasco.common.FileIface;
import vasco.common.TopInterface;
import vasco.common.fileSelector;

class PointFileSelector extends fileSelector {

	PointFileSelector(FileIface rc, String act, TopInterface ti) { // action = { SAVE | LOAD }
		super(rc, "POINTS", "Data format:\n<x> <y>", act, ti);
	}

	protected String genRandom(int nr) {
		String s = "";
		for (int i = 0; i < nr; i++) {
			DPoint p;
			p = rcanvas.randomDPoint();
			s += p.x + " " + p.y + " ";
		}
		return s;
	}

	protected void formVector(String s, Vector v) {
		double x1, y1;
		StringTokenizer st = new StringTokenizer(s);
		try {
			while (st.hasMoreTokens()) {
				DPoint p;
				x1 = new Double(st.nextToken()).doubleValue();
				y1 = new Double(st.nextToken()).doubleValue();
				p = new DPoint(x1, y1);
				if (rcanvas.testCoordinates(p))
					v.addElement(p);
			}
		} catch (Exception e) {
			System.out.println("LineFileSelector: formVector : " + e.toString());
			v = null;
		}
		if (v != null) {
			rcanvas.vectorIn(v);
		}
	}

}
