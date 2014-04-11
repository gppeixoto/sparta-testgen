//package instrumentation.agent;
//
//import instrumentation.agent.InstrumentationState.Event.Kind;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.Stack;
//import java.util.Map.Entry;
//
//
//public class InstrumentationState {
//  
//  
//  /***
//   * will use one or the other data-structure depending
//   * on the configuration (HeuristicOptions.traceForThreads)
//   ***/
//  public static Map<Thread, List<Event>> traceForThreads = new HashMap<Thread,List<Event>>();
//  public static List<Event> simpleTrace = new ArrayList<Event>();
//  public static Thread t;
//  
//  public static void clear() {
//	  simpleTrace.clear();
//  }
//  
//  private static void updateEvent(Event ev) {
//    simpleTrace.add(ev);
//  }
//  
//  /**
//   * @return a clone of simpleTrace
//   */
//  public static List<Event> getSimpleTrace() {
//    ArrayList<Event> evList = new ArrayList<Event>(simpleTrace.size());
//    evList.addAll(simpleTrace);
//    return evList;
//  }
//  
//
//  /*** methods called from instrumented sequence ***/
//  public synchronized static void entry(String m) {
//    Event ev = getEvent(m, Event.Kind.ENTRY);
//    updateEvent(ev);
//  }
//  public synchronized static void exit(String m) {
//    Event ev = getEvent(m, Event.Kind.EXIT);
//    updateEvent(ev);
//  }
//  public synchronized static void throwE(String m) {
//    Event ev = getEvent(m, Event.Kind.THROW);
//    updateEvent(ev);
//  }
//  public synchronized static void catchE(String m) {
//    Event ev = getEvent(m, Event.Kind.CATCH);
//    updateEvent(ev);
//  }
//  
//  private static Event getEvent(String m, Kind entry) {
//    String key = m + entry.toString();
//    Event ev = eventCache.get(key);
//    
//    if(ev == null) { //store in the cache
//      ev = new Event(m, entry);
//      eventCache.put(key, ev);
//    }
//    
//    return ev;
//  }
//
//  
//  public static void printHistogram(Set<Stack<String>> covered) {
//    Map<Integer,Integer> histogram = new HashMap<Integer,Integer>();
//    for(Stack<String> stack : covered) {    
//      int size = stack.size();
//      Integer totalStacks = histogram.get(size);
//      totalStacks = totalStacks == null ? 1 : totalStacks + 1;
//      histogram.put(size, totalStacks);
//    }
//
//    System.out.println("HISTOGRAM (COVERED STACKS)");
//    for(Entry<Integer,Integer> entry : histogram.entrySet()) {
//      System.out.println(entry.getKey() + " - " + entry.getValue());
//    }
//  }
//  
//  /*** supporting types (immutable) ***/
//  public static class Event {
//    public final String m;
//    public final Kind k;
//    public enum Kind { THROW , CATCH, ENTRY, EXIT }
//    public Event(String m, Kind k) {
//      this.m = m;
//      this.k = k;
//    };
//    public String toString() {
//      return m + " " + k;
//    }
//    public static Event parseEvent(String str) {
//      Kind kind; 
//      int idx;
//      if ((idx = str.indexOf(""+Kind.THROW)) != -1) {
//        kind = Kind.THROW;
//      } else if ((idx = str.indexOf(""+Kind.CATCH)) != -1) {
//        kind = Kind.CATCH;
//      } else if ((idx = str.indexOf(""+Kind.ENTRY)) != -1) {
//        kind = Kind.ENTRY;
//      } else if ((idx = str.indexOf(""+Kind.EXIT)) != -1) {
//        kind = Kind.EXIT;
//      } else {
//        throw new RuntimeException();
//      }
//      return new Event(str.substring(0, idx).trim(), kind);
//    }
//    public int hashCode() {
//      return (m + k.toString()).hashCode();
//    }
//  }
//
//  public static Map<String,Event> eventCache = new HashMap<String,Event>();
//  public static Stack<String> requirement = null; //current target requeriment (if any)
//}
