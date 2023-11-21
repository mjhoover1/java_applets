/* 
 * The TopInterface class represents the top-level interface components in a Java application.
 * $Id: TopInterface.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $
 */
package vasco.common;

import javax.swing.*; // import java.awt.*;

public class TopInterface {

    // Components in the top interface
    private Panel specPanel;
    private MouseDisplay mouseDisplay;
    private TextArea helpArea;
    private Component cursor;

    /**
     * Constructs a TopInterface object with the specified components.
     * 
     * @param specPanel    The panel for special interface elements
     * @param mouseDisplay The mouse display component
     * @param helpArea     The text area for displaying help information
     * @param cursor       The cursor component
     */
    public TopInterface(Panel specPanel, MouseDisplay mouseDisplay, TextArea helpArea, Component cursor) {
        this.specPanel = specPanel;
        this.mouseDisplay = mouseDisplay;
        this.helpArea = helpArea;
        this.cursor = cursor;
    }

    /**
     * Gets the special panel in the top interface.
     * 
     * @return The special panel
     */
    public Panel getPanel() {
        return specPanel;
    }

    /**
     * Gets the mouse display component in the top interface.
     * 
     * @return The mouse display component
     */
    public MouseDisplay getMouseDisplay() {
        return mouseDisplay;
    }

    /**
     * Gets the help area in the top interface.
     * 
     * @return The help area
     */
    public TextArea getHelpArea() {
        return helpArea;
    }

    /**
     * Sets the cursor for the top interface.
     * 
     * @param c The cursor to set
     */
    public void setCursor(Cursor c) {
        cursor.setCursor(c);
    }
}
