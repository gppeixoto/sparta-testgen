package instrumentation.options.stmt;

import instrumentation.options.ITransform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class ExecutionTracer implements ITransform { 

  public ExecutionTracer() { } 

  @SuppressWarnings("unchecked")
  public void transform(ClassNode cn) {

    for (MethodNode mn : (List<MethodNode>) cn.methods) {

      InsnList insns = mn.instructions;
      if (insns.size() == 0) { 
        continue;
      }
      Iterator<AbstractInsnNode> j = insns.iterator();

      List<LabelNode> exceptionHandlers = new ArrayList<LabelNode>();

      for (int i = 0; i < mn.tryCatchBlocks.size(); i++) {
        LabelNode lnode = ((TryCatchBlockNode) mn.tryCatchBlocks.get(i)).handler;
        if (lnode != null) {
          exceptionHandlers.add(lnode);
        }
      }

      while (j.hasNext()) {
        AbstractInsnNode in = j.next();

        InsnList il = new InsnList();
        notify(il, insnToString(mn, in));
        
        if (in.getPrevious() == null) {
          // ???
        } else {
          insns.insert(in.getPrevious(), il);
        }
        
        
//        if (op == Opcodes.ATHROW) {
//          // notify on explicit throws
//          notify(cn, mn, il, "throwE");
//          insns.insert(in.getPrevious(), il);
//        } else if (isRet) {
//          // notify on exits
//          notify(cn, mn, il, "exit");
//          insns.insert(in.getPrevious(), il);
//        } else if (exceptionHandlers.contains(in)) {
//          // notify on catches
//          notify(cn, mn, il, "catchE");
//          AbstractInsnNode place = getNextRelevant(j);
//          insns.insert(place.getPrevious(), il);          
//        }

      }

      // notify on entry
//      InsnList il = new InsnList(); 
//      notify(cn, mn, il, "---");      
//      insns.insert(il); 
      
      mn.maxStack += 10;
      
//      System.out.println(il.toString());
      
    } 
  }

//  private AbstractInsnNode getNextRelevant(Iterator<AbstractInsnNode> it) {
//    AbstractInsnNode insn = null;
//    while (it.hasNext()) {
//      insn = it.next();
//      if (insn != null && !(insn instanceof LineNumberNode)) {
//        break;
//      }
//    }
//    if (insn == null) {
//      throw new RuntimeException();
//    }
//    return insn;
//  }

  public static String insnToString(MethodNode mn, AbstractInsnNode insn){
    insn.accept(mp);
    StringWriter sw = new StringWriter();
    sw.append(mn.name + " : ");
    printer.print(new PrintWriter(sw));
    printer.getText().clear();
    return sw.toString().replace("\n", "\t");
  }
  
  private static Printer printer = new Textifier();
  private static TraceMethodVisitor mp = new TraceMethodVisitor(printer); 

  private void notify(InsnList il, String msg) {
    il.add(new LdcInsnNode(msg));
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "instrumentation/options/stmt/ExecutionTracer", "log", "(Ljava/lang/String;)V"));
  }
  
  private static List<String> log = new ArrayList<String>();  
  
  public static void log(String msg) {
    log.add(msg);
  }

  public static void dump() throws Exception {
    
   for (String str : log) {
     System.out.println(str);
   }
   
   
//TODO: Need to check why this code raises ClassNotFoundException -Marcelo
   
//    StringBuffer buffer = new StringBuffer();
//    for (String str : log) {
//      buffer.append(str);
//      buffer.append("\n");
//    }
//    String tmpDir = System.getProperty("java.io.tmpdir");
//    String fileName = tmpDir + System.getProperty("file.separator") + "trace.out";
//    FileWriter fw = new FileWriter(fileName);
//    fw.write(buffer.toString());
//    fw.flush();
//    fw.close();
  }

}