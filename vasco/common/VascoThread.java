/* $Id: VascoThread.java,v 1.2 2002/09/25 20:55:07 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import java.util.*;

public abstract class VascoThread extends Thread {

    protected DrawingTarget[] off;	
    protected SearchVector v;
    protected int currentStep;
    protected int oldStep;
    protected CanvasIface pc;
    protected QueryObject qo;

    public VascoThread(QueryObject qo, DrawingTarget[] dt, SearchVector vv, CanvasIface c) {
	this.qo = qo;
	off = dt;
	v = vv;
	oldStep = -1;
	currentStep = -1;
	pc = c;
	//    if (pc != null && v != null)
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
	    for (int i = 0; i < off.length; i++)
		fillQueryObject(off[i]);
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
	for (int i = 0; i < off.length; i++)
	    drawQueryObject(off[i]);
    }
    public void drawQueryObject(DrawingTarget dt) {
	dt.setColor(Color.orange);
	qo.draw(dt);
    }

    public void fillQueryObject() {
	for (int i = 0; i < off.length; i++)
	    fillQueryObject(off[i]);
    }; // override if necessary
    public void fillQueryObject(DrawingTarget dt) {}; // override if necessary

    public int getProgress() {
	return currentStep;
    }

    public synchronized boolean setProgress(int step) {
	if (step >= 0 && step < v.size()) {
	    currentStep = step;
	    redraw();
	    return true;
	} 
	return false;
    };

}

