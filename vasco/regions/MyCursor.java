package vasco.regions;

import java.awt.Color;
import java.awt.Rectangle;

import vasco.common.DrawingTarget;

/**
 * The MyCursor class represents a custom cursor used for drawing operations in a graphics application.
 * It allows for changing the cursor style and displaying the cursor on a drawing target.
 */
public class MyCursor {
    private CursorStyle currCursor;

    /**
     * Initializes a new instance of the MyCursor class with a null cursor style.
     */
    public MyCursor() {
        currCursor = null;
    }

    /**
     * Displays an invalid cursor at the specified position on the drawing target.
     *
     * @param rec   The rectangle representing the cursor's position and size.
     * @param color The color of the cursor.
     * @param dt    The drawing target where the cursor will be displayed.
     */
    public void showInvalid(Rectangle rec, Color color, DrawingTarget dt) {
        currCursor = new CursorStyle();
        currCursor.add(new ValidGridCursor(rec, color, false));
        move(dt);
    }

    /**
     * Sets the validity status of the current cursor.
     *
     * @param valid The validity status to set.
     */
    public void setValid(boolean valid) {
        currCursor.setValid(valid);
    }

    /**
     * Gets the validity status of the current cursor.
     *
     * @return true if the cursor is valid, false otherwise.
     */
    public boolean getValid() {
        return currCursor.getValid();
    }

    /**
     * Checks if the given cursor style is different from the current cursor style.
     *
     * @param cs The cursor style to compare with.
     * @return true if the cursor styles are different, false if they are the same or both null.
     */
    public boolean isDifferentCursor(CursorStyle cs) {
        if ((currCursor == null) && (cs == null))
            return false;

        if ((currCursor == null) || (cs == null))
            return true;

        return (!currCursor.equals(cs));
    }

    /**
     * Displays the current cursor on the specified drawing target.
     *
     * @param dt The drawing target where the cursor will be displayed.
     */
    public void move(DrawingTarget dt) {
        if (currCursor != null)
            currCursor.display(dt);
    }

    /**
     * Changes the current cursor style and displays the new cursor on the specified drawing target.
     *
     * @param dt The drawing target where the new cursor will be displayed.
     * @param cs The new cursor style to set and display.
     */
    public void move(DrawingTarget dt, CursorStyle cs) {
        currCursor = cs;

        // Display the new cursor
        if (currCursor != null) {
            currCursor.display(dt);
        }
    }
}