/* $Id: fileList.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

// import java.awt.*;
import javax.swing.DefaultListModel;
import javax.swing.JList;

class fileList extends JList<String> { // {

	fileList(String datatype) {
		super(new DefaultListModel<>()); // super(10, false);

		String[] s = Tools.getDir(datatype);
		DefaultListModel<String> listModel = (DefaultListModel<String>) getModel(); // Get the list model

		for (String element : s)
			listModel.addElement(element); // Add elements to the list model addItem(s[i]);
	}

}
