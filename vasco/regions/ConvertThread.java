package vasco.regions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import vasco.common.CanvasIface;
import vasco.common.DrawingTarget;
import vasco.common.QueryObject;
import vasco.common.VascoThread;

public class ConvertThread extends VascoThread {
	int mode;	
	private volatile boolean paused = false;
	private volatile int delay = 1000; // Delay between animation steps



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
        try {
            while (!interrupted()) {
                if (paused) {
                    synchronized (this) {
                        while (paused) {
                            wait();
                        }
                    }
                }

                if (v != null && ((ConvertVector) v).size() > 0) {
                    while (!interrupted() && !paused && getProgress() < ((ConvertVector) v).size()) {
                        if (!drawCurrentStep(off)) {
                            setProgress(getProgress() + 1);
                        } else {
                            pc.setPause();
                            break;
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                redraw();
                            }
                        });

                        System.out.println("Current delay: " + delay); // Debug statement
                        // Use Thread.sleep for handling delay
                        Thread.sleep(delay);
                    }

                    if (!paused) {
                        pc.reset();
                    }
                } else {
                    for (DrawingTarget element : off) {
                        element.redraw();
                    }
                    pc.reset();
                }
            }
        } catch (InterruptedException e) {
            // Handle interruption, potentially logging or resetting state
        	System.out.print(e);
        }
    }

    // Setter for the delay
	public synchronized void setDelay(int newDelay) {
	    delay = newDelay;
	    System.out.println("Updated delay to: " + delay); // Debug statement
	}

	
	// Method to pause the thread
	public void pauseThread() {
	    paused = true;
	}

	// Method to resume the thread
	public void resumeThread() {
	    synchronized (this) {
	        paused = false;
	        notify();
	    }
	}

	
//	@Override
//	public void run() {
//	    if (v != null && ((ConvertVector) v).size() > 0) {
//	        setProgress(0);
//	        final int delay = pc.getDelay(); // Delay in milliseconds between steps
//
//	        // ActionListener for the timer that will perform the animation steps
//	        ActionListener timerAction = new ActionListener() {
//	            public void actionPerformed(ActionEvent evt) {
//	                if (!drawCurrentStep()) {
//	                    if (!setProgress(getProgress() + 1)) {
//	                        ((Timer)evt.getSource()).stop(); // Stop the timer
//	                        pc.reset();
//	                    }
//	                } else {
//	                    pc.setPause();
//	                    ((Timer)evt.getSource()).stop(); // Stop the timer
//	                }
//	    	        SwingUtilities.invokeLater(new Runnable() {
//	    	            public void run() {
//	    	                redraw();
//	    	            }
//	    	        });
//	            }
//	        };
//
//	        // Create a new timer that calls the ActionListener at the specified delay
//	        Timer timer = new Timer(delay, timerAction);
//
//	        // Start the animation
//	        timer.start();
//	    } else {
//	        for (DrawingTarget element : off) {
//	            element.redraw();
//	        }
//	        pc.reset();
//	    }
//	}

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
