/* $Id: PasteWindow.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// import java.awt.*;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class PasteWindow extends JDialog implements ActionListener {
	fileSelector fileSel;
	JButton ok;
	JButton cancel;
	JTextArea imp;
	int nrArgs;

	public PasteWindow(fileSelector fs, String[] out, String helpLine, int nA, boolean loadMode) {
		super(new JFrame(), "Clipboard", true);
		nrArgs = nA;
		setLayout(new BorderLayout());
		fileSel = fs;
		imp = new JTextArea(20, 20);
		for (String element : out)
			imp.append(element + "\n");

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		JTextArea help = new JTextArea(helpLine, 2, 20);
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

	@Override
	public void actionPerformed(ActionEvent event) {
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
