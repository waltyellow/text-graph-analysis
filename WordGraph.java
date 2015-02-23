//@Author Zhenglin Huang
import java.util.*; // auto-import
import java.nio.file.*;
import java.io.*;

//Specification: Users are expected to input a file of at least two tokenizable words
public class WordGraph{
  //Fields
  String[] wordList;
  //String: node key/word. Int:count
  Hashtable<String,Node> nodes;
  boolean pathFound;
  int totalWords;
  int totalEdges;
  
  //Constuctor
  public WordGraph(String fileName) throws java.io.IOException{
    Tokenizer t = new Tokenizer(fileName);
    wordList = new String[t.wordList().size()];
    wordList = t.wordList().toArray(wordList);
    totalWords = wordList.length;
    totalEdges = 0;
    nodes = new Hashtable<String,Node>();
    //Constructing Nodes, for node value and edge adjacent list
    
    //construct empty hashTables
    for (int i = 0; i < wordList.length; i++){
      nodes.put(wordList[i], new Node(wordList[i]));
    }
    
    for (int i = 0; i < (wordList.length - 1); i++){
      //Create edges
      Node currentNode = nodes.get(wordList[i]);
      Node nextNode = nodes.get(wordList[i+1]);
      //count
      currentNode.value++;
      //count edges and create out edge
      //important method: Node.outToNext(String s0 method returns 1 for a new edge created, returns 0 for updating an existing edge
      totalEdges = totalEdges + currentNode.outToNext(wordList[i+1]);
      //create in edge
      nextNode.inFromPrev(wordList[i]);
    }
    //count the last one
    nodes.get(wordList[wordList.length -1]).value++;
    
  }
  
  public int numNodes(){
    return nodes.size();
  }
  
  public int numEdges(){
    //int outEdges = 0;
    //Enumeration<String> uniqueWords = nodes.keys();
    //while (uniqueWords.hasMoreElements()){
    //outEdges = outEdges + outDegree(uniqueWords.nextElement());
    //}
    return totalEdges;
  }
  
  public int wordCount(String w){
    if (nodes.get(w) == null)
      return -1;
    return nodes.get(w).value;
  }
  
  public int inDegree(String w){
    if (nodes.get(w) == null)
      return -1;
    Node node = nodes.get(w);
    return node.prev.size();
  }
  
  public int outDegree(String w){
    if (nodes.get(w) == null)
      return -1;
    Node node = nodes.get(w);
    return node.next.size();
  }
  
  public String[] prevWords(String w){
    Node node = nodes.get(w);
    String[] previous = new String[node.prev.size()];
    Enumeration<String> enumWords = node.prev.keys();
    int i = 0;
    while (enumWords.hasMoreElements()){
      previous[i]= enumWords.nextElement();
      i++;
    }
    
    return previous;
  }
  
  
  //return the next
  public String[] nextWords(String w){
    Node node = nodes.get(w);
    String[] nexts = new String[node.next.size()];
    Enumeration<String> enumWords = node.next.keys();
    
    int i = 0;
    while (enumWords.hasMoreElements()){
      nexts[i]= enumWords.nextElement();
      i++;
    }
    
    return nexts;
  }
  
  //Cost with the formula
  public double wordSeqCost(String[] wordSeq){
    double L = Math.log(totalWords/wordCount(wordSeq[0]));
    for(int i = 0; i < (wordSeq.length - 1); i++){
      Node node = nodes.get(wordSeq[i]);
      double weight = node.next.get(wordSeq[i+1]);
      L=L+Math.log(1.0*wordCount(wordSeq[i])/weight);
    }
    return L;
  }
  
  public String generatePhrase(String startWord, String endWord, int limit){
    if (nodes.get(endWord) == null){
      //System.out.println("----end of the search sequence----");
      return null;
    }
    if (startWord.equals(endWord)){
      //System.out.println("----end of the search sequence----");
      return startWord;
    }
    if (limit == 1){
      //System.out.println("----end of the search sequence----");
      return null;
    }
    //endWord guaraunteed
    //Comparator for heaped nodes
    nodeComparator heapComparator = new nodeComparator();
    //List for finalized nodes
    ArrayList<Node> finalized = new ArrayList<Node>(numNodes());
    //The heap to organize nodes
    PriorityQueue<Node> heap = new PriorityQueue<Node>(numNodes(),heapComparator);
    
    //Put the first element in, need some speciial handling
    //The initial case
    Node start = nodes.get(startWord);
    start.distance = Math.log(totalWords/wordCount(startWord));
    start.height = 1;
    finalized.add(start);
    
    Node current = start;
    //mark this node as visited, meaning that it has a distance now
    //find all its neighbors
    String[] nextList = nextWords(startWord);
    //operate on each of them
    //if this is the end word of sequence, cut it off immediately
    if (nextList.length == 0){
      //System.out.println("----end of the search sequence----");
      return null;
    }
    for (int i = 0; i < nextList.length; i++){
      //update each of them un distance
      Node nextNode = nodes.get(nextList[i]);
      //calculate the new distance by adding one more log C(wi)/C(wi,wi+1) on its base
      double newDistance = current.distance + Math.log(wordCount(current.key)/current.next.get(nextNode.key));
      nextNode.distance = newDistance;
      //make it count as the next height
      nextNode.height = current.height + 1;
      nextNode.parent = current;
      nextNode.visited = true;
      heap.add(nextNode);
    }
    //search until maximum height reaches the limit
    //Precondition: the previously finalized node, called "current", will not exceed a height of (limit-1)
    //So all nodes in the heap will be at most at (limit) height
    //The loop stops, once we finalized a node that has limited height
    //If the finalized node with limited height is the endword, the method will return and break.
    while(current.height < limit){
      while ((heap.size() > 0) && (finalized.contains(heap.peek()))) {
        //filter out already finalized repeated copies
        heap.poll();
      }
      //this one is not finalized
      current = heap.poll();
      //consider heap size is 0
      if (current == null){
        //System.out.println("----end of the search sequence----");
        return null;
      }
      //^(not found for all depleted)
      
      //finalize it
      finalized.add(current);
      //Find the list of its previous words
      nextList = nextWords(current.key);
      /*-------------------This section is for testing only----------------------
       //Finalize tracing
       System.out.print(current.key+"<----(Finalized)");
       System.out.print("@d=");
       System.out.println((int)(current.distance*100));
       
       
       //if it founds an available word without being the endword
       
       //Tracing for current finalized word's path to original
       Node previousOne = current;
       while (!previousOne.key.equals(startWord)){
       System.out.print(previousOne.key);
       System.out.print(" <--");
       previousOne = previousOne.parent;
       }
       
       System.out.print(previousOne.key);
       System.out.println("----|");
       System.out.println(">>");
       //--------------------------End of testing ---------------------------------*/
      
      //found the end word
      if (current.key.equals(endWord)){
        //output process 
        Node previousOne = current;
        String[] output = new String[current.height];
        StringBuilder so = new StringBuilder();
        //trace back
        for (int i = current.height; i > 0; i--){
          output[i-1]= previousOne.key;
          previousOne = nodes.get(previousOne.parent.key);
        }
        
        for (int i = 0; i < (current.height - 1); i++){
          so.append(output[i]);
          so.append(" ");
        }
        
        so.append(output[current.height-1]);
        //System.out.println("----end of the search sequence----");
        return so.toString();
      }
      
      //Since the word is not the end, we go on updating edges
      //operate on each of the edges
      for (int i = 0; i < nextList.length; i++){
        //update each of them on distance
        Node nextNode = nodes.get(nextList[i]);
        if (!finalized.contains(nextNode)){
          //calculate the new distance by adding one more log C(wi)/C(wi,wi+1) on its base
          double newDistance = current.distance + Math.log(wordCount(current.key)/current.next.get(nextNode.key));
          //replace when needed
          if ((!nextNode.visited) || (newDistance < nextNode.distance)){
            //replace the new distance
            nextNode.distance = newDistance;
            //make it count as the next height
            nextNode.height = current.height + 1;
            nextNode.parent = current;
            
            nextNode.visited = true;
            heap.add(nextNode);
            
          }
        }
      }
    }
    //maxHeight = limit
    //System.out.println("----end of the search sequence----");
    return null;
  }
  
  
  //helpers
  
  private void tokenizeInputW(String w){
    w.replaceAll("[^a-zA-Z]","").toLowerCase().trim();
  }
  
  
  private class nodeComparator implements Comparator<Node>{
    public int compare(Node n1, Node n2){
      if (n1.distance < n2.distance)
        return -1;
      if (n1.distance > n2.distance)
        return 1;
      return 0;
    }
  }
  
  //Main, used for manual testing
  public static void main(String[] args) throws java.io.IOException{
    WordGraph testFile = new WordGraph("Test 1.txt");
    System.out.println("Text1.txt: one sentence: 'after the sun the sun rises from the crest'");
    System.out.println("expected nodes below: 6");
    System.out.println(testFile.numNodes());
    System.out.println("expected edges below: 7");
    System.out.println(testFile.numEdges());
    //3 occurances
    System.out.println("expected count below of 'the': 3 ");
    System.out.println(testFile.wordCount("the"));
    //1 occurances
    System.out.println("expected count below of 'count: 1");
    System.out.println(testFile.wordCount("crest"));
    //does not exist
    System.out.println("expected count below of an non-existent word: -1");
    System.out.println(testFile.wordCount("none"));
    
    System.out.println("expected indegree below of 'the':3");
    System.out.println(testFile.inDegree("the"));
    System.out.println("expected indegree below of 'crest':1");
    System.out.println(testFile.inDegree("crest"));
    System.out.println("expected indegree below of 'after': 0 ");
    System.out.println(testFile.inDegree("after"));
    
    System.out.println("expected outDegree below of 'the': 2 ");
    System.out.println(testFile.outDegree("the"));
    System.out.println("expected outDegree below of 'from': 1 ");
    System.out.println(testFile.outDegree("from"));
    System.out.println("expected outDegree below of 'crest': 0 ");
    System.out.println(testFile.outDegree("crest"));
    System.out.println();
    
    System.out.println("expected below of 'sun - > crest': sun the crest ");
    //System.out.println("----the search sequence----");
    System.out.println(testFile.generatePhrase("sun","crest",6));
    System.out.println();
    System.out.println();
    
    testFile = new WordGraph("Test 1.txt");
    System.out.println("expected below of 'rises->sun': rises from the sun ");
    //System.out.println("----the search sequence----");
    System.out.println(testFile.generatePhrase("rises","sun",6));
    System.out.println();
    System.out.println();
    
    testFile = new WordGraph("Test 1.txt");
    System.out.println("expected below of 'rises->crest': rises from the crest  ");
    //System.out.println("----the search sequence----");
    System.out.println(testFile.generatePhrase("rises","crest",60));
    System.out.println();
    System.out.println();
    
    //cutoff
    testFile = new WordGraph("Test 1.txt");
    System.out.println("expected below of 'rises->crest @ 2': null ");
    //System.out.println("----the search sequence----");
    System.out.println(testFile.generatePhrase("rises","crest",2));
    System.out.println();
    System.out.println();
    
    
    testFile = new WordGraph("Test 2.txt");
    System.out.println("Test 2.txt: one word");
    System.out.println("expected nodes below: 1");
    System.out.println(testFile.numNodes());
    System.out.println("expected edges below: 0");
    System.out.println(testFile.numEdges());
    System.out.println();
    
    testFile = new WordGraph("Test 3.txt");
    System.out.println("Test 3.txt: : a paragraph from a game of thrones to test the phrase generation");
    System.out.println(testFile.generatePhrase("lord","bran",6));
    
    testFile = new WordGraph("A Game of Thrones.txt");
    System.out.println("A ~290k word book");
    System.out.println("expected nodes below: 11812");
    System.out.println(testFile.numNodes());
    System.out.println("expected edges below: 121861");
    System.out.println(testFile.numEdges());
    System.out.println("expected count below of 'the': 17693 ");
    System.out.println(testFile.wordCount("the"));
    System.out.println("expected count below of 'bran': 494 ");
    System.out.println(testFile.wordCount("bran"));
    System.out.println("expected indegree below of 'lannister':197");
    System.out.println(testFile.inDegree("lannister"));
    System.out.println("expected outdegree below of 'lord':84");
    System.out.println(testFile.outDegree("lord"));
    
    System.out.println("below of is used for matching a set of output from P3");
    System.out.println(testFile.numNodes());
    System.out.println(testFile.numEdges());
    System.out.println(testFile.wordCount("the"));
    System.out.println(testFile.wordCount("sun"));
    System.out.println(testFile.inDegree("lannister"));
    System.out.println(testFile.outDegree("lord"));
    System.out.println(testFile.nextWords("lannister").length);
    System.out.println(testFile.nextWords("lord").length);
    
  }
  
}