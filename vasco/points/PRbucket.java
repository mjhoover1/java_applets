package vasco.points;
/* $Id: PRbucket.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */

// import java.awt.Choice;

import javax.swing.JComboBox;

import vasco.common.Bucket;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class PRbucket extends GenPRbuck {

	public PRbucket(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
		super(can, b, md, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		new Bucket(topInterface, "Bucket Capacity", this);
	}

	@Override
	public String getName() {
		return "Bucket PR Quadtree";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

}
