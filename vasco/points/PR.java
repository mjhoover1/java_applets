package vasco.points;

/* $Id: PR.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
import java.awt.Choice;

import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class PR extends GenPRbuck {

	public PR(DRectangle can, int md, TopInterface p, RebuildTree r) {
		super(can, 1, md, p, r);
	}

	public void reInit(Choice ao) {
		super.reInit(ao);
	}

	public String getName() {
		return "PR Quadtree";
	}

	public boolean orderDependent() {
		return false;
	}
}
