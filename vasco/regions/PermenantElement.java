package vasco.regions;
import vasco.common.*;

import javax.swing.*; // import java.awt.*;
import java.util.*;
  
public class PermenantElement extends  ConvertGenElement{
  ConvertVector cv;
  ConvertGenElement elm;
  int index;

  public PermenantElement(ConvertVector cv, ConvertGenElement elm){
    this.cv = cv;
    this.elm = elm;
    index = cv.addPermenant(elm);
  }
 
  public void fillElementFirst(DrawingTarget g){
  }

  public void fillElementNext(DrawingTarget g){
  }

  public void drawElementFirst(DrawingTarget g){
    int x;
    Vector p = cv.getPermenant();
    ConvertGenElement ge;

    for(x = 0; x <= index; x++){
      ge = (ConvertGenElement)p.elementAt(x); 
      if (ge != null)
	ge.drawElementFirst(g);
    }
  }

  public void drawElementNext(DrawingTarget g){
  }

}

