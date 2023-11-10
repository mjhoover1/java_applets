package vasco.regions;
public class RefInt{
  int value;
  
  RefInt(){
    value = 0;
  }

  RefInt(int v){
    value = v;
  }

  public int getValue(){
    return value;
  }

  public void setValue(int v){
    value = v;
  }

}
