package instrumentation.examples;

public class StaticRef {
  
  static String s;
  static Object t;
  
  public static void main(String[] args) {
    s = "Hello";
    t = s;
  }
}
