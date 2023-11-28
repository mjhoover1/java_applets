package vasco.regions;
import java.awt.Color;

import javax.swing.*; // import java.awt.*;
 
public class Constants{

  // Color codes

  public static final int BLACK = 0;
  public static final int WHITE = 1;
  public static final int GRAY = 2;
  public static final int NO_COLOR = -1;
  public static final int YELLOW = 3;
  public static final int RED = 4;

  // Operations color code
  public static final Color Color_Current = Color.yellow;
  public static final Color Color_Processed = Color.blue;


  // quad-tree related constants

  public final static int NW = 0;
  public final static int NE = 1;
  public final static int SW = 2;
  public final static int SE = 3;

  public final static int N = 0;
  public final static int E = 1;
  public final static int S = 2;
  public final static int W = 3;
  


  public Constants(){
    int i;
  }

  public static String formatString(String s, int width) {
    int i;
    int index = 0;
    while (s.length() - index > width) {
      for (i = Math.min(index + width, s.length() - 1); i > index; i--) {
        if (s.charAt(i) == ' ') {
          s = s.substring(0, i) + new String("\n") + s.substring(i + 1);
          index = i + 1;
          break;
        }
      }
      if (index == i) {
        index += MF.min(index + width, s.length() - index);
      }
    }
    return s;
  }

  public static int opquad(int q){
    if (Constants.NE == q) return Constants.SW;
    if (Constants.NW == q) return Constants.SE;
    if (Constants.SE == q) return Constants.NW;
    if (Constants.SW == q) return Constants.NE;

    return -1;
  }
 
  public static int cquad(int q){
    if (Constants.NE == q) return Constants.SE;
    if (Constants.NW == q) return Constants.NE;
    if (Constants.SE == q) return Constants.SW;
    if (Constants.SW == q) return Constants.NW;

    return -1;
  }
  
  public static int ccquad(int q){
    if (Constants.NE == q) return Constants.NW;
    if (Constants.NW == q) return Constants.SW;
    if (Constants.SE == q) return Constants.NE;
    if (Constants.SW == q) return Constants.SE;

    return -1;
  }

  public static int common_edge(int q1, int q2){

    if ((Constants.NW == q1)&&(Constants.NE == q2)) return Constants.E;
    if ((Constants.NW == q2)&&(Constants.NE == q1)) return Constants.E;

    if ((Constants.NE == q1)&&(Constants.SE == q2)) return Constants.S;
    if ((Constants.NE == q2)&&(Constants.SE == q1)) return Constants.S;

    if ((Constants.SE == q1)&&(Constants.SW == q2)) return Constants.W;
    if ((Constants.SE == q2)&&(Constants.SW == q1)) return Constants.W;
    
    if ((Constants.SW == q1)&&(Constants.NW == q2)) return Constants.N;
    if ((Constants.SW == q2)&&(Constants.NW == q1)) return Constants.N;

    return -1;

  }

  public static int reflect(int e, int q){

    if (Constants.NE == q){
      if (Constants.E == e) return Constants.NW;
      if (Constants.W == e) return Constants.NW;
      if (Constants.N == e) return Constants.SE;
      if (Constants.S == e) return Constants.SE;
      return -1;
    }

    if (Constants.NW == q){
      if (Constants.E == e) return Constants.NE;
      if (Constants.W == e) return Constants.NE;
      if (Constants.N == e) return Constants.SW;
      if (Constants.S == e) return Constants.SW;
      return -1;
    }

    if (Constants.SE == q){
      if (Constants.E == e) return Constants.SW;
      if (Constants.W == e) return Constants.SW;
      if (Constants.N == e) return Constants.NE;
      if (Constants.S == e) return Constants.NE;
      return -1;
    }
      
    if (Constants.SW == q){
      if (Constants.E == e) return Constants.SE;
      if (Constants.W == e) return Constants.SE;
      if (Constants.N == e) return Constants.NW;
      if (Constants.S == e) return Constants.NW;
      return -1;
    }
  
    return -1;
  }

  public static int cedge(int e){
    
    if (Constants.N == e) return Constants.E;
    if (Constants.E == e) return Constants.S;
    if (Constants.S == e) return Constants.W;
    if (Constants.W == e) return Constants.N;

    return -1;
  }

  public static int ccedge(int e){
    
    if (Constants.N == e) return Constants.W;
    if (Constants.E == e) return Constants.N;
    if (Constants.S == e) return Constants.E;
    if (Constants.W == e) return Constants.S;

    return -1;
  }

  public static int opedge(int e){

    if (Constants.N == e) return Constants.S;
    if (Constants.E == e) return Constants.W;
    if (Constants.S == e) return Constants.N;
    if (Constants.W == e) return Constants.E;

    return -1;
  }


  public static int quad(int e1, int e2){

    if (Constants.N == e1){
      if (Constants.E == e2) return Constants.NE;
      if (Constants.W == e2) return Constants.NW;
    }

    if (Constants.S == e1){
      if (Constants.E == e2) return Constants.SE;
      if (Constants.W == e2) return Constants.SW;
    }

    if (Constants.W == e1){
      if (Constants.N == e2) return Constants.NW;
      if (Constants.S == e2) return Constants.SW;
    }

    if (Constants.E == e1){
      if (Constants.N == e2) return Constants.NE;
      if (Constants.S == e2) return Constants.SE;
    }

    return -1;
  }

  public static boolean adj(int e, int q){

    if (Constants.N == e){
      if (Constants.NW == q) return true;
      if (Constants.NE == q) return true;
      if (Constants.SW == q) return false;
      if (Constants.SE == q) return false;
      return false;
    }

    if (Constants.E == e){
      if (Constants.NW == q) return false;
      if (Constants.NE == q) return true;
      if (Constants.SW == q) return false;
      if (Constants.SE == q) return true;
      return false;
    }

    if (Constants.S == e){
      if (Constants.NW == q) return false;
      if (Constants.NE == q) return false;
      if (Constants.SW == q) return true;
      if (Constants.SE == q) return true;
      return false;
    }
     
    if (Constants.W == e){
      if (Constants.NW == q) return true;
      if (Constants.NE == q) return false;
      if (Constants.SW == q) return true;
      if (Constants.SE == q) return false;
      return false;
    }

    return false;
  }
  

}

