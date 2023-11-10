package vasco.rectangles;
/* $Id: main.java,v 1.2 2007/10/28 15:38:20 jagan Exp $ */

//Import necessary packages
import vasco.common.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.io.*;

//Main class definition
public class main extends CommonApplet implements GenericMain {

	// Instance variable for the drawing canvas
	GenericCanvas drawcanvas;

	// Applet initialization method
	public void init() {
		init(this.drawcanvas); // Call the overloaded init method with drawcanvas
	}

	// Overloaded init method with parameter
	public void init(GenericCanvas drawcanvas) {
		super.init(); // Call the superclass's init method

		// Create a RectangleCanvas with specified parameters
		drawcanvas = new RectangleCanvas(new DRectangle(0, 0, SIZE, SIZE), can, overviewCanvas, animp, topInterface);
		DrawingPanel dpanel = new DrawingPanel(can, drawcanvas, drawcanvas, drawcanvas, mp);
		overviewDialog = new OverviewWindow(overviewCanvas, drawcanvas, mp);
		drawcanvas.initStructs(); // Initialize data structures in drawcanvas

		// Set layout using GridBagLayout
		GridBagLayout gbl;
		gbl = new GridBagLayout();
		setLayout(gbl);

		// Set constraints for the drawing panel and top bar
		gbl.setConstraints(dpanel, createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, GridBagConstraints.NONE));

		gbl.setConstraints(topBar, createConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL));

		gbl.setConstraints(dpanel,
				createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, GridBagConstraints.HORIZONTAL));

		// Add top bar and drawing panel to the applet
		add(topBar);
		add(dpanel);

		// Set constraints for the main panel
		gbl.setConstraints(mp, createConstraints(0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,
				GridBagConstraints.REMAINDER, GridBagConstraints.HORIZONTAL));

		// Create button panel with specific constraints
		buttonpanel = new Panel();

		GridBagConstraints bp = new GridBagConstraints();
		bp.gridx = GridBagConstraints.RELATIVE;
		bp.gridy = 0;
		bp.gridwidth = GridBagConstraints.REMAINDER;
		bp.gridheight = 2;
		bp.fill = GridBagConstraints.NONE;
		bp.anchor = GridBagConstraints.NORTHWEST;
		gbl.setConstraints(buttonpanel, bp);

		// Set layout for the button panel
		GridBagLayout bplayout = new GridBagLayout();
		buttonpanel.setLayout(bplayout);
		GridBagConstraints butpan = new GridBagConstraints();
		butpan.gridx = 0;
		butpan.gridy = GridBagConstraints.RELATIVE;
		butpan.gridwidth = bp.gridheight = GridBagConstraints.REMAINDER;
		butpan.fill = GridBagConstraints.NONE;
		butpan.anchor = GridBagConstraints.NORTHWEST;

		// Disabled for SIGMOD 2010 -> Switches between point, line and rectangle
		// applets
		// Add a switch button to the button panel (disabled for SIGMOD 2010)
		buttonpanel.add(new appletSwitch(appletSwitch.RECTANGLES, this, topInterface));

		// Create and add components to the button panel
		centralmenu = new CentralMenu(drawcanvas, treeType, indStructP, this, helpArea, topBar, overviewButton, mp);
		buttonpanel.add(centralmenu);
		bplayout.setConstraints(centralmenu, butpan);
		buttonpanel.add(animp);

		Label date = new Label(CompileDate.compileDate);
		bplayout.setConstraints(date, butpan);
		buttonpanel.add(date);

		// Add button panel and main panel to the applet
		add(buttonpanel);
		add(mp);

		validate(); // Validate the layout

	}
}
