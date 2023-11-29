package vasco.points;

import java.awt.Color;
import java.util.Vector;

/* $Id: YellowPoints.java,v 1.1 2007/10/29 01:19:54 jagan Exp $ */
import vasco.common.DPoint;
import vasco.common.DrawingTarget;

public class YellowPoints extends ColorPoints {

	public YellowPoints(Vector v) {
		super(v);
	}

	public YellowPoints(DPoint p) {
		super(p);
	}

	@Override
	public void setColor(DrawingTarget g) {
		g.setColor(Color.yellow);
	}
}
