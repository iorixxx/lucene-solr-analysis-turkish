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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.apache.lucene.benchmark.quality.mc.SolrSearcher.*;


/**
 * Evaluation class processes trec_eval outputs
 */
public class Evaluator {

  public enum Metric {
    NCDG, ERR
  }

  /**
   * Reads output of trec_eval program, and creates performance metric LateX table
   *
   * @param metric      map, P@5, bpref etc.
   * @param queryLength Medium, Short, etc.
   * @param outputPath  directory where trec_eval's outputs are saved.
   * @throws java.io.IOException
   */
  public static void printTrecEvalMetricTable(String metric, SolrSearcher.QueryLength queryLength, String outputPath) throws IOException {

    for (String stemmer : stemmers) {

      if (stemmer.length() < 3)
        System.out.print(stemmer + " \t\t\t& ");
      else
        System.out.print(stemmer + " \t& ");

      String fileName = "out_" + "tr_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");


      fileName = "out_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");

      int i = 1;
      for (String deasciifier : deasciifiers) {
        fileName = "out_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        System.out.print(getMetric(metric, outputPath + fileName));

        i++;

        if (i == deasciifiers.length)
          System.out.print(" & ");
        else
          System.out.print(" \\\\ ");

      }

      System.out.println();
      System.out.println("\\hline");
    }
  }

  /**
   * Reads output of gdeval.pl script, and creates performance metric LateX table
   *
   * @param metric      ndcg@20,err@20.
   * @param queryLength Medium, Short, etc.
   * @param outputPath  directory where gdeval.pl's outputs are saved.
   * @throws IOException
   */
  public static void printGDEvalMetricTable(Metric metric, SolrSearcher.QueryLength queryLength, String outputPath) throws IOException {

    for (String stemmer : stemmers) {

      if (stemmer.length() < 3)
        System.out.print(stemmer + " \t\t\t& ");
      else
        System.out.print(stemmer + " \t& ");

      String fileName = "gdeval_" + "tr_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");


      fileName = "gdeval_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");

      int i = 1;
      for (String deasciifier : deasciifiers) {
        fileName = "gdeval_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        System.out.print(getMetric(metric, outputPath + fileName));

        i++;

        if (i == deasciifiers.length)
          System.out.print(" & ");
        else
          System.out.print(" \\\\ ");

      }

      System.out.println();
      System.out.println("\\hline");
    }
    System.out.println("-------------------------------------------------------------");
  }

  /**
   * Reads output of gdeval.pl script, and creates performance metric LateX table
   *
   * @param metric      ndcg@20, err@20.
   * @param queryLength Medium, Short, etc.
   * @param outputPath  directory where gdeval.pl's outputs are saved.
   * @throws IOException
   */
  public static void printRiskTable(Metric metric, SolrSearcher.QueryLength queryLength, String outputPath) throws IOException {

    for (String stemmer : stemmers) {

      if (stemmer.length() < 3)
        System.out.print(stemmer + " \t\t\t& ");
      else
        System.out.print(stemmer + " \t& ");

      String fileName = "risk_sensitive_gdeval_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, outputPath + fileName) + " & ");

      int i = 1;
      for (String deasciifier : deasciifiers) {
        fileName = "risk_sensitive_gdeval_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        System.out.print(getMetric(metric, outputPath + fileName));

        i++;

        if (i == deasciifiers.length)
          System.out.print(" & ");
        else
          System.out.print(" \\\\ ");

      }

      System.out.println();
      System.out.println("\\hline");
    }
    System.out.println("-------------------------------------------------------------");
  }

  static String getMetric(Metric metric, String fileName) throws IOException {
    List<String> lines = Files.readAllLines(new File(fileName).toPath(), StandardCharsets.US_ASCII);

    for (String line : lines) {
      if (line.contains(",amean,")) {
        String[] parts = line.split("\\s*,");
        if (parts.length == 4) {
          if (Metric.ERR.equals(metric)) return parts[3];
          if (Metric.NCDG.equals(metric)) return parts[2];
        }
        if (parts.length == 6) {
          if (Metric.ERR.equals(metric)) return parts[5];
          if (Metric.NCDG.equals(metric)) return parts[4];
        }

        throw new RuntimeException("line should have four or six parts " + Arrays.toString(parts));

      }
    }

    throw new RuntimeException(metric + " metric cannot be found!");
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

  public static void main(String[] args) throws IOException {

    for (final QueryLength queryLength : new QueryLength[]{QueryLength.Short, QueryLength.Medium}) {
      printTrecEvalMetricTable("bpref", queryLength, "/Users/iorixxx/Dropbox/diacritic/");
      System.out.println("-------------------------------------------------------------");
    }
/*
    for (final QueryLength queryLength : new QueryLength[]{QueryLength.Short, QueryLength.Medium})
      for (final Metric metric : new Metric[]{Metric.ERR, Metric.NCDG})
        printGDEvalMetricTable(metric, queryLength, "/Users/iorixxx/Dropbox/diacritic/");


    printRiskTable(Metric.ERR, SolrSearcher.QueryLength.Short, "/Users/iorixxx/Dropbox/diacritic/");
    */
  }
}
