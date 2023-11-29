/* $Id: Bucket.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.BorderLayout;

// import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
// import java.awt.event.TextListener;
// import java.awt.event.*;
import javax.swing.event.DocumentEvent; //import java.awt.event.TextEvent;
import javax.swing.event.DocumentListener; //import java.awt.event.TextEvent;

public class Bucket implements DocumentListener {
	BucketIface bi;
	JTextField bsize; // Keep a reference to the JTextField Added

	public Bucket(TopInterface ti, String lab, BucketIface b) {
		bi = b;

		JPanel buck = new JPanel();
		buck.setLayout(new BorderLayout());
		buck.add(BorderLayout.WEST, new JLabel(lab));
		JTextField bsize = new JTextField(Integer.toString(bi.getBucket()), 2);
		new MouseHelp(bsize, ti.getMouseDisplay(), "Set " + lab, "", "");
		buck.add(BorderLayout.EAST, bsize);
		bsize.getDocument().addDocumentListener(this); // Add DocumentListener bsize.addTextListener(this);
		ti.getPanel().add(buck);
	}

	// Implement the methods from the DocumentListener interface
	@Override
	public void insertUpdate(DocumentEvent e) {
		handleTextChange();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		handleTextChange();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		handleTextChange();
	}

	// Handle the text change event
	private void handleTextChange() {
		int nr;
		try {
			nr = Integer.parseInt(bsize.getText());
			if (nr < 1 || nr > 99)
				bsize.setText(Integer.toString(nr = bi.getBucket()));
		} catch (NumberFormatException exc) {
			bsize.setText(Integer.toString(nr = bi.getBucket()));
		}
		bi.setBucket(nr);
	}

	// public void textValueChanged(TextEvent te) {
	// JTextField tf = (JTextField)te.getSource();
	// int nr;
	// try {
	// nr = Integer.parseInt(tf.getText());
	// if (nr < 1 || nr > 99)
	// tf.setText(Integer.toString(nr = bi.getBucket()));
	// } catch (NumberFormatException exc) {
	// tf.setText(Integer.toString(nr = bi.getBucket()));
	// }
	// bi.setBucket(nr);
	// }
}
