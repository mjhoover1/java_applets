/* $Id: TopInterface.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;

public class TopInterface {
    private Panel specPanel;
    private MouseDisplay mouseDisplay;
    private TextArea helpArea;
    private Component cursor;


    public TopInterface(Panel specPanel, MouseDisplay mouseDisplay, TextArea helpArea, Component cursor) {
	this.specPanel = specPanel;
	this.mouseDisplay = mouseDisplay;
	this.helpArea = helpArea;
	this.cursor = cursor;
    }

    public Panel getPanel() {
	return specPanel;
    }

    public MouseDisplay getMouseDisplay() {
	return mouseDisplay;
    }

    public TextArea getHelpArea() {
	return helpArea;
    }

    public void setCursor(Cursor c) {
	cursor.setCursor(c);
    }

}
