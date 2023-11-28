/* $Id: NearestThread.java,v 1.2 2002/09/25 20:55:05 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.util.*;
import vasco.drawable.*;

public class NearestThread extends VascoThread {
	// --- use PCanvas for: 1) drawBackground, 2) drawGrid, 3) drawContents, 4)
	// waitTime, 5) resetButtons
	double dist;
	int sr, sg, sb;
	double dr, dg, db;
	boolean blend;

	public NearestThread(SearchVector vv, QueryObject p, CanvasIface c, double dist, DrawingTarget[] g) {
		super(p, g, vv, c);
		this.dist = dist;
		blend = false;
	}

	public NearestThread(SearchVector vv, QueryObject p, CanvasIface c, double dist, DrawingTarget[] g, int sr, int sg,
			int sb, double dr, double dg, double db) {
		this(vv, p, c, dist, g);
		this.sr = sr;
		this.sg = sg;
		this.sb = sb;
		this.dr = dr;
		this.dg = dg;
		this.db = db;
		blend = true;
	}

	public NearestThread(SearchVector vv, QueryObject p, CanvasIface c, double dist, DrawingTarget[] g, int r1, int g1,
			int b1, int r2, int g2, int b2) {
		this(vv, p, c, dist, g);

		int counter = 0;
		for (int i = 0; i < vv.size(); i++)
			if (((NNElement) (vv.elementAt(i))).isElement())
				counter++;

		// System.out.println("Orig Counter: " + counter);
		sr = r1;
		sg = g1;
		sb = b1;
		if (counter > 0) {
			dr = (r2 - r1) / ((double) counter);
			dg = (g2 - g1) / ((double) counter);
			db = (b2 - b1) / ((double) counter);
		} else {
			dr = dg = db = 0;
		}
		blend = true;
	}

	public VascoThread makeCopy() {
		NearestThread nw;

		if (blend) {
			nw = new NearestThread(v, qo, pc, dist, off, sr, sg, sb, dr, dg, db);
		} else {
			nw = new NearestThread(v, qo, pc, dist, off);
		}
		return nw;
	}

	public void drawQueryObject(DrawingTarget off) {
		qo.drawBuffer(Color.gray, off, dist);
		super.drawQueryObject(off);
	}

	public void fillQueryObject(DrawingTarget off) {
		qo.fillBuffer(Color.gray, Color.white, off, dist);
	}

	synchronized protected boolean drawCurrentStep(DrawingTarget off) {
		DrawingTarget[] dt = new DrawingTarget[1];
		dt[0] = off;
		return drawCurrentStep(dt);
	}

	public synchronized boolean drawCurrentStep(DrawingTarget[] off) {
		pc.setProgressBar(getProgress());
		NNElement re = (NNElement) v.elementAt(getProgress());

		for (int i = 0; i < off.length; i++) {
			pc.drawBackground(off[i], Color.lightGray);
			qo.fillBuffer(Color.yellow, Color.lightGray, off[i], re.dist);
			for (int j = 0; j < getProgress(); j++)
				v.elementAt(j).ge.fillElementNext(off[i]);
			re.fillQueue(off[i]);
			re.ge.fillElementFirst(off[i]);

			pc.drawGrid(off[i]);
			qo.drawBuffer(Color.yellow, off[i], re.dist);
			drawQueryObject(off[i]);
			pc.drawContents(off[i]);

			re.drawQueue(off[i]);

			int counter = 0;
			for (int j = 0; j < getProgress(); j++) {
				NNElement ne = (NNElement) v.elementAt(j);
				if (blend) {
					// System.out.println((sr + counter * dr) + " " + (sg + counter * dg) + " " +
					// (sb + counter * db));
					off[i].setColor(
							new Color((int) (sr + counter * dr), (int) (sg + counter * dg), (int) (sb + counter * db)));
					if (ne.isElement())
						counter++;
					// System.out.println("onFly counter" + counter);
				} else
					off[i].setColor(Color.blue);
				ne.ge.drawElementNext(off[i]);
			}
			re.ge.drawElementFirst(off[i]);

			off[i].redraw();
		}
		return (pc.getSuccessMode() != CommonConstants.RUNMODE_CONTINUOUS && (re.ge.pauseMode() == GenElement.SUCCESS
				|| (re.ge.pauseMode() == GenElement.FAIL && pc.getSuccessMode() == CommonConstants.RUNMODE_OBJECT)));
	}

	public void run() {
		if (v != null && v.size() > 0) {
			setProgress(0);
			do {
				/*
				 * rectangle (PointIn or YellowBlock) && vector of green points or cyan rects
				 * with green borders
				 */
				if (drawCurrentStep()) {
					pc.setPause();
					suspend();
				} else
					try {
						sleep(pc.getDelay());
					} catch (Exception e) {
					}
				;
			} while (setProgress(getProgress() + 1));

			for (int i = 0; i < off.length; i++) {

				pc.drawBackground(off[i], Color.lightGray);

				qo.fillBuffer(Color.gray, Color.lightGray, off[i], dist);

				for (int j = 0; j < v.size(); j++)
					v.elementAt(j).ge.fillElementNext(off[i]);

				qo.drawBuffer(Color.gray, off[i], dist);
				pc.drawContents(off[i]);
				drawQueryObject(off[i]);
				int counter = 0;
				for (int j = 0; j < v.size(); j++) {
					NNElement ne = (NNElement) v.elementAt(j);
					if (blend) {
						off[i].setColor(new Color((int) (sr + counter * dr), (int) (sg + counter * dg),
								(int) (sb + counter * db)));
						if (ne.isElement())
							counter++;
					} else
						off[i].setColor(Color.blue);
					ne.ge.drawElementNext(off[i]);
				}
				off[i].redraw();
			}
		}
		pc.reset();
	}
}
