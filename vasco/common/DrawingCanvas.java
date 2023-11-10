/* $Id: DrawingCanvas.java,v 1.4 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;

/* -------------------------------------------
 * Drawing Canvas - provides drawing functions for the application
 * contains transformation matrix to scale based on zoom in/out and move from DrawingPanel
 * -------------------------------------------
 */

public class DrawingCanvas extends Canvas implements DrawingTarget {
	private static final int POINTSIZE = 6;
	private Image i;
	private Graphics offscr;
	private Rectangle orig; // defines size of drawing area on the screen, e.g. (0, 0, SIZE, SIZE)
	private Rectangle target;
	private Rectangle viewPort;
	private MouseHelp mh;
	private MouseDisplay mouseDisplay;

	public DrawingCanvas(Rectangle o, Rectangle viewPort, Image im, MouseDisplay md) {
		i = im;
		this.viewPort = viewPort;
		setSize(viewPort.width, viewPort.height);
		offscr = i.getGraphics();
		orig = target = o;
		mh = null;
		mouseDisplay = md;
	}

	public void setMouseDisplay(MouseDisplay md) {
		mouseDisplay = md;
	}

	public void changeViewPort(Rectangle r) {
		viewPort = r;
		// i = createImage(r.width, r.height);
		redraw();
	}

	public void changeHelp(int mask, String b1, String b2, String b3) {
		if (mh == null)
			mh = new MouseHelp(this, mouseDisplay, b1, b2, b3, mask);
		else
			mh.changeHelp(mask, b1, b2, b3);
	}

	public int getOperationMask() {
		return mh.getMask();
	}

	public void redraw() {
		paint(getGraphics());
	}

	public void paint(Graphics g) {
		if (g == null)
			return;
		g.drawImage(i, 0, 0, this);
	}

	public Rectangle getView() {
		return target;
	}

	public Rectangle getOrig() {
		return orig;
	}

	// public void update(Graphics g) {
	// paint(g);
	// }

	// ----------------------------------------------------

	void zoom(DPoint center, double factor) {
		// center - 0..512 coordinates
		// target = new Rectangle(target.x - target.width / 2,
		// target.y - target.height / 2,
		// 2 * target.width, 2 * target.height);

		double percX = (center.x - orig.x) / orig.width;
		double percY = (center.y - orig.y) / orig.height;

		target = new Rectangle(0, 0, (int) (factor * orig.width), (int) (factor * orig.height));
		moveX(percX);
		moveY(percY);
	}

	void moveX(double p) {
		double percent = Math.max(0, Math.min(1, p));
		target = new Rectangle(Math.round((float) (viewPort.x + percent * (viewPort.width - target.width))), target.y,
				target.width, target.height);
		// target = new Rectangle(Math.round((float)(orig.x + percent * (orig.width -
		// target.width))),
		// target.y, target.width, target.height);
	}

	void moveY(double p) {
		double percent = Math.max(0, Math.min(1, p));
		target = new Rectangle(target.x, Math.round((float) (viewPort.y + percent * (viewPort.height - target.height))),
				target.width, target.height);
		// target = new Rectangle(target.x,
		// Math.round((float)(orig.y + percent * (orig.height - target.height))),
		// target.width, target.height);
	}

	float getXperc() {
		if (viewPort.width == target.width)
			return 0;
		return Math.max(0, Math.min(1, (target.x - viewPort.x) / (float) (viewPort.width - target.width)));
	}

	float getYperc() {
		if (viewPort.height == target.height)
			return 0;
		return Math.max(0, Math.min(1, (target.y - viewPort.y) / (float) (viewPort.height - target.height)));
	}

	public DPoint getCenter() {
		return transPointT(viewPort.x + viewPort.width / 2, viewPort.y + viewPort.height / 2);
	}

	DPoint getUL() {
		return transPointT(viewPort.x, viewPort.y);
	}

	DPoint getUR() {
		return transPointT(viewPort.x + viewPort.width, viewPort.y);
	}

	DPoint getLL() {
		return transPointT(viewPort.x, viewPort.y + viewPort.height);
	}

	DPoint getLR() {
		return transPointT(viewPort.x + viewPort.width, viewPort.y + viewPort.height);
	}

	// ---------------- transformations -------------------

	public Point adjustPoint(Point p) {
		return new Point(Math.min(Math.max(orig.x, p.x), orig.x + orig.width),
				Math.min(Math.max(orig.y, p.y), orig.y + orig.height));
	}

	public DPoint transPointT(Point p) {
		return transPointT(p.x, p.y);
	}

	DPoint transPointT(int x, int y) {
		return new DPoint(orig.width * (x - target.x) / (double) target.width + orig.x,
				orig.height * (y - target.y) / (double) target.height + orig.y);
	}

	public Point transPoint(DPoint p) {
		return transPoint(p.x, p.y);
	}

	public Point transPoint(double x, double y) {
		// <0,512> -> global size
		return new Point((int) Math.round(target.width * (x - orig.x) / (double) orig.width + target.x),
				(int) Math.round(target.height * (y - orig.y) / (double) orig.height + target.y));
	}

	private Point transVector(double dx, double dy) {
		return new Point((int) (target.width * dx / orig.width), (int) (target.height * dy / orig.height));
	}

	// ---------------- drawing primitives ----------------

	public boolean visible(DRectangle r) {
		Point newo = transPoint(r.x, r.y);
		Point newv = transVector(r.width, r.height);
		Rectangle newr = new Rectangle(newo.x, newo.y, newv.x, newv.y);
		return newr.intersects(orig);
	}

	public boolean visible(Rectangle r) {
		Point newo = transPoint(r.x, r.y);
		Point newv = transVector(r.width, r.height);
		Rectangle newr = new Rectangle(newo.x, newo.y, newv.x, newv.y);
		return newr.intersects(orig);
	}

	public void drawString(String s, double x, double y) {
		Point newo = transPoint(x, y);
		offscr.drawString(s, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2);
	}

	public void drawString(String s, double x, double y, Font f) {
		Font oldfont = offscr.getFont();
		offscr.setFont(f);

		Point newo = transPoint(x, y);
		offscr.drawString(s, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2);

		offscr.setFont(oldfont);
	}

	public void drawImg(Image img, double x, double y) {
		Point newo = transPoint(x, y);
		offscr.drawImage(img, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2, this);
	}

	public void directDrawString(String s, double x, double y) {
		Graphics cur = getGraphics();
		Point newo = transPoint(x, y);
		cur.drawString(s, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2);
	}

	public void drawRect(double xx, double yy, double ww, double hh) {
		Point newo = transPoint(xx, yy);
		Point newv = transVector(ww, hh);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		offscr.drawRect(x1, y1, x2 - x1, y2 - y1);
	}

	public void fillRect(double xx, double yy, double ww, double hh) {
		Point newo = transPoint(xx, yy);
		Point newv = transVector(ww, hh);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		offscr.fillRect(x1, y1, x2 - x1, y2 - y1);
	}

	public void fillPoly(DPolygon p) {
		Polygon plg = new Polygon();
		for (int i = 0; i < p.Size(); i++) {
			Point pnt = transPoint(p.vertex(i));
			plg.addPoint(pnt.x, pnt.y);
		}
		offscr.fillPolygon(plg);
	}

	public void fillOval(double xx, double yy, int ww, int hh) {
		Point newo = transPoint(xx, yy);
		offscr.fillOval(newo.x - ww / 2, newo.y - hh / 2, ww, hh);
	}

	public void drawOval(double x, double y, int width, int height) {
		Point newo = transPoint(x, y);
		offscr.drawOval(newo.x - width / 2, newo.y - height / 2, width, height);
	}

	public void drawArc(double x, double y, double w, double h, int sA, int rA) {
		Point newo = transPoint(x, y);
		Point newd = transVector(w, h);
		offscr.drawArc(newo.x, newo.y, newd.x, newd.y, sA, rA);
	}

	public void drawOval(DPoint p, double radx, double rady) {
		Point newo = transPoint(p.x, p.y);
		Point newd = transVector(radx, rady);
		offscr.drawOval(newo.x - newd.x, newo.y - newd.y, 2 * newd.x, 2 * newd.y);
	}

	public void fillOval(DPoint p, double radx, double rady) {
		Point newo = transPoint(p.x, p.y);
		Point newd = transVector(radx, rady);
		offscr.fillOval(newo.x - newd.x, newo.y - newd.y, 2 * newd.x, 2 * newd.y);
	}

	public void drawLine(double x1, double y1, double x2, double y2) {
		// System.out.println(x1 + " " + y1 + " " + x2 + " " + y2);

		Point new1 = transPoint(x1, y1);
		Point new2 = transPoint(x2, y2);

		// System.out.println(new1.x + " " + new1.y + " " + new2.x + " " + new2.y);

		offscr.drawLine(new1.x, new1.y, new2.x, new2.y);
	}

	public void setColor(Color c) {
		offscr.setColor(c);
	}

	// ------------------------------------------

	public void directLine(Color c, double x1, double y1, double x2, double y2) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point new1 = transPoint(x1, y1);
		Point new2 = transPoint(x2, y2);

		cur.drawLine(new1.x, new1.y, new2.x, new2.y);
	}

	public void directRect(Color c, double x, double y, double w, double h) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		Point newv = transVector(w, h);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		cur.drawRect(x1, y1, x2 - x1, y2 - y1);
	}

	public void directThickRect(Color c, double xx, double yy, double ww, double hh, int thk) {
		Graphics cur = getGraphics();
		Graphics2D g2d = (Graphics2D) cur;
		Stroke sk = g2d.getStroke();
		g2d.setColor(c);
		g2d.setStroke(new BasicStroke(thk));
		Point newo = transPoint(xx, yy);
		Point newv = transVector(ww, hh);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		g2d.drawRect(x1, y1, x2 - x1, y2 - y1);
		g2d.setStroke(sk);
	}

	public void directFillRect(Color c, double x, double y, double w, double h) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		Point newv = transVector(w, h);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		cur.fillRect(x1, y1, x2 - x1, y2 - y1);
	}

	public void directFillOval(Color c, double x, double y, int w, int h) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		cur.fillOval(newo.x - w / 2, newo.y - h / 2, w, h);
	}

	public void directDrawOval(Color c, double x, double y, int w, int h) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		cur.drawOval(newo.x - w / 2, newo.y - h / 2, w, h);
	}

	public void directDrawArc(Color c, double x, double y, double w, double h, int sA, int rA) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		Point newd = transVector(w, h);
		cur.drawArc(newo.x, newo.y, newd.x, newd.y, sA, rA);
	}

}
