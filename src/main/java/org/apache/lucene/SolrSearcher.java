package org.apache.lucene;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;


public class SolrSearcher {

  public SolrSearcher(String solrURL, String outputPath, String queryCSV) throws IOException {

    server = new HttpSolrServer(solrURL);

    this.outputPath = outputPath;

    List<String> lines = Files.readAllLines(new File(queryCSV).toPath(), StandardCharsets.UTF_8);

    for (String line : lines) {
      String[] parts = line.split("\t");
      if (parts.length != 3) throw new RuntimeException("line should have three parts " + Arrays.toString(parts));

      String id = parts[0];
      String title = parts[1];
      String desc = parts[2];
      Topic topic = new Topic(id, title, desc);
      topics.add(topic);
    }
  }

  public enum QueryLength {
    Short, Medium, QMS;

    public String toString() {
      switch (this) {
        case Short:
          return "QS";
        case Medium:
          return "QM";
        case QMS:
          return "QMS";
      }
      return super.toString();
    }
  }

  /**
   * Constant <code>TURKISH_CHARACTERS={'\u00e7', '\u011f', '\u0131', '\u00f6', '\u015f', '\u00fc', '\u00c7', '\u011e', '\u0130', '\u00d6', '\u015e', '\u00dc'}</code>
   */
  public static final char[] TURKISH_CHARACTERS = {'\u00e7', '\u011f', '\u0131', '\u00f6', '\u015f', '\u00fc', '\u00c7', '\u011e', '\u0130', '\u00d6', '\u015e', '\u00dc'};
  /**
   * Constant <code>ENGLISH_CHARACTERS={'c', 'g', 'i', 'o', 's', 'u', 'C', 'G', 'I', 'O', 'S', 'U'}</code>
   */
  public static final char[] ENGLISH_CHARACTERS = {'c', 'g', 'i', 'o', 's', 'u', 'C', 'G', 'I', 'O', 'S', 'U'};

  private static final Map<Character, Character> characterMap = new HashMap<>();

  private HttpSolrServer server;
  private String defaultField = null;
  private final String outputPath;

  private ArrayList<Topic> topics = new ArrayList<>();

  private PrintWriter output;

  private QueryLength queryLength = QueryLength.Medium;

  public List<Topic> getTopics() {
    return this.topics;
  }

  public void setQueryLength(QueryLength queryLength) {
    this.queryLength = queryLength;
  }

  public void setDefaultField(String defaultField) {
    this.defaultField = defaultField;
  }

  public String getRunName() {
    return defaultField + "_" + queryLength.toString();
  }

  private void search(String q, String QueryID) throws SolrServerException {

    SolrQuery query = new SolrQuery(q.replace("?", ""));
    query.setParam(CommonParams.DF, defaultField);
    query.setFields("id", "score");
    query.setStart(0);
    query.setRows(1000);

    int i = 0;

    for (SolrDocument document : server.query(query).getResults()) {

      String docno = (String) document.getFieldValue("id");
      Float score = (Float) document.getFieldValue("score");

      output.println(QueryID + "\t0\tMilliyet_0105_v00_" + docno.trim() + "\t" + i + "\t" + score + "\tSTANDARD");
      i++;
    }

  }

  public String search() throws SolrServerException, FileNotFoundException {

    output = new PrintWriter(outputPath + getRunName() + "_submitted.txt");

    for (Topic topic : topics) {

      if (queryLength == QueryLength.Short)
        search(topic.title, topic.id);
      else if (queryLength == QueryLength.Medium)
        search(topic.desc, topic.id);
      else if (queryLength == QueryLength.QMS)
        search(topic.title + " " + topic.desc, topic.id);
    }
    output.close();
    return getRunName();

  }

  public String searchAsciify() throws SolrServerException, FileNotFoundException {

    output = new PrintWriter(outputPath + getRunName() + "_submitted.txt");

    for (Topic topic : topics) {

      if (queryLength == QueryLength.Short)
        search(asciify(topic.title), topic.id);
      else if (queryLength == QueryLength.Medium)
        search(asciify(topic.desc), topic.id);
      else if (queryLength == QueryLength.QMS)
        search(asciify(topic.title + " " + topic.desc), topic.id);
    }
    output.close();
    return getRunName();

  }

  public static String asciify(String string) {

    StringBuilder builder = new StringBuilder(string.length());

    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);

      if (characterMap.containsKey(c))
        builder.append(characterMap.get(c));
      else
        builder.append(c);


    }

    return builder.toString().toLowerCase(Locale.US);

  }

  static {
    for (int j = 0; j < TURKISH_CHARACTERS.length; j++)
      characterMap.put(TURKISH_CHARACTERS[j], ENGLISH_CHARACTERS[j]);


  }

  private static void printAccentedLettersTable() {

    System.out.println(characterMap.keySet());
    System.out.println(characterMap.values());

    for (char c : TURKISH_CHARACTERS) {
      System.out.print("\\symbol{");
      System.out.print((int) c);
      System.out.print("} & ");
    }

    for (char c : ENGLISH_CHARACTERS) {
      System.out.print("\\symbol{");
      System.out.print((int) c);
      System.out.print("} & ");
    }
  }

  static final String[] deasciifiers = {"zemberek2_deascii", "turkish_deascii"};
  static final String[] stemmers = {"ns", "f5", "snowball", "zemberek2"};

  /**
   * Reads output of trec_eval program, and creates performance metric LateX table
   *
   * @param metric      map, P@5, etc.
   * @param queryLength Medium, Short, etc.
   * @param outputPath  directory where trec_eval's outputs are saved.
   * @throws IOException
   */
  public static void printMetricTable(String metric, SolrSearcher.QueryLength queryLength, String outputPath) throws IOException {

    for (String stemmer : stemmers) {

      System.out.print(stemmer + " & ");

      String fileName = "out_" + "tr_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");


      fileName = "out_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");

      int i = 1;
      for (String deasciifier : deasciifiers) {
        fileName = "out_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        System.out.print(getMetric(metric, outputPath + fileName));

        i++;

        if (i == deasciifiers.length )
          System.out.print(" & ");
        else
          System.out.print(" \\\\ ");

      }

      System.out.println();
      System.out.println("\\hline");
    }
  }

  static String getMetric(String metric, String fileName) throws IOException {
    List<String> lines = Files.readAllLines(new File(fileName).toPath(), StandardCharsets.US_ASCII);

    for (String line : lines) {
      if (line.startsWith(metric) && line.contains("all")) {
        String[] parts = line.split("\\s+");
        if (parts.length != 3) throw new RuntimeException("line should have three parts " + Arrays.toString(parts));
        return parts[2];
      }
    }

    throw new RuntimeException(metric + " metric cannot be found!");
  }

  public static void main(String[] args) throws SolrServerException, IOException {

    printMetricTable("bpref", QueryLength.Medium, "/Users/iorixxx/Dropbox/diacritic/");

   // if (true) return;

    SolrSearcher searcher = new SolrSearcher(
        "http://localhost:8983/solr/deascii/",
        "/Users/iorixxx/Dropbox/diacritic/",
        "/Users/iorixxx/Dropbox/queries.csv/"
    );
    searcher.setQueryLength(QueryLength.Medium);


    for (String stemmer : stemmers) {

      searcher.setDefaultField("tr_" + stemmer);
      System.out.println(searcher.search());

      searcher.setDefaultField("ascii_" + stemmer);
      System.out.println(searcher.searchAsciify());

      for (String deasciifier : deasciifiers) {
        searcher.setDefaultField(deasciifier + "_" + stemmer);
        System.out.println(searcher.searchAsciify());
      }
    }
  }

  public static class Topic {

    final String id;
    final String title;
    final String desc;

    public Topic(String id, String title, String desc) {
      this.id = id;
      this.title = title;
      this.desc = desc;
    }
  }
}
