/* $Id: QueryObject.java,v 1.2 2002/09/25 20:55:05 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;
import java.awt.*;
import java.util.*;

public class QueryObject implements Drawable {

    public final static int QO_POINT = 1;
    public final static int QO_POLYGON = 2;
    public final static int QO_RECTANGLE = 4;
    public final static int QO_PATH = 8;
    public final static int QO_LINE = 16;
    public final static int QO_SECTOR = 32;

    class DrawableVector extends Vector {
	void add(Drawable d) {
	    addElement(d);
	}
	Drawable drawableGet(int i) {
	    return (Drawable)elementAt(i);
	}
	int Size() {
	    return size();
	}
    }

    DrawableVector qov;

    public QueryObject(Drawable p) {
	qov = new DrawableVector();
	qov.add(p);
    }

    public void addQueryObject(Drawable p) {
	qov.add(p);
    }

    public double distance(DPoint p) {
	double min = Double.MAX_VALUE;
	for (int i = 0; i < qov.Size(); i++) {
	    double d = qov.drawableGet(i).distance(p);
	    if (min > d)
		min = d;
	}
	return min;
    }

    public double[] distance(DPoint p, double[] k) {
	double[] min = {Double.MAX_VALUE, Double.MAX_VALUE};
	for (int i = 0; i < qov.Size(); i++) {
	    qov.drawableGet(i).distance(p, k);
	    if (min[0] > k[0] || (min[0] == k[0] && min[1] > k[1])) {
		min[0] = k[0]; min[1] = k[1];
	    }
	}
	k[0] = min[0]; k[1] = min[1];
	return k;
    }

    public double distance(DLine l) {
	double min = Double.MAX_VALUE;
	for (int i = 0; i < qov.Size(); i++) {
	double d =  qov.drawableGet(i).distance(l);
	    if (min > d)
		min = d;
	}
	return min;
    }

    public double[] distance(DLine l, double[] k) {
	double[] min = {Double.MAX_VALUE, Double.MAX_VALUE};
	for (int i = 0; i < qov.Size(); i++) {
	    qov.drawableGet(i).distance(l, k);
	    if (min[0] > k[0] || (min[0] == k[0] && min[1] > k[1])) {
		min[0] = k[0]; min[1] = k[1];
	    }
	}
	k[0] = min[0]; k[1] = min[1];
	return k;
    }

    public double distance(DRectangle r) {
	double min = Double.MAX_VALUE;
	for (int i = 0; i < qov.Size(); i++) {
	double d = qov.drawableGet(i).distance(r);
	    if (min > d)
		min = d;
	}
	return min;
    }

    public double[] distance(DRectangle r, double[] k) {
	double[] min = {Double.MAX_VALUE, Double.MAX_VALUE};
	for (int i = 0; i < qov.Size(); i++) {
	    qov.drawableGet(i).distance(r, k);
	    if (min[0] > k[0] || (min[0] == k[0] && min[1] > k[1])) {
		min[0] = k[0]; min[1] = k[1];
	    }
	}
	k[0] = min[0]; k[1] = min[1];
	return k;
    }

    public double distance(Drawable drw) {
	double min = Double.MAX_VALUE;
	for (int i = 0; i < qov.Size(); i++) {
	    double d;
	    if (drw instanceof DPoint) {
		d = distance((DPoint)drw);
	    } else if (drw instanceof DRectangle) {
		d = distance((DRectangle)drw);
	    } else if (drw instanceof DLine) {
		d = distance((DLine)drw);
	    } else {
		Thread.dumpStack();
		return 0;
	    }
	    if (min > d)
		min = d;
	}
	return min;
    }

    public double[] distance(Drawable d, double[] keys) {
	double[] min = {Double.MAX_VALUE, Double.MAX_VALUE};
	for (int i = 0; i < qov.Size(); i++) {
	    if (d instanceof DPoint) {
		distance((DPoint)d, keys);
	    } else if (d instanceof DRectangle) {
		distance((DRectangle)d, keys);
	    } else if (d instanceof DLine) {
		distance((DLine)d, keys);
	    } else {
		Thread.dumpStack();
	    }
	    if (min[0] > keys[0] || (min[0] == keys[0] && min[1] > keys[1])) {
		min[0] = keys[0]; min[1] = keys[1];
	    }
	}
	keys[0] = min[0]; keys[1] = min[1];
	return keys;
    }

    public void draw(DrawingTarget g) {
	g.setColor(Color.orange);
	for (int i = 0; i < qov.Size(); i++) {
	    qov.drawableGet(i).draw(g);
	}
    }

    public void directDraw(Color c, DrawingTarget g) {
	for (int i = 0; i < qov.Size(); i++) {
	    qov.drawableGet(i).directDraw(c, g);
	}
    }

    public DRectangle getBB() {
	DRectangle union = qov.drawableGet(0).getBB();
	for (int i = 1; i < qov.Size(); i++) 
	    union = union.union(qov.drawableGet(i).getBB());
	return union;
    }

    public boolean hasArea() {
	for (int i = 0; i < qov.Size(); i++) 
	    if (qov.drawableGet(i).hasArea())
		return true;
	return false;
    }

    private void fillPointset(Color front, Color back, DrawingTarget dt, double dist) {
	double esize, dx, dy;
	DLine e;
	boolean close = false;
	DPoint[] p = null;

	dt.setColor(front);
	for (int i = 0; i < qov.Size(); i++) {
	    if (qov.drawableGet(i) instanceof DPoint) {
		p = new DPoint[1];
		p[0] = (DPoint)qov.drawableGet(i);
		close = false;
	    } else if (qov.drawableGet(i) instanceof DLine) {
		DLine dl = (DLine)qov.drawableGet(i);
		p = new DPoint[2];
		p[0] = dl.p1;
		p[1] = dl.p2;
		close = false;
	    } else if (qov.drawableGet(i) instanceof DRectangle) {
		DRectangle dr = (DRectangle)qov.drawableGet(i);
		p = new DPoint[4];
		p[0] = dr.SWcorner();
		p[1] = dr.SEcorner();
		p[2] = dr.NEcorner();
		p[3] = dr.NWcorner();
		close = true;
	    } else if (qov.drawableGet(i) instanceof DPolygon) {
		p = ((DPolygon)qov.drawableGet(i)).getborder();
		close = true;
	    } else if (qov.drawableGet(i) instanceof DPath) {
		p = ((DPath)qov.drawableGet(i)).getborder();
		close = false;
	    } else
		Thread.dumpStack();
	
	    if (close)
		dt.fillPoly(new DPolygon(p));

	    for (int j = 0; j < p.length - (close ? 0 : 1); j++) {
		e = new DLine(p[j], p[(j+1) % p.length]);

		esize = Math.sqrt((e.p1.x - e.p2.x)*(e.p1.x - e.p2.x) + 
				  (e.p1.y - e.p2.y)*(e.p1.y - e.p2.y));
		dy = (e.p2.y - e.p1.y) / esize;
		dx = (e.p2.x - e.p1.x) / esize;

		DPoint[] pol = new DPoint[4];
		pol[0] = new DPoint(e.p1.x + dy * dist, e.p1.y - dx * dist);
		pol[1] = new DPoint(e.p1.x - dy * dist, e.p1.y + dx * dist);
		pol[2] = new DPoint(e.p2.x - dy * dist, e.p2.y + dx * dist);
		pol[3] = new DPoint(e.p2.x + dy * dist, e.p2.y - dx * dist);
	
		DPolygon dp = new DPolygon(pol);
		dt.fillPoly(dp);

		dt.fillOval(e.p1, dist, dist);

	    }
	    if (!close)
		dt.fillOval(p[p.length - 1], dist, dist);
	}

	dt.setColor(back);
	for (int i = 0; i < qov.Size(); i++) {
	    if (qov.drawableGet(i) instanceof DPoint) {
		p = new DPoint[1];
		p[0] = (DPoint)qov.drawableGet(i);
		close = false;
	    } else if (qov.drawableGet(i) instanceof DLine) {
		DLine dl = (DLine)qov.drawableGet(i);
		p = new DPoint[2];
		p[0] = dl.p1;
		p[1] = dl.p2;
		close = false;
	    } else if (qov.drawableGet(i) instanceof DRectangle) {
		DRectangle dr = (DRectangle)qov.drawableGet(i);
		p = new DPoint[4];
		p[0] = dr.SWcorner();
		p[1] = dr.SEcorner();
		p[2] = dr.NEcorner();
		p[3] = dr.NWcorner();
		close = true;
	    } else if (qov.drawableGet(i) instanceof DPolygon) {
		p = ((DPolygon)qov.drawableGet(i)).getborder();
		close = true;
	    } else if (qov.drawableGet(i) instanceof DPath) {
		p = ((DPath)qov.drawableGet(i)).getborder();
		close = false;
	    } else
		Thread.dumpStack();

	double locDist;

	//	    dist = Math.max(0, dist - 1.1);
	if (close)
	    dt.fillPoly(new DPolygon(p));
	for (int j = 0; j < p.length - (close ? 0 : 1); j++) {
	    e = new DLine(p[j], p[(j+1) % p.length]);
	    esize = Math.sqrt((e.p1.x - e.p2.x)*(e.p1.x - e.p2.x) + 
			      (e.p1.y - e.p2.y)*(e.p1.y - e.p2.y));
	    dy = (e.p2.y - e.p1.y) / esize;
	    dx = (e.p2.x - e.p1.x) / esize;

	    double alpha = 2*Math.asin(dy);
	    locDist = dist - 1 - Math.abs(Math.sin(alpha)* (Math.sqrt(2) - 1));

	    DPoint[] pol = new DPoint[4];
	    pol[0] = new DPoint(e.p1.x + dy * locDist, e.p1.y - dx * locDist);
	    pol[1] = new DPoint(e.p1.x - dy * locDist, e.p1.y + dx * locDist);
	    pol[2] = new DPoint(e.p2.x - dy * locDist, e.p2.y + dx * locDist);
	    pol[3] = new DPoint(e.p2.x + dy * locDist, e.p2.y - dx * locDist);
	
	    DPolygon dp = new DPolygon(pol);
	    dt.fillPoly(dp);

	    dt.fillOval(e.p1, dist-1, dist-1);
	}
	if (!close)
	    dt.fillOval(p[p.length - 1], dist-1, dist-1);
	}
    }

    public void fillBuffer(Color fore, Color back, DrawingTarget dt, double dist) {
	if (dist == Double.MAX_VALUE)
	    return;
	fillPointset(fore, back, dt, dist);
    }

    public void drawBuffer(Color c, DrawingTarget dt, double dist) {
	if (dist < Double.MAX_VALUE)
	    for (int i = 0; i < qov.Size(); i++) 
		qov.drawableGet(i).drawBuffer(c, dt, dist);
    }


    public boolean intersects(DRectangle r) {
	for (int i = 0; i < qov.Size(); i++) 
	    if (qov.drawableGet(i).intersects(r))
		return true;
	return false;
    }

    public boolean overlaps(Drawable r) {
	// 'r' can only be DPoint, DLine or DRectangle (applet spatial data)
	// intersection when one has some vertices inside another

	Drawable qo = qov.drawableGet(0);

	if (qo instanceof DPoint)
	    return false;
	else if (qo instanceof DLine) {
	    DLine l = (DLine)qo;
	    if (r instanceof ArealObject) {
		ArealObject rec = (ArealObject)r;
		return ((rec.contains(l.p1) && !rec.contains(l.p2)) ||
			(!rec.contains(l.p1) && rec.contains(l.p2)));
	    } else
		return false;
	} else if (qo instanceof DRectangle) {
	    DRectangle rec = (DRectangle)qo;
	    if (r instanceof DPoint) {
		return false;
	    } else if (r instanceof DLine) {
		DLine l = (DLine)r;
		return ((rec.contains(l.p1) && !rec.contains(l.p2)) ||
		     (!rec.contains(l.p1) && rec.contains(l.p2)));
	    } else if (r instanceof DRectangle) {
		return rec.intersects((DRectangle)r) && !crosses(r);
	    } else
		return false;
	} else if (qo instanceof DPolygon) {
	    DPolygon rec = (DPolygon)qo;
	    if (r instanceof DPoint) {
		return false;
	    } else if (r instanceof DLine) {
		DLine l = (DLine)r;
		return ((rec.contains(l.p1) && !rec.contains(l.p2)) ||
		     (!rec.contains(l.p1) && rec.contains(l.p2)));
	    } else if (r instanceof DRectangle) {
		return rec.intersects((DRectangle)r) && !crosses(r);
	    } else
		return false;
	} else if (qo instanceof DPath) {
	    DPath rec = (DPath)qo;
	    if (r instanceof DPoint) {
		return false;
	    } else if (r instanceof DLine) {
		return false;
	    } else if (r instanceof DRectangle) {
		return rec.intersects((DRectangle)r) && !crosses(r);
	    } else
		return false;
	} else
	    return false;
    }


    public boolean contains(Drawable r) {
	// 'this' has 'r' completely inside itself
	for (int i = 0; i < qov.Size(); i++) {
	    if (qov.drawableGet(i) instanceof DPoint || qov.drawableGet(i) instanceof DLine || qov.drawableGet(i) instanceof DPath)
		continue;
	    if (qov.drawableGet(i) instanceof DRectangle || qov.drawableGet(i) instanceof DPolygon) {
		ArealObject rec = (ArealObject)qov.drawableGet(i);
		if (r instanceof DPoint && rec.contains((DPoint)r) ||
		    r instanceof DLine && rec.contains((DLine)r) ||
		    r instanceof DRectangle && rec.contains((DRectangle)r))
		    return true;
	    }
	}   
	return false;
    }

    public boolean isContained(Drawable r) {
    // 'r' has 'this' completely inside itself
	if (!(r instanceof ArealObject))
	    return false;
	    
	ArealObject ao = (ArealObject)r;

	for (int i = 0; i < qov.Size(); i++) {
	    if (qov.drawableGet(i) instanceof DPoint && !ao.contains((DPoint)qov.drawableGet(i)) ||
		qov.drawableGet(i) instanceof DLine && !ao.contains((DLine)qov.drawableGet(i)) ||
		qov.drawableGet(i) instanceof DRectangle && !ao.contains((DRectangle)qov.drawableGet(i)) ||
		qov.drawableGet(i) instanceof DPolygon && !ao.contains((DPolygon)qov.drawableGet(i)) ||
		qov.drawableGet(i) instanceof DPath && !ao.contains((DPath)qov.drawableGet(i)))
		return false;
	}
	return true;
    }; 

    public boolean crosses(Drawable r) {
	// intersection, but no vertices of one are inside the other
	Drawable qo = qov.drawableGet(0);

	if (qo instanceof DPoint) {
	    return false;
	} else if (qo instanceof DLine) {
	    DLine dl = (DLine)qo;
	    if (r instanceof DPoint)
		return false;
	    else if (r instanceof DLine) {
		return dl.intersects((DLine)r);
	    } else if (r instanceof DRectangle) {
		DRectangle rec = (DRectangle)r;
		return (rec.intersects(dl) && 
			!rec.contains(dl.p1) && !rec.contains(dl.p2));
	    }	
	    return false;
	} else if (qo instanceof DRectangle) {
	    DRectangle rec = (DRectangle)qo;
	    if (r instanceof DPoint)
		return false;
	    else if (r instanceof DLine) {
		DLine dl = (DLine)r;
		return (rec.intersects(dl) && 
			!rec.contains(dl.p1) && !rec.contains(dl.p2));
	    } else if (r instanceof DRectangle) {
		DRectangle rec2 = (DRectangle)r;
		return rec.intersects(rec2) && 
		    !(rec.contains(rec2.SWcorner()) ||
		      rec.contains(rec2.SEcorner()) ||
		      rec.contains(rec2.NWcorner()) ||
		      rec.contains(rec2.NEcorner())) && 
		    !(rec2.contains(rec.SWcorner()) ||
		      rec2.contains(rec.SEcorner()) ||
		      rec2.contains(rec.NWcorner()) ||
		      rec2.contains(rec.NEcorner()));
	    } else
		    return false;
	} else if (qo instanceof DPolygon) {
	    DPolygon rec = (DPolygon)qo;
	    if (r instanceof DPoint)
		return false;
	    else if (r instanceof DLine) {
		DLine dl = (DLine)r;
		return (rec.intersects(dl) && 
			!rec.contains(dl.p1) && !rec.contains(dl.p2));
	    } else if (r instanceof DRectangle) {
		DRectangle rec2 = (DRectangle)r;
		boolean inside = false;
		for (int i = 0; i < rec.Size(); i++)
		    inside = inside || rec2.contains(rec.vertex(i));
		return rec.intersects(rec2) && 
		    !(rec.contains(rec2.SWcorner()) ||
		      rec.contains(rec2.SEcorner()) ||
		      rec.contains(rec2.NWcorner()) ||
		      rec.contains(rec2.NEcorner())) && 
		    !inside;
	    } else
		return false;
	} else if (qo instanceof DPath) {
	    DPath dl = (DPath)qo;
	    if (r instanceof DPoint)
		return false;
	    else if (r instanceof DLine) {
		return dl.intersects((DLine)r);
	    } else if (r instanceof DRectangle) {
		DRectangle rec = (DRectangle)r;
		for (int i = 0; i < dl.Size(); i++) {
		    if (rec.contains(dl.vertex(i)))
			return false;
		}
		return (dl.intersects(rec)); 
	    }
	    return false;
	} else
	    return false;
    }
}




