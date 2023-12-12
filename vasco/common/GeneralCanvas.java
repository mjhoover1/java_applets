/* $Id: GeneralCanvas.java,v 1.8 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import vasco.regions.ConvertThread;

// ------------- P Canvas -------------------

/**
 * Abstract class representing a general canvas with common functionality for
 * drawing and handling mouse events. This class serves as a base for creating
 * custom canvas elements in a graphical user interface, with support for
 * animation and interaction via mouse events.
 */
public abstract class GeneralCanvas implements CanvasIface, CommonConstants, MouseListener, MouseMotionListener {

	// Drawing targets for the canvas
	protected DrawingTarget offscrG; // Offscreen graphics target
	protected DrawingTarget overview; // Overview graphics target
	protected DrawingTarget[] allDrawingTargets; // Array of all drawing targets

	// Bounding rectangle of the entire canvas
	protected DRectangle wholeCanvas; // Defines the size and position of the canvas

	// Offscreen image for double buffering and last mouse position
	Image offscr;
	protected Point lastP; // Stores the last mouse position in screen coordinates

	// Animation-related variables for controlling animations within the canvas
	int waitTime; // Wait time between animation frames
	protected int successMode; // Mode indicating successful animation completion
	protected int searchMode; // Current mode of search animation
	protected WithinStats withinStats; // Stores statistics for 'within' search operations
	protected int gridLevel; // Current level of the grid for drawing
	AnimationPanel animPanel; // Panel for controlling animation settings
	protected QueryObject lastNear; // Last object near a query point
	protected QueryObject lastWindow; // Last object in a query window
	protected Vector polyRange; // Stores a range of polygon points for queries
	protected boolean gridOn; // Flag to indicate if the grid should be drawn
	protected DSector sec; // Sector object for sector-based queries
	public Vector historyList = new Vector(); // History of actions for undo functionality

	// Top-level interface and the thread controlling the canvas
	protected TopInterface topInterface; // Interface for top-level application interaction
	protected VascoThread runningThread; // Thread for handling canvas operations

	/**
	 * Inner class representing statistics for the 'within' operation. This class is
	 * used to store and manage data related to the within search operation, such as
	 * mode, distance, and blending options.
	 */
	public class WithinStats implements IDEval {
		int mode; // Mode of the within operation
		double dist; // Distance parameter for the operation
		boolean blend; // Flag to indicate if blending is to be used

		// Default constructor
		public WithinStats() {
		}

		/**
		 * Sets the values for the within statistics.
		 *
		 * @param mode  The mode of operation.
		 * @param dist  The distance for the operation.
		 * @param blend The blending flag.
		 */
		public void setValues(int mode, double dist, boolean blend) {
			this.mode = mode;
			this.dist = dist;
			this.blend = blend;
		}

		// Getters for the properties
		public boolean getBlend() {
			return blend;
		}

		public double getDist() {
			return dist;
		}

		@Override
		public int getValue() {
			return mode;
		}
	}

	// Constants representing different types of mouse events
	public final static int MOUSE_ENTERED = 0;
	public final static int MOUSE_EXITED = 1;
	public final static int MOUSE_PRESSED = 2;
	public final static int MOUSE_MOVED = 3;
	public final static int MOUSE_DRAGGED = 4;
	public final static int MOUSE_RELEASED = 5;
	public final static int MOUSE_CLICKED = 6;

	/**
	 * Constructor for GeneralCanvas. Initializes a new canvas with given
	 * parameters, sets up drawing targets, and configures the animation panel.
	 *
	 * @param can      The bounding rectangle of the canvas.
	 * @param dt       The primary drawing target.
	 * @param overview The overview drawing target.
	 * @param m        The panel containing the canvas.
	 * @param ti       The top-level interface.
	 */
	public GeneralCanvas(DRectangle can, DrawingTarget dt, DrawingTarget overview, JPanel m, TopInterface ti) {
		topInterface = ti;
		animPanel = new AnimationPanel(m);
		offscrG = dt;
		this.overview = overview;
		allDrawingTargets = new DrawingTarget[2];
		allDrawingTargets[0] = offscrG;
		allDrawingTargets[1] = overview;
		wholeCanvas = can;

		drawBackground(offscrG);
		gridLevel = 0;
		lastNear = null;
		lastWindow = null;
		gridOn = false;

		runningThread = null;
		withinStats = new WithinStats();
		sec = new DSector(new DPoint(wholeCanvas.x / 2, wholeCanvas.y / 2), 50, 50);
	}

	/**
	 * Abstract method to get the current operation's name.
	 *
	 * @return The name of the current operation.
	 */
	abstract protected String getCurrentOperationName();
	
	
	protected OpFeature getCurrentOpFeature() {
		int i;
		int op = getCurrentOperation();
		for (i = 0; i < opFeature.length; i++)
			if (opFeature[i] != null) {
				if (opFeature[i].ID == op)
					break;
			}
		if (i >= opFeature.length) {
			return null;
		}
		return opFeature[i].getFeature();
	}

    /**
     * Abstract method to get the current operation's ID.
     *
     * @return The ID of the current operation.
     */
	protected int getCurrentOperation() {
		String op = getCurrentOperationName();
		if (op != null) {
			for (int i = 0; i < opFeature.length; i++)
				if (opFeature[i] != null) {
					if (opFeature[i].name.equals(op))
						return opFeature[i].ID;
				}
		}
		return -1;
	}

	/**
	 * Abstract method to get the search mode mask for the current operation.
	 *
	 * @return The search mode mask.
	 */
	abstract protected int getSearchModeMask();

	/**
	 * Abstract method to get the allowed overlap query objects for the current
	 * operation.
	 *
	 * @return The allowed overlap query objects.
	 */
	abstract protected int getAllowedOverlapQueryObjects();

	/**
	 * Abstract method to perform the 'nearest' operation.
	 *
	 * @param p    The query object for the 'nearest' operation.
	 * @param dist The distance for the 'nearest' operation.
	 * @param off  The drawing targets.
	 */
	abstract protected void nearest(QueryObject p, double dist, DrawingTarget[] off);

	/**
	 * Abstract method to perform the 'search' operation.
	 *
	 * @param s   The query object for the 'search' operation.
	 * @param off The drawing targets.
	 */
	abstract protected void search(QueryObject s, DrawingTarget[] off);

	/**
	 * Abstract method to set the help text for the current operation.
	 */
	protected void setHelp() {
		OpFeature of = getCurrentOpFeature();
		topInterface.getHelpArea().setText(Tools.formatHelp(of.helpText, topInterface.getHelpArea().getColumns()));
		offscrG.changeHelp(of.buttonMask, of.h1, of.h2, of.h3);
	}

	// abstract protected void setHelp();

	@Override
	public void itemStateChanged(ItemEvent ie) {
		// operation selection has changed
		polyRange = new Vector();

		if (runningThread != null)
			terminate();
		
		int currOperation = getCurrentOperation();

		if (currOperation != previousOpfeature) {
			if (currOperation == OPFEATURE_WINDOW) {
				animPanel.setOverlap();
				SearchMode sm = new SearchMode(getSearchModeMask());
				WithinMode wm = new WithinMode(sm, getAllowedOverlapQueryObjects());
				searchMode = sm.getSearchMode();
				withinStats.setValues(wm.getWithinMode(), wm.getWithinDist(), wm.getBlend());
				if (lastWindow != null) {
					search(lastWindow, allDrawingTargets);
				}
			}

			if (currOperation == OPFEATURE_NEAREST || currOperation == OPFEATURE_WITHIN) {
				animPanel.setNearest();
				WithinMode wm = new WithinMode(getCurrentOperation() == OPFEATURE_WITHIN);
				withinStats.setValues(wm.getWithinMode(), wm.getWithinDist(), wm.getBlend());
				if (lastNear != null)
					nearest(lastNear, withinStats.getDist(), allDrawingTargets);
			}
		}
		
		previousOpfeature = currOperation;

		setHelp();
		redraw();
	}

	/**
	 * Class representing an animation panel with controls for starting, pausing,
	 * and stopping the animation.
	 */
	class AnimationPanel implements ActionListener, AdjustmentListener {
		final String RUNMODE_CONTINUOUS_S = "continuous";
		final String RUNMODE_OBJECT_S = "stop on object";
		final String RUNMODE_SUCCESS_S = "stop on success";

		JButton start, stop, pauseresume;
		JScrollBar ranger;

		JScrollBar progress;
		MouseHelp starthelp, pauseresumehelp, stophelp;
		JComboBox<String> runmode = new JComboBox<>();

		/**
		 * Constructs an AnimationPanel with controls for animation.
		 *
		 * @param r The panel containing the animation controls.
		 */
		AnimationPanel(JPanel r) {
			r.setLayout(new GridLayout(4, 1));

			JPanel anim = new JPanel();
			anim.setLayout(new GridLayout(1, 2));
			anim.add(new JLabel("Speed"));
			ranger = new JScrollBar(Adjustable.HORIZONTAL, 5, 1, 0, 10);
			setWaitTime(100 * (15 - ranger.getValue()));
			anim.add(ranger);
			r.add(anim);

			new MouseHelp(ranger, topInterface.getMouseDisplay(), "Drag slider to change animation speed", "", "");

			JPanel progressPanel = new JPanel();
			progressPanel.setLayout(new GridLayout(1, 2));
			progressPanel.add(new JLabel("Progress"));
			progressPanel.add(progress = new JScrollBar(Adjustable.HORIZONTAL, 0, 1, 0, 10));
			r.add(progressPanel);

			new MouseHelp(progress, topInterface.getMouseDisplay(), "Drag to view animation progress", "", "");

			JPanel buts = new JPanel();
			buts.setLayout(new GridLayout(1, 3));
			buts.add(start = new JButton("Start"));
			buts.add(pauseresume = new JButton("Pause"));
			buts.add(stop = new JButton("Stop"));
			starthelp = new MouseHelp(start, topInterface.getMouseDisplay(), "Start animation", "", "");
			pauseresumehelp = new MouseHelp(pauseresume, topInterface.getMouseDisplay(), "Pause animation", "", "",
					"Resume animation", "", "");
			stophelp = new MouseHelp(stop, topInterface.getMouseDisplay(), "Stop animation", "", "");
			r.add(buts);

			// runmode = new JComboBox<>();
			runmode.addItem(RUNMODE_CONTINUOUS_S); // runmode.add(RUNMODE_CONTINUOUS_S);
			runmode.addItem(RUNMODE_OBJECT_S);
			JPanel runmodeP = new JPanel();
			runmodeP.setLayout(new BorderLayout());
			runmodeP.add("West", new JLabel("Run Mode:"));
			runmodeP.add("Center", runmode);
			r.add(runmodeP);

			resetButtons();

			ranger.addAdjustmentListener(this);
			progress.addAdjustmentListener(this);
			start.addActionListener(this);
			pauseresume.addActionListener(this);
			stop.addActionListener(this);
		}

		/**
		 * Sets the overlap mode for the animation panel.
		 */
		void setOverlap() {
			if (runmode.getItemCount() == 2)
				runmode.addItem(RUNMODE_SUCCESS_S);
		}

		/**
		 * Sets the nearest mode for the animation panel.
		 */
		void setNearest() {
			if (runmode.getItemCount() == 3)
				runmode.removeItem(RUNMODE_SUCCESS_S);
		}

		/**
		 * Initializes the progress bar with the specified number of steps.
		 *
		 * @param nrSteps The number of steps for the progress bar.
		 */
		void initProgress(int nrSteps) {
			progress.setMaximum(nrSteps);
		}

		/**
		 * Resets the buttons in the animation panel.
		 */
		void resetButtons() {
			start.setText("Start"); // setText instead of setLabel
			pauseresume.setText("Pause"); // setText instead of setLabel
			pauseresumehelp.frontHelp();
			pauseresume.setEnabled(false);
			stop.setEnabled(false);
		}

		/**
		 * Gets the success mode based on the selected run mode.
		 *
		 * @return The success mode.
		 */
		int getSuccess() {
			if (runmode.getSelectedItem().equals(RUNMODE_CONTINUOUS_S))
				return CommonConstants.RUNMODE_CONTINUOUS;
			else if (runmode.getSelectedItem().equals(RUNMODE_OBJECT_S))
				return CommonConstants.RUNMODE_OBJECT;
			else if (runmode.getSelectedItem().equals(RUNMODE_SUCCESS_S))
				return CommonConstants.RUNMODE_SUCCESS;
			Thread.dumpStack();
			return -1;
		}

		/**
		 * Handles adjustment events for sliders.
		 *
		 * @param ae The adjustment event.
		 */
		@Override
		public void adjustmentValueChanged(AdjustmentEvent ae) {
		    if (ae.getSource() == ranger) {
		        // Assuming the slider range is 0 to 10, with 5 mapping to 1000 ms
		        int sliderValue = ae.getValue();
		        // Mapping the slider value to delay, such that slider value 5 gives 1000 ms
		        // Adjust the formula as necessary
		        int delay = 1000 + (sliderValue - 5) * -200; // 200 ms increment/decrement for each slider unit
		        System.out.println("Slider value: " + sliderValue + ", Calculated delay: " + delay); // Debug statement
		        setWaitTime(delay);
		    } else if (ae.getSource() == progress) {
		        setProgress(progress.getValue());
		    }
		}
		
		
//		@Override
//		public void adjustmentValueChanged(AdjustmentEvent ae) {
//			if (ae.getSource() != progress)
//				setWaitTime(100 * (15 - ae.getValue()));
//			else
//				setProgress(progress.getValue());
//		}

		/**
		 * Sets the pause state for the animation panel.
		 */
		public void setPause() {
			pauseresume.setText("Resume"); // setText instead of setLabel
			pauseresumehelp.backHelp();
			pauseresumehelp.show();
		}

		/**
		 * Handles action events for buttons.
		 *
		 * @param ae The action event.
		 */
		@Override
		public void actionPerformed(ActionEvent ae) {
			Object src = ae.getSource();
			if (runningThread == null)
				return;

			if (src == stop) {
				resetButtons();
				stop();
			}
			if (src == start) {
				start();
				start.setText("Restart"); // .setText instead of setLabel
				pauseresume.setEnabled(true);
				stop.setEnabled(true);
				
		        // Reset the slider to 5 when the Restart button is clicked
		        ranger.setValue(5); 
		        // Alternatively, use the current slider value to set the wait time
		        // int sliderValue = ranger.getValue();
		        // int delay = 1000 + (sliderValue - 5) * -200;
		        // setWaitTime(delay);
			}

			if (src == pauseresume) {
				if (pauseresume.getText().compareTo("Resume") == 0) { // getText() instead of getLabel()
					pauseresume.setText("Pause"); // .setText instead of setLabel
					pauseresumehelp.frontHelp();
					pauseresumehelp.show();
		            GeneralCanvas.this.resume(); // resume();
				} else {
					setPause();
		            GeneralCanvas.this.pause(); // pause();
				}
			}

		}
	}

	/* ------------- file load / save ------------ */

	/**
	 * Abstract method to get the applet type.
	 *
	 * @return The applet type.
	 */
	public abstract int getAppletType();

	/**
	 * Abstract method to get the count of structures.
	 *
	 * @return The count of structures.
	 */
	public abstract int getStructCount();

	/**
	 * Abstract method to get the name of a structure.
	 *
	 * @param i The index of the structure.
	 * @return The name of the structure.
	 */
	public abstract String getStructName(int i);

	/**
	 * Abstract method to get the current name.
	 *
	 * @return The current name.
	 */
	public abstract String getCurrentName();

	/**
	 * Clears the canvas.
	 */
	public void clear() {
		historyList = new Vector<>();
	}

	/**
	 * Abstract method to rebuild structures.
	 */
	public abstract void rebuild();

	/**
	 * Undoes the last action.
	 */
	public void undo() {
		// TODO - do more efficiently for structures independent on the insertion order
		if (historyList.size() > 0) {
			historyList.removeElementAt(historyList.size() - 1);
			rebuild();
		}
	}

	// Abstract method to get a file selector based on the specified type
	public abstract fileSelector getFileSelector(String type);

	// Method to test if a given point is within the wholeCanvas
	public boolean testCoordinates(DPoint c) {
		return wholeCanvas.contains(c);
	}

	// Method to generate a random DPoint within the wholeCanvas
	public DPoint randomDPoint() {
		return new DPoint(wholeCanvas.x + Math.random() * wholeCanvas.width,
				wholeCanvas.y + Math.random() * wholeCanvas.height);
	}

	/* ----- drawing utilities ----------- */

	// Method to draw the background on a specified DrawingTarget with a default
	// color
	@Override
	public void drawBackground(DrawingTarget g) {
		drawBackground(g, Color.white);
	}

	// Method to draw the background on a specified DrawingTarget with a specified
	// color
	@Override
	public void drawBackground(DrawingTarget g, Color c) {
		g.setColor(c);
		g.fillRect(wholeCanvas.x, wholeCanvas.y, wholeCanvas.width, wholeCanvas.height);
	}

	// Abstract method to draw the contents on a specified DrawingTarget
	@Override
	abstract public void drawContents(DrawingTarget g);

	// Method to draw a grid on a specified DrawingTarget
	@Override
	public void drawGrid(DrawingTarget g) {
		// Check if grid should be drawn
		if (gridLevel == 0 || !gridOn)
			return;
		g.setColor(Color.lightGray);
		double canvasWidth = Math.min(wholeCanvas.width, wholeCanvas.height);
		double add = canvasWidth / (int) Math.pow(2, gridLevel);

		// Draw horizontal and vertical lines to form the grid
		for (double line = add; line < canvasWidth; line += add) {
			g.drawLine(wholeCanvas.x, wholeCanvas.y + line, wholeCanvas.x + wholeCanvas.width, wholeCanvas.y + line);
			g.drawLine(wholeCanvas.x + line, wholeCanvas.y, wholeCanvas.x + line, wholeCanvas.y + wholeCanvas.height);
		}
	}

	// Method to redraw the whole canvas
	public void redraw() {

			drawBackground(offscrG);
			if (runningThread != null)
				runningThread.refill();
			drawGrid(offscrG);
			drawContents(offscrG);
			if (runningThread != null)
				runningThread.redraw();
			offscrG.redraw();

	
			redraw(overview);
	}

	// Method to redraw a specified DrawingTarget
	public void redraw(DrawingTarget overview) {
		drawBackground(overview);
		if (runningThread != null)
			runningThread.refill(overview);
		drawGrid(overview);
		drawContents(overview);
		if (runningThread != null)
			runningThread.redraw(overview);

		overview.redraw();
	}

	// Method to redraw the path by drawing orange lines between consecutive points
	protected void redrawPath() {
		offscrG.redraw();
		for (int i = 0; i < polyRange.size() - 1; i++)
			(new DLine((DPoint) (polyRange.elementAt(i)), (DPoint) (polyRange.elementAt(i + 1))))
					.directDraw(Color.orange, offscrG);
	}

	// Method to redraw the polygon by drawing orange lines between consecutive
	// points
	protected void redrawPolygon() {
		offscrG.redraw();
		for (int i = 0; i < polyRange.size(); i++)
			(new DLine((DPoint) (polyRange.elementAt(i)), (DPoint) (polyRange.elementAt((i + 1) % polyRange.size()))))
					.directDraw(Color.orange, offscrG);
	}

	/* --------------- operations on structures ------------------- */

	// Abstract method to set a tree based on a structure and operation choice
	public abstract void setTree(int str, JComboBox<String> opChoice);

	// Method to set the wait time
	void setWaitTime(int delayValue) {
		// Assuming 'value' is the position of your slider, ranges from 0 to 10
		// Modify this formula as needed to achieve the desired range of delays
		waitTime = delayValue; // Set the delay directly from the calculated value
	
		if (runningThread instanceof ConvertThread) {
			((ConvertThread) runningThread).setDelay(waitTime);
		}
	}

	// Method to set the wait time
	// void setWaitTime(int w) {
	// 	waitTime = w;
	// }

	// Method to set the progress step
	void setProgress(int step) {
		if (runningThread != null) {
			runningThread.setProgress(step);
		}
	}

	// Method to set the progress bar value
	@Override
	public void setProgressBar(int step) {
		animPanel.progress.setValue(step);
		animPanel.progress.validate();
	}

	// Method to initialize the progress bar with a given step
	@Override
	public void initProgress(int step) {
		animPanel.initProgress(step);
	}

	// Method to get the delay time
	@Override
	public int getDelay() {
		return waitTime;
	}

	// Method to get the success mode from the animation panel
	@Override
	public int getSuccessMode() {
		return animPanel.getSuccess();
	}

	// Method to increment the grid level
	public void incGrid() {
		if (gridLevel < 7)
			setGrid(gridLevel + 1);
	}

	// Method to decrement the grid level
	public void decGrid() {
		if (gridLevel > 0)
			setGrid(gridLevel - 1);
	}

	// Method to set the grid level
	public void setGrid(int i) {
		gridLevel = i;
		redraw();
	}

	// Method to set the grid state
	public void setGrid(boolean b) {
		gridOn = b;
		redraw();
	}

	// Synchronized method to start the animation thread
	synchronized void start() {
		if (runningThread != null) {
			Thread oldrun = runningThread;
			runningThread = runningThread.makeCopy();
			if (oldrun.isAlive())
				oldrun.stop();
			reset();
			runningThread.start();
		}
	}

	// Synchronized method to terminate the animation
	public synchronized void terminate() {
		// don't call from the thread itself or runningThreat won't be set to null
		stop();
		animPanel.initProgress(0);
		runningThread = null;
	}

	// Method to set the animation panel to pause state
	@Override
	public void setPause() {
		animPanel.setPause();
	}

	synchronized void pause() {
	    if (runningThread != null && runningThread instanceof ConvertThread) {
	        ((ConvertThread)runningThread).pauseThread();
	    }
	}

	synchronized void resume() {
	    if (runningThread != null && runningThread instanceof ConvertThread) {
	        ((ConvertThread)runningThread).resumeThread();
	    }
	}


	// Synchronized method to pause the animation thread
//	synchronized void pause() {
//		if (runningThread != null)
//			runningThread.suspend();
//	}
//
//	// Synchronized method to resume the animation thread
//	synchronized void resume() {
//		if (runningThread != null)
//			runningThread.resume();
//	}

	// Synchronized method to reset the animation panel buttons
	@Override
	public synchronized void reset() {
		animPanel.resetButtons();
	}

	// Synchronized method to stop the animation thread
	public synchronized void stop() {
		if (runningThread != null) {
			reset();
			if (runningThread.isAlive())
				runningThread.stop();
		}
	}

	// Method to adjust the search rectangle based on the current structure name
	protected QueryObject adjustSearchRectangle(QueryObject s) {
		if (!getCurrentName().equals("Priority Tree"))
			return s;

		DRectangle bb = s.getBB();
		return new QueryObject(new DRectangle(bb.x, bb.y, bb.width, wholeCanvas.y + wholeCanvas.height - bb.y));
	}

	// ---------------- MouseListener && MouseMotionListener -------------
	// Class representing an operation feature
	public class OpFeature {
		public String name;
		public int ID;
		public String helpText;
		public String h1, h2, h3;
		public int buttonMask;

		public OpFeature(String name, int ID, String helpText, String h1, String h2, String h3, int buttonMask) {
			this.name = name;
			this.helpText = helpText;
			this.ID = ID;
			this.h1 = h1;
			this.h2 = h2;
			this.h3 = h3;
			this.buttonMask = buttonMask;
		}

		public OpFeature getFeature() {
			return this;
		}

	}

	// Class extending OpFeature to include an array of features and an evaluation
	// function
	public class OpFeatures extends OpFeature {
		private OpFeature[] oa;
		private IDEval ie;

		public OpFeatures(String name, int ID, OpFeature[] oa, IDEval ie) {
			super(name, ID, "", "", "", "", 0);
			this.oa = oa;
			this.ie = ie;
		}

		@Override
		public OpFeature getFeature() {
			for (OpFeature element : oa)
				if (element.ID == ie.getValue()) {
					return element;
				}
			return null;
		}
	}

	// Array of operation features and associated constants
	protected OpFeature[] opFeature;

	protected int previousOpfeature = 0;
	protected final static int OPFEATURE_INSERT = 1;
	protected final static int OPFEATURE_DELETE = 2;
	protected final static int OPFEATURE_MOVE = 3;
	protected final static int OPFEATURE_MOVEVERTEX = 4;
	protected final static int OPFEATURE_MOVECOLLECTION = 5;
	protected final static int OPFEATURE_ROTATECOLLECTION = 6;
	protected final static int OPFEATURE_NEAREST = 7;
	protected final static int OPFEATURE_WINDOW = 8;
	protected final static int OPFEATURE_MOVEEDGE = 9;
	protected final static int OPFEATURE_BINTREES = 10;
	protected final static int OPFEATURE_WITHIN = 11;
	protected final static int OPFEATURE_NEAREST_SITE = 12;
	protected final static int OPFEATURE_LINE_INDEX = 13;
	protected final static int OPFEATURE_VERTEX_INDEX = 14;

	protected final static int OPFEATURE_UMOVE = 15;
	protected final static int OPFEATURE_COPY = 16;
	protected final static int OPFEATURE_SELECT = 17;
	protected final static int OPFEATURE_TO_QUADTREE = 18;
	protected final static int OPFEATURE_TO_ARRAY = 19;
	protected final static int OPFEATURE_TO_RASTER = 20;
	protected final static int OPFEATURE_TO_CHAIN = 21;
	protected final static int OPFEATURE_MOTIONSENSITIVITY = 22;
	protected final static int OPFEATURE_SHOWQUAD = 23;

	public static void debugPrint(String s) {
		// System.err.println(s);
	}

	// Handles mouse enter events
	@Override
	public void mouseEntered(MouseEvent me) {
	}

	// Handles mouse exit events
	@Override
	public void mouseExited(MouseEvent me) {
	}

	// Handles mouse click events
	@Override
	public void mouseClicked(MouseEvent me) {
	}

	// Handles mouse press events
	@Override
	public void mousePressed(MouseEvent me) {
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);

		lastP = scrCoord;

		terminate();
		redraw();

		int op = getCurrentOperation();

		if ((op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST || op == OPFEATURE_WITHIN)
				&& withinStats.getValue() == QueryObject.QO_POINT || op == OPFEATURE_NEAREST_SITE) {
			if (op == OPFEATURE_WINDOW) {
				lastWindow = new QueryObject(p);
				search(lastWindow, allDrawingTargets);
			} else {
				lastNear = new QueryObject(p);
				nearest(lastNear, withinStats.getDist(), allDrawingTargets);
			}
			redraw();
		}

		if ((op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST || op == OPFEATURE_WITHIN)
				&& withinStats.getValue() == QueryObject.QO_SECTOR) {
			if (me.isControlDown()) { // button 3
				sec.adjustExtent(p);
			} else if (me.isAltDown()) {
				sec.adjustStart(p);
			} else {
				sec.adjustVertex(p);
			}
			if (op == OPFEATURE_WINDOW) {
				lastWindow = new QueryObject(sec);
				search(lastWindow, allDrawingTargets);
			} else {
				lastNear = new QueryObject(sec);
				nearest(lastNear, withinStats.getDist(), allDrawingTargets);
			}
			redraw();
		}

		if ((op == OPFEATURE_WINDOW || op == OPFEATURE_WITHIN || op == OPFEATURE_NEAREST)
				&& (withinStats.getValue() == QueryObject.QO_POLYGON
						|| withinStats.getValue() == QueryObject.QO_PATH)) {
			polyRange.addElement(p);
			if (withinStats.getValue() == QueryObject.QO_PATH) {
				redrawPath();
				if (me.isMetaDown() || me.isAltDown()) {
					if (me.isAltDown() && polyRange.size() > 1) {
						polyRange.removeElement(polyRange.lastElement());
						polyRange.addElement(polyRange.elementAt(0));
						redrawPath();
					}
					DPoint[] ar = new DPoint[polyRange.size()];
					polyRange.copyInto(ar);
					if (ar.length >= 2) {
						if (op == OPFEATURE_WINDOW) {
							lastWindow = new QueryObject(new DPath(ar));
							search(lastWindow, allDrawingTargets);
						} else {
							lastNear = new QueryObject(new DPath(ar));
							nearest(lastNear, withinStats.getDist(), allDrawingTargets);
						}
					}
					polyRange.removeAllElements();
					redraw();
				}
			} else {
				if (me.isMetaDown() || DPolygon.non_self_intersecting(polyRange)) {
					redrawPolygon();
					if (me.isMetaDown()) {
						DPoint[] ar = new DPoint[polyRange.size()];
						polyRange.copyInto(ar);
						DPolygon dp = new DPolygon(ar);
						if (ar.length >= 3 && dp.non_self_intersecting()) {
							if (op == OPFEATURE_WINDOW) {
								lastWindow = new QueryObject(dp);
								search(lastWindow, allDrawingTargets);
							} else {
								lastNear = new QueryObject(dp);
								nearest(lastNear, withinStats.getDist(), allDrawingTargets);
							}
						}
						polyRange.removeAllElements();
						redraw();
					}
				} else {
					polyRange.removeElement(polyRange.lastElement());
					redrawPolygon();
				}
			}
		}

		// mouseDragged(me);
	}

	// Handles mouse move events
	@Override
	public void mouseMoved(MouseEvent me) {
		int op = getCurrentOperation();
		DPoint p = offscrG.transPointT(offscrG.adjustPoint(me.getPoint()));

		showPath(op, p);
	}

	private void showPath(int op, DPoint p) {
		if ((op == OPFEATURE_WINDOW || op == OPFEATURE_WITHIN || op == OPFEATURE_NEAREST)
				&& (withinStats.getValue() == QueryObject.QO_POLYGON
						|| withinStats.getValue() == QueryObject.QO_PATH)) {
			if (polyRange.size() > 0) {
				polyRange.addElement(p);
				if (withinStats.getValue() == QueryObject.QO_PATH)
					redrawPath();
				else
					redrawPolygon();
				polyRange.removeElement(polyRange.lastElement());
			}
		}
	}

	// Handles mouse drag events
	@Override
	public void mouseDragged(MouseEvent me) {
		// System.out.println("IN");

		// event out of order, first you have to press before you can drag
		if (lastP == null)
			return;

		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);
		DPoint last = offscrG.transPointT(lastP);
		int op = getCurrentOperation();

		showPath(op, p); // show path when both dragging and moving

		if ((op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST || op == OPFEATURE_WITHIN)
				&& withinStats.getValue() == QueryObject.QO_RECTANGLE) {
			DRectangle drgRect = new DRectangle(Math.min(p.x, last.x), Math.min(p.y, last.y), Math.abs(p.x - last.x),
					Math.abs(p.y - last.y));
			offscrG.redraw();
			if (op == OPFEATURE_WINDOW) {
				QueryObject drb = adjustSearchRectangle(new QueryObject(drgRect));
				drb.directDraw(Color.orange, offscrG);
			} else
				drgRect.directDraw(Color.orange, offscrG);
		}
		// System.out.println("OUT");
	}

	// Handles mouse release events
	@Override
	public void mouseReleased(MouseEvent me) {
		Point scrCoord = offscrG.adjustPoint(me.getPoint());
		DPoint p = offscrG.transPointT(scrCoord);
		// if (tree.runningThread != null)
		// return true;
		int op = getCurrentOperation();

		if ((op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST || op == OPFEATURE_WITHIN)
				&& withinStats.getValue() == QueryObject.QO_RECTANGLE) {

			if (lastP.x == scrCoord.x || lastP.y == scrCoord.y) {
				redraw();
				return;
			}

			DPoint last = offscrG.transPointT(lastP);
			DRectangle r = new DRectangle(Math.min(p.x, last.x), Math.min(p.y, last.y), Math.abs(p.x - last.x),
					Math.abs(p.y - last.y));

			if (op == OPFEATURE_WINDOW) {
				lastWindow = new QueryObject(r);
				lastWindow = adjustSearchRectangle(lastWindow);
				search(lastWindow, allDrawingTargets);
			} else {
				lastNear = new QueryObject(r);
				nearest(lastNear, withinStats.getDist(), allDrawingTargets);
			}
			redraw();
		}
	}
	
	public String comboBoxItemsToString(JComboBox<String> comboBox) {
	    StringBuilder items = new StringBuilder();
	    for (int i = 0; i < comboBox.getItemCount(); i++) {
	        items.append(comboBox.getItemAt(i));
	        if (i < comboBox.getItemCount() - 1) {
	            items.append(", ");
	        }
	    }
	    return items.toString();
	}

}
