package examples;

class Rectangle {
  private double length;
  private double height;
  
  public Rectangle(double x, double y){
    this.length = x;
    this.height = y;
  }
  
  public double getLength(){
    return this.length;
  }
  
  public double getHeight(){
    return this.height;
  }
  
  public void setLength(double l){
    this.length = l;
  }
  
  public void setHeight(double h){
    this.height = h;
  }
}

class Circle{
  private double radius;
  
  public Circle(double r){
    this.radius = r;
  }
  
  public void setRadius(double r){
    this.radius = r;
  }
  
  public double getRadius(){
    return this.radius;
  }
  
  public double shrink(double factor){
    return this.radius*factor;
  }
}

public class Geometry {
  public static void main(String[] args){
    Circle c = new Circle(3.14);
    Rectangle r = new Rectangle(10,0.8);
    double s = c.shrink(0.8);
    double g = c.getRadius();
    double sX = 0.8*g;
    double sY = 0.6*s;
    r.setLength(sX);
    r.setHeight(sY);
  }
}
