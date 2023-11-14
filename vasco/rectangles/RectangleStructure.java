package vasco.rectangles;

/* $Id: RectangleStructure.java,v 1.2 2007/10/28 15:38:20 jagan Exp $ */
import vasco.common.*;
import java.awt.*;
//import java.util.*;
import vasco.drawable.*;

/**
 * Represents the abstract base class for spatial structures that manage
 * rectangular shapes. This class extends SpatialStructure and implements
 * operations common to rectangle-based spatial data structures.
 */
public abstract class RectangleStructure extends SpatialStructure implements CommonConstants {

	/**
	 * Constructs a new RectangleStructure.
	 *
	 * @param can The canvas or area within which the structure operates.
	 * @param p   Interface for higher-level operations and interactions.
	 * @param r   Utility for rebuilding tree structures.
	 */
	public RectangleStructure(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
	}

	/**
	 * Reinitializes the operations available in the structure, typically used when
	 * resetting or starting the structure.
	 *
	 * @param ops The operations Choice menu to which the operations will be added.
	 */
	public void reInit(Choice ops) {
		super.reInit(ops);
		availOps.addItem("Insert");
		availOps.addItem("Move");
		availOps.addItem("Move vertex");
		availOps.addItem("Move edge");
		availOps.addItem("Delete");
		availOps.addItem("Overlap");
		availOps.addItem("Nearest");
		availOps.addItem("Within");
	}

	/**
	 * Inserts a drawable object, specifically a DRectangle, into the structure.
	 *
	 * @param d The drawable object to insert.
	 * @return True if the insertion was successful, false otherwise.
	 */
	public boolean Insert(Drawable d) {
		return Insert((DRectangle) d);
	}

	/**
	 * Abstract method to insert a DRectangle. Must be implemented by subclasses.
	 *
	 * @param r The DRectangle to insert.
	 * @return True if the insertion was successful, false otherwise.
	 */
	abstract boolean Insert(DRectangle r);

	/**
	 * Replaces one rectangle with another in the structure. This method is
	 * currently not implemented.
	 *
	 * @param OldRect The rectangle to be replaced.
	 * @param NewRect The new rectangle to insert.
	 * @return False, as the method is not implemented.
	 */
	public boolean ReplaceRectangles(DRectangle OldRect, DRectangle NewRect) {
		return false;
	}

	/**
	 * Finds the enclosing quad block for a given rectangle. This method is not
	 * implemented.
	 *
	 * @param OldRect   The rectangle for which to find the enclosing block.
	 * @param nextLevel If true, find the next level's enclosing block.
	 * @return null, as the method is not implemented.
	 */
	public DRectangle EnclosingQuadBlock(DRectangle OldRect, boolean nextLevel) {
		return null;
	}

	/**
	 * Expands the given rectangle. Currently, this method returns the rectangle
	 * unchanged.
	 *
	 * @param rect The rectangle to expand.
	 * @return The unchanged input rectangle.
	 */
	public DRectangle expand(DRectangle rect) {
		return rect;
	}

	/* ------------------ common utilities ------------------- */

	/**
	 * Returns the opposite quadrant relative to the given quadrant.
	 *
	 * @param Q The quadrant for which to find the opposite.
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
	 * Returns the clockwise quadrant relative to the given quadrant.
	 *
	 * @param Q The quadrant for which to find the clockwise quadrant.
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
	 * Returns the counter-clockwise quadrant relative to the given quadrant.
	 *
	 * @param Q The quadrant for which to find the counter-clockwise quadrant.
	 * @return The counter-clockwise quadrant.
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
