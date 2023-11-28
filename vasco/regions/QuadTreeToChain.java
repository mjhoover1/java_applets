package vasco.regions;

import vasco.common.*;
import javax.swing.*; // import java.awt.*;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
    
class MarkElement{
  Node p, q;
  public MarkElement(){
    p = null;
    q = null;
  }

  public MarkElement(Node pp, Node qq){
    p = pp;
    q = qq; 
  }

}

public class QuadTreeToChain{ 

  protected Grid grid;
  protected Node root;
  protected ChainCode code;
  protected StructureBox si;

  static ConvertElement oldConvertElm = null;
  static ConvertElement currConvertElm = null;

  QuadTreeToChain(Node root, Grid grid, StructureBox si){
    this.root = root;
    this.grid = grid;
    code = null;
    this.si = si;
  }  
     
  public ChainCode convert(ConvertVector sv){
    
    code = new ChainCode();
 
    /* whole area is just one region */
    sv.constructRegion();
	
    QuadTreeToChain.convert(root, grid, code.chain, sv, si);

    return code;
  }
 
     
  public static void convert(Node node, Grid g, Vector code, ConvertVector sv, StructureBox si){   
    int level, dir;
    RefInt lp, lq;
    RefNode p, q;
    Vector markList;
    ContainerElement container = null;

    level = g.res;
    markList = new Vector();

    //Node.clearMark(node);
  
    p = new RefNode(null);
    q = new RefNode(null);
    lp = new RefInt(0);
    lq = new RefInt(0);
      
    p.node = node;
    lp.value = level;
     
    while(p.node.isGray()){
      if (sv != null){ 
	container = new ContainerElement(node, g, null);
	container.addElement(new DialogElement(si, " "));
	container.addElement( 
		 new RectangleElement( 
		   new Rectangle(p.node.x, p.node.y, p.node.size, p.node.size),
		   Color.green, 1, false, false));      
	currConvertElm = new ConvertElement(container, new Vector());
	sv.addElement(currConvertElm);
	oldConvertElm = currConvertElm;
      } 

      lp.value = lp.value - 1;
      if (!p.node.child[Node.SW].isWhite())
	dir = Node.SW; 
      else if (!p.node.child[Node.SE].isWhite())
	dir = Node.SE;
      else if (!p.node.child[Node.NW].isWhite())
	dir = Node.NW;
      else
	dir = Node.NE;

      p.node = p.node.child[dir];
    }
  
    lq.value = lp.value;
       
    qtVertexEdgeNeighbor2(p, Node.S, Node.SW, q, lq);
    nextLink(node, g, code, sv, si, markList, p, lp, q, lq, Node.S);
 
  } 
  
  public static boolean marked(Vector markList, Node p, Node q){
    boolean found;
    MarkElement elm;
 
    found = false;
    for(int x = 0; x < markList.size(); x++){
      elm = (MarkElement)markList.elementAt(x);
      if ( ((elm.p == p) && (elm.q == q)) ||
	   ((elm.p == q) && (elm.q == p)) )
	return true;
    }
    return false;
  }
 
  public static void mark(Vector markList, Node p, Node q){
    markList.addElement(new MarkElement(p, q));
  }

 
  public static void addToLinkList(Vector code, int e){
    code.addElement(new Integer(e));
  } 
 
  public static void nextLink(Node node, Grid g, Vector code, ConvertVector sv, StructureBox si,
			      Vector markList,
			      RefNode p, RefInt lp, RefNode q,
			      RefInt lq, int e){
    int min, i;
    RefInt lx, ly;
    RefNode x, y;
    ContainerElement container = null;

    lx = new RefInt(0);
    ly = new RefInt(0);
    x = new RefNode(null);
    y = new RefNode(null);

    if (marked(markList, p.node, q.node))
      return;

    mark(markList, p.node, q.node);

    if (lp.value < lq.value)
      min = lp.value;
    else
      min = lq.value;
        
    for(i = 0; i < Math.pow(2, min); i++){
      addToLinkList(code, ChainCode.link(e));   
    }

    if (sv != null){
      LineElement le = null;
      Vector oldList = null;
      

      if (sv.size() > 0) /* remember the old boundaries */
	oldList = ((ContainerElement) (oldConvertElm.ge)).getList();

      container = new ContainerElement(node, g, oldList);
      // update the dialog box
      container.addElement(new DialogElement(si, ChainCode.print(code)));

      Rectangle pRect = new Rectangle(p.node.x, p.node.y, p.node.size, p.node.size);
      Rectangle qRect = new Rectangle(q.node.x, q.node.y, q.node.size, q.node.size);
      Rectangle intersect = pRect.intersection(qRect);
      le = new LineElement(g, 
	       new Point(intersect.x, intersect.y), 
	       new Point(intersect.x + intersect.width, intersect.y+intersect.height),
	       ChainCode.link(e), Color.blue, 
	       null, 
	       (int)Math.pow(2, min), true);
       
      container.addElement(le);
      container.addElement(
		new RectangleElement(
		   new Rectangle(p.node.x, p.node.y, p.node.size, p.node.size),
		   Color.green, 1, false, false));
      container.addElement( 
		new RectangleElement(
		   new Rectangle(q.node.x, q.node.y, q.node.size, q.node.size),
		   Color.green, 1, false, false));

      currConvertElm = new ConvertElement(container, new Vector()); 
      sv.addElement(currConvertElm);
      oldConvertElm = currConvertElm;
    }
 
    if (aligned(p.node, lp.value, q.node, lq.value, Node.cedge(e))){
      lx.value = lp.value;
      qtVertexEdgeNeighbor2(p, Node.cedge(e), Node.quad(Node.cedge(e), e),
			    x, lx);
      if (x.node.isWhite()){
	nextLink(node, g, code, sv, si, markList, p, lp, x, lx, Node.cedge(e));
      } /* WHITE */
      else{ /* !WHITE */
	ly.value = lq.value;
	qtVertexEdgeNeighbor2(q, Node.cedge(e), 
			      Node.quad(Node.cedge(e), Node.opedge(e)),
			      y, ly);

	if (y.node.isBlack()){
	  nextLink(node, g, code,  sv, si, markList, y, ly, q, lq, Node.ccedge(e));
	} /* if BALCK */
	else{ /* !BALCK */
	  nextLink(node, g, code,  sv, si, markList, x, lx, y, ly, e);
	}
      } /* !WHITE */
    } /* aligned */ 
    else if (lp.value > lq.value){
      lx.value = lq.value;
      qtVertexEdgeNeighbor2(q, Node.cedge(e), 
			    Node.quad(Node.cedge(e), Node.opedge(e)),
			    x, lx);
      if (x.node.isWhite())
	nextLink(node, g, code,  sv, si, markList,  p, lp, x, lx, e);
      else
	nextLink(node, g, code,  sv, si, markList, x, lx, q, lq, Node.ccedge(e));
    } /* if lp > lq */
    else{
      lx.value = lp.value;
      qtVertexEdgeNeighbor2(p, Node.cedge(e), Node.quad(Node.cedge(e), e),
			    x, lx);

      if (x.node.isWhite())
	nextLink(node, g, code,  sv, si, markList, p, lp, x, lx, Node.cedge(e));
      else 
	nextLink(node, g, code,  sv, si, markList, x, lx, q, lq, e);
    } /* last else */ 
 
  }

  public static  boolean aligned(Node p, int lp, Node q, int lq, int i){
    int j;
    Node r;
    int tmp;
 
    if (p == null) 
      return true;
    if (q ==  null) 
      return true;
    if (lp == lq) 
      return true;
    
    if (lp > lq){
      tmp = lp - lq;
      r = q;
    }
    else{
      tmp = lq - lp;
      r = p;
    }

    for(j = 0; j<tmp; j++){
      if (!Node.adj(i, r.sonOf)) 
	return false;
      else  
	r = r.parent;
    }
    return true;
  } 
 
  public static void qtVertexEdgeNeighbor2(RefNode p, int e, int v, 
					   RefNode q, RefInt l){
 
    qtGteqEdgeNeighbor2(p.node, e, q, l); 
 
    if (q.node == null)
      return;

    while (q.node.isGray()){ 
      q.node = q.node.child[Node.reflect(e, v)];
      l.value--;
    }
  }
  
  public static void qtGteqEdgeNeighbor2(Node p, int i, RefNode q, 
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

