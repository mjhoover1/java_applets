package vasco.regions;

import vasco.common.*;
import vasco.drawable.*;

import javax.swing.*; // import java.awt.*;
import java.util.*;
  
public class ArrayStructure extends RegionStructure {

  BinaryArray array;
 
  public ArrayStructure(GenericCanvas rc, DRectangle can, DrawingTarget dt, TopInterface p, RebuildTree r, Grid g) {
    super(rc, can, dt, p, r, g, ARRAY_MODE); 
    array = new BinaryArray(g);
  }   
      
  /************** RegionStructure **************/
  public int search(Point p){
    return super.search(p);
  }

  public int search(Node node, Point p){
    return super.search(node, p);
  } 
 
  public void reInit(JComboBox ao) {
    super.reInit(ao);
    ao.addItem("To quadtree");
    ao.addItem("To raster");
    ao.addItem("To chain");
  }  
 
  public void Clear() {
    super.Clear();
    root = new Node(null, 0, 0, 512, 0, -1, 0, true);
    /* clear the grid */
    grid.Clear();
  }
 
  public String getName(){
    return "Array"; 
  } 

  public boolean orderDependent() {
    return false;
  } 
 
  public boolean Insert(Point p){
    if (!array.setColor(p.x, p.y, 1))
      return false;  
     
    return true;
  }
  
  public boolean Insert(Rectangle r){
    for (int x = 0; x < r.width; x++)
      for(int y = 0; y < r.height; y++)
	Insert(new Point(r.x + x, r.y + y));
    return true;
  }

  public boolean Delete(Point p){
    if (!array.setColor(p.x, p.y, 0))
      return false; 

    return true;
  }  

  public boolean Delete(Rectangle r){
    for (int x = 0; x < r.width; x++)
      for(int y = 0; y < r.height; y++)
	Delete(new Point(r.x + x, r.y + y));
    return true;
  }
  
  public boolean Insert(Drawable r){
    return true;
  }
 
  public void Delete(DPoint p){
  }

  public void DeleteDirect(Drawable d){
  }
 
  public SearchVector Search(QueryObject r, int mode){
    return null;
  }
 
  public SearchVector Nearest(QueryObject p){
    return null;
  }

  public SearchVector Nearest(QueryObject p, double dist){
    return null;
  }

  public Drawable NearestFirst(QueryObject p){
    return null;
  }

  public Drawable[] NearestRange(QueryObject p, double dist){
    return null;
  }
     
  public void drawContents(DrawingTarget g, Rectangle view){
    array.drawContents(g, view);
    /* display grid */
    if (grid.gridOn) 
      drawGrid(g, grid.res);
  }   
 
    public void drawContents(DrawingTarget g){
      array.drawContents(g, null);
      /* display grid */
      if (grid.gridOn) 
	drawGrid(g, grid.res);
    }

  public void convert(ConvertVector cv){
    if (operation.equals("To quadtree")){
      convertToQuadTree(cv);
    }
    else if (operation.equals("To array")){
      convertToBinaryArray(cv);
    }
    else if (operation.equals("To raster")){
      convertToRaster(cv);
    } 
    else if (operation.equals("To chain")){
      convertToChainCode(cv);
    }
  }
  
  public Node convertToQuadTree(ConvertVector cv){
    Node root;

    root = (new ArrayToQuadTree(grid)).convert(cv);
    
    return root;
  }

  public ChainCode convertToChainCode(ConvertVector cv){
    return null;
  }

  public BinaryArray convertToBinaryArray(ConvertVector cv){
    return null;
  }

  public Raster convertToRaster(ConvertVector cv){
    Raster raster;

    raster = (new ArrayToRaster(grid)).convert(cv);

    return raster;
  }  

}






