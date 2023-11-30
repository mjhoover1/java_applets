/*
 * The MouseDisplay class provides a graphical representation of mouse events in a Java application.
 * $Id: MouseDisplay.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $
 */
package vasco.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

// import java.awt.*;
import javax.swing.JPanel;

public class MouseDisplay extends JPanel {

	private Image im;
	private static final int HEIGHT = 80;
	private static final String middleBut = "[<ALT>+click]";
	private static final String rightBut = "[<CTRL>+click]";

	String b1, b2, b3;

	/**
	 * Constructs a MouseDisplay object with the specified size and image.
	 *
	 * @param sz The size of the MouseDisplay
	 * @param im The image used for display
	 */
	public MouseDisplay(int sz, Image im) {
		setPreferredSize(new Dimension(sz, HEIGHT)); // setSize(sz, HEIGHT);
		this.im = im;
		b1 = b2 = b3 = "";
	}

	/**
	 * Updates the display based on the mouse event mask and button labels.
	 *
	 * @param mask The mouse event mask
	 * @param b1   Label for button 1
	 * @param b2   Label for button 2
	 * @param b3   Label for button 3
	 */
	public void show(int mask, String b1, String b2, String b3) {
		if ((mask & InputEvent.BUTTON1_MASK) != 0) this.b1 = b1;
		if ((mask & InputEvent.BUTTON2_MASK) != 0) this.b2 = b2;
		if ((mask & InputEvent.BUTTON3_MASK) != 0) this.b3 = b3;
		repaint();
	}

	/**
	 * Clears all button labels on the display.
	 */
	public void clear() {
		b1 = b2 = b3 = "";
		repaint();
	}

	/**
	 * Clears the specified button label on the display.
	 *
	 * @param mask The mouse event mask for the button to clear
	 */
	public void clear(int mask) {
		if ((mask & InputEvent.BUTTON1_MASK) != 0) b1 = "";
		if ((mask & InputEvent.BUTTON2_MASK) != 0) b2 = "";
		if ((mask & InputEvent.BUTTON3_MASK) != 0) b3 = "";
		repaint();
	}

	/**
	 * Gets the mouse buttons based on the MouseEvent.
	 *
	 * @param me The MouseEvent
	 * @return The mouse buttons as a mask
	 */
	public static int getMouseButtons(MouseEvent me) {
		int ret = 0;
		if (me.isAltDown()) ret |= InputEvent.BUTTON2_MASK;
		if (me.isControlDown()) ret |= InputEvent.BUTTON3_MASK;
		if (!me.isAltDown() && !me.isControlDown()) ret |= InputEvent.BUTTON1_MASK;
		return ret;
	}

	/**
	 * Overrides the imageUpdate method to ignore image loading status.
	 */
	@Override
	public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
		// HACK - ignore image not being fully loaded (for some unknown reason)
		return true;
	}

	/**
	 * Paints the graphical representation of mouse events.
	 *
	 * @param mdG The Graphics object for painting
	 */
	@Override
	protected void paintComponent(Graphics mdG) {
		super.paintComponent(mdG);
		FontMetrics fm = mdG.getFontMetrics();
		Dimension dim = getSize();
		mdG.setColor(Color.black);
		mdG.drawImage(im, dim.width / 2 - im.getWidth(this) / 2, dim.height - im.getHeight(this), this);
		mdG.drawString(b1, dim.width / 2 - im.getWidth(this) / 2 - fm.stringWidth(b1),
				dim.height - im.getHeight(this) / 2);
		mdG.drawString(b2, dim.width / 2 - fm.stringWidth(b2) / 2, dim.height - im.getHeight(this));
		mdG.drawString(b3, dim.width / 2 + im.getWidth(this) / 2, dim.height - im.getHeight(this) / 2);
		mdG.setColor(Color.blue);
		if (b2.length() > 0)
			mdG.drawString(middleBut, dim.width / 2 - fm.stringWidth(middleBut) / 2,
					dim.height - im.getHeight(this) - fm.getHeight());
		if (b3.length() > 0)
			mdG.drawString(rightBut, dim.width / 2 + im.getWidth(this) / 2,
					dim.height - im.getHeight(this) / 2 + fm.getHeight());
	}

	/**
	 * Paints the graphical representation of mouse events.
	 *
	 * @param mdG The Graphics object for painting
	 */
	// public void paint(Graphics mdG) {
	// FontMetrics fm = mdG.getFontMetrics();
	// Dimension dim = getSize();
	// mdG.setColor(Color.black);
	// mdG.drawImage(im, dim.width / 2 - im.getWidth(this) / 2, dim.height -
	// im.getHeight(this), this);
	// mdG.drawString(b1, dim.width / 2 - im.getWidth(this) / 2 -
	// fm.stringWidth(b1),
	// dim.height - im.getHeight(this) / 2);
	// mdG.drawString(b2, dim.width / 2 - fm.stringWidth(b2) / 2, dim.height -
	// im.getHeight(this));
	// mdG.drawString(b3, dim.width / 2 + im.getWidth(this) / 2, dim.height -
	// im.getHeight(this) / 2);
	// mdG.setColor(Color.blue);
	// if (b2.length() > 0)
	// mdG.drawString(middleBut, dim.width / 2 - fm.stringWidth(middleBut) / 2,
	// dim.height - im.getHeight(this) - fm.getHeight());
	// if (b3.length() > 0)
	// mdG.drawString(rightBut, dim.width / 2 + im.getWidth(this) / 2,
	// dim.height - im.getHeight(this) / 2 + fm.getHeight());
	// }
}
