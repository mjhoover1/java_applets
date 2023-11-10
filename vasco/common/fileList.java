/* $Id: fileList.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

import java.awt.*;
import java.text.*;
import java.lang.*;

class fileList extends List {

  fileList(String datatype) {
    super(10, false);

    String[] s = Tools.getDir(datatype);
    for (int i = 0; i < s.length; i++)
      addItem(s[i]);
  }


}
