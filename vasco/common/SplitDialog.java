/**
 * This class represents a dialog for selecting data structure split methods in the application.
 *
 * @version $Id: SplitDialog.java,v 1.4 2007/10/29 01:19:51 jagan Exp $
 */
package vasco.common;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// import java.awt.*;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SplitDialog extends JDialog implements ActionListener, ItemListener, Runnable {
	GeneralCanvas rcanvas;
	JCheckBox[] splitMethod;
	ButtonGroup smeth;
	JButton close;
	JComboBox<String> opChoice; // Added generic type argument
	boolean visible;
	JLabel topBar;
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
	public SplitDialog(GeneralCanvas rc, JComboBox<String> c, String treeType, JLabel topBar, AppletValidate av,
			MouseDisplay md) {
		super(new JFrame(), "Data Structures", false); // The dialog is created with a new Frame as its parent and a
														// title. It's also set as non-modal.

		// Initialize the variables like topBar, av, opChoice, rcanvas, etc.
		this.topBar = topBar;
		this.av = av;
		int i = 0;
		opChoice = c;
		rcanvas = rc;

		// The layout of the dialog is set to a GridLayout.
		setLayout(new GridLayout(rc.getStructCount() + 1, 1));

		// Initialize and Add Components:
		smeth = new ButtonGroup();
		splitMethod = new JCheckBox[rc.getStructCount()];
		close = new JButton("Close");
		new MouseHelp(close, md, "Close structure selection window", "", "");

		// Set the preferred size for the close button
		close.setPreferredSize(new Dimension(100, 50)); // Adjust width and height as needed

		rcanvas.setTree(0, opChoice); // if no switch present
		for (int j = 0; j < rc.getStructCount(); j++) {
			boolean isSelected = rc.getStructName(j).equalsIgnoreCase(treeType);
			splitMethod[j] = new JCheckBox(rc.getStructName(j), isSelected);
			smeth.add(splitMethod[j]); // Add the checkbox to the button group
			add(splitMethod[j]);
			splitMethod[j].addItemListener(this);
		}

		// bplayout.setConstraints(splitMethod, butpan);
		topBar.setText(rc.getCurrentName() + " data structure");
		add(close);
		close.addActionListener(this);

		// Pack the components and then set the size of the dialog
		pack();
		setSize(new Dimension(600, 400)); // Adjust width and height as needed

		setResizable(true); // This sets the data structure radio button pop up to be resizable

		addWindowListener(new WindowAdapter() { // Add window listener to handle window closing event
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		// This makes the pop up come up right in the center of the screen
		// Get the screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width;
		int height = screenSize.height;

		// Calculate the position to center the dialog
		int x = (width - getWidth()) / 2;
		int y = (height - getHeight()) / 2;

		// Set the location of the dialog
		setLocation(x, y);

		visible = false;
		new Thread(this).start();
	}

	/**
	 * Runs the SplitDialog in a separate thread.
	 */
	@Override
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
	@Override
	public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.visible = visible; // Keep track of visibility state if needed
    }

	/**
	 * Disposes of the SplitDialog and sets the "visible" flag to false.
	 */
	@Override
	public void dispose() {
		visible = false;
		super.dispose();
	}

	/**
	 * Handles actionPerformed events, such as when the "Close" button is clicked.
	 *
	 * @param e The ActionEvent object representing the event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton src = (JButton) e.getSource();
		if (src == close)
			dispose();
	}

	/**
	 * Handles itemStateChanged events, such as when a Checkbox state changes.
	 *
	 * @param e The ItemEvent object representing the event.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox b = (JCheckBox) e.getSource();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			rcanvas.stop();
			for (int i = 0; i < splitMethod.length; i++)
				if (b == splitMethod[i]) {
//					System.out.println("opChoice " + opChoice);
//					System.out.println("i " + i);
//					System.out.println("rcanvas " + i);
					rcanvas.setTree(i, opChoice);
					topBar.setText(rcanvas.getCurrentName() + " data structure");
					opChoice.invalidate();
					av.globalValidate();
				}
		}
	}
}
