package vasco.regions;

import java.util.Vector;

import vasco.common.DrawingTarget;

/**
 * The CursorStyle class represents a style for custom cursors used in a graphics application.
 * It allows combining multiple cursor styles and provides methods for displaying them on a drawing target.
 */
public class CursorStyle {
    protected Vector<CursorStyleInterface> v;
    boolean valid;

    /**
     * Initializes a new instance of the CursorStyle class with an empty vector of cursor styles and a valid status.
     */
    public CursorStyle() {
        v = new Vector<>();
        valid = true;
    }

    /**
     * Sets the validity status of the cursor style.
     *
     * @param valid The validity status to set.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Gets the validity status of the cursor style.
     *
     * @return true if the cursor style is valid, false otherwise.
     */
    public boolean getValid() {
        return valid;
    }

    /**
     * Adds a cursor style to the collection of cursor styles.
     *
     * @param cs The cursor style to add.
     */
    public void add(CursorStyleInterface cs) {
        v.addElement(cs);
    }

    /**
     * Displays the cursor style on the specified drawing target.
     *
     * @param dt The drawing target where the cursor style will be displayed.
     */
    public void display(DrawingTarget dt) {
        for (int x = 0; x < v.size(); x++)
            v.elementAt(x).display(dt);
    }

    /**
     * Checks if the current cursor style is equal to the specified object.
     *
     * @param obj The object to compare with.
     * @return true if the cursor styles are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (v.size() != ((CursorStyle) obj).v.size()))
            return false;

        for (int x = 0; x < v.size(); x++)
            if (!((CursorStyleInterface) v.elementAt(x)).equals(((CursorStyle) obj).v.elementAt(x)))
                return false;

        return true;
    }
}
