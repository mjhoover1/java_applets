package vasco.regions;

import java.awt.Color;
import java.awt.Rectangle;

import vasco.common.DrawingTarget;

/**
 * The ValidGridCursor class represents a cursor style for highlighting a valid grid area or cell in a graphics application.
 * It allows setting the color, shape, and validity status of the cursor style and provides a method to display it on a drawing target.
 */
public class ValidGridCursor implements CursorStyleInterface {
    protected Color color;
    protected Rectangle rect;
    protected boolean isValid;

    /**
     * Initializes a new ValidGridCursor with the specified rectangle and color.
     *
     * @param rect  The rectangle representing the cursor's position and size.
     * @param color The color of the cursor.
     */
    public ValidGridCursor(Rectangle rect, Color color) {
        this.rect = rect;
        this.color = color;
        isValid = true;
    }

    /**
     * Initializes a new ValidGridCursor with the specified rectangle, color, and validity status.
     *
     * @param rect    The rectangle representing the cursor's position and size.
     * @param color   The color of the cursor.
     * @param isValid The validity status of the cursor (true for valid, false for invalid).
     */
    public ValidGridCursor(Rectangle rect, Color color, boolean isValid) {
        this.rect = rect;
        this.color = color;
        this.isValid = isValid;
    }

    /**
     * Displays the ValidGridCursor on the specified drawing target.
     *
     * @param dt The drawing target where the cursor will be displayed.
     */
    @Override
    public void display(DrawingTarget dt) {
        // dt.setColor(color);
        dt.directRect(color, rect.x, rect.y, rect.width, rect.height);
        if (!isValid) {
            dt.directLine(color, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
            dt.directLine(color, rect.x + rect.width, rect.y, rect.x, rect.y + rect.height);
        }
        // System.out.println("Displaying ValidGridCursor: " + rect + ", Color: " + color + ", isValid: " + isValid);
    }

    /**
     * Checks if the current ValidGridCursor is equal to the specified object.
     *
     * @param obj The object to compare with.
     * @return true if the cursors are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof ValidGridCursor) {
            ValidGridCursor vgc = (ValidGridCursor) obj;
            return (color.equals(vgc.color) && rect.equals(vgc.rect) && isValid == vgc.isValid);
        }

        return false;
    }
}
