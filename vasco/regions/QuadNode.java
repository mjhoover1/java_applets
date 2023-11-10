package vasco.regions;

import vasco.common.*;

public class QuadNode{
 
  public int color; 
  public int type;
  public QuadNode parent;
  public QuadNode []child;
  public int size;
  public int x;
  public int y;
   
  QuadNode(){
    color = 0;
    type = 0;
    x = 0;
    y = 0;
    size = 0;
    parent = null;
    child = new QuadNode[4];
    child[0] = null;
    child[1] = null;
    child[2] = null;
    child[3] = null;
  }
  
  QuadNode(QuadNode parent, int x, int y, int size, int color, int type){
    this.color = color;
    this.type = type;
    this.x = x;
    this.y = y;
    this.size = size;
    this.parent = parent;
    child = new QuadNode[4];
    child[0] = null;
    child[1] = null;
    child[2] = null;
    child[3] = null;
  }

}














