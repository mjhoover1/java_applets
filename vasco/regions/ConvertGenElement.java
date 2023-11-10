package vasco.regions;
import vasco.common.*;

import java.awt.*;

public class ConvertGenElement implements GenElement{
  protected boolean mCopy;

  public ConvertGenElement(){
    mCopy = true;
  }

  public boolean makeCopy(){
    return mCopy;
  }

  public void fillElementFirst(DrawingTarget g){
  }

  public void fillElementNext(DrawingTarget g){
  }

  public void drawElementFirst(DrawingTarget g){
  }

  public void drawElementNext(DrawingTarget g){
  }

  public int pauseMode(){
    return 0;
  }

}
