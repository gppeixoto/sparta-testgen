package replayer;


import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
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

	enum OPCODE {DUP2, DUP2_X1, DUP2_X2, F2D, BIPUSH, ISTORE, ILOAD, 
		LLOAD_0, LLOAD_1, LLOAD_2, LLOAD_3, ICONST, 
		RETURN, NEWARRAY, DUP, IASTORE, IALOAD, ASTORE, ALOAD, 
		LDC, GETSTATIC, NEW, INVOKESPECIAL, PUTFIELD, INVOKESTATIC, 
		LINENUMBER, IADD, IRETURN, POP, ISUB, IMUL, IDIV, IREM, 
		INEG, IAND, IOR, ISHL, ISHR, IUSHR, IXOR, LCMP, IF, GOTO, 
		FRAME, ANEWARRAY, AASTORE, PUTSTATIC, GETFIELD, AALOAD, SIPUSH,
		DSTORE, DLOAD, DMUL, DADD, DDIV, DSUB, LOOKUPSWITCH, INVOKEVIRTUAL
	};

	/**
	 * ACONST_NULL
	 * ARETURN
	 * ARRAYLENGTH
	 * ATHROW
	 * BALOAD
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
			String[] complements = extractComplements(splits);
			String complementOne = complements[0];
			String complementTwo = complements[1];

			boolean isStatic = false;

			/**
			 * decide which instruction to apply
			 */
			switch (kind) {

			case DUP2:

				if(!(operandStack.peek() instanceof Double || operandStack.peek() instanceof Long)){
					//Categoria 1
					Object value1 = operandStack.pop();
					Object value2 = operandStack.peek();
					if(!(value2 instanceof Double || value2 instanceof Long)){
						operandStack.push(value1);
						operandStack.push(value2);
					}
					operandStack.push(value1);
				}else{
					//Categoria 2
					operandStack.push(operandStack.peek());
				}

				break;

			case DUP2_X1:
				
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
				
				break;
			
			case DUP2_X2:	
				
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
				
				break;
				
			case F2D:
				float value = (Float) operandStack.pop();
				double result = value;
				operandStack.push(result);
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
				
			case LLOAD_0:
				operandStack.load(0);
				break;

			case LLOAD_1:
				operandStack.load(1);
				break;
				
			case LLOAD_2:
				operandStack.load(2);
				break;
				
			case LLOAD_3:
				operandStack.load(3);
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

				try {
					int k = Integer.parseInt(complementOne);
					operandStack.push(k);
				} catch(NumberFormatException _) {
					try {
						double k = Double.parseDouble(complementOne);
						operandStack.push(k);
					} catch(NumberFormatException __) {
						operandStack.push(complementOne);
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

			case INVOKESTATIC:
				isStatic = true;

			case INVOKEVIRTUAL:
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