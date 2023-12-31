package vasco.regions;

import vasco.common.*;
import java.awt.*;
import java.util.*;
   
public class ValidGridCursor implements CursorStyleInterface{
  protected Color color;
  protected Rectangle rect;
  protected boolean isValid;

  public ValidGridCursor(Rectangle rect, Color color){
    this.rect = rect;
    this.color = color;
    isValid = true;
  }
  
  public ValidGridCursor(Rectangle rect, Color color, boolean isValid){
    this.rect = rect;
    this.color = color;
    this.isValid = isValid;
  }

  public void display(DrawingTarget dt){
    //dt.setColor(color);
    dt.directRect(color, rect.x, rect.y, rect.width, rect.height);
    if (!isValid){
      dt.directLine(color, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
      dt.directLine(color, rect.x + rect.width, rect.y, rect.x, rect.y + rect.height);
    } 
  }
 
  public boolean equals(Object obj){
    if (obj == null)
      return false;

    if (obj instanceof ValidGridCursor){
      ValidGridCursor vgc = (ValidGridCursor)obj;
      return (color.equals(vgc.color) && rect.equals(vgc.rect) && isValid == vgc.isValid);
    }

    return false;
  }

}





