package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.util.List;
import java.util.Properties;

/**
 * The Class AbstractQueryExpander defines the behavior of a query expander. All
 * query expander should inherit this class.
 */
public abstract class AbstractQueryExpander {

	/**
	 * Get the expanded query for a query.
	 * 
	 * @param query
	 *            the query, typically a keyterm or a key phrase.
	 * @param size
	 *            maximum size of the returned terms.
	 * @return the list of expanded terms.
	 */
	abstract public List<String> expandQuery(String query, int size);

	/**
	 * Initial the expander.
	 * 
	 * @param prop
	 *            contains the properties a expander need to init.
	 * @return true, if successful
	 */
	abstract public boolean init(Properties prop);
}
