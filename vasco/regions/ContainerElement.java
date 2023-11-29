package vasco.regions;

import java.util.Vector;

import vasco.common.DrawingTarget;
import vasco.common.GenElement;

public class ContainerElement implements GenElement {
	Vector list;
	Node node;
	Grid grid;

	public ContainerElement() {
		list = null;
		node = null;
		grid = null;
	}

	public ContainerElement(Node n, Grid g, Vector l) {
		ConvertGenElement cge;

		node = n;
		grid = g;
		list = new Vector();
		if (l != null) {
			for (int x = 0; x < l.size(); x++) {
				cge = (ConvertGenElement) l.elementAt(x);
				if (cge.makeCopy())
					list.addElement(cge);
			}
		}

	}

	public Vector getList() {
		return list;
	}

	public void addElement(ConvertGenElement e) {
		list.addElement(e);
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
		ConvertGenElement e;

		if (node != null) {
			node.completeNode(0, 0, 512, 0);
			node.display(g, 0); /* display leafs */
			grid.display(g);
			node.display(g, 1); /* display non-leafs */
		}

		/* display the elements in the list */
		for (int i = 0; i < list.size(); i++) {
			e = (ConvertGenElement) list.elementAt(i);
			e.drawElementFirst(g);
		}
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

	@Override
	public int pauseMode() {
		return 0;
	}

}
