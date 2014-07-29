package replayer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class Graph {
 
  Map<String, HashSet<String>> graph; //Realiza o mapeamento de uma feature 
  
  public Graph(){
    graph = new HashMap<String,HashSet<String>>();
  }
  
  public void add(Set<String> source, Set<String> dest){
//    if(graph.containsKey(s)){
//      graph.get(s).add(t);
//    } else {
//    }
    System.out.println("SOURCE : " + source);
    System.out.println("DEST : " + dest);
    source.removeAll(dest);
    for (String s : source){
      for (String t : dest){
        if(s.equals(t))continue;
        if (graph.containsKey(s)){
          
          graph.get(s).add(t);
          System.out.println("inseri "+t+" em "+s);
        } else {
          HashSet<String> aux = new HashSet<String>();
          aux.add(t);
          graph.put(s, aux);
          System.out.println("inseri "+t+" em "+s);
        }
      }
    }
  }
  
  @Override
  public String toString(){
    String ret = "";
    for (String s : graph.keySet()){
      ret += s + " => " + graph.get(s).toString() + "\n";
    }
    return ret;
  }
}