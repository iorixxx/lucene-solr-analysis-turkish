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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import info.bliki.wiki.dump.WikiXMLParser;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import net.zemberek.erisim.Zemberek;
import net.zemberek.islemler.cozumleme.CozumlemeSeviyesi;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import net.zemberek.yapi.Kelime;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.ApostropheFilter;
import org.apache.lucene.analysis.tr.TurkishDeasciifyFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * intrinsic evaluation of the different diacritics restoration systems
 * on a Turkish Wikipedia dump from January 2015.
 * Downloaded from http://dumps.wikimedia.org/trwiki/20150121/trwiki-20150121-pages-meta-current.xml.bz2
 */
public class IntrinsicEvaluator {

    static abstract class Deasciifier {

        int wrongTerms = 0;
        int numTerms = 0;

        public Deasciifier() {
            wrongTerms = numTerms = 0;
        }

        void incrementWrongTermCount() {
            wrongTerms++;
        }

        void incrementNumTerms() {
            numTerms++;
        }

        void printAccuracy() {
            printAccuracy(numTerms);
        }

        void printAccuracy(int numTerms) {
            if (numTerms != 0) {
                double errorRate = (double) wrongTerms / numTerms;
                errorRate *= 100.0;
                System.out.println(getName() + " : numTerms = " + numTerms + " wrong terms = " + wrongTerms + " error rate = " + errorRate + "%");
            }
        }

        abstract String deasciify(String asciiTerm);

        abstract String getName();
    }

    static class ZemberekDeasciifier extends Deasciifier {

        private final Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

        @Override
        public String deasciify(String asciiTerm) {

            Kelime[] kelimeler = zemberek.asciiCozumle(asciiTerm, CozumlemeSeviyesi.TUM_KOKLER);

            if (kelimeler.length == 0)
                return asciiTerm;
            else
                return kelimeler[0].icerikStr();
        }

        @Override
        public String getName() {
            return "zemberek";
        }
    }

    static class TurkishDeasciifier extends Deasciifier {

        @Override
        public String deasciify(String asciiTerm) {
            return TurkishDeasciifyFilter.convert_to_turkish(asciiTerm.toCharArray());
        }

        @Override
        public String getName() {
            return "turkish";
        }
    }

    /**
     * Filters {@link org.apache.lucene.analysis.standard.StandardTokenizer} with {@link org.apache.lucene.analysis.tr.ApostropheFilter} and {@link org.apache.lucene.analysis.tr.TurkishLowerCaseFilter}.
     */
    final static class TurkishAnalyzer extends Analyzer {

        @Override
        protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
            final ClassicTokenizer src = new ClassicTokenizer(Version.LUCENE_48, reader);
            src.setMaxTokenLength(255);
            TokenStream tok = new ClassicFilter(src);
            tok = new ApostropheFilter(tok);
            tok = new LengthFilter(Version.LUCENE_48, tok, 3, 255);
            tok = new TurkishLowerCaseFilter(tok);


            return new TokenStreamComponents(src, tok) {
                @Override
                protected void setReader(final Reader reader) throws IOException {
                    src.setMaxTokenLength(255);
                    super.setReader(reader);
                }
            };
        }
    }


    static int globalCounter = 0;
    static final WikiModel model = new WikiModel("", "");
    static final Deasciifier[] deasciifiers = new Deasciifier[]{new ZemberekDeasciifier(), new TurkishDeasciifier()};
    static final Map<String, Multiset<String>> collisions = new HashMap<>();
    static final Set<Character> accentedCharSet = new HashSet<>(SolrSearcher.TURKISH_CHARACTERS.length);

    static {

        for (int j = 0; j < SolrSearcher.TURKISH_CHARACTERS.length; j++)
            accentedCharSet.add(SolrSearcher.TURKISH_CHARACTERS[j]);

        //  for (int j = 0; j < SolrSearcher.ENGLISH_CHARACTERS.length; j++)
        //     accentedCharSet.add(SolrSearcher.ENGLISH_CHARACTERS[j]);
    }

    static boolean isComposedOfLetterOnly(final CharTermAttribute termAtt) {

        final char[] buffer = termAtt.buffer();
        final int length = termAtt.length();

        for (int i = 0; i < length; i++)
            if (!Character.isLetter(buffer[i])) return false;

        return true;

    }

    static boolean containsTurkishAccentedChar(final CharTermAttribute termAtt) {

        final char[] buffer = termAtt.buffer();
        final int length = termAtt.length();

        for (int i = 0; i < length; i++)
            if (accentedCharSet.contains(buffer[i])) return true;

        return false;

    }

    static void prune(Map<String, Multiset<String>> map, int minTF) {

        for (Map.Entry<String, Multiset<String>> entry : map.entrySet()) {
            Multiset<String> set = entry.getValue();

            Iterator<String> iterator = set.iterator();

            while (iterator.hasNext()) {

                String string = iterator.next();
                int count = set.count(string);

                if (count < minTF)
                    iterator.remove();

            }
        }
    }

    static class DemoArticleFilter implements IArticleFilter {

        @Override
        public void process(WikiArticle page, Siteinfo siteinfo) throws SAXException {

            String plainString = model.render(new PlainTextConverter(), page.toString());

            Analyzer analyzer = new TurkishAnalyzer();
            try (TokenStream ts = analyzer.tokenStream("field", new StringReader(plainString))) {

                final CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
                ts.reset(); // Resets this stream to the beginning. (Required)
                while (ts.incrementToken()) {

                    if (!isComposedOfLetterOnly(termAtt)) continue;

                    globalCounter++;

                    if (!containsTurkishAccentedChar(termAtt))
                        continue;


                    final String term = termAtt.toString();

                    final String asciiTerm = SolrSearcher.asciify(term);

                    for (Deasciifier deasciifier : deasciifiers) {

                        deasciifier.incrementNumTerms();

                        final String deasciiTerm = deasciifier.deasciify(asciiTerm);
                        if (!deasciiTerm.equals(term))
                            deasciifier.incrementWrongTermCount();
                    }

                    if (collisions.containsKey(asciiTerm))
                        collisions.get(asciiTerm).add(term);
                    else {
                        Multiset<String> list = HashMultiset.create();
                        list.add(term);
                        collisions.put(asciiTerm, list);
                    }

                    ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        // http://dumps.wikimedia.org/trwiki/20150121/trwiki-20150121-pages-meta-current.xml.bz2
        String bz2Filename = "/Users/iorixxx/trwiki-20150121-pages-meta-current.xml.bz2";

        try {
            IArticleFilter handler = new DemoArticleFilter();
            WikiXMLParser wxp = new WikiXMLParser(bz2Filename, handler);
            wxp.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        prune(collisions, 1);

        List<Multiset<String>> allTheLists = new ArrayList<>(collisions.values());
        Collections.sort(allTheLists, new Comparator<Multiset<String>>() {
            @Override
            public int compare(Multiset<String> a1, Multiset<String> a2) {
                // biggest to smallest
                return a2.elementSet().size() - a1.elementSet().size();
            }
        });

        for (Multiset<String> set : allTheLists)
            if (set.entrySet().size() > 1) {
                System.out.println(set);

            }

        for (Deasciifier deasciifier : deasciifiers) {
            deasciifier.printAccuracy();
        }

        System.out.println("Total number of words : " + globalCounter);
    }

}
