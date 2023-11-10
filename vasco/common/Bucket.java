/* $Id: Bucket.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Bucket implements TextListener {
	BucketIface bi;

	public Bucket(TopInterface ti, String lab, BucketIface b) {
		bi = b;

		Panel buck = new Panel();
		buck.setLayout(new BorderLayout());
		buck.add("West", new Label(lab));
		TextField bsize = new TextField(Integer.toString(bi.getBucket()), 2);
		new MouseHelp(bsize, ti.getMouseDisplay(), "Set " + lab, "", "");
		buck.add("East", bsize);
		bsize.addTextListener(this);
		ti.getPanel().add(buck);
	}

	public void textValueChanged(TextEvent te) {
		TextField tf = (TextField) te.getSource();
		int nr;
		try {
			nr = Integer.parseInt(tf.getText());
			if (nr < 1 || nr > 99)
				tf.setText(Integer.toString(nr = bi.getBucket()));
		} catch (NumberFormatException exc) {
			tf.setText(Integer.toString(nr = bi.getBucket()));
		}
		bi.setBucket(nr);
	}
}
