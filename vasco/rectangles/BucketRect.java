package vasco.rectangles;
/* $Id: BucketRect.java,v 1.2 2004/11/20 22:38:48 brabec Exp $ */
import vasco.common.*;
import java.awt.*;


public class BucketRect extends GenRectTree {
  
    public BucketRect(DRectangle can, int md, int bs, TopInterface p, RebuildTree r) {
	super(can, md, bs, p, r);
    }

    public void reInit(Choice c) {
	super.reInit(c);
	new Bucket(topInterface, "Bucket Capacity", this);
    }

    public String getName() {
	return "Bucket Rectangle Quadtree";
    }

    public boolean orderDependent() {
        return false;
    }

}
