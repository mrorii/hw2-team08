package edu.cmu.lti.f12.hw2.hw2_team08.retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/*
 *  @author Naoki Orii <norii@andrew.cmu.edu>
 * 
 */
public class GSRetrievalStrategist extends AbstractRetrievalStrategist {

  private Map<String, List<String>> mQueryDocIDMap;
  
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    
    mQueryDocIDMap = new HashMap<String, List<String>>();
    String filePath = "src/main/resources/data/gs.passages.txt";
    try {
      loadMap(filePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void loadMap(String filePath) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
            filePath))));
    String line;

    while ((line = br.readLine()) != null) {
      String[] columns = line.split("\t");
      String query = columns[0];
      int endIndex = columns.length;
      List<String> docIDs = Arrays.asList(columns).subList(1, endIndex);
      mQueryDocIDMap.put(query, docIDs);
    }
  }
  
  @Override
  protected List<RetrievalResult> retrieveDocuments(String query, List<Keyterm> keyterms) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    
    if (!mQueryDocIDMap.containsKey(query)) {
      System.out.println("Error: query not found in GSRetrievalStrategist");
      System.out.println("Query: " + query);
      System.exit(1);
    }
    
    for (String docID : mQueryDocIDMap.get(query)) {
      RetrievalResult r = new RetrievalResult(docID, 1.0F, query);
      result.add(r);
    }

    return result;
  }
}
