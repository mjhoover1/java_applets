package vasco.regions;

import java.awt.Color;
import java.awt.Rectangle;

import vasco.common.DrawingTarget;

public class BackgrdElement extends ConvertGenElement {

	Grid grid;
	Rectangle rec;
	Color color;

	// grid row, sCol, and eCol
	public BackgrdElement(Grid grid, Rectangle rec, Color color) {
		this.grid = grid;
		this.rec = rec;
		this.color = color;
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		// draw the background
		g.setColor(color);
		g.fillRect(rec.x, rec.y, rec.width, rec.height);
		// draw the grid
		if (grid.gridOn)
			grid.display(g);
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

}
