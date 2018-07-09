package org.apache.lucene.analysis.tr;

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
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.TurkishMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Factory for {@link Zemberek3StemFilter}.
 * <pre class="prettyprint">
 * &lt;fieldType name="zemberek3" class="solr.TextField" positionIncrementGap="100"&gt;
 * &lt;analyzer&gt;
 * &lt;tokenizer class="solr.StandardTokenizerFactory"/&gt;
 * &lt;filter class="solr.ApostropheFilterFactory"/&gt;
 * &lt;filter class="solr.TurkishLowerCaseFilterFactory"/&gt;
 * &lt;filter class="solr.Zemberek3StemFilterFactory" strategy="maxLength" dictionary="master-dictionary.dict,secondary-dictionary.dict,non-tdk.dict,proper.dict"/&gt;
 * &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class Zemberek3StemFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    private TurkishMorphology morphology;

    private final String strategy;
    private final String dictionaryFiles;

    public Zemberek3StemFilterFactory(Map<String, String> args) {
        super(args);
        dictionaryFiles = get(args, "dictionary");
        strategy = get(args, "strategy", "maxLength");

        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {

        if (dictionaryFiles == null || dictionaryFiles.trim().isEmpty()) {
            this.morphology = TurkishMorphology.createWithDefaults();
            // Use default dictionaries shipped with Zemberek3.
            return;
        }
        List<String> lines = new ArrayList<>();

        List<String> files = splitFileNames(dictionaryFiles);
        if (files.size() > 0) {
            for (String file : files) {
                List<String> wlist = getLines(loader, file.trim());
                lines.addAll(wlist);
            }
        }

        if (lines.isEmpty()) {
            this.morphology = TurkishMorphology.createWithDefaults();
            // Use default dictionaries shipped with Zemberek3.
            return;
        }


        String[] linesArray = new String[lines.size()];
        linesArray = lines.toArray(linesArray);
        morphology = (new TurkishMorphology.Builder()).addDictionaryLines(linesArray).build();


    }

    @Override
    public TokenStream create(TokenStream input) {
        return new Zemberek3StemFilter(input, morphology, strategy);
    }

    private static void parse(String word, TurkishMorphology morphology) {

        WordAnalysis results = morphology.analyze(word);
        System.out.println("Word = " + word + " has " + results.analysisCount() + " many solutions");

        if (results.analysisCount() == 0) return;

        System.out.println("Parses: ");

        for (SingleAnalysis result : results) {
            System.out.println("number of morphemes = " + result.getMorphemeDataList().size()) ;
            System.out.println(result.formatLong());
            System.out.println("\tStems = " + result.getStems());
            System.out.println("\tLemmas = " + result.getLemmas());
            System.out.println("\tStemAndEnding = " + result.getStemAndEnding());
            System.out.println("-------------------");
        }

        System.out.println("final selected stem : " + Zemberek3StemFilter.stem(results, "maxLength"));
        System.out.println("==================================");
    }

    public static void main(String[] args) throws IOException {

        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();


        String a = "kuş asisi ortaklar çekişme masalı İCARETİN DE ARTMASI BEKLENİYOR\n" +
                "Savinykh, Ege Bölgesi Sanayi Odası'nda (EBSO) düzenlenen \"Belarus Türkiye Yatırım ve İşbirliği Olanakları Semineri\"nde yaptığı konuşmada, \" 2 Haziran'dan itibaren Türk halkı vizesiz olarak Belarus'a gidip gelebilecek. İki ülke arasındaki ticaret bu anlaşma ile daha da artacak\" dedi. Türkiye ile Belarus arasında ticari, kültürel ve sosyal ilişkilerin gelişmesini arzu ettiklerini kaydeden Andrei Savinykh, ülkesinin Kırgızistan ve Kazakistan ile Gümrük Birliği anlaşması bulunduğunu, önümüzdeki kuku birliğ";

        a = a.toLowerCase(Locale.forLanguageTag("tr"));

        for (String s : a.split("\\s+")) {
            parse(s, morphology);
        }

    }
}