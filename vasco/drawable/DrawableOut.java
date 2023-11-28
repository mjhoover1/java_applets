package vasco.drawable;

import java.awt.Color;

import javax.swing.*; // import java.awt.*;
import vasco.common.*;

public class DrawableOut extends GenDrawable {
  public DrawableOut(Drawable r) {
    super(r);
  }

  public void drawElementNext(DrawingTarget g) {
    g.setColor(Color.magenta);
    drawable.draw(g);
  }

  public int pauseMode() {
    return FAIL;
  }
}
