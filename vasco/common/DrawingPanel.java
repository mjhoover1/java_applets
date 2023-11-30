/* $Id: DrawingPanel.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.Adjustable;

// import org.w3c.dom.events.MouseEvent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
//import java.applet.*;
//import java.util.*;
import java.text.DecimalFormat;

// import java.awt.*;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

/* ---------------------------------------------------------------------
 *
 *         Drawing Panel
 *
 */
/**
 * A panel for drawing graphics with zooming and panning capabilities.
 */
public class DrawingPanel extends JPanel
		implements AdjustmentListener, MouseMotionListener, MouseListener, ItemListener {

	Insets insets;
	int zoomStep;
	JScrollBar sphor, spvert;
	// JScrollPane sphor, spvert;
	static final int MAXSCROLL = 512; // make equal to CANSIZE
	RebuildTree rt;
	JTextField upper_left_coord, upper_right_coord, lower_left_coord, lower_right_coord, position_coordinates;
	DrawingCanvas can;
	MouseHelp mh; // help for canvas
	JCheckBox toggleZoom;
	Cursor defCur;
	MouseDisplay mouseDisplay;
	MouseListener canvasML;
	MouseMotionListener canvasMML;
	MouseListener zoomModeML;

	/**
	 * Constructs a DrawingPanel with specified components and listeners.
	 *
	 * @param dc           DrawingCanvas for the drawing area.
	 * @param reb          RebuildTree for updating the graphics.
	 * @param ml           MouseListener for canvas mouse events.
	 * @param mml          MouseMotionListener for canvas mouse motion events.
	 * @param mouseDisplay MouseDisplay for displaying mouse information.
	 */
	public DrawingPanel(DrawingCanvas dc, RebuildTree reb, MouseListener ml, MouseMotionListener mml,
			MouseDisplay mouseDisplay) {
		final int COORDSIZE = 13;
		can = dc;
		this.mouseDisplay = mouseDisplay;
		canvasML = ml;
		canvasMML = mml;
		zoomModeML = new ZoomMode();

		rt = reb;
		defCur = getCursor();
		zoomStep = 1;
		setLayout(new BorderLayout());

		JPanel glob = new JPanel();
		glob.setLayout(new BorderLayout());

		JPanel hor = new JPanel();
		hor.setLayout(new GridLayout(1, 3));
		hor.add(upper_left_coord = new JTextField(COORDSIZE));
		hor.add(toggleZoom = new JCheckBox("Zoom In/Out Mode", false));
		new MouseHelp(toggleZoom, mouseDisplay, "Switch to zoom mode", "", "", "Switch to operation mode", "", "");
		hor.add(upper_right_coord = new JTextField(COORDSIZE));
		// ur.setAlignment(TextField.RIGHT);
		upper_left_coord.setEditable(false);
		upper_right_coord.setEditable(false);
		glob.add("North", hor);

		toggleZoom.addItemListener(this);

		sphor = new JScrollBar(Adjustable.HORIZONTAL, 0, MAXSCROLL, 0, MAXSCROLL);
		spvert = new JScrollBar(Adjustable.VERTICAL, 0, MAXSCROLL, 0, MAXSCROLL);
		glob.add("East", spvert);
		glob.add("South", sphor);

		glob.add("Center", can);
		spvert.addAdjustmentListener(this);
		sphor.addAdjustmentListener(this);
		add("Center", glob);

		// Bottom coordinates panel with FlowLayout
	    JPanel bottomCoord = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Adjust horizontal and vertical gaps as needed

	    // Left coordinate JTextField
	    lower_left_coord = new JTextField(COORDSIZE);
	    lower_left_coord.setEditable(false);
	    bottomCoord.add(lower_left_coord);

	    // Cursor label and position JTextField in a separate panel for better alignment
	    JPanel cursorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // No gaps between label and text field
	    JLabel cursorLabel = new JLabel("Cursor");
	    cursorPanel.add(cursorLabel);

	    position_coordinates = new JTextField(COORDSIZE);
	    position_coordinates.setEditable(false);
	    cursorPanel.add(position_coordinates);

	    bottomCoord.add(cursorPanel);

	    // Right coordinate JTextField
	    lower_right_coord = new JTextField(COORDSIZE);
	    lower_right_coord.setEditable(false);
	    bottomCoord.add(lower_right_coord);

	    add("South", bottomCoord);

		new MouseHelp(can, mouseDisplay, "", "Zoom In", "Zoom out", InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
		can.addMouseListener(this);
		can.addMouseMotionListener(this); // cursor position
		can.addMouseListener(canvasML);
		can.addMouseMotionListener(canvasMML);
		updateCoords();
		insets = new Insets(5, 5, 5, 5);
		setPreferredSize(new Dimension(512 + 28, 512 + 82)); // setting preferred size of drawing panel adding + # for
																// scroll bars
	}

	/**
	 * Returns the insets of the panel.
	 *
	 * @return The insets object representing the margin of this panel.
	 */
	@Override
	public Insets getInsets() {
		return insets;
	}

	/**
	 * Updates the coordinate display fields with current values.
	 */
	private void updateCoords() {
		DecimalFormat df = new DecimalFormat("0.##");
		DPoint p;
		p = can.getUL();
		upper_left_coord.setText("[" + df.format(p.x) + ", " + df.format(p.y) + "]");
		p = can.getUR();
		upper_right_coord.setText("[" + df.format(p.x) + ", " + df.format(p.y) + "]");
		p = can.getLL();
		lower_left_coord.setText("[" + df.format(p.x) + ", " + df.format(p.y) + "]");
		p = can.getLR();
		lower_right_coord.setText("[" + df.format(p.x) + ", " + df.format(p.y) + "]");
	}

	/**
	 * Inner class to handle zooming mode when mouse is clicked.
	 */
	public class ZoomMode extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent me) {
			Point scrCoord = can.adjustPoint(me.getPoint());
			DPoint p = can.transPointT(scrCoord);
			if (!me.isAltDown())
				zoomIn(p);
			else
				zoomOut(p);
		}
	}

	/**
	 * Handles item state changes, particularly for the zoom toggle checkbox.
	 *
	 * @param ie The item event that triggered the change.
	 */
	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() == ItemEvent.SELECTED) {
			mh = new MouseHelp(can, mouseDisplay, "Zoom In", "Zoom out", "",
					InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
			can.removeMouseListener(canvasML);
			can.removeMouseMotionListener(canvasMML);
			can.removeMouseListener(this);
			can.addMouseListener(zoomModeML);
		} else {
			mh.removeHelp();
			can.addMouseListener(canvasML);
			can.addMouseMotionListener(canvasMML);
			can.addMouseListener(this);
			can.removeMouseListener(zoomModeML);
		}
	}

	/**
	 * Zooms into the graphics area.
	 *
	 * @param p The point around which to zoom in.
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
				rt.redraw();
			}
			zoomStep *= 2;
			sphor.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float) zoomStep) * can.getXperc()),
					MAXSCROLL / zoomStep, 0, MAXSCROLL);
			spvert.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float) zoomStep) * can.getYperc()),
					MAXSCROLL / zoomStep, 0, MAXSCROLL);
			updateCoords();
			rt.redraw();
		}
	}

	/**
	 * Zooms out of the graphics area.
	 *
	 * @param p The point around which to zoom out.
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
				rt.redraw();
			}
			zoomStep /= 2;
			sphor.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float) zoomStep) * can.getXperc()),
					MAXSCROLL / zoomStep, 0, MAXSCROLL);
			spvert.setValues(Math.round((MAXSCROLL - MAXSCROLL / (float) zoomStep) * can.getYperc()),
					MAXSCROLL / zoomStep, 0, MAXSCROLL);
			updateCoords();
			rt.redraw();
		}
	}

	/**
	 * Handles mouse click events, particularly for zooming in and out.
	 *
	 * @param me The mouse event that triggered the click.
	 */
	@Override
	public void mouseClicked(MouseEvent me) {
		int mask = MouseDisplay.getMouseButtons(me);
		mask = ~can.getOperationMask() & mask;

		Point scrCoord = can.adjustPoint(me.getPoint());
		DPoint p = can.transPointT(scrCoord);

		if ((mask & InputEvent.BUTTON2_MASK) != 0)
			zoomIn(p);
		if ((mask & InputEvent.BUTTON3_MASK) != 0)
			zoomOut(p);
	}

	/**
	 * Sets the cursor to the default cursor.
	 */
	void setDefaultCursor() {
		setCursor(defCur);
	}

	/**
	 * Sets the cursor to a crosshair cursor.
	 */
	void setHairCursor() {
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	/**
	 * Handles mouse press events. This method is currently empty and can be
	 * overridden to provide custom functionality on mouse press.
	 *
	 * @param me The MouseEvent object containing details about the mouse press
	 *           event.
	 */
	@Override
	public void mousePressed(MouseEvent me) {
	}

	/**
	 * Handles mouse release events. This method is currently empty and can be
	 * overridden to provide custom functionality on mouse release.
	 *
	 * @param me The MouseEvent object containing details about the mouse release
	 *           event.
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
	}

	/**
	 * Handles mouse entered events. Changes the cursor to a crosshair if the
	 * toggleZoom checkbox is selected.
	 *
	 * @param me The MouseEvent object containing details about the mouse entered
	 *           event.
	 */

	@Override
	public void mouseEntered(MouseEvent me) {
		if (toggleZoom.isSelected()) { // Use isSelected instead of getState() in Swing
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
	}

	// public void mouseEntered(MouseEvent me) {
	// if (toggleZoom.getState())
	// setHairCursor();
	// }

	/**
	 * Handles mouse exited events. Resets the cursor to the default cursor.
	 *
	 * @param me The MouseEvent object containing details about the mouse exited
	 *           event.
	 */
	@Override
	public void mouseExited(MouseEvent me) {
		setDefaultCursor();
	}

	/**
	 * Handles mouse moved events. Updates the position text field with the current
	 * mouse coordinates on the drawing canvas.
	 *
	 * @param me The MouseEvent object containing details about the mouse moved
	 *           event.
	 */
	@Override
	public void mouseMoved(MouseEvent me) {
		DecimalFormat df = new DecimalFormat("0.##");
		DPoint p = can.transPointT(me.getX(), me.getY());
		position_coordinates.setText(df.format(p.x) + ", " + df.format(p.y));
	}

	/**
	 * Handles mouse dragged events. Similar to mouseMoved, updates the position
	 * text field with the current mouse coordinates while dragging.
	 *
	 * @param me The MouseEvent object containing details about the mouse dragged
	 *           event.
	 */
	@Override
	public void mouseDragged(MouseEvent me) {
		DPoint p = can.transPointT(me.getX(), me.getY());
		position_coordinates.setText(p.x + ", " + p.y);
	}

	/**
	 * Handles adjustment value changes for scrollbars.
	 *
	 * @param ae The adjustment event from the scrollbar.
	 */
	@Override
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		JScrollBar s = (JScrollBar) ae.getSource();
		if (s == sphor) {
			can.moveX(s.getValue() / (MAXSCROLL - MAXSCROLL / (float) zoomStep));
		} else {
			can.moveY(s.getValue() / (MAXSCROLL - MAXSCROLL / (float) zoomStep));
		}
		updateCoords();
		rt.redraw();
	}
}
