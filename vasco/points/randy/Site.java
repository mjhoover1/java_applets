/**
 * 
 */
package vasco.points.randy;

import java.awt.Color;
import java.util.ArrayList;

import vasco.common.DPoint;
import vasco.common.DPointWrapper;
import vasco.common.QueryObject;

public class Site extends DPoint {
	DPoint d;
	ArrayList edge = new ArrayList();

	public static int colorIndex = 0;
	public static Object[] colors = getColors();

	public Color color;

	public static Object[] getColors() {
		float vals[] = { .1f, .2f, .3f, .4f };

		ArrayList toReturn = new ArrayList();
		for (int i = 0; i < vals.length; i++) {
			for (int j = i + 1; j < vals.length; j++) {
				for (int k = j + 1; k < vals.length; k++) {
					toReturn.add(new Color(vals[i], vals[j], vals[k]));
					toReturn.add(new Color(vals[j], vals[i], vals[k]));
					toReturn.add(new Color(vals[j], vals[k], vals[i]));
				}
				toReturn.add(new Color(vals[i], vals[j], vals[j]));
				toReturn.add(new Color(vals[j], vals[j], vals[i]));
				toReturn.add(new Color(vals[j], vals[i], vals[j]));
				toReturn.add(new Color(vals[i], vals[i], vals[j]));
				toReturn.add(new Color(vals[i], vals[j], vals[i]));
				toReturn.add(new Color(vals[j], vals[i], vals[i]));
			}
		}

		return toReturn.toArray();
	}

	public Site(DPoint p) {
		super(p);
		color = (Color) colors[colorIndex % colors.length];
		colorIndex++;
	}

	public Color getColor() {
		return color;
	}

	public void addEdge(Site s) {
		if (!edge.contains(s))
			edge.add(s);
	}

	public void removeEdge(Site s) {
		for (int i = 0; i < edge.size(); i++) {
			if (new DPointWrapper((DPoint) (edge.get(i))).equals(s)) {
				edge.remove(i);
			}
		}
	}

	public Site getCloser(QueryObject d) {
		double currd = d.distance(this);
		Site minSite = null;
		double minD = -1;
		for (int i = 0; i < edge.size(); i++) {
			Site s = (Site) edge.get(i);
			double dist = d.distance(s);
			if (dist < currd) {
				if (minD == -1 || dist < minD) {
					minD = dist;
					minSite = s;
				}
			}
		}
		return minSite;
	}
}