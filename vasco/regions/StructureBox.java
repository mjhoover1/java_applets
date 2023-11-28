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

public class StructureBox extends JDialog implements ActionListener {
  int row;
  int col;
  JTextArea ta;

  public StructureBox(String title, int row, int col){
    super(new JFrame(), title, false);
    this.row = row;
    this.col = col;
    setLayout(new BorderLayout());
    ta = new JTextArea("", row, col);
    ta.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(ta);
    add(scrollPane, BorderLayout.CENTER);
    pack(); 
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(( screen.width - getSize().width ) / 2,
	 ( screen.height - getSize().height ) / 2 );
    setResizable(false);
    setVisible(true);
  }
 
  public void setText(String text){
    //ta.replaceRange(text, 0, row * col);
    ta.setText(text);
  }

  public void actionPerformed(ActionEvent event) {
    dispose();
  }
}
