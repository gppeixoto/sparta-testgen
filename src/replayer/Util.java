package replayer;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;

public class Util {

  public static AccessibleObject lookup(String[] args) {
    try {
      Class<?> clazz = Class.forName(args[0].replace('/', '.'));
      String params = "";
//      if (!args[2].startsWith("()")) {
//        System.out.println(Arrays.toString(args) + "===> " + args[2]);
       params = args[2].substring(args[2].indexOf("(")+1, args[2].indexOf(")"));
//      }
      List<Class<?>> list = new ArrayList<Class<?>>();
      while (!params.equals("")) {
        char c = params.charAt(0);
        Class<?> tp;
        switch (c) {
        case 'I':
          tp = Integer.TYPE;
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
        case 'J':
          tp = Long.TYPE;
          break;
        case 'V':
          tp = Void.TYPE;
          break;
        case 'L':
          int idx = params.indexOf(";");
          String tmp = params.substring(1, idx);
          tp = Class.forName(tmp.replace("/", "."));
          params = params.substring(idx+1);
          break;
        default:
          throw new RuntimeException("missing implementation --->" + c);
        }
        if (params.length() > 0) {
          if (c != 'L') {
            params = params.substring(1);
          }
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
