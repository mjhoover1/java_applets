package vasco.regions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import vasco.common.CanvasIface;
import vasco.common.DrawingTarget;
import vasco.common.QueryObject;
import vasco.common.VascoThread;

public class ConvertThread extends VascoThread {
	int mode;

	public ConvertThread(ConvertVector vv, QueryObject qo, CanvasIface c, DrawingTarget[] g) {
		super(qo, g, vv, c);
		setProgress(0);
	}

	@Override
	public VascoThread makeCopy() {
		ConvertThread nw = new ConvertThread((ConvertVector) v, qo, pc, off);
		return nw;
	}

	@Override
	public synchronized boolean drawCurrentStep(DrawingTarget[] off) {
		int i, x;
		pc.setProgressBar(getProgress());
		ConvertElement re = (ConvertElement) (((ConvertVector) v).elementAt(getProgress()));

		/* construct the quadtree */

		int currStep = getProgress();

		for (i = 0; i < off.length; i++) {
			pc.drawBackground(off[i]);

			((ConvertVector) v).elementAt(getProgress()).ge.drawElementFirst(off[i]);

			off[i].redraw();
		}
		return false;
	}
	
	@Override
	public void run() {
	    if (v != null && ((ConvertVector) v).size() > 0) {
	        setProgress(0);
	        final int delay = pc.getDelay(); // Delay in milliseconds between steps

	        // ActionListener for the timer that will perform the animation steps
	        ActionListener timerAction = new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                if (!drawCurrentStep()) {
	                    if (!setProgress(getProgress() + 1)) {
	                        ((Timer)evt.getSource()).stop(); // Stop the timer
	                        pc.reset();
	                    }
	                } else {
	                    pc.setPause();
	                    ((Timer)evt.getSource()).stop(); // Stop the timer
	                }
	            }
	        };

	        // Create a new timer that calls the ActionListener at the specified delay
	        Timer timer = new Timer(delay, timerAction);

	        // Start the animation
	        timer.start();
	    } else {
	        for (DrawingTarget element : off) {
	            element.redraw();
	        }
	        pc.reset();
	    }
	}

//	@Override
//	public void run() {
//	    if (v != null && ((ConvertVector) v).size() > 0) {
//	        setProgress(0);
//	        final Timer timer = new Timer(pc.getDelay(), null);
//
//	        ActionListener listener = new ActionListener() {
//	            public void actionPerformed(ActionEvent evt) {
//	                if (!drawCurrentStep()) {
//	                    if (!setProgress(getProgress() + 1)) {
//	                        timer.stop();
//	                        pc.reset();
//	                    }
//	                } else {
//	                    pc.setPause();
//	                    timer.stop();
//	                }
//	            }
//	        };
//
//	        timer.addActionListener(listener);
//	        timer.start();
//	    } else {
//	        for (DrawingTarget element : off) {
//	            element.redraw();
//	        }
//	        pc.reset();
//	    }
//	}
//	
//	@Override
//	public void run() {
//		if (v != null && ((ConvertVector) v).size() > 0) {
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
//			/*
//			 * for(int j = 0; j < off.length; j++) { pc.drawBackground(off[j]);
//			 *
//			 * for (int i = 0; i < ((ConvertVector)v).size(); i++)
//			 * ((ConvertVector)v).elementAt(i).ge.fillElementNext(off[j]);
//			 * //pc.drawContents(off[j]);
//			 *
//			 * for (int i = 0; i < ((ConvertVector)v).size(); i++)
//			 * ((ConvertVector)v).elementAt(i).ge.drawElementNext(off[j]); }
//			 */
//		}
//
//		for (DrawingTarget element : off)
//			element.redraw();
//
//		pc.reset();
//	}

	@Override
	public void redraw(DrawingTarget dt) {
		DrawingTarget[] dta = new DrawingTarget[1];
		dta[0] = dt;
		redraw();
		if (v == null || currentStep < 0 || currentStep >= ((ConvertVector) v).size()) {
			// drawQueryObject(dt);
		} else {
			drawCurrentStep(dta);
		}
	}

	@Override
	public void redraw() {
		if (v == null || currentStep < 0 || currentStep >= ((ConvertVector) v).size()) {
			// drawQueryObject();
		} else {
			drawCurrentStep();
		}
	}

}
