package vasco.common;

import javax.swing.JComboBox;

/**
 * This class provides various helper utility functions.
 */
public class Helper {

    /**
     * Converts the items of a JComboBox to a String representation.
     * 
     * @param comboBox The JComboBox whose items are to be converted to String.
     * @return A String representation of all items in the JComboBox.
     */
    public static String comboBoxItemsToString(JComboBox<String> comboBox) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            items.append(comboBox.getItemAt(i));
            if (i < comboBox.getItemCount() - 1) {
                items.append(", ");
            }
        }
        return items.toString();
    }

    // You can add more helper methods here as needed
}