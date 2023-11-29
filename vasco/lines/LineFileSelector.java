package vasco.lines;

import java.util.StringTokenizer;
import java.util.Vector;

import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.FileIface;
import vasco.common.TopInterface;
import vasco.common.fileSelector;

class LineFileSelector extends fileSelector {

	LineFileSelector(FileIface rc, String act, TopInterface ti) { // action = { SAVE | LOAD }
		super(rc, "LINES", "Data format:\n<x1> <y1> <x2> <y2>", act, ti);
	}

	@Override
	protected String genRandom(int nr) {
		String s = "";
		for (int i = 0; i < nr; i++) {
			DPoint p1, p2;
			p1 = rcanvas.randomDPoint();
			p2 = rcanvas.randomDPoint();
			s += p1.x + " " + p1.y + " " + p2.x + " " + p2.y + " ";
		}
		return s;
	}

	@Override
	protected void formVector(String s, Vector v) {
		double x1, y1, x2, y2;
		StringTokenizer st = new StringTokenizer(s);
		try {
			while (st.hasMoreTokens()) {
				DPoint p1, p2;
				x1 = new Double(st.nextToken()).doubleValue();
				y1 = new Double(st.nextToken()).doubleValue();
				p1 = new DPoint(x1, y1);
				x2 = new Double(st.nextToken()).doubleValue();
				y2 = new Double(st.nextToken()).doubleValue();
				p2 = new DPoint(x2, y2);
				// System.out.println(x + " " + y + " " + width + " " + height);
				if (rcanvas.testCoordinates(p1) && rcanvas.testCoordinates(p2))
					v.addElement(new DLine(p1, p2));
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
