package vasco.regions;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.text.*;

public class StructureBox extends Dialog implements ActionListener {
  int row;
  int col;
  TextArea ta;

  public StructureBox(String title, int row, int col){
    super(new Frame(), title, false);
    this.row = row;
    this.col = col;
    setLayout(new BorderLayout());
    ta = new TextArea("", row, col, TextArea.SCROLLBARS_BOTH);
    ta.setEditable(false);
    add("Center", ta);
    pack(); 
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(( screen.width - getSize().width ) / 2,
	 ( screen.height - getSize().height ) / 2 );
    setResizable(false);
    show();
  }
 
  public void setText(String text){
    //ta.replaceRange(text, 0, row * col);
    ta.setText(text);
  }

  public void actionPerformed(ActionEvent event) {
    dispose();
  }
}
