/* $Id: ArealObject.java,v 1.1.1.1 2002/09/25 05:48:36 brabec Exp $ */
package vasco.common;

public interface ArealObject {
	public boolean contains(DPoint p);

	public boolean contains(DLine p);

	public boolean contains(DRectangle p);

	public boolean contains(DPolygon p);

	public boolean contains(DPath p);
}
