package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * 
 * The class GeneEntrezQueryExpander uses the See <a
 * href="http://www.ncbi.nlm.nih.gov/gene">GeneEntrez</a> ontology to find the
 * similar gene names.
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */
public class GeneEntrezQueryExpander extends AbstractQueryExpander {

	/** The dictionary containing the similar gene names. */
	private HashMap<String, String[]> mGeneSynonymMap;

	@Override
	public boolean init(Properties prop) {
		mGeneSynonymMap = new HashMap<String, String[]>();
		String path = (String) prop.getProperty("dictionary");
		return loadDict(path);
	}

	/**
	 * Load a dictionary from the path. The dictionary should have format
	 * "<word>\t<sym1> <sym2>...<symn>"
	 * 
	 * @param path
	 *            the path of the dictionary.
	 * @return true, if successful
	 */
	private boolean loadDict(String path) {

		URI dictPath;
		BufferedReader br;
		String line;

		try {
			dictPath = getClass().getClassLoader().getResource(path).toURI();
			File dictFile = new File(dictPath);

			int num = 0;
			br = new BufferedReader(new FileReader(dictFile));
			while ((line = br.readLine()) != null) {
				String[] splitTmp = line.split("\t");
				String gene = splitTmp[0].trim();
				String[] syms = splitTmp[1].split(" ");
				mGeneSynonymMap.put(gene, syms);
				num++;
			}
			System.out.println("Processed " + num + " entries.");
			br.close();
		} catch (IOException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}

		return true;
	}

	/** The instance. */
	private static GeneEntrezQueryExpander instance = null;

	/**
	 * Gets the single instance of GeneEntrezQueryExpander.
	 * 
	 * @return single instance of GeneEntrezQueryExpander
	 */
	public static GeneEntrezQueryExpander getInstance() {
		if (instance == null) {
			instance = new GeneEntrezQueryExpander();
		}
		return instance;
	}

	@Override
	public List<String> expandQuery(String query, int size) {
		
	    String[] arrSyns = mGeneSynonymMap.get(query);
	    if (arrSyns == null)
	    	return null;
	   
	    List<String> expandedQueries = Arrays.asList(arrSyns);

		if (expandedQueries.size() > size)
			return expandedQueries.subList(0, size);

		return expandedQueries;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		AbstractQueryExpander expander = GeneEntrezQueryExpander.getInstance();
		Properties prop = new Properties();
		prop.setProperty("dictionary", "data/dict.txt");
		expander.init(prop);

		String query = "GJ15797";
		List<String> expandedQueries = expander.expandQuery(query, 100);

		for (String expandedQuery : expandedQueries) {
			System.out.println(expandedQuery);
		}
	}
}