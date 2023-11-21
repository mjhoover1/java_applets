package vasco.regions;
import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
// import java.applet.*;
import java.util.*;
import java.text.*;

public class InfoBox extends JDialog implements ActionListener {
  Button ok;

  public InfoBox(String text) {
    super(new JFrame(), "Message", true);
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
