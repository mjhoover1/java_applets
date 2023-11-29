package vasco.points;

import java.awt.Color;
import java.util.Vector;

/* $Id: RedPoints.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
import vasco.common.DPoint;
import vasco.common.DrawingTarget;

public class RedPoints extends ColorPoints {

	public RedPoints(Vector v) {
		super(v);
	}

	public RedPoints(DPoint p) {
		super(p);
	}

	@Override
	public void setColor(DrawingTarget g) {
		g.setColor(Color.red);
	}
}
