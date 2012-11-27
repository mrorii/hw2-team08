package edu.cmu.lti.f12.hw2.hw2_team08.retrieval;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.f12.hw2.hw2_team08.qexpansion.MeshQueryExpander;
import edu.cmu.lti.f12.hw2.hw2_team08.util.GeneEntrezWrapper;
import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * 
 * @author Yuan Gu <yuangu@andrew.cmu.edu>
 * 
 */
public class QueryExpansionRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapper wrapper;

  private MeshQueryExpander meshQueryExpander;

  private GeneEntrezWrapper geneEntrezWrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

    this.meshQueryExpander = MeshQueryExpander.getInstance();
    this.geneEntrezWrapper = GeneEntrezWrapper.getInstance();
    String geneEntrezDictPath = (String) aContext.getConfigParameterValue("gene-dict-path");
    this.geneEntrezWrapper.loadDict(geneEntrezDictPath);

  }

  @Override
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
    String query = formulateQuery(keyterms);
    return retrieveDocuments(query);
  }

  protected String formulateQuery(List<Keyterm> keyterms) {
    String query = "";

    for (Keyterm keyterm : keyterms) {
      String strKeyterm = keyterm.getText();
      List<String> expandedKeyterms = new ArrayList<String>();
      List<String> geneEntrezExpandedKeyterms = this.geneEntrezWrapper.getSynonyms(strKeyterm);
      if (geneEntrezExpandedKeyterms != null)
        expandedKeyterms.addAll(geneEntrezExpandedKeyterms);

      List<String> meshExpandedKeyterms = this.meshQueryExpander.expandQuery(strKeyterm);
      if (meshExpandedKeyterms != null)
        expandedKeyterms.addAll(meshExpandedKeyterms);

      String queryComponent = "(" + strKeyterm;
      for (String strExpandedTerm : expandedKeyterms) {
        queryComponent += " OR " + strExpandedTerm;
      }

      queryComponent += ")";

      query += queryComponent + " AND ";
    }

    /* remove the last " AND " */
    query = query.substring(0, query.length() - 5);

    System.out.println(" I was called QUERY: " + query);
    return query;
  }

  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        System.out.println(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }
}