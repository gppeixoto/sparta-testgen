package instrumentation;

public class Util {
  
  public static boolean DEBUG       = false;
  
  public static final boolean FILTER_INTEREST = false;
  //TODO: should be regex
  public static String  INTERESTED  = "examples";

  public static enum KIND { EXECUTION_TRACER, INSTRUCTION_PRINTER, ENTRYEXIT, MEMACCESS }
  public static KIND OPTION = KIND.EXECUTION_TRACER;
  //tst
}
