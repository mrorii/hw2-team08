package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

/**
 * 
 * The ExpanderFactory class generates the query expander objcet automatically.
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class ExpanderFactory {

	/**
	 * Gets the query expander.
	 * 
	 * @param name
	 *            the expander's class name
	 * @return the query expander. If the query expander class is not found,
	 *         return null.
	 */
	public AbstractQueryExpander getQueryExpander(String name) {
		AbstractQueryExpander expander = null;

		try {
			expander = (AbstractQueryExpander) Class.forName(name)
					.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
		return expander;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {

	}

}
