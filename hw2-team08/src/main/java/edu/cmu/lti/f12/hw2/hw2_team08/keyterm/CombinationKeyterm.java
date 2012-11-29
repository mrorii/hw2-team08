package edu.cmu.lti.f12.hw2.hw2_team08.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CombinationKeyterm extends AbstractKeytermExtractor {
  StanfordCoreNLP pipeline = null;

  Set<String> stoptermsSet = new HashSet<String>();

  Map<String, Integer> termStatMap = new HashMap<String, Integer>();

  public static final String PARAM_MODELFILE = "model_file";

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    init();
  }

  public CombinationKeyterm() {
    super();
    init();
  }
  
  private void init() {
    
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
    
    stoptermsSet.add("role");
    stoptermsSet.add("do");
    termStatMap.put("NN", 1); // 1 : entity
    termStatMap.put("NNP", 1); // 1 : entity
    termStatMap.put("NNS", 1);
    termStatMap.put("JJ", 2); // 2 : prefix
    termStatMap.put("VBG", 2);
    termStatMap.put("POS", 3); // 3 : conjunction
    termStatMap.put("VBP", 11);
  }

  @Override
  public List<Keyterm> getKeyterms(String text) {

    List<Keyterm> keytermList = new ArrayList<Keyterm>();

    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      List<Integer> statlist = new ArrayList<Integer>();
      List<Integer> beginList = new ArrayList<Integer>();
      List<Integer> endList = new ArrayList<Integer>();
      List<CoreLabel> candidate = new ArrayList<CoreLabel>();
      int i = 0;
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        String pos = token.get(PartOfSpeechAnnotation.class);
        int stat = 0;
        if (termStatMap.containsKey(pos)) {
          stat = termStatMap.get(pos);
        }
        statlist.add(stat);

        if (stat != 0) {
          if (i > 0) {
            int statClass = statlist.get(i - 1) / 10;
            if (statClass != 0 && statClass != stat / 10)
              popout(candidate, beginList, endList);
          }
          candidate.add(token);

        } else {
          popout(candidate, beginList, endList);
        }

        i++;
      }
      popout(candidate, beginList, endList);
      // System.out.println(begin2end.size());
      for (int j = 0; j < beginList.size(); j++) {
        String substring = text.substring(beginList.get(j), endList.get(j));
        if (!stoptermsSet.contains(substring)) {
          keytermList.add(new Keyterm(substring));
          System.out.println("--" + substring);
        }
      }
      // for(int s : statlist) {
      // System.out.print(s + " ");
      // }
      // System.out.println();

    }

    return keytermList;
  }

  private void popout(List<CoreLabel> candidate, List<Integer> beginList, List<Integer> endList) {
    if (candidate.size() > 0) {
      int begin = candidate.get(0).beginPosition();
      int end = candidate.get(candidate.size() - 1).endPosition();
      beginList.add(begin);
      endList.add(end);
      candidate.clear();
    }
  }

  public static void main(String[] args) {
    CombinationKeyterm t = new CombinationKeyterm();
    String file = CombinationKeyterm.class.getResource("/input/trecgen06.txt").getFile();
    File f = new File(file);
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    }
    String line = "";
    try {
      while ((line = reader.readLine()) != null) {
        String query = line.split("\\|")[1];
//        System.out.println(line);
//        System.out.println(t.getKeyterms(query));

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}