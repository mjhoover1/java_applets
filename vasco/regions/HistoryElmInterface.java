package vasco.regions;

import java.util.Vector;

public interface HistoryElmInterface {

	public void save(Vector v);

	public void build(RegionStructure pstruct);

	public Vector switchGrid(int oldRes, int oldCellSize, int[][] oldGrid, Grid newGrid);
}
