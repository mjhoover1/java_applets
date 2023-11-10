/* $Id: CanvasIface.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;

public interface CanvasIface extends ItemListener {
  
  void drawBackground(DrawingTarget g, Color c);
  void drawBackground(DrawingTarget g);
  void drawGrid(DrawingTarget g);
  void drawContents(DrawingTarget g);
  int getDelay();
  int getSuccessMode();
  void reset();
    void setPause();
    void setProgressBar(int step);
    void initProgress(int step);
}
