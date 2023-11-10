package vasco.regions;
import vasco.common.*;

import java.awt.*;
import java.util.*;
  
public class RectangleElement extends  ConvertGenElement{
  Rectangle r;
  Color color;
  boolean filled;
  int width;

  RectangleElement(){
    super();
    r = null;
    color = null;
    filled = false;
    width = 1;
    mCopy = true;
  }

  RectangleElement(Rectangle rect, Color c, int w, boolean f, boolean mcp){
    super();
    r = rect;
    color = c;
    filled = f;
    width = w;
    mCopy = mcp;
  }
 
  public boolean makeCopy(){
    return mCopy;
  } 

  public void fillElementFirst(DrawingTarget g){
  } 
 
  public void fillElementNext(DrawingTarget g){
  }   
 
  
  public void drawElementFirst(DrawingTarget g){
    g.setColor(color);
    if (filled)
      g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
    else
      g.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);

  } 
 
  public void drawElementNext(DrawingTarget g){
  }
 
  public int pauseMode(){
    return 0;
  }

} 



