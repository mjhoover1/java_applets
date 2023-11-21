package vasco.rectangles;

/* $Id: RectangleRTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;

/**
 * The `RectangleRTree` class represents a rectangle structure based on an R-tree.
 * It extends the `RectangleStructure` class and uses an `RTree` for managing rectangles.
 */
public class RectangleRTree extends RectangleStructure {
	RTree rt;

    /**
     * Constructs a new `RectangleRTree` instance with the given parameters.
     *
     * @param can The bounding rectangle of the structure.
     * @param p   The parent structure (if any).
     * @param r   The rebuild tree object.
     */
	public RectangleRTree(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
		rt = new RTree(can, 3, 6, p, r);
	}

    /**
     * Reinitializes the `RectangleRTree` structure with the specified options.
     *
     * @param ops The choice of options for reinitialization.
     */
	public void reInit(JComboBox ops) {
		super.reInit(ops);
		rt.reInit(ops);
	}

    /**
     * Checks if the structure is order-dependent.
     *
     * @return `true` if order-dependent, `false` otherwise.
     */
	public boolean orderDependent() {
		return rt.orderDependent();
	}

    /**
     * Inserts a rectangle into the structure.
     *
     * @param r The rectangle to insert.
     * @return `true` if the insertion is successful, `false` otherwise.
     */
	public boolean Insert(DRectangle r) {
		return rt.Insert(r);
	}

    /**
     * Deletes a point from the structure.
     *
     * @param p The point to delete.
     */
	public void Delete(DPoint p) {
		rt.Delete(p);
	}

    /**
     * Deletes a drawable object directly from the structure.
     *
     * @param p The drawable object to delete.
     */
	public void DeleteDirect(Drawable p) {
		rt.DeleteDirect(p);
	}

    /**
     * Starts the message processing for the structure.
     */
	public void MessageStart() {
		// super.MessageStart();
		rt.MessageStart();
	};

    /**
     * Clears the structure.
     */
	public void Clear() {
		super.Clear();
		rt.Clear();
	}

    /**
     * Ends the message processing for the structure.
     */
	public void MessageEnd() {
		// super.MessageEnd();
		rt.MessageEnd();
	};

    /**
     * Searches for rectangles in the structure based on the specified query and mode.
     *
     * @param r    The query object.
     * @param mode The search mode.
     * @return A `SearchVector` containing the search results.
     */
	public SearchVector Search(QueryObject r, int mode) {
		return rt.Search(r, mode);
	}

    /**
     * Finds the nearest rectangle to the specified query point.
     *
     * @param p The query object representing the point.
     * @return A `SearchVector` containing the nearest rectangle.
     */
	public SearchVector Nearest(QueryObject p) {
		return rt.Nearest(p);
	}

    /**
     * Finds rectangles within a certain distance of the specified query point.
     *
     * @param p    The query object representing the point.
     * @param dist The maximum distance for the query.
     * @return A `SearchVector` containing the matching rectangles.
     */
	public SearchVector Nearest(QueryObject p, double dist) {
		return rt.Nearest(p, dist);
	}

    /**
     * Finds the nearest drawable object to the specified query point.
     *
     * @param p The query object representing the point.
     * @return The nearest drawable object.
     */
	public Drawable NearestFirst(QueryObject p) {
		return rt.NearestFirst(p);
	}


    /**
     * Finds rectangles within a certain distance of the specified query point.
     *
     * @param p    The query object representing the point.
     * @param dist The maximum distance for the query.
     * @return An array of drawable objects within the specified distance.
     */
	public Drawable[] NearestRange(QueryObject p, double dist) {
		return rt.NearestRange(p, dist);
	}

    /**
     * Draws the contents of the structure on the specified drawing target within the given view rectangle.
     *
     * @param g    The drawing target.
     * @param view The view rectangle.
     */
	public void drawContents(DrawingTarget g, Rectangle view) {
		rt.drawContents(g, view);
	}

    /**
     * Draws the grid of the structure at the specified level.
     *
     * @param g     The drawing target.
     * @param level The grid level.
     */
	public void drawGrid(DrawingTarget g, int level) {
		rt.drawGrid(g, level);
	};

    /**
     * Gets the name of the structure.
     *
     * @return The name of the structure.
     */
	public String getName() {
		return rt.getName();
	}

}
