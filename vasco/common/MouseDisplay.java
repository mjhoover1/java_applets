/* $Id: MouseDisplay.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public class MouseDisplay extends Canvas {
    private Image im;
    private static int HGHT = 80;
    private static String middleBut = "[<ALT>+click]";
    private static String rightBut = "[<META>+click]";

    String b1, b2, b3;

    public MouseDisplay(int sz, Image im) {
	setSize(sz, HGHT);
	this.im = im;
	b1 = b2 = b3 = "";
    }

    public void show(int mask, String b1, String b2, String b3) {
	if ((mask & InputEvent.BUTTON1_MASK) != 0)
	    this.b1 = b1; 
	if ((mask & InputEvent.BUTTON2_MASK) != 0)
	    this.b2 = b2;
	if ((mask & InputEvent.BUTTON3_MASK) != 0)
	    this.b3 = b3;
	repaint();
    }

    public void clear() {
	b1 = b2 = b3 = "";
	repaint();
    }

    public void clear(int mask) {
	if ((mask & InputEvent.BUTTON1_MASK) != 0)
	    b1 = ""; 
	if ((mask & InputEvent.BUTTON2_MASK) != 0)
	    b2 = ""; 
	if ((mask & InputEvent.BUTTON3_MASK) != 0)
	    b3 = "";
	repaint();
    }

    public static int getMouseButtons(MouseEvent me) {
	int ret = 0;
	if (me.isAltDown())
	    ret |= InputEvent.BUTTON2_MASK;
	if (me.isMetaDown())
	    ret |= InputEvent.BUTTON3_MASK;
	if (!me.isAltDown() && !me.isMetaDown())
	    ret |= InputEvent.BUTTON1_MASK;
	return ret;
    }

    public boolean imageUpdate(Image img, int flags,
                               int x, int y, int w, int h) {
	// HACK - ignore image not being fully loaded (for some unknown reason)
	//	System.out.println(x + " " + y +" " + w + " " + h + "flags " + (ALLBITS & flags));
	//	System.out.println((FRAMEBITS & flags) + " " + (HEIGHT & flags) + " " + (SOMEBITS & flags));
	return true;
    }

    public void paint(Graphics mdG) {
	//	Graphics mdG = getGraphics();
	FontMetrics fm = mdG.getFontMetrics();
	Dimension dim = getSize();
	mdG.setColor(Color.black);
	mdG.drawImage(im, dim.width / 2 - im.getWidth(this) / 2, dim.height - im.getHeight(this), this);
	mdG.drawString(b1, dim.width / 2 - im.getWidth(this) / 2 - fm.stringWidth(b1), 
		       dim.height - im.getHeight(this)/2);
	mdG.drawString(b2, dim.width / 2 - fm.stringWidth(b2)/2, dim.height - im.getHeight(this));
	mdG.drawString(b3, dim.width / 2 + im.getWidth(this) / 2, dim.height - im.getHeight(this)/2);
	mdG.setColor(Color.blue);
	if (b2.length() > 0)
	    mdG.drawString(middleBut, dim.width / 2 - fm.stringWidth(middleBut)/2, 
			   dim.height - im.getHeight(this) - fm.getHeight());
	if (b3.length() > 0)
	    mdG.drawString(rightBut, dim.width / 2 + im.getWidth(this) / 2, 
			   dim.height - im.getHeight(this)/2 + fm.getHeight());

    }
}

    
