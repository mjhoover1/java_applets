package vasco.regions;

import vasco.common.*;
import javax.swing.*; // import java.awt.*;
import java.util.*;

public class Node{  
  public static final int NW = 0;
  public static final int NE = 1;
  public static final int SW = 2;
  public static final int SE = 3;
  public static final int N = 4;
  public static final int S = 5;
  public static final int W = 6;
  public static final int E = 7;

  public static final int WHITE = 0;
  public static final int BLACK = 1;
  public static final int GRAY = 2;

  public int level;
  public int sonOf;
  public int color; 
  public int x;
  public int y;
  public int size;
  public int type; 
  public int mark;
  public boolean isLeaf;
  public Node parent;
  public Node []child;
 
  public static int opquad(int q){
    if (NE == q) return SW;
    if (NW == q) return SE;
    if (SE == q) return NW;
    if (SW == q) return NE;
  
    return -1;
  }

  public static int cquad(int q){
    if (NE == q) return SE;
    if (NW == q) return NE;
    if (SE == q) return SW;
    if (SW == q) return NW;
 
    return -1;
  }

  public static int ccquad(int q){
    if (NE == q) return NW;
    if (NW == q) return SW;
    if (SE == q) return NE;
    if (SW == q) return SE;
 
    return -1;
  }

  public static int quad(int e1, int e2){

    if (N == e1){
      if ( E == e2) return  NE;
      if ( W == e2) return  NW;
    }

    if ( S == e1){
      if ( E == e2) return  SE;
      if ( W == e2) return  SW;
    }

    if ( W == e1){
      if ( N == e2) return  NW;
      if ( S == e2) return  SW;
    }

    if ( E == e1){
      if ( N == e2) return  NE;
      if ( S == e2) return  SE;
    }
 
    return -1;
  }
 
  public static int commonedge(int q1, int q2){

    if (( NW == q1)&&( NE == q2)) return  E;
    if (( NW == q2)&&( NE == q1)) return  E;

    if (( NE == q1)&&( SE == q2)) return  S;
    if (( NE == q2)&&( SE == q1)) return  S;

    if (( SE == q1)&&( SW == q2)) return  W;
    if (( SE == q2)&&( SW == q1)) return  W;
    
    if (( SW == q1)&&( NW == q2)) return  N;
    if (( SW == q2)&&( NW == q1)) return  N;

    return -1;

  }

  public static int reflect(int e, int q){

    if ( NE == q){
      if ( E == e) return  NW;
      if ( W == e) return  NW;
      if ( N == e) return  SE;
      if ( S == e) return  SE;
      return -1;
    }

    if ( NW == q){
      if ( E == e) return  NE;
      if ( W == e) return  NE;
      if ( N == e) return  SW;
      if ( S == e) return  SW;
      return -1;
    }

    if ( SE == q){
      if ( E == e) return  SW;
      if ( W == e) return  SW;
      if ( N == e) return  NE;
      if ( S == e) return  NE;
      return -1;
    }
      
    if ( SW == q){
      if ( E == e) return  SE;
      if ( W == e) return  SE;
      if ( N == e) return  NW;
      if ( S == e) return  NW;
      return -1;
    }
  
    return -1;
  }

  public static int cedge(int e){
    
    if ( N == e) return  E;
    if ( E == e) return  S;
    if ( S == e) return  W;
    if ( W == e) return  N;

    return -1;
  }

  public static int ccedge(int e){
    
    if ( N == e) return  W;
    if ( E == e) return  N;
    if ( S == e) return  E;
    if ( W == e) return  S;

    return -1;
  }

  public static int opedge(int e){

    if ( N == e) return  S;
    if ( E == e) return  W;
    if ( S == e) return  N;
    if ( W == e) return  E;
 
    return -1;
  }


  public static boolean adj(int e, int q){

    if ( N == e){
      if ( NW == q) return true;
      if ( NE == q) return true;
      if ( SW == q) return false;
      if ( SE == q) return false;
      return false;
    }

    if ( E == e){
      if ( NW == q) return false;
      if ( NE == q) return true;
      if ( SW == q) return false;
      if ( SE == q) return true;
      return false;
    }

    if ( S == e){
      if ( NW == q) return false;
      if ( NE == q) return false;
      if ( SW == q) return true;
      if ( SE == q) return true;
      return false;
    }
     
    if ( W == e){
      if ( NW == q) return true;
      if ( NE == q) return false;
      if ( SW == q) return true;
      if ( SE == q) return false;
      return false;
    } 

    return false;
  }
  
  Node(){
    parent = null;
    child = null;
  }

  Node(Node n){
     
    level = n.level;
    mark = n.mark;
    sonOf = n.sonOf;
    color = n.color;
    x = n.x;
    y = n.y;
    type = n.type;
    size = n.size;
    isLeaf = n.isLeaf;
    
    parent = null;
    child = new Node[4];
    child[0] = null; child[1] = null;
    child[2] = null; child[3] = null;
  }

  Node(Node p, int xx, int yy, int s, int l, int sOf, int c, boolean leaf){
    level = l;
    mark = 0;
    x = xx;
    type = 0;
    y = yy; 
    size = s;
    sonOf = sOf;
    color = c;
    isLeaf = leaf;
    parent = p;
    child = new Node[4];
    child[0] = null; child[1] = null;
    child[2] = null; child[3] = null;
  }

  public void setType(int c){
    if (c == Node.GRAY)
      isLeaf = false;
    else{
      isLeaf = true;
      color = c;
    }
  }

  public Node getCopy(){
    Node n = new Node(this);

    for(int i = 0; i < 4; i++){
      if (child[i] != null){
	n.child[i] = child[i].getCopy();
	n.child[i].parent = n;
      } 
    }
 
    return n;
  }

  public void Clear(){
    
    if (parent != null){
      parent.child[sonOf] = null;
    }
    for(int i = 0; i < 4; i++){
      if (child[i] != null)
	child[i].Clear();
    }
  }
 
  public static boolean insert(Node n, Point p, int nC, int cellSize){
    int x;
    boolean found;
    Node node = n;

    if (node == null)
      return false;
 
    if (!node.inside(p))
      return false;

    while(node != null){
      if (node.isLeaf){
	if (node.color == nC)
	  return false;
	if (node.size > cellSize){
	  node.split();
	  found = false;
	  for (x = 0; (x < 4 && !found); x++){
	    if (node.child[x] != null && (node.child[x]).inside(p))
	      found = true;
	  }

	  if (!found)
	    return false;
	  
	  node = node.child[x-1];
	}   
	else{
	  node.color = nC; 
	  Node.merge(node.parent);
	  return true;          
	}    
      }
      else{
	found = false;
	for (x = 0; (x < 4 && !found); x++){
	  if (node.child[x] != null && (node.child[x]).inside(p))
	    found = true;
	}

	if (!found)
	  return false;
	 
	node = node.child[x-1];
       
      }

    }

    return false;
  }

  public static boolean merge(Node node){
    boolean result = false;
    if (node == null)
      return false; 
    result = node.merge();     
    if (result)
      Node.merge(node.parent); 
    return result;
  } 
 
  public static void clearMark(Node n){
    if (n == null)
      return;

    n.mark = 0;
    if (!n.isLeaf){
      for(int i = 0; i < 4; i++){
	if (n.child[i] != null)
	  Node.clearMark(n.child[i]);
      }       
    }
  }

  public boolean isWhite(){
    if ((isLeaf) && (color == Node.WHITE))
      return true;

    return false;
  }

  public boolean isBlack(){
    if ((isLeaf) && (color == Node.BLACK))
      return true; 

    return false;
  }

  public boolean isGray(){
    if ((!isLeaf))
      return true;
 
    return false;
  }


  public void display(DrawingTarget g, int mode){
    Stack s = new Stack();
    Node n;
    int ss;

    /* draw the outer rectangle */
    g.setColor(Color.black); 
    g.drawRect(0, 0, 511, 511);

    if (mode == 0){
      g.setColor(Color.black);
      s.push(this);
      while(!s.empty()){
	n = (Node)s.pop();

	if (n.isLeaf){
	  switch(n.color){
	  case 0:
	  break;
	  case 1:
	    g.setColor(Colors.GRID_CELL);
	    g.fillRect(n.x, n.y, n.size, n.size);
	  break;
	  case -1:
	    g.setColor(Color.yellow);
	    g.fillRect(n.x, n.y, n.size, n.size);
	    break;  
	  } 
	
	
	  if (n.type != 0){
	    g.setColor(Color.blue);
	    ss = (int)(n.size / 2);
	    g.drawString("" + n.type, n.x + ss, y + ss);
	  }
	  
	} // if leaf
	else{
	  for(int i = 0; i < 4; i++){
	    if (n.child[i] != null)
	      s.push(n.child[i]);
	  } 
	} // non leaf
      } 
    } // if mode
    else if (mode == 1){
      g.setColor(Color.black);
      if (!isLeaf)
	s.push(this);
      while(!s.empty()){
	n = (Node)s.pop();
	ss = (int)(n.size / 2);

	g.drawLine(n.x, n.y + ss, n.x + n.size, n.y + ss);
	g.drawLine(n.x + ss, n.y, n.x + ss, n.y + n.size);
	 
	for(int i = 0; i < 4; i++){
	  if (n.child[i] != null && !n.child[i].isLeaf)
	    s.push(n.child[i]);
	} 
      }
    }
  }

  public void completeNode(int xx, int yy, int siz, int l){
    size = siz;
    x = xx;
    y = yy;
    level = l;
    int s = (int)(size / 2);
    if (child[0] != null)
      child[0].completeNode(x, y, s, level + 1);
    if (child[1] != null)
      child[1].completeNode(x + s, y, s, level + 1);
    if (child[2] != null)
      child[2].completeNode(x, y + s, s, level + 1); 
    if (child[3] != null)
      child[3].completeNode(x + s, y+s, s, level + 1);
  } 

  public static Node create(Node root, int t, int c){
    Node p;
     
    p = new Node();
    p.mark = 0; 
    p.sonOf = t;
    p.type = 0;
    if (root != null)
      root.child[t] = p;
    p.parent = root;
    if (c == GRAY)
      p.isLeaf = false;
    else{
      p.isLeaf = true;
      p.color = c;
    }
    p.child = new Node[4];
    p.child[0] = null; p.child[1] = null;
    p.child[2] = null; p.child[3] = null;    

    return p;
  }  
 
  public void split(){
    isLeaf = false;
    int s = (int)(size / 2);
    child[0] = new Node(this, x, y, s, level+1, 0, color, true);
    child[1] = new Node(this, x+s, y, s, level+1, 1, color, true);
    child[2] = new Node(this, x, y+s, s, level+1, 2, color, true);
    child[3] = new Node(this, x+s, y+s, s, level+1, 3, color, true); 
  } 
 
  public boolean merge(){
    if (child[0] == null)
      return false;

    if (!((child[0].isLeaf) && (child[1].isLeaf) && (child[2].isLeaf) && 
	  (child[3].isLeaf))) 
      return false;

    int c = child[0].color;
    if (!((c == child[1].color) && (c == child[2].color) 
	  && (c == child[3].color) ))
      return false;
 
    color = child[0].color;
    isLeaf = true;
    child[0] = null; child[1] = null;
    child[2] = null; child[3] = null;
    
    return true; 
  } 

  public boolean inside(Point p){
    if ((p.x >= x) && (p.x < x + size) && 
	(p.y >= y) && (p.y < y + size))
      return true;
  
    return false;
  }

}














