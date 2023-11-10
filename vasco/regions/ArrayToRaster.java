package vasco.regions;
import vasco.common.*;
import java.awt.*;
import java.util.*;
  
public class ArrayToRaster{ 
  protected Grid grid;
  protected Raster raster;

  public ArrayToRaster(Grid grid){
    this.grid = grid;
    raster = new Raster(this.grid);
  }
 
  public Raster convert(ConvertVector sv){
    int x;
    ContainerElement container = null;

    sv.constructRegion();

    for(x = 0; x < grid.cellCount; x++){
	container = new ContainerElement(null, grid, null);
	container.addElement(new BackgrdElement(grid, new Rectangle(0, 0, 512, 512),  
						Colors.UNKNOWN));
	container.addElement(new GridElement(grid, x, grid.cellCount));

	container.addElement(
	     new RectangleElement( 
	      new Rectangle(0, x * grid.cellSize, 512, grid.cellSize),
	      Color.green, 1, false, false));
	 
	container.addElement(
	      new RectangleElement( 
	       new Rectangle(1, 
			     x * grid.cellSize + 1, 
			     512 - 2,  
			     grid.cellSize - 2),
	       Color.blue, 1, false, false)); 

	sv.addElement(new ConvertElement(container, new Vector())); 
    } // for x

    return raster;
  }


}
