package vasco.points;
/* $Id: PRkd.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */

// import java.awt.Choice;

import javax.swing.JComboBox;

import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class PRkd extends GenPRkdbuck {
	public PRkd(DRectangle can, int md, TopInterface p, RebuildTree r) {
		super(can, 1, md, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ao) {
		super.reInit(ao);
	}

	@Override
	public String getName() {
		return "PR k-d Tree";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

}
