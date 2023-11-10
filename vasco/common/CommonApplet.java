/* $Id: CommonApplet.java,v 1.3 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class CommonApplet extends Applet implements AppletValidate, ActionListener {

	public void globalValidate() {
		// validate();
	}

	protected final int SIZE = 512;
	protected final int helpWidth = 20;

	protected Panel buttonpanel;
	protected CentralMenu centralmenu;
	protected Label topBar;
	protected TopInterface topInterface;
	protected MouseDisplay mp;
	protected Panel indStructP;
	protected TextArea helpArea;
	protected Button overviewButton;
	protected Dialog overviewDialog;

	protected String treeType;
	protected Panel animp;
	// protected DrawingCanvas can;
	public DrawingCanvas can;
	protected DrawingCanvas overviewCanvas;

	protected GridBagConstraints createConstraints(int gx, int gy, int gw, int gh, int fill) {
		GridBagConstraints dp = new GridBagConstraints();
		dp.gridx = gx;
		dp.gridy = gy;
		dp.gridwidth = gw;
		dp.gridheight = gh;
		dp.fill = fill;
		dp.anchor = GridBagConstraints.CENTER;
		return dp;
	}

	public void init() {
		Tools.currentApplet = this;
		String imageFileName = "mousehelp.gif";
		InputStream jpgStream = CommonApplet.class.getResourceAsStream(imageFileName);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Image mouseIm = null;
		try {
			byte imageBytes[] = new byte[jpgStream.available()];
			jpgStream.read(imageBytes);
			mouseIm = tk.createImage(imageBytes);
		} catch (Exception e) {
			System.err.println("Error loading image <" + imageFileName + "> " + e.toString());
		}
		;

		mp = new MouseDisplay(getSize().width, mouseIm);

		try {
			treeType = getParameter("treetype");
		} catch (NullPointerException e) {
			treeType = "PM2Quadtree";
		}
		// System.out.println(treeType);

		indStructP = new Panel();
		indStructP.addContainerListener(new PanelListener(indStructP, this));
		animp = new Panel();
		helpArea = new TextArea(5, helpWidth);

		overviewButton = new Button("Zoom window");
		overviewButton.addActionListener(this);

		topInterface = new TopInterface(indStructP, mp, helpArea, this);

		can = new DrawingCanvas(new Rectangle(0, 0, SIZE, SIZE), new Rectangle(0, 0, SIZE, SIZE),
				createImage(SIZE, SIZE), mp);

		topBar = new Label();
		topBar.setForeground(Color.red);

		overviewCanvas = new DrawingCanvas(new Rectangle(0, 0, SIZE, SIZE),
				new Rectangle(0, 0, OverviewWindow.OVERVIEW_SIZE, OverviewWindow.OVERVIEW_SIZE),
				createImage(OverviewWindow.OVERVIEW_SIZE, OverviewWindow.OVERVIEW_SIZE), mp);
	}

	public void actionPerformed(ActionEvent ae) {
		Object c = ae.getSource();
		if (c == overviewButton) {
			overviewDialog.show();
		}
	}

	public void start() {
		super.start();
		validate();
	}

	public void stop() {
		centralmenu.dispose();
		super.stop();
	}

	public void destroy() {
		stop();
		super.destroy();
	}
}
