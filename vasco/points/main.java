package vasco.points;

/* $Id: main.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import vasco.common.CentralMenu;
import vasco.common.CommonApplet;
import vasco.common.CompileDate;
import vasco.common.DRectangle;
import vasco.common.DrawingPanel;
import vasco.common.GeneralCanvas;
import vasco.common.GenericCanvas;
import vasco.common.GenericMain;
import vasco.common.OverviewWindow;
import vasco.common.RebuildTree;
import vasco.common.appletSwitch;

public class main extends CommonApplet implements GenericMain {

	GenericCanvas drawcanvas;

	public void init() {
		init(this.drawcanvas);
	}

	public void init(GenericCanvas drawcanvas) {
		super.init();

		// this.setSize(900,700);
		drawcanvas = new PointCanvas(new DRectangle(0, 0, SIZE, SIZE), can, overviewCanvas, animp, topInterface);
		DrawingPanel dpanel = new DrawingPanel(can, (RebuildTree) drawcanvas, (MouseListener) drawcanvas,
				(MouseMotionListener) drawcanvas, mp);

		overviewDialog = new OverviewWindow(overviewCanvas, (RebuildTree) drawcanvas, mp);
		drawcanvas.initStructs();

		GridBagLayout gbl;

		gbl = new GridBagLayout();
		setLayout(gbl);

		/*
		 * gbl.setConstraints(dpanel, createConstraints(0, GridBagConstraints.RELATIVE,
		 * 1, 1, GridBagConstraints.NONE));
		 */
		gbl.setConstraints(topBar, createConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL));

		gbl.setConstraints(dpanel,
				createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, GridBagConstraints.HORIZONTAL));

		add(topBar);
		add(dpanel);

		gbl.setConstraints(mp, createConstraints(0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,
				GridBagConstraints.REMAINDER, GridBagConstraints.HORIZONTAL));

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

		buttonpanel.add(new appletSwitch(appletSwitch.POINTS, this, topInterface));

		centralmenu = new CentralMenu(drawcanvas, treeType, indStructP, this, helpArea, topBar, overviewButton, mp);
		buttonpanel.add(centralmenu);
		bplayout.setConstraints(centralmenu, butpan);
		buttonpanel.add(animp);

		Label date = new Label(CompileDate.compileDate);
		bplayout.setConstraints(date, butpan);
		buttonpanel.add(date);

		add(buttonpanel);
		add(mp);

		validate();
	}
}
