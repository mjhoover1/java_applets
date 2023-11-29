package vasco.regions;

import java.awt.Rectangle;

import vasco.common.DrawingTarget;

public class Raster {
	protected Grid grid;

	Raster() {
		grid = null;
	}

	Raster(Grid g) {
		grid = g;
	}

	public int getColor(int x, int y) {
		return grid.getColor(x, y);
	}

	public boolean setColor(int x, int y, int color) {
		return grid.setColor(x, y, color);
	}

	public int getLevel() {
		return grid.res;
	}

	public int getRowCount() {
		return (int) (Math.pow(2, getLevel()));
	}

	public int getColCount() {
		return (int) (Math.pow(2, getLevel()));
	}

	public int[] getRow(int r) {
		if ((r < 0) || (r >= getRowCount()))
			return null;

		int colCount = getColCount();
		int[] row = new int[colCount];
		for (int i = 0; i < colCount; i++) {
			row[i] = grid.getColor(i, r);
		}

		return row;
	}

	public void drawContents(DrawingTarget g, Rectangle view) {
		grid.drawContents(g);
	}

}
