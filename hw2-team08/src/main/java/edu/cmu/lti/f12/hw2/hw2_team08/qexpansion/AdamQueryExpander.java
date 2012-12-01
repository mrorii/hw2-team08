package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * The AdamQueryExpander wraps the ADAM database
 * (Another Database of Abbreviations in MEDLINE)
 * and retrieves long forms for abbreviated terms.
 * 
 * See <a href="http://arrowsmith.psych.uic.edu/arrowsmith_uic/adam.html">URL</a>
 */
public class AdamQueryExpander extends AbstractQueryExpander {

  private Map<String, List<String>> mTermVariantMap;
  
  // Use singleton pattern
  private static AdamQueryExpander instance = null;

  public static AdamQueryExpander getInstance() {
    if (instance == null) {
      try {
        instance = new AdamQueryExpander();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return instance;
  }
  
  private AdamQueryExpander() throws IOException {
    mTermVariantMap = new HashMap<String, List<String>>();
    
    Properties prop = new Properties();
    InputStreamReader isr = new InputStreamReader(
            new FileInputStream("qexpansion.properties"), "UTF-8");
    prop.load(isr);
    
    String adamFilePath = prop.getProperty("adam_rawfile");

    loadMap(adamFilePath);
  }

  private void loadMap(String filePath) throws FileNotFoundException, IOException {
    BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(new File(filePath))));
    String line;
    
    while ((line = br.readLine()) != null) {
      if (line.startsWith("#")) {
        continue;
      }
      String[] columns = line.split("\t");

      String term = columns[0];
      if (! mTermVariantMap.containsKey(term)) {
        mTermVariantMap.put(term, new ArrayList<String>());
      }
      
      String[] variants = columns[2].split("\\|");
      for (int i = 0; i < variants.length; i++) {
        String variant = variants[i];
        String[] tmp = variant.split(":");
        String var = tmp[0];
        
        mTermVariantMap.get(term).add(var);
      }
    }
  }
  
  @Override
  public List<String> expandQuery(String query) {
    List<String> retval = new ArrayList<String>();
    if (mTermVariantMap.containsKey(query)) {
      retval = mTermVariantMap.get(query);
    }
    return retval;
  }
  
  public static void main(String[] args) {
    AbstractQueryExpander expander = AdamQueryExpander.getInstance();
    
    String query = "BRCA1";
    List<String> expandedQueries = expander.expandQuery(query);
    
    for (String expandedQuery : expandedQueries) {
      System.out.println(expandedQuery);
    }
  }
}
