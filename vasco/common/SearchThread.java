/* $Id: SearchThread.java,v 1.2 2002/09/25 20:55:06 brabec Exp $ */
package vasco.common;

import java.awt.Color;

public class SearchThread extends VascoThread {
	int mode;

	public SearchThread(SearchVector vv, QueryObject qo, CanvasIface c, DrawingTarget[] g) {
		super(qo, g, vv, c);
	}

	@Override
	public VascoThread makeCopy() {
		SearchThread nw = new SearchThread(v, qo, pc, off);
		return nw;
	}

	@Override
	public synchronized boolean drawCurrentStep(DrawingTarget[] off) {
		pc.setProgressBar(getProgress()); 
		SVElement re = (SVElement) v.elementAt(getProgress());

//		if (setProgress(getProgress() + 1)) {
			for (DrawingTarget element : off) {
				pc.drawBackground(element);
	
				if (setProgress(getProgress() + 1)) {
					re.drawCyan(element);
				}
				for (int j = 0; j < getProgress(); j++)
					v.elementAt(j).ge.fillElementNext(element);
				if (setProgress(getProgress() + 1)) {
					re.ge.fillElementFirst(element);
				}
				
				pc.drawGrid(element);
				drawQueryObject(element);
				pc.drawContents(element);
	
				for (int j = 0; j < getProgress(); j++)
					v.elementAt(j).ge.drawElementNext(element);
				re.ge.drawElementFirst(element);
	
				element.redraw();
			}
//		}
		return (pc.getSuccessMode() != CommonConstants.RUNMODE_CONTINUOUS && (re.ge.pauseMode() == GenElement.SUCCESS
				|| (re.ge.pauseMode() == GenElement.FAIL && pc.getSuccessMode() == CommonConstants.RUNMODE_OBJECT)));
	}
	
	@Override
	public void run() {
	    try {
	        if (v != null && v.size() > 0) {
	        	for (DrawingTarget element : off) {
	        		if (element instanceof DrawingCanvas) {
	        			((DrawingCanvas) element).clearColoredRectangles(Color.magenta);
	        		}
		        }
	        	
	            setProgress(0);

	            while(setProgress(getProgress() + 1)) {
	                if (drawCurrentStep()) {
	                    pc.setPause();
	                    // Use higher-level concurrency utilities or Thread interruption logic
	                } else {
	                    Thread.sleep(pc.getDelay());
	                }
	            } 
	            
	            pc.setProgressBar(getProgress() + 1); // Added to have progress bar end
	            
	            for (DrawingTarget element : off) {
	                pc.drawBackground(element);
					for (int i = 0; i < v.size(); i++)
						v.elementAt(i).ge.fillElementNext(element);
	                pc.drawContents(element);
	                drawQueryObject();
					for (int i = 0; i < v.size(); i++)
						v.elementAt(i).ge.drawElementNext(element);
	            }
	        }
	        for (DrawingTarget element : off) {
	            element.redraw();
	        }
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt(); // Restore interrupted status
	        // Handle interruption
	    } finally {
	        pc.reset();
	    }
	}

//	@Override
//	public void run() {
//		if (v != null && v.size() > 0) {
//
//			setProgress(0);
//			do {
//				if (drawCurrentStep()) {
//					pc.setPause();
//					suspend();
//				} else
//					try {
//						sleep(pc.getDelay());
//					} catch (Exception e) {
//					}
//			} while (setProgress(getProgress() + 1));
//
//			for (DrawingTarget element : off) {
//
//				pc.drawBackground(element);
//
//				for (int i = 0; i < v.size(); i++)
//					v.elementAt(i).ge.fillElementNext(element);
//				pc.drawContents(element);
//				drawQueryObject();
//				for (int i = 0; i < v.size(); i++)
//					v.elementAt(i).ge.drawElementNext(element);
//			}
//		}
//		for (DrawingTarget element : off)
//			element.redraw();
//		pc.reset();
//	}
}
