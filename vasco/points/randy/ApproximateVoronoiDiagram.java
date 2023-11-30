package vasco.points.randy;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
// import java.awt.BorderLayout;
// import java.awt.Choice;
// import java.awt.Color;
// import java.awt.Label;
// import java.awt.Panel;
// import java.awt.Rectangle;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.awt.event.ItemEvent;
// import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import vasco.common.DPoint;
import vasco.common.DPointWrapper;
import vasco.common.DRectangle;
import vasco.common.DrawingTarget;
import vasco.common.MaxDecompIface;
import vasco.common.QueryObject;
import vasco.common.RebuildTree;
import vasco.common.SearchVector;
import vasco.common.TopInterface;
import vasco.drawable.Drawable;
import vasco.points.PointCanvas;
import vasco.points.PointStructure;

public class ApproximateVoronoiDiagram extends PointStructure implements MaxDecompIface {

	VoronoiDiagram vd;
	AVDQuadtree quadtree;
	double epsilon;
	int t;
	int maxDecomp = 9;

	public boolean showColor = false;
	public boolean maxDecomposition = false;

	ShowColorCombo showColorCombo;
	DecomposeMaximallyCombo decomposeMaximallyCombo;
	MaxDecompCombo maxDecompCombo;
	EpsilonText epsilonText;
	TText tText;

	public ApproximateVoronoiDiagram(DRectangle can, double epsilon, int t, TopInterface p, RebuildTree r,
			PointCanvas pc) {
		super(can, p, r);
		vd = new VoronoiDiagram(can, p, r, pc);
		this.epsilon = epsilon;
		this.t = t;
		try {
			rebuildQuadtree();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO AVD constructor
	}

	@Override
	public SearchVector Nearest(QueryObject p) {
//		TODO AVD NEAREST
		return vd.Nearest(p);
	}

	@Override
	public SearchVector Nearest(QueryObject p, double dist) {
//		TODO AVD NEAREST 2
		return vd.Nearest(p, dist);
	}

	@Override
	public Drawable[] NearestRange(QueryObject p, double dist) {
//		TODO AVD NEAREST RANGE
		return vd.NearestRange(p, dist);
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public void setT(int t) {
		this.t = t;
	}

	public void rebuildQuadtree() throws AVDQuadtree.DepthExceededException {
		quadtree = new AVDQuadtree(vd.delaunay.getNonFixedSites(), this.epsilon, this.t, this.maxDecomp,
				this.wholeCanvas, showColor, maxDecomposition);
	}

	@Override
	public boolean Insert(DPoint p) {
//		TODO AVD INSERT
		Iterator it = vd.delaunay.sites.iterator();
		while (it.hasNext()) {
			DPoint curr = (DPoint) it.next();
			if (new DPointWrapper(curr).equals(p))
				return false;
		}
		vd.Insert(p);
		try {
			rebuildQuadtree();
		} catch (AVDQuadtree.DepthExceededException dee) {
			vd.Delete(p);
			return false;
		}
		return true;
	}

	@Override
	public void Delete(DPoint d) {
//		TODO AVD DELETE
		vd.Delete(d);
		try {
			rebuildQuadtree();
		} catch (Exception e) {
		}
	}

	@Override
	public void DeleteDirect(Drawable d) {
//		TODO AVD DELETE DIRECT
		vd.DeleteDirect(d);
		try {
			rebuildQuadtree();
		} catch (Exception e) {
		}
	}

	@Override
	public SearchVector Search(QueryObject q, int mode) {
//		TODO AVD SEARCH
		return vd.Search(q, mode);
	}

	@Override
	public Drawable NearestFirst(QueryObject p) {
//		TODO AVD NEAREST FIRST
		return vd.NearestFirst(p);
	}

	@Override
	public boolean orderDependent() {
//		TODO AVD ORDER DEPENDENT
		return false;
	}

	@Override
	public String getName() {
		return "Approximate Voronoi Diagram";
	}

	@Override
	public void drawContents(DrawingTarget dt, Rectangle r) {
//		TODO AVD DRAW CONTENTS
		quadtree.drawContents(dt, r);
		vd.drawContents(dt, r); // ,Color.blue);
		if (showColor) {
			for (Object element : vd.delaunay.sites) {
				Site dp = (Site) element;
				dt.setColor(dp.getColor().brighter().brighter().brighter());
				dp.draw(dt);
			}
		}
	}

	@Override
	public void Clear() {
		vd.Clear();
	}

	@Override
	public void reInit(JComboBox<String> ops) {
		Clear();
		topInterface.getPanel().removeAll();
		JComboBox<String> availOps = ops;
		addItemIfNotExists(availOps, "Insert");
		addItemIfNotExists(availOps, "Move");
		addItemIfNotExists(availOps, "Delete");
		addItemIfNotExists(availOps, "Nearest Site");
		showColorCombo = new ShowColorCombo(topInterface);
		decomposeMaximallyCombo = new DecomposeMaximallyCombo(topInterface);
		maxDecompCombo = new MaxDecompCombo(topInterface);
		epsilonText = new EpsilonText(topInterface);
		tText = new TText(topInterface);
		new SubmitButton(topInterface);
	}

	@Override
	public void setMaxDecomp(int i) {
		// System.out.println("Setting maxDecomp " +i);
		this.maxDecomp = i;
	}

	@Override
	public int getMaxDecomp() {
		return this.maxDecomp;
	}

	public class MaxDecompCombo implements ItemListener {
		public JComboBox choice;

		public MaxDecompCombo(TopInterface ti) {

			JPanel maxD = new JPanel();
			maxD.setLayout(new BorderLayout());
			maxD.add("West", new JLabel("Max Decomposition"));
			choice = new JComboBox();
			choice.addItem("1");
			choice.addItem("2");
			choice.addItem("3");
			choice.addItem("4");
			choice.addItem("5");
			choice.addItem("6");
			choice.addItem("7");
			choice.addItem("8");
			choice.addItem("9");
			choice.setSelectedIndex(maxDecomp - 1);// 6 is default choice.select(maxDecomp-1);//6 is default
			maxD.add("East", choice);
			// choice.addItemListener(this);
			ti.getPanel().add(maxD);
		}

		@Override
		public void itemStateChanged(ItemEvent ie) {
			JComboBox ch = (JComboBox) ie.getSource();
			setMaxDecomp(Integer.parseInt((String) ch.getSelectedItem()));
		}
	}

	public class EpsilonText {
		public JTextField text;

		public EpsilonText(TopInterface ti) {

			JPanel maxD = new JPanel();
			maxD.setLayout(new BorderLayout());
			maxD.add("West", new JLabel("epsilon            "));
			text = new JTextField("" + epsilon);
			maxD.add("East", text);
			// choice.addItemListener(this);
			ti.getPanel().add(maxD);
		}
	}

	public class TText {
		public JTextField text;

		public TText(TopInterface ti) {

			JPanel maxD = new JPanel();
			maxD.setLayout(new BorderLayout());
			maxD.add("West", new JLabel("t                  "));
			text = new JTextField("" + t);
			maxD.add("East", text);
			// choice.addItemListener(this);
			ti.getPanel().add(maxD);
		}
	}

	public class ShowColorCombo implements ItemListener {
		public JComboBox choice;

		public ShowColorCombo(TopInterface ti) {

			JPanel maxD = new JPanel();
			maxD.setLayout(new BorderLayout());
			maxD.add("West", new JLabel("Show Color            "));
			choice = new JComboBox();
			choice.addItem("No");
			choice.addItem("Yes");
			if (showColor)
				choice.setSelectedIndex(1);
			else
				choice.setSelectedIndex(0);// No is default
			maxD.add("East", choice);
			// choice.addItemListener(this);
			ti.getPanel().add(maxD);
		}

		@Override
		public void itemStateChanged(ItemEvent ie) {
			JComboBox ch = (JComboBox) ie.getSource();
			if (ch.getSelectedItem().equals("No"))
				showColor = false;
			else
				showColor = true;
			try {
				rebuildQuadtree();
			} catch (Exception e) {
			}
			reb.redraw();
		}
	}

	public class DecomposeMaximallyCombo implements ItemListener {
		public JComboBox choice;

		public DecomposeMaximallyCombo(TopInterface ti) {

			JPanel maxD = new JPanel();
			maxD.setLayout(new BorderLayout());
			maxD.add("West", new JLabel("Full Decomposition (slow)"));
			choice = new JComboBox();
			choice.addItem("No");
			choice.addItem("Yes");
			if (maxDecomposition)
				choice.setSelectedIndex(1);
			else
				choice.setSelectedIndex(0);// No is default
			maxD.add("East", choice);
			// choice.addItemListener(this);
			ti.getPanel().add(maxD);
		}

		@Override
		public void itemStateChanged(ItemEvent ie) {
			JComboBox ch = (JComboBox) ie.getSource();
			if (ch.getSelectedItem().equals("No"))
				maxDecomposition = false;
			else
				maxDecomposition = true;
			try {
				rebuildQuadtree();
			} catch (Exception e) {
			}
			reb.redraw();
		}
	}

	public class SubmitButton implements ActionListener {
		public SubmitButton(TopInterface ti) {

			JPanel maxD = new JPanel();
			maxD.setLayout(new BorderLayout());
			JButton submitButton = new JButton("    Change Parameters    ");
			submitButton.addActionListener(this);
			maxD.add("East", submitButton);
			ti.getPanel().add(maxD);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			String s;
			double d;
			int i;

			s = epsilonText.text.getText();
			try {
				d = Double.parseDouble(s);
				if (d < 0)
					d = epsilon;
			} catch (NumberFormatException nfe) {
				d = epsilon;
			}
			epsilonText.text.setText("" + d);
			setEpsilon(d);

			s = tText.text.getText();
			try {
				i = Integer.parseInt(s);
				if (i < 1)
					i = t;
			} catch (NumberFormatException nfe) {
				i = t;
			}
			tText.text.setText("" + i);
			setT(i);

			s = (String) maxDecompCombo.choice.getSelectedItem();
			setMaxDecomp(Integer.parseInt(s));

			s = (String) showColorCombo.choice.getSelectedItem();
			if (s.equals("Yes"))
				showColor = true;
			else
				showColor = false;

			s = (String) decomposeMaximallyCombo.choice.getSelectedItem();
			if (s.equals("Yes"))
				maxDecomposition = true;
			else
				maxDecomposition = false;

			try {
				rebuildQuadtree();
			} catch (Exception e) {
			}
			reb.redraw();
		}
	}
}
