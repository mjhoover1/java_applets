package vasco.rectangles;
/* $Id: RectangleRTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import java.awt.*;

public class RectangleRTree extends RectangleStructure {
  RTree rt;

  public RectangleRTree(DRectangle can, TopInterface p, RebuildTree r) {
    super(can, p, r);
    rt = new RTree(can, 3, 6, p, r);
  }

  public void reInit(Choice ops) {
    super.reInit(ops);
    rt.reInit(ops);
  }

    public boolean orderDependent() {
        return rt.orderDependent();
    }

  public boolean Insert(DRectangle r) {
    return rt.Insert(r);
  }

  public void Delete(DPoint p) {
      rt.Delete(p);
  }

    public void DeleteDirect(Drawable p) {
	rt.DeleteDirect(p);
    }

  public void MessageStart() {
      //      super.MessageStart();
    rt.MessageStart();
  };

  public void Clear() {
    super.Clear();
    rt.Clear();
  }

  public void MessageEnd() {
      //      super.MessageEnd();
    rt.MessageEnd();
  };

  public SearchVector Search(QueryObject r, int mode) {
    return rt.Search(r, mode);
  }

  public SearchVector Nearest(QueryObject p) {
    return rt.Nearest(p);
  }

  public SearchVector Nearest(QueryObject p, double dist) {
    return rt.Nearest(p, dist);
  }

  public Drawable NearestFirst(QueryObject p) {
    return rt.NearestFirst(p);
  }

    public Drawable[] NearestRange(QueryObject p, double dist) {
	return rt.NearestRange(p, dist);
    }

  public void drawContents(DrawingTarget g, Rectangle view) {
    rt.drawContents(g, view);
  }

  public void drawGrid(DrawingTarget g, int level) {
    rt.drawGrid(g, level);
  };

  public String getName() {
    return rt.getName();
  }

}


