package edu.cmu.lti.f12.hw2.hw2_team08.passage;

import java.util.LinkedList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;

/**
 * 
 * This class builds on top of the PassageCandiateFinder in the openqa package but adds some of the
 * improvements found in the "Knowledge-intesive Conceptual Retrieval and Passage Extraction of
 * Biomedical Literature" paper which has lead to some insights used in other packages.
 * 
 * Similar to the paper, we will make sure that the
 * 
 * @author amr1 (Andrew Rodriguez)
 * 
 */
public class NoOverlapsPassageCandidateFinder extends PassageCandidateFinder {

  public NoOverlapsPassageCandidateFinder(String docId, String text, KeytermWindowScorer scorer) {
    super(docId, text, scorer);
  }

  public List<PassageCandidate> extractPassages(String[] keyterms) {

    List<PassageCandidate> passages = super.extractPassages(keyterms);

    List<PassageCandidate> toIgnore = new LinkedList<PassageCandidate>();
    List<PassageCandidate> toAdd = new LinkedList<PassageCandidate>();

    // 1. If we've got two passages such that one consumes the other, then
    // we return the smaller one (should have all the necessary information)
    // 2. If two passages are budding, then we make a new passage that incorporates both of them
    for (PassageCandidate pc1 : passages) {
      for (PassageCandidate pc2 : passages) {
        if (pc1.equals(pc2))
          break;
        if (pc1.getStart() >= pc2.getStart() && pc1.getEnd() <= pc2.getEnd()) {
          toIgnore.add(pc2);
        } else {
          try {
            if (pc1.getStart() == pc2.getEnd()) {
              toAdd.add(new PassageCandidate(pc1.getDocID(), pc2.getStart(), pc1.getEnd(), Math
                      .max(pc1.getProbability(), pc2.getProbability()), pc1.getQueryString()));
              toIgnore.add(pc1);
              toIgnore.add(pc2);
            } else if (pc1.getEnd() == pc2.getStart()) {
              toAdd.add(new PassageCandidate(pc1.getDocID(), pc1.getStart(), pc2.getEnd(), Math
                      .max(pc1.getProbability(), pc2.getProbability()), pc1.getQueryString()));
              toIgnore.add(pc1);
              toIgnore.add(pc2);
            }
          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
        }
      }
    }

    passages.removeAll(toIgnore);

    return passages;
  }
}
