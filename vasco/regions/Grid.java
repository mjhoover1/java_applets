package vasco.regions;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import vasco.common.DrawingTarget;

public class Grid {
	public int size;
	public int cellCount;
	public int cellSize;
	public int[][] grid;
	public int res;
	public boolean gridOn;

	protected Rectangle orig;
	protected DrawingTarget offscrG;

	Grid() {
		size = 0;
		res = 0;
		cellCount = 0;
		cellSize = 0;
		grid = null;
		gridOn = true;
		orig = null;
		offscrG = null;
	}

	public Grid(Rectangle orig, DrawingTarget offscrG, int size, int res) {
		this.orig = orig;
		this.offscrG = offscrG;
		this.size = size;
		this.res = res;
		cellCount = (int) Math.pow(2, this.res);
		cellSize = size / cellCount;
		grid = new int[cellCount][cellCount];
		for (int x = 0; x < cellCount; x++)
			for (int y = 0; y < cellCount; y++)
				grid[x][y] = 0;
		gridOn = true;
	}

	public void Clear() {
		for (int x = 0; x < cellCount; x++)
			for (int y = 0; y < cellCount; y++)
				grid[x][y] = 0;
	}

	public int getSize() {
		return size;
	}

	public int getCellSize() {
		return cellSize;
	}

	public int getCellCount() {
		return cellCount;
	}

	public int[][] getGrid() {
		return grid;
	}

	public void setGrid(boolean on) {
		gridOn = on;
	}

	public int[][] setGrid(int res) {
		int[][] oldGrid = grid;
		this.res = res;
		cellCount = (int) Math.pow(2, this.res);
		cellSize = size / cellCount;
		grid = new int[cellCount][cellCount];
		Clear();
		return oldGrid;
	}

	public void setGrid(int size, int res) {
		this.size = size;
		this.res = res;
		cellCount = (int) Math.pow(2, res);
		cellSize = size / cellCount;
		grid = new int[cellCount][cellCount];
		Clear();
	}

	public int getColor(int y, int x) {
		if (x < 0 || y < 0 || !((x < cellCount) && (y < cellCount)))
			return -1;

		return grid[x][y];
	}

	public int[][] get(Rectangle r) {
		int[][] res;

		res = new int[r.width][r.height];

		for (int x = 0; x < r.width; x++)
			for (int y = 0; y < r.height; y++)
				if ((y + r.y) < 0 || (x + r.x) < 0 || (y + r.y) >= cellCount || (x + r.x) >= cellCount)
					res[x][y] = -1;
				else
					res[x][y] = grid[y + r.y][x + r.x];

		return res;
	}

	public int[][] get(Rectangle r, int[][] block) {
		int[][] res;
		int color;

		res = new int[r.width][r.height];

		for (int x = 0; x < r.width; x++) {
			for (int y = 0; y < r.height; y++) {
				if ((y + r.y) >= cellCount || (x + r.x) >= cellCount)
					color = -1;
				else
					color = grid[y + r.y][x + r.x];

				if (color == -1)
					res[x][y] = -1;
				else if (color == 1 || block[x][y] == 1)
					res[x][y] = 1;
				else
					res[x][y] = 0;

			} // for y
		} // for x

		return res;
	}

	public boolean setColor(int y, int x, int color) {

		if (x < 0 || y < 0 || !((x < cellCount) && (y < cellCount)) || (grid[x][y] == color))
			return false;

		grid[x][y] = color;

		return true;
	}

	public int flipColor(int y, int x) {
		if (!((x < cellCount) && (y < cellCount)))
			return -1;

		if (grid[x][y] == 0)
			grid[x][y] = 1;
		else
			grid[x][y] = 0;

		return grid[x][y];
	}

	public Point grdToScr(Point p) {
		return new Point(p.x * cellSize, p.y * cellSize);
	}

	public void display(DrawingTarget g) {
		if (res == 0 || !gridOn)
			return;
		int width = cellCount * cellSize;
		int height = width;

		g.setColor(Color.lightGray);
		double canvasWidth = width;
		double add = canvasWidth / (int) Math.pow(2, res);
		for (double line = add; line < canvasWidth; line += add) {
			g.drawLine(0, line, width, line);
			g.drawLine(line, 0, line, height);
		}
	}

	// draw from (0, 0) to (col, row)
	public void drawContents(DrawingTarget g, int row, int col) {
		for (int x = 0; x < row; x++)
			drawContents(g, x);
		drawContents(g, new Rectangle(0, row, col, 1));
	}

	// draw a row
	public void drawContents(DrawingTarget g, int row) {
		drawContents(g, new Rectangle(0, row, cellCount, 1));
	}

	// draw the grid inside the Rectangle r
	public void drawContents(DrawingTarget g, Rectangle r) {
		for (int x = r.x; x < (r.x + r.width); x++)
			for (int y = r.y; y < (r.y + r.height); y++) {
				if (grid[y][x] == 1) {
					g.setColor(Colors.GRID_CELL);
					g.fillRect(x * cellSize + 1, y * cellSize + 1, cellSize - 1, cellSize - 1);
				} else {
					g.setColor(Color.white);
					g.fillRect(x * cellSize + 1, y * cellSize + 1, cellSize - 1, cellSize - 1);
				}
			} // for y
	}

	public void drawContents(DrawingTarget g) {
		for (int x = 0; x < cellCount; x++)
			for (int y = 0; y < cellCount; y++) {
				if (grid[x][y] == 1) {
					g.setColor(Colors.GRID_CELL);
					g.fillRect(y * cellSize + 1, x * cellSize + 1, cellSize - 1, cellSize - 1);
				} else {
					g.setColor(Color.white);
					g.fillRect(y * cellSize + 1, x * cellSize + 1, cellSize - 1, cellSize - 1);
				}
			}
	}

	public void drawContents(Image i, Rectangle r, int[][] block) {
		int x, y;
		int mode;
		int color;

		if (block == null)
			mode = 0; // copy mode
		else
			mode = 1; // union mode

		// draw content
		Graphics g = i.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, r.width * cellSize, r.height * cellSize);
		g.setColor(Colors.GRID_CELL);
		for (x = 0; x < r.width; x++)
			for (y = 0; y < r.height; y++) {
				if ((y + r.y) >= cellCount || (x + r.x) >= cellCount)
					color = -1;
				else
					color = grid[r.y + y][r.x + x];

				if (mode == 0) {
					if (color == -1)
						continue;

					if (color == 1)
						g.fillRect(x * cellSize + 1, y * cellSize + 1, cellSize - 1, cellSize - 1);
				} else {
					if (color == 1 || block[x][y] == 1)
						g.fillRect(x * cellSize + 1, y * cellSize + 1, cellSize - 1, cellSize - 1);
				}
			}

		// draw grid itself
		if (res == 0 || !gridOn)
			return;

		g.setColor(Color.lightGray);

		for (x = 0; x < r.height; x++)
			g.drawLine(0, x * cellSize, r.width * cellSize, x * cellSize);

		for (x = 0; x < r.width; x++)
			g.drawLine(x * cellSize, 0, x * cellSize, r.height * cellSize);

		g.setColor(Colors.SELECTED_CELL);
		g.drawRect(1, 1, r.width * cellSize - 2, r.height * cellSize - 2);
	}

	/* real world coordinated to real word grid coordinates */
	public Point getRGridCoor(int x, int y) {
		return new Point(x / cellSize, y / cellSize);
	}

	/* screen coordinates to realworld grid coordinates */
	public Point getGridCoor(Point scrC) {
		Rectangle target = offscrG.getView();

		int x = orig.width * (scrC.x - target.x) / target.width + orig.x;
		int y = orig.height * (scrC.y - target.y) / target.height + orig.y;

		return new Point(x / cellSize, y / cellSize);
	}

	public Rectangle getScreenCoor(Rectangle rec) {
		return new Rectangle(rec.x * cellSize, rec.y * cellSize, rec.width * cellSize, rec.height * cellSize);
	}

	public Rectangle getScreenCoor(Point gridC) {
		return new Rectangle(gridC.x * cellSize, gridC.y * cellSize, cellSize, cellSize);
	}

}
