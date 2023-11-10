/* $Id: MaxDecomp.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class MaxDecomp implements ItemListener {
  MaxDecompIface pstr;
  static final int OFF = 35;

  public MaxDecomp(TopInterface ti, int maxValue, MaxDecompIface th) {
    pstr = th;

    Panel maxD = new Panel();
    maxD.setLayout(new BorderLayout());
    maxD.add("West", new Label("Max Decomposition"));
    Choice maxDChoice = new Choice();
    new MouseHelp(maxDChoice, ti.getMouseDisplay(), "Set maximum decomposition depth", "", "");
    maxDChoice.addItem("Off");
    for (int i = 1; i <= maxValue; i++)
      maxDChoice.addItem(String.valueOf(i));
    maxDChoice.select(pstr.getMaxDecomp() == OFF ? 0 : pstr.getMaxDecomp());
    maxD.add("East", maxDChoice);
    maxDChoice.addItemListener(this);
    ti.getPanel().add(maxD);
  }
 
  public void itemStateChanged(ItemEvent ie) {
    Choice ch = (Choice)ie.getSource();
    if (ch.getSelectedIndex() == 0)
      pstr.setMaxDecomp(OFF);
    else
      pstr.setMaxDecomp(ch.getSelectedIndex());
  }
}
