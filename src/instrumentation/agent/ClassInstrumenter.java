package instrumentation.agent;

import instrumentation.Util;
import instrumentation.options.ITransform;
import instrumentation.options.entryexit.EntryExitTransformer;
import instrumentation.options.memaccess.MemoryAccessTransformer;
import instrumentation.options.stmt.ExecutionTracer;
import instrumentation.options.stmt.InstructionPrinter;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;


public class ClassInstrumenter implements ClassFileTransformer {

  /************************************************************
   * 
   * This method is invoked for every class that the JVM is 
   * about to load.  It triggers the instrumentation on a class.
   * 
   * 1. JVM may install a class file transformer if there is an 
   *    instrumentation agent installed (see ClassInstrumenter.java)
   *     
   * 2. JVM requests a class to the class loader
   * 
   * 3. Before loading any class file, the ClassLoader calls 
   *    transform (see method below) in it.  This enables one to change
   *    the class file however he wants using any instrumentation library
   *    he wants.  We chose ASM. 
   * 
   *********************************************************/
  @Override
  public byte[] transform(ClassLoader loader, String className,
      Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer) throws IllegalClassFormatException {
    byte[] result = classfileBuffer;

    /**
     * The class will be instrumented only if 
     * it is a class of "interest"
     */
    if (!Util.FILTER_INTEREST || className.contains(Util.INTERESTED)) {
      
      // building class node object
      ClassReader cr = new ClassReader(classfileBuffer);
      ClassNode cnode = new ClassNode(Opcodes.ASM4);
      cr.accept(cnode, 0);

      // transforming class node object

      ITransform transformer;
      switch (Util.OPTION) {
      case ENTRYEXIT:
        transformer = new EntryExitTransformer();
        break;
      case MEMACCESS:
        transformer = new MemoryAccessTransformer();
        break;
      case INSTRUCTION_PRINTER:
        transformer = new InstructionPrinter();
        break;
      case EXECUTION_TRACER:
        transformer = new ExecutionTracer();
        break;
      default:
        throw new UnsupportedOperationException();
      }

      try {
        transformer.transform(cnode);
      }
      catch (RuntimeException _) {
        _.printStackTrace();
        throw _;
      }

      // building JVM bytecodes
      ClassWriter cw = new ClassWriter(0);

      if (Util.DEBUG) {
        TraceClassVisitor tracer = new TraceClassVisitor(cw, new PrintWriter(System.out));
        cnode.accept(tracer);
      } else {
        cnode.accept(cw);
      }

      // spitting out modified bytecodes
      result = cw.toByteArray();
    }

    return result;
  }
}