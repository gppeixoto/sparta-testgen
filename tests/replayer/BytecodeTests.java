package replayer;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BytecodeTests {

  public Main loadAndRun(String... s) {
    List<String> ops = Arrays.asList(s);
    Main main = new Main(ops);
    main.replay();
    return main;
  }
  
  public Main load(String... s) {
    List<String> ops = Arrays.asList(s);
    Main main = new Main(ops);
    return main;
  }

  @Test
  public void testACONST_NULL() {
    Main m = loadAndRun("main: ACONST_NULL");
    Assert.assertNull(m.operandStack.peek());
  }

  public static Object ret() {
    return new Object();
  }

  @Test
  public void testARETURN() {
    Main m = loadAndRun(
        "main :     INVOKESTATIC replayer/BytecodeTests.ret ()Ljava/lang/Object;",
        "foo :     NEW java/lang/Object",
        "foo :     ARETURN");
    Assert.assertTrue(m.operandStack.peek() != null);
  }

  @Test
  public void testARRAYLENGTH() {
    Main m = loadAndRun("main :     ICONST_5",
        "main :     ANEWARRAY java/lang/String", 
        "main :     ARRAYLENGTH");
    Assert.assertEquals(5, m.operandStack.peek());
  }

  @Test
  public void testBALOAD() {
    Main m = load("main : BALOAD");
    HeapCell arr = m.heap.newCell();
    arr.store("0", new Byte((byte) 2));
    m.operandStack.push(arr);
    m.operandStack.push(0);
    m.replay();
    Assert.assertEquals(new Integer(2), m.operandStack.peek());
    
    m = load("main : BALOAD");
    arr = m.heap.newCell();
    arr.store("0", Boolean.TRUE);
    m.operandStack.push(arr);
    m.operandStack.push(0);
    m.replay();
    Assert.assertEquals(new Integer(1), m.operandStack.peek());
  }
  
  @Test
  public void testIFNULL() {
    //TODO how to test branching?
  }
  
  @Test
  public void testIINC() {
    Main m = load("main : IINC 1 10");
    m.operandStack.store(1, 10);
    m.replay();
    Assert.assertEquals(new Integer(20), m.operandStack.getLocals().get(1));
  }
  
  @Test
  public void testBASTORE() {
    Main m = load("main : BASTORE");
    HeapCell arr = m.heap.newCell();
    m.operandStack.push(arr);
    m.operandStack.push(0);
    m.operandStack.push((byte)8);
    m.replay();
    Assert.assertEquals(new Byte((byte)8), arr.load("0"));
  }
  
  @Test
  public void testCASTORE() {
    Main m = load("main : CASTORE");
    HeapCell arr = m.heap.newCell();
    m.operandStack.push(arr);
    m.operandStack.push(0);
    m.operandStack.push('a');
    m.replay();
    Assert.assertEquals('a', arr.load("0"));
  }
  
  @Test
  public void testCALOAD() {
    Main m = load("main : CALOAD");
    HeapCell arr = m.heap.newCell();
    arr.store("0", 'a');
    m.operandStack.push(arr);
    m.operandStack.push(0);
    m.replay();
    Assert.assertEquals('a', m.operandStack.peek());
  }
  
  @Test
  public void testD2F() {
    Main m = load("main : D2F");
    m.operandStack.push(2.21d);
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Float && val.equals(new Float(2.21d)));
  }
  
  @Test
  public void testL2D() {
    Main m = load("main : L2D");
    m.operandStack.push(2l);
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Double && val.equals(new Double(2l)));
  }
  
  @Test
  public void testD2I() {
    Main m = load("main : D2I");
    m.operandStack.push(2.1);
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Integer && val.equals(new Integer(2)));
    
    m = load("main : D2I");
    m.operandStack.push(-2.1);
    m.replay();
    val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Integer && val.equals(new Integer(-2)));
  }
  
  @Test
  public void testD2L() {
    Main m = load("main : D2L");
    m.operandStack.push(2.1);
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Long && val.equals(new Long(2)));
    
    m = load("main : D2L");
    m.operandStack.push(-2.1);
    m.replay();
    val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Long && val.equals(new Long(-2)));
  }
  
  @Test
  public void testL2I() {
    Main m = load("main : L2I");
    long l = 200000000000l;
    m.operandStack.push(l);
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Integer);
    Assert.assertEquals((int) (l << 32 >> 32), val);
  }
  
  @Test
  public void testDCONST() {
    Main m = load("main : DCONST_1");
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Double);
    Assert.assertEquals(1.0 , val);
  }
  
  @Test
  public void testLCONST() {
    Main m = load("main : LCONST_1");
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Long);
    Assert.assertEquals(1l , val);
  }
  
  @Test
  public void testDREM() {
    Main m = load("main : DREM");
    m.operandStack.push(8.0);
    m.operandStack.push(3.0);
    m.replay();
    Object val = m.operandStack.peek();
    Assert.assertTrue(val instanceof Double);
    Assert.assertEquals(2.0 , val);
  }
  
  @Test
  public void testDUPX1() {
    Main m = load("main : DUP_X1");
    m.operandStack.push(2.0);
    m.operandStack.push(1.0);
    m.replay();
    Assert.assertEquals(m.operandStack.pop(), new Double(1.0));
    Assert.assertEquals(m.operandStack.pop(), new Double(2.0));
    Assert.assertEquals(m.operandStack.pop(), new Double(1.0));
  }
  
}