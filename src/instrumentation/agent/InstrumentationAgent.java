package instrumentation.agent;


import java.io.IOException;
import java.lang.instrument.Instrumentation;


/************************************************
 * This is the instrumentation agent class that 
 * must be passed in the command-line using
 * the command below.
 * 
 * -javaagent:jarpath[=options]
 ***********************************************/

public class InstrumentationAgent {

  /**
   * Prints modified bytecodes on the screen, if enabled	
   */
  public static boolean DEBUG = false;

  /**
   * This method is the entry point of the java agent.
   */
  public static void premain (String agentArgs, Instrumentation inst) throws IOException {
    // add the transformation that you want here
    // this will hook your transformation to the JVM
    // infrastructure
    inst.addTransformer(new ClassInstrumenter());
  }

}