package vasco.rectangles;

/* $Id: RectTree.java,v 1.3 2007/10/28 15:38:19 jagan Exp $ */
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

/**
 * Represents a specialized Quadtree data structure specifically for rectangles.
 * This class extends the generic rectangle quadtree class 'GenRectTree'.
 */
public class RectTree extends GenRectTree {

    /**
     * Constructs a new Rectangle Quadtree.
     *
     * @param can   The canvas area within which the quadtree operates.
     * @param md    Maximum depth of the quadtree.
     * @param p     Interface for higher-level operations and interactions.
     * @param r     Utility for rebuilding tree structures.
     */
	public RectTree(DRectangle can, int md, TopInterface p, RebuildTree r) {
        // Calls the constructor of the superclass (GenRectTree) 
        // with a specific configuration suitable for rectangle quadtrees.
		super(can, md, 1, p, r);
	}

    /**
     * Returns the name of the tree structure.
     * 
     * @return A string representing the name of this quadtree.
     */
	public String getName() {
		return "Rectangle Quadtree";
	}

    /**
     * Indicates whether the order of insertion affects the structure of the quadtree.
     * 
     * @return A boolean value indicating the order dependency of the quadtree.
     *         In this case, it returns false, meaning the tree structure is
     *         not dependent on the order of insertion.
     */
	public boolean orderDependent() {
		return false;
	}
}
