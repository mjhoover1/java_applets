package vasco.rectangles;

/* $Id: GenRectTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import java.util.*;
import java.awt.*;

public abstract class GenRectTree extends GenericRectTree {

	public GenRectTree(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
		super(can, md, bs, p, r);
	}

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
