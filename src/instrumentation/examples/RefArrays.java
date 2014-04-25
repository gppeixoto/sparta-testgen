package instrumentation.examples;


public class RefArrays {

  static int K;

  static class WTF {
    String[] ar = new String[]{"A", "B"};
    WTF() {
      K = 10;
    }
  }

  @SuppressWarnings("unused")
  public static void main(String[] args) {
    WTF wtf = new WTF();
    wtf.ar[1] = ""; // killed
    String s = wtf.ar[1];
    K++; // used
    s = wtf.ar[0];
  }

}
