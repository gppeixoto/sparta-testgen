package replayer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import replayer.examples.Samples;

public class Main {

  /**
   * state
   */
  static List<String> instructionTrace;
  static Heap heap;
  static OperandStack operandStack;
  static CallStack callStack;

  public static void main(String[] args) {
    instructionTrace = Samples.sample3();
    heap = new Heap(); 
    callStack = new CallStack();
    operandStack = callStack.push("main");

    replay();
  }

  enum OPCODE {bipush, istore, iload, iconst, imul, ret, newarray, dup, iastore, iaload, 
    astore, aload, ldc, getstatic, NEW, invokespecial, putfield, invokestatic};

    private static void replay() {
      for(int i = 0; i < instructionTrace.size(); i++) {
        String insn = instructionTrace.get(i);

        // parsing instruction string
        // TODO: optimize this if inefficient
        String[] splits = insn.split("\\s++|_");
        OPCODE kind = null;
        for (OPCODE k : OPCODE.values()) {
          if (splits[0].startsWith(k.toString())) {
            kind = k;
            break;
          }
        }
        if (kind == null) {
          throw new RuntimeException("Could not find instruction: >" + splits[0] + "<");
        }

        // found kind.  now parse complement if any
        String complementOne = null;
        String complementTwo = null;
        String complementThree = null;
        if (splits.length > 1) {
          complementOne = splits[1].trim();
          if (!complementOne.equals("")) {
            if (complementOne.startsWith("_")) {
              complementOne = complementOne.substring(1);
            }
          }
          if (splits.length > 2) {
            complementTwo = splits[2].trim();
            if (!complementTwo.equals("")) {
              if (complementTwo.startsWith("_")) {
                complementTwo = complementTwo.substring(1);
              }
            }
            if (splits.length > 3) {
              complementThree = splits[3].trim();
              if (!complementThree.equals("")) {
                if (complementThree.startsWith("_")) {
                  complementThree = complementThree.substring(1);
                }
              }
            }
          }
        }
        // done with parsing

        System.out.println(kind);
        boolean isStatic = false;
        // replay instruction
        switch (kind) {
        case bipush:
          operandStack.push(Integer.parseInt(complementOne));
          break;
        case astore:
        case istore:
          operandStack.store(Integer.parseInt(complementOne));
          break;
        case aload:
        case iload:
          operandStack.load(Integer.parseInt(complementOne));
          break;
        case iconst:
          operandStack.push(Integer.parseInt(complementOne));
          break;
        case imul:
          int tmp1 = (Integer) operandStack.pop();
          int tmp2 = (Integer) operandStack.pop();
          operandStack.push(tmp1 * tmp2);
          break;
        case ret:
          operandStack = callStack.pop();
          break;
        case newarray: 
          // ignoring type for now
          operandStack.push(heap.newCell());
          break;
        case dup:
          operandStack.push(operandStack.peek());
          break;
        case iastore:
          Object val = operandStack.pop();
          int index = (Integer) operandStack.pop();
          HeapCell arRef = (HeapCell) operandStack.pop();
          arRef.store(index+"", val);
          break;
        case iaload:
          index = (Integer) operandStack.pop();
          arRef = (HeapCell) operandStack.pop();
          operandStack.push(arRef.load(index+""));
          break;
        case ldc:
          if (!complementTwo.equals("//String")) {
            throw new UnsupportedOperationException("expecting string literal");
          }
          operandStack.push(complementThree);
          break;
        case getstatic:
          if (!complementTwo.equals("//Field")) {
            throw new UnsupportedOperationException("MISSING");
          }
          try {
            String[] tmp = complementThree.split(":");
            String className = tmp[0];
            tmp = className.split("\\.");
            Field f = Class.forName(tmp[0].replace('/', '.')).getField(tmp[1]);
            operandStack.push(f.get(null));
          } catch (Exception e) {
            throw new RuntimeException("check this!");
          }
          break;
        case NEW: 
          if (!complementTwo.equals("//class")) {
            throw new UnsupportedOperationException("expecting class literal");
          }
          operandStack.push(heap.newCell());
          break;
        case invokestatic:
          isStatic = true;
        case invokespecial: 
          if (!complementTwo.equals("//Method")) {
            throw new UnsupportedOperationException("expecting Method literal");
          }
          String[] args = complementThree.split("\\.|:");
          AccessibleObject aobj = Util.lookup(args);
          if (aobj instanceof Method) {
            throw new RuntimeException("missing");
          } else {
            Constructor<?> cons = (Constructor<?>) aobj;
            List<Object> list = new ArrayList<Object>();
            for (int k = 0; k < cons.getParameterTypes().length; k++) {
              list.add(operandStack.pop());
            }
            // this reference
            if (!isStatic) {
              list.add(operandStack.pop());
            }
            if (Modifier.isNative(cons.getModifiers())) {
              // ASSUMING NO RELEVANT MUTATION
            } else {
              operandStack = callStack.push(args[1]);
              for (int j = 0; j < list.size() ; j++) {
                operandStack.store(list.size()-j-1, list.get(j));
              }
            }
          }
          break;
        case putfield: 
          if (!complementTwo.equals("//Field")) {
            throw new UnsupportedOperationException("expecting class literal");
          }
          val = operandStack.pop();
          HeapCell objRef = (HeapCell) operandStack.pop();
          String fieldName = complementThree.substring(complementThree.charAt('.')+1, complementThree.charAt(':'));
          objRef.store(fieldName, val);
          break;
        default:
          throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
        }

      }

    }


}