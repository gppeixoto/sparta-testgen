package instrumentation.examples;

public class RefArrayAllocation {

  static class NonSense {
    String[] ar = new String[]{};
  }
  
  public static void main(String[] args) {
    
    NonSense ns = new NonSense();
    
  }

}
