package vasco.regions;
import java.util.*;
import java.awt.*;

public interface HistoryElmInterface{

  public void save(Vector v);
  public void build(RegionStructure pstruct);
  public Vector switchGrid(int oldRes, int oldCellSize,
			    int[][] oldGrid, Grid newGrid);
}
