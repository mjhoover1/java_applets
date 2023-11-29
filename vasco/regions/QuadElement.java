package vasco.regions;

import vasco.common.DrawingTarget;

public class QuadElement extends ConvertGenElement {

	public static final int DRAW_ALL = 0;
	public static final int DRAW_NONLEAF = 1;

	Grid grid;
	Node node;
	int mode;

	// grid row, sCol, and eCol
	public QuadElement(Grid grid, Node node, int mode) {
		this.grid = grid;
		this.node = node;
		this.mode = mode;
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		switch (mode) {
		case DRAW_ALL:
			node.display(g, 0);
			node.display(g, 1);
			break;
		case DRAW_NONLEAF:
			node.display(g, 1);
			break;
		}
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

}
