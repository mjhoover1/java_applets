package vasco.rectangles;

//import java.awt.*;
import java.util.Vector;

/* $Id: GenRectTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

/**
 * Represents a generic rectangle quadtree structure. This abstract class serves
 * as a base for more specific quadtree implementations.
 */
public abstract class GenRectTree extends GenericRectTree {

	/**
	 * Constructs a new generic rectangle quadtree.
	 *
	 * @param can The canvas area within which the quadtree operates.
	 * @param md  Maximum depth of the quadtree.
	 * @param bs  Bucket size for the nodes of the quadtree.
	 * @param p   Interface for higher-level operations and interactions.
	 * @param r   Utility for rebuilding tree structures.
	 */
	public GenRectTree(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
		// Calls the constructor of the superclass (GenericRectTree)
		// with the specified configuration.
		super(can, md, bs, p, r);
	}

	/**
	 * Attempts to insert a rectangle into the quadtree.
	 *
	 * @param q     The current node of the quadtree.
	 * @param r     The rectangle to be inserted.
	 * @param block The area of the canvas represented by the node.
	 * @param md    Remaining depth for insertion.
	 * @return A boolean value indicating if the insertion was successful.
	 */
	@Override
	boolean localInsert(RNode q, DRectangle r, DRectangle block, int md) {
		boolean ok = true;
		if (q.NODETYPE == WHITE) {
			q.r.addElement(r);
			q.NODETYPE = BLACK;
			if (md < 0)
				ok = false;
			return ok;
		}
		if (q.NODETYPE == BLACK && q.r.size() < maxBucketSize)
			q.r.addElement(r);
		else if (q.NODETYPE == BLACK /* && !r.equal(q.r) */) {
			for (int i = 0; i < q.r.size(); i++) // do not allow intersecting rectangles
				if (r.intersects((DRectangle) q.r.elementAt(i))) {
					q.r.addElement(r);
					return false;
				}

			for (int i = 0; i < 4; i++)
				q.son[i] = new RNode();
			q.NODETYPE = GRAY;
			Vector tmp = q.r;
			q.r = new Vector();
			for (int i = 0; i < tmp.size(); i++)
				ok = localInsert(q, (DRectangle) tmp.elementAt(i), block, md) && ok;
			ok = localInsert(q, r, block, md) && ok;
			return ok;
		}
		if (q.NODETYPE == GRAY) {
			for (int i = 0; i < 4; i++) {
				DRectangle dr = new DRectangle(block.x + xf[i] * block.width, block.y + yf[i] * block.height,
						block.width / 2, block.height / 2);
				if (r.intersects(dr)) {
					// System.out.println("inserting to :" + i);
					ok = localInsert(q.son[i], r, dr, md - 1) && ok;
				}
			}
			return ok;
		}

		return ok;
	}

}
