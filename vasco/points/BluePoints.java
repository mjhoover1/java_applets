package vasco.points;
/* $Id: BluePoints.java,v 1.1 2007/10/29 01:19:54 jagan Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;

public class BluePoints extends ColorPoints {

	public BluePoints(Vector v) {
    super(v);
  }

	public BluePoints(DPoint p) {
    super(p);
  }

	public void setColor(DrawingTarget g) {
    g.setColor(Color.blue);
  }
}
