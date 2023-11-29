/* $Id: SearchMode.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.BorderLayout;
import java.awt.Color;

// import java.awt.*;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

class SearchMode extends JPanel implements CommonConstants {
	JCheckBox m1, m2, m3, m4;

	SearchMode(int mask) {
		super();
		JLabel l1 = new JLabel("Search Options - Look for:");
		l1.setForeground(Color.blue);
		add(l1);
		m1 = new JCheckBox("Objects completely inside query range");
		m2 = new JCheckBox("Objects intersecting query range, at least one vertex part of intersection");
		m3 = new JCheckBox("Object intersecting query range, no vertices part of intersection");
		m4 = new JCheckBox("Objects that completely enclose the query range");
		if ((mask & SEARCHMODE_CONTAINS) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", m1);
			add(p);
		}
		if ((mask & SEARCHMODE_OVERLAPS) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", m2);
			add(p);
		}
		if ((mask & SEARCHMODE_CROSSES) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", m3);
			add(p);
		}
		if ((mask & SEARCHMODE_ISCONTAINED) != 0) {
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("West", m4);
			add(p);
		}

		m1.setSelected(true); // m1.setState(true);
	}

	int getSearchMode() {
		return ((m1.isSelected() ? SEARCHMODE_CONTAINS : 0) | (m2.isSelected() ? SEARCHMODE_OVERLAPS : 0)
				| (m3.isSelected() ? SEARCHMODE_CROSSES : 0) | (m4.isSelected() ? SEARCHMODE_ISCONTAINED : 0));
	}

}
