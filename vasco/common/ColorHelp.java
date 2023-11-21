/* $Id: ColorHelp.java,v 1.2 2002/09/25 20:55:02 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ColorHelp extends Dialog implements ActionListener {
  public static int POINT_APPLET = 0;
  public static int LINE_APPLET = 1;
  public static int RECTANGLE_APPLET = 2;
  public static int RTREE_APPLET = 3;
  public static int REGION_APPLET = 4;

  String[] insertHelp = { "Objects in the tree.", "Object being inserted."}; 


  String lineInsert = "Object being inserted and the closest vertex to the position of the mouse for snapping purposes.";

  Color[] insertColors = {Color.red, Color.orange};

  String[] deleteHelp = {
    "Objects in the tree.",
    "Object that would be deleted."
  };

  Color[] deleteColors = {Color.red, Color.orange};

    String[] moveHelp = {
	"Objects in the tree.",
	"Object that would be or is being moved."
    };
    Color[] moveColors = {Color.red, Color.orange};

    String[] moveVertexHelp = {
	"Objects in the tree.",
	"Vertex that would be or is being moved."
    };

    String[] moveEdgeHelp = {
	"Objects in the tree.",
	"Edge that would be or is being moved."
    };

  String[] windowHelp = {
    "Blocks that remain to be processed (i.e., they partially overlap the query range).", 
    "Objects that have not yet been processed but whose smallest enclosing block has been found to be in the query range.", 
    "Objects that have not yet been processed in the sense that their smallest containing block has not been tested with respect to being in the query range or has been found to be outside the query range.",
    "Objects that have been processed and that have been found to be in the query range.",
    "Objects that have been explicitly processed and that have been found to be outside the query range.",
    "Blocks that have been processed (although some of the objects that they contain remain to be processed).",
    "Blocks that have not been processed as they are outside the query range.",
    "The next item to be processed (could be a block or an object).",
    "The query range."
  };
  Color[] windowColors = {Color.cyan, Color.green, Color.red, Color.blue, Color.magenta, Color.gray, Color.white, Color.yellow, Color.orange};


  String[] nearestHelp = {
    "Blocks in the priority queue.", "Objects in the priority queue.", 
    "Objects that have not yet been processed (i.e., entered explicitly into the queue or output into the ranking).", 
    "Objects that have been processed and hence have been ranked. The numeric position of the object in the ranking is displayed next to the object.", 
    "Blocks that have been processed (although their objects may still be in the queue).", 
    "The next item in the queue to be processed (could be a block or an object).", 
    "Parts of the underlying space which have not been processed as they are outside the space spanned by the bounding boxes of the data objects.",
    "The query range."};
  Color[] nearestColors = {Color.cyan, Color.green, Color.red, Color.blue, Color.gray, Color.yellow, Color.white, Color.orange};

  String[] bintreeHelp = {
    "Bintree structure.",
    "Center of quadrant for which the bintrees are being displayed."
  };
  Color[] bintreeColors = {Color.blue, Color.orange};

    // --------------------------

    class helpStruct {
	String opName;
	String opTitle;
	String[] opText;
	Color[] opColor;
	helpStruct(String n, String t, String[] tx, Color[] c) {
	    opName = n; opTitle = t; opText = tx; opColor = c;
	}
    }

    helpStruct[] help = {
	new helpStruct("Insert", "Insert legend", insertHelp, insertColors),
	new helpStruct("Move", "Move legend", moveHelp, moveColors),
	new helpStruct("Move vertex", "Move legend", moveVertexHelp, moveColors),
	new helpStruct("Move edge", "Move legend", moveEdgeHelp, moveColors),
	new helpStruct("Delete", "Delete legend", deleteHelp, deleteColors),
	new helpStruct("Overlap", "Range Search Animation Legend", windowHelp, windowColors),
	new helpStruct("Nearest", "Incremental Nearest Neighbor Animation Legend", nearestHelp, nearestColors),
	new helpStruct("Within", "Within Query Animation Legend", nearestHelp, nearestColors),
	new helpStruct("Show Bintree", "Bintree Coloring Legend", bintreeHelp, bintreeColors)
    };

  static String[] objects = {"point", "line", "rectangle", "rectangle"};

  protected Button close;
  protected String objectString;
  public ColorHelp(){
    super(new Frame(), false);
  }

  public ColorHelp(String opType, int obj_nr) {
    super(new Frame(), false);
    int legIndex = -1;
    for (int i = 0; i < help.length; i++)
	if (help[i].opName.equals(opType)) {
	    legIndex = i;
	    break;
	}
    if (legIndex == -1) {
	return;
    }

    setTitle(help[legIndex].opTitle);

    objectString = objects[obj_nr];

    setBackground(Color.lightGray);
    GridBagLayout gbl = new GridBagLayout();
    setLayout(gbl);
    
    GridBagConstraints cn = new GridBagConstraints();
    cn.gridx = 0;
    cn.gridy = GridBagConstraints.RELATIVE;
    cn.gridwidth = GridBagConstraints.REMAINDER;
    cn.fill = GridBagConstraints.NONE;
    cn.anchor = GridBagConstraints.NORTHWEST;

    for (int i = 0; i < help[legIndex].opColor.length; i++) {
      if (help[legIndex].opColor[i] == Color.white && obj_nr != RTREE_APPLET && opType.equals("Nearest"))
	continue;
      Panel p = createPanel(( (help[legIndex].opColor[i] == Color.orange && 
			       opType.equals("Insert") && 
			       obj_nr == LINE_APPLET)?
			      lineInsert : help[legIndex].opText[i]), 
			    help[legIndex].opColor[i]);
      add(p);
      gbl.setConstraints(p, cn);
    }
    add(close = new Button("Close"));
    close.addActionListener(this);
    pack();
  }

  protected Panel createPanel(String text, Color c) {
    Panel p = new Panel();
    Panel sub = new Panel();
    Label l = new Label("    ");
    l.setBackground(c);
    p.add(l);

    String[] form = format(replace(text, objectString), 50);
    sub.setLayout(new GridLayout(form.length, 1));
    for (int i = 0; i < form.length; i++)
      sub.add(new Label(form[i]));
    p.add(sub);
    return p;
  }

  String replace(String source, String newobj) {
    String res = "";
    String upper = Character.toUpperCase(newobj.charAt(0)) + newobj.substring(1);
    int i, j;
    i = 0;

    do {
      j = source.toLowerCase().indexOf("object", i);
      if (j != -1) {
	res += source.substring(i, j);
	res += source.charAt(j) == 'O' ? upper : newobj;
	i = j + "object".length();
      } else { 
	res += source.substring(i);
	return res;
      }
    } while (true);
  }

  public static String[] format(String s, int width) {
    int i;
    int index = 0;

    Vector strings = new Vector();
    while (s.length() - index > width) { 
      for (i = Math.min(index + width, s.length() - 1); i > index; i--) {
	if (s.charAt(i) == ' ') {
	  strings.addElement(s.substring(index, i));
	  index = i + 1;
	  break;
	}
      }
      if (index == i) {
	index += Math.min(index + width, s.length() - index);
      }
    }
    strings.addElement(s.substring(index, s.length()));

    String[] res = new String[strings.size()];
    strings.copyInto(res);
    return res;
  }

  public void actionPerformed(ActionEvent event) {
      if (event.getSource() == close) {
	dispose();
      }
  }
}

