package instrumentation.examples;

public class Switch {
  
  public static void main(String[] args) {
    int k = 10;
    switch (k) {
    case 1:
      k = 55;
      break;
    case 10:
      k = -1;
      break;
    default:
      break;
    }
  }

}
