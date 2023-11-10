/* $Id: CommonConstants.java,v 1.1.1.1 2002/09/25 05:48:35 brabec Exp $ */
package vasco.common;

public interface CommonConstants {
  static final double XF[] = {-0.25, 0.25, -0.25, 0.25};
  static final double YF[] = {0.25, 0.25, -0.25, -0.25};

  static final int BLACK = 0;
  static final int WHITE = 1;
  static final int GRAY = 2;

  static final int NW = 0;
  static final int NE = 1;
  static final int SW = 2;
  static final int SE = 3;

  static final int LEFT = 0;
  static final int RIGHT = 1;

  static final int SEARCHMODE_CONTAINS = 0x01;
  static final int SEARCHMODE_ISCONTAINED = 0x02;
  static final int SEARCHMODE_OVERLAPS = 0x04;
  static final int SEARCHMODE_CROSSES = 0x10; // line

    final static int RUNMODE_CONTINUOUS = 0;
    final static int RUNMODE_OBJECT = 1;
    final static int RUNMODE_SUCCESS = 2;

    static final double accuracy = 1e-6;
}
