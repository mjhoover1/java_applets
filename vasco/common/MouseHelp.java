/* $Id: MouseHelp.java,v 1.2 2002/09/25 20:55:04 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;

import org.w3c.dom.events.MouseEvent;

/**
 * Class providing mouse-over help functionality for components.
 * When the mouse hovers over a component, help messages are displayed.
 * This class supports different messages for different states of a component, 
 * e.g., a checkbox in checked or unchecked state.
 */
public class MouseHelp extends MouseAdapter implements ItemListener {
    String b1, b2, b3; // Strings to hold help messages
    MouseDisplay md;   // MouseDisplay object to show help messages
    Component c;       // Component to which this mouse help is attached
    String[] front, back, current; // Arrays to hold front and back messages
    int buttonMask;    // Mouse button mask for identifying the button press

    /**
     * Default constructor initializes the front and back message arrays.
     */
	public MouseHelp() {
		front = new String[3];
		back = new String[3];
	}

    /**
     * Constructor for MouseHelp.
     * Attaches mouse help to a component with specified help messages.
     *
     * @param c  The component to which this mouse help is attached.
     * @param md MouseDisplay object to show help messages.
     * @param b1 First help message.
     * @param b2 Second help message.
     * @param b3 Third help message.
     */
	public MouseHelp(Component c, MouseDisplay md, String b1, String b2, String b3) {
		this(c, md, b1, b2, b3, InputEvent.BUTTON1_MASK);
	}

    /**
     * Constructor for MouseHelp with back messages.
     * Allows specifying different help messages for two states of the component.
     *
     * @param ckbox Component (like Checkbox) with two states.
     * @param md    MouseDisplay object to show help messages.
     * @param b1    First front message.
     * @param b2    Second front message.
     * @param b3    Third front message.
     * @param c1    First back message.
     * @param c2    Second back message.
     * @param c3    Third back message.
     */
	public MouseHelp(Component ckbox, MouseDisplay md, String b1, String b2, String b3, String c1, String c2,
			String c3) {
		this(ckbox, md, b1, b2, b3, c1, c2, c3, InputEvent.BUTTON1_MASK);
	}

    /**
     * Constructor for MouseHelp.
     * Attaches mouse help to a component with specified help messages. This constructor is 
     * used when the component does not have alternate states like a checkbox.
     *
     * @param c    The component to which this mouse help is attached. 
     *             It can be any component that is not expected to have different states.
     * @param md   MouseDisplay object to show help messages. This is where the help messages 
     *             are displayed when the mouse hovers over the component.
     * @param b1   First help message to be displayed.
     * @param b2   Second help message to be displayed.
     * @param b3   Third help message to be displayed.
     * @param mask Mouse button mask to identify the specific mouse button events of interest.
     *             This mask helps in determining for which mouse actions (like left click, right click) 
     *             the help should be shown.
     */
	public MouseHelp(Component c, MouseDisplay md, String b1, String b2, String b3, int mask) {
        this(); // Call the default constructor to initialize the front and back arrays

        // Assign help messages to the front array. These are the messages that will be displayed
        front[0] = b1;
        front[1] = b2;
        front[2] = b3;
        // Set the current help messages to front, as this constructor does not use back messages
        current = front;

        // Assign the MouseDisplay and the component to the class fields
        this.md = md;
        this.c = c;
        // Set the button mask for identifying mouse button interactions
        buttonMask = mask;
        // Add this instance as a mouse listener to the component
        c.addMouseListener(this);
	}

    /**
     * Constructor for MouseHelp with alternate states.
     * This constructor is specifically designed for components like checkboxes that have two states.
     * It allows specifying two sets of help messages: one for each state of the checkbox.
     *
     * @param ckbox Component to which this mouse help is attached. It is expected to be a Checkbox.
     * @param md    MouseDisplay object to show help messages on the screen.
     * @param b1    First help message for the primary (front) state.
     * @param b2    Second help message for the primary (front) state.
     * @param b3    Third help message for the primary (front) state.
     * @param c1    First help message for the alternate (back) state.
     * @param c2    Second help message for the alternate (back) state.
     * @param c3    Third help message for the alternate (back) state.
     * @param mask  Mouse button mask to identify the specific mouse button events of interest.
     */
	public MouseHelp(Component ckbox, MouseDisplay md, String b1, String b2, String b3, String c1, String c2, String c3,
			int mask) {
        this(); // Call the default constructor to initialize arrays

        // Assign front (primary) help messages
        front[0] = b1;
        front[1] = b2;
        front[2] = b3;

        // Assign back (alternate) help messages
        back[0] = c1;
        back[1] = c2;
        back[2] = c3;

        // Initially set the current help messages to front
        current = front;

        // Check if the component is a Checkbox and set item listener
        if (ckbox instanceof JCheckBox) {
            ((JCheckBox) ckbox).addItemListener(this);
            // Set current help messages based on the state of the Checkbox
            current = ((JCheckBox) ckbox).getState() ? back : front;
        }

        // Set the MouseDisplay and the component
        this.md = md;
        this.c = ckbox;
        buttonMask = mask;
        // Add this instance as a mouse listener to the component
        c.addMouseListener(this);
    }

    /**
     * Returns the current mouse button mask.
     * The mask indicates which mouse button's events this MouseHelp instance is interested in.
     *
     * @return The mouse button mask.
     */
	public int getMask() {
		return buttonMask;
	}

    /**
     * Changes the help messages and the mouse button mask.
     *
     * @param mask The new mouse button mask.
     * @param b1   The new first help message.
     * @param b2   The new second help message.
     * @param b3   The new third help message.
     */
	public void changeHelp(int mask, String b1, String b2, String b3) {
		front[0] = b1;
		front[1] = b2;
		front[2] = b3;
		current = front;

		buttonMask = mask;
	}

    /**
     * Swaps between front and back help messages.
     */
	public void swapHelp() {
		current = (current == front) ? back : front;
	}

    /**
     * Sets the help messages to front messages.
     */
	public void frontHelp() {
		current = front;
	}

    /**
     * Sets the help messages to back messages.
     */
	public void backHelp() {
		current = back;
	}

    /**
     * Displays the current help messages.
     */
	public void show() {
		md.show(buttonMask, current[0], current[1], current[2]);
	}

    // Displays help when the mouse enters the component
	public void mouseEntered(MouseEvent me) {
		show();
	}

    // Clears the help display when the mouse exits the component
	public void mouseExited(MouseEvent me) {
		md.clear(buttonMask);
	}

    /**
     * Removes this mouse help from the component.
     */
	public void removeHelp() {
		c.removeMouseListener(this);
	}

    // Handles state changes for components like checkboxes
	public void itemStateChanged(ItemEvent ie) {
		current = ((Checkbox) c).getState() ? back : front;
		show();
	}
}
