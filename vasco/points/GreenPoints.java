package vasco.points;
/* $Id: GreenPoints.java,v 1.2 2007/10/28 15:38:17 jagan Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.util.*;

public class GreenPoints extends ColorPoints {

	public GreenPoints(Vector v) {
    super(v);
  }

	public GreenPoints(DPoint p) {
    super(p);
  }

	public void setColor(DrawingTarget g) {
    g.setColor(Color.green);
  }
}

