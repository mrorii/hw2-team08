package edu.cmu.lti.f12.hw2.hw2_team08.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * The GeneLingPipeAnnotator class wraps the LingPipe ConfidenceChunker. It utilizes the wrapped
 * Chunker to annotate gene mentions in the document and convert the Chunker's output to {@link
 * <BaseAnnotation> [BaseAnnotation]} and adds them into the CAS's indexes. It can be configured
 * with the following parameters:
 * 
 * <ul>
 * <li><code>BestChunkNumber</code> - number of best chunks when annotating with LingPipe confidence
 * chunker</li>
 * <li><code>ModelPath</code> - number of best chunks when annotating with LingPipe confidence
 * chunker</li>
 * </ul>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class YuanGuKeytermExtractor extends AbstractKeytermExtractor {

  /**
   * Name of configuration parameter which set the Number of Best Chunk.
   */
  public static final String PARAM_BESTCHUNKNUMBER = "BestChunkNumber";

  /**
   * Name of configuration parameter which set the Model File.
   */
  public static final String PARAM_CONFIDENCETHRESHOLD = "ConfidenceThreshold";

  /**
   * Name of configuration parameter which set the Model File.
   */
  public static final String PARAM_MODELFILE = "ModelFile";

  /**
   * LingPipe ConfidenceChunker
   */
  private ConfidenceChunker mChunker;

  /**
   * Number of Best Chunk, used by LingPipe ConfidenceChunker
   */
  private int mBestChunkNumber;

  /**
   * Confidence Threshold used to filter low confidence annotations
   */
  private float mConfidenceThreshold;

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> keytermList = new ArrayList<Keyterm>();

    // add extracted keyterms
    Iterator<Chunk> it = mChunker.nBestChunks(question.toCharArray(), 0,
            question.toCharArray().length, mBestChunkNumber);
    while (it.hasNext()) {
      Chunk chunk = it.next();
      int start = chunk.start();
      int end = chunk.end();
      float confidence = (float) Math.pow(2.0, chunk.score());

      if (confidence > mConfidenceThreshold) {
        String keyterm = question.substring(start, end);
        keytermList.add(new Keyterm(keyterm));
      }
    }

    return keytermList;
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // get the number of best chunk candidates
    mBestChunkNumber = (Integer) aContext.getConfigParameterValue(PARAM_BESTCHUNKNUMBER);
    mConfidenceThreshold = Float.parseFloat((String) aContext
            .getConfigParameterValue(PARAM_CONFIDENCETHRESHOLD));
    String modelFilePath = (String) aContext.getConfigParameterValue(PARAM_MODELFILE);

    // initialize LingPipe ConfidenceChunker
    File modelFile;
    try {
      modelFile = new File(modelFilePath);
      mChunker = (ConfidenceChunker) AbstractExternalizable.readObject(modelFile);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceInitializationException(
              ResourceInitializationException.COULD_NOT_ACCESS_DATA,
              new Object[] { "LingPipeGeneTagModel" }, e);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new ResourceInitializationException(e);
    }
  }
}