/* $Id: SearchMode.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;
import java.awt.event.*;
import java.util.*;

class SearchMode extends Container implements CommonConstants {
    Checkbox m1, m2, m3, m4;

    SearchMode(int mask) {
	super();
	Label l1 = new Label("Search Options - Look for:");
	l1.setForeground(Color.blue);
	add(l1);
      m1 = new Checkbox("Objects completely inside query range");
      m2 = new Checkbox("Objects intersecting query range, at least one vertex part of intersection");
      m3 = new Checkbox("Object intersecting query range, no vertices part of intersection");
      m4 = new Checkbox("Objects that completely enclose the query range");
      if ((mask & SEARCHMODE_CONTAINS) != 0) {
	  Panel p = new Panel();
	  p.setLayout(new BorderLayout());
	  p.add("West", m1);
	  add(p);
      }
      if ((mask & SEARCHMODE_OVERLAPS) != 0) {
	  Panel p = new Panel();
	  p.setLayout(new BorderLayout());
	  p.add("West", m2);
	  add(p);
      }
      if ((mask & SEARCHMODE_CROSSES) != 0) {
	  Panel p = new Panel();
	  p.setLayout(new BorderLayout());
	  p.add("West", m3);
	  add(p);
      }
      if ((mask & SEARCHMODE_ISCONTAINED) != 0) {
	  Panel p = new Panel();
	  p.setLayout(new BorderLayout());
	  p.add("West", m4);
	  add(p);
      }

      m1.setState(true);
    }

    int getSearchMode() {
	return ((m1.getState() ? SEARCHMODE_CONTAINS : 0) | 
		(m2.getState() ? SEARCHMODE_OVERLAPS : 0) | 
		(m3.getState() ? SEARCHMODE_CROSSES : 0) |
		(m4.getState() ? SEARCHMODE_ISCONTAINED : 0));
    }

  }
