/* $Id: SpatialStructure.java,v 1.3 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import vasco.drawable.*;
import java.awt.*;
import java.util.*;

public abstract class SpatialStructure implements CommonConstants {

	public TopInterface topInterface;
	public RebuildTree reb;
	public Choice availOps;
	public DRectangle wholeCanvas;
	private SwitchCursor cursorThread;

	public SpatialStructure(DRectangle w, TopInterface topInterface, RebuildTree r) {
		this.topInterface = topInterface;
		reb = r;
		wholeCanvas = w;
	}

	public void reInit(Choice ops) {
		Clear();
		topInterface.getPanel().removeAll();
		availOps = ops;
		// if (ops != null) {
		// ops.removeAll();
		// }
	}

	public abstract boolean orderDependent();

	class SwitchCursor extends Thread {
		boolean done;

		SwitchCursor() {
			done = false;
			// System.out.println("run beg");
		}

		public void run() {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			if (!done) {
				topInterface.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// System.out.println("cursor set");
			}
			// System.out.println("run end");
		}

		void finish() {
			done = true;
		}
	}

	public void MessageStart() {
		// Thread.dumpStack();
		// System.out.println("Start");
		cursorThread = new SwitchCursor();
		cursorThread.start();
	};

	public void Clear() {
	};

	public boolean Insert(Point p) {
		return false;
	}

	public boolean Delete(Point p) {
		return false;
	}

	public abstract boolean Insert(Drawable r);

	public abstract void Delete(DPoint p);

	public abstract void DeleteDirect(Drawable d);

	public void MessageEnd() {
		// Thread.dumpStack();
		// System.out.println("End");
		if (cursorThread != null)
			cursorThread.finish();
		topInterface.setCursor(Cursor.getDefaultCursor());
	};

	public abstract SearchVector Search(QueryObject r, int mode);

	public abstract SearchVector Nearest(QueryObject p);

	public abstract SearchVector Nearest(QueryObject p, double dist);

	public abstract Drawable NearestFirst(QueryObject p);

	public abstract Drawable[] NearestRange(QueryObject p, double dist);

	public abstract void drawContents(DrawingTarget g, Rectangle view);

	public void drawGrid(DrawingTarget g, int level) {
	};

	public abstract String getName();

	public String getCurrentOperation() {
		if (availOps == null)
			return null;
		return availOps.getSelectedItem();
	}

	public void drawableInOut(QueryObject s, Drawable cur, int mode, SearchVector v, Vector searchVector) {
		boolean isBlue = false;
		if (cur == null || s == null)
			return;

		if ((mode & SEARCHMODE_CONTAINS) != 0) {
			isBlue = isBlue || s.contains(cur);
		} // contains

		if ((mode & SEARCHMODE_ISCONTAINED) != 0) {
			isBlue = isBlue || s.isContained(cur);
		} // is contained

		if ((mode & SEARCHMODE_OVERLAPS) != 0) {
			isBlue = isBlue || s.overlaps(cur);
			// intersection includes some vertices
		}

		if ((mode & SEARCHMODE_CROSSES) != 0) {
			isBlue = isBlue || s.crosses(cur);
			// crosses but not vertices inside one another
		}

		if (isBlue)
			v.addElement(new SVElement(new DrawableIn(cur), searchVector));
		else
			v.addElement(new SVElement(new DrawableOut(cur), searchVector));
	}
}
