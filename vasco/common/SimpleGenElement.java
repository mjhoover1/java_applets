package vasco.common;

import java.awt.Color;
import java.util.Vector;

import vasco.drawable.Drawable;

public class SimpleGenElement implements GenElement {
	Vector elements = new Vector();

	private class Element {
		public Drawable draw;
		public Color c;

		public Element(Drawable draw, Color c) {
			this.draw = draw;
			this.c = c;
		}
	}

	public SimpleGenElement() {

	}

	public void addLine(DPoint p1, DPoint p2, Color c) {
		DLine d = new DLine(p1, p2);
		addLine(d, c);
	}

	public void addLine(DLine p1, Color c) {
		Element e = new Element(p1, c);
		elements.add(e);
	}

	public void addPoint(DPoint p1, Color c) {
		Element e = new Element(p1, c);
		elements.add(e);
	}

	public void addPolygon(DPolygon p1, Color c) {
		Element e = new Element(p1, c);
		elements.add(e);
	}

	public void fillElementFirst(DrawingTarget g) {
	};

	public void fillElementNext(DrawingTarget g) {
	};

	public void drawElementFirst(DrawingTarget g) {
		for (int i = 0; i < elements.size(); i++) {
			Element e = (Element) elements.get(i);
			g.setColor(e.c);
			e.draw.draw(g);
		}
	};

	public void drawElementNext(DrawingTarget g) {
		drawElementFirst(g);
	};

	public int pauseMode() {
		return BASIC;
	}
}
