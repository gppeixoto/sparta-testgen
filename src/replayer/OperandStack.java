package replayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


class OperandStack {

  @SuppressWarnings("unused")
  private String mName;
  private Map<Integer, Object> locals = new HashMap<Integer, Object>();
  private Stack<Object> stack = new Stack<Object>();

  OperandStack(String mName) {
    this.mName = mName;
  }

  void push(Object val) {
    stack.push(val);
  }

  public void store(int local) {
    locals.put(local, stack.pop());
  }

  public void store(int j, Object object) {
    locals.put(j, object);
  }

  public void load(int local) {
    stack.push(locals.get(local));
  }

  public Object pop() {
    return stack.pop();
  }

  public Object peek() {
    return stack.peek();
  }


}