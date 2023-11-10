package vasco.rectangles;
/* $Id: CIFTree.java,v 1.3 2003/09/05 16:33:12 brabec Exp $ */
import vasco.common.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import vasco.drawable.*;

public class CIFTree extends RectangleStructure implements MaxDecompIface {

    int maxDecomp;
    CIFcnode ROOT;

    public CIFTree(DRectangle can, int md, TopInterface p, RebuildTree r) {
	super(can, p, r);
	ROOT = null;
	maxDecomp = md;
    }

    public void Clear() {
	super.Clear();
	ROOT = null;
    }

    public boolean orderDependent() {
        return false;
    }

    public void reInit(Choice c) {
	super.reInit(c);
	new MaxDecomp(topInterface, 9, this);
	availOps.addItem("Show bintree");
    }

    public boolean Insert(DRectangle P) {
	boolean[] res = new boolean[1];
	ROOT = insert(P, ROOT, wholeCanvas.x + wholeCanvas.width / 2, 
		      wholeCanvas.y + wholeCanvas.height / 2, 
		      wholeCanvas.width / 2, wholeCanvas.height / 2, 
		      maxDecomp, res);
	if (!res[0])
	    ROOT = delete(P, ROOT, wholeCanvas.x + wholeCanvas.width / 2, 
			  wholeCanvas.y + wholeCanvas.height / 2, 
			  wholeCanvas.width / 2, wholeCanvas.height / 2);
	return res[0];
    }

    public void Delete(DPoint qu) {
	if (ROOT == null) 
	    return;

	CIFIncNearest kdin = new CIFIncNearest(ROOT);
	DRectangle mx = kdin.Query(new QueryObject(qu));
	ROOT = delete(mx, ROOT, wholeCanvas.x + wholeCanvas.width / 2, 
		      wholeCanvas.y + wholeCanvas.height / 2, 
		      wholeCanvas.width / 2, wholeCanvas.height / 2);
    }


    public void DeleteDirect(Drawable d) {
	if (ROOT == null)
	    return;
	ROOT = delete((DRectangle)d, ROOT, wholeCanvas.x + wholeCanvas.width / 2, 
		      wholeCanvas.y + wholeCanvas.height / 2, 
		      wholeCanvas.width / 2, wholeCanvas.height / 2);
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
	    CIFIncNearest mxin = new CIFIncNearest(ROOT);
	    mxin.Query(p, v);
	}
	return v;
    }

    public SearchVector Nearest(QueryObject p, double dist) {
	SearchVector v = new SearchVector();
	if (ROOT != null) {
	    CIFIncNearest mxin = new CIFIncNearest(ROOT);
	    mxin.Query(p, v, dist, Integer.MAX_VALUE);
	}
	return v;
    }

    public DPoint NearestMXCIF(DPoint qu) {
	if (ROOT == null) 
	    return null;
	CIFIncNearest mxin = new CIFIncNearest(ROOT);
	return mxin.CIFQuery(qu).pnt;
    }


    public Drawable NearestFirst(QueryObject qu) {
	if (ROOT == null) 
	    return null;
	CIFIncNearest mxin = new CIFIncNearest(ROOT);
	return mxin.Query(qu);
    }

    public Drawable[] NearestRange(QueryObject qu, double dist) {
	if (ROOT == null) 
	    return null;
	CIFIncNearest mxin = new CIFIncNearest(ROOT);
	return mxin.Query(qu, dist);
    }


    public String getName() {
	return "MX-CIF Quadtree";
    }

    public void drawContents(DrawingTarget g, Rectangle view) {
	drawC(ROOT, g, wholeCanvas.x + wholeCanvas.width / 2, 
	      wholeCanvas.y + wholeCanvas.height / 2, 
	      wholeCanvas.width / 2, wholeCanvas.height / 2, view);
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

    int CIFCompare(DRectangle P, double CX, double CY) {
	if (P.x + P.width / 2 < CX)
	    return (P.y + P.height / 2 < CY) ? SW : NW;
	else
	    return (P.y + P.height / 2 < CY) ? SE : NE;
    }

    int BINCompare(DRectangle P, double CV, int V) {
	if (V == XAXIS) {
	    if (P.x <= CV && CV < P.x + P.width)
		return BOTH;
	    else
		return (CV < P.x + P.width / 2) ? RIGHT : LEFT;
	} else
	    if (P.y <= CV && CV < P.y + P.height)
		return BOTH;
	    else
		return (CV < P.y + P.height / 2) ? RIGHT : LEFT;
    }
		

    CIFcnode insert(DRectangle P, CIFcnode R, double CX, double CY, double LX, double LY, int md, 
		    boolean [] res) {
	CIFcnode T;
	int Q;
	int DX, DY;

	if (R == null)
	    R = new CIFcnode();
	T = R;
	DX = BINCompare(P, CX, XAXIS);
	DY = BINCompare(P, CY, YAXIS);

	while (DX != BOTH && DY != BOTH) {
	    Q = CIFCompare(P, CX, CY);
	    if (T.SON[Q] == null)
		T.SON[Q] = new CIFcnode();
	    T = T.SON[Q];
	    LX /= 2;
	    CX += xf[Q] * LX;
	    LY /= 2;
	    CY += yf[Q] * LY;
	    DX = BINCompare(P, CX, XAXIS);
	    DY = BINCompare(P, CY, YAXIS);
	    md--;
	}
	if (DX == BOTH)
	    InsertAxis(P, T, CY, LY, YAXIS);	
	else
	    InsertAxis(P, T, CX, LX, XAXIS);

	res[0] = md > 0;
	return R;
    }

    void InsertAxis(DRectangle P, CIFcnode R, double CV, double LV, int V) {

	CIFbnode T;
	int D;

	T = R.BIN[V];
	if (T == null) 
	    T = R.BIN[V] = new CIFbnode();
	D = BINCompare(P, CV, V);
	while (D != BOTH) {
	    if (T.SON[D] == null)
		T.SON[D] = new CIFbnode();
	    T = T.SON[D];
	    LV /= 2;
	    CV += vf[D] * LV;
	    D = BINCompare(P, CV, V);
	}
	T.rv.append(P);
    }


    CIFcnode delete(DRectangle P, CIFcnode R, double CX, double CY, double LX, double LY) {
	CIFcnode T, FT, RB, tempc;
	CIFbnode B, FB, tempb, TB;
	int Q, QF = -1;
	int D, DF = -1;
	int V;
	double CV, LV;
    
	if (R == null)
	    return null;
	T = R;
	FT = null;
	while (BINCompare(P, CX, V = XAXIS) != BOTH && BINCompare(P, CY, V = YAXIS) != BOTH) {
	    Q = CIFCompare(P, CX, CY);	
	    if (T.SON[Q] == null)
		return R;
	    if (T.SON[CQuad(Q)] != null || T.SON[OpQuad(Q)] != null || 
		T.SON[CCQuad(Q)] != null || T.BIN[XAXIS] != null || T.BIN[YAXIS] != null) {
		FT = T;
		QF = Q;
	    }
	    T = T.SON[Q];
	    LX /= 2;
	    CX += xf[Q] * LX;	
	    LY /= 2;
	    CY += yf[Q] * LY;
	}
	V = OtherAxis(V);
	RB = T;
	FB = null;
	B = T.BIN[V];
	CV = V == XAXIS ? CX : CY;
	LV = V == XAXIS ? LX : LY;
	D = BINCompare(P, CV, V);	
	while (B != null && D != BOTH) {
	    if (B.SON[OpDirection(D)] != null || B.rv.size() != 0) {
		FB = B;
		DF = D;
	    }
	    B = B.SON[D];
	    LV /= 2;
	    CV += vf[D] * LV;
	    D = BINCompare(P, CV, V);
	}
	if (B == null) // no rectangle at all
	    return R;

    del_loop: {
	    for (int t = 0; t < B.rv.size(); t++) {
		if (P.equals(B.rv.get(t))) {
		    B.rv.remove(t);
		    break del_loop;
		}
	    }
	    return R;
	}

	if (B.rv.size() > 0)
	    return R;  // elements left in the list
	else
	    if (B.SON[LEFT] == null && B.SON[RIGHT] == null) {
		TB = FB == null ? RB.BIN[V] : FB.SON[DF];
		D = LEFT;
		while (TB != B) {
		    if (TB.SON[D] == null)
			D = OpDirection(D);
		    tempb = TB.SON[D];
		    TB.SON[D] = null;
		    TB = tempb;
		}
		if (FB != null)
		    FB.SON[DF] = null;
		else {
		    RB.BIN[V] = null;
		    if (RB.BIN[OtherAxis(V)] != null || RB.SON[NW] != null || 
			RB.SON[NE] != null || RB.SON[SW] != null || RB.SON[SE] != null)
			return R;
		    T = FT == null ? R : FT.SON[QF];
		    while (T != null) {
			for (Q = 0; Q < 4; Q++) {
			    if (T.SON[Q] != null)
				break;
			}
			if (Q < 4) {
			    tempc = T.SON[Q];
			    T.SON[Q] = null;
			    T = tempc;
			} else {
			    T = null;
			    //System.err.println("WARNING: maybe shouldn't run through here");
			}
		    }
		    if (FT == null)
			R = null;
		    else
			FT.SON[QF] = null;
		}
	    }
	return R;
    }

    Vector searchVector;


    void findAll(QueryObject searchRect, CIFcnode R, DRectangle block, SearchVector v, int mode) {
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

    void CrossAll(QueryObject P, CIFbnode R, double CV, double LV, int V, SearchVector v, int mode) {
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


    void drawRectangles(CIFbnode r, DrawingTarget g) {
	if (r == null)
	    return;
	DRectVector t = r.rv;
	for (int i = 0; i < t.size(); i++)
	    t.get(i).draw(g);
	drawRectangles(r.SON[0], g);
	drawRectangles(r.SON[1], g);
    }

    void drawC(CIFcnode r, DrawingTarget g, double CX, double CY, double LX, double LY, Rectangle view) {
	if (!g.visible(new DRectangle(CX - LX, CY - LY, 2 * LX, 2 * LY)))
	    return;

	g.setColor(Color.black);
	g.drawRect(CX - LX, CY - LY, 2 * LX, 2 * LY);

	if (r == null)
	    return;

	g.setColor(Color.red);
	drawRectangles(r.BIN[0], g);
	drawRectangles(r.BIN[1], g);


	for (int i = 0; i < r.NRDIRS; i++) {
	    drawC(r.SON[i], g, CX + xf[i]*LX/2, CY+yf[i]*LY/2, LX / 2, LY / 2, view);
	}
    }

    void drawBintree(DPoint p, DrawingTarget dt) {
	// p .. in wholeCanvas coordinates;  
	// to locate the apropriate bintree find the nearest node in the quadtree to 'p'
	if (ROOT == null)
	    return;
	CIFIncNearest kdin = new CIFIncNearest(ROOT);
	NearestINode mx = kdin.CIFQuery(p);

	dt.setColor(Color.blue);
	mx.pnt.draw(dt);

	new BinTreeFrame(mx.cnode, dt);
    }

    //----------------------------------------------

    class BinTreeFrame extends Frame implements ActionListener {
	ScrollPane sp;
	Button close;

	class BinTreeCanvas extends Canvas {
	    CIFcnode cn;
	    DrawingTarget dt;
	    int bintreecounter;

	    BinTreeCanvas(CIFcnode c, int w, int h, DrawingTarget dt) {
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

	    void drawBinRectangles(CIFbnode r, DrawingTarget dt) {
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

	    void drawBinTree(CIFbnode r, Graphics g, int curx, int cury, int depth, double factor) {
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
	int getDepth(CIFbnode r, int cur) {
	    if (r == null)
		return cur;
	    return Math.max(getDepth(r.SON[0], cur + 1), getDepth(r.SON[1], cur + 1));
	}

	int getTreeWidth(int depth) {
	    return (int)(20 * (Math.pow(2, depth - 1) - 1));
	}

	BinTreeFrame(CIFcnode cn, DrawingTarget dt) {
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


    class CIFQueueElement {
	double [] keys;

	CIFQueueElement(double[] k) {
	    keys = k;
	}
    }

    class CIFQLeaf extends CIFQueueElement {
	DRectangle rec;

	CIFQLeaf(double[] k, DRectangle dr) {
	    super(k);
	    rec = dr;
	}
    }

    class CIFQINode extends CIFQueueElement {
	CIFcnode r;          // represents tree node
	DRectangle block;

	CIFQINode(double[] k, CIFcnode p, DRectangle b) {
	    super(k);
	    r = p;
	    block = b;
	}
    }

    class NearestINode extends CIFQLeaf {
	CIFcnode cnode;
	DPoint pnt;
	NearestINode(double[] k, CIFcnode cn, DPoint p) {
	    super(k, null);
	    cnode = cn;
	    pnt = p;
	}
    }


    class CIFIncNearest {

	class CIFQueue {
	    Vector v;

	    CIFQueue() {
		v = new Vector();
	    }

	    void Enqueue(CIFQueueElement qe) {
		v.addElement(qe);
		for (int i = v.size() - 1; i > 0; i--) {
		    CIFQueueElement q1 = (CIFQueueElement)v.elementAt(i - 1);
		    CIFQueueElement q2 = (CIFQueueElement)v.elementAt(i);
		    if (q1.keys[0] > q2.keys[0] || 
			(q1.keys[0] == q2.keys[0] && 
			 ((q1.keys[1] > q2.keys[1] && q1 instanceof CIFQLeaf && q2 instanceof CIFQLeaf) || 
			  (q1 instanceof CIFQLeaf && !(q2 instanceof CIFQLeaf))))) {
			v.setElementAt(q2, i - 1);
			v.setElementAt(q1, i);
		    }
		}
	    }

	    CIFQueueElement First() {
		CIFQueueElement q = (CIFQueueElement)v.elementAt(0);
		return q;
	    }

	    void DeleteFirst() {
		v.removeElementAt(0);
	    }

	    CIFQueueElement Dequeue() {
		CIFQueueElement q = (CIFQueueElement)v.elementAt(0);
		v.removeElementAt(0);
		return q;
	    }

	    void EnqueueBinTree(QueryObject qu, CIFbnode r) {
		if (r == null)
		    return;
		for (int i = 0; i < r.rv.size(); i++) {
		    double[] dist = new double[2];
		    qu.distance(r.rv.get(i), dist);
		    Enqueue(new CIFQLeaf(dist, r.rv.get(i)));
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
		    CIFQueueElement q = (CIFQueueElement)v.elementAt(i);
		    if (q instanceof CIFQLeaf)
			r.addElement(new GreenRect(((CIFQLeaf)q).rec));	
		    else
			r.addElement(new QueueBlock(((CIFQINode)q).block));
		}
		return r;
	    }
	}

	CIFQueue q;

	CIFIncNearest(CIFcnode rt) {
	    q = new CIFQueue();
	    double[] dist = new double[2];
	    dist[0] = dist[1] = 0.0;
	    q.Enqueue(new CIFQINode(dist, rt, wholeCanvas));
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

	DRectangle[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
	    Vector rect = new Vector();
	    final double xf[] = {0, .5, 0, .5};
	    final double yf[] = {.5, .5, 0, 0};
	    int counter = 1;
	
	    while(!q.isEmpty()) {
		CIFQueueElement element = q.Dequeue();
		if (element instanceof CIFQLeaf) {
		    CIFQLeaf ql = (CIFQLeaf)element;
		    if (nrelems-- <= 0 || qu.distance(ql.rec) > dist)
			break;
		    rect.addElement(ql.rec);
		    ret.addElement(new NNElement(new NNDrawable(ql.rec, counter++), ql.keys[0], q.makeVector()));
		} else {
		    CIFQINode in = (CIFQINode)element;
		    ret.addElement(new NNElement(new YellowBlock(in.block, false), in.keys[0], q.makeVector()));
		    q.EnqueueBinTree(qu, in.r.BIN[0]);
		    q.EnqueueBinTree(qu, in.r.BIN[1]);
	    
		    for (int i = 0; i < 4; i++) 
			if (in.r.SON[i] != null) {
			    DRectangle dr = new DRectangle(in.block.x + xf[i] * in.block.width, 
							   in.block.y + yf[i] * in.block.height,
							   in.block.width / 2, in.block.height / 2);
			    double[] keys = new double[2];
			    qu.distance(dr, keys);
			    q.Enqueue(new CIFQINode(keys, in.r.SON[i], dr));
			}
		}
	    }
	    DRectangle[] ar = new DRectangle[rect.size()];
	    rect.copyInto(ar);
	    return ar;
	}
   
	NearestINode CIFQuery(DPoint qu) {
	    final double xf[] = {0, .5, 0, .5};
	    final double yf[] = {.5, .5, 0, 0};
	
	    while(!q.isEmpty()) {
		CIFQueueElement element = q.Dequeue();

		if (element instanceof NearestINode) {
		    return (NearestINode)element;
		} else {
		    CIFQINode in = (CIFQINode)element;
		    double[] lkeys = new double[2];
		    DPoint dp = new DPoint(in.block.x + in.block.width / 2, 
					   in.block.y + in.block.height / 2);
		    qu.distance(dp, lkeys);
		    q.Enqueue(new NearestINode(lkeys, in.r, dp)); 
		    for (int i = 0; i < 4; i++) 
			if (in.r.SON[i] != null) {
			    DRectangle dr = new DRectangle(in.block.x + xf[i] * in.block.width, 
							   in.block.y + yf[i] * in.block.height,
							   in.block.width / 2, in.block.height / 2);
			    double[] keys = new double[2];
			    qu.distance(dr, keys);
			    q.Enqueue(new CIFQINode(keys, in.r.SON[i], dr));
			}
		}
	    }
	    return null;
 
	}

    }
  

    class CIFbnode {
	CIFbnode[] SON;
	DRectVector rv;

	CIFbnode () {
	    SON = new CIFbnode[2];
	    SON[0] = SON[1] = null;
	    rv = new DRectVector();
	}
    }

    class CIFcnode {
	final int NRDIRS = 4;

	CIFbnode BIN[] = new CIFbnode[2]; // indexed by 'x' and 'y' coordinates
	CIFcnode SON[] = new CIFcnode[NRDIRS];

	CIFcnode() {
	    BIN[0] = BIN[1] = null;
	    SON[0] = SON[1] = SON[2] = SON[3] = null;
	}
    }


}

