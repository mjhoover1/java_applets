package vasco.lines;
/* $Id: main.java,v 1.2 2007/10/28 15:38:16 jagan Exp $ */
// import java.awt.GridBagConstraints;
// import java.awt.GridBagLayout;
// import java.awt.Label;
// import java.awt.Panel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import vasco.common.CentralMenu;
import vasco.common.CommonApplet;
import vasco.common.CompileDate;
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DrawingPanel;
import vasco.common.GenericCanvas;
import vasco.common.GenericMain;
import vasco.common.OverviewWindow;
import vasco.common.Tools;
import vasco.common.appletSwitch;

public class main extends CommonApplet implements GenericMain {

    public GenericCanvas drawcanvas;
	
    public void init()
    {
    	init(this.drawcanvas);
    }
    
    public void init(GenericCanvas drawcanvas) {
      super.init();
      
    drawcanvas = new LineCanvas(new DRectangle(0, 0, SIZE, SIZE), can, overviewCanvas, animp, topInterface);
    this.drawcanvas=drawcanvas;
    DrawingPanel dpanel = new DrawingPanel(can, drawcanvas, drawcanvas, drawcanvas, mp);
    
    overviewDialog = new OverviewWindow(overviewCanvas, drawcanvas, mp);
    drawcanvas.initStructs();

    GridBagLayout gbl;

    gbl = new GridBagLayout();
    setLayout(gbl);

    gbl.setConstraints(dpanel, 
		       createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, GridBagConstraints.NONE));

    gbl.setConstraints(topBar, createConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL));

    gbl.setConstraints(dpanel, createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 
                                                 GridBagConstraints.HORIZONTAL));
                                            
    add(topBar);
    add(dpanel);

    gbl.setConstraints(mp, createConstraints(0, GridBagConstraints.RELATIVE, 
					     GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 
					     GridBagConstraints.HORIZONTAL));


 
    buttonpanel = new JPanel();

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

    buttonpanel.add(new appletSwitch(appletSwitch.LINES, this, topInterface));


    centralmenu = new CentralMenu(drawcanvas, treeType, indStructP, this, helpArea, topBar, overviewButton, mp);
    buttonpanel.add(centralmenu);
    bplayout.setConstraints(centralmenu, butpan);
    buttonpanel.add(animp);

    JLabel date = new JLabel(CompileDate.compileDate);
    bplayout.setConstraints(date, butpan);
    buttonpanel.add(date);

    add(buttonpanel);
    add(mp);

    String file=this.getParameter("autoloadfile");
    LineCanvas canvas=((LineCanvas)drawcanvas);
    if(treeType!=null && treeType.equals("Bucket PR Quadtree"))
    {
    	canvas.setTree(4,this.centralmenu.operations);
    }
    if(file!=null)
    {
	    String[] lines=Tools.getFile("LINES",file);
	    for(int i=0;i<lines.length;i++)
	    {
	    	String []split=lines[i].split(" ");
	    	DPoint p1=new DPoint(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
	    	DPoint p2=new DPoint(Double.parseDouble(split[2]),Double.parseDouble(split[3]));
	    	canvas.pstruct.Insert(new DLine(p1,p2));
	    	canvas.historyList.addElement(new DLine(p1,p2));
	    }
	    drawcanvas.redraw();
	    Tools.deleteFile("LINES",file);
    }
    validate();
  }
}

