package vasco.drawable;
import java.awt.*;
import vasco.common.*;

abstract public class GenDrawable implements GenElement {
  Drawable drawable;

  GenDrawable(Drawable r) {
    drawable = r;
  }

  void draw(DrawingTarget g) {
    drawable.draw(g);
  }

  public void drawElementFirst(DrawingTarget g) {
    g.setColor(Color.yellow);
    drawable.draw(g);
  }

  public abstract void drawElementNext(DrawingTarget g);

  public abstract int pauseMode();

  public void fillElementFirst(DrawingTarget g) {};
  public void fillElementNext(DrawingTarget g) {};

}
