package vasco.common;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.text.*;
import java.lang.Math.*;

// Implements a slider with log-scale units

public class LoosenessFactor implements AdjustmentListener {

	LoosenessFactorIface pstr;
	static final double OFF = 1.0;
	static final double startValue = 0;
	static final double finishValue = 35;
	static final double logBase = (double) 1.06;
	static final double cutOff = (double) 1.01;
	Label currentLoosenessFactor;

	private String valueToString(double kk) {
		NumberFormat formatter = new DecimalFormat("0.00");
		return formatter.format(kk);
	}

	private double logScale(double d) {
		double kk = Math.pow(logBase, d);
		if (kk < cutOff)
			kk = (double) 1.0;
		return kk;
	}

	public LoosenessFactor(TopInterface ti, double initialValue, LoosenessFactorIface th) {
		pstr = th;

		initialValue = Math.log(initialValue) / Math.log(logBase);

		Panel maxD = new Panel();
		maxD.setLayout(new GridLayout(1, 3));
		maxD.add(new Label("Looseness:"));

		Scrollbar maxDChoice = new Scrollbar(Scrollbar.HORIZONTAL, (int) ((initialValue)), 1, (int) (startValue),
				(int) (finishValue));

		new MouseHelp(maxDChoice, ti.getMouseDisplay(), "Set maximum looseness factor", "", "");

		maxDChoice.addAdjustmentListener(this);

		currentLoosenessFactor = new Label();
		double kk = logScale(maxDChoice.getValue());
		currentLoosenessFactor.setText(valueToString(kk - 1.0));

		maxD.add(maxDChoice);
		maxD.add("West", currentLoosenessFactor);
		ti.getPanel().add(maxD);
	}

	public void adjustmentValueChanged(AdjustmentEvent ae) {
		Scrollbar ch = (Scrollbar) ae.getSource();
		double kk = logScale(ch.getValue());
		pstr.setLoosenessFactor(kk);
		currentLoosenessFactor.setText(valueToString(kk - 1.0));
	}

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

}
