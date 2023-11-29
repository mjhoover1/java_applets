package vasco.points;

import java.awt.Color;
import java.util.Vector;

/* $Id: GreenPoints.java,v 1.2 2007/10/28 15:38:17 jagan Exp $ */
import vasco.common.DPoint;
import vasco.common.DrawingTarget;

public class GreenPoints extends ColorPoints {

	public GreenPoints(Vector v) {
		super(v);
	}

	public GreenPoints(DPoint p) {
		super(p);
	}

	@Override
	public void setColor(DrawingTarget g) {
		g.setColor(Color.green);
	}
}
