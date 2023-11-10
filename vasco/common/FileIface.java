/* $Id: FileIface.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.util.*;

public interface FileIface {
	public Vector vectorOut();

	public void vectorIn(Vector in);

	public String[] stringsOut();

	boolean testCoordinates(DPoint c);

	DPoint randomDPoint();
}
