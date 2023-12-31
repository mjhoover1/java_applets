/* $Id: appletSwitch.java,v 1.3 2003/01/30 04:05:52 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.net.*;

/**
 * Represents a switch panel for different applet demonstrations in a spatial
 * data structure context.
 */
public class appletSwitch extends Panel implements ItemListener {
	// Constants representing different types of applets
	public final static int POINTS = 0;
	public final static int LINES = 1;
	public final static int RECTANGLES = 2;
	public final static int AREA = 3;

	// URLs corresponding to different applets
	final static String[] urls = { "/quadtree/points.html", "/quadtree/lines.html", "/quadtree/rectangles.html",
			"/quadtree/regions.html" };

	Choice ch; // Choice component for selecting applets
	Applet applet; // Reference to the parent applet
	int initValue; // Initial value for the choice component

	/**
	 * Constructs the applet switch panel.
	 *
	 * @param iv Initial value for the choice component.
	 * @param ac Reference to the parent applet.
	 * @param ti TopInterface instance for accessing mouse display.
	 */
	public appletSwitch(int iv, Applet ac, TopInterface ti) {
		ch = new Choice();
		new MouseHelp(ch, ti.getMouseDisplay(), "Go to other spatial demo applets", "", "");
		ch.addItem("Point Applet");
		ch.addItem("Line Applet");
		ch.addItem("Rectangle Applet");
		ch.addItem("Region Applet");
		applet = ac;
		initValue = iv;
		add(ch);
		ch.select(initValue);
		ch.addItemListener(this);
	}

	/**
	 * Handles item state changes in the choice component.
	 *
	 * @param ie The item event triggered by a change in the choice component.
	 */
	public void itemStateChanged(ItemEvent ie) {
		try {
			// Navigate to the selected applet's URL
			applet.getAppletContext()
					.showDocument(new java.net.URL(applet.getCodeBase() + urls[ch.getSelectedIndex()]));
		} catch (MalformedURLException e) {
			// Handle potential MalformedURLException
		}
		// Reset the choice component to its initial value
		ch.select(initValue);
	}

}
