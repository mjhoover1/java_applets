/* $Id: DPolygon.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.util.*;
import vasco.drawable.*;
//============================================================================
//
//  POLYGON
//
//============================================================================

//	A polygon, as it is used here, is a single closed planar region
//	with no interior holes, defined by a non-self-intersecting closed
//	loop of line segments.  This loop need not be convex.  The polygon
//	is considered to be all points on the interior of the border,
//	including the border lines.

public class DPolygon implements Drawable, ArealObject {

  public boolean hasArea() {
    return true;
  }

  public void draw(DrawingTarget g) {
    for (int i = 0; i < size; i++) {
      (new DLine(border[i], border[(i+1)%size])).draw(g);
    }
  }

  public void directDraw(Color c, DrawingTarget g) {
    for (int i = 0; i < size; i++) {
      (new DLine(border[i], border[(i+1)%size])).directDraw(c, g);
    }
  }


  protected DPoint[] border;		// vector of border points
  int current;			// current position on border
  int size;			// total number of border points
  protected DRectangle boundingbox;	// bounding rectangle of polygon

  public int Size () {
    return size; 
  }; // Returns the number of vertices


    public DPoint vertex(int i) {
    return border[i];        // Returns i'th vertex
  }

  DLine Edge (int i) {
    return new DLine(border[i], border[(i+1) % size]);
  };
   
  DPoint[] getborder () { 
    return border; 
  }       // Returns a pointer to the 
  // Array of points that 
  // defines its border

  public DPolygon () {
    //       -------
    //
    //  Builds a null polygon
  
    border = null;
    size = 0;
    current = 0;
    boundingbox = new DRectangle (0, 0, 0, 0);
  }

  public DPolygon (DPolygon g) {
    //       -------
    //
    border = new DPoint[g.size];
    size = g.size;
    for (int i = 0; i < size; i++)
      border[i] = g.border[i];
    current = 0;
    boundingbox = g.boundingbox;
    // no need to check semantic constraints on the border, as they would
    // have been enforced when the copied polygon was originally created.
  }


  public DPolygon (DPoint[] vec) {
    //       -------
    //
    int sz = vec.length;

    double xmax,ymax,xmin,ymin;
    int i;	// loop variables

    border = new DPoint[sz];
    size = sz;
    xmax = vec[0].x;
    xmin = vec[0].x;
    ymax = vec[0].y;
    ymin = vec[0].y;
    for (i = 0; i<sz; i++)
      {
        border[i] = vec[i];
	xmax = xmax > vec[i].x ? xmax : vec[i].x;
	xmin = xmin < vec[i].x ? xmin : vec[i].x;
	ymax = ymax > vec[i].y ? ymax : vec[i].y;
	ymin = ymin < vec[i].y ? ymin : vec[i].y;
      }
    current = 0;
    boundingbox = new DRectangle(xmin,ymin, xmax-xmin, ymax-ymin);

    // assert( non_self_intersecting(*this) );   semantic constraints

  }



  public DPolygon (DRectangle r) {
    //       -------
    //

    border = new DPoint[4];
    size = 4;
    current = 0;

    border[0] = new DPoint(r.x, r.y);
    border[1] = new DPoint(r.x, r.y + r.height);
    border[2] = new DPoint(r.x + r.width, r.y + r.height);;
    border[3] = new DPoint(r.x + r.width, r.y);

    boundingbox = r;
  }

  DPoint FirstCorner () {
    //    --------------------
    //
    // returns the first corner of the polygon border
    // alters the position to the new corner examined
    current = 0;
    return border[current];
  }

  DPoint NextCorner () {
    //    -------------------
    //
    // returns the next corner of the polygon border from the current one
    // alters the position to the new corner examined
    current++;
    if (current >= size) current = 0;
    return border[current];
  }

  DPoint PrevCorner () {
    //    -------------------
    //
    // returns the corner of the polygon border previous to the current one
    // alters the position to the new corner examined
    if (current == 0) current = size;
    current--;
    return border[current];
  }

  DPoint LastCorner () {
    //    -------------------
    //
    // returns the corner of the polygon border previous to the first one
    // alters the position to the new corner examined
    current = size - 1;
    return border[current];
  }

  DPoint ThisCorner () {
    //    -------------------
    //
    // returns the current corner of the polygon border
    // does not shift position on the border
    return border[current];
  }

  DLine FirstEdge () {
    //   ------------------
    //
    // returns the first edge of the polygon border
    // advances position to the second point of the edge
    return new DLine (FirstCorner(), NextCorner());
  }

  DLine NextEdge () {
    //   -----------------
    //
    // returns the next edge of the polygon border
    // advances position to the second point of the edge
    return new DLine (ThisCorner(), NextCorner());
  }

  DLine PrevEdge () {
    //   -----------------
    //
    // returns the previous edge of the polygon border
    // leaves position at the second point of the edge

    DPoint p2 = PrevCorner();		// end point of previous edge
    DPoint p1 = PrevCorner();		// start point of previous edge
    NextCorner();			// leave position at 2nd point of edge
    return new DLine (p1,p2);
  }

  DLine LastEdge () {
    //   -----------------
    //
    return new DLine (LastCorner(), NextCorner());
  }

  DLine ThisEdge () {
    //   -----------------
    //
    DPoint p1 = PrevCorner();	// shifts current corner back to start
    DPoint p2 = NextCorner();	// shifts current corner forward
    return new DLine (p1,p2);
  }

  public DRectangle getBB() {
    //        ---------------------
    //
    return boundingbox;
  }


  public boolean equals (DPolygon g) {
    //  -------------------
    //  NOTE: the concept of == defined here is that the two polygons
    //	have exactly the same border.  Starting position is not
    //	relevant.  The direction that the border is stored in
    //	(counterclockwise or clockwise) is also irrelevant.
    //
    int direction = 0;	// do we look the same direction on both
    // polygons, or different ones?
    int firstmatch;	// position of first match of points

    if (size != g.size) return false;	// sizes aren't the same

    firstmatch = 0;
    while ( (firstmatch < size) && (!border[firstmatch].equals(g.border[0])) )
      firstmatch++;
    if (firstmatch==size) return false;	// no match for first point

    // check which direction is the right one
    switch (firstmatch==0 ? 1 : 2) {
    case 1 /* true */: {
      if (border[size-1].equals(g.border[1]))
	direction = -1;
      else	direction = +1;
      break;
    }
    case 2 /* false */: {
      if (border[firstmatch-1].equals(g.border[1]))
	direction = -1;
      else	direction = +1;
      break;
    }
    }

    // check in direction indicated
    for (int i=0; i<size; i++)
      if (!border[ (firstmatch+(i*direction)) % size].equals(g.border[i]))
	return false;		// a point didn't match
    return true;				// all points matched
   
  }

    public static boolean non_self_intersecting(Vector v) { // vector of DPoints, specifies path
	DPoint[] brd = new DPoint[v.size()];
	v.copyInto(brd);
	DLine l1,l2;

	if (brd.length < 4) return true;// border line segments cannot cross each other

	for (int i = 0; i < brd.length - 3; i++) {
	    l1 = new DLine(brd[i], brd[i+1]);
	    for (int j = i+2; j < brd.length - 1; j++) {
		l2 = new DLine(brd[j], brd[j+1]);
		if (l1.intersects(l2))
		    return false;
	    }
	}
	return true;
    }

    public boolean non_self_intersecting () { 
    //  ---------------------
    //
    // this predicate tests the semantic constraints.  All polygons must
    // have non-self-intersecting borders.  Any polygon with a self-intersecting
    // border will invalidate most of the algorithms on polygons.
    //
    // this operation takes time O( n^2)
    //
    int i,j;
    DLine l1,l2;

    if (size < 4) return true;// border line segments cannot cross each other

    i = 0;		// first line is a special case:  do not check
    // against last line.  All other lines must check
    // against last line.
    l1 = new DLine(border[i], border[i+1]);
    for (j = i+2; j<size-1; j++)
      {
	l2 = new DLine(border[j], border[j+1]);
	if (l1.intersects(l2))
	  return false;
      }

    // general loop for all lines except first line
    for (i = 1; i<size-2; i++)
      {
	l1 = new DLine(border[i], border[i+1]);
	for (j = i+2; j<size; j++)
	  {
	    l2 = new DLine(border[j],border[ (j+1) % size ]);
	    if (l1.intersects(l2))
	      return false;
	  }
      }
    return true;
  }


  // ----------------- CONTAINS -----------------


  public boolean contains (DPoint p) {
    //  ----------
    //
    // Tells if point p in polygon g
    //
    int intersections = 0;
    DLine arc, edge;

    if ( !getBB().contains(p) )
      return false;			// no intersection is possible

    double x = getBB().x;
    double y = getBB().y;

    DPoint outside = new DPoint(x - Math.abs(x), y - Math.abs(y));

    arc = new DLine(p, outside);	// draw an arc to outside the box


    for (int i = size-1; i >= 0; --i) {
      edge = Edge(i);
      if (arc.intersects(edge) && !arc.intersects(edge.p2))  
	  intersections++;
    }

    return (intersections % 2 != 0);		// odd means yes; even means no.
  }

  public boolean contains (DLine l) {
    DLine edge;

    if ( !getBB().intersects(l) )
	return false;			// no intersection is possible

    for (int i = size - 1; i >= 0; --i) 	{
      edge = Edge (i);
      
      if (l.intersects (edge)) 
	 return false;	     // we have an intersection
   }

   // now check for wholly enclosed line
   return (contains(l.p1));
  }

  public boolean contains(DRectangle r) {
    //  ----------
    //  
    // Tell if a Rectangle intersects a Polygon
    //
    DLine edge;

    if ( !r.intersects(getBB()) )
	return false;			// no intersection is possible

   // first check to see if any part of the border intersects the rectangle

   for (int i = size-1; i >= 0; --i) {
     edge = Edge(i);
     if (r.intersects(edge))
       return false;		// we have an intersection
   }

   // If it passes through the above loop, no border line intersects
   // the rectangle.  Now we need to check for point in polygon to
   // eliminate wholly enclosing case

   return contains(new DPoint(r.x, r.y));
  }


    public boolean contains(DPath p) {
	for (int i = 0; i < p.Size() - 1; i++)
	    if (!contains(p.Edge(i)))
		return false;
	return true;
    }

    public boolean contains(DPolygon p) {
	for (int i = 0; i < p.Size(); i++)
	    if (!contains(p.Edge(i)))
		return false;
	return true;
    }


  // ------------ INTERSECTS ---------------------


  public boolean intersects(DLine l) {
    //  ----------
    //
    //  Tells if line segment l intersects polygon g
    //
    DLine edge;

    if ( !getBB().intersects(l) )
	return false;			// no intersection is possible

    for (int i = size - 1; i >= 0; --i) 	{

      edge = Edge (i);

      if (l.intersects (edge)) 
	 return true;	     // we have an intersection
   }

   // now check for wholly enclosed line
   return (contains(l.p1));
  }


  public boolean intersects(DRectangle r) {
    //  ----------
    //  
    // Tell if a Rectangle intersects a Polygon
    //
    DLine edge;

    if ( !r.intersects(getBB()) )
	return false;			// no intersection is possible

   if (r.contains(border[0])) 
      return true;  // At least one vertex of g is inside r

   // first check to see if any part of the border intersects the rectangle

   for (int i = size-1; i >= 0; --i) {
     edge = Edge(i);
      if (r.intersects(edge))
	   return true;		// we have an intersection
   }

   // If it passes through the above loop, no border line intersects
   // the rectangle.  Now we need to check for point in polygon to
   // eliminate wholly enclosing case

   return contains(new DPoint(r.x, r.y));
  }

  //------------------ DISTANCE ---------------------


    public double distance(DPoint p) {
	return p.distance(this);
    }

    public double[] distance(DPoint p, double [] k) {
	p.distance(this, k);
	return k;
    }

    public double distance(DLine l) {
	return l.distance(this);
    }

    public double[] distance(DLine l, double[] k) {
	l.distance(this, k);
	return k;
    }


    public double distance(DRectangle l) {
	return l.distance(this);
    }

    public double[] distance(DRectangle l, double[] k) {
	l.distance(this, k);
	return k;
    }
    public double distance(DPolygon l) {
	return l.distance(this);
    }

    public double[] distance(DPolygon l, double[] k) {
	l.distance(this, k);	
	return k;
    }

    public void drawBuffer(Color c, DrawingTarget dt, double dist) {
	// empty till the real algorithm is developed
    }
    


}


