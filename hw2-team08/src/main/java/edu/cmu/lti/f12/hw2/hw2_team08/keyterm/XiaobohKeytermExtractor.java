package edu.cmu.lti.f12.hw2.hw2_team08.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class XiaobohKeytermExtractor extends AbstractKeytermExtractor {
  
  ConfidenceChunker chunker = null;
  
  public static final String PARAM_MODELFILE = "model_file";

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
  
  
    File modelFile = new File(((String) aContext.getConfigParameterValue(PARAM_MODELFILE)).trim());
      
  if (!modelFile.exists()) {
    throw new ResourceInitializationException();
  }
  
  try {
    chunker = (ConfidenceChunker)AbstractExternalizable.readObject(modelFile);

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  
  }
  
  
  protected List<Keyterm> getKeyterms(String question) {
    
    List<Keyterm> keytermList = new ArrayList<Keyterm>();
       
    char[] cs = question.toCharArray();
    Iterator<Chunk> it= chunker.nBestChunks(cs,0,question.length(),6);
    
    int start = 0;
    int end = 0;
    int pos = 0;
    
    for (int n = 0; it.hasNext(); ++n) 
    {
        Chunk chunk = it.next();
        double conf = Math.pow(2.0,chunk.score());
        if(conf*Math.pow(10, 11) < Math.pow(10, 10))
        {
          if(6 == n)
            pos = question.length();
          continue;
        }
        
        start = chunk.start();
        end = chunk.end();
        
        String keyterm = question.substring(start, end);
        keytermList.add(new Keyterm(keyterm));
    }

    return keytermList;
  }

}
