/**
 * This class represents the central menu panel in the application.
 *
 * @version $Id: CentralMenu.java,v 1.3 2007/10/28 15:38:13 jagan Exp $
 */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
//import java.applet.*;
//import java.util.*;

/**
 * CentralMenu is a panel that provides various controls and options for the
 * application.
 */
public class CentralMenu extends JPanel implements ActionListener, ItemListener {
	protected GeneralCanvas rcanvas; // Reference to the GeneralCanvas used in the application.
	public Choice operations; // Choice component for selecting operations.
	protected SplitDialog dlg; // Reference to the SplitDialog used for splitting data structures.
	protected Button splitbut; // Button for opening the SplitDialog.
	protected JDialog dialog; // Dialog for displaying color legends.
	protected Button chbut; // Button for opening the color legend dialog.
	protected Button undo; // Button for undoing the last insert or delete operation.
	protected Button plusGrid, minusGrid; // plusGrid - Button for zooming in the grid, & minusGrid - Button for zooming
											// out the grid.
	protected JCheckBox gridChb; // Checkbox for toggling the grid visibility.
	protected Button load, save, clear; // load - Button for loading a data set, save - Button for saving a data set., &
										// clear - Button for clearing (erasing) the data set.
	protected int gridLevel; // The current grid level.
	protected Label topBar; // Label for displaying information in the top bar.
	protected TextArea helpArea; // TextArea for displaying help information.

	/**
	 * Constructs a CentralMenu panel that provides various user interface elements
	 * and actions for controlling the application.
	 *
	 * @param rc          The GeneralCanvas associated with the CentralMenu.
	 * @param treeMode    The mode of the tree structure.
	 * @param indStructP  The Panel for displaying structural information.
	 * @param av          The AppletValidate object for validation.
	 * @param helpArea    The TextArea for displaying help information.
	 * @param topBar      The Label representing the top bar of the application.
	 * @param overviewButton The Button for showing the zoom window.
	 * @param md          The MouseDisplay for handling mouse-related actions.
	 */
	public CentralMenu(GeneralCanvas rc, String treeMode, JPanel indStructP, AppletValidate av, TextArea helpArea,
			Label topBar, Button overviewButton, MouseDisplay md) {
		GridBagLayout bplayout; // Initialize the GridBagLayout
		setLayout(bplayout = new GridBagLayout()); // Set the layout for this panel to GridBagLayout
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

		bplayout.setConstraints(indStructP, butpan); // Apply constraints to the indStructP Panel

		JPanel opanel = new JPanel();
		opanel.setLayout(new BorderLayout());
		bplayout.setConstraints(opanel, butpan); // Apply constraints to the opanel Panel

		operations = new Choice();
		operations.addItemListener(rc);
		dlg = new SplitDialog(rcanvas, operations, treeMode, topBar, av, md); // Initialize SplitDialog

		splitbut = new Button("Data Structures");
		bplayout.setConstraints(splitbut, butpan);
		splitbut.addActionListener(this);
		new MouseHelp(splitbut, md, "Open a structure selection window", "", ""); // Add mouse help for splitbut

		opanel.add("West", new Label("Operations"));
		opanel.add("East", operations);
		new MouseHelp(operations, md, "Select a data structure operation", "", ""); // Add mouse help for operations Choice

		undo = new Button("Undo");
		undo.addActionListener(this);
		bplayout.setConstraints(undo, butpan);
		new MouseHelp(undo, md, "Undo the last insert or delete", "", ""); // Add mouse help for undo Button

		chbut = new Button("Operation Color Legend");
		chbut.addActionListener(this);
		bplayout.setConstraints(chbut, butpan);
		new MouseHelp(chbut, md, "Open a color legend window", "", ""); // Add mouse help for chbut Button

		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(1, 3));
		load = new Button("Load");
		load.addActionListener(this);
		new MouseHelp(load, md, "Load a data set from the data server", "", "");  // Add mouse help for load Button
		save = new Button("Save");
		save.addActionListener(this);
		new MouseHelp(save, md, "Save a data set to the data server", "", ""); // Add mouse help for save Button
		clear = new Button("Clear");
		clear.addActionListener(this);
		new MouseHelp(clear, md, "Clear (erase) the data set", "", ""); // Add mouse help for clear Button
		pan.add(load);
		pan.add(save);
		pan.add(clear);
		bplayout.setConstraints(pan, butpan); // Apply constraints to the pan Panel

		JPanel grd = new JPanel();
		grd.setLayout(new GridLayout(1, 3));
		bplayout.setConstraints(grd, butpan);  // Apply constraints to the grd Panel
		grd.add(gridChb = new JCheckBox("Grid", true));
		rc.setGrid(true);
		new MouseHelp(gridChb, md, "Show grid", "", "", "Hide grid", "", "");  // Add mouse help for gridChb Checkbox
		gridChb.addItemListener(this);
		grd.add(plusGrid = new Button("+"));
		plusGrid.addActionListener(this);
		grd.add(minusGrid = new Button("-"));
		minusGrid.addActionListener(this);
		gridLevel = 0;
		new MouseHelp(plusGrid, md, "Show a finer grid", "", "");  // Add mouse help for plusGrid Button
		new MouseHelp(minusGrid, md, "Show a coarser grid", "", ""); // Add mouse help for minusGrid Button

		helpArea.setEditable(false);

		// SIGMOD 2010: Disabled these for SIGMOD
	    add(pan); // Add pan Panel to this panel: Shows the Load, Save, and Delete on side Menu
	    add(grd); // Add grd Panel to this panel
	    add(splitbut); // Add splitbut Button to this panel: Shows the Data structures drop down menu
	    add(opanel); // Add opanel Panel to this panel
	    add(undo); // Add undo Button to this panel
	    add(chbut); // Add chbut Button to this panel
	    add(indStructP); // Add indStructP Panel to this panel

		add(new Label("Help"));
	    add(helpArea); // Add helpArea TextArea to this panel
	    bplayout.setConstraints(helpArea, butpan); // Apply constraints to helpArea

	    new MouseHelp(overviewButton, md, "Show zoom window", "", ""); // Add mouse help for overviewButton
	    add(overviewButton); // Add overviewButton Button to this panel
	    bplayout.setConstraints(overviewButton, butpan); // Apply constraints to overviewButton

	    // rcanvas.setBucketSize(bucketSize); // Commented out code
	    validate();
	}

    /**
     * Disposes of the resources associated with this CentralMenu.
     */
	public void dispose() {
		if (dlg != null)
			dlg.dispose();
		if (dialog != null)
			dialog.dispose();
	}

	/**
	 * Handles item state change events, such as checkbox toggling.
	 *
	 * @param ie The ItemEvent representing the item state change event.
	 */
	public void itemStateChanged(ItemEvent ie) {
		Object obj = ie.getSource();

		if (obj == gridChb) {
	        // Set the grid visibility based on the state of the grid checkbox.
			rcanvas.setGrid(gridChb.getState());
			
	        // Enable or disable zoom buttons based on the grid state.
			if (gridChb.getState()) {
				plusGrid.setEnabled(true);
				minusGrid.setEnabled(true);
			} else {
				plusGrid.setEnabled(false);
				minusGrid.setEnabled(false);
			}
		}
	}

	/**
	 * Handles action events, such as button clicks.
	 *
	 * @param event The ActionEvent representing the action event.
	 */
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj == chbut) {
	        // Show the color legend dialog.
			if (dialog != null)
				dialog.dispose();
			dialog = new ColorHelp(operations.getSelectedItem(), rcanvas.getAppletType());
			dialog.show();
		}

		if (obj == undo) {
	        // Undo the last insert or delete operation.
			rcanvas.undo();
		}

		if (obj == splitbut) {
	        // Show the SplitDialog for data structure splitting.
			dlg.show();
		}
		if (obj == load) {
	        // Terminate and load a data set from the data server.
			rcanvas.terminate();
			fileSelector fs = rcanvas.getFileSelector("LOAD");
			fs.show();
			rcanvas.redraw();
		}
		if (obj == save) {
	        // Terminate and save a data set to the data server.
			rcanvas.terminate();
			fileSelector fs = rcanvas.getFileSelector("SAVE");
			fs.show();
			rcanvas.redraw();
		}
		if (obj == clear) {
	        // Terminate and clear (erase) the data set.
			rcanvas.terminate();
			rcanvas.clear();
			rcanvas.redraw();
		}
		if (obj == plusGrid) {
	        // Increase the grid level (zoom in).
			rcanvas.incGrid();
		}
		if (obj == minusGrid) {
	        // Decrease the grid level (zoom out).
			rcanvas.decGrid();
		}
	}
}
