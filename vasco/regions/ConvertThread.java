package vasco.regions;
 
import vasco.common.*;
 
import javax.swing.*; // import java.awt.*;
import java.util.*;

public class ConvertThread extends VascoThread {
  int mode;
 
  public ConvertThread(ConvertVector vv, QueryObject qo, CanvasIface c, 
		      DrawingTarget[] g) {
    super(qo, g, vv, c);
    setProgress(0);
  }

  public VascoThread makeCopy() { 
    ConvertThread nw = new ConvertThread((ConvertVector)v, qo, pc, off);
    return nw;
  }
   
  public synchronized boolean drawCurrentStep(DrawingTarget[] off) {
    int i, x; 
    pc.setProgressBar(getProgress());
    ConvertElement re = 
      (ConvertElement)(((ConvertVector)v).elementAt(getProgress()));
 
    /* construct the quadtree */

    int currStep = getProgress(); 

    for (i = 0; i < off.length; i++) {
      pc.drawBackground(off[i]);
        
      ((ConvertVector)v).elementAt(getProgress()).ge.drawElementFirst(off[i]);

      off[i].redraw();
    } 
    return false;
  }
   
  public void run() {
    if (v != null && ((ConvertVector)v).size() > 0) {
      
      setProgress(0); 
      do {
	if (drawCurrentStep()) {
	  pc.setPause();
	  suspend();
	} else try {
	  sleep(pc.getDelay());
	} catch(Exception e){};
      } while(setProgress(getProgress() + 1));
       
      /*
      for(int j = 0; j < off.length; j++) {	
	pc.drawBackground(off[j]);
	
	for (int i = 0; i < ((ConvertVector)v).size(); i++)
	  ((ConvertVector)v).elementAt(i).ge.fillElementNext(off[j]);
	//pc.drawContents(off[j]);
	
	for (int i = 0; i < ((ConvertVector)v).size(); i++)
	  ((ConvertVector)v).elementAt(i).ge.drawElementNext(off[j]);
      } 
      */
    }

    for(int i = 0; i < off.length; i++) 
      off[i].redraw();

    pc.reset();
  }

 
  public void redraw(DrawingTarget dt) {
    DrawingTarget[] dta = new DrawingTarget[1];
    dta[0] = dt;
    redraw();
    if (v == null || currentStep < 0 || currentStep >= ((ConvertVector)v).size()) {
      //drawQueryObject(dt);
    } else {
      drawCurrentStep(dta);
    }
  }
 
  public void redraw() { 
    if (v == null || currentStep < 0 || currentStep >= ((ConvertVector)v).size()) {
      //drawQueryObject();
    } else {
      drawCurrentStep();
    }
  }

}



