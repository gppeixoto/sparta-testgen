package replayer;

import java.util.HashMap;
import java.util.Map;

public class HeapCell {

  // non-native
  private Map<String, Object> map = new HashMap<String, Object>();

  public int getMapSize(){
    return map.size();
  }
  
  public Object load(String name) {
    return map.get(name);
  }

  public void store(String name, Object val) {
    assert val instanceof HeapCell || val.getClass().isPrimitive();
    map.put(name, val);
  }
}