package examples;

public class IntArrays {
  
  public static void main(String[] args) {
    int[] ar = new int[]{10, 5};
    int t = ar[0];
    ar[0] = ar[1];
    ar[1] = t;
  }

}
