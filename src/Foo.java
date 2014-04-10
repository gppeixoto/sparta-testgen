public class Foo {
    public static class Bar {
        int x;
        Bar(int y) {
            x = y;
        }
    }
    public static void main(String[] args) {
        Bar b = new Bar(5);
        b.x = 6;
    }
}