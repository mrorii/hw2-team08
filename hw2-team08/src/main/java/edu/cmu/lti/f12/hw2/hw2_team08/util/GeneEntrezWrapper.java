package edu.cmu.lti.f12.hw2.hw2_team08.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The GeneEntrezWrapper class wraps the Gene Entrez dictionary. Given a gene name, it will
 * automatically retrieve the synonyms of this gene.
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class GeneEntrezWrapper {

  private static GeneEntrezWrapper instance = null;

  private HashMap<String, String[]> mGeneSynonymMap;

  private GeneEntrezWrapper() {
    mGeneSynonymMap = new HashMap<String, String[]>();
  }

  /* Singleton design */
  public static GeneEntrezWrapper getInstance() {
    if (instance == null) {
      instance = new GeneEntrezWrapper();
    }
    return instance;
  }

  /* Load a dict from the path, the dict has format "<word>\t<sym1> <sym2>...<symn>" */
  public void loadDict(String path) {
    
    URI dictPath;
    BufferedReader br;
    String line;

    try {
      dictPath = getClass().getClassLoader().getResource(path).toURI();
      File dictFile = new File(dictPath);

      br = new BufferedReader(new InputStreamReader(new FileInputStream(dictFile)));
      while ((line = br.readLine()) != null) {
        String[] splitTmp = line.split("\t");
        String gene = splitTmp[0].trim();
        String[] syms = splitTmp[1].split(" ");
        mGeneSynonymMap.put(gene, syms);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  /* Get the synonyms of a gene (if any). If the gene is not contained in the dict, returns null. */
  public List<String> getSynonyms(String gene) {
    String[] arrSyns = mGeneSynonymMap.get(gene);
    if (arrSyns != null)
      return Arrays.asList(arrSyns);

    return null;
  }

  public static void main(String[] args) throws Exception {
    GeneEntrezWrapper wrapper = GeneEntrezWrapper.getInstance();
    wrapper.loadDict("data/dict.txt");
    for (String s : wrapper.getSynonyms("GJ15797"))
      System.out.println(s);
  }
}
