package examples;

class GPS {
  int lat;
  int longi;

  GPS(int lat, int longi) {
    this.lat = lat;
    this.longi = longi;
  }

  int getLat() {
    return this.lat;
  }

  int getLongi() {
    return this.longi;
  }

  void setNewPosition(int lat, int longi) {
    this.lat = lat;
    this.longi = longi;
  }
  
  GPS getLocation(){
    return this;
  }
}

class SMS{
  String sender;
  String receiver;
  String message;
  GPS gps = new GPS(0, 0);
  
  SMS(){}
  
  void newMessage(String sender, String receiver, String message){
    this.sender = sender;
    this.receiver = receiver;
    this.message = message;
  }
  
  SMS newMessageWithLocation(String s, String r, String m, GPS g){
    this.sender = s;
    this.receiver = r;
    this.message = m;
    this.gps = g;
    return this;
  }
  
  String getMessage(){
    return this.message;
  }
  
  void setGPS(GPS g){
    this.gps = g;
  }
  
  void setMessage(String s){
    this.message = s;
  }
}

class Message{
  String text;
  
  Message(){this.text = "Text";}
  
  String getMessage(){
    return this.text;
  }
  
  void setMessage(String s){
    this.text = s;
  }
}

public class Celular {
  public static void main(String[] args) {
    GPS gps = new GPS(30, 40);
    SMS sms = new SMS();
    SMS newSMS = sms.newMessageWithLocation("Alice", "Bob", "Test", gps.getLocation());
    newSMS.setGPS(gps.getLocation());
    Message m = new Message();
    m.setMessage(newSMS.getMessage());
    int newLat = gps.getLat();
    int newLongi = gps.getLongi();
    gps.setNewPosition(newLat, newLongi);
    //gps.setNewPosition(gps.getLat(), gps.getLongi());
    /*GPS examples/GPSgetLocation
    SMS_LOCATION examples/SMSnewMessageWithLocation-3
    SMS_GPS examples/SMS.gps
    SMS_MESSAGE examples/SMSgetMessage
    TEXT examples/Message.text*/
  }
}