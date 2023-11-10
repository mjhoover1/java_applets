package vasco.regions;
import vasco.common.*;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;  
import java.io.*;

public class main extends CommonApplet implements GenericMain {
  public static final int DEFAULT_GRID_LEVEL = 5;
   
  GenericCanvas drawcanvas;
  
  public void init()
  {
	  init(drawcanvas);
  }
     
  public void init(GenericCanvas drawcanvas) {
    super.init();  
    drawcanvas = new RegionCanvas(new DRectangle(0, 0, SIZE, SIZE), can, 
				  overviewCanvas, animp, topInterface);//,
				  //centralmenu);
    DrawingPanel dpanel = new DrawingPanel(can, drawcanvas, drawcanvas, 
					   drawcanvas, mp);
    overviewDialog = new OverviewWindow(overviewCanvas, drawcanvas, mp);
    drawcanvas.initStructs();
  
    GridBagLayout gbl;

    gbl = new GridBagLayout();
    setLayout(gbl);

    gbl.setConstraints(dpanel, 
		       createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 
					 GridBagConstraints.NONE));

    gbl.setConstraints(topBar, createConstraints(0, 0, 1, 1, 
						 GridBagConstraints.HORIZONTAL));

    gbl.setConstraints(dpanel, createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 
                                                 GridBagConstraints.HORIZONTAL));
                                            
    add(topBar);
    add(dpanel);

    gbl.setConstraints(mp, createConstraints(0, GridBagConstraints.RELATIVE, 
					     GridBagConstraints.REMAINDER, 
					     GridBagConstraints.REMAINDER, 
					     GridBagConstraints.HORIZONTAL));
 
    buttonpanel = new Panel();

    GridBagConstraints bp = new GridBagConstraints();
    bp.gridx = GridBagConstraints.RELATIVE;
    bp.gridy = 0;
    bp.gridwidth = GridBagConstraints.REMAINDER;
    bp.gridheight = 2;
    bp.fill = GridBagConstraints.NONE;
    bp.anchor = GridBagConstraints.NORTHWEST;
    gbl.setConstraints(buttonpanel, bp);

    GridBagLayout bplayout = new GridBagLayout();
    buttonpanel.setLayout(bplayout);
    GridBagConstraints butpan = new GridBagConstraints();
    butpan.gridx = 0;
    butpan.gridy = GridBagConstraints.RELATIVE;
    butpan.gridwidth = bp.gridheight = GridBagConstraints.REMAINDER;
    butpan.fill = GridBagConstraints.NONE;
    butpan.anchor = GridBagConstraints.NORTHWEST;

    buttonpanel.add(new appletSwitch(appletSwitch.AREA, this, topInterface));
  
    centralmenu = new RegionCentralMenu((RegionCanvas)drawcanvas, treeType, indStructP, this, 
				  helpArea, topBar, overviewButton, mp); 
    buttonpanel.add(centralmenu);
    bplayout.setConstraints(centralmenu, butpan);
    buttonpanel.add(animp);
  
    Label date = new Label(CompileDate.compileDate);
    bplayout.setConstraints(date, butpan);
    buttonpanel.add(date);

    add(buttonpanel);
    add(mp);

    validate();

    /* init grid */
    drawcanvas.setGrid(true); 
    drawcanvas.setGrid(DEFAULT_GRID_LEVEL); 
    ((RegionCanvas)drawcanvas).setHelp();
    drawcanvas.redraw();
  } 
}


