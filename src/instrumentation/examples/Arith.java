package instrumentation.examples;

public class Arith {
  
  public static void main(String[] args) {
    int x = 10;
    x = x * 100 + 23 - 29 / 4 ^ 4 % 2 & 43 | 1000;
    
    
    double y = 10;
    y = y * 100 + 23 - 29 / 4 % 243;
  }

}
