package replayer;

import java.util.EmptyStackException;
import java.util.Stack;

public class CallStack {
	
	private Stack<OperandStack> stack = new Stack<OperandStack>();
	
	public OperandStack push(String mName) {
		OperandStack result = new OperandStack(mName);
		stack.push(result);
		return result;
	}

	public OperandStack pop() {
		stack.pop();
		OperandStack tmp = null;
		try {
			tmp = stack.peek();
		} catch (EmptyStackException _)  { }
		return tmp;
	}
	
}
