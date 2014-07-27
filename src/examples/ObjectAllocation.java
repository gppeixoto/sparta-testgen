package examples;

public class ObjectAllocation {
  
  static class NonSense {
    int f;
    NonSense() { }
  }
  
  public static void main(String[] args) {
    NonSense ns = new NonSense();
    ns.f = 5;
  }

}
