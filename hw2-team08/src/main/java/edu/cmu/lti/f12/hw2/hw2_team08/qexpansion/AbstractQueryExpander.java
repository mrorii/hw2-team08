package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.util.List;
import java.util.Properties;

public abstract class AbstractQueryExpander {
  abstract public List<String> expandQuery(String query, int size);

  abstract public boolean init(Properties prop);
}
