/* $Id: MouseHelp.java,v 1.2 2002/09/25 20:55:04 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;

public class MouseHelp extends MouseAdapter implements ItemListener {
	String b1, b2, b3;
	MouseDisplay md;
	Component c;
	String[] front, back, current;
	int buttonMask;

	public MouseHelp() {
		front = new String[3];
		back = new String[3];
	}

	public MouseHelp(Component c, MouseDisplay md, String b1, String b2, String b3) {
		this(c, md, b1, b2, b3, InputEvent.BUTTON1_MASK);
	}

	public MouseHelp(Component ckbox, MouseDisplay md, String b1, String b2, String b3, String c1, String c2,
			String c3) {
		this(ckbox, md, b1, b2, b3, c1, c2, c3, InputEvent.BUTTON1_MASK);
	}

	public MouseHelp(Component c, MouseDisplay md, String b1, String b2, String b3, int mask) {
		this();
		front[0] = b1;
		front[1] = b2;
		front[2] = b3;
		current = front;

		this.md = md;
		this.c = c;
		buttonMask = mask;
		c.addMouseListener(this);
	}

	public MouseHelp(Component ckbox, MouseDisplay md, String b1, String b2, String b3, String c1, String c2, String c3,
			int mask) {
		this();

		front[0] = b1;
		front[1] = b2;
		front[2] = b3;

		back[0] = c1;
		back[1] = c2;
		back[2] = c3;

		current = front;

		if (ckbox instanceof Checkbox) {
			((Checkbox) ckbox).addItemListener(this);
			current = ((Checkbox) ckbox).getState() ? back : front;
		}

		this.md = md;
		this.c = ckbox;
		buttonMask = mask;
		c.addMouseListener(this);
	}

	public int getMask() {
		return buttonMask;
	}

	public void changeHelp(int mask, String b1, String b2, String b3) {
		front[0] = b1;
		front[1] = b2;
		front[2] = b3;
		current = front;

		buttonMask = mask;
	}

	public void swapHelp() {
		current = (current == front) ? back : front;
	}

	public void frontHelp() {
		current = front;
	}

	public void backHelp() {
		current = back;
	}

	public void show() {
		md.show(buttonMask, current[0], current[1], current[2]);
	}

	public void mouseEntered(MouseEvent me) {
		show();
	}

	public void mouseExited(MouseEvent me) {
		md.clear(buttonMask);
	}

	public void removeHelp() {
		c.removeMouseListener(this);
	}

	public void itemStateChanged(ItemEvent ie) {
		current = ((Checkbox) c).getState() ? back : front;
		show();
	}
}
