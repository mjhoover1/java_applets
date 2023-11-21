/* $Id: PanelListener.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import javax.swing.*; // import java.awt.*;
import javax.swing.event.*; // import java.awt.event.*;
//import java.applet.*;
//import java.util.*;

/**
 * PanelListener is a class that implements the ContainerListener interface.
 * It is designed to resize a Panel individually for each structure based on the
 * number of elements in it. This is achieved by adjusting the layout of the
 * Panel using GridBagLayout whenever components are added or removed.
 */
public class PanelListener implements ContainerListener {

    // The Panel to be dynamically resized
    JPanel tovalid;

    // Reference to the AppletValidate instance
    AppletValidate appl;

    /**
     * Constructs a PanelListener with the specified Panel and AppletValidate
     * instances.
     *
     * @param p The Panel to be dynamically resized.
     * @param a The AppletValidate instance.
     */
    public PanelListener(JPanel p, AppletValidate a) {
        tovalid = p;
        appl = a;
    }

    /**
     * Adjusts the layout of the Panel based on the number of components in it,
     * and triggers a global validation in the associated AppletValidate instance.
     */
    void adjustPanel() {
        GridBagLayout gbl = new GridBagLayout();
        tovalid.setLayout(gbl);

        // Constraints for the first component
        GridBagConstraints top = new GridBagConstraints();
        top.gridx = top.gridy = 0;

        // Constraints for the rest of the components
        GridBagConstraints rest = new GridBagConstraints();
        rest.gridx = 0;
        rest.gridy = GridBagConstraints.RELATIVE;

        // Applying constraints to each component in the Panel
        for (int i = 1; i < tovalid.getComponentCount(); i++)
            gbl.setConstraints(tovalid.getComponent(i), i == 0 ? top : rest);

        // Triggering a global validation in the AppletValidate instance
        appl.globalValidate();
    }

    /**
     * Invoked when a component has been added to the container.
     * Adjusts the layout of the Panel and triggers a global validation.
     *
     * @param e The ContainerEvent associated with the component addition.
     */
    public void componentAdded(ContainerEvent e) {
        adjustPanel();
    }

    /**
     * Invoked when a component has been removed from the container.
     * Adjusts the layout of the Panel and triggers a global validation.
     *
     * @param e The ContainerEvent associated with the component removal.
     */
    public void componentRemoved(ContainerEvent e) {
        adjustPanel();
    }
}
