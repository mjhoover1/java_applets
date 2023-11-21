package vasco.points.randy;

// import java.awt.BorderLayout;
// import java.awt.Choice;
// import java.awt.Color;
// import java.awt.Label;
// import java.awt.Panel;
// import java.awt.Rectangle;
// import java.awt.event.ItemEvent;
// import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vasco.common.AppletSwitcher;
import vasco.common.DLine;
import vasco.common.DPoint;
import vasco.common.DRectangle;
import vasco.common.DTriangle;
import vasco.common.DrawingTarget;
import vasco.common.QueryObject;
import vasco.common.RebuildTree;
import vasco.common.SearchVector;
import vasco.common.Tools;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;
import vasco.lines.PM2;
import vasco.points.PointCanvas;
import vasco.points.PointStructure;

public class VoronoiDiagram extends PointStructure implements Indexable {
	DelaunayTriangulation delaunay;
	PointCanvas pc;
	
	boolean showDelaunay=false;
	boolean showPM2=false;
	
	PM2 lineIndex;
	
	public double fixPoint(double x1,double y1,double x2,double y2,double y3)
	{
		if(y3==y1 || y3==y2)return -1;
		if(x2==x1)return x2;
		double x3=((x2-x1)*(y3-y1)/(y2-y1))+x1;
		//System.out.println("x1="+x1+",y1="+y1+",x2="+x2+",y2="+y2+",x3="+x3+",y3="+y3+".");
		return x3;
	}
	
	public ArrayList getClippedLines()
	{
		ArrayList linesToInsert=new ArrayList(); 
		Iterator it=delaunay.triangles.iterator();
		while(it.hasNext())
		{
			DTriangle tri=(DTriangle)it.next();
			//tri.drawCircumcircle(dt);
			for(int i=0;i<tri.adjacencies.length;i++)
			{
				if(tri.adjacencies[i]!=null)
				{
					DPoint p1=tri.circumcircleCenter;
					DPoint p2=tri.adjacencies[i].circumcircleCenter;
					
					if(!pc.can.contains(p1))
					{
						if(pc.can.height<p1.y)p1=new DPoint(fixPoint(p1.x,p1.y,p2.x,p2.y,pc.can.height),pc.can.height);
						if(0>p1.y)p1=new DPoint(fixPoint(p1.x,p1.y,p2.x,p2.y,0),0);
						if(pc.can.width<p1.x)p1=new DPoint(pc.can.width,fixPoint(p1.y,p1.x,p2.y,p2.x,pc.can.width));
						if(0>p1.x)p1=new DPoint(0,fixPoint(p1.y,p1.x,p2.y,p2.x,0));
					}
					if(!pc.can.contains(p2))
					{
						if(pc.can.height<p2.y)p2=new DPoint(fixPoint(p2.x,p2.y,p1.x,p1.y,pc.can.height),pc.can.height);
						if(0>p2.y)p2=new DPoint(fixPoint(p2.x,p2.y,p1.x,p1.y,0),0);
						if(pc.can.width<p2.x)p2=new DPoint(pc.can.width,fixPoint(p2.y,p2.x,p1.y,p1.x,pc.can.width));
						if(0>p2.x)p2=new DPoint(0,fixPoint(p2.y,p2.x,p1.y,p1.x,0));
					}
				
					DLine l=new DLine(p1,p2);
					
					if(pc.can.contains(p1) && pc.can.contains(p2))
						linesToInsert.add(l);
					//else
						//System.out.println(l+" is rejected");
				}
			}
		}
		return linesToInsert;
	}
	
	public void openLinesIndex()
	{
		//IndexWindow iw=new IndexWindow();
		ArrayList linesToInsert=getClippedLines();
		
String[] data=new String[linesToInsert.size()];
		
		for(int i=0;i<linesToInsert.size();i++)
		{
			//<x1> <y1> <x2> <y2>
			DLine line=(DLine)linesToInsert.get(i);
			data[i]=""+line.p1.x+" "+line.p1.y+" "+line.p2.x+" "+line.p2.y;
		}
		
		//make a file name that will avoid some collisions
		long n=Calendar.getInstance().getTimeInMillis();
		String fname="TmpVDFile"+(n%10000)+"n"+(int)(Math.random()*10000);
		
		Tools.writeFile("LINES",fname,data,"SAVE",false);
		
		String[] pnames={"autoloadfile","treetype"};
		String[] ps={fname,"Bucket PR Quadtree"};
		
		AppletSwitcher apper=new AppletSwitcher(Tools.currentApplet,vasco.lines.main.class,pnames,ps);
		apper.start();
	}
	
	public VoronoiDiagram(DRectangle can, TopInterface p, RebuildTree r,PointCanvas pc) {
		super(can, p, r);
		this.pc=pc;
		delaunay=new DelaunayTriangulation(can,p,r);
		lineIndex=new PM2(can,9,p,r);
	    }
	
	public void redoPM2()
	{
		lineIndex.Clear();
		ArrayList lines=getClippedLines();
		Iterator it=lines.iterator();
		while(it.hasNext())
		{
			DLine line=(DLine)it.next();
			lineIndex.Insert(line);
		}
	}
	
	  public SearchVector Nearest(QueryObject p) {
		  return delaunay.Nearest(p);
		  }
	  public SearchVector Nearest(QueryObject p, double dist) {
		  return delaunay.Nearest(p,dist);
		  }
	  
	  public Drawable[] NearestRange(QueryObject p, double dist) {
		  return delaunay.NearestRange(p,dist);
	    }
	  
	  public boolean Insert(DPoint p) {
		    boolean success=delaunay.Insert(p);
		    if(showPM2)redoPM2();
			return success;
		    }
	  
	  public void Delete(DPoint d) {
		  	delaunay.Delete(d);
		  	if(showPM2)redoPM2();
		    }
	  public void DeleteDirect(Drawable d) {
		  	delaunay.DeleteDirect(d);
		  	if(showPM2)redoPM2();
		    }
	  
	  public SearchVector Search(QueryObject q, int mode) {
		  	return delaunay.Search(q,mode);
		  }
	  
	  public Drawable NearestFirst(QueryObject p) {
return delaunay.NearestFirst(p);
		  }
	  
	  public boolean orderDependent() {
	        return false;
	    }
	  
	  public String getName() {
			return "Voronoi Diagram";
		    }
	  
	  public void drawContents(DrawingTarget dt,Rectangle r) 
	  {
		  drawContents(dt,r,Color.black);
	  }
	  
	  public void drawContents(DrawingTarget dt, Rectangle r, Color c) {
		  Iterator it;
			
		  if(showPM2)lineIndex.drawContents(dt,r,Color.LIGHT_GRAY);
		  if(showDelaunay)delaunay.drawContents(dt,r,Color.yellow);
		  
			dt.setColor(c);
			ArrayList clippedLines=getClippedLines();
			it=clippedLines.iterator();
			while(it.hasNext())
			{
				DLine line=(DLine)it.next();
				line.draw(dt);
			}
			dt.setColor(Color.red);
			for(int i=0;i<delaunay.sites.size();i++)
			{
				DPoint dp=(DPoint)delaunay.sites.get(i);
				dp.draw(dt);
			}
			
		}
	  
	  public void Clear() {
			delaunay.Clear();
		}
		
		public void reInit(JComboBox ops) {
			Clear();
		    topInterface.getPanel().removeAll();
		    availOps = ops;
		    availOps.addItem("Insert");
		    availOps.addItem("Move");
		    availOps.addItem("Delete");
		    availOps.addItem("Nearest Site");
		    availOps.addItem("Line Index");
		    availOps.addItem("Vertex Index");
		    new ShowDTCombo(topInterface);
		    new ShowPM2Combo(topInterface);
		  }
		
		public Vector getAllLines()
		{
			Vector toReturn=new Vector();
			Iterator it=delaunay.triangles.iterator();
			while(it.hasNext())
			{
				DTriangle tri=(DTriangle)it.next();
				//tri.drawCircumcircle(dt);
				for(int i=0;i<tri.adjacencies.length;i++)
				{
					if(tri.adjacencies[i]!=null)
					{
						DLine l=new DLine(tri.circumcircleCenter,tri.adjacencies[i].circumcircleCenter);
						toReturn.add(l);
					}
				}
			}
			return toReturn;
		}

		public class ShowDTCombo implements ItemListener
		  {
			  public ShowDTCombo(TopInterface ti) {

				    JPanel maxD = new JPanel();
				    maxD.setLayout(new BorderLayout());
				    maxD.add("West", new JLabel("Show DT"));
				    JComboBox maxDChoice = new JComboBox();
				    maxDChoice.addItem("No");
				    maxDChoice.addItem("Yes");
				    if(showDelaunay)
				    	maxDChoice.select(1);//No is default
				    else
				    	maxDChoice.select(0);//No is default
				    maxD.add("East", maxDChoice);
				    maxDChoice.addItemListener(this);
				    ti.getPanel().add(maxD);
				  }
			  
			  public void itemStateChanged(ItemEvent ie) {
					JComboBox ch = (JComboBox)ie.getSource();
				    if(ch.getSelectedItem().equals("No"))showDelaunay=false;
				    else showDelaunay=true;
				    reb.redraw();
				  }
		  }
		

		public class ShowPM2Combo implements ItemListener
		  {
			  public ShowPM2Combo(TopInterface ti) {

					JPanel maxD = new JPanel();
				    maxD.setLayout(new BorderLayout());
				    maxD.add("West", new JLabel("Show PM2 Index"));
				    JComboBox maxDChoice = new JComboBox();
				    maxDChoice.addItem("No");
				    maxDChoice.addItem("Yes");
				    if(showPM2)
				    	maxDChoice.select(1);//No is default
				    else
				    	maxDChoice.select(0);
				    maxD.add("East", maxDChoice);
				    maxDChoice.addItemListener(this);
				    ti.getPanel().add(maxD);
				  }
			  
			  public void itemStateChanged(ItemEvent ie) {
					JComboBox ch = (JComboBox)ie.getSource();
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
