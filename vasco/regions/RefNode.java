package vasco.regions;

public class RefNode {
	public Node node;

	RefNode() {
		node = null;
	}

	RefNode(Node n) {
		node = n;
	}

	public Node getValue() {
		return node;
	}

	public void setValue(Node n) {
		node = n;
	}

}
