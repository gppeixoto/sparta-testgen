package replayer;


import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
  StaticArea sa;

  public Main(List<String> input) {
    instructionTrace = input;

    heap = new Heap(); 
    callStack = new CallStack();
    operandStack = callStack.push("main");
    sa = new StaticArea();

  }

  public static void main(String[] args) throws Exception {

    // read trace
    List<String> buffer = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new FileReader("trace.out"));
    String s;
    while ((s = br.readLine()) != null) {
      buffer.add(s.trim());
    }
    br.close();   

    // replay
    try {
      (new Main(buffer)).replay();
    } catch (FinishedExecutionException _) {
      System.out.println("  execution replayed");
    }

  }

  enum OPCODE {ACONST, ACONST_NULL, ARETURN, ARRAYLENGTH, BALOAD, IFNULL,
    IINC, INVOKEDYNAMIC, BIPUSH, ISTORE, ILOAD, ICONST, 
    RETURN, NEWARRAY, DUP, IASTORE, IALOAD, ASTORE, ALOAD, 
    LDC, GETSTATIC, NEW, INVOKESPECIAL, PUTFIELD, INVOKESTATIC, 
    LINENUMBER, IADD, IRETURN, POP, ISUB, IMUL, IDIV, IREM, 
    INEG, IAND, IOR, ISHL, ISHR, IUSHR, IXOR, LCMP, IF, GOTO, 
    FRAME, ANEWARRAY, AASTORE, PUTSTATIC, GETFIELD, AALOAD, SIPUSH,
    DSTORE, DLOAD, DMUL, DADD, DDIV, DSUB, LOOKUPSWITCH, INVOKEVIRTUAL, 
    D2I, D2L, DALOAD, DASTORE, L2F, L2I, LADD, LALOAD,
    BASTORE, CASTORE, CALOAD, D2F, L2D, INVOKEINTERFACE,
    DCMPG, DCMPL, DCONST, DNEG, LAND, LCONST, LASTORE,
    DREM, DRETURN, ldc_w, ldc2_w, ldiv, LLOAD, DUP2, F2I, F2L, FADD, FALOAD,
    LMUL, LNEG, LOR, LREM, FASTORE, FCMPG, FCMPL, FCONST, LRETURN, LSHL, LSHR,
    FDIV, FSTORE, LSTORE, FLOAD, FMUL, FNEG, FREM, FRETURN, FSUB, LSUB, LUSHR, NOP, POP2, RET, GOTO_W, SASTORE, SALOAD, SWAP, I2D, I2C, I2B, I2F, I2L, I2S, IFGT, IFLE,
  };

  /**
   * ACONST_NULL - nosso  (feito)
   * ARETURN - nosso  (feito)
   * ARRAYLENGTH - nosso (feito?)
   * ATHROW
   * BALOAD - nosso (feito?)
   * BASTORE
   * BREAKPOINT
   * CALOAD
   * CASTORE
   * CHECKCAST
   * D2F
   * D2I
   * D2L
   * DALOAD
   * DASTORE
   * DCMPG
   * DCMPL
   * DCONST
   * DNEG
   * DREM
   * DRETURN
   * DUP_X1
   * DUP_X2
   * DUP2
   * DUP2_X1
   * DUP2_X2
   * F2D
   * F2I
   * F2L
   * FADD
   * FALOAD
   * FASTORE
   * FCMPG
   * FCMPL
   * FCONST
   * FDIV
   * FLOAD
   * FMUL
   * FNEG
   * FREM
   * FRETURN
   * FSTORE
   * FSUB
   * ... // CONTINUE AFTER F
   * 
   * ifnull - nosso (feito)
   * iinc - nosso (feito)
   * invokedynamic - nosso
   */

  public OPCODE lookupOpcode(String str) {   
    OPCODE kind = null;
    for (OPCODE opcode : OPCODE.values()) {
      if (opcode.toString().equals(str)) {
        kind = opcode;
        break;
      }
    }
    return kind;
  }
  
  public void replay() {

    /**
     * Mapping from position labels (which can be referenced
     * from control-flow instructions) to positions in 
     * the list of instructions (instructionTrace)  
     */
    Map<String, Integer> labels = new HashMap<String, Integer>();

    /**
     * look across all instructions in the trace
     */
    for(int i = 0; i < instructionTrace.size(); i++) {
      
      String insn = instructionTrace.get(i);
      
      String[] nameIns = insn.split(":"); 
      insn = nameIns[1].trim();
      String[] splits = insn.split("\\s++|_");

      /**
       * lookup kind of instruction, raise 
       * error message if kind not 
       */
      OPCODE kind = lookupOpcode(splits[0]);
      if (kind == null) {
        if (splits[0].trim().matches("L\\d*")) {
          labels.put(splits[0], i);
          continue; // skip instruction
        }
        throw new RuntimeException("Could not find instruction: >" + splits[0] + "<");
      }

      /**
       * update complements of instructions
       */
      System.out.println(Arrays.toString(splits));
      String[] complements = extractComplements(splits);
      String complementOne = complements[0];
      String complementTwo = complements[1];
      System.out.println("Virou " + complementOne + " e " + complementTwo);

      boolean isStatic = false;

      /**
       * decide which instruction to apply
       */
      switch (kind) {

      case ACONST: //extra case to simplify the '_'-handling code
      case ACONST_NULL:
          operandStack.push(null);
         break;
        
      case ARETURN:
          Object objectref = operandStack.pop();
          operandStack = callStack.pop();
          operandStack.push(objectref);
          break;
      
      case ARRAYLENGTH: //TODO: MAB - WRONG!
        // The arrayref must be of type reference and must refer to an array. It is popped from the operand stack.
        HeapCell arrayref = (HeapCell) operandStack.pop();
        // The length of the array it references is determined. That length is pushed onto the operand stack as an int.
        operandStack.push(arrayref.getMapSize());
        // Para isso, criamos o metodo getMapSize() em HeapCell para acessar o tamanho do Map, pois este eh privado.
        break;
      
      case BALOAD:
        //The arrayref must be of type reference and must refer to an array
        //whose components are of type byte or of type boolean. 
        
        //The index must be of type int. 
        //Both arrayref and index are popped from the operand stack
        int indice = (Integer) operandStack.pop();
        HeapCell arrayRef = (HeapCell) operandStack.pop();
        Object objetoCarregado = (Object) arrayRef.load(indice+"");
        int inteiroCarregar = 0;
        
        //The byte value in the component of the array at index is retrieved,
        //sign-extended to an int value, and pushed onto the top of the operand stack.
        
        // Caso o objeto seja um byte
        if (objetoCarregado instanceof Byte) inteiroCarregar = ((Byte)objetoCarregado).intValue();
        // Caso seja um boolean
        else if (objetoCarregado instanceof Boolean){
          if (((Boolean)objetoCarregado).booleanValue()) inteiroCarregar = 1;
          else inteiroCarregar = 0;
        } 
        
        // Colocando na operandStack
        operandStack.push(inteiroCarregar);
        break;
        
      case IFNULL:
        Object valor = operandStack.pop();
        if (valor==null) {
          i = lookupForLabel(labels, complementTwo, i);
        }
        break;
     
      case IINC:
        operandStack.load(Integer.parseInt(complementOne)); // coloca o valor da variavel em index na pilha
        int valorVariavel = (Integer) operandStack.pop(); // recupera o valor da variavel
        int valorConstante = Integer.parseInt(complementTwo); // valor a ser incrementado 
        int resultado = valorVariavel + valorConstante; // resultado a ser salvo
        operandStack.push(resultado); // coloca o resultado na pilha
        operandStack.store(Integer.parseInt(complementOne)); // coloca o resultado colocado na pilha na variavel
        
        break;
      
      case BIPUSH:
        operandStack.push(Integer.parseInt(complementOne));
        break;

      case DSTORE:
      case ASTORE:
      case ISTORE:
        operandStack.store(Integer.parseInt(complementOne));
        break;

      case DLOAD:
      case ALOAD:
      case ILOAD:
        operandStack.load(Integer.parseInt(complementOne));
        break;
        
      case LLOAD:
        operandStack.load((int) Long.parseLong(complementOne));
        break;  

      case ICONST:
        boolean neg = false;
        if (complementOne.charAt(0)=='M') {
          complementOne = complementOne.substring(1);
          neg = true;
        }
        int tmp = Integer.parseInt(complementOne);
        operandStack.push(neg?-tmp:tmp);
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

      case LAND:
        long op1 = (Long) operandStack.pop();
        long op2 = (Long) operandStack.pop();
        operandStack.push(op1&op2);
        break;
              
      case LCONST:
        long constant = Long.parseLong(complementOne);
        operandStack.push(constant);
        break;
              
      case DCMPG:
      case DCMPL:
              
        double valor1 = (Double) operandStack.pop();
        double valor2 = (Double) operandStack.pop();
              
        switch(kind){
            case DCMPG:
                if(valor1 == Double.NaN || valor2 == Double.NaN || valor1 > valor2)
                    operandStack.push(1);
                else if(valor1 == valor2)
                    operandStack.push(0);
                else
                    operandStack.push(-1);
                break;
            case DCMPL:
                if(valor1 == Double.NaN || valor2 == Double.NaN || valor1 < valor2)
                    operandStack.push(-1);
                else if(valor1 == valor2)
                    operandStack.push(0);
                else
                    operandStack.push(1);
                break;
            default:
                 throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
        }
      
        break;
              
      case DCONST:
        double constante = Double.parseDouble(complementOne);
        operandStack.push(constante);
        break;
              
      case DNEG:
        double to_neg = (Double) operandStack.pop();
        operandStack.push((-1.0)*to_neg);
        break;
        
      case DUP:
        if (complementOne != null) {
          Object operand1;
          Object operand2;
          
          if (complementOne.equals("X1")) {
            operand1 = operandStack.pop();
            operand2 = operandStack.pop();
            
            operandStack.push(operand1);
            operandStack.push(operand2);
            operandStack.push(operand1);

          } else if (complementOne.equals("X2")) {
            operand1 = operandStack.pop();
            operand2 = operandStack.pop();
            
            if ((!(operand1 instanceof Double) || !(operand1 instanceof Long)) && ((operand2 instanceof Double) || (operand2 instanceof Long))) {
              operandStack.push(operand1);
              operandStack.push(operand2);
              operandStack.push(operand1);
            } else {
              Object operand3 = operandStack.pop();
              operandStack.push(operand1);
              operandStack.push(operand3);
              operandStack.push(operand2);
              operandStack.push(operand1);
            }
          } else {
            throw new RuntimeException("Unknown case");
          }
        } else {
          operandStack.push(operandStack.peek());
        }
        break;

      case DUP2:
        if (complementOne != null) {
          if (complementOne.equals("X1")) {
            if((operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
              //Categoria 2
              Object value1 = operandStack.pop();
              if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                  Object value2 = operandStack.pop();
                  operandStack.push(value1);
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }else{
              //Categoria 1
              Object value1 = operandStack.pop();
              if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                  Object value2 = operandStack.pop();
                  if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                      Object value3 = operandStack.pop();
                      operandStack.push(value2);
                      operandStack.push(value1);
                      operandStack.push(value3);
                  }
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }
          } else if (complementOne.equals("X2")) {
            if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
              Object value1 = operandStack.pop();
              if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                  Object value2 = operandStack.pop();
                  if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                      //Form 1
                      Object value3 = operandStack.pop();
                      if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                          Object value4 = operandStack.pop();
                          operandStack.push(value2);
                          operandStack.push(value1);
                          operandStack.push(value4);
                      }
                      operandStack.push(value3);
                  }else{
                      //Form 3
                      Object value3 = operandStack.pop();
                      operandStack.push(value2);
                      operandStack.push(value1);
                      operandStack.push(value3);
                  }
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }else{
              Object value1 = operandStack.pop();
              if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                  //Form 2
                  Object value2 = operandStack.pop();
                  if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
                      Object value3 = operandStack.pop();
                      operandStack.push(value1);
                      operandStack.push(value3);
                  }
                  operandStack.push(value2);
              }else{
                  //Form 4
                  Object value2 = operandStack.pop();
                  operandStack.push(value1);
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }
          } else {
            throw new RuntimeException("Unknown complement");
          }
        } else {
          if (!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)) {
            // Categoria 1
            Object value1 = operandStack.pop();
            Object value2 = operandStack.peek();
            if (!(value2 instanceof Double || value2 instanceof Long)) {
              operandStack.push(value1);
              operandStack.push(value2);
            }
            operandStack.push(value1);
          } else {
            // Categoria 2
            operandStack.push(operandStack.peek());
          }
        }
        break;
        
      case AASTORE:
      case IASTORE:
      case BASTORE:
      case CASTORE:
      case DASTORE:
      case LASTORE:
        Object val = operandStack.pop();
        int index = (Integer) operandStack.pop();
        HeapCell arRef = (HeapCell) operandStack.pop();
        arRef.store(index+"", val);
        break;

      case AALOAD:
      case CALOAD:
      case IALOAD:
      case LALOAD:
      case DALOAD:
      case FALOAD:
        index = (Integer) operandStack.pop();
        arRef = (HeapCell) operandStack.pop();
        operandStack.push(arRef.load(index+""));
        break;
        
      case D2I:
        double var_d2i1 = (Double) operandStack.pop();
        if (Double.isNaN(var_d2i1)){
          operandStack.push((Integer) 0);
        } else if (var_d2i1 == Double.POSITIVE_INFINITY){
          operandStack.push(Integer.MAX_VALUE);
        } else if (var_d2i1 == Double.NEGATIVE_INFINITY){
          operandStack.push(Integer.MIN_VALUE);
        } else {
          operandStack.push((var_d2i1 < 0) ? ((int) Math.ceil(var_d2i1)) : ((int) Math.floor(var_d2i1)));
        }
        break;
      
      case D2L:
        double var_d2l = (Double) operandStack.pop();
        if (Double.isNaN(var_d2l)){
          operandStack.push(new Long(0));
        } else if (var_d2l == Double.POSITIVE_INFINITY){
          operandStack.push(Long.MAX_VALUE);
        } else if (var_d2l == Double.NEGATIVE_INFINITY){
          operandStack.push(Long.MIN_VALUE);
        } else {
          operandStack.push((var_d2l < 0) ? (new Double(Math.ceil(var_d2l)).longValue()) : (new Double(Math.floor(var_d2l))).longValue());
        }
        break;
      
      case L2F:
        long var_l2f = (Long) operandStack.pop();
        operandStack.push((float) var_l2f);
        break;
      
      case L2I:
        long var_l2i = (Long) operandStack.pop();
        String str_l2i = Long.toBinaryString(var_l2i);
        str_l2i = str_l2i.substring(str_l2i.length()-33>=0?str_l2i.length()-33:0);
        int aux_l2i = 1;
        int ret_l2i = 0;
        for (int count=str_l2i.length()-1; count>=0; --count){
            if (str_l2i.charAt(count) == '1') {
                ret_l2i += aux_l2i;
            }
            aux_l2i *= 2;
        }
        operandStack.push(ret_l2i);
        break;
        
        
      case DREM:
      {
          double d2 = (Double) operandStack.pop(); 
          double d1 = (Double) operandStack.pop();
          double res;
          if(d1==Double.NaN || d2==Double.NaN)
            res = Double.NaN;
          else if(Double.isInfinite(d1) || d2 == 0)
            res = Double.NaN;
          else if(Double.isInfinite(d2))
            res = d1;
          else
            res = (Double) (d1-(d2*((int)(d1/d2))));
          operandStack.push(res);
      }
          break;      
          
      case DRETURN:
      {
        double d1 = (Double) operandStack.pop();
        operandStack = callStack.pop();
        operandStack.push(d1);
      }
          break;
          
      case ldc_w: //TODO MAB: wrong - pode receber strings tambem!
          try {
            //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
            //int k = Integer.parseInt(complementOne+complementTwo);
            
            //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
            //int k = bin2int(complementOne+complementTwo);
            
            //se receber ints:
            int k1 = Integer.parseInt(complementOne);
            int k2 = Integer.parseInt(complementTwo);
            int k = k1*(2^8) + k2;
            //fim
            
            operandStack.push(k);
          } catch(NumberFormatException _) {
            try {
              //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
              //Float k = Float.parseFloat(complementOne+complementTwo);
              
              //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
              //Float k = bin2float(complementOne+complementTwo);
              
              //se receber ints:
              float k1 = Float.parseFloat(complementOne);
              float k2 = Float.parseFloat(complementTwo);
              float k = k1*(2^8) + k2;
              //fim
              operandStack.push(k);
            } catch(NumberFormatException __) {
              operandStack.push(complementOne+complementTwo);
            } 
          }
          break;
  
      case ldc2_w:
          try {
            //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
            //Long k = Long.parseLong(complementOne+complementTwo);
            
            //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
            //Long k = bin2Long(complementOne+complementTwo);
           
            //se receber ints:
            long k1 = Long.parseLong(complementOne);
            long k2 = Long.parseLong(complementTwo);
            long k = k1*(2^8) + k2;
            //fim
            
            operandStack.push(k);
          } catch(NumberFormatException _) {
            try {
              //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
              //double k = Double.parseDouble(complementOne+complementTwo);
              
              //se receber a cadeia de bits, e o parseInt já transforma em inteiro:
              //double k = bin2doub(complementOne+complementTwo);
              
              //se receber ints:
              double k1 = Double.parseDouble(complementOne);
              double k2 = Double.parseDouble(complementTwo);
              double k = k1*(2^8) + k2;
              //fim
              operandStack.push(k);
            } catch(NumberFormatException __){
            
            }
          }
          break;
      
      case ldiv:
         long valB = (Long) operandStack.pop();
         long valA = (Long) operandStack.pop();
         
         if(valB==0)
           throw (new RuntimeException("Arithmetic Exception"));
         else if(valB==-1 && valA==Long.MAX_VALUE)
         {
           operandStack.push(valA);
         }
         else
         {
           operandStack.push(valA/valB); 
         }
          break;    
      
      case LADD:
        long myTmp_ladd;
        long var_ladd1 = (Long) operandStack.pop();
        long var_ladd2 = (Long) operandStack.pop();
        
        myTmp_ladd = var_ladd1 + var_ladd2;
        operandStack.push(myTmp_ladd);
        break;
        
      case LDC:
        try {
          int k = Integer.parseInt(complementOne);
          operandStack.push(k);
        } catch(NumberFormatException _) {
          try {
            long k = Long.parseLong(complementOne);
            operandStack.push(k);
          } catch(NumberFormatException __) {
            try {
              double k = Double.parseDouble(complementOne);
              operandStack.push(k);
            } catch (NumberFormatException ___) {
              operandStack.push(complementOne);
            }
          } 
        }
        break;

      case GETSTATIC:
        try {
          int idx = complementOne.lastIndexOf(".");
          String clazz = complementOne.substring(0, idx).replace('/', '.');
          String fieldName = complementOne.substring(idx+1);
          operandStack.push(sa.getStatic(Class.forName(clazz), fieldName));
        } catch (Exception e) {
          throw new RuntimeException("check this!");
        }
        break;

      case PUTSTATIC:
        try {
          int idx = complementOne.lastIndexOf(".");
          String clazz = complementOne.substring(0, idx).replace('/', '.');
          String fieldName = complementOne.substring(idx+1);
          sa.putStatic(Class.forName(clazz), fieldName, operandStack.pop());
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("check this!");
        }
        break;

      case NEW: 
        operandStack.push(heap.newCell());
        break;
        
      case INVOKEDYNAMIC:
        int idxDyn = complementOne.lastIndexOf('.');
        String cNameDyn = complementOne.substring(0, idxDyn);
        String mNameDyn = complementOne.substring(idxDyn+1);
        String[] argsDyn = new String[]{cNameDyn, mNameDyn, complementTwo};
        AccessibleObject aObj = Util.lookup(argsDyn);
        
        Method metodo = (Method) aObj;
        int numParametros = metodo.getParameterTypes().length;
        
        
        List<Object> listParametros = new ArrayList<Object>();
        for (int k = 0; k < numParametros; k++) {
          listParametros.add(operandStack.pop());
        }
        
        Object methodType = operandStack.pop();
        Object methodName = operandStack.pop();
        Object methodLookup = operandStack.pop();
        Object methodHandle = operandStack.pop();
        
        listParametros.add(methodType);
        listParametros.add(methodName);
        listParametros.add(methodLookup);
        listParametros.add(methodHandle);
        
        operandStack = callStack.push(cNameDyn + mNameDyn);
        for (int j = 0 ; j < (int) listParametros.size(); ++j){
          operandStack.store(listParametros.size()-j-1, listParametros.get(j));
        }
      break;
        

      case INVOKESTATIC:
        isStatic = true;

      case INVOKEVIRTUAL:
      case INVOKEINTERFACE:
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
          Class<?> clazz = cons.getDeclaringClass();
          if (!skip && (clazz==Object.class || clazz==Enum.class) && mName.equals("<init>")) {
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

      case DADD:
      case DSUB: 
      case DMUL:
      case DDIV:

        double d1 = (Double) operandStack.pop();
        double d2 = (Double) operandStack.pop();

        double res;
        switch(kind) {
        case DADD:
          res = d1 + d2;
          break;
        case DSUB: 
          res = d1 - d2;
          break;
        case DMUL:
          res = d1 * d2;
          break;
        case DDIV:
          res = d1 / d2;
          break;
        default:
          throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
        }

        operandStack.push(res);
        break;


      case IADD:
      case ISUB: 
      case IMUL:
      case IDIV:
      case IREM:
      case ISHL:
      case ISHR:
      case IUSHR:
      case IAND:
      case IOR:
      case IXOR:

        int val1 = (Integer) operandStack.pop();
        int val2 = (Integer) operandStack.pop();

        switch(kind) {
        case IADD:
          tmp = val1 + val2;
          break;
        case ISUB: 
          tmp = val1 - val2;
          break;
        case IMUL:
          tmp = val1 * val2;
          break;
        case IDIV:
          tmp = val1 / val2;
          break;
        case IREM:
          tmp = val1 % val2;
          break;
        case ISHL:
          tmp = val1 << val2;
          break;
        case ISHR:
          tmp = val1 >> val2;
          break;
        case IUSHR:
          tmp = val1 >>> val2;
        break;
        case IAND: 
          tmp = val1 & val2;
          break;
        case IOR:
          tmp = val1 | val2;
          break;
        case IXOR:
          tmp = val1 ^ val2;
          break;
        default:
          throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
        }

        operandStack.push(tmp);
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
        
      case F2I:

        int result = 0;
        float value1 = (Float) operandStack.pop();
        if (value1 < 0) {
          result = (int) value1;
        } else if (value1 > 0) {
          result = (int) Math.ceil(value1);
        } else {
          result = 0;
        }
        operandStack.push(result);

        break;

      case F2L:

        long resultLong = 0;
        float valueFloat = (Float) operandStack.pop();
        if (valueFloat < 0) {
          resultLong = (long) valueFloat;
        } else if (valueFloat > 0) {
          resultLong = (long) Math.ceil(valueFloat);
        } else {
          resultLong = 0;
        }
        operandStack.push(resultLong);

        break;

      case FADD:

        float float1 = (Float) operandStack.pop();
        float float2 = (Float) operandStack.pop();
        float floatResult = float1 + float2;
        operandStack.push(floatResult);

        break;

      case LMUL:
        long long1 = (Long) operandStack.pop();
        long long2 = (Long) operandStack.pop();
        long longResult = long1 * long2;
        operandStack.push(longResult);

        break;

      case LNEG:
        long1 = (Long) operandStack.pop();
        longResult = 0 - long1;
        operandStack.push(longResult);

        break;

      case LOR:
        long1 = (Long) operandStack.pop();
        long2 = (Long) operandStack.pop();
        longResult = long1 | long2;
        operandStack.push(longResult);

        break;

      case LREM:
        long2 = (Long) operandStack.pop();
        long1 = (Long) operandStack.pop();
        longResult = long1 - (long1 / long2) * long2;
        operandStack.push(longResult);

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
        } else if(op.equals("ACMPEQ")){
          shouldJump = val2 == val1;
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

      case SIPUSH:
        // representing as integer (ignoring overflows)
        operandStack.push(Integer.parseInt(complementOne));
        break;

      case LOOKUPSWITCH:

        val1 = (Integer) operandStack.pop();
        String[] parts = insn.substring(OPCODE.LOOKUPSWITCH.name().length()+1).trim().split("  ");
        String gotoLabel = null;
        for (String part : parts) {
          if (!part.trim().equals("")) {
            String[] keyval = part.split(":");
            String key = keyval[0].trim();
            if (key.equals("default") || val1 == Integer.parseInt(key)) {
              gotoLabel = keyval[1].trim();
              break;
            }
          }
        }
        if (gotoLabel == null) {
          throw new RuntimeException("should not happen!");
        }
        i = lookupForLabel(labels, gotoLabel, i);
        break;
        
      case D2F:
        double value = (Double) operandStack.pop();
        operandStack.push((float) value);
        break;
        
      case L2D:
        Object value_o = null;
        try {
          value_o = operandStack.pop();
          long value_long = (Long) value_o;
          operandStack.push((double) value_long); 
        } catch (ClassCastException _) {
          int value_int = (Integer) value_o;
          operandStack.push((double) value_int);
        }
        break;

      // ----------------------------------------------mra2--->
      case FASTORE:
        Object val_f = operandStack.pop();
        int index_f = (Integer) operandStack.pop();
        HeapCell arRef_f = (HeapCell) operandStack.pop();
        arRef_f.store(index_f + "", val_f);
        break;
        
      case FCMPG:
        float val_g = (Float) operandStack.pop();
        float val2_g = (Float) operandStack.pop();

        if (val_g > val2_g) {
          operandStack.push(1);
        } else if (val_g == val2_g) {
          operandStack.push(0);
        } else {
          operandStack.push(-1);
        }
        break;
        
      case FCMPL:
        float val_l = (Float) ((Double) operandStack.pop()).floatValue();
        float val2_l = (Float) ((Double) operandStack.pop()).floatValue();

        if (val_l > val2_l) {
          operandStack.push(1);
        } else if (val_l == val2_l) {
          operandStack.push(0);
        } else {
          operandStack.push(-1);
        }
        break;
        
      case FCONST:
        float tmp3 = Float.parseFloat(complementOne);
        operandStack.push(tmp3);
        break;

      case LRETURN:
        long val3 = ((Integer) operandStack.pop()).longValue();
        operandStack = callStack.pop();
        operandStack.push(val3);
        break;

      case LSHL:
      case LSHR:

        int val5 = ((Integer) operandStack.pop()).intValue();
        long val4 = ((Integer) operandStack.pop()).longValue();
        long resp2;

        switch (kind) {
        case LSHL:
          resp2 = val4 << val5;
          break;
        case LSHR:
          resp2 = val4 >> val5;
          break;
        default:
          throw new RuntimeException(
              "Interpretation of Instruction undefined: " + kind);
        }

        operandStack.push(resp2);
        break;

      case FDIV:
        float f1 = (Float) operandStack.pop();
        float f2 = (Float) operandStack.pop();
        float resp = f1 / f2;
        operandStack.push(new Float(resp)); 
        break;

      case LSTORE:
        operandStack.store(Integer.parseInt(complementOne));
        break;

      case FLOAD:
        operandStack.load(Integer.parseInt(complementOne));
        break;
        
      case FMUL: 
        float mf1 = ((Double) operandStack.pop()).floatValue();
        float mf2 = ((Double) operandStack.pop()).floatValue();
        float r = mf1 * mf2;    
        operandStack.push(r);           
        break;
       
      case FNEG:
        float f = ((Double) operandStack.pop()).floatValue();
        operandStack = callStack.pop();
        operandStack.push(-f);
        break;
        
      case FREM: 
        float fop1 = (Float) operandStack.pop();
        float fop2 = (Float) operandStack.pop();
        float rep = (fop1 % fop2);
        operandStack.push(new Float(rep));
        break;

      case FRETURN:
        float fvalue = (Float) operandStack.pop();
        operandStack = callStack.pop();
        operandStack.push(fvalue);
        break;

      case FSTORE:
        operandStack.store(Integer.parseInt(complementOne));
        break;

      case FSUB:
        float ff1 = (Float) operandStack.pop();
        float ff2 = (Float) operandStack.pop();
        float fresp = ff1 - ff2;
        operandStack.push(fresp);
        break;

      case LSUB:
        long l1 = (Long) operandStack.pop();
        long l2 = (Long) operandStack.pop();
        long respl = l1 - l2;
        operandStack.push(respl);

        break;

      case LUSHR:
        int lo1 = (Integer) operandStack.pop();
        long lo2 = (Long) operandStack.pop();
        long resu = lo2 >>> lo1;
        operandStack.push(resu);
        break;

      case NOP:
        break;
        
      case POP2:
        operandStack.pop();

        if (!operandStack.empty()) {
          operandStack.pop();
        }

        break;

      case RET:
          operandStack.load(Integer.parseInt(complementOne));
          i = lookupForLabel(labels, (String) operandStack.pop(), i);
          break;
        
      case GOTO_W:
          i = lookupForLabel(labels, complementOne+complementTwo, i);
          break;
        
      case SASTORE:
          Object v = operandStack.pop();
          int indx = (Integer) operandStack.pop();
          HeapCell arref = (HeapCell) operandStack.pop();
          arref.store(((short) indx)+"", v);
          break;
        
        case SALOAD:
          int ind = (Integer) operandStack.pop();
          HeapCell ref = (HeapCell) operandStack.pop();
          operandStack.push((Integer) ref.load(ind+""));
          break;
        
        case SWAP:
          Object v1 = operandStack.pop();
          Object v2 = operandStack.pop();
          operandStack.push(v1);
          operandStack.push(v2);
          break;      
        
        case I2D:
          value = (Integer) operandStack.pop();
          double d = (double) value;
          operandStack.push(d);
          break;
        
        case I2C:
          value = (Integer) operandStack.pop();
          char c = (char) value;
          operandStack.push((int) c);
          break;
        
        case I2B:
            value = (Integer) operandStack.pop();
            byte b = (byte) value;
            operandStack.push((int) b);
            break; 

        case I2F:
          val1 = (Integer) operandStack.pop();
          operandStack.push((float) val1);
          break;
          
        case I2L:
          val1 = (Integer) operandStack.pop();
          operandStack.push((long) val1);
          break;
          
        case I2S:
          val1 = (Integer) operandStack.pop();
          operandStack.push((short) val1);
          break;
          
        case IFGT:
          val1 = (Integer) operandStack.pop();
          if(val1 > 0){
            i = lookupForLabel(labels, complementTwo, i);
          }
          break;
          
        case IFLE:
          val1 = (Integer) operandStack.pop();
          if(val1 <= 0){
            i = lookupForLabel(labels, complementTwo, i);
          }
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
      String tmp = instructionTrace.get(k);
      tmp = tmp.split(":")[1].trim(); 
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
      throw new RuntimeException("ERROR: Could not find requested label!");
    }
    return res;
  }
  
  public String[] extractComplements(String[] splits) {
    String complementOne, complementTwo, complementThree;
    complementOne = complementTwo = complementThree = null;
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
    return new String[]{complementOne, complementTwo, complementThree};
  }

}