package vasco.common;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**
 * Abstract class representing a generic canvas that extends GeneralCanvas and
 * implements RebuildTree, MouseListener, and MouseMotionListener interfaces.
 */
public abstract class GenericCanvas extends GeneralCanvas implements RebuildTree, MouseListener, MouseMotionListener {

	/**
	 * Constructs a GenericCanvas with the specified parameters.
	 *
	 * @param can      The bounding rectangle of the canvas.
	 * @param dt       The primary drawing target.
	 * @param overview The overview drawing target.
	 * @param m        The panel containing the canvas.
	 * @param ti       The top-level interface.
	 */
	public GenericCanvas(DRectangle can, DrawingTarget dt, DrawingTarget overview, JPanel m, TopInterface ti) {
		super(can, dt, overview, m, ti);
	}

	/**
	 * Abstract method to initialize data structures in the canvas.
	 */
	public abstract void initStructs();
}
