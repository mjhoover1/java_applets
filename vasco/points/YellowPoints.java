package vasco.points;
/* $Id: YellowPoints.java,v 1.1 2007/10/29 01:19:54 jagan Exp $ */
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.util.*;

public class YellowPoints extends ColorPoints {

	public YellowPoints(Vector v) {
    super(v);
  }

	public YellowPoints(DPoint p) {
    super(p);
  }

	public void setColor(DrawingTarget g) {
    g.setColor(Color.yellow);
  }
}
