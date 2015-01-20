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
import net.zemberek.erisim.Zemberek;
import net.zemberek.islemler.cozumleme.CozumlemeSeviyesi;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import net.zemberek.yapi.Kelime;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.ApostropheFilter;
import org.apache.lucene.analysis.tr.TurkishDeasciifyFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * intrinsic evaluation of the different diacritics restoration systems
 */
public class IntrinsicEvaluator {

    static abstract class Deasciifier {

        int wrongTerms = 0;

        public Deasciifier() {
            wrongTerms = 0;
        }

        void incrementWrongTermCount() {
            wrongTerms++;
        }

        int getWrongTermCount() {
            return wrongTerms;
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
            final LetterTokenizer src = new LetterTokenizer(Version.LUCENE_48, reader);
            // src.setMaxTokenLength(255);
            TokenStream tok = new ClassicFilter(src);
            tok = new ApostropheFilter(tok);
            tok = new TurkishLowerCaseFilter(tok);


            return new TokenStreamComponents(src, tok) {
                @Override
                protected void setReader(final Reader reader) throws IOException {
                    //src.setMaxTokenLength(255);
                    super.setReader(reader);
                }
            };
        }
    }


    static List<Path> discoverTextFiles(Path p, String pattern) {

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
        final List<Path> txtFiles = new ArrayList<>();

        FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path name = file.getFileName();
                if (name != null && matcher.matches(name))
                    txtFiles.add(file);
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(p, fv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txtFiles;
    }

    static final Set<Character> accentedCharSet = new HashSet<>(SolrSearcher.TURKISH_CHARACTERS.length);

    static {

        for (int j = 0; j < SolrSearcher.TURKISH_CHARACTERS.length; j++)
            accentedCharSet.add(SolrSearcher.TURKISH_CHARACTERS[j]);

        for (int j = 0; j < SolrSearcher.ENGLISH_CHARACTERS.length; j++)
            accentedCharSet.add(SolrSearcher.ENGLISH_CHARACTERS[j]);
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

    public static void main(String[] args) throws IOException {

        Deasciifier[] deasciifiers = new Deasciifier[]{new ZemberekDeasciifier(), new TurkishDeasciifier()};

        Map<String, Multiset<String>> collisions = new HashMap<>();


        int numTerms = 0;
        int c = 0;

        for (Path path : discoverTextFiles(Paths.get("/Volumes/data/collection-20072011/"), "glob:*.txt")) {
            System.out.println("processing file : " + path);

            //   if (++c % 7 == 0) break;
            Analyzer analyzer = new TurkishAnalyzer();
            try (TokenStream ts = analyzer.tokenStream("field", Files.newBufferedReader(path, StandardCharsets.UTF_8))) {

                final CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
                ts.reset(); // Resets this stream to the beginning. (Required)
                while (ts.incrementToken()) {

                    if (!containsTurkishAccentedChar(termAtt))
                        continue;

                    numTerms++;

                    final String term = termAtt.toString();

                    final String asciiTerm = SolrSearcher.asciify(term);

                    for (Deasciifier deasciifier : deasciifiers) {
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

            }

        }


        prune(collisions, 1);

        List<Multiset<String>> allTheLists = new ArrayList<>(collisions.values());
        Collections.sort(allTheLists, new Comparator<Multiset<String>>() {
            public int compare(Multiset<String> a1, Multiset<String> a2) {
                return a2.elementSet().size() - a1.elementSet().size(); // assumes you want biggest to smallest
            }
        });

        for (int i = 0; i < allTheLists.size(); i++)
            if (allTheLists.get(i).entrySet().size() > 1) {
                System.out.println(allTheLists.get(i));

            }

        for (Deasciifier deasciifier : deasciifiers) {
            deasciifier.printAccuracy(numTerms);
        }
    }
}
