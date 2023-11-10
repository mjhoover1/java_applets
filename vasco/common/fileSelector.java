/* $Id: fileSelector.java,v 1.2 2002/09/25 20:55:08 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
//import java.applet.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.net.*;
import java.lang.*;

public abstract class fileSelector extends Dialog implements ActionListener, ItemListener {

	class RandomWindow extends Dialog implements TextListener, ActionListener {
		Button load, merge, cancel;
		TextField tf;
		int number;

		RandomWindow() {
			super(new Frame(), true);
			load = new Button("Load");
			merge = new Button("Merge");
			cancel = new Button("Cancel");

			setLayout(new GridLayout(3, 1));
			add(new Label("Number of objects to generate:"));
			tf = new TextField("20");
			add(tf);
			tf.addTextListener(this);

			number = 20;
			Panel buts = new Panel();
			buts.setLayout(new GridLayout(1, 3));
			buts.add(load);
			buts.add(merge);
			buts.add(cancel);

			load.addActionListener(this);
			merge.addActionListener(this);
			cancel.addActionListener(this);
			add(buts);
			pack();
			setResizable(false);
			show();
		}

		public void textValueChanged(TextEvent te) {
			int nr;
			TextField tf = (TextField) te.getSource();
			try {
				nr = Integer.parseInt(tf.getText());
			} catch (NumberFormatException exc) {
				nr = -1;
			}
			if (nr < 0 || nr > 5000)
				tf.setText(String.valueOf(number));
			else
				number = nr;
		}

		public void actionPerformed(ActionEvent ae) {
			Button b = (Button) ae.getSource();
			if (b != cancel) {
				formVector(genRandom(number), b == load ? new Vector() : rcanvas.vectorOut());
			}
			dispose();
		}
	}

	protected FileIface rcanvas;
	protected Button save, load, merge, delete, append;
	protected Button cancel, clip, random;
	protected java.awt.List imp;
	protected TextField fname;
	protected String actStr;
	protected String datatype;
	protected String helpmess;
	protected TopInterface topInterface;

	public fileSelector(FileIface rc, String dtype, String hm, String act, TopInterface ti) { // action = { SAVE | LOAD
																								// }
		super(new Frame(), act.compareTo("SAVE") == 0 ? "Save" : "Load", true);
		actStr = act;
		topInterface = ti;
		setLayout(new BorderLayout());
		rcanvas = rc;
		datatype = dtype;
		helpmess = hm;

		Panel fileListPanel = new Panel();
		fileListPanel.setLayout(new BorderLayout());
		fileListPanel.add("North", new Label("Existing files:"));
		fileListPanel.add("South", imp = new fileList(datatype));
		imp.addItemListener(this);

		Panel fileNamePanel = new Panel();
		fileNamePanel.setLayout(new BorderLayout());
		fileNamePanel.add("North", new Label("Filename:"));
		fileNamePanel.add("South", fname = new TextField(25));

		save = new Button("Save");
		save.addActionListener(this);
		new MouseHelp(save, ti.getMouseDisplay(), "Save current data set to server", "", "");
		load = new Button("Load");
		load.addActionListener(this);
		new MouseHelp(load, ti.getMouseDisplay(), "Load data set from server", "", "");
		random = new Button("Random set");
		random.addActionListener(this);
		new MouseHelp(random, ti.getMouseDisplay(), "Generate random data set", "", "");
		merge = new Button("Merge");
		merge.addActionListener(this);
		new MouseHelp(merge, ti.getMouseDisplay(), "Merge server data set with current", "", "");
		append = new Button("Append");
		append.addActionListener(this);
		new MouseHelp(append, ti.getMouseDisplay(), "Append current data set to set on server", "", "");
		delete = new Button("Delete");
		delete.addActionListener(this);
		new MouseHelp(delete, ti.getMouseDisplay(), "Delete data set from server", "", "");
		clip = new Button("Clipboard");
		clip.addActionListener(this);
		if (act.equals("SAVE"))
			new MouseHelp(clip, ti.getMouseDisplay(), "Export current set to text window", "", "");
		else
			new MouseHelp(clip, ti.getMouseDisplay(), "Import data set from text window", "", "");
		cancel = new Button("Cancel");
		cancel.addActionListener(this);
		new MouseHelp(cancel, ti.getMouseDisplay(), "Cancel file operation", "", "");
		add("North", fileListPanel);
		add("Center", fileNamePanel);
		Panel p = new Panel();
		if (act.compareTo("SAVE") == 0) {
			p.add(save);
			p.add(append);
		} else {
			p.add(load);
			p.add(merge);
			p.add(random);
		}
		p.add(clip);
		p.add(delete);
		p.add(cancel);

		p.setLayout(new GridLayout(1, p.getComponentCount()));
		add("South", p);
		pack();
		setResizable(false);
	}

	protected abstract String genRandom(int nr);

	protected abstract void formVector(String s, Vector v);

	void formVector(String[] s, Vector v) {
		String news = "";
		for (int i = 0; i < s.length; i++)
			news += s[i] + " ";
		formVector(news, v);
	}

	void clipBoard(String s) { // called by PasteWindow on exit
		if (s != null && actStr.compareTo("LOAD") == 0)
			formVector(s, new Vector());
	}

	public void actionPerformed(ActionEvent ae) {
		Button btn = (Button) ae.getSource();

		if (btn == load && fname.getText().length() > 0) {
			formVector(Tools.getFile(datatype, fname.getText()), new Vector());
			dispose();
		}

		if (btn == merge && fname.getText().length() > 0) {
			formVector(Tools.getFile(datatype, fname.getText()), rcanvas.vectorOut());
			dispose();
		}

		if (btn == append && fname.getText().length() > 0) {
			Tools.appendFile(datatype, fname.getText(), rcanvas.stringsOut());
			dispose();
		}

		if (btn == save && fname.getText().length() > 0) {
			Tools.putFile(datatype, fname.getText(), rcanvas.stringsOut());
			dispose();
		}
		if (btn == random) {
			new RandomWindow();
			dispose();
		}
		if (btn == clip) {
			PasteWindow pw = new PasteWindow(this, rcanvas.stringsOut(), helpmess, 2, "LOAD".equals(actStr));
			pw.show();
			dispose();
		}
		if (btn == delete && fname.getText().length() > 0) {
			Tools.deleteFile(datatype, fname.getText());
			dispose();
		}

		if (btn == cancel) {
			dispose();
		}

		topInterface.getMouseDisplay().clear();
	}

	public void itemStateChanged(ItemEvent ie) {
		fname.setText(imp.getSelectedItem());
	}

}
