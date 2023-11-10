package vasco.regions;
import vasco.common.*;
import java.awt.*;
import java.util.*;

public class RasterToQuadTree{ 
  
  protected Grid grid;
  protected Raster raster;
  protected Node root;

  public RasterToQuadTree(Grid g){
    this.grid = g;
    raster = new Raster(grid);
  } 
  
  protected void oddRow(int row, int w, int[] q, Node r, ConvertVector sv){
    Node tmp = null;
    Point g;
    Point scr;
  
    tmp = r;
    tmp.setType(q[0]);  

    /* for display */ 
    g = new Point(0, row); 
    scr = grid.grdToScr(g); 
    if (sv.inActiveRegion(scr.x, scr.y)){
      root.completeNode(0, 0, 512, 0);
      sv.addElement(new ConvertElement(new ColorGrids(grid, g, q[0], 
				       root.getCopy()), new Vector()));
    }   
    else{   
      root.completeNode(0, 0, 512, 0);
      sv.addElement(new ConvertElement(new ColorGrids(grid, g, q[0], 
				       root), new Vector()));
    } 
 

    for(int i = 1; i < w; i++){ 
      tmp = addEdgeNeighbor(tmp, Node.E, -1, sv); //, Node.WHITE, )
      tmp.setType(q[i]);

      /* for display */ 
      g = new Point(i, row);
      scr = grid.grdToScr(g); 
      if (sv.inActiveRegion(scr.x, scr.y)){
	root.completeNode(0, 0, 512, 0);
	sv.addElement(new ConvertElement(  
	   new ColorGrids(grid, g, q[i], 
			  root.getCopy()), 
	   new Vector()));
      }   
      else{   
	root.completeNode(0, 0, 512, 0);
	sv.addElement(new ConvertElement(  
	   new ColorGrids(grid, g, q[i], 
			  root), 
	   new Vector()));
      }
 
    }   
  } 
 
  protected Node evenRow(int i, int w, boolean lastRow, int []q, RefNode first, 
			 ConvertVector sv){
    int j;
    
    Node r;
    Node p = first.getValue();
 
    if (lastRow == false){ /* remember the first node of the next row */
      first.setValue(addEdgeNeighbor(p, Node.S, -1, sv));
    }  
    else{
      i = raster.getColCount() - 1;
    } 

    for (j = 0; j < w - 1;  j++){
      r = addEdgeNeighbor(p, Node.E, -1, sv); //, Node.WHITE, )
      p.setType(q[j]);
      if (((j+1) % 2) == 0) 
	merge(i + 1, j + 1, p.parent);
 
      /* for display */  
      Point g = new Point(j, i);
      Point scr = grid.grdToScr(g); 
      if (sv.inActiveRegion(scr.x, scr.y)){
	root.completeNode(0, 0, 512, 0);
	sv.addElement(new ConvertElement(  
	   new ColorGrids(grid, g, q[j], 
			  root.getCopy()), 
	   new Vector()));
      }   
      else{   
	root.completeNode(0, 0, 512, 0);
	sv.addElement(new ConvertElement(  
	   new ColorGrids(grid, g, q[j], 
			  root),  
	   new Vector()));
      }

      p = r;
    }
      
    p.setType(q[w - 1]);   
    merge(i+1, w, p.parent); //raster.getColCount(),
 
    /* for display */   
    Point g = new Point(w-1, i);
    Point scr = grid.grdToScr(g); 
    if (sv.inActiveRegion(scr.x, scr.y)){
      root.completeNode(0, 0, 512, 0);
      sv.addElement(new ConvertElement(  
	 new ColorGrids(grid, g, q[w-1], root.getCopy()), new Vector()));
    }   
    else{    
      root.completeNode(0, 0, 512, 0); 
      sv.addElement(new ConvertElement(  
         new ColorGrids(grid, g, q[w-1], root), new Vector()));
    }

    return first.getValue();
  }
  
  protected void merge(int i, int j, Node p){ 
    int t;
    
    t = p.child[0].color;
    while (((i % 2) == 0) && ((j % 2) == 0) && (p.child[0].isLeaf) &&
	   (t == p.child[1].color) && (p.child[1].isLeaf) &&
	   (t == p.child[2].color) && (p.child[2].isLeaf) &&
	   (t == p.child[3].color) && (p.child[3].isLeaf)){
      i = (int)(i / 2); 
      j = (int)(j / 2);
      p.setType(p.child[0].color);
      /* TO DO: */
      for(int x = 0; x < 4; x++)
	p.child[x] = null;

      p = p.parent;;
    }  

  }

  protected Node addEdgeNeighbor(Node q, int e, int c, ConvertVector sv){
    Node p;
    int typeQ;
    int i;

    if (q.parent == null){ /* nearest common ancestor does not exists */
      p = Node.create(null, -1, Node.GRAY);
      root = p; 
      q.parent = p;
      typeQ = Node.quad(Node.ccedge(e), Node.opedge(e));
      p.child[typeQ] = q;
      q.sonOf = typeQ;  
      /* create three sons */ 
      p.child[Node.opquad(q.sonOf)] = 
	Node.create(p, Node.opquad(typeQ), c);
      p.child[Node.opquad(Node.reflect(e, typeQ))] =
	Node.create(p, Node.opquad(Node.reflect(e, typeQ)), c );
      p.child[Node.reflect(e, typeQ)] = 
	(Node.create(p, Node.reflect(e, typeQ), c));

      return p.child[Node.reflect(e, typeQ)];
    }  
    else if(Node.adj(e, q.sonOf))
	p = addEdgeNeighbor(q.parent, e, c, sv);
    else
	p = q.parent;
     
    if (p.child[Node.reflect(e, q.sonOf)] == null){
      p.setType(Node.GRAY);
      for (i = 0; i < 4; i++){
	p.child[i] = Node.create(p, i, c);
      }  
    }
   
  
    return p.child[Node.reflect(e, q.sonOf)]; 
  }
   
  public Node convert(ConvertVector sv){
    int i, r; 
    int []q = null;
    int []rowP = null;
    int []tmp = null;
    int width;
    RefNode first;
    RefNode tmpNode = new RefNode();

    root = null;

    width = raster.getColCount();
    q = raster.getRow(0);
    first = new RefNode(Node.create(null, -1, q[0])); // ,q[0])
    root = Node.create(null, -1, q[0]);
    oddRow(0, width, q, first.getValue(), sv); 
    i = 1;   
    tmpNode.node = addEdgeNeighbor(first.getValue(), Node.S, -1, sv);
    first.node = evenRow(i, width, (raster.getRow(2) == null), 
			 raster.getRow(1),
			 tmpNode, sv);

    while((rowP = raster.getRow(i+1)) != null){
      oddRow(i+1, width, rowP, first.getValue(), sv);
      i = i + 2;
      tmpNode.node = addEdgeNeighbor(first.getValue(), Node.S, -1, sv);
      first.setValue(evenRow(i, width, (raster.getRow(i+1) == null),
        raster.getRow(i), 
        tmpNode, sv));
    } 
 
    root.completeNode(0, 0, 512, 0);

    return root;
  }

}

 

