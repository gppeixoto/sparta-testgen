package instrumentation.examples;

public class Enum {
  
  enum MODEL {FORD, FIAT};
  
  public static void main(String[] args) {
    MODEL md = MODEL.FORD;
    int k = md.ordinal();
  }
  
}
