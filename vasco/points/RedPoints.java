package vasco.points;

/* $Id: RedPoints.java,v 1.2 2007/10/28 15:38:18 jagan Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;

public class RedPoints extends ColorPoints {

	public RedPoints(Vector v) {
		super(v);
	}

	public RedPoints(DPoint p) {
		super(p);
	}

	public void setColor(DrawingTarget g) {
		g.setColor(Color.red);
	}
}
