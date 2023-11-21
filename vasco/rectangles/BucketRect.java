package vasco.rectangles;

/* $Id: BucketRect.java,v 1.2 2004/11/20 22:38:48 brabec Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

/**
 * BucketRect is a specialized implementation of a rectangle-based Quadtree.
 * This class extends GenRectTree to implement a Quadtree structure where each
 * leaf node (bucket) can hold a specified number of rectangles. It's tailored for
 * efficient spatial operations involving rectangular regions.
 */
public class BucketRect extends GenRectTree {

    /**
     * Constructs a new BucketRect Quadtree with specified parameters.
     *
     * @param can The bounding rectangle of the spatial area covered by the Quadtree.
     * @param md  The maximum decomposition level of the Quadtree.
     * @param bs  The bucket size, specifying the maximum number of rectangles in each leaf node.
     * @param p   The top-level interface for interaction with the Quadtree.
     * @param r   An instance of RebuildTree, used for rebuilding the tree structure when necessary.
     */
	public BucketRect(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
		super(can, md, bs, p, r);
	}

    /**
     * Reinitializes the Quadtree with new settings. Extends the functionality of
     * the super class by adding the option to configure bucket capacity.
     *
     * @param c The Choice component used for reinitialization options.
     */
	public void reInit(JComboBox c) {
		super.reInit(c);
		new Bucket(topInterface, "Bucket Capacity", this);
	}

    /**
     * Returns the name of the Quadtree implementation.
     * Useful for identification purposes in user interfaces or logs.
     *
     * @return A string representing the name of this Quadtree implementation.
     */
	public String getName() {
		return "Bucket Rectangle Quadtree";
	}

    /**
     * Indicates whether the order of insertion affects the structure of the Quadtree.
     * For BucketRect, the structure is not dependent on the order of insertion.
     *
     * @return A boolean value indicating if the tree is order dependent. Always returns false for BucketRect.
     */
	public boolean orderDependent() {
		return false;
	}
}
