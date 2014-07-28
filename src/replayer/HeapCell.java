package replayer;

import java.util.HashMap;
import java.util.Map;

public class HeapCell {

  // non-native
  private Map<String, MyObject> map = new HashMap<String, MyObject>();

  public int getMapSize(){
    return map.size();
  }
  
  public MyObject load(String name) {
    return map.get(name);
  }

  public void store(String name, MyObject val) {
    assert val.getObject() instanceof HeapCell || val.getClass().isPrimitive();
    map.put(name, val);
  }
}