/* $Id: PanelListener.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;

public class PanelListener implements ContainerListener {
	/*
	 * resize panel individual to each structure based on number of elements in it
	 */
	Panel tovalid;
	AppletValidate appl;

	public PanelListener(Panel p, AppletValidate a) {
		tovalid = p;
		appl = a;
	}

	void adjustPanel() {
		GridBagLayout gbl = new GridBagLayout();

		tovalid.setLayout(gbl);
		GridBagConstraints top = new GridBagConstraints();
		top.gridx = top.gridy = 0;
		GridBagConstraints rest = new GridBagConstraints();
		rest.gridx = 0;
		rest.gridy = GridBagConstraints.RELATIVE;

		for (int i = 1; i < tovalid.getComponentCount(); i++)
			gbl.setConstraints(tovalid.getComponent(i), i == 0 ? top : rest);

		appl.globalValidate();
	}

	public void componentAdded(ContainerEvent e) {
		adjustPanel();
	}

	public void componentRemoved(ContainerEvent e) {
		adjustPanel();
	}
}
