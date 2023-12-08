/* $Id: OverviewWindow.java,v 1.2 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
//import java.applet.*;
//import java.util.*;
import java.text.DecimalFormat;

// import java.awt.*;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/* ---------------------------------------------------------------------
 *
 *         Drawing Panel
 *
 */
/**
 * OverviewWindow class provides a detailed overview window with zoom and scroll
 * functionalities.
 */
public class OverviewWindow extends JDialog
		implements AdjustmentListener, MouseMotionListener, MouseListener, ComponentListener, ActionListener {

	public static final int OVERVIEW_SIZE = 192;
	// public static final int OVERVIEW_SIZE = 512;

	int zoomStep;
	JScrollBar sphor, spvert;
	static final int MAXSCROLL = 512; // make equal to CANSIZE
	RebuildTree rt;
	JTextField ulx, uly, urx, ury, llx, lly, lrx, lry, position;
	DrawingCanvas can;
	JPanel left, right;
	MouseHelp mh; // help for canvas
	MouseDisplay mouseDisplay;
	JButton close;

	/**
	 * Inner class representing a canvas with basic setup for the overview window.
	 */
	abstract class OverviewCanvas extends JPanel {
		public OverviewCanvas() {
			super();
			setPreferredSize(new Dimension(80, OVERVIEW_SIZE)); // setSize(80, OVERVIEW_SIZE);
		}
	}

	/**
	 * LeftCanvas class extends OverviewCanvas to display specific information on
	 * the left side.
	 */
	class LeftCanvas extends OverviewCanvas {
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			// g.setColor(Color.red);
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
			g.drawString(s, getSize().width - fm.stringWidth(s), 2 * fm.getHeight());

			s = "[" + df.format(down.x) + ", ";
			g.drawString(s, getSize().width - fm.stringWidth(s), getSize().height - 2 * fm.getHeight());
			s = df.format(down.y) + "]";
			g.drawString(s, getSize().width - fm.stringWidth(s), getSize().height - fm.getHeight());
		}
	}

	/**
	 * RightCanvas class extends OverviewCanvas to display specific information on
	 * the right side.
	 */
	class RightCanvas extends OverviewCanvas {
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			// g.setColor(Color.red);
//g.fillRect(0, 0, getSize().width, getSize().height);
			FontMetrics fm = g.getFontMetrics();
			DecimalFormat df = new DecimalFormat("0.##");
			String s;

			DPoint up = can.getUR();
			DPoint down = can.getLR();

			g.setColor(Color.black);
			g.drawString("[" + df.format(up.x) + ", ", 0, fm.getHeight());
			g.drawString(df.format(up.y) + "]", 0, 2 * fm.getHeight());

			s = "[" + df.format(down.x) + ", ";
			g.drawString(s, 0, getSize().height - 2 * fm.getHeight());
			s = df.format(down.y) + "]";
			g.drawString(s, 0, getSize().height - fm.getHeight());
		}
	}

	/**
	 * Creates a GridBagConstraints object with specified grid position, size, and
	 * fill behavior. This overloaded version provides default weights as 0.
	 *
	 * @param gx   The grid x position (column) in the layout.
	 * @param gy   The grid y position (row) in the layout.
	 * @param gw   The number of columns the component occupies in the grid.
	 * @param gh   The number of rows the component occupies in the grid.
	 * @param fill The fill behavior, which specifies how to distribute extra space.
	 * @return A GridBagConstraints object configured with the specified properties.
	 */
	protected GridBagConstraints createConstraints(int gx, int gy, int gw, int gh, int fill) {
		return createConstraints(gx, gy, gw, gh, fill, 0, 0);
	}

	/**
	 * Creates a GridBagConstraints object with specified grid position, size, fill
	 * behavior, and weight.
	 *
	 * @param gx   The grid x position (column) in the layout.
	 * @param gy   The grid y position (row) in the layout.
	 * @param gw   The number of columns the component occupies in the grid.
	 * @param gh   The number of rows the component occupies in the grid.
	 * @param fill The fill behavior, which specifies how to distribute extra space.
	 * @param wx   The weight of the component in the x direction.
	 * @param wy   The weight of the component in the y direction.
	 * @return A GridBagConstraints object configured with the specified properties.
	 */
	protected GridBagConstraints createConstraints(int gx, int gy, int gw, int gh, int fill, int wx, int wy) {
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

	/**
	 * Constructor for OverviewWindow. Sets up the window with necessary components
	 * and listeners.
	 *
	 * @param dc           The DrawingCanvas to be displayed in this window.
	 * @param reb          An instance of RebuildTree for redraw operations.
	 * @param mouseDisplay A MouseDisplay instance for displaying mouse-related
	 *                     information.
	 */
	public OverviewWindow(DrawingCanvas dc, RebuildTree reb, MouseDisplay mouseDisplay) {
		super(new JFrame(), "Magnifying glass");
		// setSize(150,150);
		final int COORDSIZE = 7;
		can = dc;
		can.addComponentListener(this);
		this.mouseDisplay = mouseDisplay;
		rt = reb;
		zoomStep = 1;

		/*
		 * setLayout(new BorderLayout());
		 *
		 *
		 * Panel topCoor = new Panel(); topCoor.setLayout(new GridLayout(2, 2));
		 * topCoor.add(ulx = new TextField(COORDSIZE)); topCoor.add(urx = new
		 * TextField(COORDSIZE)); topCoor.add(uly = new TextField(COORDSIZE));
		 * topCoor.add(ury = new TextField(COORDSIZE)); ulx.setEditable(false);
		 * urx.setEditable(false); uly.setEditable(false); ury.setEditable(false);
		 * add("North", topCoor);
		 */

		Container glob = this;
		GridBagLayout gbl = new GridBagLayout();
		glob.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();

		left = new LeftCanvas();
		right = new RightCanvas();

		// Set preferred sizes for main components
		left.setPreferredSize(new Dimension(100, OVERVIEW_SIZE));
		right.setPreferredSize(new Dimension(100, OVERVIEW_SIZE));
		can.setPreferredSize(new Dimension(OVERVIEW_SIZE, OVERVIEW_SIZE));

		JPanel dcan = new JPanel();
		dcan.setLayout(new BorderLayout());

		sphor = new JScrollBar(Adjustable.HORIZONTAL, 0, OVERVIEW_SIZE, 0, MAXSCROLL);
		spvert = new JScrollBar(Adjustable.VERTICAL, 0, OVERVIEW_SIZE, 0, MAXSCROLL);
		spvert.addAdjustmentListener(this);
		sphor.addAdjustmentListener(this);

		// Setting constraints for left canvas
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(left, gbc); // gbl.setConstraints(left, createConstraints(0, 0, 1, 1, GridBagConstraints.VERTICAL));
        glob.add(left);

		// Setting constraints for drawing canvas and scrollbars
		dcan.add("Center", can); // dcan.add("West", can);
		dcan.add("East", spvert);
		dcan.add("South", sphor);

		gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(dcan, gbc); // gbl.setConstraints(dcan, createConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, GridBagConstraints.NONE));
        glob.add(dcan);

		// Setting constraints for right canvas
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(right, gbc); // 		gbl.setConstraints(right, createConstraints(GridBagConstraints.RELATIVE, 0, GridBagConstraints.REMAINDER, 1, GridBagConstraints.BOTH));
		glob.add(right);

		/*
		 * glob.add("East", spvert); glob.add("South", sphor); Panel dp = new Panel();
		 * dp.setSize(OVERVIEW_SIZE, OVERVIEW_SIZE); dp.add(can); glob.add("Center",
		 * dp); add("Center", glob);
		 *
		 * Panel bottm = new Panel(); bottm.setLayout(new BorderLayout());
		 *
		 * Panel bottomCoor = new Panel(); bottomCoor.setLayout(new GridLayout(2, 2));
		 * bottomCoor.add(llx = new TextField(COORDSIZE)); bottomCoor.add(lrx = new
		 * TextField(COORDSIZE)); bottomCoor.add(lly = new TextField(COORDSIZE));
		 * bottomCoor.add(lry = new TextField(COORDSIZE)); llx.setEditable(false);
		 * lrx.setEditable(false); lly.setEditable(false); lry.setEditable(false);
		 * bottm.add("North", bottomCoor);
		 */

		// Cursor position panel
		JPanel cur = new JPanel();
		cur.setLayout(new FlowLayout());
		JLabel l = new JLabel("Cursor");
		// l.setAlignmentX(SwingConstants.RIGHT); // l.setAlignment(JLabel.RIGHT);
		cur.add(l);
		position = new JTextField(2 * COORDSIZE);
		position.setEditable(false);
		cur.add(position);

		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // Span across three columns
        gbc.gridheight = 1;
        gbl.setConstraints(cur, gbc); // 		gbl.setConstraints(cur, createConstraints(0, 1, GridBagConstraints.REMAINDER, 1, GridBagConstraints.HORIZONTAL));
		glob.add(cur);
//    bottm.add("Center", cur);

        // Close button
		close = new JButton("Close");
		close.addActionListener(this);

		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3; // Span across three columns
        gbc.gridheight = 1;
        gbl.setConstraints(close, gbc); // gbl.setConstraints(close, createConstraints(0, 2, GridBagConstraints.REMAINDER, 1, GridBagConstraints.BOTH));
		glob.add(close);

		new MouseHelp(can, mouseDisplay, "Zoom in", "Zoom out", "", InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK);
		can.addMouseListener(this);
		can.addMouseMotionListener(this); // cursor position
		// updateCoords();
		spvert.invalidate();
		sphor.invalidate();
		validate();
		pack();

		// Set a minimum size to ensure the window is not too small
		setMinimumSize(new Dimension(400, 300));

		centerDialogOnScreen();
		// setSize(300, 300);
		setResizable(true);
		// show();
	}

    private void centerDialogOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(screenSize.width, getWidth());
        int height = Math.min(screenSize.height, getHeight());
        setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
    }

	/**
	 * Updates the coordinates displayed in the overview window.
	 */
	private void updateCoords() {
		DecimalFormat df = new DecimalFormat("0.##");
		DPoint p;
		left.repaint();
		right.repaint();

		p = can.getUL();
		// ulx.setText("[" + df.format(p.x) + ", ");
		// uly.setText(df.format(p.y) + "]");
		p = can.getUR();
		// urx.setText("[" + df.format(p.x) + ", ");
		// ury.setText(df.format(p.y) + "]");
		p = can.getLL();
		// llx.setText("[" + df.format(p.x) + ", ");
		// lly.setText(df.format(p.y) + "]");
		p = can.getLR();
		// lrx.setText("[" + df.format(p.x) + ", ");
		// lry.setText(df.format(p.y) + "]");
	}

	/**
	 * Zooms in towards a specified point in the drawing canvas. The zooming is done
	 * incrementally to provide a smooth transition.
	 *
	 * @param p The point towards which the zooming is directed. It specifies the
	 *          focal point of the zoom.
	 */
	private void zoomIn(DPoint p) {
		if (zoomStep < 64) {
			DPoint center = can.getCenter();

			double xdif = (p.x - center.x) / 10;

			double ydif = (p.y - center.y) / 10;

			double zs = zoomStep;
			for (int i = 1; i <= 10; i++) {
				zs = zoomStep + i * zoomStep / 10.0;
				can.zoom(new DPoint(center.x + i * xdif, center.y + i * ydif), zs);
				rt.redraw(can);
			}
			zoomStep *= 2;
			sphor.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float) zoomStep) * can.getXperc()),
					OVERVIEW_SIZE / zoomStep, 0, MAXSCROLL);
			spvert.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float) zoomStep) * can.getYperc()),
					OVERVIEW_SIZE / zoomStep, 0, MAXSCROLL);
			rt.redraw(can);
			updateCoords();
		}
	}

	/**
	 * Zooms out from a specified point in the drawing canvas. The zooming out
	 * process is gradual for a smoother visual effect.
	 *
	 * @param p The point from which the zooming out is initiated.
	 */
	private void zoomOut(DPoint p) {
		if (zoomStep > 1) {
			DPoint center = can.getCenter();

			double xdif = (p.x - center.x) / 10;
			double ydif = (p.y - center.y) / 10;

			double zs = zoomStep;

			for (int i = 1; i <= 10; i++) {
				zs = zoomStep - i * zoomStep / 20.0;
				can.zoom(new DPoint(center.x + i * xdif, center.y + i * ydif), zs);
				rt.redraw(can);
			}
			zoomStep /= 2;
			sphor.setValues(Math.round((MAXSCROLL - OVERVIEW_SIZE / (float) zoomStep) * can.getXperc()),
					OVERVIEW_SIZE / zoomStep, 0, MAXSCROLL);
			spvert.setValues(Math.round((MAXSCROLL - OVERVIEW_SIZE / (float) zoomStep) * can.getYperc()),
					OVERVIEW_SIZE / zoomStep, 0, MAXSCROLL);
			rt.redraw(can);
			updateCoords();
		}
	}

	/**
	 * Handles mouse clicked events for zooming in and out.
	 *
	 * @param me MouseEvent that triggered the click.
	 */
	@Override
	public void mouseClicked(MouseEvent me) {
		int mask = MouseDisplay.getMouseButtons(me);
		Point scrCoord = can.adjustPoint(me.getPoint());
		DPoint p = can.transPointT(scrCoord);

		if ((mask & InputEvent.BUTTON1_MASK) != 0)
			zoomIn(p);
		if ((mask & InputEvent.BUTTON2_MASK) != 0)
			zoomOut(p);
	}

	/**
	 * Handles mouse pressed event. This method is called when a mouse button is
	 * pressed on the component.
	 *
	 * @param me The MouseEvent triggered when a mouse button is pressed.
	 */
	@Override
	public void mousePressed(MouseEvent me) {
	}

	/**
	 * Handles mouse released event. This method is called when a mouse button is
	 * released over the component.
	 *
	 * @param me The MouseEvent triggered when a mouse button is released.
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
	}

	/**
	 * Handles mouse entered event. This method is called when the mouse enters the
	 * component.
	 *
	 * @param me The MouseEvent triggered when the mouse enters the component.
	 */
	@Override
	public void mouseEntered(MouseEvent me) {
	}

	/**
	 * Handles mouse exited event. This method is called when the mouse exits the
	 * component.
	 *
	 * @param me The MouseEvent triggered when the mouse exits the component.
	 */
	@Override
	public void mouseExited(MouseEvent me) {
	}

	/**
	 * Handles mouse moved event. This method updates the cursor position display
	 * when the mouse is moved over the component.
	 *
	 * @param me The MouseEvent triggered when the mouse is moved.
	 */
	@Override
	public void mouseMoved(MouseEvent me) {
		DecimalFormat df = new DecimalFormat("0.##");
		DPoint p = can.transPointT(me.getX(), me.getY());
		position.setText(df.format(p.x) + ", " + df.format(p.y));
	}

	/**
	 * Handles mouse dragged event. This method updates the cursor position display
	 * when the mouse is dragged over the component.
	 *
	 * @param me The MouseEvent triggered when the mouse is dragged.
	 */
	@Override
	public void mouseDragged(MouseEvent me) {
		DPoint p = can.transPointT(me.getX(), me.getY());
		position.setText(p.x + ", " + p.y);
	}

	/**
	 * Handles adjustment value changes for scrollbars.
	 *
	 * @param ae AdjustmentEvent that triggered the change.
	 */
	@Override
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		JScrollBar s = (JScrollBar) ae.getSource();
		if (s == sphor) {
			can.moveX(s.getValue() / (MAXSCROLL - OVERVIEW_SIZE / (float) zoomStep));
		} else {
			can.moveY(s.getValue() / (MAXSCROLL - OVERVIEW_SIZE / (float) zoomStep));
		}
		rt.redraw(can);
		updateCoords();
	}

	/**
	 * Handles the component resized event. It adjusts the viewport of the drawing
	 * canvas to match its preferred size when the component is resized.
	 *
	 * @param ce The ComponentEvent triggered when the component is resized.
	 */
	@Override
	public void componentResized(ComponentEvent ce) {
//		 can.changeViewPort(new Rectangle(0, 0, can.getPreferredSize().width,
//		 can.getPreferredSize().height));
	}

	/**
	 * Handles the component moved event. This method is called when the component's
	 * position changes.
	 *
	 * @param ce The ComponentEvent triggered when the component is moved.
	 */
	@Override
	public void componentMoved(ComponentEvent ce) {
	}

	/**
	 * Handles the component shown event. This method is called when the component
	 * becomes visible.
	 *
	 * @param ce The ComponentEvent triggered when the component is shown.
	 */
	@Override
	public void componentShown(ComponentEvent ce) {
	}

	/**
	 * Handles the component hidden event. This method is called when the component
	 * is no longer visible.
	 *
	 * @param ce The ComponentEvent triggered when the component is hidden.
	 */
	@Override
	public void componentHidden(ComponentEvent ce) {
	}

	/**
	 * Handles action performed events, particularly for closing the dialog.
	 *
	 * @param ae ActionEvent that triggered the action.
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		dispose();
	}

}
