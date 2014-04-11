package instrumentation;

public class Util {

  public static boolean DEBUG       = false;
  public static String  INTERESTED  = "examples";

  public static enum KIND { INSTRUCTION_PRINTER, ENTRYEXIT, MEMACCESS }
  public static KIND OPTION = KIND.INSTRUCTION_PRINTER;

}
