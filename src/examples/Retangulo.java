package examples;

class Quadrado{
  int largura;
  int altura;
  
  Quadrado(int a, int b){
    this.largura = a;
    this.altura = b;
  }
  
  void changeDimension(int a, int b){
    this.largura = a;
    this.altura = b;
  }
}

public class Retangulo {
  public static void main(String[] args) {
    Quadrado quad = new Quadrado(20, 10);
    quad.changeDimension(5, 10);
  }
}