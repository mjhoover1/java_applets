package vasco.regions;

import java.util.Vector;

import vasco.common.DrawingTarget;

public class CursorStyle {
	protected Vector v;
	boolean valid;

	public CursorStyle() {
		v = new Vector();
		valid = true;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public boolean getValid() {
		return valid;
	}

	public void add(CursorStyleInterface cs) {
		v.addElement(cs);
	}

	public void display(DrawingTarget dt) {
		for (int x = 0; x < v.size(); x++)
			((CursorStyleInterface) v.elementAt(x)).display(dt);
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (v.size() != ((CursorStyle) obj).v.size()))
			return false;

		for (int x = 0; x < v.size(); x++)
			if ((!((CursorStyleInterface) v.elementAt(x)).equals(((CursorStyle) obj).v.elementAt(x))))
				return false;

		return true;
	}

}
