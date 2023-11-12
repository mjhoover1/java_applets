package vasco.rectangles;

/* 
 * The main class for the rectangles package, representing a Java applet.
 * $Id: main.java,v 1.2 2007/10/28 15:38:20 jagan Exp $
 */
import vasco.common.*;

import java.awt.*;

public class main extends CommonApplet implements GenericMain {

    // The canvas used for drawing rectangles
    GenericCanvas drawcanvas;

    /**
     * Initializes the applet.
     */
    public void init() {
        // Call the init method with the drawcanvas instance
        init(this.drawcanvas);
    }

    /**
     * Initializes the applet with the specified GenericCanvas.
     * 
     * @param drawcanvas The canvas used for drawing rectangles
     */
    public void init(GenericCanvas drawcanvas) {
        super.init();

        // Create a RectangleCanvas instance with initial parameters
        drawcanvas = new RectangleCanvas(new DRectangle(0, 0, SIZE, SIZE), can, overviewCanvas, animp, topInterface);

        // Create a DrawingPanel with necessary components
        DrawingPanel dpanel = new DrawingPanel(can, drawcanvas, drawcanvas, drawcanvas, mp);

        // Create an OverviewWindow instance
        overviewDialog = new OverviewWindow(overviewCanvas, drawcanvas, mp);

        // Initialize structures in the drawcanvas
        drawcanvas.initStructs();

        // Set layout to GridBagLayout
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        // Set constraints for the DrawingPanel
        gbl.setConstraints(dpanel, createConstraints(0, GridBagConstraints.RELATIVE, 1, 1, GridBagConstraints.NONE));

        // Set constraints for the topBar component
        gbl.setConstraints(topBar, createConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL));

        // Add components to the applet
        add(topBar);
        add(dpanel);

        // Set constraints for the mp component
        gbl.setConstraints(mp, createConstraints(0, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,
                GridBagConstraints.REMAINDER, GridBagConstraints.HORIZONTAL));

        // Create a button panel
        buttonpanel = new Panel();

        // Set constraints for the button panel
        GridBagConstraints bp = new GridBagConstraints();
        bp.gridx = GridBagConstraints.RELATIVE;
        bp.gridy = 0;
        bp.gridwidth = GridBagConstraints.REMAINDER;
        bp.gridheight = 2;
        bp.fill = GridBagConstraints.NONE;
        bp.anchor = GridBagConstraints.NORTHWEST;
        gbl.setConstraints(buttonpanel, bp);

        // Set layout for the button panel
        GridBagLayout bplayout = new GridBagLayout();
        buttonpanel.setLayout(bplayout);

        GridBagConstraints butpan = new GridBagConstraints();
        butpan.gridx = 0;
        butpan.gridy = GridBagConstraints.RELATIVE;
        butpan.gridwidth = bp.gridheight = GridBagConstraints.REMAINDER;
        butpan.fill = GridBagConstraints.NONE;
        butpan.anchor = GridBagConstraints.NORTHWEST;

        // Disabled for SIGMOD 2010 -> Switches between point, line, and rectangle applets
        buttonpanel.add(new appletSwitch(appletSwitch.RECTANGLES, this, topInterface));

        // Create a CentralMenu instance and add it to the button panel
        centralmenu = new CentralMenu(drawcanvas, treeType, indStructP, this, helpArea, topBar, overviewButton, mp);
        buttonpanel.add(centralmenu);
        bplayout.setConstraints(centralmenu, butpan);
        
        // Add animp to the button panel
        buttonpanel.add(animp);

        // Add compile date label to the button panel
        Label date = new Label(CompileDate.compileDate);
        bplayout.setConstraints(date, butpan);
        buttonpanel.add(date);

        // Add components to the applet
        add(buttonpanel);
        add(mp);

        // Validate the layout
        validate();
    }
}
