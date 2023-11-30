package vasco.regions;

import java.awt.event.ActionEvent;

// import java.awt.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import vasco.common.AppletValidate;
import vasco.common.CentralMenu;
import vasco.common.MouseDisplay;

public class RegionCentralMenu extends CentralMenu {

	RegionCanvas regionCanvas;

	public RegionCentralMenu(RegionCanvas rc, String treeMode, JPanel indStructP, AppletValidate av, JTextArea helpArea,
			JLabel topBar, JButton overviewButton, MouseDisplay md) {
		super(rc, treeMode, indStructP, av, helpArea, topBar, overviewButton, md);
		regionCanvas = rc;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();

		if (regionCanvas.pstruct.si != null) {
			regionCanvas.pstruct.si.dispose();
			regionCanvas.pstruct.si = null;
		}

		if (obj instanceof JButton && "Operation Color Legend".equals(((JButton) obj).getText())) { // use getText instead of getLabel
			if (dialog != null)
				dialog.dispose();
			dialog = new RegionColorHelp((String) operations.getSelectedItem(), regionCanvas.getAppletType(),
					regionCanvas.getCurrentName());
			dialog.setVisible(true); // Instead of dialog.show()
		} else
			super.actionPerformed(event);
	}
}
