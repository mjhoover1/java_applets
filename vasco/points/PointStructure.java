package vasco.points;

/* $Id: PointStructure.java,v 1.3 2007/10/28 15:38:18 jagan Exp $ */
import java.awt.Choice;
import vasco.common.CommonConstants;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.SpatialStructure;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;

/**
 * Abstract class PointStructure extends SpatialStructure and provides a
 * framework for spatial data structures that manipulate point data. It defines
 * common operations and utilities for managing and interacting with points in a
 * spatial context.
 */
abstract public class PointStructure extends SpatialStructure implements CommonConstants {

	// Factors used in spatial calculations
	final double xf[] = { 0, 0.5, 0, 0.5 };
	final double yf[] = { 0.5, 0.5, 0, 0 };

	/**
	 * Constructs a new PointStructure.
	 * 
	 * @param can The bounding rectangle for the spatial structure.
	 * @param ti  The top interface for interacting with the spatial structure.
	 * @param r   The rebuild tree for dynamic modification of the spatial
	 *            structure.
	 */
	public PointStructure(DRectangle can, TopInterface ti, RebuildTree r) {
		super(can, ti, r);
	}

	/**
	 * Reinitializes the spatial structure with new operations.
	 * 
	 * @param ops The set of operations to be available in the spatial structure.
	 */
	public void reInit(Choice ops) {
		super.reInit(ops);
		availOps.addItem("Insert");
		availOps.addItem("Move");
		availOps.addItem("Delete");
		availOps.addItem("Overlap");
	}

	/**
	 * Inserts a drawable object into the spatial structure. This is a wrapper
	 * method that casts the drawable object to a DPoint and calls the abstract
	 * Insert method.
	 * 
	 * @param r The drawable object to be inserted.
	 * @return true if the insertion is successful, false otherwise.
	 */
	public boolean Insert(Drawable r) {
		return Insert((DPoint) r);
	}

	/**
	 * Abstract method to insert a point into the spatial structure.
	 * 
	 * @param p The point to be inserted.
	 * @return true if the insertion is successful, false otherwise.
	 */
	abstract public boolean Insert(DPoint p);

	/* -------------- common utilities ------------------- */

	/**
	 * Utility class for comparing points based on their X coordinates.
	 */
	public class XComparable implements vasco.common.Comparable {
		DPoint p;

		/**
		 * Constructs a new XComparable object for a given point.
		 * 
		 * @param p The point to be compared.
		 */
		public XComparable(DPoint p) {
			this.p = p;
		}

		/**
		 * Returns the X coordinate of the point for comparison.
		 * 
		 * @return The X coordinate of the point.
		 */
		public double sortBy() {
			return p.x;
		}
	}

	/**
	 * Utility class for comparing points based on their Y coordinates.
	 */
	public class YComparable implements vasco.common.Comparable {
		DPoint p;

		/**
		 * Constructs a new YComparable object for a given point.
		 * 
		 * @param p The point to be compared.
		 */
		public YComparable(DPoint p) {
			this.p = p;
		}

		/**
		 * Returns the Y coordinate of the point for comparison.
		 * 
		 * @return The Y coordinate of the point.
		 */
		public double sortBy() {
			return p.y;
		}
	}

	/*
	 * double compareToX(DPoint a, DPoint b) { return a.x - b.x; }
	 * 
	 * double compareToY(DPoint a, DPoint b) { return a.y - b.y; }
	 */

	/**
	 * Returns the opposite quadrant of a given quadrant.
	 * 
	 * @param Q The quadrant for which the opposite is to be found.
	 * @return The opposite quadrant.
	 */
	int OpQuad(int Q) {
		switch (Q) {
		case NW:
			return SE;
		case NE:
			return SW;
		case SW:
			return NE;
		case SE:
			return NW;
		}
		return -1;
	}

	/**
	 * Returns the clockwise quadrant of a given quadrant.
	 * 
	 * @param Q The quadrant for which the clockwise quadrant is to be found.
	 * @return The clockwise quadrant.
	 */
	int CQuad(int Q) {
		switch (Q) {
		case NW:
			return NE;
		case NE:
			return SE;
		case SE:
			return SW;
		case SW:
			return NW;
		}
		return -1;
	}

	/**
	 * Determines the counter-clockwise quadrant relative to a given quadrant. This
	 * method is used to identify the adjacent quadrant in a counter-clockwise
	 * direction.
	 * 
	 * @param Q The initial quadrant for which the counter-clockwise adjacent
	 *          quadrant is sought. Quadrants are represented by the constants NW,
	 *          NE, SE, and SW.
	 * @return The quadrant that is counter-clockwise adjacent to the given
	 *         quadrant. Returns -1 if the input quadrant is not recognized.
	 */
	int CCQuad(int Q) {
		switch (Q) {
		case NW:
			return SW;
		case NE:
			return NW;
		case SE:
			return NE;
		case SW:
			return SE;
		}
		return -1;
	}
}
