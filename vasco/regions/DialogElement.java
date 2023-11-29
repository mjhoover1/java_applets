package vasco.regions;

import vasco.common.DrawingTarget;

public class DialogElement extends ConvertGenElement {
	String text;
	StructureBox si;

	public DialogElement(StructureBox si, String text) {
		this.si = si;
		this.text = text;
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		if (si != null)
			si.setText(text);
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

}
