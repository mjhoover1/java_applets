/* $Id: OverviewWindow.java,v 1.2 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;
import java.text.*;

/* ---------------------------------------------------------------------
 *
 *         Drawing Panel
 *
 */


public class OverviewWindow extends Dialog implements AdjustmentListener, MouseMotionListener, 
						      MouseListener, ComponentListener,
						      ActionListener {


    public static final int OVERVIEW_SIZE = 192;
	//public static final int OVERVIEW_SIZE = 512;

    int zoomStep;
    Scrollbar sphor, spvert;
    static final int MAXSCROLL = 512;  // make equal to CANSIZE
    RebuildTree rt;
    TextField ulx, uly, urx, ury, llx, lly, lrx, lry, position;
    DrawingCanvas can;
    Canvas left, right;
    MouseHelp mh; // help for canvas
    MouseDisplay mouseDisplay;
    Button close;

    abstract class OverviewCanvas extends Canvas {
	public OverviewCanvas() {
	    super();
	    setSize(80, OVERVIEW_SIZE);
	}
    }

    class LeftCanvas extends OverviewCanvas {
	public void paint(Graphics g) {
	    super.paint(g);
	    //g.setColor(Color.red);
//g.fillRect(0, 0, getSize().width, getSize().height);
	    FontMetrics fm = g.getFontMetrics();	
	    DecimalFormat df = new DecimalFormat("0.##");
	    String s;
	    DPoint up = can.getUL();
	    DPoint down = can.getLL();
	    g.setColor(Color.black);
	    s = "[" + df.format(up.x) + ", ";
	    g.drawString(s, getSize().width - fm.stringWidth(s), fm.getHeight());
	    s = df.format(up.y) + "]";
	    g.drawString(s, getSize().width - fm.stringWidth(s), 2*fm.getHeight());

	    s = "[" + df.format(down.x) + ", ";
	    g.drawString(s, getSize().width - fm.stringWidth(s), 
			 getSize().height - 2*fm.getHeight());
	    s = df.format(down.y) + "]";
	    g.drawString(s, getSize().width - fm.stringWidth(s), 
			 getSize().height - fm.getHeight());
	}
    }
    class RightCanvas extends OverviewCanvas {
	public void paint(Graphics g) {
	    super.paint(g);
	    //g.setColor(Color.red);
//g.fillRect(0, 0, getSize().width, getSize().height);
	    FontMetrics fm = g.getFontMetrics();	
	    DecimalFormat df = new DecimalFormat("0.##");
	    String s;

	    DPoint up = can.getUR();
	    DPoint down = can.getLR();

	    g.setColor(Color.black);
	    g.drawString("[" + df.format(up.x) + ", ", 0, fm.getHeight());
	    g.drawString(df.format(up.y) + "]", 0, 2*fm.getHeight());

	    s = "[" + df.format(down.x) + ", ";
	    g.drawString(s, 0, getSize().height - 2 * fm.getHeight());
	    s = df.format(down.y) + "]";
	    g.drawString(s, 0, getSize().height - fm.getHeight());
	}
    }

    protected GridBagConstraints createConstraints(int gx, int gy, 
						   int gw, int gh, int fill) {
	return createConstraints(gx, gy, gw, gh, fill, 0, 0);
    }

    protected GridBagConstraints createConstraints(int gx, int gy, 
						   int gw, int gh, int fill, 
						   int wx, int wy) {
	GridBagConstraints dp = new GridBagConstraints();
	dp.gridx = gx;
	dp.gridy = gy;
	dp.gridwidth = gw;
	dp.gridheight = gh;
	dp.fill = fill;
	dp.anchor = GridBagConstraints.NORTH;
	dp.weightx = wx;
	dp.weighty = wy;
	return dp;
    }

  public OverviewWindow(DrawingCanvas dc, RebuildTree reb, MouseDisplay mouseDisplay) {
      super(new Frame(), "Magnifying glass");
      //setSize(150,150);
    final int COORDSIZE = 7;
    can = dc;
    can.addComponentListener(this);
    this.mouseDisplay = mouseDisplay;
    rt = reb;
    zoomStep = 1;

    /*
    setLayout(new BorderLayout());


    Panel topCoor = new Panel();
    topCoor.setLayout(new GridLayout(2, 2));
    topCoor.add(ulx = new TextField(COORDSIZE));
    topCoor.add(urx = new TextField(COORDSIZE));
    topCoor.add(uly = new TextField(COORDSIZE));
    topCoor.add(ury = new TextField(COORDSIZE));
    ulx.setEditable(false);
    urx.setEditable(false);
    uly.setEditable(false);
    ury.setEditable(false);
    add("North", topCoor);
    */

    Container glob = this;
    GridBagLayout gbl = new GridBagLayout();
    glob.setLayout(gbl);
    left = new LeftCanvas();
    right = new RightCanvas();

    Panel dcan = new Panel();
    dcan.setLayout(new BorderLayout());

    sphor = new Scrollbar(Scrollbar.HORIZONTAL, 0, OVERVIEW_SIZE, 0, MAXSCROLL);
    spvert = new Scrollbar(Scrollbar.VERTICAL, 0, OVERVIEW_SIZE, 0, MAXSCROLL);
    spvert.addAdjustmentListener(this);
    sphor.addAdjustmentListener(this);

    gbl.setConstraints(left, createConstraints(0, 0,
					       1, 1, 
					       GridBagConstraints.VERTICAL));
    glob.add(left);

    dcan.add("West", can);
    dcan.add("East", spvert);
    dcan.add("South", sphor);
    gbl.setConstraints(dcan, createConstraints(GridBagConstraints.RELATIVE, 0,
					      1, 1, 
					      GridBagConstraints.NONE));

    glob.add(dcan);
    gbl.setConstraints(right, createConstraints(GridBagConstraints.RELATIVE, 0,
					       GridBagConstraints.REMAINDER, 1, 
					       GridBagConstraints.BOTH));
    glob.add(right);

    /*
    glob.add("East", spvert);
    glob.add("South", sphor);
    Panel dp = new Panel();
    dp.setSize(OVERVIEW_SIZE, OVERVIEW_SIZE);
    dp.add(can);
    glob.add("Center", dp);
    add("Center", glob);

    Panel bottm = new Panel();
    bottm.setLayout(new BorderLayout());

    Panel bottomCoor = new Panel();
    bottomCoor.setLayout(new GridLayout(2, 2));
    bottomCoor.add(llx = new TextField(COORDSIZE));
    bottomCoor.add(lrx = new TextField(COORDSIZE));
    bottomCoor.add(lly = new TextField(COORDSIZE));
    bottomCoor.add(lry = new TextField(COORDSIZE));
    llx.setEditable(false);
    lrx.setEditable(false);
    lly.setEditable(false);
    lry.setEditable(false);
    bottm.add("North", bottomCoor);
    */
    
    Panel cur = new Panel();
    cur.setLayout(new FlowLayout());
    Label l = new Label("Cursor");
    l.setAlignment(Label.RIGHT);
    cur.add(l);
    position = new TextField(2 * COORDSIZE);
    position.setEditable(false);
    cur.add(position);
    gbl.setConstraints(cur, createConstraints(0, 1,
					      GridBagConstraints.REMAINDER, 1, 
					      GridBagConstraints.HORIZONTAL));
    glob.add(cur);
//    bottm.add("Center", cur);

    close = new Button("Close");
    close.addActionListener(this);
    gbl.setConstraints(close, createConstraints(0, 2,
						GridBagConstraints.REMAINDER, 1, 
						GridBagConstraints.BOTH));
    glob.add(close);

    new MouseHelp(can, mouseDisplay, "Zoom In", "Zoom out", "",
		  InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK);
    can.addMouseListener(this);
    can.addMouseMotionListener(this); // cursor position
    //    updateCoords();
    spvert.invalidate();
    sphor.invalidate();
    validate();
    pack();
    //    setSize(300, 300);
    setResizable(false);
    //    show();
  }

  private void updateCoords() {
    DecimalFormat df = new DecimalFormat("0.##");
    DPoint p;
    left.repaint();
    right.repaint();

    p = can.getUL();
    //    ulx.setText("[" + df.format(p.x) + ", ");
    //    uly.setText(df.format(p.y) + "]");
    p = can.getUR();
    //    urx.setText("[" + df.format(p.x) + ", ");
    //    ury.setText(df.format(p.y) + "]");
    p = can.getLL();
    //    llx.setText("[" + df.format(p.x) + ", ");
    //    lly.setText(df.format(p.y) + "]");
    p = can.getLR();
    //    lrx.setText("[" + df.format(p.x) + ", ");	
    //    lry.setText(df.format(p.y) + "]");
  }

    private void zoomIn(DPoint p) {
	if (zoomStep < 64) {
	    DPoint center = can.getCenter();
	    
	    double xdif = (p.x - center.x) / 10;;
	    double ydif = (p.y - center.y) / 10;;

	    double zs = zoomStep;
	    for (int i = 1; i <= 10; i++) {
		zs = zoomStep + i * zoomStep / 10.0;
		can.zoom(new DPoint(center.x + i*xdif, center.y + i*ydif), zs);
		rt.redraw(can);
	    }
	    zoomStep *= 2;
	    sphor.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float)zoomStep) * 
				       can.getXperc()),
			    OVERVIEW_SIZE / zoomStep, 0, MAXSCROLL);
	    spvert.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float)zoomStep) *
					can.getYperc()), 
			     OVERVIEW_SIZE / zoomStep, 0, MAXSCROLL);
	    rt.redraw(can);
	    updateCoords();
	}
    }

    private void zoomOut(DPoint p) {
	if (zoomStep > 1) {
	    DPoint center = can.getCenter();

	    double xdif = (p.x - center.x) / 10;
	    double ydif = (p.y - center.y) / 10;

	    double zs = zoomStep;

	    for (int i = 1; i <= 10; i++) {
		zs = zoomStep - i * zoomStep / 20.0;
		can.zoom(new DPoint(center.x + i*xdif, center.y + i*ydif), zs);
		rt.redraw(can);
	    }
	    zoomStep /= 2;
	    sphor.setValues(Math.round((MAXSCROLL - OVERVIEW_SIZE / (float)zoomStep) * can.getXperc()), 
			    OVERVIEW_SIZE / zoomStep, 0 ,MAXSCROLL);
	    spvert.setValues(Math.round((MAXSCROLL - OVERVIEW_SIZE / (float)zoomStep) * can.getYperc()), 
			     OVERVIEW_SIZE / zoomStep, 0 ,MAXSCROLL);
	    rt.redraw(can);
	    updateCoords();
	}
    }

  public void mouseClicked(MouseEvent me) {
      int mask = MouseDisplay.getMouseButtons(me);
      Point scrCoord = can.adjustPoint(me.getPoint());
      DPoint p  = can.transPointT(scrCoord);

      if ((mask & InputEvent.BUTTON1_MASK) != 0) 
	  zoomIn(p);
      if ((mask & InputEvent.BUTTON2_MASK) != 0) 
	  zoomOut(p);
  }

  public void mousePressed(MouseEvent me) {}
  public void mouseReleased(MouseEvent me) {}
  public void mouseEntered(MouseEvent me) {
  }
  public void mouseExited(MouseEvent me) {
  }



  public void mouseMoved(MouseEvent me) {
    DecimalFormat df = new DecimalFormat("0.##");
    DPoint p = can.transPointT(me.getX(), me.getY());
    position.setText(df.format(p.x) + ", " + df.format(p.y));
  }

  public void mouseDragged(MouseEvent me) {
    DPoint p = can.transPointT(me.getX(), me.getY());
    position.setText(p.x + ", " + p.y);
  }

  public void adjustmentValueChanged(AdjustmentEvent ae) {
    Scrollbar s = (Scrollbar)ae.getSource();
    if (s == sphor) {
      can.moveX(s.getValue() / (MAXSCROLL - OVERVIEW_SIZE / (float)zoomStep));
    } else {
      can.moveY(s.getValue() / (MAXSCROLL - OVERVIEW_SIZE / (float)zoomStep));
    }
    rt.redraw(can);
    updateCoords();
  }

    public void componentResized(ComponentEvent ce){
	//	can.changeViewPort(new Rectangle(0, 0, can.getPreferredSize().width, 
	//					 can.getPreferredSize().height));
    };
    public void componentMoved(ComponentEvent ce){};
    public void componentShown(ComponentEvent ce){};
    public void componentHidden(ComponentEvent ce){};

    public void actionPerformed(ActionEvent ae) {
	dispose();
    }

}
