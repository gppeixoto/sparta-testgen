package replayer;

import java.util.HashMap;
import java.util.Map;

public class StaticArea {
  
  private Map<Class<?>, Map<String, MyObject>> sa = new HashMap<Class<?>, Map<String, MyObject>>();

  public void putStatic(Class<?> clazz, String fieldName, MyObject val) {
    Map<String, MyObject> table = sa.get(clazz);
    if (table == null) {
      table = new HashMap<String, MyObject>();
      sa.put(clazz, table);
    }
    table.put(fieldName, val);
  }
  
  public MyObject getStatic(Class<?> clazz, String fieldName) {
    Map<String, MyObject> table = sa.get(clazz);
    if (table == null) {
      table = new HashMap<String, MyObject>();
      sa.put(clazz, table);
    }
    return table.get(fieldName);
  }
  
}