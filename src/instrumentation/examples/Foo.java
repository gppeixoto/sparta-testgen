package instrumentation.examples;

public class Foo {
  static void foo(int a, int b, int c) {
    
    //TODO: I recommend to write comments and variable names in English -Marcelo
    
    //IP L1 Q1 ->Maior,medio,menor
    int maior, medio, menor;
    medio = 0;
    if (a <= b){
      maior = b;
      menor = a;
    } else {
      maior = a;
      menor = b;
    } if ( c >= maior){
      medio = maior;
      maior = c;
    } else if (c < menor){
      medio = menor;
      menor = c;
    }
     if (maior == menor){
       maior = maior + 1;
     } else {
      menor = medio - 1;
     }
     if (medio > menor){
       medio = medio + 1;
     }
     
  }
  public static void main(String[] args) {
    foo(5, 10, 7);
  }
}