package replayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Main {

  /**
   * state
   */
  List<String> instructionTrace;
  Heap heap;
  OperandStack operandStack;
  CallStack callStack;
  
  public Main(List<String> input) {
    instructionTrace = input;
    
    heap = new Heap(); 
    callStack = new CallStack();
    operandStack = callStack.push("main");

    replay();
  }

  public static void main(String[] args) throws Exception {
    
    // read trace
    List<String> buffer = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new FileReader("trace.out"));
    String s;
    boolean log = false;
    while ((s = br.readLine()) != null) {
      if (!log) {
        if (s.contains("Method.invoke")) {
          log = true;
        }
        continue;
      } 
      
      buffer.add(s.trim());
    }
    br.close();   
    
    // replay
    try {
      (new Main(buffer)).replay();
    } catch (FinishedExecutionException _) {
      System.out.println("done replayed execution");
    }
    
  }

  //TODO: please revise all instructions in lowercase -Marcelo
  enum OPCODE {BIPUSH, ISTORE, ILOAD, ICONST, imul, 
    RETURN, newarray, dup, iastore, iaload, astore, aload, 
    ldc, getstatic, NEW, invokespecial, putfield, INVOKESTATIC, 
    LINENUMBER, IADD, IRETURN, POP};

  public void replay() {
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
        if (splits[0].trim().matches("L\\d*")) {
          continue; // skip
        }
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

      boolean isStatic = false;
      // replay instruction
      switch (kind) {
      case BIPUSH:
        operandStack.push(Integer.parseInt(complementOne));
        break;
      case astore:
      case ISTORE:
        operandStack.store(Integer.parseInt(complementOne));
        break;
      case aload:
      case ILOAD:
        operandStack.load(Integer.parseInt(complementOne));
        break;
      case ICONST:
        operandStack.push(Integer.parseInt(complementOne));
        break;
      case imul:
        int tmp1 = (Integer) operandStack.pop();
        int tmp2 = (Integer) operandStack.pop();
        operandStack.push(tmp1 * tmp2);
        break;
      case RETURN:
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
      case INVOKESTATIC:
        isStatic = true;
      case invokespecial: 
        int idx = complementOne.lastIndexOf('.');
        String cName = complementOne.substring(0, idx);
        String mName = complementOne.substring(idx+1);
        String[] args = new String[]{cName, mName, complementTwo};
        AccessibleObject aobj = Util.lookup(args);
        
        int numParams; int mod;
        if (aobj instanceof Method) {
          Method meth = (Method) aobj;
          numParams = meth.getParameterTypes().length;
          mod = meth.getModifiers();
        } else {
          Constructor<?> cons = (Constructor<?>) aobj;
          numParams = cons.getParameterTypes().length;
          mod = cons.getModifiers();
        }
        
        List<Object> list = new ArrayList<Object>();
        for (int k = 0; k < numParams; k++) {
          list.add(operandStack.pop());
        }
        // this reference
        if (!isStatic) {
          list.add(operandStack.pop());
        }
        if (Modifier.isNative(mod)) {
          // ASSUMING NO RELEVANT MUTATION
        } else {
          operandStack = callStack.push(cName + mName);
          for (int j = 0; j < list.size() ; j++) {
            operandStack.store(list.size()-j-1, list.get(j));
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
        
      case LINENUMBER:
        break;
        
      case IADD: 
        int val1 = (Integer) operandStack.pop();
        int val2 = (Integer) operandStack.pop();
        operandStack.push(val1 + val2);
        break;
        
      case IRETURN:
        val1 = (Integer) operandStack.pop();
        operandStack = callStack.pop();
        operandStack.push(val1);
        break;
        
        
      case POP:
        operandStack.pop();
        break;
        
      default:
        throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
      }

    }

  }


}