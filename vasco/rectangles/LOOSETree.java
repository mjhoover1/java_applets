package vasco.rectangles;
/* $Id: LOOSETree.java,v 1.1 2007/10/29 01:19:57 jagan Exp $ */
import vasco.common.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import vasco.drawable.*;

public class LOOSETree extends RectangleStructure 
     implements MaxDecompIface, LoosenessFactorIface {

    int maxDecomp;
    double loosenessfactor;
    LOOSEcnode ROOT;

    public LOOSETree(DRectangle can, int md, double loose, 
                     TopInterface p, RebuildTree r) {
	super(can, p, r);
	ROOT = null;
	maxDecomp = md;
        loosenessfactor = loose;
    }

    public void Clear() {
	super.Clear();
	ROOT = null;
    }

    public boolean orderDependent() {
        return false;
    }

    public void reInit(Choice c) {
		System.out.println("c " + c.getItemCount());
		super.reInit(c);
		System.out.println("after c " + c.getItemCount());
		System.out.print("this is " + this);
		new MaxDecomp(topInterface, 9, this);
		new LoosenessFactor(topInterface, 2.0, this);
		availOps.addItem("Motion Insensitivity");
		availOps.addItem("Show Quadtree");
		System.out.print("availOps is " + availOps);
    }

    public boolean Insert(DRectangle P) {
	boolean[] res = new boolean[1];
	ROOT = insert(P, ROOT, wholeCanvas.x + wholeCanvas.width / 2, 
		      wholeCanvas.y + wholeCanvas.height / 2, 
		      wholeCanvas.width / 2, wholeCanvas.height / 2, 
		      maxDecomp, res);

	if (!res[0]) {
	    delete(P, ROOT);
	    ROOT = CompactTree(ROOT);
	}
	
	return res[0];
    }
    
   
    public boolean ReplaceRectangles(DRectangle OldRect, DRectangle NewRect) {

      if (ROOT == null) 
	  return false;

       boolean result = LOOSEReplaceRectangles(ROOT, OldRect, NewRect,
			      wholeCanvas.x + wholeCanvas.width / 2,
			      wholeCanvas.y + wholeCanvas.height / 2,
		              wholeCanvas.width / 2, wholeCanvas.height / 2);
       ROOT = CompactTree(ROOT);

       return result;
    }
    
    
    public DRectangle EnclosingQuadBlock(DRectangle OldRect, boolean nextLevel) {

      if (ROOT == null) 
	  return null;

      return  QuadBlockContaining(ROOT, OldRect,
			          wholeCanvas.x + wholeCanvas.width / 2,
			          wholeCanvas.y + wholeCanvas.height / 2,
		                  wholeCanvas.width / 2,
				  wholeCanvas.height / 2, nextLevel);
    }

    public void Delete(DPoint qu) {
	if (ROOT == null) 
	    return;

	LOOSEIncNearest kdin = new LOOSEIncNearest(ROOT);
	DRectangle mx = kdin.Query(new QueryObject(qu));
	delete(mx, ROOT);
	ROOT = CompactTree(ROOT);
    }


    public void DeleteDirect(Drawable d) {
	if (ROOT == null)
	    return;
	delete((DRectangle)d, ROOT);
	ROOT = CompactTree(ROOT);
    }


    public SearchVector Search(QueryObject r, int mode) {
	SearchVector res = new SearchVector();
	searchVector = new Vector();
	findAll(r, ROOT, wholeCanvas, res, mode);
	return res;
    }

    public SearchVector Nearest(QueryObject p) {
	SearchVector v = new SearchVector();
	if (ROOT != null) {
	    LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
	    mxin.Query(p, v);
	}
	return v;
    }

    public SearchVector Nearest(QueryObject p, double dist) {
	SearchVector v = new SearchVector();
	if (ROOT != null) {
	    LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
	    mxin.Query(p, v, dist, Integer.MAX_VALUE);
	}
	return v;
    }

    public DPoint NearestMXLOOSE(DPoint qu) {
	if (ROOT == null) 
	    return null;
	LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
	return mxin.LOOSEQuery(qu).pnt;
    }


    public Drawable NearestFirst(QueryObject qu) {
	if (ROOT == null) 
	    return null;
	LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
	return mxin.Query(qu);
    }

    public Drawable[] NearestRange(QueryObject qu, double dist) {
	if (ROOT == null) 
	    return null;
	LOOSEIncNearest mxin = new LOOSEIncNearest(ROOT);
	return mxin.Query(qu, dist);
    }


    public String getName() {
	return "Loose Quadtree";
    }

    public void drawContents(DrawingTarget g, Rectangle view) {
	drawC(ROOT, g, wholeCanvas.x + wholeCanvas.width / 2, 
	      wholeCanvas.y + wholeCanvas.height / 2, 
	      wholeCanvas.width / 2, wholeCanvas.height / 2, view);
	drawR(ROOT, g, wholeCanvas.x + wholeCanvas.width / 2, 
	      wholeCanvas.y + wholeCanvas.height / 2, 
	      wholeCanvas.width / 2, wholeCanvas.height / 2, view);
    }
  /* ---------------- interface implementation ---------- */

  public double getLoosenessFactor() {
    return loosenessfactor;
  }

  public void setLoosenessFactor(double b) {
    loosenessfactor = b;
    reb.rebuild();
  }


  /* ---------------- interface implementation ---------- */

  public int getMaxDecomp() {
    return maxDecomp;
  }

  public void setMaxDecomp(int b) {
    maxDecomp = b;
    reb.rebuild();
  }

    //----------------- private ----------------
    class DRectVector {
	private Vector hiddenVector;
	DRectVector() {
	    hiddenVector = new Vector();
	}
	public int size() {
	    return hiddenVector.size();
	}
	public void set(DRectangle r, int i) {
	    hiddenVector.setElementAt(r, i);
	}
	public void append(DRectangle d) {
	    hiddenVector.addElement(d);
	}
	public DRectangle get(int i) {
	    return (DRectangle)hiddenVector.elementAt(i);
	}
	public void remove(int i) {
	    hiddenVector.removeElementAt(i);
	}
    }


    static final int XAXIS = 0;
    static final int YAXIS = 1;
    static final int BOTH = 2;

    static final double xf[] = {-1, 1, -1, 1};
    static final double yf[] = {1, 1, -1, -1};

    static final double vf[] = {-1, 1};

    int OtherAxis(int V) {
	return 1 - V;
    }

    int OpDirection(int V) {
	return 1 - V;
    }
    
    
    /* Search for the old Rectangle. If the enclosing quadtree 
     * block of the old rectangle can still contain the NewRect, 
     * switch OldRect for NewRect and return true.
     * No need to change the structure. Else, return false 
     */

    public boolean LOOSEReplaceRectangles(LOOSEcnode T,
	                      DRectangle OldRect, DRectangle NewRect, 
			      double CX, double CY, double LX, double LY) {

	 if (T == null) return false;
	 
	 DRectangle Box = new DRectangle(CX - loosenessfactor * LX, 
					 CY - loosenessfactor * LY,
					 2 * loosenessfactor * LX, 
					 2 * loosenessfactor * LY);

	 if (T.BIN[YAXIS] != null) 
	   for (int t = 0; t < T.BIN[YAXIS].rv.size(); t++) {
	      if (OldRect.equals(T.BIN[YAXIS].rv.get(t))) {
		     if (Box.contains(NewRect)) { 
		      	  T.BIN[YAXIS].rv.remove(t);
			  T.BIN[YAXIS].rv.append(NewRect);
			  return true;
		     } else return false; 
	      }
	   }


	 for (int i = 0; i < T.NRDIRS; ++i) {
	   if (T.SON[i] == null) continue;
	   boolean t = LOOSEReplaceRectangles(T.SON[i], OldRect, NewRect,
		                              CX + xf[i] * LX/2, CY + yf[i] * LY/2,
					      LX/2, LY/2);
	   if (t) return t;
	 }

	 return false;
    }
    
    public void delete(DRectangle OldRect, LOOSEcnode T) {

	 if (T == null) return;
	  
	 if (T.BIN[YAXIS] != null)
	  for (int t = 0; t < T.BIN[YAXIS].rv.size(); t++)
	     if (OldRect.equals(T.BIN[YAXIS].rv.get(t))) 
		    T.BIN[YAXIS].rv.remove(t);

	 for (int i = 0; i < T.NRDIRS; ++i) {
	   if (T.SON[i] == null) continue;
	   delete(OldRect, T.SON[i]);
	 }

	 return;
    }
       
    public DRectangle expand(DRectangle in) {
	 return new DRectangle(in.x - (loosenessfactor - 1) * in.width / 2, 
			       in.y - (loosenessfactor - 1) * in.height / 2, 
			       loosenessfactor * in.width,
                               loosenessfactor * in.height);
    }
    
    
    public DRectangle QuadBlockContaining(LOOSEcnode T, DRectangle OldRect,
	   				  double CX, double CY, double LX,
					  double LY, boolean nextLevel) {

	 if (T == null) return null;
         boolean axes = false;
	 
	 DRectangle Box = new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY);

	 if (T.BIN[YAXIS] != null) 
	   for (int t = 0; t < T.BIN[YAXIS].rv.size(); t++) {
	      if (OldRect.equals(T.BIN[YAXIS].rv.get(t))) {

		if (!nextLevel) return Box; // If nextLevel is required? 

		DPoint p = new DPoint(OldRect.x + OldRect.width/2,
		      		      OldRect.y + OldRect.height/2);

		for (int i = 0; i < T.NRDIRS; ++i) {
	            DRectangle tt = new DRectangle(CX + xf[i] * LX/2 - LX/2,
				                   CY + yf[i] * LY/2 - LY/2,
					           LX, LY);
		    if (tt.contains(p)) return tt;
		}

		return null;
	      }
	   }

	 for (int i = 0; i < T.NRDIRS; ++i) {
	   if (T.SON[i] == null) continue;
	   DRectangle t = QuadBlockContaining(T.SON[i], OldRect,
		                              CX + xf[i] * LX/2, CY + yf[i] * LY/2,
					      LX/2, LY/2, nextLevel);
	   if (t != null) return t;
	 }

	 return null;
    }
    
    public LOOSEcnode CompactTree(LOOSEcnode T) {

	 if (T == null) return T;
	  
         boolean deleteIt = true;

	 for (int i = 0; i < T.NRDIRS; ++i) {
	   if (T.SON[i] == null) continue;
	   T.SON[i] = CompactTree(T.SON[i]);
	   if (T.SON[i] != null) deleteIt = false;
	 }

	 if (deleteIt & T.BIN[0] != null && T.BIN[0].rv.size() > 0)
	          deleteIt = false;
	 if (deleteIt & T.BIN[1] != null && T.BIN[1].rv.size() > 0)
	          deleteIt = false;
	  
	 if (deleteIt) return null; else return T;

    }

    int LOOSECompare(DRectangle P, double CX, double CY) {
	if (P.x + P.width / 2 < CX)
	    return (P.y + P.height / 2 < CY) ? SW : NW;
	else
	    return (P.y + P.height / 2 < CY) ? SE : NE;
    }

    int BINCompare(DRectangle P, double CV, int V) {
	if (V == XAXIS) {
	    if (P.x <= CV && CV < P.x + P.width)
		return BOTH;
	    else return LEFT;
	} else
	    if (P.y <= CV && CV < P.y + P.height)
		return BOTH;
	    else return LEFT;
    }


    LOOSEcnode insert(DRectangle P, LOOSEcnode R, double CX, 
	              double CY, double LX, double LY, int md, 
		      boolean [] res) {

	LOOSEcnode T;
	int Q;
	int DX, DY;

	if (R == null) R = new LOOSEcnode();
	T = R;
	DX = BINCompare(P, CX, XAXIS);
	DY = BINCompare(P, CY, YAXIS);
	DRectangle  Box;

	double RR = (P.width > P.height)? P.width:P.height;

	while (LX * loosenessfactor > RR) {

	    Q = LOOSECompare(P, CX, CY);

	    Box = new DRectangle(CX + xf[Q] * LX/2 - loosenessfactor * LX/2,
		                 CY + yf[Q] * LY/2 - loosenessfactor * LY/2,
			         loosenessfactor * LX, loosenessfactor * LY);


	    if (Box.contains(P)) { 

	    	if (T.SON[Q] == null)
		    T.SON[Q] = new LOOSEcnode();
	        T = T.SON[Q];
	    	LX /= 2;
	    	CX += xf[Q] * LX;
	    	LY /= 2;
	    	CY += yf[Q] * LY;
	    	DX = BINCompare(P, CX, XAXIS);
	    	DY = BINCompare(P, CY, YAXIS);
	   	md--;

	    } else { break;}
	}

	InsertAxis(P, T, CY, LY, YAXIS);	
	res[0] = md > 0;
	return R;
    }

 void InsertAxis(DRectangle P, LOOSEcnode R, double CV, double LV, int V) {

	LOOSEbnode T;
	int D;

	T = R.BIN[V];
	if (T == null) 
	    T = R.BIN[V] = new LOOSEbnode();
	T.rv.append(P);
    }


    Vector searchVector;


    void findAll(QueryObject searchRect, LOOSEcnode R, DRectangle block, SearchVector v, int mode) {
	// searchRect ... external rectangle to be compared with others stored in the quadtree
	// R ... root; CX, CY ... center of region of size LX, LY
	int Q;
	final double xf[] = {0, 0.5, 0, 0.5};
	final double yf[] = {0.5, 0.5, 0, 0};

	if (!searchRect.intersects(block))
	    return;
 
	v.addElement(new SVElement(new YellowBlock(block, R == null), searchVector));

	if (R == null)
	    return;

	//    rectangleInOut(cur, searchRect.toRectangle(), mode, v, searchVector);

	CrossAll(searchRect, R.BIN[YAXIS], block.y + block.height / 2, block.height / 2, YAXIS, v, mode);
	CrossAll(searchRect, R.BIN[XAXIS], block.x + block.width / 2, block.width / 2, XAXIS, v, mode);

	for (Q = 3; Q >= 0; Q--) {
	    DRectangle dr = new DRectangle(block.x + block.width * xf[Q], block.y + block.height * yf[Q],
					   block.width / 2, block.height / 2);
	    searchVector.addElement(dr);
	}
	for (Q = 0; Q < 4; Q++) {
	    DRectangle dr = new DRectangle(block.x + block.width * xf[Q], block.y + block.height * yf[Q],
					   block.width / 2, block.height / 2);
	    searchVector.removeElementAt(searchVector.size() - 1);
	    findAll(searchRect, R.SON[Q], dr, v, mode);
	}
    }

    void CrossAll(QueryObject P, LOOSEbnode R, double CV, double LV,
	          int V, SearchVector v, int mode) {
	int D;
	if (R == null) 
	    return;
	for (int i = 0; i < R.rv.size(); i++) {
	    drawableInOut(P, R.rv.get(i), mode, v, searchVector);
	}
	D = BINCompare(P.getBB(), CV, V);
	LV /= 2;
	if (D == BOTH) {
	    CrossAll(P, R.SON[LEFT], CV-LV, LV, V, v, mode);
	    CrossAll(P, R.SON[RIGHT], CV+LV, LV, V, v, mode);
	} else 
	    CrossAll(P, R.SON[D], CV+vf[D]*LV, LV, V, v, mode);
    }

    // ---- drawing routines


    void drawRectangles(LOOSEbnode r, DrawingTarget g) {
	if (r == null)
	    return;
	DRectVector t = r.rv;
	for (int i = 0; i < t.size(); i++)
	    t.get(i).draw(g);
	drawRectangles(r.SON[0], g);
	drawRectangles(r.SON[1], g);
    }
    
    void drawR(LOOSEcnode r, DrawingTarget g, double CX, double CY, 
	       double LX, double LY, Rectangle view) {

	if (!g.visible(new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY)))
	    return;


	if (r == null)
	    return;

	for (int i = 0; i < r.NRDIRS; i++) {
	    drawR(r.SON[i], g, CX + xf[i]*LX/2,
		  CY+yf[i]*LY/2, LX / 2, LY / 2, view);
	}
	
	// Draw Rectangles in RED
	g.setColor(Color.red);
	drawRectangles(r.BIN[0], g);
	drawRectangles(r.BIN[1], g);
    }

    void drawC(LOOSEcnode r, DrawingTarget g, double CX, double CY, 
	       double LX, double LY, Rectangle view) {

	if (!g.visible(new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY)))
	    return;

	if (r == null)
	    return;

	boolean drawAxes = false;

	for (int i = 0; i < r.NRDIRS; i++) {

	    if (r.SON[i] != null) drawAxes = true; 
	    
	    drawC(r.SON[i], g, CX + xf[i]*LX/2,
		  CY+yf[i]*LY/2, LX / 2, LY / 2, view);
	}
	
        if (drawAxes) 
	for (int i = 0; i < r.NRDIRS; i++) {
	    g.setColor(Color.black);
	    g.drawRect(CX + xf[i]*LX/2 - LX/2, CY + yf[i]*LY/2 - LY/2, LX, LY);
	}
    }

    void drawBintree(DPoint p, DrawingTarget dt) {
	// p .. in wholeCanvas coordinates;  
	// to locate the apropriate bintree find the nearest node in the quadtree to 'p'
	if (ROOT == null)
	    return;
	LOOSEIncNearest kdin = new LOOSEIncNearest(ROOT);
	NearestINode mx = kdin.LOOSEQuery(p);

	dt.setColor(Color.blue);
	mx.pnt.draw(dt);

	new BinTreeFrame(mx.cnode, dt);
    }

    //----------------------------------------------

    class BinTreeFrame extends Frame implements ActionListener {
	ScrollPane sp;
	Button close;

	class BinTreeCanvas extends Canvas {
	    LOOSEcnode cn;
	    DrawingTarget dt;
	    int bintreecounter;

	    BinTreeCanvas(LOOSEcnode c, int w, int h, DrawingTarget dt) {
		setSize(w, h);
		cn = c;
		this.dt = dt;
		bintreecounter = 1;
		dt.setColor(Color.blue);
		drawBinRectangles(cn.BIN[0], dt);
		drawBinRectangles(cn.BIN[1], dt);
	    }

	    public void paint(Graphics g) {	
		bintreecounter = 1;
		g.setColor(Color.blue);
		g.drawString("x axis bintree", 10, 10); 
		g.drawString("y axis bintree", 10, getSize().height / 2);
		drawBinTree(cn.BIN[0], g, getSize().width / 2, 10, getDepth(cn.BIN[0], 0), 1.0);
		drawBinTree(cn.BIN[1], g, getSize().width / 2, getSize().height / 2, getDepth(cn.BIN[1], 0), 1.0);
	    }

	    void drawBinRectangles(LOOSEbnode r, DrawingTarget dt) {
		if (r == null)
		    return;	

		if (r.SON[0] != null) {
		    drawBinRectangles(r.SON[0], dt);
		}
		for (int i = 0; i < r.rv.size(); i++) {
		    DRectangle cr = r.rv.get(i);
		    dt.drawString(String.valueOf(bintreecounter++), 
				  cr.x + cr.width / 2, 
				  cr.y + cr.height / 2,
				  new Font("Helvetica",Font.PLAIN,20));
		}
		if (r.SON[1] != null) {
		    drawBinRectangles(r.SON[1], dt);
		}
	    }

	    void drawBinTree(LOOSEbnode r, Graphics g, int curx, int cury, int depth, double factor) {
		if (r == null)
		    return;	

		if (r.SON[0] != null) {
		    drawBinTree(r.SON[0], g, curx - (int)(10 * Math.pow(2, depth - 2) / factor), 
				cury + 30, depth - 1, factor);
		    g.drawLine(curx, cury, curx - (int)(10 * Math.pow(2, depth - 2) / factor), cury + 30);
		}
		if (r.rv.size() > 0) {
		    String s = "";
		    g.fillOval(curx - 3, cury - 3, 6, 6);
		    for (int i = 0; i < r.rv.size(); i++) {
			s += String.valueOf(bintreecounter++) + "; ";
		    }
		    g.drawString(s, curx, cury - 3);
		} else {
		    g.drawOval(curx - 3, cury - 3, 6, 6);
		}
		if (r.SON[1] != null) {
		    drawBinTree(r.SON[1], g, curx + (int)(10* Math.pow(2, depth - 2) / factor), 
				cury + 30, depth - 1, factor);
		    g.drawLine(curx, cury, curx + (int)(10 * Math.pow(2, depth - 2) / factor), cury + 30);
		}
	    }
	}
	int getDepth(LOOSEbnode r, int cur) {
	    if (r == null)
		return cur;
	    return Math.max(getDepth(r.SON[0], cur + 1), getDepth(r.SON[1], cur + 1));
	}

	int getTreeWidth(int depth) {
	    return (int)(20 * (Math.pow(2, depth - 1) - 1));
	}

	BinTreeFrame(LOOSEcnode cn, DrawingTarget dt) {
	    super("Bintrees");
	    setLayout(new BorderLayout());
	    int width = Math.max(getTreeWidth(getDepth(cn.BIN[0], 0)), 
				 getTreeWidth(getDepth(cn.BIN[1], 0)));
	    int height = 30 * (getDepth(cn.BIN[0], 0) + getDepth(cn.BIN[1], 0));
	    
	    BinTreeCanvas bt = new BinTreeCanvas(cn, width, height, dt);
	    sp = new ScrollPane();
	    sp.setSize(300, 300);
	    add("Center", sp);
	    sp.add(bt);
	    close = new Button("Close");
	    close.addActionListener(this);
	    add("South", close);
	    pack();
	    show();
	}
	
	public void actionPerformed(ActionEvent e) {
	    Button src = (Button)e.getSource();
	    if (src == close)
		dispose();
	}


    }


    //---------------- inc nearest -----------------


    class LOOSEQueueElement {
	double [] keys;

	LOOSEQueueElement(double[] k) {
	    keys = k;
	}
    }

    class LOOSEQLeaf extends LOOSEQueueElement {
	DRectangle rec;

	LOOSEQLeaf(double[] k, DRectangle dr) {
	    super(k);
	    rec = dr;
	}
    }

    class LOOSEQINode extends LOOSEQueueElement {
	LOOSEcnode r;          // represents tree node
	DRectangle block;

	LOOSEQINode(double[] k, LOOSEcnode p, DRectangle b) {
	    super(k);
	    r = p;
	    block = b;
	}
    }

    class NearestINode extends LOOSEQLeaf {
	LOOSEcnode cnode;
	DPoint pnt;
	NearestINode(double[] k, LOOSEcnode cn, DPoint p) {
	    super(k, null);
	    cnode = cn;
	    pnt = p;
	}
    }


    class LOOSEIncNearest {

	class LOOSEQueue {
	    Vector v;

	    LOOSEQueue() {
		v = new Vector();
	    }

	    void Enqueue(LOOSEQueueElement qe) {
		v.addElement(qe);
		for (int i = v.size() - 1; i > 0; i--) {
		    LOOSEQueueElement q1 = (LOOSEQueueElement)v.elementAt(i - 1);
		    LOOSEQueueElement q2 = (LOOSEQueueElement)v.elementAt(i);
		    if (q1.keys[0] > q2.keys[0] || 
			(q1.keys[0] == q2.keys[0] && 
			 ((q1.keys[1] > q2.keys[1] && q1 instanceof LOOSEQLeaf && q2 instanceof LOOSEQLeaf) || 
			  (q1 instanceof LOOSEQLeaf && !(q2 instanceof LOOSEQLeaf))))) {
			v.setElementAt(q2, i - 1);
			v.setElementAt(q1, i);
		    }
		}
	    }

	    LOOSEQueueElement First() {
		LOOSEQueueElement q = (LOOSEQueueElement)v.elementAt(0);
		return q;
	    }

	    void DeleteFirst() {
		v.removeElementAt(0);
	    }

	    LOOSEQueueElement Dequeue() {
		LOOSEQueueElement q = (LOOSEQueueElement)v.elementAt(0);
		v.removeElementAt(0);
		return q;
	    }

	    void EnqueueBinTree(QueryObject qu, LOOSEbnode r) {
		if (r == null)
		    return;
		for (int i = 0; i < r.rv.size(); i++) {
		    double[] dist = new double[2];
		    qu.distance(r.rv.get(i), dist);
		    Enqueue(new LOOSEQLeaf(dist, r.rv.get(i)));
		}
		for (int i = 0; i < 2; i++)
		    EnqueueBinTree(qu, r.SON[i]);
	    }

	    boolean isEmpty() {
		return (v.size() == 0);
	    }

	    Vector makeVector() {
		Vector r = new Vector();
		for (int i = 0; i < v.size(); i++) {
		    LOOSEQueueElement q = (LOOSEQueueElement)v.elementAt(i);
		    if (q instanceof LOOSEQLeaf)
			r.addElement(new GreenRect(((LOOSEQLeaf)q).rec));	
		    else
			r.addElement(new QueueBlock(expand(((LOOSEQINode)q).block)));
		}
		return r;
	    }
	}

	LOOSEQueue q;

	LOOSEIncNearest(LOOSEcnode rt) {
	    q = new LOOSEQueue();
	    double[] dist = new double[2];
	    dist[0] = dist[1] = 0.0;
	    q.Enqueue(new LOOSEQINode(dist, rt, wholeCanvas));
	}

	DRectangle Query(QueryObject qu) {
	    DRectangle[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
	    return (ar.length == 0) ? null : ar[0];
	}

	void Query(QueryObject qu, SearchVector v) {
	    Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
	}

	DRectangle[] Query(QueryObject qu, double dist) {
	    return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
	}


       DRectangle expand(DRectangle in) {
	 return new DRectangle(in.x - (loosenessfactor - 1) * in.width / 2, 
			       in.y - (loosenessfactor - 1) * in.height / 2, 
			       loosenessfactor * in.width,
                               loosenessfactor * in.height);
       }



	DRectangle[] Query(QueryObject qu, SearchVector ret, double dist,
	                   int nrelems) {

	    Vector rect = new Vector();
	    final double xf[] = {0, .5, 0, .5};
	    final double yf[] = {.5, .5, 0, 0};
	    int counter = 1;
	    
	    while(!q.isEmpty()) {
		LOOSEQueueElement element = q.Dequeue();
		if (element instanceof LOOSEQLeaf) {
		    LOOSEQLeaf ql = (LOOSEQLeaf)element;
		    if (nrelems-- <= 0 || qu.distance(ql.rec) > dist)
			break;
		    rect.addElement(ql.rec);
		    ret.addElement(new NNElement(new NNDrawable(ql.rec, counter++), ql.keys[0], q.makeVector()));
		} else {
		    LOOSEQINode in = (LOOSEQINode)element;
		    ret.addElement(new NNElement(new YellowBlock(expand(in.block), false), in.keys[0], q.makeVector()));
		    q.EnqueueBinTree(qu, in.r.BIN[0]);
		    q.EnqueueBinTree(qu, in.r.BIN[1]);
	    
		    for (int i = 0; i < 4; i++) 
			if (in.r.SON[i] != null) {
			    DRectangle dr1 = new DRectangle(in.block.x + 
                                                           xf[i] * in.block.width, 
							   in.block.y +
                                                           yf[i] * in.block.height,
							   in.block.width / 2,
                                                           in.block.height / 2);
			    DRectangle dr2 = new DRectangle(
                                in.block.x + xf[i] * in.block.width 
					- (loosenessfactor - 1)  * in.block.width / 4, 
			        in.block.y + yf[i] * in.block.height 
					- (loosenessfactor - 1)  * in.block.height / 4, 
				loosenessfactor * in.block.width / 2,
                                loosenessfactor * in.block.height / 2);
			    double[] keys = new double[2];
			    qu.distance(dr2, keys);
			    q.Enqueue(new LOOSEQINode(keys, in.r.SON[i], dr1));
			}
		}
	    }
	    DRectangle[] ar = new DRectangle[rect.size()];
	    rect.copyInto(ar);
	    return ar;
	}
   
	NearestINode LOOSEQuery(DPoint qu) {

	    /* Notice how the INN algorithm has to be changed in order
               to work on LooseQuadtrees
	     */

	    final double xf[] = {0, .5, 0, .5};
	    final double yf[] = {.5, .5, 0, 0};
	
	    while(!q.isEmpty()) {
		LOOSEQueueElement element = q.Dequeue();

		if (element instanceof NearestINode) {
		    return (NearestINode)element;
		} else {
		    LOOSEQINode in = (LOOSEQINode)element;
		    double[] lkeys = new double[2];
		    DPoint dp = new DPoint(in.block.x + in.block.width / 2, 
					   in.block.y + in.block.height / 2);
		    qu.distance(dp, lkeys); 
		    q.Enqueue(new NearestINode(lkeys, in.r, dp)); 
		    for (int i = 0; i < 4; i++) 
			if (in.r.SON[i] != null) {
			    DRectangle dr1 = new DRectangle(
                                in.block.x + xf[i] * in.block.width, 
			        in.block.y + yf[i] * in.block.height,
				in.block.width / 2, in.block.height / 2);
			    DRectangle dr2 = new DRectangle(
                                in.block.x + xf[i] * in.block.width 
					- (loosenessfactor - 1)  * in.block.width / 4, 
			        in.block.y + yf[i] * in.block.height 
					- (loosenessfactor - 1) * in.block.height / 4, 
				loosenessfactor * in.block.width / 2,
                                loosenessfactor * in.block.height / 2);
			    double[] keys = new double[2];
			    qu.distance(dr2, keys);
			    q.Enqueue(new LOOSEQINode(keys, in.r.SON[i], dr1));
			}
		}
	    }
	    return null;
 
	}

    }
  

    class LOOSEbnode {
	LOOSEbnode[] SON;
	DRectVector rv;

	LOOSEbnode () {
	    SON = new LOOSEbnode[2];
	    SON[0] = SON[1] = null;
	    rv = new DRectVector();
	}
    }

    class LOOSEcnode {
	final int NRDIRS = 4;

	LOOSEbnode BIN[] = new LOOSEbnode[2]; // indexed by 'x' and 'y' coordinates
	LOOSEcnode SON[] = new LOOSEcnode[NRDIRS];

	LOOSEcnode() {
	    BIN[0] = BIN[1] = null;
	    SON[0] = SON[1] = SON[2] = SON[3] = null;
	}
    }

}
