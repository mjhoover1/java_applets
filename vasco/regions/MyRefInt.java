package vasco.regions;
public class MyRefInt{
  int value;
   
  MyRefInt(){
    value = 0;
  } 

  MyRefInt(int v){
    value = v;
  }

  public int getValue(){
    return value;
  }

  public void setValue(int v){
    value = v;
  }

}
