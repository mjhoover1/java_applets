/* $Id: GenElement.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

public interface GenElement {
	public static final int BASIC = 0;
	public static final int FAIL = 1;
	public static final int SUCCESS = 2;

	void fillElementFirst(DrawingTarget g);

	void fillElementNext(DrawingTarget g);

	void drawElementFirst(DrawingTarget g); /* on top of the event vector */

	void drawElementNext(DrawingTarget g);

	int pauseMode();
}
