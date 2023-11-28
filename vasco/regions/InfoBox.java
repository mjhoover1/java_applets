package vasco.regions;
import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
// import java.applet.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.*;

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
    setLocation(( screen.width - getSize().width ) / 2,
	 ( screen.height - getSize().height ) / 2 );
    setResizable(false); 
    show();
  }

    public void actionPerformed(ActionEvent event) {
	dispose();
    }
}
