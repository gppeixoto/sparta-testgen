package replayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


class OperandStack {

  @SuppressWarnings("unused")
  private String mName;
  private Map<Integer, MyObject> locals = new HashMap<Integer, MyObject>();
  private Stack<MyObject> stack = new Stack<MyObject>();

  OperandStack(String mName) {
    this.mName = mName;
  }

  void push(MyObject val) {
    stack.push(val);
  }

  public void store(int local) {
    MyObject aux = stack.pop();
    System.out.println("STORE:"+aux.getSet());
    locals.put(local, aux);
  }

  public void store(int j, MyObject object) {
    locals.put(j, object);
  }

  public void load(int local) {
    stack.push(locals.get(local));
  }

  public MyObject pop() {
    return stack.pop();
  }

  public MyObject peek() {
    return stack.peek();
  }
  
  Map<Integer, MyObject> getLocals() {
    return locals;
  }

  public boolean empty() {
    return stack.empty();
  }
  
  public String getMethodName(){
    return this.mName;
  }

}