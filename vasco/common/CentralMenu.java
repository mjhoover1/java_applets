/* $Id: CentralMenu.java,v 1.3 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;

public class CentralMenu extends Panel implements ActionListener, ItemListener {
    protected GeneralCanvas rcanvas;
    public Choice operations;
    protected SplitDialog dlg;
    protected Button splitbut;
    protected Dialog dialog;
    protected Button chbut;
    protected Button undo;
    protected Button plusGrid, minusGrid;
    protected Checkbox gridChb;
    protected Button load, save, clear;
    protected int gridLevel;
    protected Label topBar;
    protected TextArea helpArea;

    public CentralMenu(GeneralCanvas rc, String treeMode, Panel indStructP, 
		       AppletValidate av, TextArea helpArea, Label topBar,
		       Button overviewButton, MouseDisplay md) {
	GridBagLayout  bplayout;
	setLayout(bplayout = new GridBagLayout());
	rcanvas = rc;
	this.topBar = topBar;
	this.helpArea = helpArea;
	dialog = null;

	GridBagConstraints butpan = new GridBagConstraints();
	butpan.gridx = 0;
	butpan.gridy = GridBagConstraints.RELATIVE;
	butpan.gridwidth = GridBagConstraints.REMAINDER;
	butpan.fill = GridBagConstraints.NONE;
	butpan.anchor = GridBagConstraints.NORTHWEST;

	bplayout.setConstraints(indStructP, butpan);

	Panel opanel = new Panel();
	opanel.setLayout(new BorderLayout());
	bplayout.setConstraints(opanel, butpan);

	operations = new Choice();
	operations.addItemListener(rc);
	dlg = new SplitDialog(rcanvas, operations, treeMode, topBar, av, md);

	splitbut = new Button("Data Structures");
	bplayout.setConstraints(splitbut, butpan);
	splitbut.addActionListener(this);
	new MouseHelp(splitbut, md, "Open a structure selection window", "", "");

	opanel.add("West", new Label("Operations"));
	opanel.add("East", operations);
	new MouseHelp(operations, md, "Select a data structure operation", "", "");


	undo = new Button("Undo");
	undo.addActionListener(this);
	bplayout.setConstraints(undo, butpan);
	new MouseHelp(undo, md, "Undo the last insert or delete", "", "");

	chbut = new Button("Operation Color Legend");
	chbut.addActionListener(this);
	bplayout.setConstraints(chbut, butpan);
	new MouseHelp(chbut, md, "Open a color legend window", "", "");

	Panel pan = new Panel();
	pan.setLayout(new GridLayout(1,3));
	load = new Button("Load");
	load.addActionListener(this);
	new MouseHelp(load, md, "Load a data set from the data server", "", "");
	save = new Button("Save");
	save.addActionListener(this);
	new MouseHelp(save, md, "Save a data set to the data server", "", "");
	clear = new Button("Clear");
	clear.addActionListener(this);
	new MouseHelp(clear, md, "Clear (erase) the data set", "", "");
	pan.add(load);
	pan.add(save);
	pan.add(clear);
	bplayout.setConstraints(pan, butpan);


	Panel grd = new Panel();
	grd.setLayout(new GridLayout(1, 3));
	bplayout.setConstraints(grd, butpan);
	grd.add(gridChb = new Checkbox("Grid", true));
	rc.setGrid(true);
	new MouseHelp(gridChb, md, "Show grid", "", "", "Hide grid", "", "");
	gridChb.addItemListener(this);
	grd.add(plusGrid = new Button("+"));
	plusGrid.addActionListener(this);
	grd.add(minusGrid = new Button("-"));
	minusGrid.addActionListener(this);
	gridLevel = 0;
	new MouseHelp(plusGrid, md, "Show a finer grid", "", "");
	new MouseHelp(minusGrid, md, "Show a coarser grid", "", "");

	helpArea.setEditable(false);


	// SIGMOD 2010: Disabled these for SIGMOD
	add(pan);
	add(grd);
	add(splitbut);
	add(opanel);
	add(undo);
	add(chbut);
	add(indStructP);
	
	add(new Label("Help"));
	add(helpArea);
	bplayout.setConstraints(helpArea, butpan);

	new MouseHelp(overviewButton, md, "Show zoom window", "", "");
	add(overviewButton);
	bplayout.setConstraints(overviewButton, butpan);

	//    rcanvas.setBucketSize(bucketSize);
	validate();
    }
  
    public void dispose() {
	if (dlg != null)
	    dlg.dispose();
	if (dialog != null)
	    dialog.dispose();
    }

    public void itemStateChanged(ItemEvent ie) {
	Object obj = ie.getSource();

	if (obj == gridChb) {
	    rcanvas.setGrid(gridChb.getState());
	    if (gridChb.getState()) {
		plusGrid.setEnabled(true);
		minusGrid.setEnabled(true);
	    } else {
		plusGrid.setEnabled(false);
		minusGrid.setEnabled(false);
	    }
	}
    }


    public void actionPerformed(ActionEvent event) {
	Object obj = event.getSource();
	if (obj == chbut) {
	    if (dialog != null)
		dialog.dispose();
	    dialog = new ColorHelp(operations.getSelectedItem(), rcanvas.getAppletType());
	    dialog.show();
	}

	if (obj == undo) {
	    rcanvas.undo();
	}

	if (obj == splitbut) {
	    dlg.show();
	}
	if (obj == load) {
	    rcanvas.terminate();
	    fileSelector fs = rcanvas.getFileSelector("LOAD");
	    fs.show();
	    rcanvas.redraw();
	}
	if (obj == save) {
	    rcanvas.terminate();
	    fileSelector fs = rcanvas.getFileSelector("SAVE");
	    fs.show();
	    rcanvas.redraw();
	}
	if (obj == clear) {
	    rcanvas.terminate();
	    rcanvas.clear();
	    rcanvas.redraw();
	}
	if (obj == plusGrid) {
	    rcanvas.incGrid();
	}
	if (obj == minusGrid) {
	    rcanvas.decGrid();
	}
    }
}
