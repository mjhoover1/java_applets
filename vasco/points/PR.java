package vasco.points;

/* $Id: PR.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
import java.awt.Choice;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

/**
 * PR class, extending GenPRbuck, represents a PR Quadtree.
 * It is used for spatial data organization and querying in a two-dimensional space.
 * This class provides methods for initialization and querying properties of the PR Quadtree.
 */
public class PR extends GenPRbuck {

    /**
     * Constructor to create a new PR Quadtree instance.
     *
     * @param can A {@link DRectangle} object defining the canvas area for the quadtree.
     * @param md  The maximum depth of the quadtree.
     * @param p   A {@link TopInterface} instance for top-level interface interactions.
     * @param r   A {@link RebuildTree} instance for managing tree rebuilding operations.
     */
	public PR(DRectangle can, int md, TopInterface p, RebuildTree r) {
		super(can, 1, md, p, r);
	}

    /**
     * Reinitializes the PR Quadtree with a new set of options.
     *
     * @param ao A {@link Choice} object containing various options for reinitialization.
     */
	public void reInit(Choice ao) {
		super.reInit(ao);
	}

    /**
     * Retrieves the name of the Quadtree implementation.
     *
     * @return A string representing the name of this Quadtree type, "PR Quadtree".
     */
	public String getName() {
		return "PR Quadtree";
	}

    /**
     * Determines if the ordering of elements affects the structure of the Quadtree.
     *
     * @return A boolean value, false indicating that the order of elements does not affect the Quadtree structure.
     */
	public boolean orderDependent() {
		return false;
	}
}
