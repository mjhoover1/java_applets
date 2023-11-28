package vasco.regions;
import vasco.common.*;

import javax.swing.*; // import java.awt.*;
import java.util.*;
  
public class DialogElement extends  ConvertGenElement{  
  String text;
  StructureBox si;

  public DialogElement(StructureBox si, String text){
    this.si = si;
    this.text = text;
  }

  public void fillElementFirst(DrawingTarget g){
  }

  public void fillElementNext(DrawingTarget g){
  }

  public void drawElementFirst(DrawingTarget g){
    if (si != null)
      si.setText(text); 
  }

  public void drawElementNext(DrawingTarget g){
  }

}



