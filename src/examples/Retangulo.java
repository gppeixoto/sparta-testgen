package examples;

class Quadrado{
  int x;
  int y;
  
  Quadrado(int a, int b){
    this.x = a;
    this.y = b;
  }
  
  void method(int a, int b){
    this.x = a;
    this.y = b;
  }
}

/*GPS examples/Quadrado.x
CAMERA examples/Quadrado.y
SMS examples/Quadradomethod-0
NAVEGADOR examples/Quadradomethod-1
NAVEGADORB examples/Quadrado.y*/
public class Retangulo {
  public static void main(String[] args) {
    Quadrado quad = new Quadrado(20, 10);
    //quad.method(5, 10);
    quad.x = quad.y;
  }
}
