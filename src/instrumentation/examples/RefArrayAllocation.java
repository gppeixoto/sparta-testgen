package instrumentation.examples;

public class RefArrayAllocation {

  static class NonSense {
    String[] ar = new String[]{};
  }
  
  public static void main(String[] args) {
    
    @SuppressWarnings("unused")
    NonSense ns = new NonSense();
    
  }

}
