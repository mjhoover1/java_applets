package vasco.lines;
/* $Id: LineStructure.java,v 1.3 2005/01/31 15:15:43 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;

public abstract class LineStructure extends SpatialStructure implements CommonConstants{
  PR prt;

  public LineStructure(DRectangle can, TopInterface p, RebuildTree r) {
    super(can, p, r);
  }

  public void reInit(JComboBox ops) {
    super.reInit(ops);
    ops.addItem("Insert");
    ops.addItem("Move");
    ops.addItem("Move vertex");
    ops.addItem("Move collection");
    ops.addItem("Rotate collection");
    ops.addItem("Delete");
    ops.addItem("Overlap");
    ops.addItem("Nearest");
    ops.addItem("Within");
   }

  public void Clear() {
    super.Clear();
    prt = new PR(wholeCanvas);
  }

  DLine remakeDLine(DLine l) {
    return new DLine(prt.Insert(l.p1), prt.Insert(l.p2));
  }

  void deletePoint(DPoint p) {
    prt.Delete(p);
  }

  public abstract boolean Insert(DLine r);

  public DPoint NearestPoint(DPoint p) {
    return prt.NearestPoint(p);
  }

  public boolean Insert(Drawable r) {
    return Insert((DLine)r);
  }

}



