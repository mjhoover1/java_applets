/* $Id: WithinMode.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

//import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
//import java.util.*;
import java.text.*;
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


public class WithinMode extends JDialog implements CommonConstants, ActionListener {
	JCheckBox point, polygon, rectangle, path, sector;
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
		JLabel l = new JLabel("Query object:");
		l.setForeground(Color.blue);
		add(l);
        point = new JCheckBox("Point", false);
        rectangle = new JCheckBox("Rectangle", false);
        polygon = new JCheckBox("Polygon", false);
        path = new JCheckBox("Path", false);
        sector = new JCheckBox("Sector", false);
		// point = new JCheckBox("Point", cg, false);
		// rectangle = new JCheckBox("Rectangle", cg, false);
		// polygon = new JCheckBox("Polygon", cg, false);
		// path = new JCheckBox("Path", cg, false);
		// sector = new JCheckBox("Sector", cg, false);

		int startComp = getComponentCount();
		if ((mask & QueryObject.QO_POINT) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", point);
			add(p);
		}
		if ((mask & QueryObject.QO_RECTANGLE) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", rectangle);
			add(p);
		}
		if ((mask & QueryObject.QO_POLYGON) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", polygon);
			add(p);
		}
		if ((mask & QueryObject.QO_PATH) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", path);
			add(p);
		}
		if ((mask & QueryObject.QO_SECTOR) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", sector);
			add(p);
		}

		JCheckBox c = (JCheckBox) (((Container) getComponent(startComp)).getComponent(0));
		// first component below label
		// cg.setSelected(cg.getSelection(), c); // cg.setSelectedCheckbox(c);
		cg.add(point);
        cg.add(rectangle);
        cg.add(polygon);
        cg.add(path);
        cg.add(sector);
        cg.setSelected(c.getModel(), true);

		this.blend = new JCheckBox("Blend");
		if (blend) {
			add(this.blend);
		}

		this.distanceYes = distanceYes;
		if (distanceYes) {
			JPanel pn = new JPanel();
			pn.setLayout(new BorderLayout());
			dist = new JTextField(initDist);
			dist.setEditable(true);
			pn.add("West", new JLabel("Max Distance"));
			pn.add("East", dist);
			add(pn);
		}

		if (container != null) {
			Component[] comp = container.getComponents(); // Added Component in case a problem
			for (int i = 0; i < comp.length; i++)
				add(comp[i]);
		}

		ok = new JButton("Continue");

		ok.addActionListener(this);
		add(ok);
		setLayout(new GridLayout(getComponentCount(), 1));
		pack();
		
		// Get the screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width;
		int height = screenSize.height;

		// Calculate the position to center the dialog
		int x = (width - getWidth()) / 2;
		int y = (height - getHeight()) / 2;

		// Set the location of the dialog
		setLocation(x, y);
		
		addWindowListener(new WindowAdapter() {     // Add window listener to handle window closing event
		    @Override
		    public void windowClosing(WindowEvent e) {
		        dispose();
		    }
		});

		
		setResizable(true);
		setVisible(true); // show();
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

	public void actionPerformed(ActionEvent event) {
		if (distanceYes) {
			try {
				NumberFormat nf = NumberFormat.getInstance();
				resDist = nf.parse(dist.getText()).doubleValue();
				if (resDist < 0)
					return;
			} catch (Exception exc) {
				return;
			}
		}
		if (point.isSelected())
			mode = QueryObject.QO_POINT;
		if (rectangle.isSelected())
			mode = QueryObject.QO_RECTANGLE;
		if (polygon.isSelected())
			mode = QueryObject.QO_POLYGON;
		if (path.isSelected())
			mode = QueryObject.QO_PATH;
		if (sector.isSelected())
			mode = QueryObject.QO_SECTOR;
		// if (point.getState())
		// 	mode = QueryObject.QO_POINT;
		// if (rectangle.getState())
		// 	mode = QueryObject.QO_RECTANGLE;
		// if (polygon.getState())
		// 	mode = QueryObject.QO_POLYGON;
		// if (path.getState())
		// 	mode = QueryObject.QO_PATH;
		// if (sector.getState())
		// 	mode = QueryObject.QO_SECTOR;
		dispose();
	}
}
