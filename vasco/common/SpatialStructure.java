/* $Id: SpatialStructure.java,v 1.3 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

import vasco.drawable.Drawable;
import vasco.drawable.DrawableIn;
import vasco.drawable.DrawableOut;

/**
 * Abstract base class for spatial structures in the Vasco framework. It defines
 * common functionality for spatial data structures and their interaction with
 * the user interface.
 */
public abstract class SpatialStructure implements CommonConstants {

	public TopInterface topInterface;
	public RebuildTree reb;
	public JComboBox<String> availOps = new JComboBox<>();
	public DRectangle wholeCanvas;
	private SwitchCursor cursorThread;

	/**
	 * Constructs a new SpatialStructure.
	 *
	 * @param w            The whole canvas area for the spatial structure.
	 * @param topInterface Interface for interacting with higher-level components.
	 * @param r            RebuildTree instance for rebuilding the spatial
	 *                     structure.
	 */
	public SpatialStructure(DRectangle w, TopInterface topInterface, RebuildTree r) {
		this.topInterface = topInterface;
		reb = r;
		wholeCanvas = w;
	}

	public static void addItemIfNotExists(JComboBox<String> comboBox, String item) {
		boolean exists = false;
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			if (item.equals(comboBox.getItemAt(i))) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			comboBox.addItem(item);
		}
	}

	/**
	 * Reinitializes the spatial structure and its operations. Typically used when
	 * resetting the structure.
	 *
	 * @param ops The choice component for available operations.
	 */
	public void reInit(JComboBox<String> ops) {
		Clear();
		topInterface.getPanel().removeAll();
		availOps = ops;
		// if (ops != null) {
		// ops.removeAll();
		// }
	}

	/**
	 * Abstract method to determine if the order of insertion affects the structure.
	 *
	 * @return True if the order of insertion affects the structure, false
	 *         otherwise.
	 */
	public abstract boolean orderDependent();

	/**
	 * Inner class to manage cursor state during long-running operations.
	 */
	class SwitchCursor extends Thread {
		boolean done;

		SwitchCursor() {
			done = false;
			// System.out.println("run beg");
		}

		@Override
		public void run() {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			if (!done) {
				topInterface.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// System.out.println("cursor set");
			}
			// System.out.println("run end");
		}

		void finish() {
			done = true;
		}
	}

	/**
	 * Begins a message or operation, starting a cursor change if the operation
	 * takes a significant amount of time.
	 */
	public void MessageStart() {
		// Thread.dumpStack();
		// System.out.println("Start");
		cursorThread = new SwitchCursor();
		cursorThread.start();
	}

	/**
	 * Clears the spatial structure. This is an abstract method that must be
	 * implemented by subclasses.
	 */
	public void Clear() {
	}

	/**
	 * Inserts a point into the spatial structure. This method is not implemented in
	 * the abstract class.
	 *
	 * @param p The point to insert.
	 * @return False, as this method is not implemented.
	 */
	public boolean Insert(Point p) {
		return false;
	}

	/**
	 * Deletes a point from the spatial structure. This method is not implemented in
	 * the abstract class.
	 *
	 * @param p The point to delete.
	 * @return False, as this method is not implemented.
	 */
	public boolean Delete(Point p) {
		return false;
	}

	/**
	 * Abstract method to insert a drawable object into the spatial structure.
	 *
	 * @param r The drawable object to insert.
	 * @return True if the insertion was successful, false otherwise.
	 */
	public abstract boolean Insert(Drawable r);

	/**
	 * Abstract method to delete a drawable object based on a DPoint from the
	 * spatial structure.
	 *
	 * @param p The DPoint based on which deletion is to be performed.
	 */
	public abstract void Delete(DPoint p);

	/**
	 * Abstract method to directly delete a drawable object from the spatial
	 * structure.
	 *
	 * @param d The drawable object to delete.
	 */
	public abstract void DeleteDirect(Drawable d);

	/**
	 * Ends a message or operation, resetting the cursor to the default state.
	 */
	public void MessageEnd() {
		// Thread.dumpStack();
		// System.out.println("End");
		if (cursorThread != null)
			cursorThread.finish();
		topInterface.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Abstract method for searching within the spatial structure.
	 *
	 * @param r    The query object for the search.
	 * @param mode The mode of the search.
	 * @return A SearchVector containing the search results.
	 */
	public abstract SearchVector Search(QueryObject r, int mode);

	/**
	 * Abstract method for finding the nearest object to a query point.
	 *
	 * @param p The query object.
	 * @return A SearchVector with the nearest object.
	 */
	public abstract SearchVector Nearest(QueryObject p);

	/**
	 * Abstract method for finding objects within a specified distance from a query
	 * point.
	 *
	 * @param p    The query object.
	 * @param dist The distance within which to search.
	 * @return A SearchVector with objects within the specified distance.
	 */
	public abstract SearchVector Nearest(QueryObject p, double dist);

	/**
	 * Finds the nearest drawable object to a given query point.
	 *
	 * @param p The query object used to find the nearest drawable.
	 * @return The nearest drawable object to the query point.
	 */
	public abstract Drawable NearestFirst(QueryObject p);

	/**
	 * Finds all drawable objects within a specified distance from a query point.
	 *
	 * @param p    The query object.
	 * @param dist The maximum distance within which drawable objects are
	 *             considered.
	 * @return An array of drawable objects within the specified distance of the
	 *         query point.
	 */
	public abstract Drawable[] NearestRange(QueryObject p, double dist);

	/**
	 * Draws the contents of the spatial structure.
	 *
	 * @param g    The drawing target for rendering the structure.
	 * @param view The rectangular area of the view in which the content is drawn.
	 */
	public abstract void drawContents(DrawingTarget g, Rectangle view);

	/**
	 * Draws a grid on the drawing target.
	 *
	 * @param g     The drawing target on which the grid is drawn.
	 * @param level The level of detail or zoom level at which the grid is drawn.
	 */
	public void drawGrid(DrawingTarget g, int level) {
	}

	/**
	 * Retrieves the name of the spatial structure.
	 *
	 * @return The name of the spatial structure.
	 */
	public abstract String getName();

	/**
	 * Gets the currently selected operation in the user interface.
	 *
	 * @return The currently selected operation, or null if no operation is
	 *         selected.
	 */
	public String getCurrentOperation() {
		if (availOps == null)
			return null;
		return (String) availOps.getSelectedItem(); // Casted to String
	}

	/**
	 * Determines how the drawable object relates to the search query and adds it to
	 * the search vector.
	 *
	 * @param s            The search query object.
	 * @param cur          The current drawable object being considered.
	 * @param mode         The search mode (e.g., contains, is contained, overlaps).
	 * @param v            The search vector to which the result is added.
	 * @param searchVector The vector containing search results.
	 */
	public void drawableInOut(QueryObject s, Drawable cur, int mode, SearchVector v, Vector searchVector) {
		boolean isBlue = false;
		if (cur == null || s == null)
			return;

		if ((mode & SEARCHMODE_CONTAINS) != 0) {
			isBlue = isBlue || s.contains(cur);
		} // contains

		if ((mode & SEARCHMODE_ISCONTAINED) != 0) {
			isBlue = isBlue || s.isContained(cur);
		} // is contained

		if ((mode & SEARCHMODE_OVERLAPS) != 0) {
			isBlue = isBlue || s.overlaps(cur);
			// intersection includes some vertices
		}

		if ((mode & SEARCHMODE_CROSSES) != 0) {
			isBlue = isBlue || s.crosses(cur);
			// crosses but not vertices inside one another
		}

		if (isBlue)
			v.addElement(new SVElement(new DrawableIn(cur), searchVector));
		else
			v.addElement(new SVElement(new DrawableOut(cur), searchVector));
	}
}
