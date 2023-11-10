package vasco.regions;
 
import vasco.common.*;
import java.awt.*;
import java.util.*;
  
public class QuadTreeToRaster{ 

  protected Grid grid;
  protected Node root;
  protected Raster raster;
 
  QuadTreeToRaster(Node root, Grid grid){
    this.root = root;
    this.grid = grid;  
    raster = new Raster(grid);
  }  
  
  public Raster convert(ConvertVector sv){
    int level = grid.res;
    int diameter, y;
    RefInt width = new RefInt(0);
    RefNode p = null; 
      
    sv.constructRegion();
 
    root.completeNode(0, 0, 512, 512);
     
    diameter = (int)Math.pow(2, level);
    for(y = 0; y < diameter; y++){
      p = new RefNode(root);
      width.value = diameter;
      find2DBlock(p, 0, diameter, y, diameter, width);   
      outRow(p.node, y, log2(width.value), sv);
    } 

    return new Raster(grid);
  }

  protected int log2(int v){
    int count;

    count = 0;
    while (v != 0){
      v = (int)(v / 2);
      if (v != 0)
	count ++;
    }

    return count;
  }
   
  protected void outRow(Node p, int row, int ll, ConvertVector sv){
    int width;
    RefInt l = new RefInt(ll);
    RefNode q = new RefNode(null);
    ContainerElement container  = null;
    RefInt newWidth = new RefInt();
    Point sCol, eCol;

    if (p == null)
      return;

      /* output run */ 
    container = new ContainerElement(root, grid, null);
    container.addElement(new BackgrdElement(grid, new Rectangle(0, 0, 512, 512),  
					    Colors.UNKNOWN));
    container.addElement(new QuadElement(grid, root, QuadElement.DRAW_NONLEAF));
    sCol = grid.getGridCoor(new Point(p.x, p.y));
    eCol = grid.getGridCoor(new Point(p.x + p.size - 1, p.y));
    container.addElement(new GridElement(grid, row, eCol.x + 1));
    container.addElement(
	  new RectangleElement( 
	   new Rectangle(p.x, p.y, p.size, p.size),
	   Color.green, 1, false, false));
	
    container.addElement(
	  new RectangleElement( 
	   new Rectangle(p.x + 1, 
			 row * grid.getCellSize() + 1, 
			 p.size - 2,  
			 grid.getCellSize() - 2),
	   Color.blue, 1, false, false)); 
  
    sv.addElement(new ConvertElement(container, new Vector())); 
 
    width = (int)Math.pow(2, l.value);
    do{       
 
      qtGteqEdgeNeighbor2(p, Node.E, q, l);
      width = (int)Math.pow(2, l.value);
    
      
      if ((q.node != null) && (q.node.isGray())){
      
	newWidth = new RefInt(width);
	find2DBlock(q, 0, width, row, row + width - (row % width),
		    newWidth);
	width = newWidth.value;
	l.value = log2(newWidth.value);
      }

  
      if (q.node != null){
	//q.node.type = width;

	container = new ContainerElement(root, grid, null);
	container.addElement(new BackgrdElement(grid, new Rectangle(0, 0, 512, 512),  
						Colors.UNKNOWN));
	container.addElement(new QuadElement(grid, root, QuadElement.DRAW_NONLEAF));
	sCol = grid.getGridCoor(new Point(q.node.x, q.node.y));
	eCol = grid.getGridCoor(new Point(q.node.x + q.node.size - 1, q.node.y));
	container.addElement(new GridElement(grid, row, eCol.x + 1));
	container.addElement(
	  new RectangleElement( 
	   new Rectangle(q.node.x, q.node.y, q.node.size, q.node.size),
	   Color.green, 1, false, false));
	
	container.addElement( 
	   new RectangleElement( 
	   new Rectangle(q.node.x + 1, 
			 row * grid.getCellSize() + 1, 
			 q.node.size - 2,  
			 grid.getCellSize() - 2),
	   Color.blue, 1, false, false)); 
	
	sv.addElement(new ConvertElement(container, new Vector()));  

      }

      p = q.node; 
  
    } while(p != null);

  }
 

  protected void find2DBlock(RefNode p, int x, int xfar, 
			     int y, int yfar, RefInt w){
    int []XF = {1, 0, 1, 0};
    int []YF = {1, 1, 0, 0};
    int q;
 
    while((p.node != null) && (p.node.isGray())){ 
      w.value = (int)(w.value / 2);
      q = getQuadrant(x, xfar - w.value, y, yfar - w.value);
      xfar = xfar - XF[q] * w.value;
      yfar = yfar - YF[q] * w.value;
      p.node = p.node.child[q];
    } 

  }

  protected int getQuadrant(int x, int xcenter, int y, int ycenter){
    if (x < xcenter){
      if (y < ycenter)
	return Node.NW;
      else
	return Node.SW;
    }
    else if (y < ycenter)
      return Node.NE;
 
    return Node.SE;
  }

  public void qtGteqEdgeNeighbor2(Node p, int i, RefNode q, 
					 RefInt l){
    int tmp;
 
    tmp = l.value;
 
    l.value++;
 
    if ((p.parent != null) && (Node.adj(i,p.sonOf)))
      qtGteqEdgeNeighbor2(p.parent, i, q, l);
    else
      q.node = p.parent;
 
    if (q.node != null){
      if (q.node.isGray()){
	q.node = q.node.child[Node.reflect(i, p.sonOf)];
	l.value--;
      }
    }


    if (q.node == null)
      l.value = tmp+1; 
   
  }
 
}

