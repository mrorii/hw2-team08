package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * Helper class for dealing with hypernym and hyponyms
 */
public class MeshConceptHelper {
  
  // Use singleton pattern
  private static MeshConceptHelper instance = null;
  private Map<String, Node> mNameNodeMap;
  
  public static MeshConceptHelper getInstance() {
    if (instance == null) {
      try {
        instance = new MeshConceptHelper();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return instance;
  }
  
  private MeshConceptHelper() throws IOException {
    Properties prop = new Properties();
    InputStreamReader isr = new InputStreamReader(
            new FileInputStream("qexpansion.properties"), "UTF-8");
    prop.load(isr);
    
    String meshTreePath = prop.getProperty("mesh_treefile");
    loadMeshTree(meshTreePath);
  }

  private void loadMeshTree(String meshTreePath) {
    mNameNodeMap = new HashMap<String, Node>();
    
    try {
      BufferedReader br = new BufferedReader(
              new InputStreamReader(new FileInputStream(meshTreePath)));
      String line;
      while ((line = br.readLine()) != null) {
        String[] columns = line.split(";");
        String treeName = columns[0];
        String treeNumber = columns[1];
        
        Node node = new Node(treeNumber, treeName);
        
        if (treeNumber.contains(".")) {
          // Should have parent
          int endIndex = treeNumber.lastIndexOf(".");
          String parentTreeNumber = treeNumber.substring(0, endIndex);
          
          Node parentNode = mNameNodeMap.get(parentTreeNumber);
          parentNode.addChildNode(node);
          node.setParentNode(parentNode);
        }
        mNameNodeMap.put(treeNumber, node);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public List<String> getHyponyms(String query) {
    List<String> retval = new ArrayList<String>();
    for (Node childNode : mNameNodeMap.get(query).getChildNodes()) {
      retval.add(childNode.getTreeName());
    }
    return retval;
  }

  public String getHypernym(String query) {
    return mNameNodeMap.get(query).getParentNode().getTreeName();
  }
  
  public static class Node {
    private String treeNumber; // Example: "C04.588.180"
    private String treeName;   // Example: "Breast Neoplasms"
    private Node parentNode;
    private List<Node> childNodes;
    
    public Node(String treeNumber, String treeName) {
      this.setTreeNumber(treeNumber);
      this.setTreeName(treeName);
      this.childNodes = new ArrayList<Node>();
    }

    public String getTreeNumber() {
      return treeNumber;
    }

    private void setTreeNumber(String treeNumber) {
      this.treeNumber = treeNumber;
    }

    public Node getParentNode() {
      return parentNode;
    }

    public void setParentNode(Node parentNode) {
      this.parentNode = parentNode;
    }

    public List<Node> getChildNodes() {
      return childNodes;
    }
    
    public void addChildNode(Node node) {
      this.childNodes.add(node);
    }

    public String getTreeName() {
      return treeName;
    }

    private void setTreeName(String treeName) {
      this.treeName = treeName;
    }
  }

  public static void main(String[] args) throws IOException {
    MeshConceptHelper helper = MeshConceptHelper.getInstance();
    String query = "C04.588.180"; // "Breast Neoplasms"
    String hypernym = helper.getHypernym(query);
    List<String> hyponyms = helper.getHyponyms(query);
    System.out.println("Hypernym: " + hypernym);
    System.out.println("Hyponyms: " + hyponyms);
  }
}
