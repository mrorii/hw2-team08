package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class MeshQueryExpander extends AbstractQueryExpander {

  private IndexSearcher searcher;
  private StandardAnalyzer analyzer;
  private IndexReader reader;

  // Use singleton pattern
  private MeshQueryExpander() throws CorruptIndexException, IOException {
    
    Properties prop = new Properties();
    InputStreamReader isr = new InputStreamReader(
            new FileInputStream("qexpansion.properties"), "UTF-8");
    prop.load(isr);
    
    String indexDir = prop.getProperty("mesh_index_dir");
    this.reader = IndexReader.open(FSDirectory.open(new File(indexDir)));
    this.searcher = new IndexSearcher(reader);
    this.analyzer = new StandardAnalyzer(Version.LUCENE_36);
  }
  
  private static MeshQueryExpander instance = null;

  public static MeshQueryExpander getInstance() {
    if (instance == null) {
      try {
        instance = new MeshQueryExpander();
      } catch (CorruptIndexException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return instance;
  }
  
  @Override
  public List<String> expandQuery(String q) {
    List<String> retval = new ArrayList<String>();
    
    try {
      QueryParser parser = new QueryParser(Version.LUCENE_36, "synonym", analyzer);
      Query query = parser.parse(q);
      
      TopDocs results = searcher.search(query, null, 10);
      ScoreDoc[] hits = results.scoreDocs;
      
      int numTotalHits = results.totalHits;
      if (numTotalHits == 0) {
        return retval;
      }
      
      Document doc = searcher.doc(hits[0].doc);
      String[] synonyms = doc.getValues("synonym");
      for (String synonym : synonyms) {
        retval.add(synonym);
      }
      
      String hypernym = doc.get("hypernym");
      retval.add(hypernym);
      
      String[] hyponyms = doc.getValues("hyponym");
      for (String hyponym : hyponyms) {
        retval.add(hyponym);
      }
      
    } catch (CorruptIndexException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    
    return retval;
  }
  
  public static void main(String[] args) {
    MeshQueryExpander expander = MeshQueryExpander.getInstance();
    
    String query = "head";
    List<String> expandedQueries = expander.expandQuery(query);
    
    for (String expandedQuery : expandedQueries) {
      System.out.println(expandedQuery);
    }
  }
}
