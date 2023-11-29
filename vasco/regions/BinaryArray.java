package vasco.regions;

import java.awt.Rectangle;

import vasco.common.DrawingTarget;

public class BinaryArray {
	protected Grid grid;

	BinaryArray() {
		grid = null;
	}

	BinaryArray(Grid g) {
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

	public void drawContents(DrawingTarget g, Rectangle view) {
		grid.drawContents(g);
	}

}
