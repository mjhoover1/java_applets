package vasco.points.randy;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import vasco.common.AppletSwitcher;
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DPointWrapper;
import vasco.common.DPolygon;
import vasco.common.DRectangle;
import vasco.common.DTriangle;
import vasco.common.DTriangleEdge;
import vasco.common.DrawingTarget;
import vasco.common.NNElement;
import vasco.common.QueryObject;
import vasco.common.RebuildTree;
import vasco.common.SearchVector;
import vasco.common.SimpleGenElement;
import vasco.common.Tools;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;
import vasco.lines.PM2;
import vasco.points.PointStructure;


public class DelaunayTriangulation extends PointStructure implements Indexable{
	ArrayList sites;
	TreeSet triangles;
	Site[] fixedPoints;
	
	boolean showPM2=false;
	
	PM2 lineIndex;
	
	public ArrayList getNonFixedSites()
	{
		ArrayList toReturn=new ArrayList();
		toReturn.addAll(sites);
		for(int i=0;i<fixedPoints.length;i++)
		{
			toReturn.remove(fixedPoints[i]);
		}
		return toReturn;
	}
	
	public DelaunayTriangulation(DRectangle can, TopInterface p, RebuildTree r) {
		super(can, p, r);
		lineIndex=new PM2(can,9,p,r);
		init();
	}
	public void Clear() {
		init();
	}
	
	public void openLinesIndex()
	{	
		ArrayList linesToInsert=new ArrayList(); 
		Iterator it=triangles.iterator();
		while(it.hasNext())
		{
			DTriangle tri=(DTriangle)it.next();
			if(!isFixedPoint((Site)tri.getBorder()[0]) && !isFixedPoint((Site)tri.getBorder()[1]))
				linesToInsert.add(new DLine(tri.getBorder()[0],tri.getBorder()[1]));
			if(!isFixedPoint((Site)tri.getBorder()[0]) && !isFixedPoint((Site)tri.getBorder()[2]))
				linesToInsert.add(new DLine(tri.getBorder()[0],tri.getBorder()[2]));
			if(!isFixedPoint((Site)tri.getBorder()[1]) && !isFixedPoint((Site)tri.getBorder()[2]))
				linesToInsert.add(new DLine(tri.getBorder()[1],tri.getBorder()[2]));
		}
		
		String[] data=new String[linesToInsert.size()];
		
		for(int i=0;i<linesToInsert.size();i++)
		{
			//<x1> <y1> <x2> <y2>
			DLine line=(DLine)linesToInsert.get(i);
			data[i]=""+line.p1.x+" "+line.p1.y+" "+line.p2.x+" "+line.p2.y;
		}
		
		//make a file name that will avoid some collisions
		long n=Calendar.getInstance().getTimeInMillis();
		String fname="TmpDTFile"+(n%10000)+"n"+(int)(Math.random()*10000);
		
		Tools.writeFile("LINES",fname,data,"SAVE",false);
		
		String[] pnames={"autoloadfile","treetype"};
		String[] ps={fname,"Bucket PR Quadtree"};
		
		AppletSwitcher apper=new AppletSwitcher(Tools.currentApplet,vasco.lines.main.class,pnames,ps);
		apper.start();
		
		/*LineCanvas lc=null;
		while(lc==null || lc.pstruct==null)
		{
			lc=(LineCanvas)(((vasco.lines.main)apper.getNewApplet()).drawcanvas);
			try{Thread.sleep(100);}catch(Exception e){}
		}
		for(int i=0;i<linesToInsert.size();i++)
		{
			//<x1> <y1> <x2> <y2>
			DLine line=(DLine)linesToInsert.get(i);
			lc.historyList.add(line);
			lc.rebuild();
		}*/
	}
	
	public void reInit(Choice ops) {
	    Clear();
	    topInterface.getPanel().removeAll();
	    availOps = ops;
	    availOps.addItem("Insert");
	    availOps.addItem("Move");
	    availOps.addItem("Delete");
	    availOps.addItem("Nearest Vertex");
	    availOps.addItem("Line Index");
	    new ShowPM2Combo(topInterface);
	  }
	
	private void init()
	{
		sites=new ArrayList();
		triangles=new TreeSet();
		int minval=-10000;
		int maxval=10000;
		int midval=(minval+maxval)/2;
		
		Site[] pts={new Site(new DPoint(minval,minval)),new Site(new DPoint(minval,maxval)),new Site(new DPoint(maxval,midval))};
		sites.add(pts[0]);
		sites.add(pts[1]);
		sites.add(pts[2]);
		fixedPoints=pts;
		try
		{
			DTriangle tri=new DTriangle(pts);
			triangles.add(tri);
			tri.addAllEdges();
		}catch(Exception e){e.printStackTrace(System.out);}
	}
	
	public SearchVector Nearest(QueryObject p) {
		Object[] o=getNearest(p);
		return (SearchVector)o[0];
	}
	public SearchVector Nearest(QueryObject p, double dist) {
//		TODO DT NEAREST 2
		Object[] o=getNearest(p);
		return (SearchVector)o[0];
	}
	
	public Drawable[] NearestRange(QueryObject p, double dist) {
//		TODO DT NEAREST RANGE
		Object[] o=getNearest(p);
		Drawable[] dr=new Drawable[1];
		dr[0]=(Drawable)o[1];
		return dr;
	}
	
	public boolean Insert(DPoint p2) {
		//Assume we start with a valid Delaunay Triangulation...
		
		//Pseudocode:
		//Find triangle abc that contains p, store in arr
		//for(adjacent triangle abd to abc, adjacent triangle acd to abc, adjacent triangle bcd to abc)
		//	if(this triangle's circumcircle contains p)
		//		store this triangle in arr
		// 		recurse on abd
		//form polygon from union of triangles in arr
		//form new triangles in interior of polygon by joining q to all of polygon's vertices
		
		for(int i=0;i<sites.size();i++)
		{
			if(new DPointWrapper((DPoint)sites.get(i)).equals(p2))return false;
		}
		
		Site p=new Site(p2);
		
		DTriangle container=null;
		Iterator it=triangles.iterator();
		while(it.hasNext())
		{
			container=(DTriangle)it.next();
			if(container.contains(p))
				break;
		}
		
		SortedSet visited=new TreeSet();
		visited.add(container);
		ArrayList oldTriangles=getOldTriangles(container,p,visited);//gets the triangles which form the polygon that we need to split

		DPolygon dp=getPolygon(oldTriangles);//gets the outer polygon of the union of these triangles
		
		ArrayList newTriangles=getNewTriangles(dp,p);//forms the new triangles by joining the vertices with p
		
		replaceTriangles(oldTriangles,newTriangles);//fixes up triangle adjacencies... if necessary
		
		sites.add(p);
		
		if(showPM2)redoPM2();
		
		return true;
	}
	
	private void replaceTriangles(ArrayList oldTriangles,ArrayList newTriangles)
	{
		Iterator it=oldTriangles.iterator();
		while(it.hasNext())
			triangles.remove(it.next());
		
		triangles.addAll(newTriangles);
		
		it=newTriangles.iterator();
		while(it.hasNext())
		{
			DTriangle tri=(DTriangle)it.next();
			Iterator it2=oldTriangles.iterator();
			while(it2.hasNext())
			{
				DTriangle old=(DTriangle)it2.next();
				Integer edge=tri.getSharedEdge(old);
				if(edge!=null)
				{
					Integer reverseEdge=old.getSharedEdge(tri);
					DTriangle newAdjacent=old.getAdjacency(reverseEdge.intValue());
					if(newAdjacent!=null)
					{
						Integer doubleReverseEdge=tri.getSharedEdge(newAdjacent);
						if(doubleReverseEdge!=null)
						{
							if(tri.getAdjacency(doubleReverseEdge.intValue())==null)
								tri.setAdjacency(doubleReverseEdge.intValue(),newAdjacent);
							reverseEdge=newAdjacent.getSharedEdge(tri);
							if(reverseEdge!=null)
							{
								newAdjacent.setAdjacency(reverseEdge.intValue(),tri);
							}
						}
					}
					
				}
			}
		}
		
		for(int i=0;i<oldTriangles.size();i++)
		{
			DTriangle tri=(DTriangle)oldTriangles.get(i);
			tri.removeAllEdges();
		}
		for(int i=0;i<newTriangles.size();i++)
		{
			DTriangle tri=(DTriangle)newTriangles.get(i);
			tri.addAllEdges();
		}
	}
	
	private ArrayList getNewTriangles(DPolygon poly,Site p)
	{
		ArrayList toReturn=new ArrayList();
		DTriangle lastTriangle=null;
		DTriangle firstTriangle=null;
		for(int i=0;i<poly.Size();i++)
		{
			Site pt1=(Site)poly.vertex(i);
			Site pt2=(Site)poly.vertex((i+1)%poly.Size());
			Site[] arr={pt1,pt2,p};
			DTriangle dt;
			try{
				dt=new DTriangle(arr);
				toReturn.add(dt);
				if(lastTriangle!=null)
					updateAdjacency(dt,lastTriangle);
				else firstTriangle=dt;
				lastTriangle=dt;
			}
			catch(Exception e){e.printStackTrace(System.out);}
		}
		updateAdjacency(lastTriangle,firstTriangle);
		return toReturn;
	}
	
	private void updateAdjacency(DTriangle t1,DTriangle t2)
	{
		Integer edge=t1.getSharedEdge(t2);
		Integer edge2=t2.getSharedEdge(t1);
				
		t1.setAdjacency(edge.intValue(),t2);
		t2.setAdjacency(edge2.intValue(),t1);
		
		int sum1=0;
		int sum2=0;
		for(int i=0;i<t1.adjacencies.length;i++)
		{
			if(t1.adjacencies[i]!=null)sum1++;
			if(t2.adjacencies[i]!=null)sum2++;
		}
	}
	
	private ArrayList getOldTriangles(DTriangle container,DPoint p,SortedSet visited)
	{
		ArrayList toReturn=new ArrayList();
		toReturn.add(container);
		for(int i=0;i<3;i++)
		{
			DTriangle adjacent=container.adjacencies[i];
			if(adjacent!=null)
			{
				if(!visited.contains(adjacent))
				{
					visited.add(adjacent);
					if(adjacent.circumcircleContains(p))
					{
						ArrayList recursive=getOldTriangles(adjacent,p,visited);
						toReturn.addAll(recursive);
					}
				}
			}
		}
		return toReturn;
	}
	
	private TreeSet getOuterEdges(ArrayList oldTriangles)
	{
		TreeSet toReturn=new TreeSet();
		Iterator it=oldTriangles.iterator();
		while(it.hasNext())
		{
			DTriangle tri=(DTriangle)it.next();
			DPoint[] border=tri.getBorder();
			DTriangleEdge[] e={new DTriangleEdge((Site)border[0],(Site)border[1]),new DTriangleEdge((Site)border[1],(Site)border[2]),new DTriangleEdge((Site)border[2],(Site)border[0])};
			for(int i=0;i<e.length;i++)
			{
				if(toReturn.contains(e[i]))toReturn.remove(e[i]);
				else toReturn.add(e[i]);
			}
		}
		
		return toReturn;
	}
	
	private DPolygon getPolygon(ArrayList oldTriangles)
	{
		//build a polygon out of the vertices of the triangle...
		//need to make sure the polygon isn't self-intersecting
		TreeSet outerEdges=getOuterEdges(oldTriangles);
		TreeMap forward=new TreeMap();
		
		DTriangleEdge curr=null;
		Iterator it=outerEdges.iterator();
		while(it.hasNext())
		{
			DTriangleEdge e=(DTriangleEdge)it.next();
			if(curr==null)curr=e;
			DPointWrapper start=new DPointWrapper(e.start);
			DPointWrapper end=new DPointWrapper(e.end);
			ArrayList tmp=(ArrayList)forward.get(start);
			if(tmp==null)
			{
				tmp=new ArrayList();
				forward.put(start,tmp);
			}
			tmp.add(e);
			
			tmp=(ArrayList)forward.get(end);
			if(tmp==null)
			{
				tmp=new ArrayList();
				forward.put(end,tmp);
			}
			tmp.add(e);
		}
		
		ArrayList points=new ArrayList();
		
		points.add(curr.start);
		DPoint endPoint=curr.start;
		DPoint prevPoint=curr.start;
		DPoint nextPoint=curr.end;
		while(new DPointWrapper(nextPoint).compareTo(endPoint)!=0)
		{
			points.add(nextPoint);
			ArrayList arr=(ArrayList)forward.get(new DPointWrapper(nextPoint));
			DPoint newNextPoint=null;
			for(int i=0;i<arr.size();i++)
			{
				DTriangleEdge e=(DTriangleEdge)arr.get(i);
				if(new DPointWrapper(e.end).compareTo(nextPoint)==0) newNextPoint=e.start;
				else newNextPoint=e.end;
				if(new DPointWrapper(newNextPoint).compareTo(prevPoint)!=0)break;
			}
			prevPoint=nextPoint;
			nextPoint=newNextPoint;
		}
		
		DPoint[] pointArr=new DPoint[points.size()];
		it=points.iterator();
		int i=0;
		while(it.hasNext())
		{
			DPoint p=(DPoint)it.next();
			pointArr[i]=p;
			i++;
		}
		
		DPolygon toReturn=new DPolygon(pointArr);
		return toReturn;
	}
	
	public void Delete(DPoint d2) {
		Object[] o=getNearest(new QueryObject(d2));
		DPoint d=(DPoint)o[1];
		
		//lazy programmer delete...
		ArrayList points=this.sites;
		init();
		
		Iterator it=points.iterator();
		while(it.hasNext())
		{
			boolean add=true;
			DPoint p=(DPoint)it.next();
			for(int i=0;i<fixedPoints.length;i++)
			{
				if(new DPointWrapper(p).equals(fixedPoints[i]))
					add=false;
			}
			if(new DPointWrapper(p).equals(d))
				add=false;
			if(add)
				Insert(p);
		}
		
		if(showPM2)redoPM2();
	}
	public void DeleteDirect(Drawable d) {
		Delete((DPoint)d);
		if(showPM2)redoPM2();
	}
	
	public SearchVector Search(QueryObject q, int mode) {
//		TODO DT SEARCH
		Object[] toReturn=getNearest(q);
		return (SearchVector)toReturn[0];
	}
	
	public Drawable NearestFirst(QueryObject p) {
		Object[] toReturn=getNearest(p);
		return (Site)toReturn[1];
	}
	
	public boolean orderDependent() {
		return false;
	}
	
	public String getName() {
		return "Delaunay Triangulation";
	}
	
	public void redoPM2()
	{
		lineIndex.Clear();
		Iterator it=triangles.iterator();
		while(it.hasNext())
		{
			DTriangle tri=(DTriangle)it.next();
			ArrayList arr=tri.getDrawableLines(fixedPoints);
			Iterator it2=arr.iterator();
			while(it2.hasNext())
			{
				lineIndex.Insert((DLine)it2.next());
			}
		}
	}
	
	public void drawContents(DrawingTarget dt,Rectangle r)
	{
		drawContents(dt,r,Color.black);
	}
	
	public void drawContents(DrawingTarget dt, Rectangle r,Color c) {
		Iterator it;
	
		if(showPM2)lineIndex.drawContents(dt,r,Color.LIGHT_GRAY);
		
		dt.setColor(Color.red);
		for(int i=0;i<sites.size();i++)
		{
			DPoint dp=(DPoint)sites.get(i);
			dp.draw(dt);
		}
		dt.setColor(c);
		it=triangles.iterator();
		while(it.hasNext())
		{
			DTriangle tri=(DTriangle)it.next();
			tri.draw(dt,fixedPoints);
		}
		
	}
	
	
	public Object[] getNearest(QueryObject p)
	{
		Object[] toReturn=new Object[2];
		SearchVector sv=new SearchVector();
		
		if(sites.size()<=3)
		{
			toReturn[0]=sv;
			toReturn[1]=null;
			return toReturn;
		}
		Site curr=(Site)sites.get((int)(Math.random()*(sites.size()-3))+3);
		
		SimpleGenElement sge=new SimpleGenElement();
		NNElement sve;
		
		if(!isFixedPoint(curr))
		{
			sge.addPoint(curr,Color.yellow);
			sve=new NNElement(sge,0,new Vector());
			sv.addElement(sve);
		}
		
		Site closer;
		while((closer=curr.getCloser(p))!=null)
		{
			sge=new SimpleGenElement();
			SimpleGenElement sgetmp=new SimpleGenElement();
			
			sge.addPoint(curr,Color.MAGENTA);
			
			double dist=p.distance(curr);
			for(int i=0;i<curr.edge.size();i++)
			{
				if(!isFixedPoint((Site)curr.edge.get(i)) && !isFixedPoint(curr))
				{
					if(p.distance((Site)curr.edge.get(i))<dist)
					{
						sge.addLine(new DLine(curr,(Site)curr.edge.get(i)),Color.cyan);
						sgetmp.addLine(new DLine(curr,(Site)curr.edge.get(i)),Color.magenta);
					}
					else
						sge.addLine(new DLine(curr,(Site)curr.edge.get(i)),Color.magenta);
				}
			}
			
			sve=new NNElement(sge,0,new Vector());
			sv.addElement(sve);
			
			sgetmp.addLine(curr,closer,Color.yellow);
			sgetmp.addPoint(closer,Color.yellow);
			sve=new NNElement(sgetmp,0,new Vector());
			sv.addElement(sve);
			
			curr=closer;
		}
		
		sge=new SimpleGenElement();
		sge.addPoint(curr,Color.blue);
		sve=new NNElement(sge,0,new Vector());
		sv.addElement(sve);
		
		toReturn[0]=sv;
		toReturn[1]=curr;
		
		return toReturn;
	}
	
	public boolean isFixedPoint(Site s)
	{
		DPointWrapper p=new DPointWrapper(s);
		for(int i=0;i<fixedPoints.length;i++)
		{
			if(p.equals(fixedPoints[i]))return true;
		}
		return false;
	}
	
	public class ShowPM2Combo implements ItemListener
	  {
		  public ShowPM2Combo(TopInterface ti) {

			    Panel maxD = new Panel();
			    maxD.setLayout(new BorderLayout());
			    maxD.add("West", new Label("Show PM2 Index"));
			    Choice maxDChoice = new Choice();
			    maxDChoice.addItem("No");
			    maxDChoice.addItem("Yes");
			    if(showPM2)
			    	maxDChoice.select(1);//No is default
			    else
			    	maxDChoice.select(0);//No is default
			    maxD.add("East", maxDChoice);
			    maxDChoice.addItemListener(this);
			    ti.getPanel().add(maxD);
			  }
		  
		  public void itemStateChanged(ItemEvent ie) {
			    Choice ch = (Choice)ie.getSource();
			    if(ch.getSelectedItem().equals("No"))showPM2=false;
			    else 
			    {
			    	showPM2=true;
			    	redoPM2();
			    }
			    reb.redraw();
			  }
	  }
}
