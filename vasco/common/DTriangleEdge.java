
package vasco.common;

import vasco.points.randy.Site;


public class DTriangleEdge implements java.lang.Comparable
{
	public Site start;
	public Site end;
	
	public String toString()
	{
		return "Edge:{"+start.x+","+end.x+"}";
	}
	public DTriangleEdge(Site start,Site end)
	{
		if(new DPointWrapper(start).compareTo(end)<0)
		{
			this.start=start;
			this.end=end;
		}
		else
		{
			this.start=end;
			this.end=start;
		}
	}
	
	public int compareTo(Object o)
	{
		if(o instanceof DTriangleEdge)
		{
			DTriangleEdge e=(DTriangleEdge)o;
			int i=new DPointWrapper(start).compareTo(e.start);
			if(i<0)return i;
			else if(i>0)return i;
			else
			{
				return new DPointWrapper(end).compareTo(e.end);
			}
		}
		else
			return -1;
	}
	
	public boolean equals(Object o)
	{
		return compareTo(o)==0;
	}
}