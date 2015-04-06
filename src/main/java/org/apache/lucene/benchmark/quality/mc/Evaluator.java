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

import org.apache.commons.math3.stat.inference.TTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

import static org.apache.lucene.benchmark.quality.mc.SolrSearcher.*;


/**
 * Evaluation class processes trec_eval outputs
 */
public class Evaluator {

  private static final TTest T_TEST = new TTest();

  public enum Metric {
    NDCG, ERR
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
      System.out.print(getMetric(metric, Paths.get(outputPath, fileName)) + " & ");

      double[] baseline = getMetrics(metric, Paths.get(outputPath, fileName));

      fileName = "out_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      double[] ascii = getMetrics(metric, Paths.get(outputPath, fileName));
      System.out.print(getMetric(metric, Paths.get(outputPath, fileName)));

      if (T_TEST.pairedTTest(baseline, ascii, 0.05)) System.out.print("\\textsuperscript{\\dagger}");

      System.out.print(" & ");

      int i = 1;
      for (String deasciifier : deasciifiers) {
        fileName = "out_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        double[] deascii = getMetrics(metric, Paths.get(outputPath, fileName));
        System.out.print(getMetric(metric, Paths.get(outputPath, fileName)));
        if (T_TEST.pairedTTest(baseline, deascii, 0.05)) System.out.print("\\textsuperscript{\\dagger}");

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

  public static void printTTestData(String metric, SolrSearcher.QueryLength queryLength, String outputPath) throws IOException {


    for (String stemmer : stemmers) {

      if (stemmer.length() < 3)
        System.out.println(stemmer + " \t\t\t& ");
      else
        System.out.println(stemmer + " \t& ");

      String fileName = "out_" + "tr_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.println(Arrays.toString(getMetrics(metric, Paths.get(outputPath, fileName))).replace(",", "\t"));

      double[] baseline = getMetrics(metric, Paths.get(outputPath, fileName));


      fileName = "out_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.println(Arrays.toString(getMetrics(metric, Paths.get(outputPath, fileName))).replace(",", "\t"));

      double[] ascii = getMetrics(metric, Paths.get(outputPath, fileName));

      System.out.println("ascii " + T_TEST.pairedTTest(baseline, ascii, 0.05) + " " + T_TEST.pairedTTest(baseline, ascii));


      for (String deasciifier : deasciifiers) {
        fileName = "out_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        System.out.println(Arrays.toString(getMetrics(metric, Paths.get(outputPath, fileName))).replace(",", "\t"));

        double[] deascii = getMetrics(metric, Paths.get(outputPath, fileName));
        System.out.println(deasciifier + " " + T_TEST.pairedTTest(baseline, deascii, 0.05) + " " + T_TEST.pairedTTest(baseline, deascii));


      }

      System.out.println();

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
      System.out.print(getMetric(metric, Paths.get(outputPath, fileName)) + " & ");


      fileName = "gdeval_" + "ascii_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
      System.out.print(getMetric(metric, Paths.get(outputPath, fileName)) + " & ");

      int i = 1;
      for (String deasciifier : deasciifiers) {
        fileName = "gdeval_" + deasciifier + "_" + stemmer + "_" + queryLength.toString() + "_submitted.txt";
        System.out.print(getMetric(metric, Paths.get(outputPath, fileName)));

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
   * @param queryLength Medium, Short, etc.
   * @param outputPath  directory where gdeval.pl's outputs are saved.
   * @throws IOException
   */
  public static void printRiskTable(SolrSearcher.QueryLength queryLength, String outputPath) throws IOException {

    LinkedHashMap<String, List<Double>> map = new LinkedHashMap<>();
    for (String stemmer : stemmers) {

      String runName = "tr_" + stemmer + "_" + queryLength.toString();

      List<Double> list = new LinkedList<>();
      list.add(Double.valueOf(getMetric(Metric.NDCG, Paths.get(outputPath, "gdeval_" + runName + "_submitted.txt"))));
      list.add(Double.valueOf(getMetric(Metric.ERR, Paths.get(outputPath, "gdeval_" + runName + "_submitted.txt"))));
      list.add(Double.NaN);
      list.add(Double.NaN);
      list.add(Double.NaN);
      list.add(Double.NaN);

      map.put(runName, list);


      runName = "ascii_" + stemmer + "_" + queryLength.toString();

      list = new LinkedList<>();
      list.add(Double.valueOf(getMetric(Metric.NDCG, Paths.get(outputPath, "gdeval_" + runName + "_submitted.txt"))));
      list.add(Double.valueOf(getMetric(Metric.ERR, Paths.get(outputPath, "gdeval_" + runName + "_submitted.txt"))));

      for (int alpha = 1; alpha <= 5; alpha = alpha + 4) {
        list.add(Double.valueOf(getMetric(Metric.NDCG, Paths.get(outputPath, "risk_sensitive_" + alpha + "_" + runName + "_submitted.txt"))));
        list.add(Double.valueOf(getMetric(Metric.ERR, Paths.get(outputPath, "risk_sensitive_" + alpha + "_" + runName + "_submitted.txt"))));
      }

      //   System.out.print(getMetric(metric, outputPath + "risk_sensitive_gdeval_" + runName + "_submitted.txt") + " & ");


      map.put(runName, list);

      for (String deasciifier : deasciifiers) {
        runName = deasciifier + "_" + stemmer + "_" + queryLength.toString();

        list = new LinkedList<>();
        list.add(Double.valueOf(getMetric(Metric.NDCG, Paths.get(outputPath, "gdeval_" + runName + "_submitted.txt"))));
        list.add(Double.valueOf(getMetric(Metric.ERR, Paths.get(outputPath, "gdeval_" + runName + "_submitted.txt"))));

        for (int alpha = 1; alpha <= 5; alpha = alpha + 4) {
          list.add(Double.valueOf(getMetric(Metric.NDCG, Paths.get(outputPath, "risk_sensitive_" + alpha + "_" + runName + "_submitted.txt"))));
          list.add(Double.valueOf(getMetric(Metric.ERR, Paths.get(outputPath,  "risk_sensitive_" + alpha + "_" + runName + "_submitted.txt"))));
        }

//          System.out.print(getMetric(metric, outputPath + "risk_sensitive_gdeval_" + runName + "_submitted.txt"));

        map.put(runName, list);
      }
    }

    DecimalFormat df = new DecimalFormat("#0.00000");
    int i = 0;
    for (Map.Entry<String, List<Double>> entry : map.entrySet()) {

      System.out.print(entry.getKey().replace("_", "\\_") + " \t & ");
      String row = "";
      //List<Double> list = entry.getValue();
      int j = 0;
      for (Double d : entry.getValue()) {

        if (d.isNaN())
          row += " *  & ";
        else {
          boolean isMax = isMax(i, j, map, d);

          if (isMax && !entry.getKey().startsWith("tr_"))
            if (d == 0)
              row += "\\textbf{0} & ";
            else
              row += ("\\textbf{" + df.format(d) + "} & ");
          else
            row += (df.format(d) + " & ");
        }
        j++;
      }

      row = row.substring(0, row.length() - 3);
      System.out.println(row + " \\\\ ");

      i++;
      if (i % 4 == 0) {
        System.out.println("\\hline");
        System.out.println("\\hline");
      }
      else
        System.out.println();
    }
  }

  static boolean isMax(final int i, int j, LinkedHashMap<String, List<Double>> map, Double d) {
    String stemString = null;
    if (i / 4 == 0) stemString = "_ns_";
    if (i / 4 == 1) stemString = "_f5_";
    if (i / 4 == 2) stemString = "_snowball_";
    if (i / 4 == 3) stemString = "_zemberek2_";

    //StringBuilder builder = new StringBuilder();
    //builder.append("d = " + d + " stem = " + stemString + " ");

    if (stemString == null) throw new RuntimeException("i = " + i);

    for (Map.Entry<String, List<Double>> entry : map.entrySet()) {

      String run = entry.getKey();

      if(run.startsWith("tr_")) continue;

      if (run.contains(stemString)) {
        Double v = entry.getValue().get(j);

        //builder.append(v).append( " ");

        if(v.isNaN()) continue;

        if (v > d) return false;
      }
    }

   // System.out.println( "====" + builder.toString() + " j = " + j + " i = " + i )  ;
    return true;
  }

  static String getMetric(Metric metric, Path fileName) throws IOException {
    List<String> lines = Files.readAllLines(fileName, StandardCharsets.US_ASCII);

    for (String line : lines) {
      if (line.contains(",amean,")) {
        String[] parts = line.split("\\s*,");
        if (parts.length == 4) {
          if (Metric.ERR.equals(metric)) return parts[3];
          if (Metric.NDCG.equals(metric)) return parts[2];
        }
        if (parts.length == 6) {
          if (Metric.ERR.equals(metric)) return parts[5];
          if (Metric.NDCG.equals(metric)) return parts[4];
        }

        throw new RuntimeException("line should have four or six parts " + Arrays.toString(parts));

      }
    }

    throw new RuntimeException(metric + " metric cannot be found!");
  }

  static double[] getMetrics(String metric, Path fileName) throws IOException {

    double results[] = new double[72];

    List<String> lines = Files.readAllLines(fileName, StandardCharsets.US_ASCII);

    int i = 0;

    for (String line : lines) {
      if (line.startsWith(metric) && !line.contains("all")) {
        String[] parts = line.split("\\s+");
        if (parts.length != 3) throw new RuntimeException("line should have three parts " + Arrays.toString(parts));
        results[i++] = Double.parseDouble(parts[2]);
      }
    }
    return results;
  }

  static String getMetric(String metric, Path fileName) throws IOException {
    List<String> lines = Files.readAllLines(fileName, StandardCharsets.US_ASCII);

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

    for(final String core: new String []{"catA", "catB"})
    for (final QueryLength queryLength : new QueryLength[]{QueryLength.Medium, QueryLength.Short}) {
      System.out.println("Category " + core.charAt(3) + " evaluation results in bpref values for " + queryLength );
      printTrecEvalMetricTable("bpref", queryLength, "/Volumes/data/diacritics/evals/" + core );
      System.out.println("-------------------------------------------------------------");
    }


      for(final String core: new String []{"catA", "catB"}) {
          System.out.println("Category " + core.charAt(3) + " results of NDCG@20 and ERR@20 and their URisk equivalents for alpha=1 and alpha=5" );
          System.out.println("-------------------------------------------------------------");
          for (final QueryLength queryLength : new QueryLength[]{QueryLength.Medium, QueryLength.Short}) {

              printRiskTable(queryLength, "/Volumes/data/diacritics/evals/" + core);
              System.out.println("\\hline");
          }
      }
  }
}
