package instrumentation.examples;

public class ControlFlow {

  static void foo(int a, int b, int c) {
    int bigger, mid, smaller;
    mid = 0;
    
    if (a <= b){
      bigger = b;
      smaller = a;
    } else {
      bigger = a;
      smaller = b;
    } 
    
    if (c >= bigger){
      mid = bigger;
      bigger = c;
    } else if (c < smaller){
      mid = smaller;
      smaller = c;
    }
    
    if (bigger == smaller){
      bigger = bigger + 1;
    } else {
      smaller = mid - 1;
    }
    
    if (mid > smaller){
      mid = mid + 1;
    }
     
  }
  
  public static void main(String[] args) {
    foo(5, 10, 7);
  }
  
}