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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

  /**
   * state
   */
  List<String> instructionTrace;
  Heap heap;
  OperandStack operandStack;
  CallStack callStack;
  StaticArea sa;
  Map<String,Set<String>> permissions;
  List<String> features;
  Graph flowGraph;
  public Main(List<String> input,List<String> mp) {
    instructionTrace = input;
    heap = new Heap(); 
    callStack = new CallStack();
    operandStack = callStack.push("main");
    sa = new StaticArea();
    features = mp;
    permissions = new HashMap<String, Set<String>>();
    flowGraph = new Graph();
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
		//read features
    List<String> buffer2 = new ArrayList<String>();
    br = new BufferedReader(new FileReader("features.in"));
    while ((s = br.readLine()) != null) {
      buffer2.add(s.trim());
    }
    br.close(); 
    // replay
    Main m = new Main(buffer,buffer2);
    try {
      m.replay();
    } catch (FinishedExecutionException _) {
      System.out.println("  execution replayed");
    }
    System.out.println("\nFLOWGRAPH:");
    System.out.println(m.flowGraph);
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

    //permissions
    for(String line : features){
      String[] p = line.split(" ");
      String feature = p[0];
      String attribute = p[1];
      Set<String> s = permissions.get(attribute);
      Set<String> set = new HashSet<String>();
      if(s == null){
        set.add(feature);
        permissions.put(attribute,set);
      }else{
        s.add(feature);
      }
    }
    System.out.println("PERMISSIONS : " + permissions);
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
          String methodARETURN = operandStack.getMethodName(); //pega nome do metodo do topo da pilha
          MyObject objectref = operandStack.pop();
          if (permissions.get(methodARETURN) != null){
            Set<String> setAreturn = objectref.getSet();
            setAreturn.addAll(permissions.get(methodARETURN));
            objectref.setFeatures(setAreturn);
          }
          operandStack = callStack.pop();
          operandStack.push(objectref);
          break;
      
      case ARRAYLENGTH: //TODO: MAB - WRONG!
        // The arrayref must be of type reference and must refer to an array. It is popped from the operand stack.
        MyObject myobj = operandStack.pop();
        HeapCell arrayref = (HeapCell) myobj.getObject();
        // The length of the array it references is determined. That length is pushed onto the operand stack as an int.
        operandStack.push(new MyObject(arrayref.getMapSize()));
        // Para isso, criamos o metodo getMapSize() em HeapCell para acessar o tamanho do Map, pois este eh privado.
        break;
      /**
       * REVISAR NA VOLTA DAQUI PRA CIMA
       */
      case BALOAD:
        //The arrayref must be of type reference and must refer to an array
        //whose components are of type byte or of type boolean. 
        
        //The index must be of type int. 
        //Both arrayref and index are popped from the operand stack
        int indice = (Integer) operandStack.pop().getObject();
        HeapCell arrayRef = (HeapCell) operandStack.pop().getObject();
        MyObject objetoCarregado = arrayRef.load(indice+"");
        int inteiroCarregar = 0;
        
        //The byte value in the component of the array at index is retrieved,
        //sign-extended to an int value, and pushed onto the top of the operand stack.
        
        // Caso o objeto seja um byte
        if (objetoCarregado.getObject() instanceof Byte) inteiroCarregar = ((Byte)objetoCarregado.getObject()).intValue();
        // Caso seja um boolean
        else if (objetoCarregado.getObject() instanceof Boolean){
          if (((Boolean)objetoCarregado.getObject()).booleanValue()) inteiroCarregar = 1;
          else inteiroCarregar = 0;
        } 
        
        // Colocando na operandStack
        operandStack.push(new MyObject(inteiroCarregar));
        break;
        
      case IFNULL:
        Object valor = operandStack.pop().getObject();
        if (valor==null) {
          i = lookupForLabel(labels, complementTwo, i);
        }
        break;
     
      case IINC:
        operandStack.load(Integer.parseInt(complementOne)); // coloca o valor da variavel em index na pilha
        int valorVariavel = (Integer) operandStack.pop().getObject(); // recupera o valor da variavel
        int valorConstante = Integer.parseInt(complementTwo); // valor a ser incrementado 
        int resultado = valorVariavel + valorConstante; // resultado a ser salvo
        operandStack.push(new MyObject((Object) resultado)); // coloca o resultado na pilha
        operandStack.store(Integer.parseInt(complementOne)); // coloca o resultado colocado na pilha na variavel
        
        break;
      
      case BIPUSH:
        operandStack.push(new MyObject(Integer.parseInt(complementOne)));
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
        operandStack.push(new MyObject (neg?-tmp:tmp));
        break;

      case RETURN:
        operandStack = callStack.pop();
        break;

      case ANEWARRAY:
      case NEWARRAY:
        //TODO: ignoring count.  this will be important to reproduce out-of-bounds exceptions
        operandStack.pop();
        // ignoring type for now
        operandStack.push(new MyObject (heap.newCell()));
        break;

      case LAND:
        long op1 = (Long) operandStack.pop().getObject();
        long op2 = (Long) operandStack.pop().getObject();
        operandStack.push(new MyObject (op1&op2));
        break;
              
      case LCONST:
        long constant = Long.parseLong(complementOne);
        operandStack.push(new MyObject (constant));
        break;
              
      case DCMPG:
      case DCMPL:
              
        double valor1 = (Double) operandStack.pop().getObject();
        double valor2 = (Double) operandStack.pop().getObject();
              
        switch(kind){
            case DCMPG:
                if(valor1 == Double.NaN || valor2 == Double.NaN || valor1 > valor2)
                    operandStack.push(new MyObject(1));
                else if(valor1 == valor2)
                    operandStack.push(new MyObject(0));
                else
                    operandStack.push(new MyObject(-1));
                break;
            case DCMPL:
                if(valor1 == Double.NaN || valor2 == Double.NaN || valor1 < valor2)
                    operandStack.push(new MyObject (-1));
                else if(valor1 == valor2)
                    operandStack.push(new MyObject(0));
                else
                    operandStack.push(new MyObject(1));
                break;
            default:
                 throw new RuntimeException("Interpretation of Instruction undefined: " + kind);
        }
      
        break;
              
      case DCONST:
        double constante = Double.parseDouble(complementOne);
        operandStack.push(new MyObject(constante));
        break;
              
      case DNEG:
        double to_neg = (Double) operandStack.pop().getObject();
        operandStack.push(new MyObject((-1.0)*to_neg));
        break;
        
      /**
       * mudar axáxá
       */
      case DUP:
        if (complementOne != null) {
          MyObject operand1;
          MyObject operand2;
          
          if (complementOne.equals("X1")) {
            operand1 = operandStack.pop();
            operand2 = operandStack.pop();
            
            operandStack.push(operand1);
            operandStack.push(operand2);
            operandStack.push(operand1);

          } else if (complementOne.equals("X2")) {
            operand1 = operandStack.pop();
            operand2 = operandStack.pop();
            
            if ((!(operand1.getObject() instanceof Double) || !(operand1.getObject() instanceof Long)) 
                && ((operand2.getObject() instanceof Double) || (operand2.getObject() instanceof Long))) {
              operandStack.push(operand1);
              operandStack.push(operand2);
              operandStack.push(operand1);
            } else {
              MyObject operand3 = operandStack.pop();
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
            if((operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
              //Categoria 2
              MyObject value1 = operandStack.pop();
              if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                  MyObject value2 = operandStack.pop();
                  operandStack.push(value1);
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }else{
              //Categoria 1
              MyObject value1 = operandStack.pop();
              if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                  MyObject value2 = operandStack.pop();
                  if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                      MyObject value3 = operandStack.pop();
                      operandStack.push(value2);
                      operandStack.push(value1);
                      operandStack.push(value3);
                  }
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }
          } else if (complementOne.equals("X2")) {
            if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
              MyObject value1 = operandStack.pop();
              if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                  MyObject value2 = operandStack.pop();
                  if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                      //Form 1
                      MyObject value3 = operandStack.pop();
                      if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                          MyObject value4 = operandStack.pop();
                          operandStack.push(value2);
                          operandStack.push(value1);
                          operandStack.push(value4);
                      }
                      operandStack.push(value3);
                  }else{
                      //Form 3
                      MyObject value3 = operandStack.pop();
                      operandStack.push(value2);
                      operandStack.push(value1);
                      operandStack.push(value3);
                  }
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }else{
              MyObject value1 = operandStack.pop();
              if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                  //Form 2
                  MyObject value2 = operandStack.pop();
                  if(!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)){
                      MyObject value3 = operandStack.pop();
                      operandStack.push(value1);
                      operandStack.push(value3);
                  }
                  operandStack.push(value2);
              }else{
                  //Form 4
                  MyObject value2 = operandStack.pop();
                  operandStack.push(value1);
                  operandStack.push(value2);
              }
              operandStack.push(value1);
          }
          } else {
            throw new RuntimeException("Unknown complement");
          }
        } else {
          if (!(operandStack.peek().getObject() instanceof Double || operandStack.peek().getObject() instanceof Long)) {
            // Categoria 1
            MyObject value1 = operandStack.pop();
            MyObject value2 = operandStack.peek();
            if (!(value2.getObject() instanceof Double || value2.getObject() instanceof Long)) {
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
        MyObject val = operandStack.pop();
        int index = (Integer) operandStack.pop().getObject();
        HeapCell arRef = (HeapCell) operandStack.pop().getObject();
        arRef.store(index+"", val);
        break;

      case AALOAD:
      case CALOAD:
      case IALOAD:
      case LALOAD:
      case DALOAD:
      case FALOAD:
        index = (Integer) operandStack.pop().getObject();
        arRef = (HeapCell) operandStack.pop().getObject();
        operandStack.push(arRef.load(index+""));
        break;
        
      case D2I:
        double var_d2i1 = (Double) operandStack.pop().getObject();
        if (Double.isNaN(var_d2i1)){
          operandStack.push(new MyObject((Integer) 0));
        } else if (var_d2i1 == Double.POSITIVE_INFINITY){
          operandStack.push(new MyObject(Integer.MAX_VALUE));
        } else if (var_d2i1 == Double.NEGATIVE_INFINITY){
          operandStack.push(new MyObject(Integer.MIN_VALUE));
        } else {
          operandStack.push(new MyObject((var_d2i1 < 0) ? ((int) Math.ceil(var_d2i1)) : ((int) Math.floor(var_d2i1))));
        }
        break;
      
      case D2L:
        double var_d2l = (Double) operandStack.pop().getObject();
        if (Double.isNaN(var_d2l)){
          operandStack.push(new MyObject(new Long(0)));
        } else if (var_d2l == Double.POSITIVE_INFINITY){
          operandStack.push(new MyObject(Long.MAX_VALUE));
        } else if (var_d2l == Double.NEGATIVE_INFINITY){
          operandStack.push(new MyObject(Long.MIN_VALUE));
        } else {
          operandStack.push(new MyObject((var_d2l < 0) ? (new Double(Math.ceil(var_d2l)).longValue()) : (new Double(Math.floor(var_d2l))).longValue()));
        }
        break;
      
      case L2F:
        long var_l2f = (Long) operandStack.pop().getObject();
        operandStack.push(new MyObject((float) var_l2f));
        break;
      
      case L2I:
        long var_l2i = (Long) operandStack.pop().getObject();
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
        operandStack.push(new MyObject(ret_l2i));
        break;
        
        
      case DREM:
      {
          double d2 = (Double) operandStack.pop().getObject(); 
          double d1 = (Double) operandStack.pop().getObject();
          double res;
          if(d1==Double.NaN || d2==Double.NaN)
            res = Double.NaN;
          else if(Double.isInfinite(d1) || d2 == 0)
            res = Double.NaN;
          else if(Double.isInfinite(d2))
            res = d1;
          else
            res = (Double) (d1-(d2*((int)(d1/d2))));
          operandStack.push(new MyObject(res));
      }
          break;      
          
      case DRETURN:
      {
        String methodDRETURN = operandStack.getMethodName();
        MyObject dreturnref = operandStack.pop();
        double d1 = (Double) dreturnref.getObject();
        if (permissions.get(methodDRETURN) != null){
          Set<String> setDRETURN = dreturnref.getSet();
          setDRETURN.addAll(permissions.get(methodDRETURN));
          dreturnref.setFeatures(setDRETURN);
        }
        operandStack = callStack.pop();
        operandStack.push(dreturnref);
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
            
            operandStack.push(new MyObject(k));
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
              operandStack.push(new MyObject(k));
            } catch(NumberFormatException __) {
              operandStack.push(new MyObject(complementOne+complementTwo));
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
            
            operandStack.push(new MyObject(k));
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
              operandStack.push(new MyObject(k));
            } catch(NumberFormatException __){
            
            }
          }
          break;
      
      case ldiv:
         long valB = (Long) operandStack.pop().getObject();
         long valA = (Long) operandStack.pop().getObject();
         
         if(valB==0)
           throw (new RuntimeException("Arithmetic Exception"));
         else if(valB==-1 && valA==Long.MAX_VALUE)
         {
           operandStack.push(new MyObject(valA));
         }
         else
         {
           operandStack.push(new MyObject(valA/valB)); 
         }
          break;    
      
      case LADD:
        long myTmp_ladd;
        long var_ladd1 = (Long) operandStack.pop().getObject();
        long var_ladd2 = (Long) operandStack.pop().getObject();
        
        myTmp_ladd = var_ladd1 + var_ladd2;
        operandStack.push(new MyObject(myTmp_ladd));
        break;
        
      case LDC:
        try {
          int k = Integer.parseInt(complementOne);
          operandStack.push(new MyObject(k));
        } catch(NumberFormatException _) {
          try {
            long k = Long.parseLong(complementOne);
            operandStack.push(new MyObject(k));
          } catch(NumberFormatException __) {
            try {
              double k = Double.parseDouble(complementOne);
              operandStack.push(new MyObject(k));
            } catch (NumberFormatException ___) {
              operandStack.push(new MyObject(complementOne));
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
          String clazz = complementOne.substring(0, idx).replace('/', '.');//classe
		  String fieldName = complementOne.substring(idx+1);
		  Set<String> putstaticSET = permissions.get(complementOne);
		  MyObject putstaticref = operandStack.pop();
		  if (putstaticSET != null){
		    putstaticref.getSet().addAll(putstaticSET);
		    flowGraph.add(putstaticref.getSet(), putstaticSET);
		  }
          sa.putStatic(Class.forName(clazz), fieldName, putstaticref);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("check this!");
        }
        break;

      case NEW: 
        operandStack.push(new MyObject(heap.newCell()));
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
          listParametros.add(operandStack.pop().getObject());
        }
        
        MyObject methodType = operandStack.pop();
        MyObject methodName = operandStack.pop();
        MyObject methodLookup = operandStack.pop();
        MyObject methodHandle = operandStack.pop();
        
        listParametros.add(methodType.getObject());
        listParametros.add(methodName.getObject());
        listParametros.add(methodLookup.getObject());
        listParametros.add(methodHandle.getObject());
        
        operandStack = callStack.push(cNameDyn + mNameDyn);
        for (int j = 0 ; j < (int) listParametros.size(); ++j){
          operandStack.store(listParametros.size()-j-1, new MyObject(listParametros.get(j)));
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
        /**
         * List<Object> list = new ArrayList<Object>();
        for (int k = 0; k < numParams; k++) {
          list.add(operandStack.pop().getObject());
        }
        // this reference
        if (!isStatic) {
          list.add(operandStack.pop().getObject());
        }
        if (skip) {
          // ASSUMING NO RELEVANT MUTATION
        } else {
          operandStack = callStack.push(cName + mName);
          for (int j = 0; j < list.size() ; j++) {
            operandStack.store(list.size()-j-1, new MyObject(list.get(j)));
          }
        }
        break;
         */
        List<MyObject> list = new ArrayList<MyObject>();
        MyObject aux;
        for (int k = 0; k < numParams; k++) {
          aux = operandStack.pop();
          list.add(aux);
        }
        
        int aux_var = 0;
        for (int k=list.size()-1; k>=0; --k, ++aux_var){
          aux = list.get(k);
          String key = cName+mName+"-"+(aux_var+"");
          if (permissions.containsKey(key)){
            Set<String> perm = permissions.get(key);
            if (!aux.getSet().isEmpty()){
              flowGraph.add(aux.getSet(), perm);
            }
            aux.getSet().addAll(permissions.get(key));
          }
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
            operandStack.store(list.size()-j-1, (list.get(j)));
          }
        }
        break;

      case PUTFIELD: 
        val = operandStack.pop();
        HeapCell objRef = (HeapCell) operandStack.pop().getObject();
        String fieldName = complementOne.substring(complementOne.lastIndexOf(".")+1);
        /*
         * vê quais são as features às quais o atributo está associada no arquivo features.in
         * se estiver associado à alguma feature, 
         *      então une essa feature ao seu conjunto
         *      atual de features
        */
        Set<String> set = permissions.get(complementOne);
        if(set != null){
          
          Set<String> newSet = new HashSet<String>();
          Set<String> auxSet = val.getSet();
          newSet.addAll(auxSet);
          newSet.addAll(set);
          val.setFeatures(newSet);
          flowGraph.add(val.getSet(), set);
        }
        objRef.store(fieldName, val);
        break;

      case GETFIELD: 
        objRef = (HeapCell) operandStack.pop().getObject();
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

        double d1 = (Double) operandStack.pop().getObject();
        double d2 = (Double) operandStack.pop().getObject();

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

        operandStack.push(new MyObject(res));
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
        
        MyObject v1 = operandStack.pop();
        MyObject v2 = operandStack.pop();
        int val1 = (Integer) v1.getObject();
        int val2 = (Integer) v2.getObject();
        Set<String> resultset = new HashSet<String>();
        resultset.addAll(v2.getSet());
        resultset.addAll(v1.getSet());
        
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
        operandStack.push(new MyObject(tmp,resultset));
        break;

      case LCMP:
        val1 = (Integer) operandStack.pop().getObject();
        val2 = (Integer) operandStack.pop().getObject();
        operandStack.push(new MyObject(val1 == val2 ? 0 : (val1 < val2 ? -1 : 1)));

      case IRETURN:
        String methodIRETURN = operandStack.getMethodName();
        MyObject ireturnref = operandStack.pop();
        val1 = (Integer) ireturnref.getObject();
        if (permissions.get(methodIRETURN) != null){
          Set<String> setIreturn = ireturnref.getSet();
          setIreturn.addAll(permissions.get(methodIRETURN));
          ireturnref.setFeatures(setIreturn);
        }
        operandStack = callStack.pop();
        operandStack.push(ireturnref);
        break;

      case INEG:
        val1 = (Integer) operandStack.pop().getObject();
        operandStack = callStack.pop(); //PERA AQUI
        operandStack.push(new MyObject(-val1));
        break;

      case POP:
        operandStack.pop();
        break;
        
      case F2I:

        int result = 0;
        float value1 = (Float) operandStack.pop().getObject();
        if (value1 < 0) {
          result = (int) value1;
        } else if (value1 > 0) {
          result = (int) Math.ceil(value1);
        } else {
          result = 0;
        }
        operandStack.push(new MyObject(result));

        break;

      case F2L:

        long resultLong = 0;
        float valueFloat = (Float) operandStack.pop().getObject();
        if (valueFloat < 0) {
          resultLong = (long) valueFloat;
        } else if (valueFloat > 0) {
          resultLong = (long) Math.ceil(valueFloat);
        } else {
          resultLong = 0;
        }
        operandStack.push(new MyObject(resultLong));

        break;

      case FADD:

        float float1 = (Float) operandStack.pop().getObject();
        float float2 = (Float) operandStack.pop().getObject();
        float floatResult = float1 + float2;
        operandStack.push(new MyObject(floatResult));

        break;

      case LMUL:
        long long1 = (Long) operandStack.pop().getObject();
        long long2 = (Long) operandStack.pop().getObject();
        long longResult = long1 * long2;
        operandStack.push(new MyObject(longResult));

        break;

      case LNEG:
        long1 = (Long) operandStack.pop().getObject();
        longResult = 0 - long1;
        operandStack.push(new MyObject(longResult));

        break;

      case LOR:
        long1 = (Long) operandStack.pop().getObject();
        long2 = (Long) operandStack.pop().getObject();
        longResult = long1 | long2;
        operandStack.push(new MyObject(longResult));

        break;

      case LREM:
        long2 = (Long) operandStack.pop().getObject();
        long1 = (Long) operandStack.pop().getObject();
        longResult = long1 - (long1 / long2) * long2;
        operandStack.push(new MyObject(longResult));

        break;

      case IF:
        val1 = (Integer) operandStack.pop().getObject();
        val2 = (Integer) operandStack.pop().getObject();

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
        operandStack.push(new MyObject(Integer.parseInt(complementOne)));
        break;

      case LOOKUPSWITCH:

        val1 = (Integer) operandStack.pop().getObject();
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
        double value = (Double) operandStack.pop().getObject();
        operandStack.push(new MyObject((float) value));
        break;
        
      case L2D:
        MyObject value_o = null;
        try {
          value_o = operandStack.pop();
          long value_long = (Long) value_o.getObject();
          operandStack.push(new MyObject((double) value_long)); 
        } catch (ClassCastException _) {
          int value_int = (Integer) value_o.getObject();
          operandStack.push(new MyObject((double) value_int));
        }
        break;

      // ----------------------------------------------mra2--->
      case FASTORE:
        MyObject val_f = operandStack.pop();
        int index_f = (Integer) operandStack.pop().getObject();
        HeapCell arRef_f = (HeapCell) operandStack.pop().getObject();
        arRef_f.store(index_f + "", val_f);
        break;
        
      case FCMPG:
        float val_g = (Float) operandStack.pop().getObject();
        float val2_g = (Float) operandStack.pop().getObject();

        if (val_g > val2_g) {
          operandStack.push(new MyObject(1));
        } else if (val_g == val2_g) {
          operandStack.push(new MyObject(0));
        } else {
          operandStack.push(new MyObject(-1));
        }
        break;
        
      case FCMPL:
        float val_l = (Float) ((Double) operandStack.pop().getObject()).floatValue();
        float val2_l = (Float) ((Double) operandStack.pop().getObject()).floatValue();

        if (val_l > val2_l) {
          operandStack.push(new MyObject(1));
        } else if (val_l == val2_l) {
          operandStack.push(new MyObject(0));
        } else {
          operandStack.push(new MyObject(-1));
        }
        break;
        
      case FCONST:
        float tmp3 = Float.parseFloat(complementOne);
        operandStack.push(new MyObject(tmp3));
        break;

      case LRETURN:
        String methodLRETURN = operandStack.getMethodName();
        MyObject lreturnref = operandStack.pop();
        long val_long = (Long) lreturnref.getObject();
        if (permissions.get(methodLRETURN) != null){
          Set<String> setlreturn = lreturnref.getSet();
          setlreturn.addAll(permissions.get(methodLRETURN));
          lreturnref.setFeatures(setlreturn);
        }
        operandStack = callStack.pop();
        operandStack.push(lreturnref);

        break;

      case LSHL:
      case LSHR:

        int val5 = ((Integer) operandStack.pop().getObject()).intValue();
        long val4 = ((Integer) operandStack.pop().getObject()).longValue();
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

        operandStack.push(new MyObject(resp2));
        break;
       
      case FDIV:
        float f1 = (Float) operandStack.pop().getObject();
        float f2 = (Float) operandStack.pop().getObject();
        float resp = f1 / f2;
        operandStack.push(new MyObject (new Float(resp))); 
        break;

      case LSTORE:
        operandStack.store(Integer.parseInt(complementOne));
        break;

      case FLOAD:
        operandStack.load(Integer.parseInt(complementOne));
        break;
        
      case FMUL: 
        float mf1 = ((Double) operandStack.pop().getObject()).floatValue();
        float mf2 = ((Double) operandStack.pop().getObject()).floatValue();
        float r = mf1 * mf2;    
        operandStack.push(new MyObject(r));           
        break;
       
      case FNEG:
        float f = ((Double) operandStack.pop().getObject()).floatValue();
        //operandStack = callStack.pop();
        operandStack.push(new MyObject(-f));
        break;
        
      case FREM: 
        float fop1 = (Float) operandStack.pop().getObject();
        float fop2 = (Float) operandStack.pop().getObject();
        float rep = (fop1 % fop2);
        operandStack.push(new MyObject(new Float(rep)));
        break;

      case FRETURN:
        String methodFRETURN = operandStack.getMethodName();
        MyObject freturnref = operandStack.pop();
        float val_float = (Float) freturnref.getObject();
        if (permissions.get(methodFRETURN) != null){
          Set<String> setfreturn = freturnref.getSet();
          setfreturn.addAll(permissions.get(methodFRETURN));
          freturnref.setFeatures(setfreturn);
        }
        operandStack = callStack.pop();
        operandStack.push(freturnref);

        break;

      case FSTORE: //n mexi
        operandStack.store(Integer.parseInt(complementOne));
        break;

      case FSUB:
        float ff1 = (Float) operandStack.pop().getObject();
        float ff2 = (Float) operandStack.pop().getObject();
        float fresp = ff1 - ff2;
        operandStack.push(new MyObject(fresp));
        break;

      case LSUB:
        long l1 = (Long) operandStack.pop().getObject();
        long l2 = (Long) operandStack.pop().getObject();
        long respl = l1 - l2;
        operandStack.push(new MyObject(respl));

        break;

      case LUSHR:
        int lo1 = (Integer) operandStack.pop().getObject();
        long lo2 = (Long) operandStack.pop().getObject();
        long resu = lo2 >>> lo1;
        operandStack.push(new MyObject(resu));
        break;

      case NOP:
        break;
        
      case POP2: //n mexi
        operandStack.pop();

        if (!operandStack.empty()) {
          operandStack.pop();
        }

        break;

      case RET:
          operandStack.load(Integer.parseInt(complementOne));
          i = lookupForLabel(labels, (String) operandStack.pop().getObject(), i);
          break;
        
      case GOTO_W:
          i = lookupForLabel(labels, complementOne+complementTwo, i);
          break;
        
      case SASTORE:
          MyObject v = operandStack.pop();
          int indx = (Integer) operandStack.pop().getObject();
          HeapCell arref = (HeapCell) operandStack.pop().getObject();
          arref.store(((short) indx)+"", v);
          break;
        
        case SALOAD:
          int ind = (Integer) operandStack.pop().getObject();
          HeapCell ref = (HeapCell) operandStack.pop().getObject();
          operandStack.push(new MyObject((Integer) (ref.load(ind+"")).getObject()));
          break;
        
        case SWAP:
          v1 = operandStack.pop();
          v2 = operandStack.pop();
          operandStack.push(v1);
          operandStack.push(v2);
          break;      
        
        case I2D:
          value = (Integer) operandStack.pop().getObject();
          double d = (double) value;
          operandStack.push(new MyObject(d));
          break;
        
        case I2C:
          value = (Integer) operandStack.pop().getObject();
          char c = (char) value;
          operandStack.push(new MyObject((int) c));
          break;
        
        case I2B:
            value = (Integer) operandStack.pop().getObject();
            byte b = (byte) value;
            operandStack.push(new MyObject((int) b));
            break; 

        case I2F:
          val1 = (Integer) operandStack.pop().getObject();
          operandStack.push(new MyObject((float) val1));
          break;
          
        case I2L:
          val1 = (Integer) operandStack.pop().getObject();
          operandStack.push(new MyObject((long) val1));
          break;
          
        case I2S:
          val1 = (Integer) operandStack.pop().getObject();
          operandStack.push(new MyObject((short) val1));
          break;
          
        case IFGT:
          val1 = (Integer) operandStack.pop().getObject();
          if(val1 > 0){
            i = lookupForLabel(labels, complementTwo, i);
          }
          break;
          
        case IFLE:
          val1 = (Integer) operandStack.pop().getObject();
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
