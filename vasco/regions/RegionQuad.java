package vasco.regions;

import vasco.common.*;
import vasco.drawable.*;

import java.awt.*;
import java.util.*;
 
public class RegionQuad extends RegionStructure {

  public RegionQuad(GenericCanvas rc, DRectangle can, DrawingTarget dt, TopInterface p, RebuildTree r, Grid g) {
    super(rc, can, dt, p, r, g, QUAD_MODE); 
  }  
     
  /************** RegionStructure **************/
  public int search(Point p){
    return super.search(p);
  }

  public int search(Node node, Point p){
    return super.search(node, p);
  } 

 
  public void reInit(Choice ao) {
    super.reInit(ao);
    ao.addItem("To raster");
    ao.addItem("To chain");
    ao.addItem("To array"); 
  }  
 
  public void Clear() {
    super.Clear();
    root = new Node(null, 0, 0, 512, 0, -1, 0, true);
    /* clear the grid */
    grid.Clear();
  }
 
  public String getName(){
    return "Region Tree"; 
  }

  public boolean orderDependent() {
    return false;
  } 
 
  public boolean Insert(Point p){
    return super.Insert(p);
  }
  
  public boolean Delete(Point p){
    return super.Delete(p);
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
    super.drawContents(g, view);
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
    return null;
  }

  public ChainCode convertToChainCode(ConvertVector cv){
    ChainCode code = null;
 
    if (si == null)
      si = new StructureBox("Chain Code", 7, 40);
    code = (new QuadTreeToChain(root, grid, si)).convert(cv);
    

    return code;
  }

  public BinaryArray convertToBinaryArray(ConvertVector cv){
    BinaryArray array;

    array = (new QuadTreeToArray(root, grid)).convert(cv);

    return null;
  }

  public Raster convertToRaster(ConvertVector cv){
    Raster raster;
 
    raster = (new QuadTreeToRaster(root, grid)).convert(cv);

    return raster;
  } 
}

 




