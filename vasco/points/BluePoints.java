package vasco.points;

import java.awt.Color;
import java.util.Vector;

/* $Id: BluePoints.java,v 1.1 2007/10/29 01:19:54 jagan Exp $ */
import vasco.common.DPoint;
import vasco.common.DrawingTarget;

public class BluePoints extends ColorPoints {

	public BluePoints(Vector v) {
		super(v);
	}

	public BluePoints(DPoint p) {
		super(p);
	}

	@Override
	public void setColor(DrawingTarget g) {
		g.setColor(Color.blue);
	}
}
