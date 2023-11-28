package vasco.regions;
import vasco.common.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.*;
   
public class TestCursor implements CursorStyleInterface{
  protected Color color;
  protected Rectangle rect;
   
  public TestCursor(Rectangle rect, Color color){
    this.rect = rect;
    this.color = color;
  }
  
  public void display(DrawingTarget dt){
    //dt.setColor(color);
    dt.directFillRect(color, rect.x, rect.y, rect.width, rect.height);
  }

  public boolean equals(Object obj){
    return false;
  }
}
