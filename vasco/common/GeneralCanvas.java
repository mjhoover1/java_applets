/* $Id: GeneralCanvas.java,v 1.8 2007/10/28 15:38:13 jagan Exp $ */
package vasco.common;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

// ------------- P Canvas -------------------


public abstract class GeneralCanvas implements CanvasIface, CommonConstants, 
					       MouseListener, MouseMotionListener {

    protected DrawingTarget offscrG;
    protected DrawingTarget overview;
    protected DrawingTarget[] allDrawingTargets;

    protected DRectangle wholeCanvas;   
    // range of the raw data that the structures should support
    Image offscr;
    protected Point lastP;   // in screen coordinates (ie. int is fine)
    int waitTime;
    protected int successMode;
    protected int searchMode;
    protected WithinStats withinStats;
    protected int gridLevel;
    AnimationPanel animPanel;
    protected QueryObject lastNear;
    protected QueryObject lastWindow;
    protected Vector polyRange;
    protected boolean gridOn;
    protected DSector sec;
    public Vector historyList=new Vector();


    protected TopInterface topInterface;
    protected VascoThread runningThread;

    public  class WithinStats implements IDEval {
	int mode;
	double dist;
	boolean blend;

	public WithinStats() {
	}

	public void setValues(int mode, double dist, boolean blend) {
	    this.mode = mode;
	    this.dist = dist;
	    this.blend = blend;
	}

	public boolean getBlend() {
	    return blend;
	}

	public double getDist() {
	    return dist;
	}

	public int getValue() {
	    return mode;
	}
    }

    public final static int MOUSE_ENTERED = 0;
    public final static int MOUSE_EXITED = 1;
    public final static int MOUSE_PRESSED = 2;
    public final static int MOUSE_MOVED = 3;
    public final static int MOUSE_DRAGGED = 4;
    public final static int MOUSE_RELEASED = 5;
    public final static int MOUSE_CLICKED = 6;
    
    public GeneralCanvas(DRectangle can, DrawingTarget dt, DrawingTarget overview,
			 Panel m, TopInterface ti) {
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
	sec = new DSector(new DPoint(wholeCanvas.x / 2, wholeCanvas.y / 2),
			  50, 50);
    }

    abstract protected String getCurrentOperationName();

    protected OpFeature getCurrentOpFeature() {
	int i;
	int op = getCurrentOperation();
	for (i = 0; i< opFeature.length; i++)
	    if (opFeature[i].ID == op)
		break;
	return opFeature[i].getFeature();
    }

    protected int getCurrentOperation() {
	String op = getCurrentOperationName();
	for (int i = 0; i < opFeature.length; i++)
	    if (opFeature[i].name.equals(op))
		return opFeature[i].ID;
	return -1;
    }

    abstract protected int getSearchModeMask();
    abstract protected int getAllowedOverlapQueryObjects();

    abstract protected void nearest(QueryObject p, double dist, DrawingTarget[] off);
    abstract protected void search(QueryObject s, DrawingTarget[] off);

    protected void setHelp() {
	OpFeature of = getCurrentOpFeature();
	topInterface.getHelpArea().setText(Tools.formatHelp(of.helpText,
							    topInterface.getHelpArea().getColumns()));
	offscrG.changeHelp(of.buttonMask, of.h1, of.h2, of.h3);
    }


    //  abstract protected void setHelp();

    public void itemStateChanged(ItemEvent ie) {
	// operation selection has changed
	polyRange = new Vector();

	if (runningThread != null)
	    terminate();

	if (getCurrentOperation() == OPFEATURE_WINDOW) {
	    animPanel.setOverlap();
	    SearchMode sm = new SearchMode(getSearchModeMask());
	    WithinMode wm = new WithinMode(sm, getAllowedOverlapQueryObjects());
	    searchMode = sm.getSearchMode();
	    withinStats.setValues(wm.getWithinMode(), wm.getWithinDist(), wm.getBlend());
	    if (lastWindow != null) {
		search(lastWindow, allDrawingTargets);
	    }
	}

	if (getCurrentOperation() == OPFEATURE_NEAREST || 
	    getCurrentOperation() == OPFEATURE_WITHIN) {
	    animPanel.setNearest();
	    WithinMode wm = new WithinMode(getCurrentOperation() == OPFEATURE_WITHIN);
	    withinStats.setValues(wm.getWithinMode(), wm.getWithinDist(), wm.getBlend());
	    if (lastNear != null) 
		nearest(lastNear, withinStats.getDist(), allDrawingTargets);
	}
	
	setHelp();
	redraw();
    }	

    class AnimationPanel implements ActionListener, AdjustmentListener {
	final String RUNMODE_CONTINUOUS_S = "continuous";
	final String RUNMODE_OBJECT_S = "stop on object";
	final String RUNMODE_SUCCESS_S = "stop on success";

	Button start, stop, pauseresume;
	Scrollbar progress;
	MouseHelp starthelp, pauseresumehelp, stophelp;
	Choice runmode;

	AnimationPanel(Panel r) {
	    Scrollbar ranger;
	    r.setLayout(new GridLayout(4, 1));

	    Panel anim = new Panel();
	    anim.setLayout(new GridLayout(1, 2));
	    anim.add(new Label("Speed"));
	    ranger = new Scrollbar(Scrollbar.HORIZONTAL, 5, 1, 0, 10);
	    setWaitTime(100*(15 - ranger.getValue()));
	    anim.add(ranger);
	    r.add(anim);

	    new MouseHelp(ranger, topInterface.getMouseDisplay(), "Drag slider to change animation speed",
			  "", "");

	    Panel progressPanel = new Panel();
	    progressPanel.setLayout(new GridLayout(1,2));
	    progressPanel.add(new Label("Progress"));
	    progressPanel.add(progress = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 0));
	    r.add(progressPanel);

	    new MouseHelp(progress, topInterface.getMouseDisplay(), "Drag to view animation progress","", "");

	    Panel buts = new Panel();
	    buts.setLayout(new GridLayout(1,3));
	    buts.add(start = new Button("Start"));
	    buts.add(pauseresume = new Button("Pause"));
	    buts.add(stop = new Button("Stop"));
	    starthelp = new MouseHelp(start, topInterface.getMouseDisplay(), 
				      "Start animation","", "");
	    pauseresumehelp = new MouseHelp(pauseresume, topInterface.getMouseDisplay(), "Pause animation","", "",
					    "Resume animation", "", "");
	    stophelp = new MouseHelp(stop, topInterface.getMouseDisplay(), 
				     "Stop animation", "", "");
	    r.add(buts);

	    runmode = new Choice();
	    runmode.add(RUNMODE_CONTINUOUS_S);
	    runmode.add(RUNMODE_OBJECT_S);
	    Panel runmodeP = new Panel();
	    runmodeP.setLayout(new BorderLayout());
	    runmodeP.add("West", new Label("Run Mode:"));
	    runmodeP.add("Center", runmode);
	    r.add(runmodeP);

	    resetButtons();
  
	    ranger.addAdjustmentListener(this);
	    progress.addAdjustmentListener(this);
	    start.addActionListener(this);
	    pauseresume.addActionListener(this);
	    stop.addActionListener(this);
	}

	void setOverlap() {
	    if (runmode.getItemCount() == 2)
		runmode.add(RUNMODE_SUCCESS_S);
	}

	void setNearest() {
	    if (runmode.getItemCount() == 3)
		runmode.remove(RUNMODE_SUCCESS_S);
	}

	void initProgress(int nrSteps) {
	    progress.setMaximum(nrSteps);
	}

	void resetButtons() {
	    start.setLabel("Start");
	    pauseresume.setLabel("Pause");
	    pauseresumehelp.frontHelp();
	    pauseresume.setEnabled(false);
	    stop.setEnabled(false);
	}
 

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

	public void adjustmentValueChanged(AdjustmentEvent ae) {
	    if (ae.getSource() != progress)
		setWaitTime(100 * (15 - ae.getValue()));
	    else
		setProgress(progress.getValue());
	}
    
	public void setPause() {
	    pauseresume.setLabel("Resume");
	    pauseresumehelp.backHelp();
	    pauseresumehelp.show();
	}

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
		start.setLabel("Restart");
		pauseresume.setEnabled(true);
		stop.setEnabled(true);
	    }

	    if (src == pauseresume) {
		if (pauseresume.getLabel().compareTo("Resume") == 0) {
		    pauseresume.setLabel("Pause");
		    pauseresumehelp.frontHelp();
		    pauseresumehelp.show();
		    resume();
		} else {
		    setPause();
		    pause();
		}
	    }

	}
    }

    /* ------------- file load / save ------------  */

    public abstract int getAppletType();
    public abstract int getStructCount();
    public abstract String getStructName(int i);
    public abstract String getCurrentName();
    public void clear() {
	historyList = new Vector();
    };
    public abstract void rebuild();

    public void undo() {
	// TODO - do more efficiently for structures independent on the insertion order
	if (historyList.size() > 0) {
	    historyList.removeElementAt(historyList.size() - 1);
	    rebuild();
	}
    }

    public abstract fileSelector getFileSelector(String type);

    public boolean testCoordinates(DPoint c) {
	return wholeCanvas.contains(c);
    }

    public DPoint randomDPoint() {
	return new DPoint(wholeCanvas.x + Math.random() * wholeCanvas.width,
			  wholeCanvas.y + Math.random() * wholeCanvas.height);
    }

    /* ----- drawing utilities ----------- */

    public void drawBackground(DrawingTarget g) {
	drawBackground(g, Color.white);
    }

    public void drawBackground(DrawingTarget g, Color c) {
	g.setColor(c);
	g.fillRect(wholeCanvas.x, wholeCanvas.y, wholeCanvas.width, wholeCanvas.height);
    }

    abstract public void drawContents(DrawingTarget g);

    public void drawGrid(DrawingTarget g) {
	if (gridLevel == 0 || !gridOn)
	    return;
	g.setColor(Color.lightGray);
	double canvasWidth = Math.min(wholeCanvas.width, wholeCanvas.height);
	double add = canvasWidth / (int)Math.pow(2, gridLevel);
	for (double line = add; line < canvasWidth; line += add) {
	    g.drawLine(wholeCanvas.x, wholeCanvas.y + line, 
		       wholeCanvas.x + wholeCanvas.width, wholeCanvas.y + line);
	    g.drawLine(wholeCanvas.x + line, wholeCanvas.y, 
		       wholeCanvas.x + line, wholeCanvas.y + wholeCanvas.height);
	}
    }

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

    protected void redrawPath() {
	offscrG.redraw();
	for (int i = 0; i < polyRange.size() - 1; i++)
	    (new DLine((DPoint)(polyRange.elementAt(i)), 
		       (DPoint)(polyRange.elementAt(i+1)))).directDraw(Color.orange, offscrG);
    }

    protected void redrawPolygon() {
	offscrG.redraw();
	for (int i = 0; i < polyRange.size(); i++)
	    (new DLine((DPoint)(polyRange.elementAt(i)), 
		       (DPoint)(polyRange.elementAt((i+1) % polyRange.size())))).directDraw(Color.orange, offscrG);
    }

    /* --------------- operations on structures ------------------- */

    public abstract void setTree(int str, Choice opChoice);

    void setWaitTime(int w) {
	waitTime = w;
    }

    void setProgress(int step) {
	if (runningThread != null) {
	    runningThread.setProgress(step);
	}
    }

    public void setProgressBar(int step) {
	animPanel.progress.setValue(step);
	animPanel.progress.validate();
    }

    public void initProgress(int step) {
	animPanel.initProgress(step);
    }

    public int getDelay() {
	return waitTime;
    }

    public int getSuccessMode() {
	return animPanel.getSuccess();
    }
    public void incGrid(){
      if (gridLevel < 7) 
	setGrid(gridLevel + 1);
    }
 
    public void decGrid(){
      if (gridLevel > 0)
	setGrid(gridLevel - 1);
    } 


    public void setGrid(int i) {
	gridLevel = i;
	redraw();
    }

    public void setGrid(boolean b) {
	gridOn = b;
	redraw();
    }

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

    public synchronized void terminate() {
	// don't call from the thread itself or runningThreat won't be set to null
	stop();
	animPanel.initProgress(0);
	runningThread = null;
    }

    public void setPause() {
	animPanel.setPause();
    }

    synchronized void pause() {
	if (runningThread != null)
	    runningThread.suspend();
    }

    synchronized void resume() {
	if (runningThread != null)
	    runningThread.resume();
    }

    public synchronized void reset() {
	animPanel.resetButtons();
    }

    public synchronized void stop() {
	if (runningThread != null) {
	    reset();   
	    if (runningThread.isAlive())
		runningThread.stop();
	}
    }

    protected QueryObject adjustSearchRectangle(QueryObject s) {
	if (!getCurrentName().equals("Priority Tree"))
	    return s;

	DRectangle bb = s.getBB();
	return new QueryObject(new DRectangle(bb.x, bb.y, bb.width, 
					      wholeCanvas.y + wholeCanvas.height - bb.y));
    }

    // ---------------- MouseListener && MouseMotionListener -------------

    public class OpFeature {
	public String name;
	public int ID;
	public String helpText;
	public String h1, h2, h3;
	public int buttonMask;
	
	public OpFeature(String name, int ID, String helpText, 
			 String h1, String h2, String h3, int buttonMask) {
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

    public class OpFeatures extends OpFeature {
	private OpFeature[] oa;
	private IDEval ie;

	public OpFeatures(String name, int ID, OpFeature[] oa, IDEval ie) {
	    super(name, ID, "", "", "", "", 0);
	    this.oa = oa;
	    this.ie = ie;
	}

	public OpFeature getFeature() {
	    for (int i = 0; i < oa.length; i++)
		if (oa[i].ID == ie.getValue()) {
		    return oa[i];
		}
	    return null;
	}
    }

    protected OpFeature[] opFeature;

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
	//	System.err.println(s);
    }


    public void mouseEntered(MouseEvent me) {};

    public void mouseExited(MouseEvent me) {};



    public void mouseClicked(MouseEvent me) {}

    public void mousePressed(MouseEvent me) {
	Point scrCoord = offscrG.adjustPoint(me.getPoint());
	DPoint p  = offscrG.transPointT(scrCoord);

	lastP = scrCoord;

	terminate();
	redraw();

	int op = getCurrentOperation();

	if ((op == OPFEATURE_WINDOW ||
	     op == OPFEATURE_NEAREST ||
	     op == OPFEATURE_WITHIN) && 
	    withinStats.getValue() == QueryObject.QO_POINT || op == OPFEATURE_NEAREST_SITE) {
	    if (op == OPFEATURE_WINDOW) {
		lastWindow = new QueryObject(p);
		search(lastWindow, allDrawingTargets);
	    } else {
		lastNear = new QueryObject(p);
		nearest(lastNear, withinStats.getDist(), allDrawingTargets);
	    }
	    redraw();
	}

	if ((op == OPFEATURE_WINDOW || op == OPFEATURE_NEAREST ||
	     op == OPFEATURE_WITHIN) &&
	    withinStats.getValue() == QueryObject.QO_SECTOR) {
	    if (me.isMetaDown()) { // button 3
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

	if ((op == OPFEATURE_WINDOW || 
	     op == OPFEATURE_WITHIN ||
	     op == OPFEATURE_NEAREST) && 
	    (withinStats.getValue() == QueryObject.QO_POLYGON || 
	     withinStats.getValue() == QueryObject.QO_PATH)) {
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

	//    mouseDragged(me);
    }

    public void mouseMoved(MouseEvent me) {
	int op = getCurrentOperation();
	DPoint p  = offscrG.transPointT(offscrG.adjustPoint(me.getPoint()));

	showPath(op, p);
    }


    private void showPath(int op, DPoint p) {
	if ((op == OPFEATURE_WINDOW || 
	     op == OPFEATURE_WITHIN || 
	     op == OPFEATURE_NEAREST) && 
	    (withinStats.getValue() == QueryObject.QO_POLYGON || 
	     withinStats.getValue() == QueryObject.QO_PATH)) {
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

    public void mouseDragged(MouseEvent me) {
	//System.out.println("IN");

	// event out of order, first you have to press before you can drag
	if (lastP == null) return; 

	Point scrCoord = offscrG.adjustPoint(me.getPoint());
	DPoint p  = offscrG.transPointT(scrCoord);
	DPoint last = offscrG.transPointT(lastP);
	int op = getCurrentOperation();

	showPath(op, p); // show path when both dragging and moving
	
	if ((op == OPFEATURE_WINDOW || 
	     op == OPFEATURE_NEAREST || 
	     op == OPFEATURE_WITHIN) && 
	    withinStats.getValue() == QueryObject.QO_RECTANGLE) {
	    DRectangle drgRect = new DRectangle(Math.min(p.x, last.x), 
						Math.min(p.y, last.y), 
						Math.abs(p.x - last.x), 
						Math.abs(p.y - last.y));
	    offscrG.redraw();
	    if (op == OPFEATURE_WINDOW) {
		QueryObject drb = adjustSearchRectangle(new QueryObject(drgRect));	
		drb.directDraw(Color.orange, offscrG);
	    } else 
		drgRect.directDraw(Color.orange, offscrG);
	}
	//    System.out.println("OUT");
    }

    public void mouseReleased(MouseEvent me) {
	Point scrCoord = offscrG.adjustPoint(me.getPoint());
	DPoint p  = offscrG.transPointT(scrCoord);
	//    if (tree.runningThread != null)
	//      return true;
	int op = getCurrentOperation();


	if ((op == OPFEATURE_WINDOW || 
	     op == OPFEATURE_NEAREST ||
	     op == OPFEATURE_WITHIN) && 
	    withinStats.getValue() == QueryObject.QO_RECTANGLE) {

	    if (lastP.x == scrCoord.x || lastP.y == scrCoord.y) {
		redraw();
		return;
	    }

	    DPoint last = offscrG.transPointT(lastP);
	    DRectangle r = new DRectangle(Math.min(p.x, last.x), Math.min(p.y, last.y), 
					  Math.abs(p.x - last.x), Math.abs(p.y - last.y));

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


}


