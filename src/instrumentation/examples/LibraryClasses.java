package instrumentation.examples;


public class LibraryClasses {
  
  public static void main(String[] args) {
    integers();
//    strings();
  }
  
  public static void integers() {
    Integer u = new Integer(2);
  }
  
  public static void strings() {
    String s = "Hello";
    s = s + "World";
  }


}
