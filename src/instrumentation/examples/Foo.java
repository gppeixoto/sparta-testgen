package instrumentation.examples;

public class Foo {
  static int foo(int a, int b) {
    return a + b;
  }
  public static void main(String[] args) {
    int a = foo(5, 10);
  }
}