package vasco.points;
/* $Id: PKTree.java,v 1.1.1.1 2002/09/25 05:48:37 brabec Exp $ */
import vasco.common.*;
import vasco.drawable.*;
import java.util.*;
import javax.swing.*; // import java.awt.*;

public class PKTree extends PointStructure implements BucketIface {

    int kParam;
    PKNode pkroot;

    public PKTree(DRectangle can, int md, int k, TopInterface p, RebuildTree r) {
	super(can, p, r);
	kParam = k;
    }

    public void reInit(JComboBox ao) {
	super.reInit(ao);
	ao.addItem("Nearest");
	ao.addItem("Within");
	new Bucket(topInterface, "Aggregation Threshold", this);
    }   

    public void Clear() {
	super.Clear();
	pkroot = null;
    }

    public boolean orderDependent() {
        return false;
    }


    public boolean Insert(DPoint p) {
	if (pkroot == null)
	    pkroot = new PKNode(new Vector(), wholeCanvas);
	insert(pkroot, pkroot, new PKLeaf(p, new DRectangle(p.x, p.y, 0, 0)));
	return true;
    }

    public void Delete(DPoint d) {
	if (pkroot == null)
	    return;
	PKIncNearest pkin = new PKIncNearest(pkroot);
	delete(pkroot, pkroot, pkin.Query(new QueryObject(d)));
    }

    public void DeleteDirect(Drawable d) {
	if (pkroot == null)
	    return;
	delete(pkroot, pkroot, (DPoint)d);
    }

    private void insert(PKNode rt, PKNode Ct, PKNode Cd) {
	for (int i = 0; i < Ct.pkSons.size(); i++) {
	    PKNode son = (PKNode)Ct.pkSons.elementAt(i);
	    DRectangle Ctr = son.r;
	    if (Ctr.contains(Cd.r)) {
		insert(rt, son, Cd);
		return;
	    }
	}
	Ct.pkSons.addElement(Cd);
	checkInstantiable(rt, Ct);
    }

    private void delete(PKNode rt, PKNode Ct, DPoint p) {
	for (int i = 0; i < Ct.pkSons.size(); i++) {
	    PKNode son = (PKNode)Ct.pkSons.elementAt(i);
	    if (son.r.contains(p)) {
		if (son instanceof PKLeaf) {
		    Ct.pkSons.removeElementAt(i);
		    checkInstantiable(rt, Ct);
		} else
		    delete(rt, son, p);
	    }
	}
    }

    private PKNode getParent(PKNode pk) {
	PKNode p = pkroot;
	while (p != null) {
	    for (int i = 0; i < p.pkSons.size(); i++) {
		if (p.pkSons.elementAt(i) == pk)
		    return p;
	    }
	    for (int i = 0; i < p.pkSons.size(); i++) {
		PKNode tmp = ((PKNode)p.pkSons.elementAt(i));
		if (tmp.r.contains(pk.r)) {
		    p = tmp;
		    break;
		}
	    }
	}
	System.err.println("getParent - no son");
	return null;
    }

    private void checkInstantiable(PKNode rt, PKNode Ct) {
	Vector chSet = Ct.pkSons;
	if (chSet.size() < kParam && Ct != rt) {
	    PKNode P = getParent(Ct);
	    for (int i = 0; i < chSet.size(); i++) {
		P.pkSons.addElement(chSet.elementAt(i));
	    }
	    P.pkSons.removeElement(Ct);
	    checkInstantiable(rt, P);
	} else if (chSet.size() >= kParam) {
	    if (split(Ct))
		checkInstantiable(rt, Ct);
	}
    }


    private DRectangle findSmallest(DRectangle r, Vector nodes) {
	double[] XF = {0, 0.5, 0, 0.5};
	double[] YF = {0, 0, 0.5, 0.5};

	for (int i = 0; i < 4; i ++) {
	    DRectangle dr = new DRectangle(r.x + XF[i] * r.width,
					   r.y + YF[i] * r.height,
					   r.width / 2, r.height / 2);
	    int counter = 0;
	    for (int j = 0; j < nodes.size(); j++) {
		if ( dr.contains(((PKNode)nodes.elementAt(j)).r))
		    counter++;
	    }
	    if (counter >= kParam) {
		DRectangle d = findSmallest(dr, nodes);
		if (d == null)
		    return dr;
		else 
		    return d;
	    }
	}
	//	System.err.println("Smallest not found");
	return null;

    }

    private boolean split(PKNode nd) {
	DRectangle dr = findSmallest(nd.r, nd.pkSons);
	if (dr != null) {
	    PKNode nn = new PKNode(new Vector(), dr);
	    for (int j = 0; j < nd.pkSons.size(); j++) {
		if ( dr.contains(((PKNode)nd.pkSons.elementAt(j)).r)) {
		    nn.pkSons.addElement(nd.pkSons.elementAt(j));
		    nd.pkSons.removeElementAt(j);
		    j--;
		}
	    }
	    nd.pkSons.addElement(nn);
	    return true;
	}
	return false;
    }


    public void MessageEnd() {
	//	adjustPKTree();
	super.MessageEnd();
    }

    public String getName() {
	return "PK Tree";
    }

    public void drawContents(DrawingTarget dt, Rectangle r) {
	drawC(pkroot, dt);
    }

  public SearchVector Search(QueryObject q, int mode) {
    SearchVector res = new SearchVector();
    searchVector = new Vector();
    search(pkroot, q, wholeCanvas, mode, res);
    return res;
  }

  public Drawable NearestFirst(QueryObject p) {
    if (pkroot == null) 
      return null;
    PKIncNearest prin = new PKIncNearest(pkroot);
    return prin.Query(p);
  }

  public SearchVector Nearest(QueryObject p) {
    SearchVector v = new SearchVector();
    if (pkroot != null) {
	PKIncNearest prin = new PKIncNearest(pkroot);
	prin.Query(p, v);
    }
    return v;
  }

  public SearchVector Nearest(QueryObject p, double dist) {
    SearchVector v = new SearchVector();
    if (pkroot != null) {
	PKIncNearest prin = new PKIncNearest(pkroot);
	prin.Query(p, v, dist, Integer.MAX_VALUE);
    }
    return v;
  }

    public Drawable[] NearestRange(QueryObject p, double dist) {
        PKIncNearest near = new PKIncNearest(pkroot);
        return near.Query(p, dist);
    }

    public void setBucket(int b) {
	kParam = b;
	reb.rebuild();
    };

    public int getBucket() {
	return kParam;
    };


    //-------------------------------------
  Vector searchVector;

    private boolean hasLeaves(PKNode p) {
	for (int i = 0; i < p.pkSons.size(); i++) {
	    if (p.pkSons.elementAt(i) instanceof PKLeaf)
		return true;
	}
	return false;
    }


    private void search(PKNode R, QueryObject qu, DRectangle block, int mode, SearchVector v) {
	v.addElement(new SVElement(new YellowBlock(block, false && (
						   R == null || hasLeaves(R))), 
				   searchVector));

    if (R == null)
      return;

    if (R instanceof PKLeaf) {
	PKLeaf pkl = (PKLeaf)R;
	v.addElement(new SVElement(new GreenPoints(pkl.p), searchVector));
	drawableInOut(qu, pkl.p, mode, v, searchVector);
	return;
    }

    for (int i = 0; i < R.pkSons.size(); i++) {
	PKNode pkn = (PKNode)R.pkSons.elementAt(i);
	if (qu.intersects(pkn.r))
	    searchVector.addElement(pkn.r);
    }

    for (int i = R.pkSons.size() - 1; i >= 0; i--) {
	PKNode pkn = (PKNode)R.pkSons.elementAt(i);
	if (qu.intersects(pkn.r)) {
	    searchVector.removeElement(searchVector.lastElement());
	    search(pkn, qu, pkn.r, mode, v);
	}
    }
  }

    private void drawC(PKNode pk, DrawingTarget dt) {
	if (pk == null)
	    return;
	dt.setColor(Color.black);
	if (!(pk instanceof PKLeaf))
	    pk.r.draw(dt);
	if (pk instanceof PKLeaf) {
	    dt.setColor(Color.red);
	    ((PKLeaf)pk).p.draw(dt);
	} else {
	    for (int i = 0; i < pk.pkSons.size(); i++)
		drawC(((PKNode)pk.pkSons.elementAt(i)), dt);
	}
    }

    class PKNode {
	Vector pkSons;
	DRectangle r;

	PKNode() {
	}

	PKNode(Vector v, DRectangle r) {
	    pkSons = v;
	    this.r = r;
	}
    }

    class PKLeaf extends PKNode {
	DPoint p;

	PKLeaf(DPoint p, DRectangle r) {
	    super(new Vector(), r);
	    this.p = p;
 	}
    }

    // ---------------------- NEAREST ---------------------

    abstract class PKQueueElement {
	double key;

	PKQueueElement(double k) {
	    key = k;
	}
    }

    class PKINode extends PKQueueElement {
	PKNode r;
	DRectangle block;

	PKINode(double k, PKNode rr, DRectangle b) {
	    super(k);
	    r = rr;
	    block = b;
	}
    }

    class PKILeaf extends PKQueueElement {
	DPoint pnt;

	PKILeaf(double k, DPoint p) {
	    super(k);
	    pnt = p;
	}
    }

    class PKQueue {
	Vector v;

	PKQueue() {
	    v = new Vector();
	}

	void Enqueue(PKQueueElement qe) {
	    v.addElement(qe);
	    for (int i = v.size() - 1; i > 0; i--) {
		PKQueueElement q1 = (PKQueueElement)v.elementAt(i - 1);
		PKQueueElement q2 = (PKQueueElement)v.elementAt(i);
		if (q1.key > q2.key) {
		    v.setElementAt(q2, i - 1);
		    v.setElementAt(q1, i);
		}
	    }
	}

	PKQueueElement Dequeue() {
	    PKQueueElement q = (PKQueueElement)v.elementAt(0);
	    v.removeElementAt(0);
	    return q;
	}
  
	boolean isEmpty() {
	    return (v.size() == 0);
	}

	Vector makeVector() {
	    Vector r = new Vector();
	    for (int i = 0; i < v.size(); i++) {
		PKQueueElement q = (PKQueueElement)v.elementAt(i);
		if (q instanceof PKILeaf)
		    r.addElement(new GreenPoints(((PKILeaf)q).pnt));
		else
		    r.addElement(new QueueBlock(((PKINode)q).block));
	    }
	    return r;
	}

    }


    class PKIncNearest {

	PKQueue q;

	PKIncNearest(PKNode rt) {
	    q = new PKQueue();
	    q.Enqueue(new PKINode(0, rt, wholeCanvas));
	}

	DPoint Query(QueryObject qu) {
	    DPoint[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
	    return (ar.length == 0) ? null : ar[0];
	}

	void Query(QueryObject qu, SearchVector v) {
	    Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
	}

	DPoint[] Query(QueryObject qu, double dist) {
	    return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
	}

	DPoint[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
	    int counter = 1;
	    Vector pts = new Vector();

	    while(!q.isEmpty()) {
		PKQueueElement element = q.Dequeue();

		if (element instanceof PKILeaf) {
		    PKILeaf l = (PKILeaf)element;
		    if (nrelems-- <= 0 || qu.distance(l.pnt) > dist)
			break;
		    pts.addElement(l.pnt);
		    ret.addElement(new NNElement(new NNDrawable(l.pnt, counter++), l.key, q.makeVector()));
		} else {
		    PKINode in = (PKINode)element;
		    ret.addElement(new NNElement(new YellowBlock(in.block, false), in.key, q.makeVector()));
		    if (in.r == null) {
			// noop
		    } else if (in.r instanceof PKLeaf) {
			DPoint p = ((PKLeaf)in.r).p;
			if (qu.distance(p) >= qu.distance(in.block))
			    q.Enqueue(new PKILeaf(qu.distance(p), p));
		    } else {
			for (int i = 0; i < in.r.pkSons.size(); i++) {
			    PKNode pkn = (PKNode)in.r.pkSons.elementAt(i);
			    if (pkn instanceof PKLeaf) {
				DPoint p = ((PKLeaf)pkn).p;
				q.Enqueue(new PKILeaf(qu.distance(p), p));
			    } else
				q.Enqueue(new PKINode(qu.distance(pkn.r), pkn, pkn.r));
			}
		    }
		}
	    }
	    DPoint[] ar = new DPoint[pts.size()];
	    pts.copyInto(ar);
	    return ar;
	}
    }
}

