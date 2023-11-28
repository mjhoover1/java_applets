package vasco.regions;
import vasco.common.*;
import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;

public class MyCursor{
  CursorStyle currCursor;

  public MyCursor(){
    currCursor = null;
  }
 
  public void showInvalid(Rectangle rec, Color color, DrawingTarget dt){
    currCursor = new CursorStyle();
    currCursor.add(new ValidGridCursor(rec, color, false));
    move(dt);
  }

  public void setValid(boolean valid){
    currCursor.setValid(valid);
  }

  public boolean getValid(){
    return currCursor.getValid();
  }

  public boolean isDifferentCursor(CursorStyle cs){
    if ((currCursor == null) && (cs == null))
      return false;

    if ((currCursor == null) || (cs == null))
      return true;
 
    return (!currCursor.equals(cs));
  }

  public void move(DrawingTarget dt){
    if (currCursor != null)
      currCursor.display(dt);
  }

  public void move(DrawingTarget dt, CursorStyle cs){
    currCursor = cs;
    
    // display the new cursor
    if (currCursor != null){
      currCursor.display(dt); 
    }
 }

}
