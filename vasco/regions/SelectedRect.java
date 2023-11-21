package vasco.regions;

import javax.swing.*; // import java.awt.*;

public class SelectedRect{
  public Point ul, lr;
  public boolean selected;

  public SelectedRect(){
    ul = null;
    lr = null;
    selected = false;
  }

  public Rectangle get(){
    int ux, uy;
    int w, h;

    if (ul.x < lr.x){
      ux = ul.x;
      w = lr.x - ul.x;
    }
    else{
      ux = lr.x;
      w = ul.x - lr.x;
    }

    if (ul.y < lr.y){
      uy = ul.y;
      h = lr.y - ul.y;
    }
    else{
      uy = lr.y;
      h = ul.y - lr.y;
    }
    
    w++;
    h++;

    return new Rectangle(ux, uy, w, h);
  }
}
 
