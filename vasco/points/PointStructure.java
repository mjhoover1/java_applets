package vasco.points;
/* $Id: PointStructure.java,v 1.3 2007/10/28 15:38:18 jagan Exp $ */
import java.awt.Choice;

import vasco.common.CommonConstants;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.SpatialStructure;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;

abstract public class PointStructure extends SpatialStructure implements CommonConstants {

  final double xf[] = {0, 0.5, 0, 0.5};
  final double yf[] = {0.5, 0.5, 0, 0};

  public PointStructure(DRectangle can, TopInterface ti, RebuildTree r) {
    super(can, ti, r);
  }

  public void reInit(Choice ops) {
    super.reInit(ops);
    availOps.addItem("Insert");
    availOps.addItem("Move");
    availOps.addItem("Delete");
    availOps.addItem("Overlap");
  }

  public boolean Insert(Drawable r) {
    return Insert((DPoint)r);
  }

  abstract public boolean Insert(DPoint p);


  /* -------------- common utilities ------------------- */

    public class XComparable implements vasco.common.Comparable {
	DPoint p;

	public XComparable(DPoint p) {
	    this.p = p;
	}
	public double sortBy() {
	    return p.x;
	}
    }
    public class YComparable implements vasco.common.Comparable {
	DPoint p;

	public YComparable(DPoint p) {
	    this.p = p;
	}
	public double sortBy() {
	    return p.y;
	}
    }

    /*
  double compareToX(DPoint a, DPoint b) {
    return a.x - b.x;
  }

  double compareToY(DPoint a, DPoint b) {
    return a.y - b.y;
  }
    */

  int OpQuad(int Q) {
    switch (Q) {
    case NW: return SE;
    case NE: return SW;
    case SW: return NE;
    case SE: return NW;
    }
    return -1;
  }
  
  int CQuad(int Q) {
    switch(Q) {
    case NW: return NE;
    case NE: return SE;
    case SE: return SW;
    case SW: return NW;
    }
    return -1;
  }

  int CCQuad(int Q) {
    switch(Q) {
    case NW: return SW;
    case NE: return NW;
    case SE: return NE;
    case SW: return SE;
    }
    return -1;
  }

}
