package replayer;

import java.util.HashMap;
import java.util.Map;

public class StaticArea {
  
  private Map<Class<?>, Map<String, Object>> sa = new HashMap<Class<?>, Map<String, Object>>();

  public void putStatic(Class<?> clazz, String fieldName, Object val) {
    Map<String, Object> table = sa.get(clazz);
    if (table == null) {
      table = new HashMap<String, Object>();
      sa.put(clazz, table);
    }
    table.put(fieldName, val);
  }
  
  public Object getStatic(Class<?> clazz, String fieldName) {
    Map<String, Object> table = sa.get(clazz);
    if (table == null) {
      table = new HashMap<String, Object>();
      sa.put(clazz, table);
    }
    return table.get(fieldName);
  }
  
}