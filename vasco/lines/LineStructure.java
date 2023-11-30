package vasco.lines;

// import java.awt.*;
import javax.swing.JComboBox;

/* $Id: LineStructure.java,v 1.3 2005/01/31 15:15:43 brabec Exp $ */
import vasco.common.CommonConstants;
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.PR;
import vasco.common.RebuildTree;
import vasco.common.SpatialStructure;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;

public abstract class LineStructure extends SpatialStructure implements CommonConstants {
	PR prt;

	public LineStructure(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
	}

	@Override
	public void reInit(JComboBox<String> ops) {
		super.reInit(ops);
		addItemIfNotExists(ops, "Insert");
		addItemIfNotExists(ops, "Move");
		addItemIfNotExists(ops, "Move vertex");
		addItemIfNotExists(ops, "Move edge");
		addItemIfNotExists(ops, "Delete");
		addItemIfNotExists(ops, "Overlap");
		addItemIfNotExists(ops, "Nearest");
		addItemIfNotExists(ops, "Within");
	}

	@Override
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

	@Override
	public boolean Insert(Drawable r) {
		return Insert((DLine) r);
	}

}
