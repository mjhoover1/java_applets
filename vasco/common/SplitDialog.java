/**
 * This class represents a dialog for selecting data structure split methods in the application.
 *
 * @version $Id: SplitDialog.java,v 1.4 2007/10/29 01:19:51 jagan Exp $
 */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
//import java.applet.*;
//import java.util.*;

public class SplitDialog extends Dialog implements ActionListener, ItemListener, Runnable {
	GeneralCanvas rcanvas;
	Checkbox[] splitMethod;
	CheckboxGroup smeth;
	Button close;
	Choice opChoice;
	boolean visible;
	Label topBar;
	AppletValidate av;

    /**
     * Constructs a SplitDialog for selecting data structure split methods.
     *
     * @param rc       The GeneralCanvas associated with the dialog.
     * @param c        The Choice component for data structure operations.
     * @param treeType The type of the tree structure.
     * @param topBar   The Label representing the top bar of the application.
     * @param av       The AppletValidate object for validation.
     * @param md       The MouseDisplay for handling mouse-related actions.
     */
	public SplitDialog(GeneralCanvas rc, Choice c, String treeType, Label topBar, AppletValidate av, MouseDisplay md) {
		super(new Frame(), "Data Structures", false);
		this.topBar = topBar;
		this.av = av;
		int i = 0;

		opChoice = c;
		rcanvas = rc;
		setLayout(new GridLayout(rc.getStructCount() + 1, 1));
		smeth = new CheckboxGroup();
		splitMethod = new Checkbox[rc.getStructCount()];
		close = new Button("Close");
		new MouseHelp(close, md, "Close structure selection window", "", "");

		rcanvas.setTree(0, opChoice); // if no switch present
		for (int j = 0; j < rc.getStructCount(); j++) {
			if (rc.getStructName(j).equalsIgnoreCase(treeType)) {
				rcanvas.setTree(j, opChoice);
			}
			splitMethod[j] = new Checkbox(rc.getStructName(j), smeth, rc.getStructName(j).equalsIgnoreCase(treeType));
			add(splitMethod[j]);
			splitMethod[j].addItemListener(this);
		}

		// bplayout.setConstraints(splitMethod, butpan);
		topBar.setText(rc.getCurrentName() + " data structure");
		add(close);
		close.addActionListener(this);
		pack();
		setResizable(true);
		visible = false;
		new Thread(this).start();
	}

	/**
	 * Runs the SplitDialog in a separate thread.
	 */
	public void run() {
		// SJ: Commented off: This code brings the DS window on to the top
		// try {
		// for (;;){
		// Thread.sleep(500);
		// if (visible)
		// toFront();
		// }
		// } catch(InterruptedException e) {
		// return;
		// }
	}

	/**
	 * Shows the SplitDialog and sets the "visible" flag to true.
	 */
	public void show() {
		visible = true;
		super.show();
	}

	/**
	 * Disposes of the SplitDialog and sets the "visible" flag to false.
	 */
	public void dispose() {
		visible = false;
		super.dispose();
	}

	/**
	 * Handles actionPerformed events, such as when the "Close" button is clicked.
	 * 
	 * @param e The ActionEvent object representing the event.
	 */
	public void actionPerformed(ActionEvent e) {
		Button src = (Button) e.getSource();
		if (src == close)
			dispose();
	}

	/**
	 * Handles itemStateChanged events, such as when a Checkbox state changes.
	 * 
	 * @param e The ItemEvent object representing the event.
	 */
	public void itemStateChanged(ItemEvent e) {
		Checkbox b = (Checkbox) e.getSource();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			rcanvas.stop();
			for (int i = 0; i < splitMethod.length; i++)
				if (b == splitMethod[i]) {
					System.out.println("opChoice " + opChoice);
					System.out.println("i " + i);
					System.out.println("rcanvas " + i);
					rcanvas.setTree(i, opChoice);
					topBar.setText(rcanvas.getCurrentName() + " data structure");
					opChoice.invalidate();
					av.globalValidate();
				}
		}
	}
}
