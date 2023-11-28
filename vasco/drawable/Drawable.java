package vasco.drawable;
import java.awt.Color;
import javax.swing.*; // import java.awt.*;
import vasco.common.*;

public interface Drawable {
    public void draw(DrawingTarget g);
    public void directDraw(Color c, DrawingTarget g);

    public void drawBuffer(Color c, DrawingTarget dt, double dist);

    public DRectangle getBB();
    public boolean hasArea();

    public double distance(DPoint p);
    public double[] distance(DPoint p, double [] k);
    public double distance(DLine p);
    public double[] distance(DLine p, double[] k);
    public double distance(DRectangle p);
    public double[] distance(DRectangle p, double[] k);

    public boolean intersects(DRectangle r);

}
