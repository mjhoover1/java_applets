package vasco.regions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// import java.awt.*;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class InfoBox extends JDialog implements ActionListener {
	JButton ok;

	public InfoBox(String text) {
		super(new JFrame(), "Message", true);
		setLayout(new BorderLayout());
		add("North", new JLabel(text));
		ok = new JButton("OK");
		ok.addActionListener(this);
		add("South", ok);
		pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screen.width - getSize().width) / 2, (screen.height - getSize().height) / 2);
		setResizable(false);
		show();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		dispose();
	}
}
