package instrumentation.examples;

public class ObjectAllocation {
  
  static class NonSense {
    int f;
    NonSense() {
//      this.f = 1;
    }
  }
  
  public static void main(String[] args) {
    NonSense ns = new NonSense();
    ns.f = 5;
  }

}
