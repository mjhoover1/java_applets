package vasco.common;

import javax.swing.*; // import java.awt.*;
import java.util.*;
import javax.swing.event.*; // import java.awt.event.*;
import javax.swing.text.PlainDocument;

import java.text.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextListener;
import java.lang.Math.*;

// Implements a slider with log-scale units

public class LoosenessFactor implements AdjustmentListener {

  LoosenessFactorIface pstr;
  static final double OFF = 1.0;
  static final double startValue = 0;
  static final double finishValue = 35;
  static final double logBase = (double) 1.06;
  static final double cutOff = (double) 1.01;
  JLabel currentLoosenessFactor;

  private String valueToString(double kk) {
    NumberFormat formatter = new DecimalFormat ( "0.00" ) ; 
    return formatter.format (kk) ; 
  }

  private double logScale(double d) {
    double kk = Math.pow(logBase, d);
    if (kk < cutOff) kk = (double) 1.0;
    return kk;
  }
     

  public LoosenessFactor(TopInterface ti, double initialValue,
                         LoosenessFactorIface th) {
    pstr = th;

    initialValue = Math.log(initialValue)/Math.log(logBase);

    JPanel maxD = new JPanel();
    maxD.setLayout(new GridLayout(1, 3));
    maxD.add(new JLabel("Looseness:"));

    JScrollBar maxDChoice = new JScrollBar(JScrollBar.HORIZONTAL, 
					(int) ((initialValue)), 
                                        1, (int) (startValue), 
                                        (int) (finishValue));


    new MouseHelp(maxDChoice, ti.getMouseDisplay(),
                  "Set maximum looseness factor", "", "");

    maxDChoice.addAdjustmentListener(this);

    currentLoosenessFactor = new JLabel(); 
    double kk = logScale(maxDChoice.getValue());
    currentLoosenessFactor.setText(valueToString(kk - 1.0));

    maxD.add(maxDChoice);
    maxD.add("West", currentLoosenessFactor);
    ti.getPanel().add(maxD);
  }

 public void adjustmentValueChanged(AdjustmentEvent ae) { 
    JScrollBar ch = (JScrollBar) ae.getSource();
    double kk = logScale(ch.getValue());
    pstr.setLoosenessFactor(kk);
    currentLoosenessFactor.setText(valueToString(kk - 1.0));
  }


public class Bucket implements DocumentListener {
  BucketIface bi;

  public Bucket(TopInterface ti, String lab, BucketIface b) {
    bi = b;

    JPanel buck = new JPanel();
    buck.setLayout(new BorderLayout());
    buck.add(new JLabel(lab), BorderLayout.WEST); // buck.add("West", new JLabel(lab));
    JTextField bsize = new JTextField(Integer.toString(bi.getBucket()), 2);
    new MouseHelp(bsize, ti.getMouseDisplay(), "Set " + lab, "", "");
    buck.add(bsize, BorderLayout.EAST); // buck.add("East", bsize);
  
    bsize.getDocument().addDocumentListener(this); // bsize.addTextListener(this);
    ti.getPanel().add(buck);
  }


  public void insertUpdate(DocumentEvent e) {
    updateBucket(e);
  }

  public void removeUpdate(DocumentEvent e) {
    updateBucket(e);
  }

  public void changedUpdate(DocumentEvent e) {
    updateBucket(e);
  }

  private void updateBucket(DocumentEvent e) {
    try {
      JTextField tf = (JTextField) e.getDocument().getProperty("owner");
      int nr = Integer.parseInt(tf.getText());
      if (nr < 1 || nr > 99)
        tf.setText(Integer.toString(nr = bi.getBucket()));
      bi.setBucket(nr);
    } catch (NumberFormatException exc) {
      // Handle the exception
    }
  }
}
 
}
