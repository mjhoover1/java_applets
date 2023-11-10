package vasco.rectangles;
/* $Id: RectangleStructure.java,v 1.2 2007/10/28 15:38:20 jagan Exp $ */
import vasco.common.*;
import java.awt.*;
import java.util.*;
import vasco.drawable.*;

public abstract class RectangleStructure extends SpatialStructure implements CommonConstants {


    public RectangleStructure(DRectangle can, TopInterface p, RebuildTree r) {
      super(can, p, r);
  }

    public void reInit(Choice ops) {
      super.reInit(ops);
    availOps.addItem("Insert");
    availOps.addItem("Move");
    availOps.addItem("Move vertex");
    availOps.addItem("Move edge");
    availOps.addItem("Delete");
    availOps.addItem("Overlap");
    availOps.addItem("Nearest");
    availOps.addItem("Within");
  }


  public boolean Insert(Drawable d) {
	return Insert((DRectangle)d);
    }

  abstract boolean Insert(DRectangle r);

  public boolean ReplaceRectangles(DRectangle OldRect, DRectangle NewRect) {
        return false;
  }
  
  public DRectangle EnclosingQuadBlock(DRectangle OldRect, boolean nextLevel) {
        return null;
  }
 
  public DRectangle expand(DRectangle rect) {
        return rect;
  }

  /* ------------------ common utilities ------------------- */


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
