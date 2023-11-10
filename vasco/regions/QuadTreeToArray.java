package vasco.regions;
import vasco.common.*;
import java.awt.*;
import java.util.*;
   
public class QuadTreeToArray{ 
  protected Grid grid;
  protected BinaryArray array;
  protected Node root;

  public QuadTreeToArray(Node root, Grid grid){
    this.root = root;
    this.grid = grid;
    array = new BinaryArray(this.grid);
  }

  public BinaryArray convert(ConvertVector sv){    
    sv.constructRegion(); 
    root.completeNode(0, 0, 512, 512);

    convert(root, sv);
    return array;
  }

  public void convert(Node n, ConvertVector sv){
    int x;
    ContainerElement container;
    Point sP, eP;
    ConvertElement currCE = null;
    ConvertElement oldCE = null;
    Stack s = new Stack();
    Node node;

    if (n == null)
      return;

    s.push(n);
    while(!s.isEmpty()){
      node = (Node)s.pop();
 
      container = new ContainerElement(null, grid, null);

      container.addElement(new BackgrdElement(grid, new Rectangle(0, 0, 512, 512),  
					      Colors.UNKNOWN));
      container.addElement(new QuadElement(grid, root, QuadElement.DRAW_NONLEAF));
      
      if (node.isLeaf){
	
	sP = grid.getGridCoor(new Point(node.x, node.y));
	eP = grid.getGridCoor(new Point(node.x + node.size - 1, node.y + node.size - 1));
	
	container.addElement(new PermenantElement(sv,
		  new GridElement(grid, new Rectangle(sP.x, sP.y, 
						      eP.x - sP.x + 1, eP.y - sP.y + 1))));

	container.addElement(new RectangleElement( 
			     new Rectangle(node.x, node.y, node.size, node.size),
			     Color.green, 1, false, false));
	 
	currCE = new ConvertElement(container, new Vector());
	sv.addElement(currCE);
	oldCE = currCE;
	
      }
      else{
	container.addElement(new PermenantElement(sv, null));
	container.addElement(new RectangleElement( 
			     new Rectangle(node.x, node.y, node.size, node.size),
			     Color.green, 1, false, false));
	
	currCE = new ConvertElement(container, new Vector());
	sv.addElement(currCE);
	oldCE = currCE;
	for(x = 3; x >=0; x--)
	  if(node.child[x] != null)
	    s.push(node.child[x]);	
      } 
    }
  }

}
