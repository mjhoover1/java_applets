/* $Id: SearchVector.java,v 1.2 2002/09/25 20:55:06 brabec Exp $ */
package vasco.common;

import java.util.*;
import java.awt.*;

public class SearchVector {
	protected Vector sv;

	public SearchVector() {
		sv = new Vector();
	}

	public AnimElement elementAt(int i) {
		return (AnimElement) sv.elementAt(i);
	}

	public void addElement(AnimElement e) {
		sv.addElement(e);
	}

	public int size() {
		return sv.size();
	}
}
