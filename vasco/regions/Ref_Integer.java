package vasco.regions;
// This is used for passing ints by reference

public class Ref_Integer{
  public int value;

  public Ref_Integer(int n){
    value = n;
  } 

  public int intValue(){
    return value;
  }

  public void setValue(int n){   
    value = n;
    return;
  }
}
