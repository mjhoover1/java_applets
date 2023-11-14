package vasco.points;

/* $Id: GenPRbuck.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;

/**
 * GenPRbuck is an abstract class extending GenericPRbucket.
 * It serves as a foundational class for implementing PR quadtrees with bucketing.
 * This class provides methods for initializing and manipulating the structure of the PR quadtree.
 */
public abstract class GenPRbuck extends GenericPRbucket {

    /**
     * Constructor for initializing a new GenPRbuck instance.
     *
     * @param can A {@link DRectangle} object defining the canvas area for the quadtree.
     * @param b   The bucket size for the quadtree.
     * @param md  The maximum depth of the quadtree.
     * @param p   A {@link TopInterface} instance for top-level interface interactions.
     * @param r   A {@link RebuildTree} instance for managing tree rebuilding operations.
     */
	public GenPRbuck(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
		super(can, b, md, p, r);
	}

    /**
     * Reinitializes the GenPRbuck instance with new options.
     *
     * @param ao A {@link Choice} object containing various options for reinitialization.
     */
	public void reInit(Choice ao) {
		super.reInit(ao);
	}

    /**
     * Inserts a point into the PR quadtree and updates its structure accordingly.
     * This method handles the division of nodes and ensures that points are placed correctly.
     *
     * @param p    The {@link DPoint} to be inserted.
     * @param R    The current {@link PRbucketNode} in the quadtree.
     * @param X    The X-coordinate of the node.
     * @param Y    The Y-coordinate of the node.
     * @param lx   The local x-axis length for the node.
     * @param ly   The local y-axis length for the node.
     * @param md   The maximum depth for the quadtree.
     * @param ok   An array of boolean flags indicating the status of insertion.
     * @return     The updated {@link PRbucketNode} after insertion.
     */
	PRbucketNode insert(DPoint p, PRbucketNode R, double X, double Y, double lx, double ly, int md, boolean[] ok) {
		PRbucketNode T, U;
		int Q;
		ok[0] = ok[0] && md > 0;

		if (R == null) {
			T = new PRbucketNode(BLACK);
			T.addPoint(p);
			return T;
		}

		if (R.NODETYPE == BLACK) {
			if (R.points.size() < maxBucketSize)
				R.addPoint(p);
			else {
				Vector pts = R.points;
				R = new PRbucketNode(GRAY);
				for (int i = 0; i < pts.size(); i++) {
					DPoint pt = (DPoint) pts.elementAt(i);
					insert(pt, R, X, Y, lx, ly, md, ok);
				}
				insert(p, R, X, Y, lx, ly, md, ok);
			}
			return R;
		}
		T = R;
		Q = PRCompare(p, X, Y);
		while (T.SON[Q] != null && T.SON[Q].NODETYPE == GRAY) {
			T = T.SON[Q];
			X += XF[Q] * lx;
			lx /= 2;
			Y += YF[Q] * ly;
			ly /= 2;
			Q = PRCompare(p, X, Y);
			md--;
		}
		if (T.SON[Q] == null) {
			T.SON[Q] = new PRbucketNode(BLACK);
			T.SON[Q].addPoint(p);
			return R;
		}

		U = T;
		T = T.SON[Q];
		X += XF[Q] * lx;
		lx /= 2;
		Y += YF[Q] * ly;
		ly /= 2;
		md--;

		if (T.NODETYPE == BLACK)
			if (T.isIn(p))
				return R;
			else if (T.points.size() < maxBucketSize) {
				T.addPoint(p);
				return R;
			} else {
				Vector pts = T.points;
				U.SON[Q] = T = new PRbucketNode(GRAY);
				for (int i = 0; i < pts.size(); i++) {
					DPoint s = (DPoint) pts.elementAt(i);
					Q = PRCompare(s, X, Y);
					if (T.SON[Q] == null)
						T.SON[Q] = new PRbucketNode(BLACK);
					T.SON[Q].addPoint(s);
				}
				insert(p, T, X, Y, lx, ly, md, ok);
			}
		return R;
	}

}
