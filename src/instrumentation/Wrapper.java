package instrumentation;

import instrumentation.options.stmt.ExecutionTracer;

public class Wrapper {
  
  public static void main(String[] args) throws Exception {
    
    
    /**
     * run test of interest and hijack log
     */
    if (args == null || args.length == 0) {
      throw new IllegalArgumentException("missing input");
    }
    Class.forName(args[0]).getDeclaredMethod("main", String[].class).invoke(null, (Object) null);
    
    
    /**
     * store in a file
     */
    ExecutionTracer.dump();
  }

}
