package vasco.points;

import java.util.Vector;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: PMRkd.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.Bucket;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class PMRkd extends GenericPRkdbucket {

	public PMRkd(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
		super(can, b, md, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		new Bucket(topInterface, "Splitting Threshold", this);
	}

	@Override
	public String getName() {
		return "PMR k-d Tree";
	}

	@Override
	public boolean orderDependent() {
		return true;
	}

	@Override
	boolean insert(DPoint p, PRkdbucketNode r, boolean xcoord, double cx, double cy, double sx, double sy, int md) {
		boolean ok = md > 0;

		if (r.nodetype == WHITE) {
			r.addPoint(p);
			r.nodetype = BLACK;
			return ok;
		}

		if (r.nodetype == GRAY) {
			if (xcoord) {
				if (p.x > cx) // x coordinate
					ok = insert(p, r.right, !xcoord, cx + sx / 4, cy, sx / 2, sy, md - 1) && ok;
				else
					ok = insert(p, r.left, !xcoord, cx - sx / 4, cy, sx / 2, sy, md - 1) && ok;

			} else { // y coordinate
				if (p.y > cy)
					ok = insert(p, r.right, !xcoord, cx, cy + sy / 4, sx, sy / 2, md - 1) && ok;
				else
					ok = insert(p, r.left, !xcoord, cx, cy - sy / 4, sx, sy / 2, md - 1) && ok;
			}
			return ok;
		}

		if (r.nodetype == BLACK) {
			r.addPoint(p);
			if (r.points.size() > maxBucketSize) {
				ok = ok && md > 1;
				r.nodetype = GRAY;
				r.left = new PRkdbucketNode(WHITE);
				r.right = new PRkdbucketNode(WHITE);
				Vector pts = r.points;
				r.points = new Vector();
				for (int i = 0; i < pts.size(); i++) {
					DPoint pt = (DPoint) pts.elementAt(i);
					if (xcoord)
						if (pt.x > cx) {
							r.right.addPoint(pt);
							r.right.nodetype = BLACK;
						} else {
							r.left.addPoint(pt);
							r.left.nodetype = BLACK;
						}
					else if (pt.y > cy) {
						r.right.addPoint(pt);
						r.right.nodetype = BLACK;
					} else {
						r.left.addPoint(pt);
						r.left.nodetype = BLACK;
					}
				}
			}
		}
		return ok;
	}

}
