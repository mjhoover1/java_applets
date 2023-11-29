/* $Id: NNElement.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.util.Vector;

import vasco.drawable.NNDrawable;

public class NNElement extends AnimElement {
	GenElement[] nq;
	double dist;

	private static GenElement[] storeGen(Vector src) {
		GenElement[] rec = new GenElement[src.size()];
		src.copyInto(rec);
		return rec;
	}

	public NNElement(GenElement e, double dist, Vector c) {
		ge = e;
		this.dist = dist;
		nq = storeGen(c);
	}

	void drawQueue(DrawingTarget g) {
		for (GenElement element : nq)
			element.drawElementFirst(g);
	}

	void fillQueue(DrawingTarget g) {
		for (GenElement element : nq)
			element.fillElementFirst(g);
	}

	boolean isElement() {
		return (ge instanceof NNDrawable);
	}

}
