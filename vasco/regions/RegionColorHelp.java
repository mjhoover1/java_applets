package vasco.regions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vasco.common.ColorHelp;

public class RegionColorHelp extends ColorHelp {

	class HelpStruct {
		String opName;
		String opTitle;
		Vector opText;
		Vector opColor;

		public HelpStruct(String n, String t, Vector tx, Vector c) {
			opName = n;
			opTitle = t;
			opText = tx;
			opColor = c;
		}
	}
	// TODO Change to List

	String structName;
	Vector<String> commonHelp = new Vector<String>();
	Vector convertHelp = new Vector();

	Vector<Color> commonColors = new Vector<>();
	Vector convertColors = new Vector();

	Vector help = new Vector();

	public RegionColorHelp(String opType, int obj_nr, String structName) {
		HelpStruct hStruct;
		Color hColor;
		String hString;
		int legIndex;

		this.structName = structName;

		// common colors
		commonHelp.addElement("Grid cell.");
		commonColors.addElement(Color.gray);
		commonHelp.addElement("Occupied cell.");
		commonColors.addElement(Colors.GRID_CELL);
		commonHelp.addElement("Empty cell.");
		commonColors.addElement(Colors.GRID_EMPTY);
		commonHelp.addElement("Selected block.");
		commonColors.addElement(Colors.SELECTED_CELL);
		commonHelp.addElement("User selection.");
		commonColors.addElement(Colors.SELECTED_AREA);

		convertHelp.addElement("Grid cell.");
		convertColors.addElement(Color.gray);
		convertHelp.addElement("Occupied cell.");
		convertColors.addElement(Colors.GRID_CELL);
		convertHelp.addElement("Empty cell.");
		convertColors.addElement(Colors.GRID_EMPTY);

		// construct help items
		if (structName.equals("Region Tree")) {
			convertHelp.addElement("Active tree node.");
			convertColors.addElement(Colors.ACTIVE_NODE);
			commonHelp.addElement("Tree node.");
			commonColors.addElement(Color.black);
			commonHelp.addElement("Center of the nearest tree node.");
			commonColors.addElement(Colors.NEAREST_NODE);

			convertHelp.addElement("Tree node.");
			convertColors.addElement(Color.black);
			if (opType.equals("To array")) {
				convertHelp.addElement("Unknown cell.");
				convertColors.addElement(Colors.UNKNOWN);
			} else if (opType.equals("To raster")) {
				convertHelp.addElement("Active cell(s) in the row.");
				convertColors.addElement(Colors.ACTIVE_ROW);
				convertHelp.addElement("Unknown cell.");
				convertColors.addElement(Colors.UNKNOWN);
			} else if (opType.equals("To chain")) {
				convertHelp.addElement("Chain code element.");
				convertColors.addElement(Colors.ACTIVE_CHAINCODE);
			}
		} else if (structName.equals("Array")) {
			if (opType.equals("To quadtree")) {
				convertHelp.addElement("Tree node.");
				convertColors.addElement(Color.black);
				convertHelp.addElement("Active grid cell.");
				convertColors.addElement(Colors.ACTIVE_NODE);
			} else if (opType.equals("To raster")) {
				convertHelp.addElement("Active array row.");
				convertColors.addElement(Colors.ACTIVE_NODE);
				convertHelp.addElement("Active cell(s) in the row.");
				convertColors.addElement(Colors.ACTIVE_ROW);
				convertHelp.addElement("Unknown cell.");
				convertColors.addElement(Colors.UNKNOWN);
			} else if (opType.equals("To chain")) {
				// TO DO!!!
			}
		} else if (structName.equals("Raster")) {
			if (opType.equals("To quadtree")) {
				// TO DO!!!
			} else if (opType.equals("To raster")) {
				convertHelp.addElement("Active raster row.");
				convertColors.addElement(Colors.ACTIVE_NODE);
				convertHelp.addElement("Active cell(s) in the row.");
				convertColors.addElement(Colors.ACTIVE_ROW);
				convertHelp.addElement("Unknown cell.");
				convertColors.addElement(Colors.UNKNOWN);
			} else if (opType.equals("To chain")) {
				// TO DO!!!
			}
		} else if (structName.equals("Chain code")) {
			if (opType.equals("To quadtree")) {
				// TO DO!!!
			} else if (opType.equals("To raster")) {
				// TO DO!!!
			} else if (opType.equals("To chain")) {
				// TO DO!!!
			}
		}

		help.addElement(new HelpStruct("Insert", "Insert legend", commonHelp, commonColors));
		help.addElement(new HelpStruct("Delete", "Delete legend", commonHelp, commonColors));
		help.addElement(new HelpStruct("Move", "Move legend", commonHelp, commonColors));
		help.addElement(new HelpStruct("U Move", "U Move legend", commonHelp, commonColors));
		help.addElement(new HelpStruct("Copy", "Copy legend", commonHelp, commonColors));
		help.addElement(new HelpStruct("Select", "Select legend", commonHelp, commonColors));
		help.addElement(new HelpStruct("To quadtree", "Quadtree conversion legend", convertHelp, convertColors));
		help.addElement(new HelpStruct("To array", "Array conversion legend", convertHelp, convertColors));
		help.addElement(new HelpStruct("To raster", "Raster conversion legend", convertHelp, convertColors));
		help.addElement(new HelpStruct("To chain", "Chain code conversion legend", convertHelp, convertColors));

		legIndex = -1;
		for (int i = 0; i < help.size(); i++)
			if (((HelpStruct) help.elementAt(i)).opName.equals(opType)) {
				legIndex = i;
				break;
			}

		if (legIndex == -1)
			return;

		hStruct = (HelpStruct) help.elementAt(legIndex);
		setTitle(hStruct.opTitle);
		// setTitle("obj_nr(" + obj_nr + ")" + " " + opType);
		setBackground(Color.lightGray);
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints cn = new GridBagConstraints();
		cn.gridx = 0;
		cn.gridy = GridBagConstraints.RELATIVE;
		cn.gridwidth = GridBagConstraints.REMAINDER;
		cn.fill = GridBagConstraints.NONE;
		cn.anchor = GridBagConstraints.NORTHWEST;

		for (int i = 0; i < hStruct.opColor.size(); i++) {
			hColor = (Color) hStruct.opColor.elementAt(i);
			hString = (String) hStruct.opText.elementAt(i);

			JPanel p = createPanel(hString, hColor);
			add(p);
			gbl.setConstraints(p, cn);
		}

		add(close = new JButton("Close"));
		close.addActionListener(this);
		pack();
		
		centerDialogOnScreen();
		setResizable(true);
	}
	
    private void centerDialogOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(screenSize.width, getWidth());
        int height = Math.min(screenSize.height, getHeight());
        setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
    }

	@Override
	protected JPanel createPanel(String text, Color c) {
		JPanel p = new JPanel();
		JLabel l = new JLabel("    "); // The label that will have the background color.
		l.setOpaque(true);              // Make the label opaque to show the background color.
	    l.setBackground(c);             // Set the background color.
	    
	    p.add(l);                       // Add the colored label to the panel.

	    String[] form = format(text, 50); // Break the text into lines if necessary.
	    
	    JPanel sub = new JPanel(new GridLayout(form.length, 1)); // Create a sub-panel with a layout to hold the text.

		sub.setLayout(new GridLayout(form.length, 1));
		
	    for (String element : form) {
	        sub.add(new JLabel(element)); // Add each line of text as a new label to the sub-panel.
	    }
		
	    p.add(sub);                      // Add the sub-panel with text to the main panel.
		
	    return p;                        // Return the complete panel.
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == close) {
			dispose();
		}
	}
}
