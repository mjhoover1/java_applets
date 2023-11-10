/* $Id: appletSwitch.java,v 1.3 2003/01/30 04:05:52 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.net.*;

public class appletSwitch extends Panel implements ItemListener {
  public final static int POINTS = 0;
  public final static int LINES = 1;
  public final static int RECTANGLES = 2;
  public final static int AREA = 3;

  final static String[] urls = {
    "/quadtree/points.html", 
    "/quadtree/lines.html", 
    "/quadtree/rectangles.html", 
    "/quadtree/regions.html"
  };

  Choice ch;
  Applet applet;
  int initValue;

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

    public void itemStateChanged(ItemEvent ie) {
	try {
	    applet.getAppletContext().showDocument(new java.net.URL(applet.getCodeBase() + urls[ch.getSelectedIndex()]));
	} catch (MalformedURLException e) {
	};
	ch.select(initValue);
    }

}



