package edu.cmu.lti.f12.hw2.hw2_team08.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * @author Andrew Rodriguez (amr1)
 */
public class Amr1KeytermExtractor extends AbstractKeytermExtractor {

  private StanfordCoreNLP pipeline;

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    // my initialization
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> keytermList = new ArrayList<Keyterm>();

    String text = question;

    Map<Integer, Integer> begin2end = new HashMap<Integer, Integer>();
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      List<CoreLabel> candidate = new ArrayList<CoreLabel>();
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        String pos = token.get(PartOfSpeechAnnotation.class);
        if (pos.startsWith("NN") || isAcronym(token.originalText())
                || likelyBeginning(token.originalText())) {
          candidate.add(token);
        } else if (candidate.size() > 0) {
          int begin = candidate.get(0).beginPosition();
          int end = candidate.get(candidate.size() - 1).endPosition();
          begin2end.put(begin, end);
          candidate.clear();
        }
      }
      if (candidate.size() > 0) {
        int begin = candidate.get(0).beginPosition();
        int end = candidate.get(candidate.size() - 1).endPosition();
        begin2end.put(begin, end);
        candidate.clear();
      }
    }

    for (Entry<Integer, Integer> entry : begin2end.entrySet()) {
      keytermList.add(new Keyterm(text.substring(entry.getKey(), entry.getValue())));
    }

    return keytermList;

  }

  String[] likelyWords = new String[] { "chain", "monomer", "codon", "region", "exon", "orf",
      "cdna", "reporter", "gene", "antibody", "complex", "gene", "product", "mrna", "oligomer",
      "chemokine", "subunit", "peptide", "message", "transactivator", "homolog", "binding", "site",
      "enhancer", "element", "allele", "isoform", "intron", "promoter", "operon" };

  private boolean likelyBeginning(String originalText) {
    return Arrays.asList(likelyWords).contains(originalText);
  }

  private boolean isAcronym(String originalText) {

    // all upper case?
    boolean upper = true;
    for (int i = 0; i < originalText.length(); i++) {
      char c = originalText.charAt(i);
      if (!Character.isUpperCase(c)) {
        upper = false;
        break;
      }
    }

    return upper;
  }
}
