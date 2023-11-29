/* $Id: MaxDecomp.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// import java.awt.*;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * MaxDecomp class allows setting the maximum decomposition depth through a
 * graphical user interface.
 *
 * The MaxDecomp class provides a graphical user interface element for setting
 * the maximum decomposition depth. Users can select the depth from a dropdown
 * menu, including an option to turn off decomposition. Changes to the maximum
 * decomposition depth trigger events that can be handled by listeners.
 */
public class MaxDecomp implements ItemListener {
	MaxDecompIface pstr;
	static final int OFF = 35;

	/**
	 * Constructs a MaxDecomp object.
	 *
	 * @param ti       The TopInterface used for mouse display interaction.
	 * @param maxValue The maximum value for decomposition depth.
	 * @param th       The MaxDecompIface interface for handling decomposition depth
	 *                 changes.
	 */
	public MaxDecomp(TopInterface ti, int maxValue, MaxDecompIface th) {
		pstr = th;

		JPanel maxD = new JPanel();
		maxD.setLayout(new BorderLayout());
		maxD.add("West", new JLabel("Max Decomposition"));
		JComboBox<String> maxDChoice = new JComboBox<>();
		new MouseHelp(maxDChoice, ti.getMouseDisplay(), "Set maximum decomposition depth", "", "");
		maxDChoice.addItem("Off");
		for (int i = 1; i <= maxValue; i++)
			maxDChoice.addItem(String.valueOf(i));
		maxDChoice.setSelectedIndex(pstr.getMaxDecomp() == OFF ? 0 : pstr.getMaxDecomp()); // maxDChoice.select(pstr.getMaxDecomp()
																							// == OFF ? 0 :
																							// pstr.getMaxDecomp());
		maxD.add("East", maxDChoice);
		maxDChoice.addItemListener(this);
		ti.getPanel().add(maxD);
	}

	/**
	 * Handles the item state change event triggered by the Max Decomposition
	 * choice.
	 *
	 * @param ie The ItemEvent object representing the item state change event.
	 */
	@Override
	public void itemStateChanged(ItemEvent ie) {
		JComboBox<String> ch = (JComboBox<String>) ie.getSource();
		if (ch.getSelectedIndex() == 0)
			pstr.setMaxDecomp(OFF);
		else
			pstr.setMaxDecomp(ch.getSelectedIndex());
	}
}
