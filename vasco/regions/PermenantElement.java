package vasco.regions;

import java.util.Vector;

import vasco.common.DrawingTarget;

public class PermenantElement extends ConvertGenElement {
	ConvertVector cv;
	ConvertGenElement elm;
	int index;

	public PermenantElement(ConvertVector cv, ConvertGenElement elm) {
		this.cv = cv;
		this.elm = elm;
		index = cv.addPermenant(elm);
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		int x;
		Vector p = cv.getPermenant();
		ConvertGenElement ge;

		for (x = 0; x <= index; x++) {
			ge = (ConvertGenElement) p.elementAt(x);
			if (ge != null)
				ge.drawElementFirst(g);
		}
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

}
