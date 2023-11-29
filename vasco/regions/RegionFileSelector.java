package vasco.regions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.StringTokenizer;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JButton;

import vasco.common.FileIface;
import vasco.common.Tools;
import vasco.common.TopInterface;
import vasco.common.fileSelector;

class RegionFileSelector extends fileSelector {

	RegionFileSelector(FileIface rc, String act, TopInterface ti) {
		super(rc, "REGIONS", "Data format:\n<resolution>\n<x1> <y1>\n<x2> <y2>\n...", act, ti);
	}

	@Override
	protected String genRandom(int nr) {
		String s = "";

		// print out the resolution
		s = s + ((RegionCanvas) rcanvas).grid.res + " ";
		System.err.println(s);

		// generate the data points
		for (int i = 0; i < nr; i++) {
			Point p;
			p = ((RegionCanvas) rcanvas).randomPoint();
			s += p.x + " " + p.y + " ";
		}

		return s;
	}

	@Override
	protected void formVector(String s, Vector v) {
		int x1, y1, res;
		boolean first = true;

		StringTokenizer st = new StringTokenizer(s);
		try {
			while (st.hasMoreTokens()) {
				Point p;
				// read the resolution first
				if (first) {
					first = false;
					// check if the vector has some elements already
					// e.g. "merge" operation
					if (v.size() > 0) {
						if (((RegionCanvas) rcanvas).grid.res == Integer.parseInt(st.nextToken())) {
							v.insertElementAt(new Integer(((RegionCanvas) rcanvas).grid.res), 0);
						} else {
							System.err.println("Resolution mismatch...");
							return;
						}
					} else // vector is already empty
						v.addElement(new Integer(st.nextToken()));
				}
				// then read the data points
				y1 = new Integer(st.nextToken()).intValue();
				x1 = new Integer(st.nextToken()).intValue();
				p = new Point(x1, y1);
				v.addElement(p);
			}
		} catch (Exception e) {
			System.out.println("LineFileSelector: formVector : " + e.toString());
			v = null;
		}
		if (v != null) {
			rcanvas.vectorIn(v);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		JButton btn = (JButton) ae.getSource();

		if (btn == append && fname.getText().length() > 0) {
			String[] s = Tools.getFile(datatype, fname.getText());
			if ((new Integer(s[0])).intValue() == ((RegionCanvas) rcanvas).grid.res) {
				Vector v = rcanvas.vectorOut();
				String[] out = new String[v.size()];
				for (int i = 0; i < v.size(); i++) {
					Point er = (Point) v.elementAt(i);
					out[i] = new String(er.y + " " + er.x);
				}
				Tools.appendFile(datatype, fname.getText(), out);
				dispose();
			} else
				System.err.println("Resolution mismatch...");
		} else
			super.actionPerformed(ae);
	}
}
