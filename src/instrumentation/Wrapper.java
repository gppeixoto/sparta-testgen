package instrumentation;


public class Wrapper {
  
  public static void main(String[] args) throws Exception {
    if (args == null || args.length == 0) {
      throw new IllegalArgumentException("missing input");
    }
    Class.forName(args[0]).getDeclaredMethod("main", String[].class).invoke(null, (Object) null);
    instrumentation.options.stmt.ExecutionTracer.dump();
  }

}
