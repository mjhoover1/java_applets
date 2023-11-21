/* $Id: RTree.java,v 1.2 2003/04/02 17:29:55 brabec Exp $ */
package vasco.common;

import vasco.drawable.*;
import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
import java.util.List;
import java.util.Vector;

public class RTree extends SpatialStructure implements ItemListener{

  interface CallPermutation { public void doPerm(int[] permut); }

  class SetMin implements CallPermutation {
    int[] min;
    double minS;
    RTreeNode[] rarray;
    int minNodeLength;

    SetMin(RTreeNode[] ra, int minNL){
      minNodeLength = minNL;
      minS = Double.MAX_VALUE;
      min = new int[ra.length + 1];  // first element to indicate split
      rarray = ra;
    }

    int[] getMin() {
      return min;
    }

    public void doPerm(int[] permut) {
      DRectangle b1, b2;

      if (permut[0] < minNodeLength || permut[0] > permut.length-1 - minNodeLength)
	return;

      int split = permut[0];
      b1 = rarray[permut[1]].boundingBox;
      b2 = rarray[permut[split + 1]].boundingBox;
      for (int i = 2; i < split; i++) {
	b1 = b1.union(rarray[permut[i]].boundingBox);
      }
      for (int i = split + 2; i < permut.length; i++) {
	b2 = b2.union(rarray[permut[i]].boundingBox);
      }
      double S = b1.width * b1.height + b2.width * b2.height;
      if (S < minS) {
	System.arraycopy(permut, 0, min, 0, permut.length);
	minS = S;
      }
    }
  }

  class Permutation {
    int[] perm;
    CallPermutation callp;

    Permutation(int len, CallPermutation cp) {
      perm = new int[len + 1];
      callp = cp;
    }

    void genPerms() {
      for (int i = 0; i < Math.pow(2, perm.length-1); i++) {
	int lower = 1;
	int upper = perm.length - 1;
	for (int j = 0; j < perm.length - 1; j++) {
	  if (((i >> j) & 1) == 1)
	    perm[upper--] = j;
	  else
	    perm[lower++] = j;
	}
	perm[0] = lower - 1;
	callp.doPerm(perm);
      }
    }
  }

  class RTreeLeaf extends RTreeNode {
    Drawable geom;

    RTreeLeaf(Drawable gm, int minlength, int maxlength) {
      super(gm.getBB(), minlength, maxlength);
      geom = gm;
    }
    
  }

  class RTreeNode {
    DRectangle boundingBox;  // either a bounding box of an internal node or the actual represented rectangle
    int occup;
    int minNodeLength;
    RTreeNode[] rarray;
    int btreeKey;  // used by Hilbert and Morton versions

    RTreeNode(DRectangle r, int minlength, int maxlength) {
      minNodeLength = minlength;
      rarray = new RTreeNode[maxlength];
      for (int i = 0; i < rarray.length; i++)
	rarray[i] = null;
      boundingBox = r;
      occup = 0;
    }


    RTreeNode tryAll() {
      int min[];

      SetMin sm = new SetMin(rarray, minNodeLength);
      Permutation per = new Permutation(rarray.length, sm);
      per.genPerms();
      min = sm.getMin();
      return splitNode(min);
    }

    RTreeNode quadratic() {
      int first = 0;
      int second = 0;
      double area = Double.MAX_VALUE;
      for (int f = 0; f < rarray.length; f++)
	for (int s = f + 1; s < rarray.length; s++) {
	  DRectangle r1 = rarray[f].boundingBox;
	  DRectangle r2 = rarray[s].boundingBox;
	  DRectangle bbox = r1.union(r2);
	  double sum = bbox.width * bbox.height - (r1.width * r1.height + r2.width * r2.height);
	  if (sum < area) {
	    first = f; second = s; area = sum;
	  }
	}
      int min[] = new int[rarray.length + 1];
      min[1] = first;
      min[rarray.length] = second;
      int forward = 2;
      int backward = rarray.length - 1;
      DRectangle r1 = rarray[first].boundingBox;
      DRectangle r2 = rarray[second].boundingBox;

      int n = 0;
      int index = 0;
      while (forward <= backward) {
	double maxdiff = Double.MIN_VALUE;
      loop:
	for (int i = 0; i < rarray.length; i++) {
	  for (int j = 1; j < forward; j++)
	    if (min[j] == i)
	      continue loop;
	  for (int j = rarray.length; j > backward; j--)
	    if (min[j] == i)
	      continue loop;

	  DRectangle tst1 = r1.union(rarray[i].boundingBox);
	  DRectangle tst2 = r2.union(rarray[i].boundingBox);
	  double diff = Math.abs((tst1.getArea() - r1.getArea()) - (tst2.getArea() - r2.getArea()));
	  if (diff > maxdiff) {
	    maxdiff = diff;
	    n = i;
	    index = (tst1.getArea() - r1.getArea() > tst2.getArea() - r2.getArea()) ? backward : forward;
	  }
	}
	min[index] = n;
	if (index == backward) {
	  r2 = r2.union(rarray[n].boundingBox);
	  backward--;
	} else {
	  r1 = r1.union(rarray[n].boundingBox);
	  forward++;
	}
      }
      min[0] = forward - 1;
      if (min[0] < minNodeLength)
	min[0] = minNodeLength;
      if (min[0] > rarray.length - minNodeLength)
	min[0] = rarray.length - minNodeLength;

      return splitNode(min);
    }

    RTreeNode linear() {
      double maxminvalX = Double.MIN_VALUE;
      double maxminvalY = Double.MIN_VALUE;
      double minmaxvalX = Double.MAX_VALUE;
      double minmaxvalY = Double.MAX_VALUE;
      double minvalX = Double.MAX_VALUE;
      double minvalY = Double.MAX_VALUE;
      double maxvalX = Double.MIN_VALUE;
      double maxvalY = Double.MIN_VALUE;
      int maxminnodX = -1;
      int maxminnodY = -1;
      int minmaxnodX = -1;
      int minmaxnodY = -1;

      for (int i = 0; i < rarray.length; ++i) {
	if (rarray[i].boundingBox.x > maxminvalX) {
	  maxminvalX = rarray[i].boundingBox.x;
	  maxminnodX = i;
	}
	if (rarray[i].boundingBox.y > maxminvalY) {
	  maxminvalY = rarray[i].boundingBox.y;
	  maxminnodY = i;
	}
	if (rarray[i].boundingBox.x < minvalX) {
	  minvalX = rarray[i].boundingBox.y;
	}
	if (rarray[i].boundingBox.y < minvalY) {
	  minvalY = rarray[i].boundingBox.y;
	}
	if (rarray[i].boundingBox.x + rarray[i].boundingBox.width < minmaxvalX) {
	  minmaxvalX = rarray[i].boundingBox.x + rarray[i].boundingBox.width;
	  minmaxnodX = i;
	}
	if (rarray[i].boundingBox.y + rarray[i].boundingBox.height < minmaxvalY) {
	  minmaxvalY = rarray[i].boundingBox.y + rarray[i].boundingBox.height;
	  minmaxnodY = i;
	}
	if (rarray[i].boundingBox.x + rarray[i].boundingBox.width  > maxvalX) {
	  maxvalX = rarray[i].boundingBox.x + rarray[i].boundingBox.width;
	}
	if (rarray[i].boundingBox.y + rarray[i].boundingBox.height > maxvalY) {
	  maxvalY = rarray[i].boundingBox.y + rarray[i].boundingBox.height;
	}
      }

      int seed1, seed2;
      double sepX = (maxminvalX - minmaxvalX) / (maxvalX - minvalX);
      double sepY = (maxminvalY - minmaxvalY) / (maxvalY - minvalY);

      if (sepX > sepY) {
	seed1 = maxminnodX;
	seed2 = minmaxnodX;
      } else {
	seed1 = maxminnodY;
	seed2 = minmaxnodY;
      }
      int min[] = new int[rarray.length + 1];
      min[1] = seed1;
      min[rarray.length] = seed2;
      int forward = 2;
      int backward = rarray.length - 1;
      DRectangle r1 = rarray[seed1].boundingBox;
      DRectangle r2 = rarray[seed2].boundingBox;

      while (forward <= backward) {
	int n = -1;
	int maxdiff = Integer.MIN_VALUE;
      loop:
	for (int i = 0; i < rarray.length; i++) {
	  for (int j = 1; j < forward; j++)
	    if (min[j] == i)
	      continue loop;
	  for (int j = rarray.length; j > backward; j--)
	    if (min[j] == i)
	      continue loop;
	  n = i;
	  break;
	} // generates next non-used index
	DRectangle tst1 = r1.union(rarray[n].boundingBox);
	DRectangle tst2 = r2.union(rarray[n].boundingBox);

	if (tst1.getArea() - r1.getArea() > tst2.getArea() - r2.getArea()) {
	  min[backward] = n;
	  r2 = r2.union(rarray[n].boundingBox);
	  backward--;
	} else {
	  min[forward] = n;
	  r1 = r1.union(rarray[n].boundingBox);
	  forward++;
	}
      }
      min[0] = forward - 1;
      if (min[0] < minNodeLength)
	min[0] = minNodeLength;
      if (min[0] > rarray.length - minNodeLength)
	min[0] = rarray.length - minNodeLength;

      return splitNode(min);
    }

    double getOverlap(Vector a1, Vector a2) {
      if (a1.size() == 0 || a2.size() == 0)
	return 0;
      DRectangle b1 = rarray[((Integer)a1.elementAt(0)).intValue()].boundingBox;
      for (int i = 1; i < a1.size(); i++)
	b1 = b1.union(rarray[((Integer)a1.elementAt(i)).intValue()].boundingBox);
      DRectangle b2 = rarray[((Integer)a2.elementAt(0)).intValue()].boundingBox;
      for (int i = 1; i < a2.size(); i++)
	b2 = b2.union(rarray[((Integer)a2.elementAt(i)).intValue()].boundingBox);
      return b1.intersection(b2).getArea();
    }


    RTreeNode ang() {
      Vector left = new Vector();
      Vector right = new Vector();
      Vector top = new Vector();
      Vector bottom = new Vector();
      for (int i = 0; i < rarray.length; i++) {
	if (Math.abs(rarray[i].boundingBox.x - boundingBox.x) < 
	    Math.abs(rarray[i].boundingBox.x + rarray[i].boundingBox.width - 
		     boundingBox.x - boundingBox.width))
	  left.addElement(new Integer(i));
	else
	  right.addElement(new Integer(i));
	if (Math.abs(rarray[i].boundingBox.y - boundingBox.y) < 
	    Math.abs(rarray[i].boundingBox.y + rarray[i].boundingBox.height - 
		     boundingBox.y - boundingBox.height))
	  bottom.addElement(new Integer(i));
	else
	  top.addElement(new Integer(i));
      }

      int[] min = new int[rarray.length + 1];
      Vector first, second;
      if (Math.abs(left.size() - right.size()) == Math.abs(bottom.size() - top.size())) {
	if (getOverlap(left, right) < getOverlap(bottom, top)) {
	  first = left; second = right;
	} else {
	  first = bottom; second = top;
	}
      } else if (Math.abs(left.size() - right.size()) < Math.abs(bottom.size() - top.size())) {
	first = left; second = right;
      } else {
	first = bottom; second = top;
      }
      min[0] = first.size();
      for (int i = 0; i < first.size(); i++)
	min[i + 1] = ((Integer)first.elementAt(i)).intValue();
      for (int i = 0; i < second.size(); i++)
	min[i + first.size() + 1] = ((Integer)second.elementAt(i)).intValue();

      return splitNode(min);
    }


    RTreeNode btree() {
      for (int i = 0; i < occup - 1; i++)
	for (int j = 0; j < occup - 1; j++)
	  if (rarray[j].btreeKey > rarray[j+1].btreeKey) {
	    RTreeNode tmp = rarray[j];
	    rarray[j] = rarray[j + 1];
	    rarray[j+1] = tmp;
	  }
      if (occup == rarray.length) {
	int[] min = new int[rarray.length + 1];
	min[0] = occup / 2;
	for (int i = 0; i < occup; i++)
	  min[i + 1] = i;
	return splitNode(min);    
      } else
	return null;
    }
    
      

    RTreeNode splitNode(int[] min) {
      int split = min[0];  // split = index of the first element from the second group

      //    System.err.println("Status:" + min[0] + min[1] + min[2] + min[3] + min[4]);
    
      DRectangle bbox = rarray[min[1]].boundingBox;
      btreeKey = rarray[min[1]].btreeKey;
      for (int i = 1; i < min[0]; i++) {
	bbox = bbox.union(rarray[min[i + 1]].boundingBox);
	btreeKey = Math.max(btreeKey, rarray[min[i + 1]].btreeKey);
      }
      boundingBox = bbox;

      bbox = rarray[min[split + 1]].boundingBox;
      int bkey = rarray[min[split + 1]].btreeKey;
      for (int i = split + 1; i < rarray.length; i++) {
	bbox = bbox.union(rarray[min[i + 1]].boundingBox);
	bkey = Math.max(bkey, rarray[min[i + 1]].btreeKey);
      }

      RTreeNode nn = new RTreeNode(bbox, minNodeLength, rarray.length);
      nn.btreeKey = bkey;
      for (int i = split; i < rarray.length; i++) {
	nn.rarray[i - split] = rarray[min[i + 1]];
      }

      RTreeNode newrarray[] = new RTreeNode[rarray.length];
      for (int i = 0; i < split; i++) {
	newrarray[i] = rarray[min[i + 1]];
      }
      rarray = newrarray;

      occup = split;
      nn.occup = rarray.length - split;

      return nn;
    }

    class statStruct{
      double margin;
      double overlap;
      double area;
    };

    void swap(int[] ar, int i, int j) {
      int index = ar[i];
      ar[i] = ar[j];
      ar[j] = index;
    }    

    void axisSplit(RTreeNode n1, RTreeNode n2) {
      // Do R*tree-style split
      // Compute sorted lists
    
      int[] minX= new int[rarray.length];
      int[] minY= new int[rarray.length];
      int[] maxX= new int[rarray.length];
      int[] maxY= new int[rarray.length];
      for (int i = 0; i < rarray.length; i++)
	minX[i] = maxX[i] = minY[i] = maxY[i] = i;

      for (int i = 0; i < rarray.length; i++) 
	for (int j = 0; j < i; j++) {
	  if (rarray[i].boundingBox.x < rarray[j].boundingBox.x) 
	    swap(minX, i, j);
	  if (rarray[i].boundingBox.y < rarray[j].boundingBox.y)
	    swap(minY, i, j);
	  if (rarray[i].boundingBox.x + rarray[i].boundingBox.width < 
	      rarray[j].boundingBox.x + rarray[j].boundingBox.width) 
	    swap(maxX, i, j);
	  if (rarray[i].boundingBox.y + rarray[i].boundingBox.height < 
	      rarray[j].boundingBox.y + rarray[j].boundingBox.height) 
	    swap(maxY, i, j);
	}

      // Array of distribution statistics

      statStruct[] stat = new statStruct[4*(rarray.length+1-2*minNodeLength)];
      for (int i = 0; i < stat.length; i++)
	stat[i] = new statStruct();
      int statCounter = 0;
      // Variables that will allow the determination of the axis to split
      double minmarginsum = Double.MAX_VALUE;
      int minmarginaxis = -1;
      // Compute statistics for the various distributions      
      double marginsum = 0.0;
      // Start by computing the cummulative bounding boxes of the 
      // 'maxentries-minentries+1' entries of each end of the scale
      // ----- minX
      DRectangle[] b1 = new DRectangle[rarray.length];
      DRectangle[] b2 = new DRectangle[rarray.length];
      b1 [0] = rarray[minX[0]].boundingBox;
      b2 [0] = rarray[minX[rarray.length - 1]].boundingBox;
      for (int i = 1; i < rarray.length; ++i) {
	b1 [i] = b1 [i-1].union (rarray[minX[i]].boundingBox);
	b2 [i] = b2 [i-1].union (rarray[minX[rarray.length - i - 1]].boundingBox);
      }
      // Now compute the statistics for the
      // maxentries - 2 * minentries + 2 distributions 
      for (int splitpoint = minNodeLength-1;
	   splitpoint <= rarray.length - 1 - minNodeLength;
	   ++splitpoint) {
	DRectangle box1 = b1[splitpoint];
	DRectangle box2 = b2[rarray.length - 1 -splitpoint-1];
	stat[statCounter].margin = box1.getPerimeter() + box2.getPerimeter();
	stat[statCounter].overlap = box1.intersection(box2).getArea();
	stat[statCounter].area = box1.getArea () + box2.getArea ();
	marginsum += stat[statCounter].margin;
	statCounter++;
      }
      // ------ maxX
      b1 = new DRectangle[rarray.length];
      b2 = new DRectangle[rarray.length];
      b1 [0] = rarray[maxX[0]].boundingBox;
      b2 [0] = rarray[maxX[rarray.length - 1]].boundingBox;
      for (int i = 1; i < rarray.length; ++i) {
	b1 [i] = b1 [i-1].union (rarray[maxX[i]].boundingBox);
	b2 [i] = b2 [i-1].union (rarray[maxX[rarray.length - i - 1]].boundingBox);
      }
      for (int splitpoint = minNodeLength-1;
	   splitpoint <= rarray.length - 1 - minNodeLength;
	   ++splitpoint) {
	DRectangle box1 = b1[splitpoint];
	DRectangle box2 = b2[rarray.length - 1 -splitpoint-1];
	stat[statCounter].margin = box1.getPerimeter() + box2.getPerimeter();
	stat[statCounter].overlap = box1.intersection(box2).getArea();
	stat[statCounter].area = box1.getArea () + box2.getArea ();
	marginsum += stat[statCounter].margin;
	statCounter++;
      }
      if (marginsum < minmarginsum) {
	minmarginsum = marginsum;
	minmarginaxis = 0; // X axis
      }
      
      marginsum = 0;
      // ----- minY
      b1 = new DRectangle[rarray.length];
      b2 = new DRectangle[rarray.length];
      b1 [0] = rarray[minY[0]].boundingBox;
      b2 [0] = rarray[minY[rarray.length - 1]].boundingBox;
      for (int i = 1; i < rarray.length; ++i) {
	b1 [i] = b1 [i-1].union (rarray[minY[i]].boundingBox);
	b2 [i] = b2 [i-1].union (rarray[minY[rarray.length - i - 1]].boundingBox);
      }
      // Now compute the statistics for the
      // maxentries - 2 * minentries + 2 distributions 
      for (int splitpoint = minNodeLength-1;
	   splitpoint <= rarray.length - 1 - minNodeLength;
	   ++splitpoint) {
	DRectangle box1 = b1[splitpoint];
	DRectangle box2 = b2[rarray.length - 1 -splitpoint-1];
	stat[statCounter].margin = box1.getPerimeter() + box2.getPerimeter();
	stat[statCounter].overlap = box1.intersection(box2).getArea();
	stat[statCounter].area = box1.getArea () + box2.getArea ();
	marginsum += stat[statCounter].margin;
	statCounter++;
      }
      // ------ maxY
      b1 = new DRectangle[rarray.length];
      b2 = new DRectangle[rarray.length];
      b1 [0] = rarray[maxY[0]].boundingBox;
      b2 [0] = rarray[maxY[rarray.length - 1]].boundingBox;
      for (int i = 1; i < rarray.length; ++i) {
	b1 [i] = b1 [i-1].union (rarray[maxY[i]].boundingBox);
	b2 [i] = b2 [i-1].union (rarray[maxY[rarray.length - i - 1]].boundingBox);
      }
      // Now compute the statistics for the
      // maxentries - 2 * minentries + 2 distributions 
      for (int splitpoint = minNodeLength-1;
	   splitpoint <= rarray.length - 1 - minNodeLength;
	   ++splitpoint) {
	DRectangle box1 = b1[splitpoint];
	DRectangle box2 = b2[rarray.length - 1 -splitpoint-1];
	stat[statCounter].margin = box1.getPerimeter() + box2.getPerimeter();
	stat[statCounter].overlap = box1.intersection(box2).getArea();
	stat[statCounter].area = box1.getArea () + box2.getArea ();
	marginsum += stat[statCounter].margin;
	statCounter++;
      }
      if (marginsum < minmarginsum) {
	minmarginsum = marginsum;
	minmarginaxis = 1; // Y axis
      }
      // At this point we have in minmarginaxis the axis on which we will 
      // split. Choose the distribution with  minimum overlap,
      // breaking ties by choosing the distribution with minimum area
      double minoverlap = Double.MAX_VALUE;
      double minarea = Double.MAX_VALUE;
      int minsplitpoint = -1;
      int mindistr = -1;
      int[] sort = null;
      int dim = minmarginaxis;
      statCounter = 0;
      for (int minmax = 0; minmax < 2; ++minmax) {
	int[] psort;
	if (dim == 0)
	  psort = minmax == 0 ? minX : maxX;
	else
	  psort = minmax == 0 ? minY : maxY;
	for (int splitpoint = minNodeLength-1;
	     splitpoint < rarray.length-minNodeLength;
	     ++splitpoint) {
	  if (stat[statCounter].overlap < minoverlap || 
	      stat[statCounter].overlap == minoverlap && stat[statCounter].area < minarea) {
	    minoverlap = stat[statCounter].overlap;
	    minarea = stat[statCounter].area;
	    minsplitpoint = splitpoint;
	    mindistr = minmax;
	    sort = psort;
	  }
	  ++statCounter;
	}
      }
      //      assert (sort != null);
      //      assert (pstat - stat == (dim+1)*2*(maxentries+2-2*minentries));
      // Picked distribution; now put the corresponding entries in the
      // two split blocks
      for (int i = 0; i <= minsplitpoint; i++) {
	n1.insert (rarray[sort [i]]);
      }
      for (int i = minsplitpoint+1; i < rarray.length; i++) {
	n2.insert (rarray [sort [i]]);
      }
    }

    void recalcBBox() {
      if (this instanceof RTreeLeaf) {
	return;
      }
      
      boundingBox = rarray[0].boundingBox;
      for (int i = 1; i < occup; i++)
	boundingBox = boundingBox.union(rarray[i].boundingBox);
    }

    void insert(RTreeNode r) { // assume no overflow
      rarray[occup++] = r;
      recalcBBox();
      btreeKey = Math.max(btreeKey, r.btreeKey);
    }
      

    RTreeNode insert(RTreeNode r, String mode) {
      boundingBox = boundingBox.union(r.boundingBox);
      btreeKey = Math.max(btreeKey, r.btreeKey);
      rarray[occup++] = r;
      if (mode.equals("Hilbert nonpacked") || mode.equals("Morton nonpacked"))
	return btree();
      else if (occup == rarray.length)
	if (mode.equals("Exhaustive")){
	  return tryAll();
	} else if (mode.equals("Quadratic")) {
	  return quadratic();
	} else if (mode.equals("Linear")) {
	  return linear();
	} else if (mode.equals("Ang/Tan")) {
	  return ang();
	} else // default for batch insertions
	  return linear();
      return null;
    }
  }

  // ------------------------  RTREE -------------------------------

  class VisibleLevels extends JPanel implements TextListener, ItemListener {
    final int listSize = 5;
    boolean[] mask;
    TextField tfMin, tfMax;
    int minNode, maxNode;

    List vis;
    Checkbox ov, cov;

    VisibleLevels() {
      mask = new boolean[0]; 
      setLayout(new BorderLayout());

      JPanel top = new JPanel();
      top.setLayout(new GridLayout(1, 4));
      top.add(new Label("Min"));
      tfMin = new TextField(Integer.toString(minNodeLength), 2);
      new MouseHelp(tfMin, topInterface.getMouseDisplay(), "Set minimum node capacity", "", "");
      tfMin.addTextListener(this);
      top.add(tfMin);
      top.add(new Label("Max"));
      tfMax = new TextField(Integer.toString(maxNodeLength - 1), 2);
      new MouseHelp(tfMax, topInterface.getMouseDisplay(), "Set maximum node capacity", "", "");
      tfMax.addTextListener(this);
      top.add(tfMax);

      JPanel bottom = new JPanel();
      bottom.setLayout(new BorderLayout());
      bottom.add("North", new Label(formString("Level", "Overlap", "Coverage")));
      vis = new List(listSize, true);
      vis.addItemListener(this);
      bottom.add("Center", vis);
      JPanel p = new JPanel();
      p.setLayout(new GridLayout(1,2));
      p.add(ov = new Checkbox("Overlap"));
      new MouseHelp(ov, topInterface.getMouseDisplay(), "Turn overlap calculation on", "", "",
		    "Turn overlap calculation off", "", "");
      p.add(cov = new Checkbox("Coverage"));
      new MouseHelp(cov, topInterface.getMouseDisplay(), "Turn coverage calculation on", "", "",
		    "Turn coverage calculation off", "", "");
      ov.addItemListener(this);
      cov.addItemListener(this);
      bottom.add("South", p);

      add("North", top);
      add("South", bottom);
    }

    public void itemStateChanged(ItemEvent ie) {
      if (ie.getSource() == ov || ie.getSource() == cov) {
	adjustList(vis.getItemCount());
	return;
      }

      if (ie.getSource() == vis) {
	switch(ie.getStateChange()){
	case ItemEvent.SELECTED:
	  lastOn = ((Integer)ie.getItem()).intValue();
	  mask[lastOn] = true;
	  reb.redraw();
	  break;
	case ItemEvent.DESELECTED:
	  mask[((Integer)ie.getItem()).intValue()] = false;
	  reb.redraw();
	  break;
	}
      }
      
      if (mask.length > 0 && mask[mask.length - 1] == false && 
	  (getCurrentOperation().equals("Delete") || getCurrentOperation().equals("Move"))) {  
	  // getSource == vis || availOps
	mask[mask.length - 1] = true;
	vis.select(mask.length - 1);
	reb.redraw();
      }
    }


    public void textValueChanged(TextEvent te) {
      int nr;
      TextField tf = (TextField)te.getSource();
      try {
	nr = Integer.parseInt(tf.getText());
      } catch (NumberFormatException exc) {
	nr = -1;
      }

      if (tf == tfMin) {
	if (nr < 1 || nr > 99 || nr > Math.ceil(maxNodeLength/2.0)) 
	  tf.setText(Integer.toString(minNodeLength));
	else if (minNodeLength != nr) {
	    minNodeLength = nr;
	    reb.rebuild();
	}
      } else {
	if (nr < 1 || nr > 99 || minNodeLength > Math.ceil(nr/2.0)) 
	  tf.setText(Integer.toString(maxNodeLength - 1));
	else if (maxNodeLength != nr + 1) {
	    maxNodeLength = nr + 1;
	    reb.rebuild();
	}
      }
    }

    String formString(String l1, String l2, String l3) {
      String o;
      String s = l1 + "         ";
      s = s.substring(0, 5);
      o = "            " + l2.substring(0, Math.min(4, l2.length()));;
      s += o.substring(o.length() - 10);
      o = "            " + l3.substring(0, Math.min(4, l3.length()));
      s += o.substring(o.length() - 10);

      return s;
    }

    String formString(int level, double overlap, double coverage) {
      return formString(Integer.toString(level), overlap < 0 ? "" : Double.toString(overlap), 
			coverage < 0 ? "" : Double.toString(coverage));
    }

    void adjustList(int depth) {
      boolean newMask[] = new boolean[depth];

      if (newMask.length > mask.length) {
	System.arraycopy(mask, 0, newMask, newMask.length - mask.length, mask.length);
	for (int i = 0; i < newMask.length - mask.length; i++)
	  newMask[i] = false;
      } else 
	System.arraycopy(mask, mask.length - newMask.length, newMask, 0, depth);

      if (mask.length == 0 && depth > 0)
	newMask[depth - 1] = true;

      mask = newMask;

      overlap = new double[depth];
      coverage = new double[depth];
      if (cov.getState() || ov.getState())
	calcCoverage(overlap, coverage);
      else
	for (int i = 0; i < depth; i++)
	  coverage[i] = overlap[i] = -1;

      if (vis.getItemCount() > 0)
	vis.removeAll();
      for (int i = 0; i < depth; i++) {
	vis.addItem(formString(i, ov.getState() ? overlap[i] : -1, 
			       cov.getState() ? coverage[i] : -1));
	if (mask[i])
	  vis.select(i);
      }
    }

  }

  final static String[] structs = {"Exhaustive",
				   "Quadratic",
				   "Linear",
				   "R* tree",
				   "Ang/Tan",
				   "Hilbert packed",
				   "Morton packed",
				   "Hilbert nonpacked",
				   "Morton nonpacked",
				   "packed"
  };


  RTreeNode root;
  String splitMode;
  int minNodeLength, maxNodeLength;
  final static int maxSpaceFillingCurveLevel = 6;
  double[] coverage;
  double[] overlap;
  int lastOn;
  VisibleLevels vl;
  Choice splitMeth;


  public RTree(DRectangle can, int minnl, int maxnl, TopInterface p, RebuildTree r) {
    super(can, p, r);
    minNodeLength = minnl;
    maxNodeLength = maxnl + 1;
    splitMode = structs[0];
    initSpaceFillingCurves();
    vl = new VisibleLevels();
    splitMeth = new Choice();
    for (int i = 0; i < structs.length; i++)
      splitMeth.add(structs[i]);
    splitMeth.addItemListener(this);
  }

  RTree(DRectangle can, String mode, int minnl, int maxnl, TopInterface p, RebuildTree r) {
    super(can, p, r);
    minNodeLength = minnl;
    maxNodeLength = maxnl + 1;
    splitMode = mode;
    initSpaceFillingCurves();
    vl = new VisibleLevels();
    splitMeth = new Choice();
    for (int i = 0; i < structs.length; i++)
      splitMeth.add(structs[i]);
    splitMeth.addItemListener(this);
  }

  public void reInit(Choice ops) {
    super.reInit(ops);
    Clear();
    coverage = new double[0];
    overlap = new double[0];
    lastOn = 0;
    topInterface.getPanel().add(new Label("Splitting Method:"));
    topInterface.getPanel().add(splitMeth);
    topInterface.getPanel().add(vl);

    ops.removeItemListener(vl);  // hack to make sure there will be just one ItemListener on Choice
    ops.addItemListener(vl);
  }

  public void itemStateChanged(ItemEvent ie) {
    splitMode = splitMeth.getSelectedItem();
    reb.rebuild();
  }

  public void Clear() {
    root = null;
  }

    public boolean orderDependent() {
        return true;
    }

  public boolean Insert(Drawable r) {
    localInsert(r);
    return true;
  }

    public void Delete(DPoint p) {
	Drawable mx = NearestFirst(new QueryObject(p));
	DeleteDirect(mx);
    }

    public void DeleteDirect(Drawable toErase) { // the actual object

    if (root == null)
      return;
    if (root instanceof RTreeLeaf) {
      Clear();
      return;
    }

    if (toErase != null) {
      Vector toRe = new Vector();
      deleteRec(toErase, root, toRe);
      if (root.occup == 1)
	root = root.rarray[0];
      for (int i = 0; i < toRe.size(); i++) {
	localInsert((Drawable)toRe.elementAt(i));
      }
    }
  }


  public void MessageEnd() {
    if (splitMode.equals("Hilbert packed"))
      insertHilbert();
    else if (splitMode.equals("Morton packed"))
      insertMorton();
    else if (splitMode.equals("packed"))
      insertRossopulos();
    vl.adjustList(getDepth());
    super.MessageEnd();
  }


  void localInsert(Drawable r) {
    if (splitMode.equals("R* tree"))
      insertRstar(r);
    else
      insert(r);
  }

  public SearchVector Search(QueryObject r, int mode) {
    SearchVector res = new SearchVector();
    Vector searchVector = new Vector();
    if (root != null)
	searchLoc(root, r, mode, res, searchVector);
    return res;
  }


  public SearchVector Nearest(QueryObject p) {
    SearchVector sv = new SearchVector();
    RTreeIncNearest near = new RTreeIncNearest(root);
    near.Query(p, sv);
    return sv;
  }

  public SearchVector Nearest(QueryObject p, double dist) {
    SearchVector sv = new SearchVector();
    RTreeIncNearest near = new RTreeIncNearest(root);
    near.Query(p, sv, dist, Integer.MAX_VALUE);
    return sv;
  }

    public Drawable[] NearestRange(QueryObject p, double dist) {
	RTreeIncNearest near = new RTreeIncNearest(root);
	return near.Query(p, dist);
    }

  public Drawable NearestFirst(QueryObject p) {
    RTreeIncNearest near = new RTreeIncNearest(root);
    return near.Query(p);
  }

  public void drawContents(DrawingTarget g, Rectangle view) {
    displayRectangles(g, vl.mask, view);
    if (vl.mask.length > lastOn && vl.mask[lastOn])
      displayRectangles(g, lastOn, vl.mask.length, view);
  }

  public String getName() {
    return "R-Tree";
  }

  public void drawGrid(DrawingTarget g, int gridLevel) {
    if (gridLevel < maxSpaceFillingCurveLevel) {
      if (splitMode.equals("Hilbert packed") || 
	  splitMode.equals("Hilbert nonpacked") ||
	  splitMode.equals("packed")) {
	g.setColor(Color.black);
	for (int i = 0; i < hilbert[gridLevel].length - 1; i++) {
	  g.drawLine(hilbert[gridLevel][i].x, hilbert[gridLevel][i].y, 
		     hilbert[gridLevel][i+1].x, hilbert[gridLevel][i+1].y);
	}
      }
      if (splitMode.equals("Morton packed") || 
	  splitMode.equals("Morton nonpacked")) {
	g.setColor(Color.black);
	for (int i = 0; i < morton[gridLevel].length - 1; i++) {
	  g.drawLine(morton[gridLevel][i].x, morton[gridLevel][i].y, 
		     morton[gridLevel][i+1].x, morton[gridLevel][i+1].y);
	}
      }
    }
  }


  //----------------------------------------
  DPoint[][] morton;
  DPoint[][] hilbert;

  int getMorton(DRectangle r) {
    final int DEFSIZE = 512;
    return getMorton((int)((DEFSIZE * ((r.x + r.width / 2) - wholeCanvas.x)) / wholeCanvas.width), 
		      (int)((DEFSIZE * ((r.y + r.height / 2) - wholeCanvas.y)) / wholeCanvas.height));
  }

  int getMorton(double ii, double jj) {
    int xloc = (int)ii; // works only for positive coordinates
    int yloc = (int)jj; // ii, jj centroids of rectangles
    int res = 0;
    for (int i = 16; i >= 0; i--) {
      int bit1 = ((xloc & (1 << i)) != 0) ? 1 : 0;
      int bit2 = ((yloc & (1 << i)) != 0) ? 1 : 0;
      res = res << 2 | (bit1 << 1) | bit2;
    }
    return res;
  }

  int getHilbert(DRectangle r) {
    final int DEFSIZE = 512;
    return getHilbert((int)(DEFSIZE * ((r.x + r.width / 2) - wholeCanvas.x) / wholeCanvas.width), 
		      (int)(DEFSIZE * ((r.y + r.height / 2) - wholeCanvas.y) / wholeCanvas.height), 
			    DEFSIZE);
  }

  int getHilbert(double ii, double jj, double size) {
    int xloc = (int)ii;
    int yloc = (int)jj;
    int side = (int)size;
    int rotationTable[] = {3, 0, 0, 1};
    int senseTable[] = {-1, 1, 1, -1};
    int quadTable[][][] = {{{0, 1}, {3, 2}}, {{1, 2}, {0, 3}}, {{2, 3}, {1, 0}}, {{3, 0}, {2, 1}}};

    int rotation = 0;
    int sense = 1;
    int num = 0;
    int xbit, ybit, quad;

    for (int k = side /2; k > 0; k /= 2) {
      xbit = xloc / k;
      ybit = yloc / k;
      xloc -= k * xbit;
      yloc -= k * ybit;
      quad = quadTable[rotation][xbit][ybit];
      num += (sense == -1)? k*k*(3 - quad) : k*k*quad;
      rotation += rotationTable[quad];
      if (rotation >= 4)
	rotation -= 4;
      sense *= senseTable[quad];
    }
    return num;
  }
  
  void initSpaceFillingCurves() {
    morton = new DPoint[maxSpaceFillingCurveLevel][];
    hilbert = new DPoint[maxSpaceFillingCurveLevel][];
    double canvasWidth = Math.min(wholeCanvas.width, wholeCanvas.height);

    for (int k = 0; k < maxSpaceFillingCurveLevel; k++) {
      double add = canvasWidth / Math.pow(2, k);
      morton[k] = new DPoint[(int)(canvasWidth / add) * (int)(canvasWidth / add)];
      hilbert[k] = new DPoint[(int)(canvasWidth / add) * (int)(canvasWidth / add)];

      for (int i = 0; i < (int)(canvasWidth / add); i++) {
	for (int j = 0; j < (int)(canvasWidth / add); j++) {
	  morton[k][getMorton(i, j)] = new DPoint(wholeCanvas.x + add/2 + i * add, 
						  wholeCanvas.y + add/2 + j * add);
	  hilbert[k][getHilbert(i, j, (int)Math.pow(2, k))] = 
	    new DPoint(wholeCanvas.x + add/2 + i * add, 
		       wholeCanvas.y + add/2 + j * add);
	}
      }
    }
  }

  void calcCoverage(double[] overlap, double[] coverage) {
    sweepPlane sp;

    if (coverage.length > 0) {
      double basearea;
      sp = new sweepPlane(returnRectangles(coverage.length - 1));

      basearea = sp.getArea();
      coverage[coverage.length - 1] = 1;
      for (int i = 0; i < coverage.length; i++) {
	sp = new sweepPlane(returnRectangles(i));
	if (i != coverage.length - 1)
	  coverage[i] = Math.max(1.0, sp.getArea() / (double)basearea);
	overlap[i] = Math.max(1.0, areaSum(i) / (double)basearea);
      }
    } 
  }

  // --- search

  void searchLoc(RTreeNode n, QueryObject searchRect, int mode, SearchVector v, Vector searchVector) {
    if (n instanceof RTreeLeaf) {
      drawableInOut(searchRect, ((RTreeLeaf)n).geom, mode, v, searchVector);
      return;
    }

    v.addElement(new SVElement(new YellowBlock(n.boundingBox, 
					       !(n instanceof RTreeLeaf) && n.rarray[0] instanceof RTreeLeaf), 
			       searchVector));

    for (int i = n.occup - 1; i >= 0; i--) 
      if (searchRect.intersects(n.rarray[i].boundingBox) || n.rarray[i] instanceof RTreeLeaf)
	searchVector.addElement(n.rarray[i].boundingBox);
    for (int i = 0; i < n.occup; i++) {
      if (searchRect.intersects(n.rarray[i].boundingBox) || n.rarray[i] instanceof RTreeLeaf) {
	searchVector.removeElementAt(searchVector.size() - 1);
	searchLoc(n.rarray[i], searchRect, mode, v, searchVector);
      }
    }
  }


  // --- insert R*

  void insertRstar(Drawable r) {
    if (root == null) {
      root = new RTreeLeaf(r, minNodeLength, maxNodeLength);
    } else if (root instanceof RTreeLeaf) {
      RTreeNode oldr = root;
      root = new RTreeNode(r.getBB().union(oldr.boundingBox), minNodeLength, maxNodeLength);
      root.rarray[root.occup++] = oldr;
      root.rarray[root.occup++] = new RTreeLeaf(r, minNodeLength, maxNodeLength);
    } else {
      RTreeNode[] ar = insertRstarLoc(r, root, null);
      if (ar != null) {
	root = new RTreeNode(null, minNodeLength, maxNodeLength);
	root.insert(ar[0]);
	root.insert(ar[1]);
      }
    }	
  }

    public class RStarComparable  implements Comparable {
	RTreeNode rtn;
	double dist;

	public RStarComparable(RTreeNode rtn, DRectangle bbox) {
	    this.rtn = rtn;
	    dist = RstarDistance(bbox, rtn.boundingBox);
	}
	
	public double sortBy() {
	    return dist;
	}
    }

    void chooseReinsert(RTreeNode rt) {
	RTreeNode[] rtn = rt.rarray;
	RStarComparable[] hc = new RStarComparable[rt.occup];
	for (int i = 0; i < rt.occup; i++)
	    hc[i] = new RStarComparable(rtn[i], rt.boundingBox);
	QSortAlgorithm.sort(hc);
	for (int i = 0; i < rt.occup; i++)
	    rtn[i] = hc[i].rtn;
    }

  double RstarDistance(DRectangle r1, DRectangle r2) {
      double cx1, cy1, cx2, cy2;
      cx1 = r1.x + r1.width / 2;
      cx2 = r2.x + r2.width / 2;
      cy1 = r1.y + r1.height / 2;
      cy2 = r2.y + r2.height / 2;
      return Math.abs(cx1 - cx2)  + Math.abs(cy1 - cy2);
  }
    /*
  void chooseReinsert(RTreeNode n) {
    for (int i = 0; i < n.occup; i++) {
      for (int j = 0; j < i; j++) {
	double checkDistI = RstarDistance(n.boundingBox, n.rarray[i].boundingBox);
	double checkDistJ = RstarDistance(n.boundingBox, n.rarray[j].boundingBox);
	if (checkDistI < checkDistJ) {
	  RTreeNode rt = n.rarray[i];
	  n.rarray[i] = n.rarray[j];
	  n.rarray[j] = rt;
	}
      }
    }
  }
    */

  RTreeNode[] insertRstarLoc(Drawable r, RTreeNode n, RTreeNode origin) {
    if (n instanceof RTreeLeaf)
      return null;
    if (n.rarray[0] instanceof RTreeLeaf) {
      n.boundingBox = r.getBB().union(n.boundingBox);
      n.rarray[n.occup++] = new RTreeLeaf(r, minNodeLength, maxNodeLength);
      if (n.occup < maxNodeLength) {
	return null; // inserted OK, enough space
      } else if (n != origin) { // reinsert
	chooseReinsert(n); // put the fartherst to the back of the array
	int split = n.occup - (int)Math.ceil(n.occup * 0.3); // reinsert 30%
	if (split < minNodeLength)
	  split = minNodeLength;
	Drawable[] box = new Drawable[maxNodeLength - split];
	for (int i = 0; i < maxNodeLength - split; i++)
	  box[i] = ((RTreeLeaf)n.rarray[i + split]).geom;
	n.occup = split;
	for (int i = 0; i < box.length; i++) {
	  RTreeNode[] ar = insertRstarLoc(box[i], root, n);
	  if (ar != null) {
	    root = new RTreeNode(null, minNodeLength, maxNodeLength);
	    root.insert(ar[0]);
	    root.insert(ar[1]);
	  }
	}
	return null;
      } else {
	RTreeNode[] rt = new RTreeNode[2];
	rt[0] = new RTreeNode(null, minNodeLength, maxNodeLength);
	rt[1] = new RTreeNode(null, minNodeLength, maxNodeLength);
	n.axisSplit(rt[0], rt[1]);
	return rt;
	//split
      }
      // can't run here
    }

    DRectangle un;
    double minS = Double.MAX_VALUE;
    int minI = -1;
    for (int i = 0; i < n.occup; i++) {
      un = r.getBB().union(n.rarray[i].boundingBox);
      double overlap = 0;
      for (int j = 0; j < n.occup; j++) {
	if (j == i)
	  continue;
	DRectangle before = n.rarray[i].boundingBox.intersection(n.rarray[j].boundingBox);
	DRectangle after = un.intersection(n.rarray[j].boundingBox);
	overlap += after.getArea() - before.getArea();
      }
      if (overlap < minS) {
	minS = overlap; minI = i;
      }
    }
    // minI - index of son to go to

    RTreeNode[] ar = insertRstarLoc(r, n.rarray[minI], origin);
    if (ar != null) {
      n.rarray[minI] = ar[0];
      n.insert(ar[1]);
      if (n.occup == maxNodeLength) {
	RTreeNode[] rt = new RTreeNode[2];
	rt[0] = new RTreeNode(null, minNodeLength, maxNodeLength);
	rt[1] = new RTreeNode(null, minNodeLength, maxNodeLength);
	n.axisSplit(rt[0], rt[1]);
	return rt;
      } else
	return null;
    }
    n.boundingBox = n.rarray[0].boundingBox;
    for (int i = 1; i < n.occup; i++)
      n.boundingBox = n.boundingBox.union(n.rarray[i].boundingBox);
    return null;
  }


  // --- insert regular R-tree

  void insert(Drawable r) {
    if (root == null) {
      root = new RTreeLeaf(r, minNodeLength, maxNodeLength);
      root.btreeKey = splitMode.equals("Hilbert nonpacked") ? 
	getHilbert(r.getBB()) : getMorton(r.getBB());
    } else {
      if (root instanceof RTreeLeaf) {
	RTreeNode oldr = root;
	root = new RTreeNode(r.getBB().union(oldr.boundingBox), minNodeLength, maxNodeLength);
	root.rarray[root.occup++] = oldr;
	root.rarray[root.occup++] = new RTreeLeaf(r, minNodeLength, maxNodeLength);
	root.rarray[1].btreeKey = splitMode.equals("Hilbert nonpacked") ?
	  getHilbert(r.getBB()) : getMorton(r.getBB());
	if (root.rarray[0].btreeKey > root.rarray[1].btreeKey) {
	  RTreeNode rt = root.rarray[0];
	  root.rarray[0] = root.rarray[1];
	  root.rarray[1] = rt;
	}
      } else {
	RTreeNode newr = insertLoc(r, root);
	if (newr != null) {
	  RTreeNode oldr = root;
	  root = new RTreeNode(newr.boundingBox.union(oldr.boundingBox), minNodeLength, maxNodeLength);
	  root.btreeKey = Math.max(oldr.btreeKey, newr.btreeKey);
	  root.insert(oldr, splitMode);
	  root.insert(newr, splitMode);
	}
      }
    }
  }

  RTreeNode insertLoc(Drawable r, RTreeNode n) {
    if (n instanceof RTreeLeaf)
      return null;
 
    n.boundingBox = n.boundingBox.union(r.getBB());

    if (n.rarray[0] instanceof RTreeLeaf) {  // second level from bottom
      RTreeLeaf rt = new RTreeLeaf(r, minNodeLength, maxNodeLength);
      rt.btreeKey = splitMode.equals("Hilbert nonpacked") ? 
	getHilbert(r.getBB()) : getMorton(r.getBB());
      return n.insert(rt, splitMode);
    }
 
    if (splitMode.equals("Hilbert nonpacked") || splitMode.equals("Morton nonpacked")) {
      int i;
      for (i = 0; i < n.occup - 1; i++)
	if (n.rarray[i].btreeKey > (splitMode.equals("Hilbert nonpacked") ? 
				    getHilbert(r.getBB()) : getMorton(r.getBB())))
	  break;
      RTreeNode nd = insertLoc(r, n.rarray[i]);
      if (nd != null)
	return n.insert(nd, splitMode);
      else 
	return null;
    }

    DRectangle un = r.getBB().union(n.rarray[0].boundingBox);
    double minS = un.getArea();
    int minI = 0;
    for (int i = 1; i < n.occup; i++) {
      un = r.getBB().union(n.rarray[i].boundingBox);
      double S = un.getArea();
      if (S < minS) {
	minS = S; minI = i;
      }
    }

    RTreeNode nd = insertLoc(r, n.rarray[minI]);
    if (nd != null)
      return n.insert(nd, splitMode);
    else 
      return null;
  }

  int gdepth(RTreeNode r) {
    if (r == null)
      return 0;
    else
      return gdepth(r.rarray[0]) + 1;
  }

  int getDepth() {
    return gdepth(root);
  }

  void gat(RTreeNode r, Vector v) {
    if (r == null)
      return;
    else if (r instanceof RTreeLeaf)
      v.addElement(((RTreeLeaf)r).geom);
    else
      for (int i = 0; i < r.occup; i++)
	gat(r.rarray[i], v);
  }
  
  Vector gatherRect() {
    Vector v = new Vector();
    gat(root, v);
    return v;
  }

  // sort hilbert ----
    public class HilbertComparable  implements Comparable {
	RTreeNode rtn;
	int hilbVal;

	public HilbertComparable(RTreeNode rtn) {
	    this.rtn = rtn;
	    hilbVal = getHilbert(rtn.boundingBox);
	}
	
	public double sortBy() {
	    return hilbVal;
	}
    }

    void sortHilbert(Vector v, int startNode) {
	HilbertComparable[] hc = new HilbertComparable[v.size() - startNode];
	for (int i = startNode; i < v.size(); i++)
	    hc[i - startNode] = new HilbertComparable((RTreeNode)v.elementAt(i));
	QSortAlgorithm.sort(hc);
	for (int i = 0; i < hc.length; i++)
	    v.setElementAt(hc[i].rtn, i + startNode);
    }
    /*
  void sortHilbert(Vector v, int startNode) {
    for (int i = startNode; i < v.size() - 1; i++) 
      for (int j = startNode; j < v.size() - 1; j++) {
	DRectangle r1 = ((RTreeNode)v.elementAt(j)).boundingBox;
	DRectangle r2 = ((RTreeNode)v.elementAt(j+1)).boundingBox;
	if (getHilbert(r1) > getHilbert(r2)) {
	  RTreeNode b = (RTreeNode)v.elementAt(j);
	  v.setElementAt(v.elementAt(j+1), j);
	  v.setElementAt(b, j+1);
	}
      }
  }
    */
  // insert Hilbert --------------------------------------------

  Vector buildHilbert(Vector v) {
    int nextSize;
    Vector ret = new Vector();
    for (int i = 0; i < v.size(); ) {
      nextSize = (v.size() - i < maxNodeLength) ? (v.size() - i) : 
	(i + maxNodeLength + minNodeLength - 1 > v.size()) ? (v.size() - i) / 2 : maxNodeLength - 1;
      DRectangle uni = ((RTreeNode)v.elementAt(i)).boundingBox;
      for (int j = i + 1; j < i + nextSize; j++)
	uni = uni.union(((RTreeNode)v.elementAt(j)).boundingBox);
      RTreeNode r = new RTreeNode(uni, minNodeLength, maxNodeLength);
      r.btreeKey = getHilbert(uni);
      ret.addElement(r);
      for (int j = 0; j < nextSize; j++) 
	r.rarray[r.occup++] = (RTreeNode)v.elementAt(i++);
    }
    if (ret.size() > 1)
      return buildHilbert(ret);
    else
      return ret;
  }

  void insertHilbert() {
    insertBatchHilbert(gatherRect());
  }

  void insertBatchHilbert(Vector v) {
    Vector newV = new Vector();
    for (int i = 0; i < v.size(); i++) {
      RTreeLeaf rt = new RTreeLeaf((Drawable)v.elementAt(i), minNodeLength, maxNodeLength);
      rt.btreeKey = getHilbert(rt.boundingBox);
      newV.addElement(rt);
    }

    sortHilbert(newV, 0);
    Vector ret = buildHilbert(newV);
    root = ret.size() == 1 ? (RTreeNode)ret.elementAt(0) : null;
  }
  // - end Hilbert ---------------------------------------------

    class RossComparable implements Comparable {
	double dist;
	RTreeNode rtn;

	public RossComparable(RTreeNode rtn, DRectangle compTo) {
	    this.rtn = rtn;
	    dist = rtn.boundingBox.distance(compTo);
	}
	
	public double sortBy() {
	    return dist;
	}
    }


  Vector buildRossopulos(Vector v) {
    int nextSize;

    Vector ret = new Vector();

    for (int i = 0; i < v.size(); ) {
      sortHilbert(v, i);

      RossComparable[] hc = new RossComparable[v.size() - i - 1];
      for (int j = i + 1; j < v.size(); j++)
	  hc[j - i - 1] = new RossComparable((RTreeNode)v.elementAt(j), 
					     ((RTreeNode)v.elementAt(i)).boundingBox);
      QSortAlgorithm.sort(hc);
      for (int j = 0; j < hc.length; j++)
	  v.setElementAt(hc[j].rtn, j + i + 1);

      /*
      for (int k = i + 1; k < v.size() - 1; k++) 
	for (int j = i + 1; j < v.size() - 1; j++) 
	  if (((RTreeNode)v.elementAt(j)).boundingBox.distance(((RTreeNode)v.elementAt(i)).boundingBox) > 
	      ((RTreeNode)v.elementAt(j+1)).boundingBox.distance(((RTreeNode)v.elementAt(i)).boundingBox)) {
	    RTreeNode b = (RTreeNode)v.elementAt(j);
	    v.setElementAt(v.elementAt(j+1), j);
	    v.setElementAt(b, j+1);
	  }
      */

      nextSize = (v.size() - i < maxNodeLength) ? (v.size() - i) : 
	(i + maxNodeLength + minNodeLength - 1 > v.size()) ? (v.size() - i) / 2 : maxNodeLength - 1;
      DRectangle uni = ((RTreeNode)v.elementAt(i)).boundingBox;
      for (int j = i + 1; j < i + nextSize; j++)
	uni = uni.union(((RTreeNode)v.elementAt(j)).boundingBox);
      RTreeNode r = new RTreeNode(uni, minNodeLength, maxNodeLength);
      r.btreeKey = getHilbert(uni);
      ret.addElement(r);
      for (int j = 0; j < nextSize; j++) 
	r.rarray[r.occup++] = (RTreeNode)v.elementAt(i++);
    }


    if (ret.size() > 1)
      return buildRossopulos(ret);
    else
      return ret;
  }

  void insertRossopulos() {
    insertBatchRossopulos(gatherRect());
  }

  void insertBatchRossopulos(Vector v) {
    Vector newV = new Vector();
    for (int i = 0; i < v.size(); i++) {
      RTreeLeaf rt = new RTreeLeaf((Drawable)v.elementAt(i), minNodeLength, maxNodeLength);
      rt.btreeKey = getHilbert(rt.boundingBox);
      newV.addElement(rt);
    }
    Vector ret = buildRossopulos(newV);
    root = ret.size() == 1 ? (RTreeNode)ret.elementAt(0) : null;
  }


  // - start Morton --------------------------------------------

    class MortComparable implements Comparable {
	double dist;
	Drawable rtn;

	MortComparable(Drawable rtn) {
	    this.rtn = rtn;
	    dist = getMorton(rtn.getBB());
	}
	
	public double sortBy() {
	    return dist;
	}
    }


  void insertMorton() {
    Vector v = gatherRect();

    MortComparable[] hc = new MortComparable[v.size()];
    for (int j = 0; j < v.size(); j++)
	hc[j] = new MortComparable((Drawable)v.elementAt(j));
    QSortAlgorithm.sort(hc);
    for (int j = 0; j < hc.length; j++)
	v.setElementAt(hc[j].rtn, j);
    

    /*
    for (int i = 0; i < v.size() - 1; i++) 
      for (int j = 0; j < v.size() - 1; j++) {
	Drawable r1 = (Drawable)v.elementAt(j);
	Drawable r2 = (Drawable)v.elementAt(j+1);
	if (getMorton(r1.getBB()) > getMorton(r2.getBB())) {
	  Drawable b = (Drawable)v.elementAt(j);
	  v.setElementAt(v.elementAt(j+1), j);
	  v.setElementAt(b, j+1);
	}
      }
    */
    Vector newV = new Vector();
    for (int i = 0; i < v.size(); i++) {
      RTreeLeaf rt = new RTreeLeaf((Drawable)v.elementAt(i), minNodeLength, maxNodeLength);
      rt.btreeKey = getMorton(rt.boundingBox);
      newV.addElement(rt);
    }
    Vector ret = buildHilbert(newV);
    root = ret.size() == 1 ? (RTreeNode)ret.elementAt(0) : null;
  }
  // - end Morton -----------------------------------------------

  void drawLevel(RTreeNode r, DrawingTarget g, int target, Rectangle view) {
    int[] counter = new int[1];
    counter[0] = 1;
    drawLevel(r, g, 0, target, counter, view);
  }

  void drawLevel(RTreeNode r, DrawingTarget g, int lev, int target, int counter[], Rectangle view) {
      /*    if (!g.visible(r.boundingBox)) - messes up the numbers
	    return; */
    //    System.out.println("-------");
    if (r == null)
      return;
    if (lev == target) {
      if (r instanceof RTreeLeaf) {
	((RTreeLeaf)r).geom.draw(g);
      } else
	g.drawRect(r.boundingBox.x, r.boundingBox.y, r.boundingBox.width, r.boundingBox.height);
      if (splitMode.equals("Hilbert nonpacked") || splitMode.equals("Hilbert packed") ||
	  splitMode.equals("Morton nonpacked") || splitMode.equals("Morton packed") ||
	  splitMode.equals("packed")) {
	g.fillOval(r.boundingBox.x + r.boundingBox.width / 2,
		   r.boundingBox.y + r.boundingBox.height / 2, 4, 4);
	g.drawString(String.valueOf(counter[0]++),  
		     r.boundingBox.x + r.boundingBox.width / 2, 
		     r.boundingBox.y + r.boundingBox.height / 2);
      }
      //      System.out.println(r.boundingBox.getMorton());
    }
    else
      for (int i = 0; i < r.occup; i++) 
	drawLevel(r.rarray[i], g, lev + 1, target, counter, view);
  }

  int areaSum(int level) {
    int area = 0;
    Vector v = returnRectangles(level);
    for (int i = 0; i < v.size(); i++) {
      DRectangle r = (DRectangle)v.elementAt(i);
      area += r.width * r.height;
    }
    return area;
  }

  void localReturnRectangles(RTreeNode r, int curlev, int target, Vector res) {
    if (r == null)
      return;
    if (curlev == target) {
      res.addElement(r.boundingBox);
    }
    else
      for (int i = 0; i < r.occup; i++)
	localReturnRectangles(r.rarray[i], curlev + 1, target, res);
  }

  Vector returnRectangles(int level) {
    Vector v = new Vector();
    localReturnRectangles(root, 0, level, v);
    return v;
  }

  void displayRectangles(DrawingTarget g, int index, int levels, Rectangle view) {
    Color[] col = {Color.red, Color.green, Color.pink, Color.cyan, Color.gray};
    g.setColor(col[(levels - 1 - index) % col.length]);
    drawLevel(root, g, index, view);
  }

  void displayRectangles(DrawingTarget g, boolean[] mask, Rectangle view) {
    if (mask == null || mask.length == 0)
      return;

    for (int i = 0; i < mask.length; i++) {
      if (mask[i]) {
	displayRectangles(g, i, mask.length, view);
      }
    }
  }

  // ---------------------  incremental deletion -----------------------
  // - reinsert as many elements as necessary to preserve the B-tree structure


  void deleteRec(Drawable toErase, RTreeNode r, Vector toBeReinserted) {
    int counter = 0;

    for (int i = 0; i < r.occup; i++)
      if (r.rarray[i] instanceof RTreeLeaf) { // sons are leaves
	if (r.rarray[i].boundingBox.equals(toErase.getBB())) {
	  r.rarray[i] = r.rarray[r.occup - 1];
	  r.occup--;
	  r.recalcBBox();
	  return;
	} else {
	  counter++;
	}
      } else if (r.rarray[i].boundingBox.contains(toErase.getBB())) {
	deleteRec(toErase, r.rarray[i], toBeReinserted);
      }

    if (counter == r.occup) // sons are leaves but none is the rectangle to be deleted
      return;
    
    // verify RTree consistency after return from the recursion

    for (int i = 0; i < r.occup; i++) {
      if (r.rarray[i].occup < minNodeLength) {
	gat(r.rarray[i], toBeReinserted);	
	r.rarray[i] = r.rarray[r.occup -1];
	r.occup--; i--;
      }
    }
    r.recalcBBox();
  }




  //-------------------------  Incremental Nearest ---------------------

  class RTreeIncNearest {
    class RTreeQueueElement {
      double key1, key2;
      RTreeQueueElement(double k1, double k2) {
	key1 = k1;
	key2 = k2;
      }
    }

    class RTreeQLeaf extends RTreeQueueElement {
      Drawable geom;
      RTreeQLeaf(double k1, double k2, Drawable o) {
	super(k1, k2);
	geom = o;
      }

	boolean compare(RTreeQueueElement e) {
	    return (e instanceof RTreeQLeaf) && (((RTreeQLeaf)e).geom.equals(geom));
	}
    }

    class RTreeQINode extends RTreeQueueElement {
      RTreeNode r;

      RTreeQINode(double k1, double k2, RTreeNode p) {
	super(k1, k2);
	r = p;
      }
    }

    class RTreeQueue {
      Vector v;

      RTreeQueue() {
	v = new Vector();
      }

      void Enqueue(RTreeQueueElement qe) {
	v.addElement(qe);
	for (int i = v.size() - 1; i > 0; i--) {
	  RTreeQueueElement q1 = (RTreeQueueElement)v.elementAt(i - 1);
	  RTreeQueueElement q2 = (RTreeQueueElement)v.elementAt(i);
	  if (q1.key1 > q2.key1 || 
	      (q1.key1 == q2.key1 && 
	       ((q1.key2 > q2.key2 && q1 instanceof RTreeQLeaf && q2 instanceof RTreeQLeaf) || 
		(q1 instanceof RTreeQLeaf && !(q2 instanceof RTreeQLeaf))))) {
	    v.setElementAt(q2, i - 1);
	    v.setElementAt(q1, i);
	  }
	}
      }

      RTreeQueueElement Dequeue() {
	RTreeQueueElement q = (RTreeQueueElement)v.elementAt(0);
	v.removeElementAt(0);
	return q;
      }
  
      boolean isEmpty() {
	return (v.size() == 0);
      }

	void DeleteFirst() {
	    v.removeElementAt(0);
	}

    RTreeQueueElement First() {
      RTreeQueueElement q = (RTreeQueueElement)v.elementAt(0);
      return q;
    }

      Vector makeVector() {
	Vector r = new Vector();
	for (int i = 0; i < v.size(); i++) {
	  RTreeQueueElement q = (RTreeQueueElement)v.elementAt(i);
	  if (!(q instanceof RTreeQLeaf)) {
	    r.addElement(new QueueBlock(((RTreeQINode)q).r.boundingBox));
	  }

	}
	for (int i = 0; i < v.size(); i++) {
	  RTreeQueueElement q = (RTreeQueueElement)v.elementAt(i);
	  if (q instanceof RTreeQLeaf) {
	    r.addElement(new GreenDrawable(((RTreeQLeaf)q).geom));
	  }
	}
	return r;
      }

    }


    RTreeQueue q;

    RTreeIncNearest(RTreeNode rt) {
      q = new RTreeQueue();
      if (rt == null)
	return;
      if (rt instanceof RTreeLeaf) {             // distances are unimportant
	  q.Enqueue(new RTreeQLeaf(0, 0, ((RTreeLeaf)rt).geom));
      } else 
	  q.Enqueue(new RTreeQINode(0, 0, rt));
    }

    Drawable Query(QueryObject qu) {
      Drawable[] ar = Query(qu, new SearchVector(), Double.MAX_VALUE, 1);
      return (ar.length == 0) ? null : ar[0];
    }

      void Query(QueryObject qu, SearchVector v) {
	  Query(qu, v, Double.MAX_VALUE, Integer.MAX_VALUE);
      }

      Drawable[] Query(QueryObject qu, double dist) {
	  return Query(qu, new SearchVector(), dist, Integer.MAX_VALUE);
      }

      private Drawable[] Query(QueryObject qu, SearchVector ret, double dist, int nrelems) {
      int counter = 1;
      Vector objs = new Vector();

      while(!q.isEmpty()) {
	RTreeQueueElement element = q.Dequeue();

	if (element instanceof RTreeQLeaf) {  // spatial object
	    RTreeQLeaf rl = (RTreeQLeaf)element;
	    double keys[] = new double[2];
	    qu.distance(rl.geom, keys);
	    if (nrelems-- <= 0 || keys[0] > dist)
		break;
	    while (!q.isEmpty() && rl.compare(q.First()))
		q.DeleteFirst();
	    objs.addElement(rl.geom);
	    ret.addElement(new NNElement(new NNDrawable(rl.geom, counter++), rl.key1,
							q.makeVector()));
	} else {
	  RTreeQINode in = (RTreeQINode)element;
	  double[] keys = new double[2];
	  ret.addElement(new NNElement(new YellowBlock(in.r.boundingBox, false), in.key1, q.makeVector()));

	  for (int i = 0; i < in.r.occup; i++) {
	      if (in.r.rarray[i] instanceof RTreeLeaf) {
		  RTreeLeaf rtl = (RTreeLeaf)in.r.rarray[i];
		  qu.distance(rtl.geom, keys);
		  q.Enqueue(new RTreeQLeaf(keys[0], keys[1], rtl.geom));
	      } else {
		  qu.distance(in.r.rarray[i].boundingBox, keys);
		  q.Enqueue(new RTreeQINode(keys[0], keys[1], in.r.rarray[i]));
	      }
	  }
	}
      }
      Drawable[] ar = new Drawable[objs.size()];
      objs.copyInto(ar);
      return ar;
    }
  }

  class sweepVert implements Comparable {
    boolean first;
    double ycoord;

    sweepVert(boolean f, double y) {
      first = f;
      ycoord = y;
    }
 
      public double sortBy() {
	  return ycoord;
      }
  }

  class sweepEntry implements Comparable {
    boolean first;
    double xcoord;
    double ymin, ymax;
    sweepEntry begin;

    sweepEntry(boolean f, double x, double yin, double yax) {
      first = f;
      xcoord = x;
      ymin = yin;
      ymax = yax;
    }
    sweepEntry(boolean f, double x, double yin, double yax, sweepEntry b) {
      first = f;
      xcoord = x;
      ymin = yin;
      ymax = yax;
      begin = b;
    }

      public double sortBy() {
	  return xcoord;
      }
  }

  class verticalList {
    // keeps all the rectangles currently in picture
    Vector v;

    verticalList() {
      v = new Vector();  // vector of sweepEntries
    }

    void add(sweepEntry s) {
      v.addElement(s);
    }

    void remove(sweepEntry s) {
      v.removeElement(s.begin);
    }

    double evaluateLine() {
      Vector vert = new Vector();
      for (int i = 0; i < v.size(); i++) {
	sweepEntry se = (sweepEntry)v.elementAt(i);
	vert.addElement(new sweepVert(true, se.ymin));
	vert.addElement(new sweepVert(false, se.ymax));
      }

      QSortAlgorithm.sort(vert);
      /*
      for (int i = 0; i < vert.size() - 1; i++)  // sort vertical list by 'y' coordinates
	for (int j = 0; j < vert.size() - 1; j++) {
	  if (((sweepVert)vert.elementAt(j)).ycoord > ((sweepVert)vert.elementAt(j+1)).ycoord) {
	    Object s = vert.elementAt(j);
	    vert.setElementAt(vert.elementAt(j+1), j);
	    vert.setElementAt(s, j+1);
	  }
	}
      */

      double lastY = 0;
      double totalDist = 0;
      int counter = 0;
      for (int i = 0; i < vert.size(); i++) {
	sweepVert se = (sweepVert)vert.elementAt(i);
	if (se.first) {
	  counter++;
	  if (counter == 1)
	    lastY = se.ycoord;
	} else {
	  counter--;
	  if (counter == 0) 
	    totalDist += se.ycoord - lastY;
	}

      }
      return totalDist;
    }
  }

  class sweepPlane {
    sweepEntry[] se;

    sweepPlane(Vector v) {
      DRectangle[] ar = new DRectangle[v.size()];
      v.copyInto(ar);
      se = new sweepEntry[2 * v.size()];
      for (int i = 0; i < v.size(); i++) {
	se[2*i] = new sweepEntry(true, ar[i].x, ar[i].y, ar[i].y + ar[i].height);
	se[2*i + 1] = new sweepEntry(false, ar[i].x + ar[i].width, ar[i].y, ar[i].y + ar[i].height, se[2*i]);
      }

      QSortAlgorithm.sort(se);
      /*
      for (int i = 0; i < se.length - 1; i++)
	for (int j = 0; j < se.length - 1; j++) {
	  if (se[j].xcoord > se[j+1].xcoord) {
	    sweepEntry s = se[j];
	    se[j] = se[j+1];
	    se[j+1] = s;
	  }
	}
      */
    }


    double getArea() {
      double lastX = 0; 
      double height;
      double totalArea = 0;
      verticalList vl = new verticalList();

      for (int i = 0; i < se.length; i++) {
	height = vl.evaluateLine();
	totalArea += height * (se[i].xcoord - lastX);
	lastX = se[i].xcoord;
	if (se[i].first)
	  vl.add(se[i]);
	else
	  vl.remove(se[i]);
      }
      return totalArea;
    }
  }	
}
