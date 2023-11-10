package vasco.points.randy;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import vasco.common.DPoint;
import vasco.common.DPointWrapper;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.drawable.Drawable;

public class AVDQuadtree// uses a PR quadtree implementation...
{
	public int maxDepth = 9;
	Node root;
	double epsilon;
	int t;
	public boolean maxDecomposition;

	public boolean showColor;

	public AVDQuadtree(ArrayList points, double epsilon, int t, int maxDecomp, DRectangle space, boolean showColor,
			boolean maxDecomposition) throws DepthExceededException {
		this.maxDecomposition = maxDecomposition;
		this.showColor = showColor;
		this.epsilon = epsilon;
		this.t = t;
		if (maxDecomposition)
			t = 5;
		this.maxDepth = maxDecomp;
		// System.out.println("Quad has maxDepth "+this.maxDepth);
		root = new WhiteNode(0, 0, space.width, space.height, null);
		Iterator it = points.iterator();
		while (it.hasNext()) {
			DPoint curr = (DPoint) it.next();
			root = root.insert(curr);
		}

		try {
			root.getNearestNeighbors();
		} catch (BadOperationException boe) {
			boe.printStackTrace(System.out);
		}
	}

	public void drawContents(DrawingTarget dt, Rectangle r) {
		root.drawContents(dt, r);
	}

	public NearestNeighborQueue getNearestNeighborQueue(Drawable dst, Node root) {
		// System.out.println("Getting nearest neighbor queue for "+rect);
		NearestNeighborQueue toReturn = new NearestNeighborQueue(dst);
		PQComparable next;
		while (!toReturn.isEmpty()) {
			next = (PQComparable) toReturn.peek();
			// System.out.println("Next item dequeued is "+next.d);
			if (next.d instanceof DPoint) {
				// System.out.println("Breaking for "+next.d);
				break;// break because we've found the nearest neighbor
			}
			toReturn.popNextNearestNeighbor();
			/*
			 * next=(PQComparable)toReturn.remove(); Node n=(Node)next.d; if(n instanceof
			 * GrayNode && !(n instanceof LightGrayNode)) { GrayNode gn=(GrayNode)n;
			 * toReturn.add(new PQComparable(pt,gn.nw)); toReturn.add(new
			 * PQComparable(pt,gn.ne)); toReturn.add(new PQComparable(pt,gn.sw));
			 * toReturn.add(new PQComparable(pt,gn.se)); } else if(n instanceof BlackNode) {
			 * BlackNode bn=(BlackNode)n; toReturn.add(new PQComparable(pt,bn.pt)); }
			 */
		}
		return toReturn;
	}

	public class NearestNeighborQueue extends PriorityQueue {
		static final long serialVersionUID = 2199052;
		Drawable d;

		public NearestNeighborQueue(Drawable d) {
			this.d = d;
			this.add(new PQComparable(d, root));
		}

		public PQComparable popNextNearestNeighbor() {
			if (this.isEmpty())
				return null;

			PQComparable next = (PQComparable) this.remove();
			if (next.d instanceof Node) {
				Node n = (Node) next.d;
				if (n instanceof GrayNode && !(n instanceof LightGrayNode)) {
					GrayNode gn = (GrayNode) n;
					this.add(new PQComparable(d, gn.nw));
					this.add(new PQComparable(d, gn.ne));
					this.add(new PQComparable(d, gn.sw));
					this.add(new PQComparable(d, gn.se));
				} else if (n instanceof BlackNode) {
					BlackNode bn = (BlackNode) n;
					this.add(new PQComparable(d, bn.pt));
				}
			}
			return next;
		}
	}

	public class PQComparable implements Comparable {
		public Drawable d;
		public Drawable dst;// destination point - thing we're comparing to

		public PQComparable(Drawable dst, Drawable d) {
			this.dst = dst;
			this.d = d;
		}

		public double distance() {
			// if(d instanceof DRectangle)
			// {
			// return d.distance(pt)*(1+epsilon);
			// }
			// else

			// Note, we're only adding points and rectangles, so this is OK.
			if (dst instanceof DRectangle && d instanceof DPoint && ((DRectangle) dst).contains((DPoint) d))
				return 0;
			if (dst instanceof DPoint)
				return d.distance((DPoint) dst);// do actual IncrementalNearestNeighbor
			else
				return d.distance((DRectangle) dst);// do actual IncrementalNearestNeighbor
		}

		public int compareTo(Object o) {
			if (o instanceof PQComparable) {
				PQComparable pqc = (PQComparable) o;
				if (this.distance() < pqc.distance())
					return -1;
				else if (this.distance() > pqc.distance())
					return 1;
				else
					return 0;
			} else
				return -1;
		}
	}

	public abstract class Node extends DRectangle {
		int nearestneighbors;
		int depth;
		double minx;
		double miny;
		double maxx;
		double maxy;
		GrayNode parent;
		Site approximateNearestSite = null;

		public Node(double minx, double miny, double maxx, double maxy, GrayNode parent) throws DepthExceededException {
			super(minx, miny, maxx - minx, maxy - miny);
			this.minx = minx;
			this.miny = miny;
			this.maxx = maxx;
			this.maxy = maxy;
			if (parent == null)
				depth = 0;
			else {
				depth = parent.depth + 1;
				if (depth > maxDepth)
					throw new DepthExceededException(this);
			}
			this.parent = parent;
		}

		public Object[] doINN(Drawable d) {
			TreeSet arr = new TreeSet();
			double mindist = -1;
			DPoint nearestPoint = null;

			NearestNeighborQueue pq = getNearestNeighborQueue(d, root);
			while (!pq.isEmpty()) {
				PQComparable pqc = pq.popNextNearestNeighbor();
				if (pqc.d instanceof DRectangle) {
					// if(count==0)System.out.println("Nearest neighbor for "+this+" is: "+pqc.d);
					if (mindist != -1 && this.distance((DRectangle) pqc.d) > (1 + epsilon) * mindist)
						break;
				} else if (pqc.d instanceof DPoint) {
					if (mindist == -1) {
						nearestPoint = (DPoint) pqc.d;
						if (d instanceof DRectangle)
							mindist = nearestPoint.distance((DRectangle) d);
						else
							mindist = nearestPoint.distance((DPoint) d);
					}
					if (mindist != -1 && this.distance((DPoint) pqc.d) > (1 + epsilon) * mindist)
						break;
					arr.add(new DPointWrapper((DPoint) pqc.d));
				}
			}

			Object[] toReturn = new Object[3];
			toReturn[0] = arr;
			toReturn[1] = new Double(mindist);
			toReturn[2] = nearestPoint;
			return toReturn;
		}

		public void getNearestNeighbors() throws BadOperationException {
			DPoint[] points = new DPoint[4];
			points[0] = new DPoint(minx, miny);
			points[1] = new DPoint(minx, maxy);
			points[2] = new DPoint(maxx, miny);
			points[3] = new DPoint(maxx, maxy);

			/*
			 * Object[] o=doINN(this);
			 * 
			 * TreeSet arr=(TreeSet)o[0]; double mindist=((Double)o[1]).doubleValue();
			 * DPoint nearestPoint=(DPoint)o[2];
			 */

			double[] mindist = new double[points.length];
			DPoint[] nearestPoint = new DPoint[points.length];
			TreeSet arr = new TreeSet();
			TreeSet arrs[] = new TreeSet[points.length];
			for (int i = 0; i < points.length; i++) {
				Object[] o = doINN(points[i]);

				arrs[i] = (TreeSet) o[0];
				arr.addAll((TreeSet) o[0]);
				mindist[i] = ((Double) o[1]).doubleValue();
				nearestPoint[i] = (DPoint) o[2];
			}

			/*
			 * boolean sameNearest=true; for(int j=1;j<points.length;j++) {
			 * if(nearestPoint[0]!=null && nearestPoint[j]!=null)
			 * if(!nearestPoint[j].equals(nearestPoint[0]))sameNearest=false; }
			 */

			// TreeSet sites=arr;

			TreeSet sites = new TreeSet();

			Iterator it = arr.iterator();
			while (it.hasNext()) {
				DPointWrapper dpw = (DPointWrapper) it.next();
				boolean add = true;
				for (int j = 0; j < points.length; j++) {
					// double newmindist=nearestPoint.distance(points[j]);
					// double newmindist=mindist;
					double newmindist = mindist[j];
					if (points[j].distance(dpw.p) > newmindist * (1 + epsilon) && (!this.contains(dpw.p)))
						add = false;
				}
				if (add)
					sites.add(dpw);
			}

			/*
			 * if(sameNearest==true)nearestneighbors=0; else nearestneighbors=-1;
			 */

			nearestneighbors = sites.size() - 1;
			if (nearestneighbors == 0) {
				this.approximateNearestSite = (Site) (((DPointWrapper) sites.first()).p);
			}

			try {
				if (nearestneighbors > t || (!(root instanceof WhiteNode) && nearestneighbors == -1)
						|| maxDecomposition) {
					GrayNode gn = this.split();
					if (this.parent != null)
						this.parent.changeNode(this, gn);
					gn.getNearestNeighbors();
				}
			} catch (DepthExceededException dee) {
				// nearestneighbors is already outside of the acceptable range, so it will be
				// colored red anyway
				// or it will be colored correctly if we're using maxDecomposition
			} // ...
				// ...
			if (this == root && this instanceof WhiteNode)// with one exception... ;)
			{
				nearestneighbors = 0;
			}
		}

		public abstract GrayNode split() throws DepthExceededException, BadOperationException;

		public abstract Node insert(DPoint p) throws DepthExceededException;

		public boolean contains(DPoint p) {
			if (minx <= p.x && miny <= p.y && maxx > p.x && maxy > p.y)
				return true;
			else
				return false;
		}

		public void drawContents(DrawingTarget dt, Rectangle r) {
			// float darkness=(1.0f-((float)nearestneighbors/(float)t));
			Color shade = new Color(1f, 1f, 1f);
			for (int i = 0; i < nearestneighbors; i++) {
				shade = shade.darker();
			}

			Color c;
			if (showColor && this.approximateNearestSite != null)
				c = this.approximateNearestSite.getColor();
			else if (nearestneighbors == -1 || (nearestneighbors > t && !maxDecomposition))
				c = new Color(1f, 0f, 0f);
			else {
				c = shade;
			}

			dt.setColor(c);
			dt.fillRect(minx, miny, maxx - minx, maxy - miny);

			dt.setColor(Color.black);
			if (!maxDecomposition)
				this.draw(dt);
		}
	}

	public class GrayNode extends Node {
		Node nw;
		Node ne;
		Node sw;
		Node se;

		public GrayNode split() throws BadOperationException {
			throw new BadOperationException("Can't split a gray node.");
		}

		public void getNearestNeighbors() throws BadOperationException {
			nw.getNearestNeighbors();
			ne.getNearestNeighbors();
			sw.getNearestNeighbors();
			se.getNearestNeighbors();
		}

		public GrayNode(double m1, double m2, double m3, double m4, GrayNode parent) throws DepthExceededException {
			super(m1, m2, m3, m4, parent);
		}

		public void setChildren(Node nw, Node ne, Node sw, Node se) {
			this.nw = nw;
			this.ne = ne;
			this.sw = sw;
			this.se = se;
		}

		public Node insert(DPoint p) throws DepthExceededException {
			if (depth > maxDepth)
				throw new DepthExceededException(this);
			if (nw.contains(p))
				nw = nw.insert(p);
			else if (ne.contains(p))
				ne = ne.insert(p);
			else if (sw.contains(p))
				sw = sw.insert(p);
			else if (se.contains(p))
				se = se.insert(p);

			return this;
		}

		public void drawContents(DrawingTarget dt, Rectangle r) {
			nw.drawContents(dt, r);
			ne.drawContents(dt, r);
			sw.drawContents(dt, r);
			se.drawContents(dt, r);
		}

		public BlackNode getOnlyBlackNode() {
			int count = 0;
			BlackNode toReturn = null;
			if (nw instanceof BlackNode) {
				count++;
				toReturn = (BlackNode) nw;
			}
			if (ne instanceof BlackNode) {
				count++;
				toReturn = (BlackNode) ne;
			}
			if (sw instanceof BlackNode) {
				count++;
				toReturn = (BlackNode) sw;
			}
			if (se instanceof BlackNode) {
				count++;
				toReturn = (BlackNode) se;
			}

			if (count == 1)
				return toReturn;
			else
				return null;
		}

		public void merge() throws DepthExceededException {
			Node newNode = null;
			if (nw instanceof WhiteNode && ne instanceof WhiteNode && sw instanceof WhiteNode
					&& se instanceof WhiteNode) {
				newNode = new WhiteNode(minx, miny, maxx, maxy, parent);
			} else {
				BlackNode b = getOnlyBlackNode();
				if (b != null) {
					newNode = new BlackNode(minx, miny, maxx, maxy, parent, b.pt);
				}
			}
			if (newNode != null)
				parent.changeNode(this, newNode);
		}

		public void changeNode(Node old, Node newn) {
			if (nw == old)
				nw = newn;
			else if (ne == old)
				ne = newn;
			else if (sw == old)
				sw = newn;
			else if (se == old)
				se = newn;
		}
	}

	public class LightGrayNode extends GrayNode {
		public LightGrayNode(double m1, double m2, double m3, double m4, GrayNode parent)
				throws DepthExceededException {
			super(m1, m2, m3, m4, parent);
		}

		public Node insert(DPoint p) throws DepthExceededException {
			GrayNode gn = new GrayNode(minx, miny, maxx, maxy, this.parent);
			this.parent.changeNode(this, gn);
			return gn.insert(p);
		}
	}

	public class WhiteNode extends Node {
		public WhiteNode(double m1, double m2, double m3, double m4, GrayNode parent) throws DepthExceededException {
			super(m1, m2, m3, m4, parent);
		}

		public Node insert(DPoint p) throws DepthExceededException {
			return new BlackNode(minx, miny, maxx, maxy, this.parent, p);
		}

		/*
		 * public void drawContents(DrawingTarget dt,Rectangle r) {
		 * dt.setColor(Color.white); dt.fillRect(minx,miny,maxx-minx,maxy-miny);
		 * dt.setColor(Color.black); this.draw(dt); }
		 */

		public GrayNode split() throws DepthExceededException {
			LightGrayNode toReturn = new LightGrayNode(minx, miny, maxx, maxy, this.parent);
			WhiteNode nw = new WhiteNode(minx, miny, (this.maxx + this.minx) / 2, (this.maxy + this.miny) / 2,
					toReturn);
			WhiteNode ne = new WhiteNode((this.maxx + this.minx) / 2, miny, this.maxx, (this.maxy + this.miny) / 2,
					toReturn);
			WhiteNode sw = new WhiteNode(minx, (this.maxy + this.miny) / 2, (this.maxx + this.minx) / 2, this.maxy,
					toReturn);
			WhiteNode se = new WhiteNode((this.maxx + this.minx) / 2, (this.maxy + this.miny) / 2, this.maxx, this.maxy,
					toReturn);

			toReturn.setChildren(nw, ne, sw, se);
			return toReturn;
		}
	}

	public class BlackNode extends Node {

		DPoint pt;

		public BlackNode(double m1, double m2, double m3, double m4, GrayNode parent, DPoint p)
				throws DepthExceededException {
			super(m1, m2, m3, m4, parent);
			this.pt = p;
		}

		public Node insert(DPoint p) throws DepthExceededException {
			GrayNode gray = this.split();
			return gray.insert(p);
		}

		public GrayNode split() throws DepthExceededException {
			GrayNode toReturn = new GrayNode(minx, miny, maxx, maxy, this.parent);
			WhiteNode nw = new WhiteNode(minx, miny, (this.maxx + this.minx) / 2, (this.maxy + this.miny) / 2,
					toReturn);
			WhiteNode ne = new WhiteNode((this.maxx + this.minx) / 2, miny, this.maxx, (this.maxy + this.miny) / 2,
					toReturn);
			WhiteNode sw = new WhiteNode(minx, (this.maxy + this.miny) / 2, (this.maxx + this.minx) / 2, this.maxy,
					toReturn);
			WhiteNode se = new WhiteNode((this.maxx + this.minx) / 2, (this.maxy + this.miny) / 2, this.maxx, this.maxy,
					toReturn);

			toReturn.setChildren(nw, ne, sw, se);
			toReturn.insert(this.pt);
			return toReturn;
		}

		/*
		 * public void drawContents(DrawingTarget dt,Rectangle r) {
		 * dt.setColor(Color.gray); dt.fillRect(minx,miny,maxx-minx,maxy-miny);
		 * dt.setColor(Color.black); this.draw(dt); }
		 */
	}

	public class DepthExceededException extends Exception {
		static final long serialVersionUID = 23593;

		public DepthExceededException(GrayNode node) {
			try {
				node.merge();
			} catch (DepthExceededException dee) {
				System.out.println("Error: Merging shouldn't cause depth to exceed.");
			}
		}

		public DepthExceededException(Node node) {

		}
	}

	public class BadOperationException extends Exception {
		static final long serialVersionUID = 23593;

		public BadOperationException(String s) {
			super(s);
		}
	}
}
