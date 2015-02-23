//@Author Zhenglin Huang
import java.util.*;
public class Node{
  //String: Node, int: Weight
  public Hashtable<String,Integer> prev;
  public Hashtable<String,Integer> next;
  public Node parent;
  public int value;
  public boolean visited;
  public double distance;
  //this is the string of this node
  public String key;
  //this is the indicator of which word in the sequence is this node
  public int height;
  
  public Node(String k){
    prev = new Hashtable<String,Integer>();
    next = new Hashtable<String,Integer>();
    key = k;
    value = 0;
    parent = this;
    height = 0;
    visited = false;
    distance = 10000000;
  }
  
  //Return 1 if one new edge is created, return 0 if a current edge is updated
  public int outToNext(String nextWord){
    if (next.containsKey(nextWord)){
      Integer newWeight = next.get(nextWord) + 1;
      next.put(nextWord,newWeight);
    }else{
      next.put(nextWord,1);
      return 1;
    }
    return 0;
  }
  
  public int inFromPrev(String prevWord){
    if (prev.containsKey(prevWord)){
      Integer newWeight = prev.get(prevWord) + 1;
      prev.put(prevWord,newWeight);
    }else{
      prev.put(prevWord,1);
      return 1;
    }
    return 0;
  }
  /*
   public int inDegree(){
   return prev.size();
   }
   
   public int outDegree(){
   return next.size();
   }
   */
  
  
}