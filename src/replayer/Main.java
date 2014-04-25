package replayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      System.out.println("execution replayed");
    }
    
  }

  enum OPCODE {BIPUSH, ISTORE, ILOAD, ICONST, 
    RETURN, NEWARRAY, DUP, IASTORE, IALOAD, ASTORE, ALOAD, 
    LDC, GETSTATIC, NEW, INVOKESPECIAL, PUTFIELD, INVOKESTATIC, 
    LINENUMBER, IADD, IRETURN, POP, ISUB, IMUL, IDIV, IREM, 
    INEG, IAND, IOR, ISHL, ISHR, IUSHR, IXOR, LCMP, IF, GOTO, 
    FRAME, ANEWARRAY, AASTORE, PUTSTATIC, GETFIELD, AALOAD};

  public void replay() {
    
    Map<String, Integer> labels = new HashMap<String, Integer>();
    
    for(int i = 0; i < instructionTrace.size(); i++) {
      String insn = instructionTrace.get(i);

      // parsing instruction string
      // TODO: optimize this if inefficient
      String[] splits = insn.split("\\s++|_");
      OPCODE kind = null;
      
      //TODO: optimize this
      for (OPCODE opcode : OPCODE.values()) {
        if (opcode.toString().equals(splits[0])) {
          kind = opcode;
          break;
        }
      }
      if (kind == null) {
        if (splits[0].trim().matches("L\\d*")) {
          labels.put(splits[0], i);
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
      
      case ASTORE:
      case ISTORE:
        operandStack.store(Integer.parseInt(complementOne));
        break;
      
      case ALOAD:
      case ILOAD:
        operandStack.load(Integer.parseInt(complementOne));
        break;
      
      case ICONST:
        operandStack.push(Integer.parseInt(complementOne));
        break;
      
      case RETURN:
        operandStack = callStack.pop();
        break;
      
      case ANEWARRAY:
      case NEWARRAY:
        //TODO: ignoring count.  this will be important to reproduce out-of-bounds exceptions
        operandStack.pop();
        // ignoring type for now
        operandStack.push(heap.newCell());
        break;
      
      case DUP:
        operandStack.push(operandStack.peek());
        break;
      
      case AASTORE:
      case IASTORE:
        Object val = operandStack.pop();
        int index = (Integer) operandStack.pop();
        HeapCell arRef = (HeapCell) operandStack.pop();
        arRef.store(index+"", val);
        break;
        
      case AALOAD:
      case IALOAD:
        index = (Integer) operandStack.pop();
        arRef = (HeapCell) operandStack.pop();
        operandStack.push(arRef.load(index+""));
        break;
      
      case LDC:
        operandStack.push(complementOne);
        break;
      
      case GETSTATIC:
        try {
          int idx = complementOne.lastIndexOf(".");
          String clazz = complementOne.substring(0, idx).replace('/', '.');
          String field = complementOne.substring(idx+1);
          Field f = Class.forName(clazz).getDeclaredField(field);
          f.setAccessible(true);
          operandStack.push(f.get(null));
        } catch (Exception e) {
          throw new RuntimeException("check this!");
        }
        break;
      
      case PUTSTATIC:
        try {
          int idx = complementOne.lastIndexOf(".");
          String clazz = complementOne.substring(0, idx).replace('/', '.');
          String field = complementOne.substring(idx+1);
          Field f = Class.forName(clazz).getDeclaredField(field);
          f.setAccessible(true);
          f.set(null, operandStack.pop());
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("check this!");
        }
        break;
      
      case NEW: 
        operandStack.push(heap.newCell());
        break;
      
      case INVOKESTATIC:
        isStatic = true;
      
      case INVOKESPECIAL: 
        int idx = complementOne.lastIndexOf('.');
        String cName = complementOne.substring(0, idx);
        String mName = complementOne.substring(idx+1);
        String[] args = new String[]{cName, mName, complementTwo};
        AccessibleObject aobj = Util.lookup(args);
        
        int numParams; 
        boolean skip = false;
        if (aobj instanceof Method) {
          Method meth = (Method) aobj;
          numParams = meth.getParameterTypes().length;
          skip = Modifier.isNative(meth.getModifiers());
        } else {
          Constructor<?> cons = (Constructor<?>) aobj;
          numParams = cons.getParameterTypes().length;
          skip = Modifier.isNative(cons.getModifiers());
          if (!skip && cons.getDeclaringClass()==Object.class && mName.equals("<init>")) {
            skip = true;
          }
        }
        
        List<Object> list = new ArrayList<Object>();
        for (int k = 0; k < numParams; k++) {
          list.add(operandStack.pop());
        }
        // this reference
        if (!isStatic) {
          list.add(operandStack.pop());
        }
        if (skip) {
          // ASSUMING NO RELEVANT MUTATION
        } else {
          operandStack = callStack.push(cName + mName);
          for (int j = 0; j < list.size() ; j++) {
            operandStack.store(list.size()-j-1, list.get(j));
          }
        }
        break;
        
      case PUTFIELD: 
        val = operandStack.pop();
        HeapCell objRef = (HeapCell) operandStack.pop();
        String fieldName = complementOne.substring(complementOne.lastIndexOf(".")+1);
        objRef.store(fieldName, val);
        break;
        
      case GETFIELD: 
        objRef = (HeapCell) operandStack.pop();
        fieldName = complementOne.substring(complementOne.lastIndexOf(".")+1);
        operandStack.push(objRef.load(fieldName));
        break;
        
      case LINENUMBER:
        labels.put(complementTwo, i);
        break;
        
      case IADD: 
        int val1 = (Integer) operandStack.pop();
        int val2 = (Integer) operandStack.pop();
        operandStack.push(val1 + val2);
        break;

      case ISUB: 
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 - val2);
        break;
        
      case IMUL:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 * val2);
        break;
        
      case IDIV:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 / val2);
        break;
        
      case IREM:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 % val2);
        break;
        
      case ISHL:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 << val2);
        break;
        
      case ISHR: 
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 >> val2);
        break;
        
      case IUSHR:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push(val1 >>> val2);
        break;
        
      case IAND:
        Boolean bol1 = (Boolean) operandStack.pop();
        Boolean bol2 = (Boolean) operandStack.pop();
        operandStack.push(bol1 & bol2);
        break;
        
      case IOR:
        bol1 = (Boolean) operandStack.pop();
        bol2 = (Boolean) operandStack.pop();
        operandStack.push(bol1 | bol2);
        break;
        
      case IXOR: 
        bol1 = (Boolean) operandStack.pop();
        bol2 = (Boolean) operandStack.pop();
        operandStack.push(bol1 ^ bol2);
        break;
      
      case LCMP:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();
        operandStack.push( val1 == val2 ? 0 : (val1 < val2 ? -1 : 1));
        
      case IRETURN:
        val1 = (Integer) operandStack.pop();
        operandStack = callStack.pop();
        operandStack.push(val1);
        break;
        
      case INEG:
        val1 = (Integer) operandStack.pop();
        operandStack = callStack.pop();
        operandStack.push(-val1);
        break;
        
      case POP:
        operandStack.pop();
        break;
        
      case IF:
        val1 = (Integer) operandStack.pop();
        val2 = (Integer) operandStack.pop();

        String op = complementOne;
        boolean shouldJump;
        if (op.equals("ICMPGT")) {
          shouldJump = val2 > val1; 
        } else if (op.equals("ICMPLT")) {
          shouldJump = val2 < val1;
        } else if (op.equals("ICMPGE")) {
          shouldJump = val2 >= val1;
        } else if (op.equals("ICMPNE")) {
          shouldJump = val2 != val1;
        } else if (op.equals("ICMPLE")) {
          shouldJump = val2 <= val1;
        } else {
          throw new UnsupportedOperationException("IF >" + op + "<");
        }
        
        if (shouldJump) { // reset program counter
          i = lookupForLabel(labels, complementTwo, i);
        }
        break;
        
      case GOTO:
        i = lookupForLabel(labels, complementOne, i);
       break;
        
      case FRAME:
        // ignore ASM-generated instruction
        break;
       
      default:
        throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
      }

    }

  }

  private int lookupForLabel(Map<String, Integer> labels, String jumpLabel, int k) {
    Integer res = labels.get(jumpLabel);
    if (res != null) {
      return res;
    }      
    for (; k < instructionTrace.size(); k++) {
      String tmp = instructionTrace.get(k).trim();
      if (tmp.matches("L\\d*")) {
        labels.put(tmp, k);
        if (tmp.equals(jumpLabel)) {
          res = k;
          break;  
        }
      } else if (tmp.startsWith("LINENUMBER")) {
        String lab = tmp.substring(tmp.lastIndexOf(" ")).trim();
        labels.put(lab, k);
        if (lab.equals(jumpLabel)) {
          res = k;
          break;
        }
      }
    }
    if (res == null) {
      throw new RuntimeException("error");
    }
    return res;
  }


}