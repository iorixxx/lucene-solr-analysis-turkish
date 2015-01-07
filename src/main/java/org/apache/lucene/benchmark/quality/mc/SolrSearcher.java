package org.apache.lucene.benchmark.quality.mc;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class SolrSearcher implements Closeable {

  private static final String solrURL = "http://localhost:8983/solr/";

  public SolrSearcher(String coreName, String outputPath, String queryCSV) throws IOException {

  server = new HttpSolrServer(solrURL+coreName);

  this.outputPath = Paths.get(outputPath, coreName);
    if (!Files.exists(this.outputPath))
       Files.createDirectories(this.outputPath);
    topics = getTopics(queryCSV);
  }

    @Override
    public void close() throws IOException {
        server.shutdown();
    }

  static class Topic {

    final String id;
    final String title;
    final String desc;

    public Topic(String id, String title, String desc) {
      this.id = id;
      this.title = title;
      this.desc = desc;
    }
  }

  static enum QueryLength {
    Short, Medium, QMS;

    @Override
    public String toString() {
      switch (this) {
        case Short:
          return "QS";
        case Medium:
          return "QM";
        case QMS:
          return "QMS";
        default:
          throw new AssertionError(this);
      }
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

  private final HttpSolrServer server;
  private String defaultField = null;
  private final Path outputPath;
  private final List<Topic> topics;

  private PrintWriter output;

  private QueryLength queryLength = QueryLength.Medium;

  public static List<Topic> getTopics(String queryCSV) throws IOException {

    List<Topic> topics = new ArrayList<>();

    List<String> lines = Files.readAllLines(Paths.get(queryCSV), StandardCharsets.UTF_8);

    for (String line : lines) {
      String[] parts = line.split("\t");
      if (parts.length != 3) throw new RuntimeException("line should have three parts " + Arrays.toString(parts));

      String id = parts[0];
      String title = parts[1];
      String desc = parts[2];
      Topic topic = new Topic(id, title, desc);
      topics.add(topic);
    }

    lines.clear();
    return topics;
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
    query.setSort("score", SolrQuery.ORDER.desc);
    query.addSort("id", SolrQuery.ORDER.asc);

    int i = 0;

    for (SolrDocument document : server.query(query).getResults()) {

      String docno = (String) document.getFieldValue("id");
      Float score = (Float) document.getFieldValue("score");

      output.println(QueryID + "\tQ0\tMilliyet_0105_v00_" + docno.trim() + "\t" + i + "\t" + score + "\t" + getRunName());
      i++;
    }

  }

  public String search() throws SolrServerException, IOException {

    output = new PrintWriter(Files.newBufferedWriter(
              outputPath.resolve(getRunName() + "_submitted.txt"),
              StandardCharsets.US_ASCII));

    for (Topic topic : topics) {

      if (queryLength == QueryLength.Short)
        search(topic.title, topic.id);
      else if (queryLength == QueryLength.Medium)
        search(topic.desc, topic.id);
      else if (queryLength == QueryLength.QMS)
        search(topic.title + " " + topic.desc, topic.id);
    }
    output.flush();
    output.close();
    return getRunName();

  }

  public String searchAsciify() throws SolrServerException, IOException {

      output = new PrintWriter(Files.newBufferedWriter(
              outputPath.resolve(getRunName() + "_submitted.txt"),
              StandardCharsets.US_ASCII));

    for (Topic topic : topics) {

      if (queryLength == QueryLength.Short)
        search(asciifyAndLowerCase(topic.title), topic.id);
      else if (queryLength == QueryLength.Medium)
        search(asciifyAndLowerCase(topic.desc), topic.id);
      else if (queryLength == QueryLength.QMS)
        search(asciifyAndLowerCase(topic.title + " " + topic.desc), topic.id);
    }
    output.flush();
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
    return builder.toString();
  }

  public static String asciifyAndLowerCase(String string) {
    return asciify(string).toLowerCase(Locale.US);
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

  public static void main(String[] args) throws SolrServerException, IOException {

      for(final String core: new String []{"catA", "catB"})

       try(SolrSearcher searcher = new SolrSearcher(core,
        "/Volumes/data/diacritics/",
        "/Users/iorixxx/Dropbox/queries.csv/")
       ) {

              for (final QueryLength queryLength : new QueryLength[]{QueryLength.Short, QueryLength.Medium}) {

                  searcher.setQueryLength(queryLength);

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
          }
  }
}
