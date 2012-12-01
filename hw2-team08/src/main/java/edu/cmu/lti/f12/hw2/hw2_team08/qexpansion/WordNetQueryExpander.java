package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import edu.mit.jwi.*;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNetQueryExpander extends AbstractQueryExpander {

  // Use singleton pattern
  private IDictionary dict;
  private WordnetStemmer stemmer;

  private WordNetQueryExpander() {
    dict = null;
  }

  public boolean init(String dictPath) {
    URL url;
    try {
      url = new URL("file", null, dictPath);
      dict = new Dictionary(url);
      dict.open();
      stemmer = new WordnetStemmer(dict);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private static WordNetQueryExpander instance = null;

  public static WordNetQueryExpander getInstance() {
    if (instance == null) {
      instance = new WordNetQueryExpander();
    }
    return instance;
  }

  /* returns null if no similar words found */
  @Override
  public List<String> expandQuery(String query) {
    
    List<String> stems = stemmer.findStems(query, POS.VERB);
    if (stems == null)
      return null;
    
    IIndexWord idxWord = dict.getIndexWord(stems.get(0), POS.VERB);
    if (idxWord == null)
      return null;
    
    IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning
    IWord word = dict.getWord(wordID);
    ISynset synset = word.getSynset();
    List<IWord> words = synset.getWords();
    List<String> expandedQueries = new ArrayList<String>(words.size());
    for (IWord w : synset.getWords())
      expandedQueries.add(w.getLemma());

    return expandedQueries;
  }

  public static void main(String[] args) throws IOException {
    WordNetQueryExpander expander = WordNetQueryExpander.getInstance();
    expander.init("src/main/resources/data/wordnet-dict");

    String query = "affected";
    List<String> expandedQueries = expander.expandQuery(query);

    for (String expandedQuery : expandedQueries) {
      System.out.println(expandedQuery);
    }
  }
}
