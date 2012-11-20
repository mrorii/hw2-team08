package edu.cmu.lti.f12.hw2.hw2_team08.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * The GoogleCustomSearchWrapper class wraps the Google Custom Search Service. Given a query
 * string, it will automatically retrieve the documents from Google Custom Search Service.
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class GoogleCustomSearchWrapper {

  private String mApiURL = "https://www.googleapis.com/customsearch/v1";

  private String mApiKey = "AIzaSyC4SE1N3aJHHaSFAo-pGDWbRiT9WyhqEhk";

  private String mEngineIdentifier = "003682192644851693078:ji5d_7vbyv4";

  private final HttpClient mHttpClient = new DefaultHttpClient();

  public GoogleCustomSearchWrapper(String apiURL, String key, String engineIdentifier) {
    System.out.println(apiURL);
    System.out.println(key);
    System.out.println(engineIdentifier);
    mApiKey = apiURL;
    mApiKey = key;
    mEngineIdentifier = engineIdentifier;
  }



  public String getDocText(String url) throws Exception {

    HttpGet request = new HttpGet(url);
    HttpResponse response = mHttpClient.execute(request);

    // Get the response
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    StringBuilder sb = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      sb.append(line);
    }

    return sb.toString();
  }

  public ArrayList<String> getDocuments(String query) throws Exception {
    ArrayList<String> documents = new ArrayList<String>();

    ArrayList<String> urls = getDocumentURLs(query);
    for (String url : urls) {
      String doc = getDocText(url);
      documents.add(doc);
    }

    System.out.println(documents.size());
    return documents;
  }

  public ArrayList<String> getDocumentURLs(String query) throws Exception {

    String url = mApiURL + "?key=" + mApiKey + "&cx=" + mEngineIdentifier + "&q="
            + query.replace(" ", "%20") + "&alt=json";

    System.out.println(url);
    HttpGet request = new HttpGet(url);
    HttpResponse response = mHttpClient.execute(request);

    // Get the response
    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }

    JsonParser parser = new JsonParser();
    JsonObject obj = parser.parse(sb.toString()).getAsJsonObject();
    JsonArray itemArray = obj.getAsJsonArray("items");

    ArrayList<String> urlArray = new ArrayList<String>();

    for (JsonElement item : itemArray) {
      JsonObject itemObj = item.getAsJsonObject();
      String link = itemObj.get("link").getAsString();
      if (!(link.endsWith("pdf") || link.endsWith("ppt") || link.endsWith("pptx")))
        urlArray.add(link);
    }

    return urlArray;
  }

  public void close() {
    // TODO Auto-generated method stub
  }

  public static void main(String[] args) throws Exception {
    GoogleCustomSearchWrapper wrapper = new GoogleCustomSearchWrapper(
            "https://www.googleapis.com/customsearch/v1",
            "AIzaSyC4SE1N3aJHHaSFAo-pGDWbRiT9WyhqEhk", "003682192644851693078:ji5d_7vbyv4");

    String doc = wrapper.getDocText("http://www.regenerativedesign.org/courses-events/rdna");
    String text = Jsoup.parse(doc).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
    System.out.println(text);
  }

}
