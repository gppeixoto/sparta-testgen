package replayer;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;

public class Util {

  public static AccessibleObject lookup(String[] args) {
    try {
      Class<?> clazz = Class.forName(args[0].replace('/', '.'));
      String params = args[2].substring(args[2].indexOf("(")+1, args[2].indexOf(")"));
      List<Class<?>> list = new ArrayList<Class<?>>();
      while (!params.equals("")) {
        char c = params.charAt(0);
        Class<?> tp;
        switch (c) {
        case 'I':
          tp = Integer.TYPE;
          break;
        case 'L':
          tp = Long.TYPE;
          break;
        case 'S':
          tp = Short.TYPE;
          break;
        case 'B':
          tp = Byte.TYPE;
          break;
        case 'C':
          tp = Character.TYPE;
          break;
        case 'F':
          tp = Float.TYPE;
          break;
        case 'D':
          tp = Double.TYPE;
          break;
        case 'Z':
          tp = Boolean.TYPE;
          break;
        default:
          throw new RuntimeException("missing implementation");
        }
        if (params.length() > 1) {
          params = params.substring(1);
        } else {
          params = "";
        }
        list.add(tp);
      }
      Class<?>[] ar = new Class<?>[list.size()];
      list.toArray(ar);

      AccessibleObject aobj;
      if (args[1].equals("<init>")) {
        aobj = clazz.getDeclaredConstructor(ar);
      } else {
        aobj = clazz.getDeclaredMethod(args[1], ar);
      }
      aobj.setAccessible(true);
      return aobj;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("unexpected");
    }

  }

}
