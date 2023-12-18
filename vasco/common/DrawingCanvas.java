/* $Id: DrawingCanvas.java,v 1.4 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image; // May need to remove if Swing replacement
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;

// import java.awt.*;
import javax.swing.JPanel;

import vasco.regions.CBlock;
import vasco.regions.Colors;
import vasco.regions.ConnectedBlocks;
import vasco.regions.CursorStyle;
import vasco.regions.PolygonCursor;

/* -------------------------------------------
 * Drawing Canvas - provides drawing functions for the application
 * contains transformation matrix to scale based on zoom in/out and move from DrawingPanel
 * -------------------------------------------
 */
public class DrawingCanvas extends JPanel implements DrawingTarget {
	private static final int POINTSIZE = 6;
	private Image i;
	private Graphics offscr;
	private Rectangle orig; // defines the size of the drawing area on the screen, e.g., (0, 0, SIZE, SIZE)
	private Rectangle target;
	private Rectangle viewPort;
	private MouseHelp mh;
	private MouseDisplay mouseDisplay;
	
    private BufferedImage offscreenImage;
    
    private List<ColoredRectangle> coloredRectanglesToDraw = new ArrayList<>();
    private List<ColoredLine> coloredLinesToDraw = new ArrayList<>();
    // Add a list to store colored ovals
    private List<ColoredOval> coloredOvalsToDraw = new ArrayList<>();



	// Constructor
	public DrawingCanvas(Rectangle o, Rectangle viewPort, Image im, MouseDisplay md) {
		super(true);
		i = im;
		this.viewPort = viewPort;
		setPreferredSize(new Dimension(viewPort.width, viewPort.height)); // setSize(viewPort.width, viewPort.height);
		offscr = i.getGraphics();
		orig = target = o;
		mh = null;
		mouseDisplay = md;
        // Initialize the offscreen image
        offscreenImage = new BufferedImage(viewPort.width, viewPort.height, BufferedImage.TYPE_INT_ARGB);
    }

	// Set the mouse display
	public void setMouseDisplay(MouseDisplay md) {
		mouseDisplay = md;
	}

	// Change the viewport
	public void changeViewPort(Rectangle r) {
		viewPort = r;
		// i = createImage(r.width, r.height);
		redraw();
	}

	// Change the help information
	@Override
	public void changeHelp(int mask, String b1, String b2, String b3) {
		if (mh == null)
			mh = new MouseHelp(this, mouseDisplay, b1, b2, b3, mask);
		else
			mh.changeHelp(mask, b1, b2, b3);
	}

	// Get the operation mask
	public int getOperationMask() {
		return mh.getMask();
	}
	
	 // Method to add or update a colored rectangle
    public void addOrUpdateRectangle(Rectangle rect, Color color) {
        ColoredRectangle newColoredRect = new ColoredRectangle(rect, color);
        coloredRectanglesToDraw.remove(newColoredRect); // Remove existing rectangle with the same color
        coloredRectanglesToDraw.add(newColoredRect); // Add the new rectangle
        repaint();
    }
    
 // Method to add a colored line for redrawing, ensuring only two lines are stored
    public void addColoredLine(Point start, Point end, Color color) {
        if (coloredLinesToDraw.size() >= 2) {
            coloredLinesToDraw.clear(); // Clear last two lines forming X
        }
        coloredLinesToDraw.add(new ColoredLine(start, end, color)); // Add the new line
        repaint();
    }
    
    public void clearRectangles() {
    	coloredRectanglesToDraw.clear();
    	redraw();
    }
    
    public void clearOvals() {
    	coloredOvalsToDraw.clear();
    	redraw();
    }
    
    public void clearLines() {
	    coloredLinesToDraw.clear();
    	redraw();
    }
    
    public void changeRectangleColor(DRectangle rect, Color newColor) {
        // Find the rectangle in the list and update its color
        for (ColoredRectangle coloredRect : coloredRectanglesToDraw) {
            if (coloredRect.rectangle.equals(rect)) {
                coloredRect.color = newColor;
                repaint();
                return;
            }
        }

        // If the rectangle is not in the list, add it with the new color
//        addOrUpdateRectangle(rect, newColor);
    }


	// Redraw the canvas
	public void redraw() {
		// paint(getGraphics());
//		paintComponent(getGraphics());
		// TODO need to use repaint() to make the canvas not flicker but in order to use repaint all of the 
		// drawing logic needs to be added to paintComponent
		updateOffscreenBuffer();
		repaint(); // Use repaint() instead of paint() paint(getGraphics());
	}

	  // Override update() and make sure the 
 	 // background is not cleared. 
//	 @Override
//	public void update (Graphics g) {
//		paint (g);
//	}
	
//    public void addRectangleToDraw(Rectangle rect) {
//    	coloredRectanglesToDraw.add(rect);
//        repaint();
//    }
//
//    public void clearRectangles() {
//    	coloredRectanglesToDraw.clear();
//        repaint();
//    }
    
//    @Override
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g); // Clears the panel

//         Graphics2D g2d = (Graphics2D) g;
// 	    Graphics2D offGraphics = offscreenImage.createGraphics();

//         g2d.drawImage(offscreenImage, 0, 0, this);

//         // Draw the rectangles from the list
//         for (ColoredRectangle cr : coloredRectanglesToDraw) {
//             g2d.setColor(cr.color);
//             Rectangle rect = cr.rectangle;
//             g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
//         }
        
//         // Draw the lines
//         for (ColoredLine cl : coloredLinesToDraw) {
//             g2d.setColor(cl.color);
//             g2d.drawLine(cl.start.x, cl.start.y, cl.end.x, cl.end.y);
//         }
        
//         // Set anti-aliasing for smoother graphics
// 	    offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

// 	    // Clear the offscreen image
// 	    offGraphics.setColor(getBackground());
// 	    offGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());

// 	    // Draw the base image
// 	    offGraphics.drawImage(i, 0, 0, this);

// 	    // Dispose of the offscreen graphics context to release resources
// 	    offGraphics.dispose();

// 	    // Draw the offscreen image onto the component
// //	    g2d.drawImage(offscreenImage, 0, 0, this);
//     }
    
	// Method to update the offscreen buffer with new drawings
	private void updateOffscreenBuffer() {
		Graphics2D offGraphics = offscreenImage.createGraphics();
		// Set anti-aliasing for smoother graphics
		offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Clear the offscreen image
		offGraphics.setColor(getBackground());
		offGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());
		// Draw the base image
		offGraphics.drawImage(i, 0, 0, this);
		// Draw the rectangles and lines
		for (ColoredRectangle cr : coloredRectanglesToDraw) {
			offGraphics.setColor(cr.color);
			Rectangle rect = cr.rectangle;
			offGraphics.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		for (ColoredLine cl : coloredLinesToDraw) {
			offGraphics.setColor(cl.color);
			offGraphics.drawLine(cl.start.x, cl.start.y, cl.end.x, cl.end.y);
		}
	    // Draw the ovals from the list
	    for (ColoredOval co : coloredOvalsToDraw) {
	    	offGraphics.setColor(co.color);
	        Point newo = transPoint(co.x, co.y);
	        offGraphics.fillOval(newo.x - co.width / 2, newo.y - co.height / 2, co.width, co.height);
	    }
		offGraphics.dispose();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(offscreenImage, 0, 0, this);
	}

	// Overridden paintComponent method
//	@Override
//	protected void paintComponent(Graphics g) {
//	    super.paintComponent(g); // Clears the panel

	    // Cast to Graphics2D for more advanced graphics capabilities
//	    Graphics2D g2d = (Graphics2D) g;
//
//	    // Draw onto the offscreen image
//	    Graphics2D offGraphics = offscreenImage.createGraphics();
//
//	    // Set anti-aliasing for smoother graphics
//	    offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//	    // Clear the offscreen image
//	    offGraphics.setColor(getBackground());
//	    offGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());
//
//	    // Draw the base image
//	    offGraphics.drawImage(i, 0, 0, this);
//
//	    // Dispose of the offscreen graphics context to release resources
//	    offGraphics.dispose();
//
//	    // Draw the offscreen image onto the component
//	    g2d.drawImage(offscreenImage, 0, 0, this);
//	}

// 	private void highlightGridElements(Graphics2D g) {
// 	    // Logic to highlight grid elements based on the cursor position and state
// 	    // This might involve drawing rectangles, lines, or points on the grid
// 	    Point mousePosition = getMousePosition(); // Get current mouse position

// 	    if (mousePosition != null) {
// 	        // Convert screen coordinates to grid coordinates
// //	        Point gridPosition = transScrToGrid(mousePosition);

// 	        // Check if the mouse is over a valid grid cell or connected block
// 	        // and highlight if necessary
// 	        // You might need to access the state of the grid or cursor to determine
// 	        // what needs to be highlighted
// 	        // For example:
// 	        // g.setColor(Color.RED);
// 	        // g.drawRect(gridPosition.x, gridPosition.y, cellSize, cellSize);
// 	        // ...

// 	        // You can also use the cursor's current style to determine what to draw
// 	        // For example, if the cursor indicates a selected area, draw that area
// 	    }
	    
//	    CursorStyle cs;
//
//	    // If a conversion operation is in progress, the mouse cursor display is bypassed.
////	    if (runningThread != null)
////	        return;
//
//	    // Adjusts the mouse point coordinates to the offscreen graphics context.
//	    Point mCor = offscrG.adjustPoint(me.getPoint());
//	    // Translates the mouse coordinates from screen to the actual canvas coordinates.
//	    Point rCor = transMouseToScr(mCor);
//	    // Translates the canvas coordinates to grid coordinates.
//	    Point gCor = transScrToGrid(mCor);
//
//	    // Retrieves the current operation mode.
//	    int op = getCurrentOperation();
//	    
//	    System.out.println("Mouse Coordinates in Canvas: " + mCor.x + " " + mCor.y);
//	    System.out.println("Translated Screen Coordinates: " + rCor.x + " " + rCor.y);
//	    System.out.println("Translated Grid Coordinates: " + gCor.x + " " + gCor.y);
//	    System.out.println("Current Operation: " + op);
//
//	    // Checks if the user is interacting with a connected block.
//	    if (cb != null) {
//	        CBlock b;
//
//	        cs = new CursorStyle();
//	        // If the mouse is over a valid connected block, updates the cursor style.
//	        if (((b = ConnectedBlocks.inBlock(cb, grid, gCor.x, gCor.y)) != null) && b.valid)
//	            cs.add(new PolygonCursor(b.p, Colors.SELECTED_CELL));
//
//	        // If the cursor style has changed, triggers a redraw of the canvas.
//	        if (cursor.isDifferentCursor(cs)) {
//	            redrawMode = OFFSCR_REDRAW;
//	            redraw();
//
//	            cursor.move(offscrG, cs);
//	            cursor.move(overview, cs);
//	        }
//	        System.out.println("Mouse is over a connected block: " + b);
//	    } else {
//	        // If the cursor style is different based on the current operation and mouse location, triggers a redraw.
//	    	boolean isCursorDifferent = cursor.isDifferentCursor(pstruct.mouseMoved(rCor.x, rCor.y, mCor.x, mCor.y, op, sRect));
//	        System.out.println("Is Cursor Different: " + isCursorDifferent);	        
//	        if (isCursorDifferent) {
//	            redrawMode = OFFSCR_REDRAW;
//	            redraw();
//	            cs = pstruct.mouseMoved(rCor.x, rCor.y, mCor.x, mCor.y, op, sRect);
//	            cursor.move(offscrG, cs);
//	            cursor.move(overview, cs);
//	        }
//	    }
	// }

	// // Paint method to draw on the canvas
	// public void paint(Graphics g) {
	// if (g == null)
	// return;
	// g.drawImage(i, 0, 0, this);
	// }
    
    class ColoredRectangle {
        Rectangle rectangle;
        Color color;

        ColoredRectangle(Rectangle rectangle, Color color) {
            this.rectangle = rectangle;
            this.color = color;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ColoredRectangle that = (ColoredRectangle) obj;
            return color.equals(that.color);
        }

        @Override
        public int hashCode() {
            return color.hashCode();
        }
    }
    

    public class ColoredLine {
        private Point start;
        private Point end;
        private Color color;

        public ColoredLine(Point start, Point end, Color color) {
            this.start = start;
            this.end = end;
            this.color = color;
        }

        public Point getStart() {
            return start;
        }

        public void setStart(Point start) {
            this.start = start;
        }

        public Point getEnd() {
            return end;
        }

        public void setEnd(Point end) {
            this.end = end;
        }

        public Color getColor() {
            return color;
        }
    }
    
 // Class to store colored ovals
    class ColoredOval {
        double x, y;
        int width, height;
        Color color;

        ColoredOval(double x, double y, int width, int height, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

	// Get the current view rectangle
	@Override
	public Rectangle getView() {
		return target;
	}

	// Get the original rectangle
	@Override
	public Rectangle getOrig() {
		return orig;
	}

	// ----------------------------------------------------

	// Zoom in/out based on a center point and a factor
	void zoom(DPoint center, double factor) {
		double percX = (center.x - orig.x) / orig.width;
		double percY = (center.y - orig.y) / orig.height;

		target = new Rectangle(0, 0, (int) (factor * orig.width), (int) (factor * orig.height));
		moveX(percX);
		moveY(percY);
		
//	    repaint(); // Trigger a redraw of the canvas
	}
	
	

	// Move along the X-axis
	void moveX(double p) {
		double percent = Math.max(0, Math.min(1, p));
		target = new Rectangle(Math.round((float) (viewPort.x + percent * (viewPort.width - target.width))), target.y,
				target.width, target.height);
	}

	// Move along the Y-axis
	void moveY(double p) {
		double percent = Math.max(0, Math.min(1, p));
		target = new Rectangle(target.x, Math.round((float) (viewPort.y + percent * (viewPort.height - target.height))),
				target.width, target.height);
	}

	// Calculate the percentage of the current position along the X-axis
	float getXperc() {
		if (viewPort.width == target.width)
			return 0;
		return Math.max(0, Math.min(1, (target.x - viewPort.x) / (float) (viewPort.width - target.width)));
	}

	// Calculate the percentage of the current position along the Y-axis
	float getYperc() {
		if (viewPort.height == target.height)
			return 0;
		return Math.max(0, Math.min(1, (target.y - viewPort.y) / (float) (viewPort.height - target.height)));
	}

	// Get the center point of the current view
	public DPoint getCenter() {
		return transPointT(viewPort.x + viewPort.width / 2, viewPort.y + viewPort.height / 2);
	}

	// Get the upper-left point of the current view
	DPoint getUL() {
		return transPointT(viewPort.x, viewPort.y);
	}

	// Get the upper-right point of the current view
	DPoint getUR() {
		return transPointT(viewPort.x + viewPort.width, viewPort.y);
	}

	// Get the lower-left point of the current view
	DPoint getLL() {
		return transPointT(viewPort.x, viewPort.y + viewPort.height);
	}

	// Get the lower-right point of the current view
	DPoint getLR() {
		return transPointT(viewPort.x + viewPort.width, viewPort.y + viewPort.height);
	}

	// ---------------- transformations -------------------

	// Adjust a point to be within the original rectangle
	@Override
	public Point adjustPoint(Point p) {
		return new Point(Math.min(Math.max(orig.x, p.x), orig.x + orig.width),
				Math.min(Math.max(orig.y, p.y), orig.y + orig.height));
	}

	// Transform a point from target coordinates to global coordinates
	@Override
	public DPoint transPointT(Point p) {
		return transPointT(p.x, p.y);
	}

	// Transform a point from target coordinates to global coordinates
	DPoint transPointT(int x, int y) {
		return new DPoint(orig.width * (x - target.x) / (double) target.width + orig.x,
				orig.height * (y - target.y) / (double) target.height + orig.y);
	}

	// Transform a point from global coordinates to target coordinates
	public Point transPoint(DPoint p) {
		return transPoint(p.x, p.y);
	}

	// Transform a point from global coordinates to target coordinates
	@Override
	public Point transPoint(double x, double y) {
		return new Point((int) Math.round(target.width * (x - orig.x) / orig.width + target.x),
				(int) Math.round(target.height * (y - orig.y) / orig.height + target.y));
	}

	// Transform a vector from global coordinates to target coordinates
	private Point transVector(double dx, double dy) {
		return new Point((int) (target.width * dx / orig.width), (int) (target.height * dy / orig.height));
	}

	// ---------------- drawing primitives ----------------

	// Check if a rectangle is visible in the current view
	@Override
	public boolean visible(DRectangle r) {
		Point newo = transPoint(r.x, r.y);
		Point newv = transVector(r.width, r.height);
		Rectangle newr = new Rectangle(newo.x, newo.y, newv.x, newv.y);
		return newr.intersects(orig);
	}

	// Check if a rectangle is visible in the current view
	@Override
	public boolean visible(Rectangle r) {
		Point newo = transPoint(r.x, r.y);
		Point newv = transVector(r.width, r.height);
		Rectangle newr = new Rectangle(newo.x, newo.y, newv.x, newv.y);
		return newr.intersects(orig);
	}

	// Draw a string at the specified position
	@Override
	public void drawString(String s, double x, double y) {
		Point newo = transPoint(x, y);
		offscr.drawString(s, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2);
	}

	// Draw a string at the specified position with a specified font
	@Override
	public void drawString(String s, double x, double y, Font f) {
		Font oldfont = offscr.getFont();
		offscr.setFont(f);

		Point newo = transPoint(x, y);
		offscr.drawString(s, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2);

		offscr.setFont(oldfont);
	}

	// Draw an image at the specified position
	public void drawImg(Image img, double x, double y) {
		Point newo = transPoint(x, y);
		offscr.drawImage(img, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2, this);
	}

	// Draw a string directly using the graphics object
	@Override
	public void directDrawString(String s, double x, double y) {
		Graphics cur = getGraphics();
		Point newo = transPoint(x, y);
		cur.drawString(s, newo.x - POINTSIZE / 2, newo.y - POINTSIZE / 2);
	}

	// Draw a rectangle with specified dimensions
	@Override
	public void drawRect(double xx, double yy, double ww, double hh) {
		Point newo = transPoint(xx, yy);
		Point newv = transVector(ww, hh);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		offscr.drawRect(x1, y1, x2 - x1, y2 - y1);
	}

	// Fill a rectangle with specified dimensions
	@Override
	public void fillRect(double xx, double yy, double ww, double hh) {
		Point newo = transPoint(xx, yy);
		Point newv = transVector(ww, hh);
		int x1 = Math.max(0, newo.x);
		int y1 = Math.max(0, newo.y);
		int x2 = Math.min(orig.width, newo.x + newv.x);
		int y2 = Math.min(orig.height, newo.y + newv.y);
		offscr.fillRect(x1, y1, x2 - x1, y2 - y1);
	}

	// Fill a polygon
	@Override
	public void fillPoly(DPolygon p) {
		Polygon plg = new Polygon();
		for (int i = 0; i < p.Size(); i++) {
			Point pnt = transPoint(p.vertex(i));
			plg.addPoint(pnt.x, pnt.y);
		}
		offscr.fillPolygon(plg);
	}

	// Fill an oval with specified dimensions
	@Override
	public void fillOval(double xx, double yy, int ww, int hh) {
		Point newo = transPoint(xx, yy);
		offscr.fillOval(newo.x - ww / 2, newo.y - hh / 2, ww, hh);
	}

	// Draw an oval with specified dimensions
	@Override
	public void drawOval(double x, double y, int width, int height) {
		Point newo = transPoint(x, y);
		offscr.drawOval(newo.x - width / 2, newo.y - height / 2, width, height);
	}

	// Draw an arc with specified dimensions
	@Override
	public void drawArc(double x, double y, double w, double h, int sA, int rA) {
		Point newo = transPoint(x, y);
		Point newd = transVector(w, h);
		offscr.drawArc(newo.x, newo.y, newd.x, newd.y, sA, rA);
	}

	// Draw an oval with a specified center and radii
	@Override
	public void drawOval(DPoint p, double radx, double rady) {
		Point newo = transPoint(p.x, p.y);
		Point newd = transVector(radx, rady);
		offscr.drawOval(newo.x - newd.x, newo.y - newd.y, 2 * newd.x, 2 * newd.y);
	}

	// Fill an oval with a specified center and radii
	@Override
	public void fillOval(DPoint p, double radx, double rady) {
		Point newo = transPoint(p.x, p.y);
		Point newd = transVector(radx, rady);
		offscr.fillOval(newo.x - newd.x, newo.y - newd.y, 2 * newd.x, 2 * newd.y);
	}

	// Draw a line from (x1, y1) to (x2, y2)
	@Override
	public void drawLine(double x1, double y1, double x2, double y2) {
		// Clear the Green Boxes in the Colored Rectangles since a new grid is being drawn of a new Data Structure had been selected
//		if (!coloredRectanglesToDraw.isEmpty()) {
//			
//		}
//		coloredRectanglesToDraw.clear();
		
		Point new1 = transPoint(x1, y1);
		Point new2 = transPoint(x2, y2);
		offscr.drawLine(new1.x, new1.y, new2.x, new2.y);
	}

	// Set the drawing color
	@Override
	public void setColor(Color c) {
		offscr.setColor(c);
	}

	// ------------------------------------------

	// Draw a line directly using the graphics object with a specified color
	@Override
	public void directLine(Color c, double x1, double y1, double x2, double y2) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point start  = transPoint(x1, y1);
		Point end = transPoint(x2, y2);
		cur.drawLine(start.x, start.y, end.x, end.y);
		
        // Add the line to the list for redrawing
        addColoredLine(start, end, c);
	}

	// Draw a rectangle directly using the graphics object with a specified color
	@Override
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
	    
//	    if (c == Color.blue) {
//	    	clearAllExceptBlue();
//	    } else {
//		    coloredRectanglesToDraw.clear();
//	    }
	    clearRectangles();

	    // Empty of current lines forming X because a new rect means the mouse moved
	    clearLines();
	    
	    // Add the rectangle to the list for redrawing
	    Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
	    if (c != null) {
	    	addOrUpdateRectangle(rect, c);
	    }
	}
	
	// Method to clear all rectangles except those colored blue
	public void clearAllExceptBlue() {
	    // Using an iterator to avoid ConcurrentModificationException while removing elements
	    Iterator<ColoredRectangle> iterator = coloredRectanglesToDraw.iterator();
	    while (iterator.hasNext()) {
	        ColoredRectangle cr = iterator.next();
	        if (!cr.color.equals(Color.blue)) {
	            // Remove the rectangle if it is not blue
	            iterator.remove();
	        }
	    }
	    repaint(); // Trigger repaint to reflect changes on the canvas
	}
	
	// Draw a rectangle directly using the graphics object with a specified color
//	public void directMoveRect(Color c, double x, double y, double w, double h) {
//	    Graphics cur = getGraphics();
//	    cur.setColor(c);
//	    Point newo = transPoint(x, y);
//	    Point newv = transVector(w, h);
//	    int x1 = Math.max(0, newo.x);
//	    int y1 = Math.max(0, newo.y);
//	    int x2 = Math.min(orig.width, newo.x + newv.x);
//	    int y2 = Math.min(orig.height, newo.y + newv.y);
//	    cur.drawRect(x1, y1, x2 - x1, y2 - y1);
//
//	    // Empty of current lines forming X because a new rect means the mouse moved
//	    coloredLinesToDraw.clear();
//	    coloredRectanglesToDraw.clear();
//	    // Add the rectangle to the list for redrawing
//	    Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
//	    if (c != null) {
//	    	addOrUpdateRectangle(rect, c);
//	    }
//	}


	// Draw a thick rectangle directly using the graphics object with a specified
	// color and thickness
	@Override
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

	// Draw a filled rectangle directly using the graphics object with a specified
	// color
	@Override
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

	// Draw a filled oval directly using the graphics object with a specified color
	@Override
	public void directFillOval(Color c, double x, double y, int w, int h) {
	    Graphics cur = getGraphics();
	    cur.setColor(c);
	    Point newo = transPoint(x, y);
	    cur.fillOval(newo.x - w / 2, newo.y - h / 2, w, h);
	    
//	    coloredOvalsToDraw.clear(); // Remove the existing oval

	    // Add the oval to the list for redrawing
	    coloredOvalsToDraw.add(new ColoredOval(x, y, w, h, c));
	    repaint();
	}

	// Draw an oval directly using the graphics object with a specified color
	@Override
	public void directDrawOval(Color c, double x, double y, int w, int h) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		cur.drawOval(newo.x - w / 2, newo.y - h / 2, w, h);
	}

	// Draw an arc directly using the graphics object with a specified color
	@Override
	public void directDrawArc(Color c, double x, double y, double w, double h, int sA, int rA) {
		Graphics cur = getGraphics();
		cur.setColor(c);
		Point newo = transPoint(x, y);
		Point newd = transVector(w, h);
		cur.drawArc(newo.x, newo.y, newd.x, newd.y, sA, rA);
	}
}
