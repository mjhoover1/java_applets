/* $Id: SearchThread.java,v 1.2 2002/09/25 20:55:06 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.util.*;

public class SearchThread extends VascoThread {
  int mode;

  public SearchThread(SearchVector vv, QueryObject qo, CanvasIface c, DrawingTarget[] g) {
    super(qo, g, vv, c);
  }

  public VascoThread makeCopy() {
    SearchThread nw = new SearchThread(v, qo, pc, off);
    return nw;
  }

  public synchronized boolean drawCurrentStep(DrawingTarget[] off) {
      pc.setProgressBar(getProgress());
    SVElement re = (SVElement)v.elementAt(getProgress());

    for (int i = 0; i < off.length; i++) {
    pc.drawBackground(off[i]);

    re.drawCyan(off[i]);
    for (int j = 0; j < getProgress(); j++)
      v.elementAt(j).ge.fillElementNext(off[i]);
    re.ge.fillElementFirst(off[i]);

    pc.drawGrid(off[i]);
    drawQueryObject(off[i]);
    pc.drawContents(off[i]);

    for (int j = 0; j < getProgress(); j++)
      v.elementAt(j).ge.drawElementNext(off[i]);
    re.ge.drawElementFirst(off[i]);

    off[i].redraw();
    }
    return (pc.getSuccessMode() != CommonConstants.RUNMODE_CONTINUOUS && 
	    (re.ge.pauseMode() == GenElement.SUCCESS || 
	     (re.ge.pauseMode() == GenElement.FAIL && pc.getSuccessMode() == CommonConstants.RUNMODE_OBJECT)));
  }

    public void run() {
	if (v != null && v.size() > 0) {

	    setProgress(0); 
	    do {
		if (drawCurrentStep()) {
		    pc.setPause();
		    suspend();
		} else try {
		    sleep(pc.getDelay());
		} catch(Exception e){};
	    } while(setProgress(getProgress() + 1));

	    for(int j = 0; j < off.length; j++) {

		pc.drawBackground(off[j]);

		for (int i = 0; i < v.size(); i++)
		    v.elementAt(i).ge.fillElementNext(off[j]);
		pc.drawContents(off[j]);
		drawQueryObject();
		for (int i = 0; i < v.size(); i++)
		    v.elementAt(i).ge.drawElementNext(off[j]);
	    }
	}
	for(int i = 0; i < off.length; i++) 
	    off[i].redraw();
	pc.reset();
    }
}
