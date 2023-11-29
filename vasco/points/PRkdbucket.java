package vasco.points;
/* $Id: PRkdbucket.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */

// import java.awt.Choice;

import javax.swing.JComboBox;

import vasco.common.Bucket;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class PRkdbucket extends GenPRkdbuck {

	public PRkdbucket(DRectangle can, int b, int md, TopInterface p, RebuildTree r) {
		super(can, b, md, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
		new Bucket(topInterface, "Bucket Capacity", this);
	}

	@Override
	public String getName() {
		return "Bucket PR k-d Tree";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

}
