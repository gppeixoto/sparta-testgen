package replayer.examples;

import java.util.ArrayList;
import java.util.List;

public class Samples {


  static List<String> sample1() {
    List<String> result = new ArrayList<String>();
    result.add("bipush 10");
    result.add("istore_1");
    result.add("iload_1");
    result.add("iconst_2");
    result.add("imul");
    result.add("istore_1");
    result.add("return");
    return result;
  }

  static List<String> sample2() {
    List<String> result = new ArrayList<String>();
    result.add("iconst_1");
    result.add("newarray int");
    result.add("dup");
    result.add("iconst_0");
    result.add("bipush	10");
    result.add("iastore");
    result.add("astore_1");
    result.add("aload_1");
    result.add("iconst_0");
    result.add("aload_1");
    result.add("iconst_0");
    result.add("iaload");
    result.add("iconst_2");
    result.add("imul");
    result.add("iastore");
    result.add("return");
    return result;
  }

  public static List<String> sample3() {
    List<String> result = new ArrayList<String>();
    result.add("iconst_5");
    result.add("bipush	10");
    result.add("invokestatic	#2; //Method foo:(II)I");
    result.add("iload_0");
    result.add("iload_1");
    result.add("iadd");
    result.add("ireturn");
    result.add("istore_1");
    result.add("return");
    return result;
  }

}