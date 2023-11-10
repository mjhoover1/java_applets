/* $Id: DSector.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.util.*;
import vasco.drawable.*;

public class DSector extends DPolygon implements Drawable, ArealObject {
    private DPoint vertex;
    int startAngle, extent;
    DRectangle wholeCanvas = new DRectangle(0, 0, 512, 512);

    public DSector(DPoint vertex, int startAngle, int extent) {
	super();
	setSector(vertex, startAngle, extent);
    }

    public void adjustVertex(DPoint p) {
	vertex = p;
	setSector();
    }

    public void adjustStart(DPoint p) {
	startAngle = (int)(Math.atan2(p.y - vertex.y, p.x - vertex.x) / Math.PI * 180);
	setSector();
    }

    public void adjustExtent(DPoint p) {
	extent = (int)((Math.atan2(p.y - vertex.y, p.x - vertex.x) / Math.PI * 180) - startAngle + 2*360) 
	    % 360;
	setSector();
    }

    public void setSector() {
	setSector(vertex, startAngle, extent);
    }

    public void setSector(DPoint vertex, int startAngle, int extent) {
	this.vertex = vertex;
	this.startAngle = startAngle;
	this.extent = extent;

	/*
	DPoint v1 = new DPoint(vertex.x + 1000 * Math.cos(startAngle),
			       vertex.y + 1000 * Math.sin(startAngle));
	DPoint v2 = new DPoint(vertex.x + 1000 * Math.cos(startAngle + extent),
			       vertex.y + 1000 * Math.sin(startAngle + extent));
	DPoint v3 = new DPoint(v1.x + v2.x - vertex.x, v1.y  + v2.y - vertex.y);
	border = new DPoint[4];
	border[0] = vertex;
	border[1] = v1;
	border[2] = v3;
	border[3] = v2;
	size = 4;
	*/

	border = getBorder(vertex, startAngle, extent);
	size = border.length;
	boundingbox = new DRectangle(border[0].x, border[0].y, 0, 0);
	for (int i = 1; i < border.length; i++)
	    boundingbox = boundingbox.union(new DRectangle(border[i].x, border[i].y, 0, 0));
    }

    public boolean equals(Object obj) {
	if (obj instanceof DSector) {
	    DSector r = (DSector)obj;
	    return r.vertex.equals(vertex) && 
		r.startAngle == startAngle && 
		r.extent == extent;
	}
	return false;
    }

    public void draw(DrawingTarget g) {
	super.draw(g);
	g.drawArc(vertex.x - 25, vertex.y - 25, 50, 50, -startAngle - extent, extent);
    }
    public void directDraw(Color c, DrawingTarget g) {
	super.directDraw(c, g);
	g.setColor(c);
	g.directDrawArc(c, vertex.x - 25, vertex.y - 25, 50, 50, 
			-startAngle - extent, extent);
    }
    private DPoint[] getBorder (DPoint vertex, int a, int e) {
	double x1 = vertex.x;
	double y1 = vertex.y;
	double a1 = a / 180.0 * Math.PI;
	double a2 = (a + e) / 180.0 * Math.PI;
	double maxdist = Math.sqrt(2) * wholeCanvas.width;
	DRectangle indexarea = wholeCanvas;
	if (!indexarea.contains(vertex))
	    maxdist += maxdist + vertex.distance(indexarea);
	double armlen = 2 * maxdist;
	Vector overlappoly = new Vector();
	overlappoly.addElement(vertex);
	double x2 = armlen * Math.cos(a1) + x1;
	double y2 = armlen * Math.sin(a1) + y1;
	overlappoly.addElement(new DPoint(x2, y2));
	double x3 = armlen*Math.cos(a2)+x1;
	double y3 = armlen*Math.sin(a2)+y1;

	DLine line1 = new DLine(x1, y1, x2, y2);

	if (e != 0) {
	    // see if we need to add points between (x2,y2) and (x3,y3)
	    DLine line2 = new DLine(x1, y1, x3, y3);
	    x1 = indexarea.x;
	    x2 = indexarea.x + indexarea.width;
	    y1 = indexarea.y;
	    y2 = indexarea.y + indexarea.height;

	    DPoint[] corners = new DPoint[4];
	    corners[0] = indexarea.NEcorner();
	    corners[1] = indexarea.NWcorner();
	    corners[2] = indexarea.SWcorner();
	    corners[3] = indexarea.SEcorner();

	    int  firstside = 0;
	    if (line1.distance(new DLine(x1, y2, x2, y2)) == 0) {
		firstside = 1;
	    } else if (line1.distance(new DLine(x1, y1, x1, y2)) == 0) {
		firstside = 2;
	    } else if (line1.distance(new DLine(x1, y1, x2, y1)) == 0) {
		firstside = 3;
	    }

	    int lastside = 0;
	    if (line2.distance(new DLine(x1, y2, x2, y2)) == 0) {
		lastside = 1;
	    } else if (line2.distance(new DLine(x1, y1, x1, y2)) == 0) {
		lastside = 2;
	    } else if (line2.distance(new DLine(x1, y1, x2, y1)) == 0) {
		lastside = 3;
	    }
	    if (((lastside - firstside+4)%4) > 0 || e > 180 ) {
		boolean first = true;
		for (int i = firstside; i != lastside || first; i = (i+1) % 4) {
		    overlappoly.addElement(corners[i]);
		    first = false;
		}
	    }
	    overlappoly.addElement(new DPoint(x3, y3));
	}
	DPoint[] dpa = new DPoint[overlappoly.size()];
	overlappoly.copyInto(dpa);

	return dpa;
    }

}





