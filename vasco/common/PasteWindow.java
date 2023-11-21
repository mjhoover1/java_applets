/* $Id: PasteWindow.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
// import java.applet.*;
import java.util.*;
import java.text.*;

public class PasteWindow extends JDialog implements ActionListener {
    fileSelector fileSel;
    Button ok;
    Button cancel;
    TextArea imp;
    int nrArgs;

    public PasteWindow(fileSelector fs, String[] out, String helpLine, int nA, boolean loadMode) {
	super(new JFrame(), "Clipboard", true);
	nrArgs = nA;
	setLayout(new BorderLayout());
	fileSel = fs;
	imp = new TextArea(20, 20);
	for (int i = 0; i < out.length; i++)
	    imp.append(out[i] + "\n");

	ok = new Button("OK");
	ok.addActionListener(this);
	cancel = new Button("Cancel");
	cancel.addActionListener(this);
	TextArea help = new TextArea(helpLine, 2, 20);
	help.setEditable(false);
	add("North", help);

	add("Center", imp);
	JPanel p = new JPanel();
	p.setLayout(new GridLayout(1, 2));
	p.add(ok);
	if (loadMode)
	    p.add(cancel);
	add("South", p);
	pack();
	setResizable(false);
    }

    public void  actionPerformed(ActionEvent event) {
	if (event.getSource() == ok) {
	    fileSel.clipBoard(imp.getText());
	    dispose();
	}
	if (event.getSource() == cancel) {
	    fileSel.clipBoard(null);
	    dispose();
	}
    }
}

