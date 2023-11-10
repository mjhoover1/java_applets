/* $Id: MessageBox.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.text.*;

public class MessageBox extends Dialog implements ActionListener {
  Button ok;

  MessageBox(String text) {
    super(new Frame(), "Message", true);
    setLayout(new BorderLayout());
    add("North", new Label(text));
    ok = new Button("OK");
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
