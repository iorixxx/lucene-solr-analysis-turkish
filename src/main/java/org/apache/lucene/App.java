package org.apache.lucene;

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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TRMorphStemFilter;
import org.apache.lucene.analysis.tr.TurkishDeasciifyFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    /**
     * Generate copy paste data for Maps
     * <a href="https://github.com/emres/turkish-deasciifier/blob/master/turkish/deasciifier.py">deasciifier.py</a>
     *
     * @throws IOException
     */
    static void generateStaticData() throws IOException {

        File file = new File("/Users/iorixxx/Downloads/turkish-deasciifier-master/turkish/deasciifier.py");
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        boolean start = false;
        for (String line : lines) {

            line = line.trim().replace("{", "").replaceAll("}", "");

            //      if(line.length()==0) continue;
            if (line.startsWith("u'i':")) {
                start = true;
                continue;
            }
            if (line.startsWith("u'i':")) break;

            if (start) {

                String[] parts = line.split(",");


                for (String part : parts) {
                    System.out.print("put(" + part.trim().replaceAll(":", ",") + "); ");
                }
                System.out.println();
            }


            if (line.startsWith("turkish_context_size = 10")) break;
        }
    }

    static void runTRMorph() throws IOException {

        String text = "Türkçe, Türk dili ya da Türkiye Türkçesi, batıda Balkanlar’dan doğuda Hazar Denizi sahasına kadar konuşulan Altay dillerinden biridir.";
        TokenStream ts = new StandardTokenizer(Version.LUCENE_47, new StringReader(text));
        ts = new TurkishLowerCaseFilter(ts);
        ts = new TRMorphStemFilter(ts, "/Volumes/data/foma/flookup Users/iorixxx/Desktop/stem.fst", "max");
        ts.reset();

        CharTermAttribute termAttribute = ts.getAttribute(CharTermAttribute.class);
        while (ts.incrementToken())
            System.out.println("--" + new String(termAttribute.buffer(), 0, termAttribute.length()));

        ts.close();
    }


    public static void main(String[] args) throws IOException {
        String input = "fadil akgunduz dogalgaz kus giribi";
        String output = TurkishDeasciifyFilter.convert_to_turkish(input.toCharArray());
        System.out.println(output);

        //generateStaticData();
        //runTRMorph();
    }
}
