/* $Id: SVElement.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.Color;
import java.util.Vector;

public class SVElement extends AnimElement {
	DRectangle[] cyan;

	private static DRectangle[] storeRect(Vector src) {
		DRectangle[] rec = new DRectangle[src.size()];
		src.copyInto(rec);
		return rec;
	}

	public SVElement(GenElement e, Vector c) {
		ge = e;
		cyan = storeRect(c);
	}

	void drawCyan(DrawingTarget g) {
		g.setColor(Color.cyan);
		for (DRectangle element : cyan)
			g.fillRect(element.x, element.y, element.width, element.height);
	}

}
