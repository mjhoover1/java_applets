package vasco.regions;

// import java.applet.*;
import javax.swing.*; // import java.awt.*;


/* 
 * First we define a helper class that will
 * implement some useful math functions
 */

public class MF{
  public static int power (int base, int exponent){
    int i;
    int result;

    if (0 > exponent) return 0;

    result = 1;
 
    for(i=0; i<exponent; i++)
      result = result*base;

    return result;
  }

  public static int min (int i1, int i2){
    if (i1>i2) return i2;

    return i1;
  }

  
  public static int max (int i1, int i2){
   
    if (i1<i2) return i2;

    return i1;
  }
  
  public static int mod (int i1, int i2){
    int i;

    if (i1 < 0) return 0;

    i = 0;
    while (i*i2 < i1)
      i++;

    if (i1 == i*i2) return 0;
    else return (i1 - (i-1)*i2);
  }

  public static int lor (int i1, int i2){
    int result, a, r1, r2;

    result = 0;
    a = 1;

    while ((i1 !=0)||(i2 != 0)){
      r1 = i1 % 2;
      r2 = i2 % 2;
      
      if ((r1 !=0)||(r2 !=0))
	result = result + a;

      a = a*2;
      i1 = i1/2;
      i2 = i2/2;
    }
    
    return result;
  }

  public static int land(int i1, int i2){
    int result, a, r1, r2;
    
    result = 0;
    a = 1;

    while ((i1 !=0)&&(i2 != 0)){
      r1 = i1 % 2;
      r2 = i2 % 2;
      
      if ((r1 == 1)&&(r2 == 1))
	result = result + a;
      
      a = a*2;
      i1 = i1/2;
      i2 = i2/2;
    }
    
    return result;
  }

  public static int[] interleaved(int position){
    int tmp;
    int i, a;
    int result[];

    result = new int[2];

    tmp = position;

    result[0] = 0;
    result[1] = 0;

    i = 0;
    a = 1;

    while (0 != tmp){
      if (0 == i%2)
	result[0] = result[0] +(tmp%2)*a; 
      else{
	result[1] = result[1] + (tmp%2)*a;
	a= a*2;
      }
      i++;

      tmp = tmp/2;
    }

    return result;
  }

}




