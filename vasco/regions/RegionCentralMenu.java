package vasco.regions;
import vasco.common.*;
import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
// import java.applet.*;
import java.util.*;

public class RegionCentralMenu extends CentralMenu{

  RegionCanvas regionCanvas;

    public RegionCentralMenu(RegionCanvas rc, String treeMode, JPanel indStructP, 
		       AppletValidate av, TextArea helpArea, Label topBar,
		       Button overviewButton, MouseDisplay md) {
      super(rc, treeMode, indStructP, av, helpArea, topBar, overviewButton, md);
      regionCanvas = rc;
    }

    public void actionPerformed(ActionEvent event) {
	Object obj = event.getSource();

	if (regionCanvas.pstruct.si != null){
	  regionCanvas.pstruct.si.dispose();
	  regionCanvas.pstruct.si = null;
	}
	
	if (obj instanceof Button && ((Button)obj).
	    getLabel().equals("Operation Color Legend")) {
	  if (dialog != null)
	    dialog.dispose();
	  dialog = new RegionColorHelp(operations.getSelectedItem(), 
		       regionCanvas.getAppletType(), regionCanvas.getCurrentName());
	  dialog.show();
	}
	else
	  super.actionPerformed(event);
    }
}








