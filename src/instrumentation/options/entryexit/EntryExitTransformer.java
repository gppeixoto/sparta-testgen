package instrumentation.options.entryexit;

import instrumentation.options.ITransform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class EntryExitTransformer implements ITransform { 

  public EntryExitTransformer() { } 

  @SuppressWarnings("unchecked")
  public void transform(ClassNode cn) {

    for (MethodNode mn : (List<MethodNode>) cn.methods) {
      //<init> - class initialization;
      //<clinit> - static block initialization
      //      if ("<init>".equals(mn.name) || "<clinit>".equals(mn.name)) {
      //        continue;
      //      }
      InsnList insns = mn.instructions;
      if (insns.size() == 0) { 
        continue;
      }
      Iterator<AbstractInsnNode> j = insns.iterator();

      List<LabelNode> exceptionHandlers = new ArrayList<LabelNode>();

      //TODO: FIX THIS      
      //      if (mn.name.contains("xfoo")) {
      for (int i = 0; i < mn.tryCatchBlocks.size(); i++) {
        LabelNode lnode = ((TryCatchBlockNode) mn.tryCatchBlocks.get(i)).handler;
        if (lnode != null) {
          exceptionHandlers.add(lnode);
        }
      }
      //      }

      //      int stackIncrement = 0;
      while (j.hasNext()) {
        AbstractInsnNode in = j.next();
        //if (mn.name.contains("xfoo")) {
        //          System.out.println(in.getType() + " " + (in.getType()==AbstractInsnNode.LINE) + " " + in.toString());
        //}
        int op = in.getOpcode();

        boolean isRet = (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) || op == Opcodes.RET;
        //        boolean isLast = !j.hasNext();

        InsnList il = new InsnList();
        if (op == Opcodes.ATHROW) {
          // notify on explicit throws
          notify(cn, mn, il, "throwE");
          insns.insert(in.getPrevious(), il);
        } else if (isRet) {
          // notify on exits
          notify(cn, mn, il, "exit");
          insns.insert(in.getPrevious(), il);
        } else if (exceptionHandlers.contains(in)) {
          // notify on catches
          notify(cn, mn, il, "catchE");
          AbstractInsnNode place = getNextRelevant(j);
          insns.insert(place.getPrevious(), il);          
        }

        // this is an over-approximation -M
        //        stackIncrement = Math.max(stackIncrement, il.size());

      }
      // notify on entry
      InsnList il = new InsnList(); 
      notify(cn, mn, il, "entry");      
      insns.insert(il); 
      mn.maxStack += 10;
    } 
  }

  private AbstractInsnNode getNextRelevant(Iterator<AbstractInsnNode> it) {
    AbstractInsnNode insn = null;
    while (it.hasNext()) {
      insn = it.next();
      if (insn != null && !(insn instanceof LineNumberNode)) {
        break;
      }
    }
    if (insn == null) {
      throw new RuntimeException();
    }
    return insn;
  }

  private void notify(ClassNode cn, MethodNode mn, InsnList il, String event) {
    String fullyQmName = "L" + cn.name + ";" + mn.name + mn.desc;
    il.add(new LdcInsnNode(fullyQmName));
    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "callret/agent/InstrumentationState", event, "(Ljava/lang/String;)V"));
  }

}