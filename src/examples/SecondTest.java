package examples;

class GPS {
  public GPS(){}

  public int readGPS(){
    return 5;
  }

  public void sendSMS(int sms){
    return;
  }
}

public class SecondTest {

  public static void main (String[] args){
    GPS gps = new GPS();
    int pos = gps.readGPS();
    gps.sendSMS(pos);
  }
}