package vasco.regions;
import vasco.common.*;
import java.awt.*;
import java.util.*;
 
public class ArrayToQuadTree{ 
 
  protected static final int []XF;
  protected static final int []YF;

  protected Grid grid;
  protected BinaryArray array;
  protected Node root;
 
  static{
    XF = new int[4];
    YF = new int[4];
    XF[0] = 1; XF[1] = 0; XF[2] = 1; XF[3] = 0;
    YF[0] = 1; YF[1] = 1; YF[2] = 0; YF[3] = 0; 
  }
 
  public ArrayToQuadTree(Grid grid){
    this.grid = grid;
    array = new BinaryArray(grid);
  } 
      
  protected Node construct(int level, int x, int y, 
			 MyRefInt c, ConvertVector sv, Node dRoot){
    int d, i; 
    Node []p = new Node[4];
    MyRefInt []t = new MyRefInt[4];
    t[0] = new MyRefInt();t[1] = new MyRefInt();
    t[2] = new MyRefInt();t[3] = new MyRefInt();
    Node q; 
  
    if (level == 0){ /* at a pixel */
      c.setValue(array.getColor(x-1, y-1)); 
     
      Point g = new Point(x-1, y-1);
      Point scr = grid.grdToScr(g); 
      if (sv.inActiveRegion(scr.x, scr.y)){ 
	Node.insert(dRoot, scr, array.getColor(x-1, y-1), grid.cellSize);
	sv.addElement(new ConvertElement(  
			  new ColorGrids(grid, new Point(x - 1, y - 1), c.getValue(),
			  dRoot.getCopy()), 
	   new Vector()));   
      }   
      else{   
	Node.insert(dRoot, scr, array.getColor(x-1, y-1), grid.cellSize);
	sv.addElement(new ConvertElement(  
		      new ColorGrids(grid, new Point(x - 1, y - 1), c.getValue(),
			  dRoot), 
	   new Vector()));   
      }

      return null;   
    }    
    else{      
      level = level - 1; 
      d = (int)(Math.pow(2, level));
      for(i = 0; i < 4; i++){  
	p[i] = construct(level, x - XF[i] * d, y - YF[i] * d, t[i], sv, dRoot);
      }  
       
      int v = t[Node.NW].getValue();
      if ((v != Node.GRAY) && (v == t[Node.NE].getValue()) &&
	  (v == t[Node.SW].getValue()) && (v == t[Node.SE].getValue())){
	c.setValue(v); 
	return null;
      }
      else{ 
	q = Node.create(null, -1, Node.GRAY);
	for (i = 0; i < 4; i++){
	  if (p[i] == null){
	    p[i] = Node.create(q, i, t[i].getValue());
	  } 
	  else{
	    q.child[i] = p[i];
	    p[i].parent = q;
	    p[i].sonOf = i;
	  }
	} 
	c.setValue(Node.GRAY);
	return q; 
      } 
      
    } 
  }

  public Node convert(ConvertVector res){ 
    Node dRoot = null;
    MyRefInt c = new MyRefInt();
    int level = array.getLevel();
    int size = (int)(Math.pow(2, level));  
 
    root = null;

    dRoot = Node.create(null, -1, -1);
    dRoot.completeNode(0, 0, 512, 0);
  
    root = construct(level, size, size, c, res, dRoot);
      
    if (root == null)   
      root = Node.create(null, -1, c.getValue());
  
    root.completeNode(0, 0, 512, 0);

    return root;
  }
  
}

