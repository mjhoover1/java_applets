/**
 *
 */
package vasco.common;

public class DPointWrapper implements java.lang.Comparable {
	public DPoint p;

	public DPointWrapper(DPoint dp) {
		p = dp;
	}

	@Override
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof DPoint || o instanceof DPointWrapper) {
			DPointWrapper dp2;
			if (o instanceof DPoint)
				dp2 = new DPointWrapper((DPoint) o);
			else
				dp2 = (DPointWrapper) o;

			if (p.x < dp2.p.x)
				return -1;
			else if (p.x > dp2.p.x)
				return 1;
			else if (p.y < dp2.p.y)
				return -1;
			else if (p.y > dp2.p.y)
				return 1;
			else
				return 0;
		} else
			return -1;
	}
}