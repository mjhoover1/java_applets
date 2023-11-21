/* 
 * The CommonApplet class serves as a base class for Java applets within the vasco.common package.
 * $Id: CommonApplet.java,v 1.3 2007/10/28 15:38:13 jagan Exp $
 */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
// import java.applet.*;
import java.io.*;

public class CommonApplet extends JApplet implements AppletValidate, ActionListener {

    // Method for global validation (commented out)
    public void globalValidate() {
         validate();
    }

    // Constants for the applet size and help width
    protected final int SIZE = 512; 
    protected final int helpWidth = 20;

    // Components used in the applet
    protected JPanel buttonpanel;
    protected CentralMenu centralmenu;
    protected Label topBar;
    protected TopInterface topInterface;
    protected MouseDisplay mp;
    protected JPanel indStructP;
    protected TextArea helpArea;
    protected Button overviewButton;
    protected JDialog overviewDialog;

    // Tree type, panels, and canvases
    protected String treeType;
    protected JPanel animp;
    protected DrawingCanvas can;
    protected DrawingCanvas overviewCanvas;

    // Method to create GridBagConstraints with specified parameters
    protected GridBagConstraints createConstraints(int gx, int gy, int gw, int gh, int fill) {
        GridBagConstraints dp = new GridBagConstraints();
        dp.gridx = gx;
        dp.gridy = gy;
        dp.gridwidth = gw;
        dp.gridheight = gh;
        dp.fill = fill;
        dp.anchor = GridBagConstraints.CENTER;
        return dp;
    }

    // Initialization method for the applet
    public void init() {
        Tools.currentApplet = this;
        String imageFileName = "mousehelp.gif";
        InputStream jpgStream = CommonApplet.class.getResourceAsStream(imageFileName);

        Toolkit tk = Toolkit.getDefaultToolkit();
        Image mouseIm = null;
        try {
            byte imageBytes[] = new byte[jpgStream.available()];
            jpgStream.read(imageBytes);
            mouseIm = tk.createImage(imageBytes);
        } catch (Exception e) {
            System.err.println("Error loading image <" + imageFileName + "> " + e.toString());
        }

        mp = new MouseDisplay(getSize().width, mouseIm);

        try {
            treeType = getParameter("treetype");
        } catch (NullPointerException e) {
            treeType = "PM2Quadtree";
        }

        indStructP = new JPanel();
        indStructP.addContainerListener(new PanelListener(indStructP, this));
        animp = new JPanel();
        helpArea = new TextArea(5, helpWidth);

        overviewButton = new Button("Zoom window");
        overviewButton.addActionListener(this);

        topInterface = new TopInterface(indStructP, mp, helpArea, this);

        can = new DrawingCanvas(new Rectangle(0, 0, SIZE, SIZE), new Rectangle(0, 0, SIZE, SIZE),
                createImage(SIZE, SIZE), mp);

        topBar = new Label();
        topBar.setForeground(Color.red);

        overviewCanvas = new DrawingCanvas(new Rectangle(0, 0, SIZE, SIZE),
                new Rectangle(0, 0, OverviewWindow.OVERVIEW_SIZE, OverviewWindow.OVERVIEW_SIZE),
                createImage(OverviewWindow.OVERVIEW_SIZE, OverviewWindow.OVERVIEW_SIZE), mp);
    }

    // ActionListener method for handling button clicks
    public void actionPerformed(ActionEvent ae) {
        Object c = ae.getSource();
        if (c == overviewButton) {
            overviewDialog.show();
        }
    }

    // Start method for the applet
    public void start() {
        super.start();
        validate();
    }

    // Stop method for the applet
    public void stop() {
        centralmenu.dispose();
        super.stop();
    }

    // Destroy method for the applet
    public void destroy() {
        stop();
        super.destroy();
    }
}
