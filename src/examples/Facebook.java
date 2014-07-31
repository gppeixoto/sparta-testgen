package examples;

class Song{
  private String artista;
  private String titulo;
  private String genero;
  
  Song(String a, String t, String g){
    this.artista = a;
    this.titulo = t;
    this.genero = g;
  }

  public String getTitulo() {
    return titulo;
  }
  
  public void setTitulo(String s){
    this.titulo = s;
  }
}

class Itunes{
  private Song m;

  public Song getM() {
    return m;
  }
  
  public Itunes(Song m){
    this.m = m;
  }
  
  public void setSong(Song s){
    this.m=s;
  }
}

public class Facebook{
  
  private Itunes g;
  public Facebook(Itunes g){
    this.g = g;
  }
  
  public String getTituloMusica(){
    return this.g.getM().getTitulo();
  }

  public static void main(String[] args) {
   Song m = new Song("Legião Urbana", "Que País é Esse", "Rock Nacional");
   Itunes g = new Itunes(m);
   Facebook fb = new Facebook(g);
   m.setTitulo(fb.getTituloMusica());
  }
}