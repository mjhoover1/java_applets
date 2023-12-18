/* $Id: VascoThread.java,v 1.2 2002/09/25 20:55:07 brabec Exp $ */
package vasco.common;

import java.awt.Color;

import javax.swing.SwingUtilities;

public abstract class VascoThread extends Thread {

	protected DrawingTarget[] off;
	protected SearchVector v;
	protected int currentStep;
	protected int oldStep;
	protected CanvasIface pc;
	protected QueryObject qo;
	
//    private volatile boolean paused = false;

	public VascoThread(QueryObject qo, DrawingTarget[] dt, SearchVector vv, CanvasIface c) {
		this.qo = qo;
		off = dt;
		v = vv;
		oldStep = -1;
		currentStep = -1;
		pc = c;
		// if (pc != null && v != null)
		pc.initProgress(v.size());
	}

	public void refill(DrawingTarget overview) {
		refill();
		if (v == null || currentStep < 0 || currentStep >= v.size()) {
			fillQueryObject(overview);
		}
	}

	public void redraw(DrawingTarget dt) {
		DrawingTarget[] dta = new DrawingTarget[1];
		dta[0] = dt;
		redraw();
		if (v == null || currentStep < 0 || currentStep >= v.size()) {
			drawQueryObject(dt);
		} else {
			drawCurrentStep(dta);
		}
	}

	public void refill() {
		if (v == null || currentStep < 0 || currentStep >= v.size()) {
			for (DrawingTarget element : off)
				fillQueryObject(element);
		}
	}

	public void redraw() {
		if (v == null || currentStep < 0 || currentStep >= v.size()) {
			drawQueryObject();
		} else {
			drawCurrentStep();
		}
	}

	public boolean drawCurrentStep() {
		return drawCurrentStep(off);
	}

	public abstract boolean drawCurrentStep(DrawingTarget[] dt);

	public abstract VascoThread makeCopy();

	public void drawQueryObject() {
		for (DrawingTarget element : off)
			drawQueryObject(element);
	}

	public void drawQueryObject(DrawingTarget dt) {
		dt.setColor(Color.orange);
		qo.draw(dt);
	}

	public void fillQueryObject() {
		for (DrawingTarget element : off)
			fillQueryObject(element);
	} // override if necessary

	public void fillQueryObject(DrawingTarget dt) {
	} // override if necessary

	public int getProgress() {
		return currentStep;
	}
	
	public synchronized boolean setProgress(int step) {
	    if (step >= 0 && step < v.size()) {
	        currentStep = step;
	        return true;
	    }
	    return false;
	}
	
    // Method to pause the thread
//    public void pauseThread() {
//        paused = true;
//    }
//
//    // Method to resume the thread
//    public void resumeThread() {
//        synchronized (this) {
//            paused = false;
//            notify();
//        }
//    }
//    
//    @Override
//    public void run() {
//        while (!interrupted()) {
//            synchronized (this) {
//                while (paused) {
//                    try {
//                        wait();
//                    } catch (InterruptedException e) {
//                        // Handle interruption
//                    }
//                }
//            }
//
//            // Existing run logic...
//        }
//    }

//	public synchronized boolean setProgress(int step) {
//		if (step >= 0 && step < v.size()) {
//			currentStep = step;
//			redraw();
//			return true;
//		}
//		return false;
//	}

}
