package vasco.regions;

import java.awt.Color;
import java.awt.Point;

import vasco.common.DrawingTarget;

public class LineElement extends ConvertGenElement {
	Point b, e;
	Color color;
	String label;
	static final int width = 1;
	static Grid grid;
	int dir;
	int repeatCount;

	LineElement() {
		super();
		b = null;
		e = null;
		color = null;
		label = null;
		dir = 0;
		grid = null;
		repeatCount = 0;
		mCopy = true;
	}

	LineElement(Grid g, Point beg, Point en, int d, Color c, String l, int rc, boolean mcp) {
		super();
		b = beg;
		e = en;
		color = c;
		label = l;
		dir = d;
		grid = g;
		repeatCount = rc;
		mCopy = mcp;
	}

	@Override
	public boolean makeCopy() {
		return mCopy;
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		g.setColor(color);
		if (b.y == e.y)
			g.fillRect(b.x, b.y - width, e.x - b.x, e.y - b.y + 2 * width);
		else if (b.x == e.x) {
			g.fillRect(b.x - width, b.y, e.x - b.x + 2 * width, e.y - b.y);
		} else {
			g.drawLine(b.x, b.y, e.x, e.y);
		}

		int x, s;
		int no = 0; /* number of grid element */
		int size = grid.getCellSize();

		if (b.y == e.y)
			no = (e.x - b.x) / size;
		else if (b.x == e.x)
			no = (e.y - b.y) / size;

		if (label != null) {
			switch (dir) {
			case 3: /* North */
				s = e.y - size / 2;
				for (x = 0; x < no; x++) {
					g.drawString(label, e.x - 4, s + 9);
					s = s - size;
				}
				break;
			case 2: /* West */
				s = e.x - size / 2;
				for (x = 0; x < no; x++) {
					g.drawString(label, s, e.y + 12);
					s = s - size;
				}
				break;
			case 1: /* south */
				s = b.y + size / 2;
				for (x = 0; x < no; x++) {
					g.drawString(label, b.x + 4, s + 9);
					s = s + size;
				}
				break;
			case 0: /* East */
				s = b.x + size / 2;
				for (x = 0; x < no; x++) {
					g.drawString(label, s, b.y);
					s = s + size;
				}
				break;
			}
		}

	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

	@Override
	public int pauseMode() {
		return 0;
	}

}
