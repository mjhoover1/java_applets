/*
 * The CommonApplet class serves as a base class for Java applets within the vasco.common package.
 * $Id: CommonApplet.java,v 1.3 2007/10/28 15:38:13 jagan Exp $
 */
package vasco.common;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
// import javax.swing.event.*; // import java.awt.event.*;
// import java.applet.*;
import java.awt.event.ActionListener;

// import java.awt.*;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class CommonApplet extends JApplet implements AppletValidate, ActionListener {

	// Method for global validation (commented out)
	@Override
	public void globalValidate() {
		validate();
	}

	// Constants for the applet size and help width
	protected final int SIZE = 512;
	protected final int helpWidth = 20;

	// Components used in the applet
	protected JPanel buttonpanel;
	protected CentralMenu centralMenu;
	protected JLabel topBar;
	protected TopInterface topInterface;
	protected MouseDisplay mp;
	protected JPanel indStructP;
	protected JTextArea helpArea;
	protected JButton overviewButton;
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
	@Override
	public void init() {
		Tools.currentApplet = this;
		String imageFileName = "mousehelp.gif";
		// InputStream jpgStream =
		// CommonApplet.class.getResourceAsStream(imageFileName);

		// Toolkit tk = Toolkit.getDefaultToolkit();
		ImageIcon mouseImIcon = null; // Image mouseIm = null;

		try {
			mouseImIcon = new ImageIcon(CommonApplet.class.getResource(imageFileName));

			// byte imageBytes[] = new byte[jpgStream.available()];
			// jpgStream.read(imageBytes);
			// mouseIm = tk.createImage(imageBytes);
		} catch (Exception e) {
			System.err.println("Error loading image <" + imageFileName + "> " + e.toString());
		}

		Image mouseIm = mouseImIcon.getImage();
		mp = new MouseDisplay(getSize().width, mouseIm);

		try {
			treeType = getParameter("treetype");
		} catch (NullPointerException e) {
			treeType = "PM2Quadtree";
		}

		indStructP = new JPanel();
		// indStructP.addComponentListener(new PanelListener(indStructP, this)); //
		// indStructP.addContainerListener(new PanelListener(indStructP, this));
		animp = new JPanel();
		helpArea = new JTextArea(5, helpWidth);

		overviewButton = new JButton("Zoom window");
		overviewButton.addActionListener(this);

		topInterface = new TopInterface(indStructP, mp, helpArea, this);

		can = new DrawingCanvas(new Rectangle(0, 0, SIZE, SIZE), new Rectangle(0, 0, SIZE, SIZE),
				createImage(SIZE, SIZE), mp);

		topBar = new JLabel();
		topBar.setForeground(Color.red);

		overviewCanvas = new DrawingCanvas(new Rectangle(0, 0, SIZE, SIZE),
				new Rectangle(0, 0, OverviewWindow.OVERVIEW_SIZE, OverviewWindow.OVERVIEW_SIZE),
				createImage(OverviewWindow.OVERVIEW_SIZE, OverviewWindow.OVERVIEW_SIZE), mp);
	}

	// ActionListener method for handling button clicks
	@Override
	public void actionPerformed(ActionEvent ae) {
		Object c = ae.getSource();
		if (c == overviewButton) {
			overviewDialog.setVisible(true); // Replace .show() with .setVisible(true) overviewDialog.show();
		}
	}

	// Start method for the applet
	@Override
	public void start() {
		super.start();
		validate();
	}

	// Stop method for the applet
	@Override
	public void stop() {
		// centralmenu.dispose();
		if (centralMenu != null) {
			centralMenu.dispose();
		}
		super.stop();
	}

	// Destroy method for the applet
	@Override
	public void destroy() {
		stop();
		super.destroy();
	}
}
