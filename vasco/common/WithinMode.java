/* $Id: WithinMode.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.util.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//import vasco.drawable.*;
// import java.awt.*;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;



public class WithinMode extends JDialog implements CommonConstants, ActionListener {
	JRadioButton point, polygon, rectangle, path, sector;
	ButtonGroup cg;
	JButton ok;
	JTextField dist;
	JCheckBox blend;
	int mode;
	double resDist;
	boolean distanceYes;
	final String initDist = "50";

	public WithinMode(Container c, int mask) {
		super(new JFrame(), "Overlap search", true);
		setup(mask, false, false, c);
	}

	public WithinMode(boolean distanceYes) {
		super(new JFrame(), "Rank by distance", true);
		setup(~0, distanceYes, true, null);
	}

	private void setup(int mask, boolean distanceYes, boolean blend, Container container) {
	    resDist = Double.MAX_VALUE;
	    cg = new ButtonGroup();
	    setLayout(new GridLayout(0, 1)); // Set layout with 0 rows, 1 column. Rows will be added as needed.
	    
	    JLabel l = new JLabel("Query object:");
	    l.setForeground(Color.blue);
	    add(l);

        addRadioButton("Point", QueryObject.QO_POINT, mask);
        addRadioButton("Rectangle", QueryObject.QO_RECTANGLE, mask);
        addRadioButton("Polygon", QueryObject.QO_POLYGON, mask);
        addRadioButton("Path", QueryObject.QO_PATH, mask);
        addRadioButton("Sector", QueryObject.QO_SECTOR, mask);

	    // Select the first checkbox if any are present
//	    if (point != null) {
//	        cg.setSelected(point.getModel(), true);
//	    }

		this.blend = new JCheckBox("Blend");
		if (blend) {
			add(this.blend);
		}

		this.distanceYes = distanceYes;
		if (distanceYes) {
            JPanel distPanel = new JPanel(new BorderLayout());
			dist = new JTextField(initDist); // dist.setEditable(true);
			distPanel.add(new JLabel("Max Distance"), BorderLayout.WEST);
            distPanel.add(dist, BorderLayout.CENTER);
            add(distPanel);
		}

        if (container != null) {
            for (Component comp : container.getComponents()) {
                add(comp);
            }
        }

		ok = new JButton("Continue");
		ok.addActionListener(this);
		add(ok);
		
//		setLayout(new GridLayout(1, getComponentCount()));
		
		pack();
	    centerDialogOnScreen();

		addWindowListener(new WindowAdapter() { // Add window listener to handle window closing event
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		setResizable(true);
		setVisible(true); // show();
	}
	
    private void addRadioButton(String label, int queryObjectOption, int mask) {
        if ((mask & queryObjectOption) != 0) {
            JRadioButton radioButton = new JRadioButton(label, cg.getButtonCount() == 0);
            cg.add(radioButton);
            add(radioButton);
        }
    }
	
    private void centerDialogOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }

	public int getWithinMode() {
		return mode;
	}

	public double getWithinDist() {
		return resDist;
	}

	public boolean getBlend() {
		return blend.isSelected(); // blend.getState();
	}

	@Override
    public void actionPerformed(ActionEvent event) {
        if (distanceYes) {
            try {
                NumberFormat nf = NumberFormat.getInstance();
                resDist = nf.parse(dist.getText()).doubleValue();
                if (resDist < 0) return;
            } catch (Exception exc) {
                return;
            }
        }

        mode = getSelectedMode();
        dispose();
    }

    private int getSelectedMode() {
        if (point.isSelected()) return QueryObject.QO_POINT;
        if (rectangle.isSelected()) return QueryObject.QO_RECTANGLE;
        if (polygon.isSelected()) return QueryObject.QO_POLYGON;
        if (path.isSelected()) return QueryObject.QO_PATH;
        if (sector.isSelected()) return QueryObject.QO_SECTOR;
        return -1; // default or error case
    }
}
