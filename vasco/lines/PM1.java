package vasco.lines;

/* $Id: PM1.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.RebuildTree;
import vasco.common.TopInterface;

public class PM1 extends GenericLine {

	public PM1(DRectangle can, int mb, TopInterface p, RebuildTree r) {
		super(can, mb, p, r);
	}

	@Override
	public String getName() {
		return "PM1 Quadtree";
	}

	@Override
	public boolean orderDependent() {
		return false;
	}

	// ------------ private methods --------

	@Override
	boolean Insert(QEdgeList P, QNode R, int md) {
		QEdgeList L;
		boolean ok = true;

		L = ClipLines(P, R.SQUARE);
		if (L == null)
			return ok;
		// System.out.println(R.toString());
		if (R.NODETYPE != GRAY) {
			L = MergeLists(L, R.DICTIONARY);
			// System.out.println("merged lists:" + L.toString());
			if (PM1Check(L, R.SQUARE) || md < 0) {
				if (md < 0)
					ok = false;
				R.DICTIONARY = L;
				return ok;
			} else {
				SplitPMNode(R);
			}
		}
		for (int i = 0; i < 4; i++) {
			ok = Insert(L, R.SON[i], md - 1) && ok;
		}
		return ok;
	}

	@Override
	void Delete(QEdgeList P, QNode R) {
		QEdgeListRef L = new QEdgeListRef();

		L.val = ClipLines(P, R.SQUARE);
		if (L.val == null)
			return;
		if (R.NODETYPE == GRAY) {
			for (int i = 0; i < 4; i++) {
				Delete(L.val, R.SON[i]);
			}
			if (Possible_PM1R_Merge(R)) {
				L.val = null;
				if (TryToMergePM1(R, R, L)) {
					R.DICTIONARY = L.val;
					R.NODETYPE = BLACK;
					R.SON[0] = R.SON[1] = R.SON[2] = R.SON[3] = null;
				}
			}
		} else
			R.DICTIONARY = SetDifference(R.DICTIONARY, L.val);
	}

	boolean TryToMergePM1(QNode P, QNode R, QEdgeListRef L) {

		if (P.NODETYPE != GRAY) {
			L.val = SetUnion(L.val, P.DICTIONARY);
			return (true);
		} else {
			return (TryToMergePM1(P.SON[0], R, L) && TryToMergePM1(P.SON[1], R, L) && TryToMergePM1(P.SON[2], R, L)
					&& TryToMergePM1(P.SON[3], R, L) && PM1Check(L.val, R.SQUARE));
		}
	}

	boolean PM1Check(QEdgeList l, QSquare S) {
		if (l == null)
			return true;
		if (l.DATA.P1 == l.DATA.P2) // Compare 'pointers'
			return (l.NEXT == null);
		if (l.NEXT == null)
			return (!(PtInSquare(l.DATA.P1, S) && PtInSquare(l.DATA.P2, S)));
		if (PtInSquare(l.DATA.P1, S) && PtInSquare(l.DATA.P2, S))
			return false;
		if (PtInSquare(l.DATA.P1, S))
			return (SharePM1Vertex(l.DATA.P1, l.NEXT, S));
		if (PtInSquare(l.DATA.P2, S))
			return (SharePM1Vertex(l.DATA.P2, l.NEXT, S));
		return false;
	}

	boolean SharePM1Vertex(DPoint P, QEdgeList l, QSquare S) {
		if (l == null)
			return true;
		if (P == l.DATA.P1) // compare pointers
			return (!PtInSquare(l.DATA.P2, S) && SharePM1Vertex(P, l.NEXT, S));
		if (P == l.DATA.P2)
			return (!PtInSquare(l.DATA.P1, S) && SharePM1Vertex(P, l.NEXT, S));
		return false;
	}
}
