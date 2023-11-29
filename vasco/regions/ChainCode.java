package vasco.regions;

import java.util.Vector;

public class ChainCode {
	public static final int SAME = 0;
	public static final int CLOCKWISE = 1;
	public static final int CCLOCKWISE = 2;
	public static final int OPPOSITE = 3;

	Vector chain;
	int curr;

	public ChainCode() {
		chain = new Vector();
		curr = 0;
	}

	public ChainCode(Vector chain) {
		this.chain = chain;
		curr = 0;
	}

	int getXInc(int dir) {
		switch (dir) {
		case Node.N:
			return 0;
		case Node.E:
			return 1;
		case Node.S:
			return 0;
		case Node.W:
			return -1;
		}

		return -1;
	}

	int getYInc(int dir) {
		switch (dir) {
		case Node.N:
			return -1;
		case Node.E:
			return 0;
		case Node.S:
			return 1;
		case Node.W:
			return 0;
		}

		return -1;
	}

	static int code(int d) {
		switch (d) {
		case Node.N:
			return 1;
		case Node.E:
			return 2;
		case Node.S:
			return 4;
		case Node.W:
			return 8;
		}

		return -1;
	}

	static int CW(int d) {
		switch (d) {
		case Node.N:
			return Node.E;
		case Node.E:
			return Node.S;
		case Node.S:
			return Node.W;
		case Node.W:
			return Node.N;
		}

		return -1;
	}

	static int CCW(int d) {
		switch (d) {
		case Node.N:
			return Node.W;
		case Node.E:
			return Node.N;
		case Node.S:
			return Node.E;
		case Node.W:
			return Node.S;
		}

		return -1;
	}

	static int compare(int e1, int e2) {
		if (e1 == e2)
			return SAME;

		if (e2 == CW(e1))
			return CLOCKWISE;

		if (e2 == CCW(e1))
			return CCLOCKWISE;

		return -1;
	}

	static int link(int e) {
		int l = -1;

		switch (e) {
		case Node.N:
			l = 0;
			break;
		case Node.E:
			l = 3;
			break;
		case Node.S:
			l = 2;
			break;
		case Node.W:
			l = 1;
			break;
		}

		return l;
	}

	static int linkToDir(int l) {
		int s = -1;

		switch (l) {
		case 0:
			s = Node.E;
			break;
		case 3:
			s = Node.S;
			break;
		case 2:
			s = Node.W;
			break;
		case 1:
			s = Node.N;
			break;
		}

		return s;
	}

	static String linkToStr(int l) {
		String s = null;

		switch (l) {
		case 0:
			s = "E";
			break;
		case 3:
			s = "S";
			break;
		case 2:
			s = "W";
			break;
		case 1:
			s = "N";
			break;
		}

		return s;
	}

	static int boundary(int d) {
		switch (d) {
		case Node.E:
			return Node.N;
		case Node.S:
			return Node.E;
		case Node.W:
			return Node.S;
		case Node.N:
			return Node.W;
		}
		return -1;
	}

	public void constructChainCode(Node node, Grid grid) {
		chain = new Vector();

		QuadTreeToChain.convert(node, grid, chain, null, null);
	}

	public int size() {
		return chain.size();
	}

	public int elementAt(int i) {
		if ((i < 0) && (i >= chain.size()))
			return -1;

		return linkToDir(((Integer) chain.elementAt(i)).intValue());
	}

	public void start() {
		curr = 0;
	}

	public int getCurr() {
		return linkToDir(((Integer) chain.elementAt(curr)).intValue());
	}

	public int getNext() {
		curr = curr + 1;
		if (curr >= chain.size()) {
			curr = 0;
			return -1;
		}

		return linkToDir(((Integer) chain.elementAt(curr)).intValue());
	}

	public static String print(Vector chain) {
		StringBuffer sb = new StringBuffer();
		int x, count, colCount;
		int curr, old;

		count = 0;
		old = -1;
		colCount = 0;
		for (x = 0; x < chain.size(); x++) {
			curr = ((Integer) chain.elementAt(x)).intValue();
			if (old == curr) {
				count++;
			} else {
				if (old != -1) {
					sb.append(ChainCode.linkToStr(old));
					sb.append("(" + count + ") ");
					colCount++;
					if (colCount == 5) {
						sb.append("\n");
						colCount = 0;
					}
				}
				count = 1;
			}
			old = curr;
		} // for x

		sb.append(ChainCode.linkToStr(old));
		sb.append("(" + count + ") ");
		colCount++;
		if (colCount == 5)
			sb.append("\n");

		return sb.toString();
	}

	@Override
	public String toString() {
		return ChainCode.print(chain);
	}

}
