package edu.cmu.lti.f12.hw2.hw2_team08.qexpansion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.InputSource;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.lingmed.mesh.Mesh;
import com.aliasi.lingmed.mesh.MeshConcept;
import com.aliasi.lingmed.mesh.MeshParser;
import com.aliasi.util.Strings;

public class MeshQueryExpansionIndexer {

  public static void main(String[] args) throws IOException {
    Properties prop = new Properties();
    InputStreamReader isr = new InputStreamReader(
            new FileInputStream("qexpansion.properties"), "UTF-8");
    prop.load(isr);
    
    String indexDir = prop.getProperty("mesh_index_dir");
    String meshGzipPath = prop.getProperty("mesh_rawfile");
    
    new MeshQueryExpansionIndexer(indexDir, meshGzipPath).index();
  }

  private String indexDir;
  private String meshGzipPath;
  
  public MeshQueryExpansionIndexer(String indexDir, String meshGzipPath) {
    this.indexDir = indexDir;
    this.meshGzipPath = meshGzipPath;
  }
  
  public void index() throws IOException {
    Directory dir = FSDirectory.open(new File(indexDir));
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
    iwc.setOpenMode(OpenMode.CREATE);
    IndexWriter writer = new IndexWriter(dir, iwc);

    MeshParser parser = new MeshParser();
    MeshHandler handler = new MeshHandler(writer);
    parser.setHandler(handler);

    File meshGzipFile = new File(meshGzipPath);
    InputStream fileIn = new FileInputStream(meshGzipFile); 
    InputStream gzipIn = new GZIPInputStream(fileIn);
    InputSource inSource = new InputSource(gzipIn); 
    inSource.setEncoding(Strings.UTF8);
    
    parser.parse(inSource);
    writer.close();
    System.out.println("Done indexing MeSH");
  }
  
  static class MeshHandler implements ObjectHandler<Mesh> {
    private IndexWriter writer;
    private MeshConceptHelper conceptHelper;

    public MeshHandler(IndexWriter writer) {
      super();
      this.writer = writer;
      this.conceptHelper = MeshConceptHelper.getInstance();
    }

    @Override
    public void handle(Mesh mesh) {
      Document doc = new Document();
      
      Field descriptorUIField = new Field("descriptorUI", mesh.descriptor().ui(),
              Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
      doc.add(descriptorUIField);
      
      Field descriptorField = new Field("descriptor", mesh.descriptor().name(),
              Field.Store.YES, Field.Index.ANALYZED);
      doc.add(descriptorField);
      
      // Store synonym(s)
      List<MeshConcept> conceptList = mesh.conceptList();
      for (MeshConcept meshConcept : conceptList) {
        Field synonymField = new Field("synonym", meshConcept.conceptNameUi().name(),
                Field.Store.YES, Field.Index.ANALYZED);
        doc.add(synonymField);
      }
      
      // Store hyponym(s)/hypernym
      List<String> treeNumbers = mesh.treeNumberList();
      for (String treeNumber : treeNumbers) {
        String hypernym = this.conceptHelper.getHypernym(treeNumber);
        List<String> hyponyms = this.conceptHelper.getHyponyms(treeNumber);
        
        Field hypernymField = new Field("hypernym", hypernym, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(hypernymField);
        
        for (String hyponym : hyponyms) {
          Field hyponymField = new Field("hyponym", hyponym, Field.Store.YES, Field.Index.ANALYZED);
          doc.add(hyponymField);
        }
      }
      
      try {
        writer.addDocument(doc);
      } catch (CorruptIndexException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
