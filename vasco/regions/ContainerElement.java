package vasco.regions;
import vasco.common.*;

import java.awt.*;
import java.util.*;
   
public class ContainerElement implements  GenElement{
  Vector list;
  Node node;
  Grid grid; 
 
  public ContainerElement(){
    list = null;
    node = null;
    grid = null;
  }

  public ContainerElement(Node n, Grid g, Vector l){
    ConvertGenElement cge;

    node = n;
    grid = g;
    list = new Vector();
    if (l != null){
      for(int x = 0; x < l.size(); x++){
	cge = (ConvertGenElement)l.elementAt(x); 
	if (cge.makeCopy())
	  list.addElement(cge);
      } 
    }

  }

  public Vector getList(){
    return list;
  }

  public void addElement(ConvertGenElement e){
    list.addElement(e);
  }

  public void fillElementFirst(DrawingTarget g){
  } 
 
  public void fillElementNext(DrawingTarget g){
  }   
  
  
  public void drawElementFirst(DrawingTarget g){
    ConvertGenElement e;
 
    if (node != null){
      node.completeNode(0, 0, 512, 0);
      node.display(g, 0); /* display leafs */
      grid.display(g);
      node.display(g, 1); /* display non-leafs */
    }

    /* display the elements in the list */
    for(int i = 0; i < list.size(); i++){
      e = (ConvertGenElement)list.elementAt(i);
      e.drawElementFirst(g);
    } 
  } 

  public void drawElementNext(DrawingTarget g){
  }
 
  public int pauseMode(){
    return 0;
  }

  
}
